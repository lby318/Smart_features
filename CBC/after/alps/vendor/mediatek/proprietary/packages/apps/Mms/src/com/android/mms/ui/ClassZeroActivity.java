/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Inbox;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.transaction.MessagingNotification;

import android.database.sqlite.SqliteWrapper;

/// M:
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony.Sms.Intents;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.telephony.PhoneConstants;
import com.android.mms.util.Recycler;
import com.android.mms.widget.MmsWidgetProvider;
import com.android.mms.util.MmsLog;
import com.mediatek.mms.ext.IOpClassZeroActivityExt;
import com.mediatek.mms.util.PermissionCheckUtil;
import com.mediatek.mms.callback.IClassZeroActivityCallback;
import com.mediatek.opmsg.util.OpMessageUtils;

import android.provider.Telephony;
//Moka add for Azumi CL CBC
import android.view.WindowManager;
import android.media.Ringtone;
import android.media.AudioSystem;
import com.mediatek.audioprofile.AudioProfileManager.Scenario;
import android.os.Vibrator;
import android.app.Activity;
import java.text.SimpleDateFormat;
import android.text.format.DateFormat;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;  
import android.content.IntentFilter; 
import android.content.BroadcastReceiver;
//Moka add for Azumi Cl CBC
/**
 * Display a class-zero SMS message to the user. Wait for the user to dismiss
 * it.
 */
public class ClassZeroActivity extends Activity implements IClassZeroActivityCallback {
    private static final String BUFFER = "         ";
    private static final int BUFFER_OFFSET = BUFFER.length() * 2;
    private static final String TAG = "display_00";
    private static final int ON_AUTO_SAVE = 1;

    private static final int SAVE_WITHOUT_DESTROY_ACTIVITY = 2;
    private static final String[] REPLACE_PROJECTION = new String[] { Sms._ID,
            Sms.ADDRESS, Sms.PROTOCOL };
    private static final int REPLACE_COLUMN_ID = 0;

    /** Default timer to dismiss the dialog. */
    private static final long DEFAULT_TIMER = 5 * 60 * 1000;

    /** To remember the exact time when the timer should fire. */
    private static final String TIMER_FIRE = "timer_fire";

    /// M: Code analyze 001, new feature, we want to show many messages on
    // ClassZeroActivity. @{
    private ArrayList<MessageContent> mMessageQueue;
    private SmsMessage[] mMessages = null;
    private int mMsgLen = 0;
	//Moka add for Azumi CL CBC
    private String mTitle;
    private String mMessage;
    private Ringtone r;
    private Vibrator mVibrator;
	//Moka add for Azumi CL CBC
    /// @}
    /** Is the message read. */
    private boolean mRead = false;

    /** The timer to dismiss the dialog automatically. */
    private long mTimerSet = 0;
    private AlertDialog mDialog = null;

    private IOpClassZeroActivityExt mOpClassZeroActivityExt;
    private IntentFilter intentFilter;  
    private CBCRingtomeStopReceiver cbcRingtoneReceiver;	

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // Do not handle an invalid message.
            mOpClassZeroActivityExt.handleMessage(msg, this, mRead, ClassZeroActivity.this,
                    ClassZeroActivity.this);
            if (msg.what == ON_AUTO_SAVE) {
                mDialog.dismiss();
                saveMessage();
            }
            mRead = false;
            processNextMessage();
        }
    };

    private boolean queueMsgFromIntent(Intent msgIntent) {
        SmsMessage[] message = Intents.getMessagesFromIntent(msgIntent);
        if (TextUtils.isEmpty(message[0].getMessageBody())) {
            if (mMessageQueue.size() == 0) {
                finish();
            }
            return false;
        }
        MessageContent messageContent = new MessageContent(msgIntent);
        mMessageQueue.add(messageContent);
        return true;
    }

    private void processNextMessage() {
        if (mMessageQueue.isEmpty()) {
            finish();
            return;
        }
        mMessageQueue.remove(0);
        if (mMessageQueue.size() == 0) {
            finish();
        } else {
            displayZeroMessage(mMessageQueue.get(0));
        }
    }

    private void saveMessage() {
        Uri messageUri = null;
        if (mMessages[0].isReplace()) {
            messageUri = replaceMessage(mMessages);
        } else {
            messageUri = storeMessage(mMessages);
        }
        if (!mRead && messageUri != null) {
            MessagingNotification.nonBlockingUpdateNewMessageIndicator(
                    this,
                    MessagingNotification.THREAD_ALL,   // always notify on class-zero msgs
                    false);
        }
        /// M: Code analyze 002, unknown. @{
        cancelMessageNotification();
        Recycler.getSmsRecycler().deleteOldMessages(getApplicationContext());
        /// M: fix bug ALPS00379747, update mms widget after save class0 msg @{
        MmsWidgetProvider.notifyDatasetChanged(getApplicationContext());
        /// @}
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.d(TAG, "onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(
                R.drawable.class_zero_background);

	//Moka add for Azumi CL CBC
	Intent intent = getIntent();
	mTitle = intent.getStringExtra("com.android.mms.cbs.title");
	mMessage = intent.getStringExtra("com.android.mms.cbs.message");

	if((mTitle != null) || (mMessage != null)){
	   final Window win = getWindow();
           win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);    
	   displayZeroMessage();
	}else{
        if (mMessageQueue == null) {
            mMessageQueue = new ArrayList<MessageContent>();
        }
        mOpClassZeroActivityExt = OpMessageUtils.getOpMessagePlugin().getOpClassZeroActivityExt();

        if (!queueMsgFromIntent(getIntent())) {
            return;
        }

        if (mMessageQueue.size() == 1) {
            displayZeroMessage(mMessageQueue.get(0));
        }

        if (icicle != null) {
            mTimerSet = icicle.getLong(TIMER_FIRE, mTimerSet);
        }
	}
	//Moka add for Azumi CL CBC
//jianqiu add 20161208 
	intentFilter = new IntentFilter();
	intentFilter.addAction("CBC_RINGTONG_STOP");
	cbcRingtoneReceiver = new CBCRingtomeStopReceiver();
	registerReceiver(cbcRingtoneReceiver, intentFilter); 
    }

//jianqiu add 20161208 
    class CBCRingtomeStopReceiver extends BroadcastReceiver{  
        @Override  
        public void onReceive(Context context, Intent intent){  
		if(r!= null){
		   r.stop();
		   r = null;
		}
		if(mVibrator !=null){
		   mVibrator.cancel();
		   mVibrator = null;
		}              
    }  
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        /// M: Extract method
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(TIMER_FIRE, mTimerSet);
        if (false) {
            Log.d(TAG, "onSaveInstanceState time = " + Long.toString(mTimerSet)
                    + " " + this.toString());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent" + intent);
        queueMsgFromIntent(intent);
        // Remove timer for previous message and save.
        Message msg = mHandler.obtainMessage(SAVE_WITHOUT_DESTROY_ACTIVITY);
        msg.obj = mMessages;
        mHandler.sendMessage(msg);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        if (false) {
            Log.d(TAG, "onStop time = " + Long.toString(mTimerSet)
                    + " " + this.toString());
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
		//Moka add for Azumi CL CBC
	    if((mTitle == null) && (mMessage == null)){
	        mHandler.removeMessages(ON_AUTO_SAVE);
	    }else{
		if(r!= null){
		   r.stop();
		   r = null;
		}
		if(mVibrator !=null){
		   mVibrator.cancel();
		   mVibrator = null;
		}
    }
	//Moka add for Azumi CL CBC
        super.onDestroy();
	//jianqiu add 20161208 
	unregisterReceiver(cbcRingtoneReceiver); 
    }

    private final OnClickListener mCancelListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            /// M: Code analyze 004, unknown, @{
		//Moka add for Azumi CL CBC
	   if((mTitle == null) && (mMessage == null)){
            cancelMessageNotification();
	   }
            dialog.dismiss();
	   if((mTitle == null) && (mMessage == null)){
            mHandler.removeMessages(ON_AUTO_SAVE);
            processNextMessage();
	   }else{
	      finish();
	   }
		//Moka add for Azumi CL CBC
        }
    };

    private final OnClickListener mSaveListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            mRead = true;
            /// M: Code analyze 005, For fix bug ALPS00260304, while
            // downloading APP from playstore, there are UIthread Database
            // blocking problem. @{
            new Thread(new Runnable() {
                public void run() {
                    saveMessage();
                }
            }).start();
            /// @}
            mHandler.removeMessages(ON_AUTO_SAVE);
            dialog.dismiss();
            processNextMessage();
        }
    };

    private ContentValues extractContentValues(SmsMessage sms) {
        // Store the message in the content provider.
        ContentValues values = new ContentValues();

        values.put(Inbox.ADDRESS, sms.getDisplayOriginatingAddress());

        // Use now for the timestamp to avoid confusion with clock
        // drift between the handset and the SMSC.
        values.put(Inbox.DATE, new Long(System.currentTimeMillis()));
        values.put(Inbox.PROTOCOL, sms.getProtocolIdentifier());
        values.put(Inbox.READ, Integer.valueOf(mRead ? 1 : 0));
        values.put(Inbox.SEEN, Integer.valueOf(mRead ? 1 : 0));
        /// M: Code analyze 006, new feature, add for msim. @{
        // MR1 need fix
        values.put(Telephony.Sms.Inbox.SUBSCRIPTION_ID, sms.getSubId());
        /// @}
        if (sms.getPseudoSubject().length() > 0) {
            values.put(Inbox.SUBJECT, sms.getPseudoSubject());
        }
        values.put(Inbox.REPLY_PATH_PRESENT, sms.isReplyPathPresent() ? 1 : 0);
        values.put(Inbox.SERVICE_CENTER, sms.getServiceCenterAddress());
        return values;
    }

    public Uri replaceMessage(SmsMessage[] Msgs) {
        ContentValues values = extractContentValues(Msgs[0]);

        /// M: Code analyze 001, new feature, we want to show many messages on
        // ClassZeroActivity. @{
        String body = "";
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < mMsgLen; ++i) {
            buf.append(Msgs[i].getMessageBody());
        }
        body  = buf.toString();


        values.put(Inbox.BODY, body);
        /// @}

        ContentResolver resolver = getContentResolver();
        String originatingAddress = Msgs[0].getOriginatingAddress();
        int protocolIdentifier = Msgs[0].getProtocolIdentifier();
        String selection = Sms.ADDRESS + " = ? AND " + Sms.PROTOCOL + " = ?";
        String[] selectionArgs = null;
        /// M: Code analyze 006, new feature, add for msim. @{
        selection += " AND " + Telephony.Sms.SUBSCRIPTION_ID + " = ?";
        selectionArgs = new String[] {
                originatingAddress, Integer.toString(protocolIdentifier),
                Long.toString(Msgs[0].getSubId())
        };
        /// @}

        Cursor cursor = SqliteWrapper.query(this, resolver, Inbox.CONTENT_URI,
                REPLACE_PROJECTION, selection, selectionArgs, null);

        try {
            if (cursor.moveToFirst()) {
                long messageId = cursor.getLong(REPLACE_COLUMN_ID);
                Uri messageUri = ContentUris.withAppendedId(
                        Sms.CONTENT_URI, messageId);

                SqliteWrapper.update(this, resolver, messageUri, values,
                        null, null);
                return messageUri;
            }
        } finally {
            cursor.close();
        }
        return storeMessage(Msgs);
    }

    public Uri storeMessage(SmsMessage[] Msgs) {
        // Store the message in the content provider.
        ContentValues values = extractContentValues(Msgs[0]);
        /// M: Code analyze 001, new feature, we want to show many messages on
        // ClassZeroActivity. @{
        String body = "";
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < mMsgLen; ++i) {
            buf.append(Msgs[i].getDisplayMessageBody());
        }
        body  = buf.toString();

        values.put(Inbox.BODY, body);
        /// @}
        ContentResolver resolver = getContentResolver();
        if (false) {
            Log.d(TAG, "storeMessage " + this.toString());
        }
        return SqliteWrapper.insert(this, resolver, Inbox.CONTENT_URI, values);
    }


    /// M: Code analyze 004, unknown, @{
    public void cancelMessageNotification() {
       MessagingNotification.cancelNotification(this,
             MessagingNotification.CLASS_ZERO_NOTIFICATION_ID);
    }

    private void displayZeroMessage(MessageContent messageContent) {
        /// M: Code analyze 001, new feature, we want to show many messages on
        // ClassZeroActivity. @{
        // byte[] pdu = intent.getByteArrayExtra("pdu");
        // String format = intent.getStringExtra("format");
        //mMessages = SmsMessage.createFromPdu(pdu, format);
        int subId = 0;

        if (messageContent != null) {
            mMessages = messageContent.mMessages;
            subId = messageContent.mSubId;
        }


        mMsgLen = mMessages.length;
        CharSequence messageChars = null;
        if (1 == mMsgLen) {
            messageChars = mMessages[0].getMessageBody();
        } else if (mMsgLen > 1) {
            /// M: Build up the body from the parts.
            StringBuilder body = new StringBuilder();
            SmsMessage sms = null;
            for (int i = 0; i < mMsgLen; i++) {
                sms = mMessages[i];
                body.append(sms.getDisplayMessageBody());
            }
            messageChars = body.toString().subSequence(0, body.toString().length());
        }
        /// @}
        ///M: add for alps00448939 @{
        String message = null;
        if (messageChars != null) {
            message = messageChars.toString();
        }
        /// @}
        /// M: @{
        //if (TextUtils.isEmpty(message)) {
        //    finish();
        //    return;
        //}
        //// @}

        /// M: Code analyze 001, new feature, we want to show many messages on
        // ClassZeroActivity. @{
        LayoutInflater inflater = LayoutInflater.from(this);
        ClassZeroView classZeroView = (ClassZeroView) inflater.inflate(
                R.layout.class_zero_view_gemini, null, false);
        classZeroView.bind(message, subId);

        mOpClassZeroActivityExt.displayZeroMessage(mDialog);

        mDialog = new AlertDialog.Builder(this, R.style.SmsAlertDialog)
                .setTitle(R.string.new_message).setPositiveButton(R.string.save, mSaveListener)
                .setNegativeButton(android.R.string.cancel, mCancelListener).setCancelable(false)
                .setView(classZeroView).show();
        /// @}
        long now = SystemClock.uptimeMillis();
        mTimerSet = now + DEFAULT_TIMER;
        setTimeToSave();
    }
   //Moka add for Auzmi CL CBC
   @Override
    public void onUserLeaveHint() {
        finish();
        super.onUserLeaveHint();
    }

//jianqiu add 20161208 
	public void sendUpdateBroadcast(){
		int delayInMs = 60 * 1000 * 3;
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent("CBC_RINGTONG_STOP"); 
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delayInMs, pendingIntent);
	}



    private void displayZeroMessage() {
        /// M: Code analyze 001, new feature, we want to show many messages on
        // ClassZeroActivity. @{
        // byte[] pdu = intent.getByteArrayExtra("pdu");
        // String format = intent.getStringExtra("format");
        //mMessages = SmsMessage.createFromPdu(pdu, format);
	String time;
	SimpleDateFormat sdf_data=new SimpleDateFormat("dd/MM/yyyy");    
	String date=sdf_data.format(new java.util.Date());  

        if(DateFormat.is24HourFormat(getApplicationContext())){  
		SimpleDateFormat sdf_time=new SimpleDateFormat("HH:mm");    
		time=sdf_time.format(new java.util.Date()); 
        }else{         
		String ampmValues;  
		if(Calendar.getInstance().get(Calendar.AM_PM) == 0){  
		    ampmValues = getApplicationContext().getString(R.string.cbc_popup_am);  
		}else{  
		    ampmValues = getApplicationContext().getString(R.string.cbc_popup_pm);  
		}  
		SimpleDateFormat sdf_time=new SimpleDateFormat("hh:mm");    
		time=sdf_time.format(new java.util.Date()) + " "+ampmValues;  
        }  
		
        Uri ringtone = Uri.parse("file:///system/media/audio/CBC_01.ogg");
	r = new Ringtone(this, true);
	r.setStreamType(AudioSystem.STREAM_ALARM);
	//Log.d("mtk_debug", " MessagingNotification.updateNotification 1253 ringtoneStr="+ringtoneStr); // modify by mtk_debug 2013-12-18;
	r.setUri(ringtone);
	r.play();
	mVibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
	mVibrator.vibrate(new long[]{300,1000,300,1000}, 0);
	LayoutInflater inflater = LayoutInflater.from(this);
        ClassZeroView classZeroView = (ClassZeroView) inflater.inflate(
               R.layout.class_zero_view_gemini, null, false);
        classZeroView.bind(mMessage + "\n" + time + " " + "|" + " " + date, 0);
        mDialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
	        .setTitle(mTitle)
        	.setNegativeButton(R.string.cbc_popup_ok, mCancelListener).setCancelable(false)
                .setView(classZeroView)
		.show();
	//jianqiu add 20161208 
	sendUpdateBroadcast();
    }
   //Moka add for Auzmi CL CBC

    private void setTimeToSave() {
        long now = SystemClock.uptimeMillis();
        if (mTimerSet <= now) {
            // Save the message if the timer already expired.
            Message msg = mHandler.obtainMessage(ON_AUTO_SAVE);
            msg.obj = mMessages;
            mHandler.sendMessage(msg);
        } else {
            Message msg = mHandler.obtainMessage(ON_AUTO_SAVE);
            msg.obj = mMessages;
            mHandler.sendMessageAtTime(msg, mTimerSet);
            if (false) {
                Log.d(TAG, "onRestart time = " + Long.toString(mTimerSet) + " "
                        + this.toString());
            }
        }
    }

}

class ClassZeroView extends LinearLayout {
    private static final String TAG  = "ClassZeroView";

    private TextView mMessageView;
    private TextView mTimestamp;
    private Context  mContext;

    public ClassZeroView(Context context) {
        super(context);
        mContext = context;
    }

    public ClassZeroView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMessageView = (TextView) findViewById(R.id.body);
        LinearLayout simInfo = (LinearLayout) findViewById(R.id.sim_info_linear);
        simInfo.setVisibility(View.VISIBLE);
        mTimestamp = (TextView) findViewById(R.id.wpms_timestamp);
    }

    public void bind(String messageBody, int subId) {
        Log.i(TAG, "Class zero message:" + messageBody + "; From sub " + subId);
        mMessageView.setText(messageBody);
        SpannableStringBuilder buf = new SpannableStringBuilder();
        // / M: Add sub info
        SubscriptionInfo subInfo = SubscriptionManager.from(MmsApp.getApplication())
                .getActiveSubscriptionInfo(subId);
        Log.i(TAG, "subInfo=" + subInfo);
        if (null != subInfo) {
            mTimestamp.setText(subInfo.getDisplayName().toString());
            mTimestamp.setVisibility(View.VISIBLE);
            mTimestamp.setTextColor(subInfo.getIconTint());
        } else {
            mTimestamp.setVisibility(View.GONE);
            MmsLog.d(TAG, "subInfo is null ");
        }
    }
}

class MessageContent {
    public SmsMessage[] mMessages = null;
    public long mAutoSaveTime = 0;
    public int mSubId = 0;

    public MessageContent(SmsMessage[] messages, int subId) {
        mMessages = messages;
        mSubId = subId;
    }

    public MessageContent(Intent intent) {
        mMessages = Intents.getMessagesFromIntent(intent);
        mSubId = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY,
                SubscriptionManager.INVALID_SUBSCRIPTION_ID);
    }
}
