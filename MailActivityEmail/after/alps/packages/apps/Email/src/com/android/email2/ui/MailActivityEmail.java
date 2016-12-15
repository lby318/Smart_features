/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.email2.ui;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.android.email.Preferences;
import com.android.email.provider.EmailProvider;
import com.android.email.service.AttachmentService;
import com.android.email.service.EmailServiceUtils;
import com.android.emailcommon.Logging;
import com.android.emailcommon.TempDirectory;
import com.android.emailcommon.provider.EmailContent.Message;
import com.android.emailcommon.provider.Mailbox;
import com.android.emailcommon.utility.DataCollectUtils;
import com.android.emailcommon.utility.IntentUtilities;
import com.android.mail.providers.Conversation;
import com.android.mail.providers.Folder;
import com.android.mail.providers.UIProvider;
import com.android.mail.ui.ActivityController;
import com.android.mail.ui.MailActivity;
import com.android.mail.ui.ViewMode;
import com.android.mail.utils.LogTag;
import com.android.mail.utils.LogUtils;
import com.android.mail.utils.PDebug;
import com.android.mail.utils.Utils;
//jianbo.ge add 20161110 for jria COPPLE-31@{
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;
import java.util.List;
import java.util.ArrayList;
//add end}
public class MailActivityEmail extends com.android.mail.ui.MailActivity {

    public static final String LOG_TAG = LogTag.getLogTag();

    private static final int MATCH_LEGACY_SHORTCUT_INTENT = 1;
	       	   //jianbo.ge add for  20161110@{
    private static final int MY_PERMISSIONS_REQUEST_READ_ATTACHMENT = 101;
    private boolean mAllGranted = false;
    private boolean mPermRequesting = false;
    private static final String [] sPermissionsNeeded = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE};
       //jianbo.ge  add end 
    /**
     * A matcher for data URI's that specify conversation list info.
     */
    private static final UriMatcher sUrlMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUrlMatcher.addURI(
                EmailProvider.LEGACY_AUTHORITY, "view/mailbox", MATCH_LEGACY_SHORTCUT_INTENT);
    }


    /**
     * M: Create our own EmailActivity's controller
     */
    @Override
    protected ActivityController createActivityController(MailActivity activity, ViewMode viewMode,
            boolean tabletUi) {
        return ControllerFactoryEmail.forActivity(activity, viewMode, tabletUi);
    }

    @Override
    public void onCreate(Bundle bundle) {
        PDebug.Start("MailActivityEmail.onCreate");
        final Intent intent = getIntent();
        final Uri data = intent != null ? intent.getData() : null;
        if (data != null) {
            final int match = sUrlMatcher.match(data);
            switch (match) {
                case MATCH_LEGACY_SHORTCUT_INTENT: {
                    final long mailboxId = IntentUtilities.getMailboxIdFromIntent(intent);
                    final Mailbox mailbox = Mailbox.restoreMailboxWithId(this, mailboxId);
                    if (mailbox == null) {
                        LogUtils.e(LOG_TAG, "unable to restore mailbox");
                        break;
                    }

                    /// M: Open specified message if message id is included. @{
                    final long messageId = IntentUtilities.getMessageIdFromIntent(intent);
                    LogUtils.d(LOG_TAG, "Get messageId: %s from the intent", messageId);
                    /// @}

                    final Intent viewIntent = getViewIntent(mailbox.mAccountKey, mailboxId, messageId);
                    if (viewIntent != null) {
                        setIntent(viewIntent);
                    }
                    break;
                }
            }
        }

        super.onCreate(bundle);
        TempDirectory.setTempDirectory(this);

        // Make sure all required services are running when the app is started (can prevent
        // issues after an adb sync/install)
        EmailProvider.setServicesEnabledAsync(this);
        PDebug.End("MailActivityEmail.onCreate");
    }

    /**
     * Internal, utility method for logging.
     * The calls to log() must be guarded with "if (Email.LOGD)" for performance reasons.
     */
    public static void log(String message) {
        LogUtils.d(Logging.LOG_TAG, message);
    }

    private Intent getViewIntent(long accountId, long mailboxId, long messageId) {
        final ContentResolver contentResolver = getContentResolver();

        final Cursor accountCursor = contentResolver.query(
                EmailProvider.uiUri("uiaccount", accountId),
                UIProvider.ACCOUNTS_PROJECTION_NO_CAPABILITIES,
                null, null, null);

        if (accountCursor == null) {
            LogUtils.e(LOG_TAG, "Null account cursor for mAccountId %d", accountId);
            return null;
        }

        com.android.mail.providers.Account account = null;
        try {
            if (accountCursor.moveToFirst()) {
                account = com.android.mail.providers.Account.builder().buildFrom(accountCursor);
            }
        } finally {
            accountCursor.close();
        }


        final Cursor folderCursor = contentResolver.query(
                EmailProvider.uiUri("uifolder", mailboxId),
                UIProvider.FOLDERS_PROJECTION, null, null, null);

        if (folderCursor == null) {
            LogUtils.e(LOG_TAG, "Null folder cursor for account %d, mailbox %d",
                    accountId, mailboxId);
            return null;
        }

        Folder folder = null;
        try {
            if (folderCursor.moveToFirst()) {
                folder = new Folder(folderCursor);
            } else {
                LogUtils.e(LOG_TAG, "Empty folder cursor for account %d, mailbox %d",
                        accountId, mailboxId);
                return null;
            }
        } finally {
            folderCursor.close();
        }

        Intent intent = Utils.createViewFolderIntent(this, folder.folderUri.fullUri, account);

        /**
         * M: If intent is for view mail(conversation), Create a view mail intent
         * instead of a view folder intent @{
         */
        if (messageId != Message.NO_MESSAGE) {
            // we used uimessages not uiconversation cause we need the value of rawfolders.
            Cursor cursor = getContentResolver().query(EmailProvider.uiUri("uimessages", folder.id),
                    UIProvider.CONVERSATION_PROJECTION, null, null, null);
            if (cursor == null) {
                LogUtils.e(LOG_TAG, "Get conversation cursor failed");
                return intent;
            }
            Conversation conversation = null;
            try {
                while (cursor.moveToNext()) {
                    if (cursor.getLong(UIProvider.CONVERSATION_ID_COLUMN) == messageId) {
                        LogUtils.d(LOG_TAG, "Get the conversation : %d", messageId);
                        conversation = new Conversation(cursor);
                        break;
                    }
                }
            } finally {
                cursor.close();
            }
            if (conversation != null) {
                intent = Utils.createViewConversationIntent(this, conversation, folder.folderUri.fullUri, account);
            }
        }
        /** @}*/

        return intent;
    }
        	//jianbo.ge add  20161110 for jria BLUFZTT-31
	@Override
    public void onRequestPermissionsResult(int requestCode,
            String[] permissions, int[] grantResults) {
        mPermRequesting = false;
        switch (requestCode) {
        case MY_PERMISSIONS_REQUEST_READ_ATTACHMENT:
            if (grantResults != null && grantResults.length > 0) {
                boolean isGranted = true;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        isGranted = false;
                        break;
                    }
                }
                if (!isGranted) {
                    String toastStr = getString(com.mediatek.internal.R.string.denied_required_permission); 
                    Toast.makeText(getApplicationContext(), toastStr, Toast.LENGTH_LONG).show();
                    finish();
                } else {
					
                    mAllGranted = true;
                }
            }
            break;
        default:
            super.onRequestPermissionsResult(requestCode, permissions,
                    grantResults);
            break;
        }
    }
	
    
	private void CheckPermission(){
        if (!mAllGranted & !mPermRequesting) {
            List<String> permissionsRequestList = new ArrayList<String>();
            for (int i = 0; i < sPermissionsNeeded.length; i++) {
                if (checkSelfPermission(sPermissionsNeeded[i]) !=
                        PackageManager.PERMISSION_GRANTED) {
                    permissionsRequestList.add(sPermissionsNeeded[i]);
                }
            }
            if (permissionsRequestList.size() > 0) {
                requestPermissions(
                        permissionsRequestList.toArray(new String[permissionsRequestList.size()]),
                        MY_PERMISSIONS_REQUEST_READ_ATTACHMENT);
                mPermRequesting = true;
            } else {
				
                mAllGranted = true;
            }
        }
    }
	// add end 
    @Override
    public void onResume() {
        PDebug.Start("MailActivityEmail.onResume");
        com.android.mail.providers.Account acct = getAccountController().getAccount();
        if (acct != null) {
            /** M: start record the account using as well as reset the flags @{ */
            DataCollectUtils.startRecord(this, Long.parseLong(acct.uri.getLastPathSegment()), sRecordOpening);
            sRecordOpening = true;
            sEmailActivityResumed = true;
            /** @} */
        }
        super.onResume();
        PDebug.End("MailActivityEmail.onResume");
		CheckPermission(); //jiano.ge add 20161110 for jria BLUFZTT-31
    }

    @Override
    public void onPause() {
        /** M: stop the account using recording @{ */
        DataCollectUtils.stopRecord(this);
        // This is the situation that user back to home screen,
        // so clear the recorded list. We do not record the opening again when
        // just launching other activities (sRecordOpening is false), or just
        // the "remote search" EmailActivity paused.
        if (sRecordOpening
                && getIntent() != null
                && !Intent.ACTION_SEARCH.equalsIgnoreCase(getIntent().getAction())) {
            DataCollectUtils.clearRecordedList();
        }
        /** @} */
        super.onPause();
    }
}
