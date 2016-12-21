/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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

package com.mediatek.cb.cbmsg;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.Telephony;
import android.util.Log;
import android.widget.Toast;
import android.telephony.SmsCbMessage;

import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.ui.MessageUtils;
import com.android.mms.util.MmsLog;
import com.android.internal.telephony.PhoneConstants;
import com.mediatek.mms.ext.IOpCBMessageReceiverServiceExt;
import com.mediatek.opmsg.util.OpMessageUtils;

import android.telephony.SubscriptionManager;

import android.content.ComponentName;
import com.android.mms.ui.ClassZeroActivity;

//add for cbc channel
import android.provider.Telephony;
import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.TelephonyIntents;
import android.telephony.SmsManager;
import android.provider.Telephony.Sms.Intents;
import com.android.internal.telephony.PhoneConstants;

/**
 * M:
 * This service essentially plays the role of a "worker thread", allowing us to store
 * incoming messages to the database, update notifications, etc. without blocking the
 * main thread that SmsReceiver runs on.
 */
public class CBMessageReceiverService extends Service {
    private static final String TAG = "MmsCB/CBMessageReceiverService";

    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    private IOpCBMessageReceiverServiceExt mOpCBMessageReceiverService;

    private static final Uri MESSAGE_URI = Telephony.SmsCb.CONTENT_URI;
    private static final int DEFAULT_SUB_ID = 1;

	
//add for cbc channel
private static final int MESSAGE_SET_STATE = 33;
private static final int MESSAGE_SET_CONFIG = 32;
private static final String KEYID = "_id";
private static final String NAME = "name";
private static final String NUMBER = "number";
private static final String ENABLE = "enable";
private static final String SUBID = "sub_id";
private static final Uri CHANNEL_URI = Uri.parse("content://cb/channel");
private static final int EVENT_RETRY_ADD_CHANNEL_TIME_OUT = 101;
private static boolean isAddingDefaultChannel = false;


    public Handler mToastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(CBMessageReceiverService.this, getString(R.string.message_queued),
                    Toast.LENGTH_SHORT).show();
        }
    };

    // This must match SEND_PROJECTION.
    private int mResultCode;

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        if (null != mServiceLooper) {
            mServiceHandler = new ServiceHandler(mServiceLooper);
        }
        mOpCBMessageReceiverService = OpMessageUtils.getOpMessagePlugin()
                .getOpCBMessageReceiverServiceExt();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mResultCode = intent != null ? intent.getIntExtra("result", 0) : 0;

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        /**
         * Handle incoming transaction requests.
         * The incoming requests are initiated by the MMSC Server or by the MMS Client itself.
         */
        @Override
        public void handleMessage(Message msg) {
            int serviceId = msg.arg1;
            Intent intent = (Intent) msg.obj;
            if (intent != null) {
                String action = intent.getAction();
                // NEED Replace with CB ACTION
                if (Telephony.Sms.Intents.SMS_CB_RECEIVED_ACTION.equals(action)) {
                    handleCBMessageReceived(intent);
                }

		  if(TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(intent.getAction())){

			synchronized(this){
				handleSIMStateChangedReceived(intent);
			}
		  }

		  
            }
            // NOTE: We MUST not call stopSelf() directly, since we need to
            // make sure the wake lock acquired by AlertReceiver is released.
             if(!isAddingDefaultChannel){
			 	
            		CBMessageReceiver.finishStartingService(CBMessageReceiverService.this, serviceId);

             	}
        }
    }

    private void handleCBMessageReceived(Intent intent) {
        // TODO need replace with cb message.
        Bundle extras = intent.getExtras();
        if (null == extras) {
            MmsLog.e(MmsApp.TXN_TAG, "Intents.getMessagesFromIntent return null !!");
            return;
        }

        SmsCbMessage message = (SmsCbMessage) extras.get("message");
        if (null == message) {
            MmsLog.e(MmsApp.TXN_TAG, "received SMS_CB_RECEIVED_ACTION with no extras!");
            return;
        }

        int subId = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY,
                SubscriptionManager.INVALID_SUBSCRIPTION_ID);

        // add for op
        mOpCBMessageReceiverService.handleCBMessageReceived(getApplicationContext());

        Uri messageUri = insertMessage(subId, this, message);
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            MmsLog.v(TAG, "handleSmsReceived" +
                    " messageUri: " + messageUri +
                    ", body: " + message.getMessageBody());
        }

        if (messageUri != null) {
            CBMessagingNotification.updateNewMessageIndicator(subId, this, true);
        }
		//Moka add for Azumi CL CBC
       displayClassZeroMessage(this, intent,message); 
	    //Moka add for Azumi CL CBC
    }
	//Moka add for Azumi CL CBC
     private void displayClassZeroMessage(Context context, Intent intent,SmsCbMessage message) {
        // Using NEW_TASK here is necessary because we're calling
        // startActivity from outside an activity.
        /// M:

        /// M:Code analyze 031, moidfy the logic,set component into intent,add simId info
        /// if in gemini mode @{

        intent.setComponent(new ComponentName(context, ClassZeroActivity.class));

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        // add for gemini, add sim id info
       // intent.putExtra(EncapsulatedPhone.GEMINI_SIM_ID_KEY, intent.getIntExtra(EncapsulatedPhone.GEMINI_SIM_ID_KEY, -1));
		
	//intent.putExtra("com.android.mms.cbs.title",message.getServiceCategory());
	 //intent.putExtra("com.android.mms.cbs.title",MmsApp.getApplication().getApplicationContext().getString(
            //R.string.cb_default_channel_name) + "(" + Integer.toString(message.getServiceCategory()) + ")");
	
	 intent.putExtra("com.android.mms.cbs.title",MmsApp.getApplication().getApplicationContext().getString(
            R.string.cbc_popup_title));	
	
	 intent.putExtra("com.android.mms.cbs.message",message.getMessageBody());

        MmsLog.v(MmsApp.TXN_TAG, "message.mLocation.mPlmn =                "  + message.getServiceCategory());
        MmsLog.v(MmsApp.TXN_TAG, "message.mBody =                "  + message.getMessageBody());
        /// M: add for OP09 @{
        //if (mDisplayClassZeroPlugin.isEnableClassZeroFeature()) {
            //intent = mDisplayClassZeroPlugin.setLaunchMode(intent);
        //}
        /// @}
        context.startActivity(intent);
    }
//Moka add for Azumi CL CBC
    public static final String CLASS_ZERO_BODY_KEY = "CLASS_ZERO_BODY";

    private static final int REPLACE_COLUMN_ID = 0;

    /**
     * If the message is a class-zero message, display it immediately
     * and return null.  Otherwise, store it using the
     * <code>ContentResolver</code> and return the
     * <code>Uri</code> of the thread containing this message
     * so that we can use it for notification.
     */
    // TODO Need replace with CBMessage
    private Uri insertMessage(int subId, Context context, SmsCbMessage msg) {
        return storeCBMessage(subId, context, msg);
    }

    // TODO Need replace with CB message
    private Uri storeCBMessage(int subId, Context context, SmsCbMessage msg) {
        // Store the message in the content provider.
        String body = msg.getMessageBody();
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = getCBContentValue(subId, msg, body);
        MmsLog.d(MmsApp.TXN_TAG, "CB message body: " + body);
        return resolver.insert(MESSAGE_URI, values);
    }

    // TODO  Need replace with CB Message
    private ContentValues getCBContentValue(int subId, SmsCbMessage msg, String body) {
        ContentValues values = new ContentValues();
        // TODO just use default SUB ID, need improve when two sub cards.
        values.put(Telephony.SmsCb.SUBSCRIPTION_ID, subId);
        values.put(Telephony.SmsCb.DATE, Long.valueOf(System.currentTimeMillis()));
        // Channel ID is getting from getMessageID
        values.put(Telephony.SmsCb.CHANNEL_ID, msg.getServiceCategory());
        values.put(Telephony.SmsCb.READ, Integer.valueOf(0));
        values.put(Telephony.SmsCb.BODY, body);
        return values;
    }


	//add 

	private void handleSIMStateChangedReceived(Intent intent) {
		

			int  subId = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, SubscriptionManager.INVALID_SUBSCRIPTION_ID);

			String stateExtra = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
			Log.d(" CBMessageReceiverService ", " CBMessageReceiverService.handleSIMStateChangedReceived 214 stateExtra="+stateExtra); 
			if(IccCardConstants.INTENT_VALUE_ICC_READY.equals(stateExtra)){
					if(queryIfChannelInDatabase(subId,"Channel1",919)||queryIfChannelInDatabase(subId,"Channel1",519)||queryIfChannelInDatabase(subId,"Channel1",514))
					{
					isAddingDefaultChannel=false;
					}else{
					if(SmsManager.getSmsManagerForSubscriptionId(subId).activateCellBroadcastSms(true))
					{                     
					addCustomChanneltoList(subId,"Channel1",919);
					addCustomChanneltoList(subId,"Channel1",519);
					addCustomChanneltoList(subId,"Channel1",514);
					isAddingDefaultChannel=false;
					 
					}else{

					//add retry
					if (!mServiceHandler.hasMessages(EVENT_RETRY_ADD_CHANNEL_TIME_OUT)) {
					Message msg = mServiceHandler.obtainMessage(EVENT_RETRY_ADD_CHANNEL_TIME_OUT,intent);
					Log.d(" CBMessageReceiverService ", " CBMessageReceiverService.handleSIMStateChangedReceived send retry message 235 "); // modify by mtk_debug 2015-3-19;
					mServiceHandler.sendMessageDelayed(msg, 2000);
					isAddingDefaultChannel=true;
			}


			}

			}




	}

}

private void addCustomChanneltoList(int  mSubId,String channelName,int channelNum){
Log.d(TAG, "addCustomChanneltoList: mSubId=" + mSubId
+ ", channelName= " + channelName + ", channelNum= " + channelNum);

SmsBroadcastConfigInfo[] objectList = new SmsBroadcastConfigInfo[1];
objectList[0] = new SmsBroadcastConfigInfo(channelNum,channelNum, -1, -1, true);

Log.d(TAG, "addCustomChanneltoList: setCellBroadcastSmsConfig");

boolean isSetConfigSuccess = SmsManager.getSmsManagerForSubscriptionId(mSubId).setCellBroadcastSmsConfig(objectList, objectList);

if(isSetConfigSuccess){
Log.d(" CBMessageReceiverService ", " CBMessageReceiverService.addCustomChanneltoList set channel " +channelNum +" success 784 "); // modify by mtk_debug 2015-3-05;
addChannelToDatabase(mSubId,channelName,channelNum);
}else{
Log.d(" CBMessageReceiverService ", " CBMessageReceiverService.addCustomChanneltoList set channel "+ channelNum +" failed 786 "); // modify by mtk_debug 2015-3-05;
}


Log.d(TAG, " CBMessageReceiverService addCustomChanneltoList: function end");
}



private boolean addChannelToDatabase(int  mSubId,String channelName,int channelNum){
	// ClearChannel();
	Log.d(TAG, "addChannelToDatabase: mSubId=" + mSubId
	+ ", channelName= " + channelName + ", channelNum= " + channelNum);
	String[] projection = new String[] { NUMBER, SUBID};
	String SELECTIONNum = "(" + NUMBER + " = " + channelNum + ")"; 
	String SELECTIONid = "(" + SUBID + " = " + mSubId + ")";
	Cursor cursornum = null;
	Cursor cursorid = null;

	Cursor cursorzz = this.getContentResolver().query(CHANNEL_URI,
	projection,
	NUMBER + " = ? AND " +
	SUBID + " = ?",
	new String[] {String.valueOf(channelNum), String.valueOf(mSubId)},
	null);
	Log.d(TAG, "addChannelToDatabase: cursor.getCount"+cursorzz.getCount());
	if(cursorzz.getCount() == 0){//if ((cursornum.getCount() == 0)&&(cursorid.getCount() == 0)){ 
	Log.d(" CBMessageReceiverService ", " SmsReceiverService.addChannelToDatabase add mSubId=" + mSubId
	+ ", channelName= " + channelName + ", channelNum= " + channelNum +"to database"); // modify by mtk_debug 2015-3-05;
	ContentValues values = new ContentValues();
	values.put(NAME,channelName);
	values.put(NUMBER, channelNum);
	values.put(ENABLE, true); 
	values.put(SUBID, mSubId);
	try {
	//if(mSimId==PhoneConstants.SUB1){
	this.getContentResolver().insert(CHANNEL_URI, values);
	//}else if(mSimId==PhoneConstants.SUB2){
	// this.getContentResolver().insert(CHANNEL_URI1, values);
	//}
	} catch (Exception e){
	Log.d(" CBMessageReceiverService ", " SmsReceiverService.addChannelToDatabase exception 828 "); // modify by mtk_debug 2015-3-05;
	//cursornum.close();
	//cursorid.close();
	cursorzz.close();
	return false;
	}
	//cursornum.close();
	// cursorid.close();
	cursorzz.close();
	return true;
	}
	else{
	//cursornum.close();
	//cursorid.close();
	cursorzz.close();
	return false;
	}

} 


private boolean queryIfChannelInDatabase(int  mSubId,String channelName,int channelNum){

Log.d(TAG, "queryIfChannelInDatabase: mSubId=" + mSubId
+ ", channelName= " + channelName + ", channelNum= " + channelNum);
String[] projection = new String[] { NUMBER, SUBID};
String SELECTIONNum = "(" + NUMBER + " = " + channelNum + ")"; 
String SELECTIONid = "(" + SUBID + " = " + mSubId + ")";
Cursor cursornum = null;
Cursor cursorid = null;

Cursor cursorzz = this.getContentResolver().query(CHANNEL_URI,
projection,
NUMBER + " = ? AND " +
SUBID + " = ?",
new String[] {String.valueOf(channelNum),String.valueOf(mSubId)},
null);

if(cursorzz != null&&cursorzz.getCount() > 0){
//if ((cursornum.getCount() != 0)&&(cursorid.getCount() != 0)){
Log.d(TAG, "queryIfChannelInDatabase: cursor.getCount"+cursorzz.getCount());
cursorzz.close();
return true;


}else{
cursorzz.close();
Log.d(TAG, "queryIfChannelInDatabase: return false");
return false;
}

}
}
