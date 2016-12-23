/*
  * Starmen write this file ZechinFeatureOptions.java
  * Copyright by Starmen
  */

package com.android.factory;

import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

public class ZechinFeatureOptions {
	public static boolean isDualSim() {
        return SystemProperties.get("ro.mtk_gemini_support").equals("1");
    }

	public static String getSerialNumber() {
		return SystemProperties.get("gsm.serial");
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
}
