========================================================================================
Time : 2016-12-21-16-58
Branch : S5557_Bmobile_Tigo_BO_Lock_Single_HD_16G_2G
commit aa5f9187bd16694b23b6a3f8bde45e51d0ccc456
Author: liubinyang <liubinyang@hkzechin.com>
Date:   Tue Aug 30 18:04:20 2016 +0800

    [Bmobile][SIMLOCK]增加Bmobile锁网需求，手动上锁码*#26872016*#，解锁码IMEI前14位，从第一位起，每相邻两位相加模以10为一位，取IMEI第15位做为第8位，生成解锁码，为方便看锁网状态在*#2687#查内部版本号时显示锁网状态UNLOCK/LOCK
Mod:  alps/frameworks/base/packages/Keyguard/AndroidManifest.xml
Mod:  alps/frameworks/base/packages/Keyguard/res_ext/values-es-rUS/mtk_strings.xml
Mod:  alps/frameworks/base/packages/Keyguard/src/com/android/keyguard/KeyguardSecurityModel.java
Mod:  alps/frameworks/base/packages/Keyguard/src/com/android/keyguard/KeyguardUpdateMonitor.java
Add:  alps/frameworks/base/packages/Keyguard/src/com/android/keyguard/NvRAMAgent.java
Mod:  alps/frameworks/base/packages/Keyguard/src/com/mediatek/keyguard/Telephony/KeyguardSimPinPukMeView.java
Add:  alps/frameworks/base/packages/Keyguard/src/com/mediatek/keyguard/Telephony/NvRAMAgent.java
Mod:  alps/frameworks/base/packages/SystemUI/AndroidManifest.xml
Mod:  alps/packages/apps/Dialer/Android.mk
Mod:  alps/packages/apps/Dialer/AndroidManifest.xml
Mod:  alps/packages/apps/Dialer/res/values/strings.xml
Add:  alps/packages/apps/Dialer/src/com/android/dialer/NvRAMAgent.java
Mod:  alps/packages/apps/Dialer/src/com/android/dialer/ZechinEngineerCode.java
Mod:  alps/packages/apps/Settings/AndroidManifest.xml
Add:  alps/packages/apps/Settings/src/com/mediatek/settings/NvRAMAgent.java
Mod:  alps/packages/apps/Settings/src/com/mediatek/settings/RestoreRotationReceiver.java
