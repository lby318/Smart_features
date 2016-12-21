package com.android.dialer;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
//add for simlock begin
import android.os.Bundle;//
import android.util.Log;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionManager;
//add for simlock end

public class ZechinEngineerCode {

	private final static String ZECHIN_INTERNAL_VERSEION_CMD = "*#2687#";
	private final static String ZECHIN_EXTERNAL_VERSION_CMD = "*#37*#";
	private final static String ZECHIN_FACTORY_MODE_CMD	= "*#28*#";
	private final static String ZECHIN_SIM_LOCK = "*#26872016*#";//zhangguoqiang 20140916 add for SIM_ME_LOCK
	private final static String ZECHIN_SIM_UNLOCK = "*#20162687*#";//zhangguoqiang 20140916 add for SIM_ME_LOCK
	private final static String ZECHIN_INTERNAL_BAND_VERSEION_CMD = "*#*#9527#";
	public static String TAG ="ZechinEngineerCode";
	public static Context mContext;
	public static boolean handleZechinCodeDisplay(Context context,String input){
		android.util.Log.d("ZECHIN", ">> Enter handleZechinCodeDisplay()<<");
		mContext = context;		
		if(input.equals(ZECHIN_INTERNAL_VERSEION_CMD)){
			
			String versionNumber = SystemProperties.get("ro.zx.itl.sw.verno","")+
						SystemProperties.get("ro.zx.itl.sw.verno.inc","")+
						SystemProperties.get("ro.zx.fota.verno","")+"_"+
						SystemProperties.get("ro.zx.date.ydmhs","");		
			String[] choices={ versionNumber,getData()}; 
		
			ArrayAdapter<String> adapter=  
        			new ArrayAdapter<String>(context, R.layout.select_dialog_item,choices);

        	
			AlertDialog dlg =new AlertDialog.Builder(context)
					.setTitle(R.string.zechin_code_sw_version_internal)
					//.setMessage(SystemProperties.get("ro.zx.itl.sw.verno","")+
					//	SystemProperties.get("ro.zx.itl.sw.verno.inc","")+
					//	SystemProperties.get("ro.zx.fota.verno","")+"_"+
					//	SystemProperties.get("ro.zx.date.ydmhs",""))
					.setAdapter(adapter, null)
					.setPositiveButton(android.R.string.ok, null)
					.setCancelable(false)
					.create();
			dlg.show();
			return true;
		}else if(input.equals(ZECHIN_EXTERNAL_VERSION_CMD)){
			Intent it = new Intent();
			it.setClassName("com.android.dialer", "com.android.dialer.ZechinVersionDisplay");
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			return startActivitySafety(context, it);
			
		}else if(input.equals(ZECHIN_FACTORY_MODE_CMD)){
			Intent it = new Intent();
			it.setClassName("com.android.factory", "com.android.factory.FactoryActivity");
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			return startActivitySafety(context, it);
		}else if (input.equals(ZECHIN_SIM_LOCK) || input.equals(ZECHIN_SIM_UNLOCK)) {
                       Intent intent = new Intent();
                       intent.setAction("zechin.intent.action.SIM_LOCK");
                       Bundle bundle = new Bundle();
                       bundle.putString("lock", input.equals(ZECHIN_SIM_LOCK) ? "true" : "false");
                       intent.putExtras(bundle);
                       context.sendBroadcast(intent);
        }// LOCK SIM 
		else if(input.equals(ZECHIN_INTERNAL_BAND_VERSEION_CMD)){
			AlertDialog dlg =new AlertDialog.Builder(context)
				                    .setTitle("Band")
				                    .setMessage(SystemProperties.get("ro.zx.band","error"))
				                    .setPositiveButton(android.R.string.ok, null)
				                    .setCancelable(false)
				                    .create();
            dlg.show();
            return true;
		}
		return false;
	}
	
	/**
	 * @author shihaijun
	 * @param context
	 * @param intent
	 * @return
	 */
	private static boolean startActivitySafety(Context context, Intent intent){
		try{
			android.util.Log.d("ZECHIN", "startActivitySafety");
			context.startActivity(intent);
		}catch(ActivityNotFoundException ex ){
			android.util.Log.d("ZECHIN", "ActivityNotFoundException");
			return false;
		}
		
		return true;
	}

	/// The previous lines are provided and maintained by Mediatek Inc.
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
        NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
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
		android.util.Log.d("ZECHIN", ">> binder<<" + binder);
        	NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
		android.util.Log.d("ZECHIN", ">> agent<<" + agent);			
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
	

	public static int getSubId(int slotId) {
		int[] SubIds = SubscriptionManager.getSubId(slotId);
		if (SubIds != null) {
			return SubIds[0];
		} else {
			return -1;
    }
	}
	
	public static int[] getSlotIds() {
        int slotCount = TelephonyManager.getDefault().getPhoneCount();
        int[] slotIds = new int[slotCount];
        for (int i = 0; i < slotCount; i++) {
            slotIds[i] = i;
        }
        return slotIds;
    }



	public static String getData()
	{
			String lw 			= null;
			String lock_status 	= null;
			if(readData() == 1){
          		 lw = "false";
			}else
			{
				 lw = "true";
			}
			Log.v(TAG, " lw " + lw);
			if(lw.equals("true")){
				 lock_status = mContext.getString( R.string.zechin_status_lock);
			
			}else
			{
				 lock_status = mContext.getString( R.string.zechin_status_unlock);
				
			}
			
			return mContext.getString( R.string.zechin_switch_lock_status )+lock_status;
	}
		
}
