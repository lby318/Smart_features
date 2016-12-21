package com.android.dialer;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class ZechinVersionDisplay extends PreferenceActivity {

	final private String KEY_PREF_VERSION_SW = "software_version";
	final private String KEY_PREF_VERSION_HW = "hardware_version";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.zechin_external_version);
		
		initVersionInfo();
	}

	private void initVersionInfo(){
		Preference prefSW = findPreference(KEY_PREF_VERSION_SW);
		if(prefSW !=null){
//			String sw = SystemProperties.get("ro.build.display.id","");
			String sw = SystemProperties.get("ro.zx.customer.verno","");
			//String sw = SystemProperties.get("ro.zx.customer.verno","")+"_"+SystemProperties.get("ro.zx.date.ydm","");
			if(!sw.equals("")){
				prefSW.setSummary(sw);
			}
		}
		
		Preference prefHW = findPreference(KEY_PREF_VERSION_HW);
		getPreferenceScreen().removePreference(prefHW); //added by shihaijun to remove HW version item.
		if(prefHW != null){
			String hw = SystemProperties.get("ro.zx.hw.verno", "");
			if(!hw.equals("")){
				prefHW.setSummary(hw);
			}
		}
	}
}
