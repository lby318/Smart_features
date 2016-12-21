========================================================================================
Time : 2016-12-21-11-17
Branch : S5508_AZUMI_Claro_CL_Lock_Single_HD_8G_1G
commit 83e7bc2f7ad8d1bfd1521e639d6e0aebf873d8fa
Author: liangjianqiu <liangjianqiu@hkzechin.com>
Date:   Thu Dec 8 17:36:32 2016 +0800

    [Azumi][CBC]添加小区广播需求
Mod:  alps/frameworks/base/data/sounds/AllAudio.mk
Add:  alps/frameworks/base/data/sounds/effects/ogg/CBC_01.ogg
Mod:  alps/frameworks/base/media/java/com/mediatek/audioprofile/AudioProfileManager.java
Mod:  alps/frameworks/base/services/core/java/com/android/server/audio/AudioService.java
Mod:  alps/frameworks/base/services/core/java/com/android/server/policy/PhoneWindowManager.java
Mod:  alps/frameworks/opt/telephony/src/java/com/android/internal/telephony/gsm/GsmCellBroadcastHandler.java
Mod:  alps/frameworks/opt/telephony/src/java/com/android/internal/telephony/gsm/SmsCbConstants.java
Mod:  alps/packages/providers/TelephonyProvider/src/com/android/providers/telephony/CbProvider.java
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/AndroidManifest.xml
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/res/values-es-rUS/strings.xml
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/res/values-es/strings.xml
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/res/values-zh-rCN/mtk_strings.xml
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/res/values-zh-rCN/strings.xml
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/res/values/strings.xml
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/res/xml/cell_broad_cast.xml
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/src/com/android/mms/ui/ClassZeroActivity.java
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/src/com/android/mms/ui/ConversationListItem.java
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/src/com/android/mms/ui/MessageUtils.java
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/src/com/mediatek/cb/cbmsg/CBMessage.java
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/src/com/mediatek/cb/cbmsg/CBMessageItem.java
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/src/com/mediatek/cb/cbmsg/CBMessageListActivity.java
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/src/com/mediatek/cb/cbmsg/CBMessageReceiver.java
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/src/com/mediatek/cb/cbmsg/CBMessageReceiverService.java
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/src/com/mediatek/cb/cbmsg/CBMessagingNotification.java
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/src/com/mediatek/cb/cbsettings/CellBroadcastCheckBox.java
Mod:  alps/vendor/mediatek/proprietary/packages/apps/Mms/src/com/mediatek/cb/cbsettings/CellBroadcastSettings.java

1.	小区广播默认打开，默认紧急信道919、921、519，紧急信道必须在小区广播列表里不能隐藏，不能被编辑和删除），
2.	任何界面强制震动+响铃（静音下也会响铃和震动），铃声是CBC专用铃声，CBC铃声时间是无限的，震动3分钟后自动停止。手动干预（按键或者点击屏幕提示框按钮）可停止，不会出现死机，无法操作等情况。
3.	收到的CBC 短信保存在手机内存里面。另外，需要保证能保存最少10 条CBC 短信
4.	打开收到的CBC消息 , 标题都改为Alerta de Emergencia.

