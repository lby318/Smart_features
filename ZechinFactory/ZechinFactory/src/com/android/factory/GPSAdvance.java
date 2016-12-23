package com.android.factory;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import android.os.Message;

public class GPSAdvance extends Activity
  implements View.OnClickListener
{
  private Button mBtFailed;
  private Button mBtOk;
  private GPSAdvanceUtil mGpsAdvanceUtil;
  private Handler mHandler;
  private TextView mResultView;
  private TextView mSatelliteNumView;
  private TextView mSignalView;
  private SharedPreferences mSp;
  private TextView mStView;
  private Chronometer mTimeView;

  class GPSAdvance1 extends Handler
  {
    public void handleMessage(Message paramMessage)
    {
     switch (paramMessage.what)
      {
      default:
      case 0:
      }
     getSatelliteInfo();
     
    }
  }
  
  public GPSAdvance()
  {
    GPSAdvance1 local1 = new GPSAdvance1();
    this.mHandler = local1;
  }

  public void getSatelliteInfo()
  {
    int i = 0;
    int j = this.mGpsAdvanceUtil.getSatelliteNumber();
	
	if (this.mGpsAdvanceUtil.searchCount > GPSAdvanceUtil.MAX_SEARCH_COUNT){
      this.mHandler.removeMessages(i);
      this.mTimeView.stop();
	  String str_failed_result = getString(R.string.GPS_failed)+GPSAdvanceUtil.VALID_NUMBER
	                                 +getString(R.string.GPS_failed_number)+GPSAdvanceUtil.SNR_THRESHOLD
									 +getString(R.string.GPS_try_again);
      this.mResultView.setText(str_failed_result);
	  return ;
    }else{
      TextView localTextView1 = this.mSatelliteNumView;
      StringBuilder localStringBuilder1 = new StringBuilder();
      String str1 = getString(R.string.GPS_satelliteNum);
      String str2 = str1 + j;
      localTextView1.setText(str2);
      TextView localTextView2 = this.mSignalView;
      StringBuilder localStringBuilder2 = new StringBuilder();
      String str3 = getString(R.string.GPS_Signal);
      StringBuilder localStringBuilder3 = localStringBuilder2.append(str3);
      String str4 = this.mGpsAdvanceUtil.getSatelliteSignals();
      String str5 = str3+str4;
      localTextView2.setText(str5);	  
      //return;
      this.mHandler.sendEmptyMessageDelayed(i,2000L);
    }
	
	if(this.mGpsAdvanceUtil.getSatelliteNumber()>=GPSAdvanceUtil.VALID_NUMBER){
	  this.mHandler.removeMessages(i);
      this.mTimeView.stop();
      this.mResultView.setText(R.string.GPS_Success);
	}
  }

  public void onClick(View paramView)
  {
    SharedPreferences localSharedPreferences = this.mSp;
    int i = R.string.gps_name;
    int j = paramView.getId();
    int k = this.mBtOk.getId();
    int kk = this.mBtFailed.getId();
    if (j == k)
    {
      //Utils.SetPreferences(this, localSharedPreferences, i, "success");
      finish();
 
    }
    if (j == kk)
    {
      //Utils.SetPreferences(this, localSharedPreferences, i, "failed");
      finish();
 
    }
    	
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.gps);
    GPSAdvanceUtil localGpsAdvanceUtil = new GPSAdvanceUtil(this);
    this.mGpsAdvanceUtil = localGpsAdvanceUtil;
    SharedPreferences localSharedPreferences = getSharedPreferences("FactoryMode", 0);
    this.mSp = localSharedPreferences;
    TextView localTextView1 = (TextView)findViewById(R.id.gps_state_id);
    this.mStView = localTextView1;
    TextView localTextView2 = (TextView)findViewById(R.id.gps_satellite_id);
    this.mSatelliteNumView = localTextView2;
    TextView localTextView3 = (TextView)findViewById(R.id.gps_signal_id);
    this.mSignalView = localTextView3;
    TextView localTextView4 = (TextView)findViewById(R.id.gps_result_id);
    this.mResultView = localTextView4;
    Chronometer localChronometer1 = (Chronometer)findViewById(R.id.gps_time_id);
    this.mTimeView = localChronometer1;
    Button localButton1 = (Button)findViewById(R.id.gps_bt_ok);
    this.mBtOk = localButton1;
    Button localButton2 = (Button)findViewById(R.id.gps_bt_failed);
    this.mBtFailed = localButton2;
    this.mBtOk.setOnClickListener(this);
    this.mBtFailed.setOnClickListener(this);
    Chronometer localChronometer2 = this.mTimeView;
    String str = getResources().getString(R.string.GPS_time);
    localChronometer2.setFormat(str);
    this.mStView.setText(R.string.GPS_connect);
    this.mTimeView.start();
    getSatelliteInfo();
  }

  protected void onDestroy()
  {
    this.mGpsAdvanceUtil.closeLocation();
    super.onDestroy();
  }
}