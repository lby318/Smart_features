package com.android.factory;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;//Tim add for LM-113
import android.widget.TextView;
import android.os.SystemProperties;
import android.graphics.Color;
import android.os.ServiceManager;
import android.os.RemoteException;

/* hetian add start for wifi address 20160112 */
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
/* hetian add end for wifi address 20160112 */

public class FactoryActivity extends Activity implements OnClickListener {
	private final static String TAG = "FactoryActivity";
	public final static String FACTOR_ITEM = "intent.factory.item";
	public final static String FACTOR_NAME = "intent.factory.name";

    public final static int     ITEM_SixItem_KEY        = 0;
    public final static int     ITEM_BACKLIGHT          = 1;
    public final static int     ITEM_LCD                = 2;
    public final static int     ITEM_FLASH_LAMP         = 3;
    public final static int     ITEM_MICROPHONE         = 4;
    public final static int     ITEM_LOUDSPEAKER        = 5;
    public final static int     ITEM_EARPHONE           = 6;
    public final static int     ITEM_MOTO               = 7;
    public final static int     ITEM_BLUETOOTH          = 8;
    public final static int     ITEM_WIFI               = 9;
    public final static int     ITEM_GPS                = 10;
    public final static int     ITEM_CAMERA             = 11;
    public final static int     ITEM_A_SENSOR           = 12;
    public final static int     ITEM_SD_CARD            = 13;
    public final static int     ITEM_FM                 = 14;
    public final static int     ITEM_SCREEN             = 15;
    public final static int     ITEM_EARPIECE        	= 16;
    //public final static int     ITEM_KEYPADLIGHT        = 17;
    public final static int     ITEM_SIM                = 17;
    public final static int     ITEM_CHARGER		= 18;
    public final static int     ITEM_LIGHTSENSOR        = 19;//starmen
    //public final static int     ITEM_LED                = 20;//moduanke
    public final static int     ITEM_GYROSCOPE_SENSOR   = 20;
	public final static int     ITEM_COMPASS            = 21;//Tim add 20161209
    public final static int     ITEM_MAX                = 22;//zgq modify default:22
    private final static int    REQUEST_OFFSET          = 100;

    private TextView[]          mTextViews;
    private String[]            mViewName;
    private int[]               mStatus;
    private Button              mStartBtn;
    private Button              mResetBtn;
    private boolean             mAutoTest = false;
    private int                 mTestIndex = 0;
    private Button              mAutobtn;    
    private TextView mCalibrationResult;

	/*hetian add start for mac addresss 20160112*/
	private TextView mMacAddress;
	private WifiManager mWifiManager;
	private String macAddress;
	/*hetian add end for mac addresss 20160112*/

	private RelativeLayout mRLayout;//Tim add for LM-113

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int     i = 0;
        setContentView(R.layout.main_short);
        //Tim add for LM-113 disable control the status bar
        mRLayout = (RelativeLayout)findViewById(R.id.content);
        mRLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED);
        //Tim add end
		//Tim 20150721 modify for close system torch
		//Intent it = new Intent("CAMERA_APP_LAUNCHED");//moka add fiexed CC-320
		//Intent it = new Intent("android.intent.action.ACTION_SHUTDOWN_FLASHLIGHT");//Tim remove for Android M
		//Tim modify end
        //sendBroadcast(it);//Tim remove for Android M

        mAutobtn = (Button)findViewById(R.id.test_all);
        mViewName = getResources().getStringArray(R.array.items_short);
        mStatus = new int[ITEM_MAX];
        mTextViews = new TextView[ITEM_MAX];
        for(i=0; i<ITEM_MAX; i++){
            mTextViews[i] = (TextView)findViewById(i + R.id.id_text00);
            mTextViews[i].setText(mViewName[i]);
            mTextViews[i].setOnClickListener(this);
            mStatus[i] = -1;
            mTextViews[i].setTag(new Integer(i));
        }
        mAutobtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAutoTest = true;
				startTest(mTestIndex);//starmen modify default;(Integer)(mTextViews[0].getTag()));
			}
		});
	mCalibrationResult=(TextView)findViewById(R.id.calibration_result);
	if(mCalibrationResult!=null){
		String s=ZechinFeatureOptions.getSerialNumber();
		if(s!=null){
			if(s.trim().equals("")){	
				mCalibrationResult.setTextColor(Color.RED);
				mCalibrationResult.setText("bar code:unknown");
					Log.d(TAG, "Bar Code = null");
			}else{
				mCalibrationResult.setTextColor(Color.BLACK);
				mCalibrationResult.setText("bar code:"+s);
			}
		}else{
			mCalibrationResult.setTextColor(Color.RED);
			mCalibrationResult.setText("bar code:unknown");
				Log.d(TAG, "Bar Code = RemoteException");
		}
	}
		/* hetian add start for wifi address 20160112 */
		try {
			mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
			WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
			macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mMacAddress = (TextView) findViewById(R.id.mac_address);
		if (mMacAddress != null) {
			if (macAddress != null) {
				if (macAddress.trim().equals("")) {
					mMacAddress.setTextColor(Color.RED);
					mMacAddress.setText("Wi-Fi MAC address:unknown");
					Log.d(TAG, "MAC address = null");
				} else {
					mMacAddress.setTextColor(Color.BLACK);
					mMacAddress.setText("Wi-Fi MAC address:		" + macAddress);
				}
			} else {
				mMacAddress.setTextColor(Color.RED);
				mMacAddress.setText("Wi-Fi MAC address:unknown");
				Log.d(TAG, "MAC address = RemoteException");
			}
		}
		/* hetian add end for wifi address 20160112 */
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(mCalibrationResult!=null){
			if(mCalibrationResult.getText().equals("bar code:unknown")){
				String sResume=ZechinFeatureOptions.getSerialNumber();;
				if(sResume!=null){
					if(sResume.trim().equals("")){
						mCalibrationResult.setTextColor(Color.RED);
						Log.d(TAG, "Bar Code = null");
						mCalibrationResult.setText("bar code:unknown");
					}else{
						mCalibrationResult.setTextColor(Color.BLACK);
						mCalibrationResult.setText("bar code:"+sResume);
					}
				}else{
					mCalibrationResult.setTextColor(Color.RED);
					Log.d(TAG, "Bar Code = RemoteException");
					mCalibrationResult.setText("bar code:unknown");
				}
			}
		}
		/* hetian add start for wifi address 20160112 */
		if (mMacAddress != null) {
			if (mMacAddress.getText().equals("Wi-Fi MAC address:unknown")) {
				if (macAddress != null) {
					if (macAddress.trim().equals("")) {
						mMacAddress.setTextColor(Color.RED);
						Log.d(TAG, "MAC address = null");
						mMacAddress.setText("Wi-Fi MAC address:unknown");
					} else {
						mMacAddress.setTextColor(Color.BLACK);
						mMacAddress.setText("Wi-Fi MAC address:		" + macAddress);
					}
				} else {
					mMacAddress.setTextColor(Color.RED);
					Log.d(TAG, "MAC address = RemoteException");
					mMacAddress.setText("Wi-Fi MAC address:unknown");
				}
			}
		}
		/* hetian add end for wifi address 20160112 */
	}
    public void startTest(int index) {
        Intent          intent = new Intent();
        intent.setClass(this, DetectorActivity.class);
        intent.putExtra(FACTOR_ITEM, index);
        intent.putExtra(FACTOR_NAME, mViewName[index]);
        startActivityForResult(intent, index+REQUEST_OFFSET);
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        int         index = (Integer)v.getTag();

        Log.d(TAG, "onClick:" + "id=" + v.getId() + ",index=" + index);

        startTest(index);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        Log.d(TAG, "request=" + requestCode + ",result=" + resultCode);
        if(requestCode>=REQUEST_OFFSET && requestCode<=ITEM_MAX+REQUEST_OFFSET && null!= data){
            mStatus[requestCode-REQUEST_OFFSET] = data.getFlags();
            if(0 == data.getFlags()){
                mTextViews[requestCode-REQUEST_OFFSET].setCompoundDrawablesWithIntrinsicBounds(null, null, null, getResources().getDrawable(R.drawable.failed));
            }
            else if(1 == data.getFlags()){
                mTextViews[requestCode-REQUEST_OFFSET].setCompoundDrawablesWithIntrinsicBounds(null, null, null, getResources().getDrawable(R.drawable.pass));
            }
            if(mTestIndex >= ITEM_MAX-1){
                mAutoTest = false;
				mTestIndex = 0;//starmen add this
            }
            if(true == mAutoTest){
                mTestIndex ++;
                startTest(mTestIndex);
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
	//moduanke add for factory onRestart state CC-320
	protected void onRestart(){
		super.onRestart();
        //Tim 20150721 modify for close system torch
        //Intent it = new Intent("CAMERA_APP_LAUNCHED");
        //Intent it = new Intent("android.intent.action.ACTION_SHUTDOWN_FLASHLIGHT");//Tim remvoe for android M
        //Tim modify end
        //sendBroadcast(it);//Tim remvoe for android M


	}
	//moduanke add for factory onRestart state

}
