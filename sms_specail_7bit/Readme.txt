========================================================================================
Time : 2016-12-15-21-12
Branch : S5027_Bmobile_Movistar_ES_Lock_Single_Fwvga_8G_1G
commit 06a9d9f896e959e7bd1bddc09aede0645a6d824e
Author: liubinyang <liubinyang@hkzechin.com>
Date:   Thu Dec 15 21:12:09 2016 +0800

    [Bmobile][GMS][Sms]短信输入7bit特殊字符时，要求字数不减半，且解决CTS报错问题
Mod:  alps/frameworks/base/telephony/java/com/android/internal/telephony/GsmAlphabet.java
Mod:  alps/frameworks/opt/telephony/src/java/android/telephony/SmsMessage.java
Mod:  alps/frameworks/opt/telephony/src/java/com/android/internal/telephony/gsm/GsmSMSDispatcher.java
Mod:  alps/frameworks/opt/telephony/src/java/com/android/internal/telephony/gsm/SmsMessage.java
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/src/com/android/mms/ui/ComposeMessageActivity.java
