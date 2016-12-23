package com.android.factory;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.provider.Settings.Secure;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;





public class GPSAdvanceUtil
{
  private Context mContext;
  private int mGpsCurrentStatus;
  private GpsStatus.Listener mGpsStatusListener;
  private LocationListener mLocationListener;
  private LocationManager mLocationManager;
  private String mProvider;
  private int mSatelliteNum;

  private List mSatelliteSignal;
  
  //Tim 20150519 add begin
  public static int searchCount = 0;/*搜索次数*/
  public static int SNR_THRESHOLD = 35; /*门阀值*/
  public static int VALID_NUMBER = 4;/*最少达标个数*/
  public static int MAX_SEARCH_COUNT = 20;/*最多搜索次数*/
  private List<GpsBaseData> mSearchedSatelliteList;/*存储被记录的有效的卫星id号及信号值*/
  
  //Tim add end
  class GpsAdvanceUtil2
  implements GpsStatus.Listener
{
  public void onGpsStatusChanged(int paramInt)
  {
    GpsStatus localGpsStatus = mLocationManager.getGpsStatus(null);
    //GPSUtil.access$100(this.this$0, paramInt, localGpsStatus);
    updateGpsStatus(paramInt,localGpsStatus);
  }
}
  
  class GpsAdvanceUtil1
  implements LocationListener
{
  public void onLocationChanged(Location paramLocation)
  {
  }

  public void onProviderDisabled(String paramString)
  {
  }

  public void onProviderEnabled(String paramString)
  {
  }

  public void onStatusChanged(String paramString, int paramInt, Bundle paramBundle)
  {
  }
}
  /// Tim add @ { 
  private class GpsBaseData{
      private int satelliteId;
	  
	  private float GPSSnr;
	  
	  public void setSatelliteId(int _satelliteId){
	      this.satelliteId = _satelliteId;
	  }
	  
	  public int getSatelliteId(){
		  return this.satelliteId;
	  }
	  
	  public void setGPSSnr(float _GPSSnr){
	      this.GPSSnr = _GPSSnr;
	  }
	  
	  public float getGPSSnr(){
		  return this.GPSSnr;
	  }
  }
  /// Tim add end @ }
  public GPSAdvanceUtil(Context paramContext)
  {
    ArrayList localArrayList = new ArrayList();
    this.mSatelliteSignal = localArrayList;
    GpsAdvanceUtil1 local1 = new GpsAdvanceUtil1();
    this.mLocationListener = local1;
    GpsAdvanceUtil2 local2 = new GpsAdvanceUtil2();
    this.mGpsStatusListener = local2;
	//Tim 20150519 add begin
	ArrayList<GpsBaseData> localSearchedSitellitList = new ArrayList<GpsBaseData>();
	this.mSearchedSatelliteList = localSearchedSitellitList;
	searchCount = 0;
	//Tim add end
    this.mContext = paramContext;
    LocationManager localLocationManager1 = (LocationManager)this.mContext.getSystemService("location");
    this.mLocationManager = localLocationManager1;
    this.mProvider = "gps";
    openGPS();
    LocationManager localLocationManager2 = this.mLocationManager;
    String str1 = this.mProvider;
    localLocationManager2.getLastKnownLocation(str1);
    LocationManager localLocationManager3 = this.mLocationManager;
    String str2 = this.mProvider;
    LocationListener localLocationListener = this.mLocationListener;
    localLocationManager3.requestLocationUpdates(str2, 2000L, 0, localLocationListener);
    LocationManager localLocationManager4 = this.mLocationManager;
    GpsStatus.Listener localListener = this.mGpsStatusListener;
    localLocationManager4.addGpsStatusListener(localListener);
    updateGpsStatus(0, null);
  }

  private void openGPS()
  {
    LocationManager localLocationManager = this.mLocationManager;
    String str = this.mProvider;
    if (localLocationManager.isProviderEnabled(str))
      return;
    openGPSSetting();
  }

  private void openGPSSetting()
  {
    ContentResolver localContentResolver = this.mContext.getContentResolver();
    String str = this.mProvider;
    Settings.Secure.setLocationProviderEnabled(localContentResolver, str, true);  
  }

  private boolean querySatellitedIdExit(int satelliteId){
      for(GpsBaseData gpsBaseData : mSearchedSatelliteList){
	      if(satelliteId == gpsBaseData.getSatelliteId()){
			  return true;
		  }
	  }
	  
	  return false;
  }
  
  private void storageGPSData(int satelliteId,float snrValue){
      if(snrValue >= GPSAdvanceUtil.SNR_THRESHOLD){
		  if(!querySatellitedIdExit(satelliteId)){
			  GpsBaseData _gpsBaseData = new GpsBaseData();
			  _gpsBaseData.setSatelliteId(satelliteId);
			  _gpsBaseData.setGPSSnr(snrValue);
			  mSearchedSatelliteList.add(_gpsBaseData);
			  
			  String gpsSatelliteInfo = "\n\r#"+satelliteId +": "+snrValue+";\r";
			  mSatelliteSignal.add(gpsSatelliteInfo);
			  
			  this.mSatelliteNum ++;
		  }
	  }
  }
  
private void updateGpsStatus(int paramInt, GpsStatus paramGpsStatus)
  {
  if (paramGpsStatus == null)
  {
      this.mSatelliteNum = 0;
      return;
  }
  
   if(paramInt == 4)
	   this.mGpsCurrentStatus = paramInt;
   else
	   return;
  

    Iterator<GpsSatellite> iterator = 	paramGpsStatus.getSatellites().iterator();
    float f = 0;
	/// Tim 20150519 modify begin @ {
    /*
    while (true)
    {
      if (iterator.hasNext())
      f = ((GpsSatellite)iterator.next()).getSnr();
      if (f <30)
        return;
      List localList = this.mSatelliteSignal;
      Float localFloat = Float.valueOf(f);
      localList.add(localFloat);
      this.mSatelliteNum ++;

      return;
    }
	*/
	int satelliteId = 0;/*搜索的卫星id*/

	while (iterator.hasNext())
    {
	  GpsSatellite tempGpsSatellite = (GpsSatellite)iterator.next();
      //f = tempGpsSatellite.getSnr();
	  //satelliteId = tempGpsSatellite.getPrn();
      /// Test begin @ {
      //List localList = this.mSatelliteSignal;
	  
	  //String gpsSatelliteInfo = "\n\t("+(searchCount+1)+")#"+satelliteId +": "+f+"\t";
	  //localList.add(gpsSatelliteInfo);
	 
	  storageGPSData(tempGpsSatellite.getPrn(),tempGpsSatellite.getSnr());
      
     ///Test end @ }
    }
	searchCount++;
	/// Tim modify end @ }
  }

  //Tim add end
  public void closeLocation()
  {
    LocationManager localLocationManager1 = this.mLocationManager;
    GpsStatus.Listener localListener = this.mGpsStatusListener;
    localLocationManager1.removeGpsStatusListener(localListener);
    LocationManager localLocationManager2 = this.mLocationManager;
    LocationListener localLocationListener = this.mLocationListener;
    localLocationManager2.removeUpdates(localLocationListener);
  }

  public int getGpsCurrentStatus()
  {
    return this.mGpsCurrentStatus;
  }

  public LocationManager getLocationManager()
  {
    return this.mLocationManager;
  }

  public int getSatelliteNumber()
  {
    return this.mSatelliteNum;
  }

  public String getSatelliteSignals()
  {
    int i = 1;
    int k =0;
    String str3 = "";
    String str1 = this.mSatelliteSignal.toString();
    int j = this.mSatelliteSignal.size();
    k = str1.length() - i;
    //String str2 = "";
    if ((j > 0)&&(this.mSatelliteNum>0))
    {
     str3 = str1.substring(i, k);
     return str3;
    }
    else
     return str3;


    /*for (; ; str3 = str1.substring(i, k))
    {
      return str2;
      k = str1.length() - i;
    }*/
  }
}
