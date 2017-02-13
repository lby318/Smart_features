========================================================================================
Time : 2017-02-13-14-09
Branch : S5027_Bmobile_Movistar_NI_Lock_Single_Fwvga_8G_1G
commit 65b6134b60472f975de3ae059ca08466d59a4a14
Author: liubinyang <liubinyang@hkzechin.com>
Date:   Fri Aug 19 11:20:48 2016 +0800

    [Bmobile][WIFI]之前预置MOVISTAR WIFI的方法影响待机功耗，现根据MTK提供方案换个文件预置，以解决打开wifi不连接AP待机功耗过大问题，from(ALPS02858892)
    重新修改：增加overlay_loaded=1属性，解决预置wifi引起的WIFI扫描重启等异常问题
Mod:  alps/vendor/mediatek/proprietary/hardware/connectivity/wlan/config/mtk-wpa_supplicant-overlay.conf
Mod:  alps/vendor/mediatek/proprietary/hardware/connectivity/wlan/config/mtk-wpa_supplicant.conf
