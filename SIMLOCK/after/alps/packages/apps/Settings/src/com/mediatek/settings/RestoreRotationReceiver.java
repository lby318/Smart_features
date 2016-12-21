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

package com.mediatek.settings;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.os.UserHandle;
//zhangguoqiang 20140916 add for SIM_ME_LOCK
import android.os.ServiceManager;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.SystemProperties;
import android.os.Bundle;//zhangguoqiang 20140916 add for SIM_ME_LOCK

public class RestoreRotationReceiver extends BroadcastReceiver {

    public static boolean sRestoreRetore = false;
    public static  String  TAG = "RestoreRotationReceiver";	//zhangguoqiang 20140916 add for SIM_ME_LOCK

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.v("RestoreRotationReceiver_IPO", action);
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)
                || action.equals("android.intent.action.ACTION_BOOT_IPO")
                || action.equals("android.intent.action.USER_SWITCHED_FOR_MULTIUSER_APP")) {
            sRestoreRetore = Settings.System.getIntForUser(context
                    .getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION_RESTORE,
                    0, UserHandle.USER_CURRENT) != 0;
            if (sRestoreRetore) {
                Settings.System.putIntForUser(context.getContentResolver(),
                        Settings.System.ACCELEROMETER_ROTATION, 1, UserHandle.USER_CURRENT);
                Settings.System.putIntForUser(context.getContentResolver(),
                        Settings.System.ACCELEROMETER_ROTATION_RESTORE, 0, UserHandle.USER_CURRENT);
            }
		       //zhangguoqiang 20140916 add begin for SIM_ME_LOCK
        }else if (action.equals("zechin.intent.action.SIM_LOCK")) {
               Bundle bundle = intent.getExtras();
	       String lockStatus = bundle.getString("lock", "true");
	       Log.v(TAG, ">>> Lock Status "+lockStatus+" <<<");
	       if( lockStatus.equals("true") )
	       {
			writeData( (byte)0 );
	       }else
	       {
			writeData( (byte)1 );
	       }	
               //SystemProperties.set("persist.sys.simlock", bundle.getString("lock", "true"));
			
               //  hukui 20140920 add begin for sim lock
               Intent i = new Intent(Intent.ACTION_REBOOT);
               i.putExtra("nowait", 1);
               i.putExtra("interval", 1);
               i.putExtra("window", 0);
               context.sendBroadcast(i);
       //  hukui 20140920 add end for sim lock

       }

       //zhangguoqiang add end
    }

    /// The previous lines are provided and maintained by Mediatek Inc.
   // private static final int file_lid=36;            //AP_CFG_REEB_PRODUCT_INFO_LID=36
    private static final int file_lid_index=1023;    //Use for salestrack
    public  static final byte file_lid_success=0x3c; //Use for salestrack
    public  static final byte file_lid_redo=0x43;    //Use for salestrack
    public static void writeData(byte data) {
	//xjc change here read by filename	
	 final String LOCK_ADDRESS_FILENAME = "/data/nvram/APCFG/APRDEB/PRODUCT_INFO";
   try {	 
        IBinder binder = ServiceManager.getService("NvRAMAgent");
		Log.v(TAG, ">>> binder "+binder+" <<<");
        NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
		Log.v(TAG, ">>> agent "+agent+" <<<");
        byte[] buff = agent.readFileByName(LOCK_ADDRESS_FILENAME);
     
         //   buff = agent .readFile(file_lid);// read buffer from nvram
            buff[file_lid_index] = data;
            if (agent.writeFileByName(LOCK_ADDRESS_FILENAME, buff) > 0) {
                Log.d(TAG, "write success addr = " + data);
            } else {
                Log.d(TAG, "write failed addr = " + buff[file_lid_index]);
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static byte readData() {
	 final String LOCK_ADDRESS_FILENAME = "/data/nvram/APCFG/APRDEB/PRODUCT_INFO"; 	        
	 try { 
	 	IBinder binder = ServiceManager.getService("NvRAMAgent");
        	NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
        	 byte[] buff = agent.readFileByName(LOCK_ADDRESS_FILENAME);
         	 Log.d(TAG, "read success addr = " + buff[file_lid_index]);
            return buff[file_lid_index];
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.d(TAG, "read failed return -1");
        return -1;
    }	
}
