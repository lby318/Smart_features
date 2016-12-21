package com.android.dialer;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;

public class ZechinEngineerCode {

	private final static String ZECHIN_INTERNAL_VERSEION_CMD = "*#2687#";
	private final static String ZECHIN_EXTERNAL_VERSION_CMD = "*#37*#";
	private final static String ZECHIN_FACTORY_MODE_CMD	= "*#28*#";
	private final static String ZECHIN_INTERNAL_BAND_VERSEION_CMD = "*#*#9527#";
	public static boolean handleZechinCodeDisplay(Context context,String input){
		android.util.Log.d("ZECHIN", ">> Enter handleZechinCodeDisplay()<<");
		if(input.equals(ZECHIN_INTERNAL_VERSEION_CMD)){
			AlertDialog dlg =new AlertDialog.Builder(context)
					.setTitle(R.string.zechin_code_sw_version_internal)
					.setMessage(SystemProperties.get("ro.zx.itl.sw.verno","")+"_"+SystemProperties.get("ro.zx.date.ydmhs",""))
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
		}else if(input.equals(ZECHIN_INTERNAL_BAND_VERSEION_CMD)){
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
}
