package com.android.factory;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.io.IOException;//starme add for current 20140902
import android.content.ActivityNotFoundException;

public class DetectorActivity extends Activity implements OnClickListener{
    private final   String      TAG = "DetectorActivity";
    private TextView            mInfoText;
    private TextView            mResultText;
    private Button              mPassBtn;
    private Button              mFailedBtn;
    private Button              mAbortBtn;
    private View                mLayout;
    private ImageView           mReportImg;

    private int                 mItemID;
    //zouguanbo 20161201 add for Jria BLUTANKXTREME-96 activity maybe destory
    int mFlag = 1;
    private String              mItemName;
    private boolean             mTestKey = false;
    private int                 mResult = 0;
    private Handler             mHandler;
    private int                 mOrgBackLight;
    private IPowerManager       mPowerManager;
    private Vibrator            mVibrator = null;
    private MediaPlayer         mMediaPlayer;
    private Camera              mCamera;

    private Button              mbackbtn;
    //private boolean				pass[]={false,false,false,false,false,false};
	private boolean				pass[]={false,false,false};
    private TextView            mInfoText1;
    private ImageView           mReportImg1;
    private Button              mPassBtn1;
    private Button              mFailedBtn1;

    private TextView            mInfoText2;
    private ImageView           mReportImg2;
    private Button              mPassBtn2;
    private Button              mFailedBtn2;

    private TextView            mInfoText3;
    private ImageView           mReportImg3;
    private Button              mPassBtn3;
    private Button              mFailedBtn3;

    private TextView            mInfoText4;
    private ImageView           mReportImg4;
    private Button              mPassBtn4;
    private Button              mFailedBtn4;

    private TextView            mInfoText5;
    private ImageView           mReportImg5;
    private Button              mPassBtn5;
    private Button              mFailedBtn5;

    private TextView            mInfoText6;
    private ImageView           mReportImg6;
    private Button              mPassBtn6;
    private Button              mFailedBtn6;

    public static final int     MSGID_BACK = 0;
    public static final int     MSGID_BACKLIGHT_OFF     = 1;
    public static final int     MSGID_BACKLIGHT_255     = 2;
    public static final int     MSGID_BACKLIGHT_OK      = 3;
    public static final int     MSGID_MICROPHONE        = 4;
    public static final int     MSGID_BT                = 5;

    public static final int     MSGID_WIFI              = 6;
    public static final int     MSGID_FM                = 7;
    public static final int     MSGID_CAMERA            = 8;
    public static final int     MSGID_FLASH_LAMP_ON     = 9;
    public static final int     MSGID_FLASH_LAMP_OFF    = 10;
    /* zouguanbo 20161107 add begin for front flash */
    public static final int     MSGID_FRONT_FLASH_LAMP_ON = 60;
    public static final int     MSGID_FRONT_FLASH_LAMP_OFF= 61;
    public static final int     MSGID_FRONT_FLASH_LAMP_OK = 62;
    /* zouguanbo 20161107 add end for front flash */
    public static final int     MSGID_FLASH_LAMP_OK     = 11;

    public static final int     MSGID_SD                = 12;
    public static final int     MSGID_GPS               = 13;
    public static final int     MSGID_LCD               = 14;
    public static final int     MSGID_SCREEN            = 15;

    public static final int     MSGID_EARPIECE          = 16;
    public static final int     MSGID_KEYPADLIGHT       = 17;
    public static final int     MSGID_A_SENSOR               = 18;//starmen

    public static final int     MSGID_LED                   =19;//moduanke
	public static final int     MSGID_COMPASS               =20;//Tim 
    public static final int     MSGID_GYROSCOPE_SENSOR  = 41;
	
    public static final int     DELAY_TIME = 500;
	private static final int DTMF_VOLUME = 1200;

    public static String        HEADSET_STATE_PATH = "/sys/class/switch/h2w/state";
    public HeadsetPlugReceiver  mHeadReceiver;
    public ChargerReceiver  mchargerReceiver;
    public boolean              mFirstReceiver = true;
	/* zouguanbo 20161107 add for front flash */
    public boolean              mSupportedFrontFlash = false;

    private SensorManager       mSensorManager;
    private Sensor              mASensor;
	private Sensor              mLSensor;//starmen
	private LightSensorListener mLightSensorListener;//starmen
    private FactorySensorListener mFactorySensorListener;
	private AudioManager mZZZAudioManager;
	private ToneGenerator mZZZToneGenerator;
	private Ringtone mZZZRingtone;
	private PowerManager.WakeLock wakeLock;

    public class HeadsetPlugReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            int     iStatus;
            // TODO Auto-generated method stub
            if(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)){
                iStatus = intent.getIntExtra("state", -1);
                if(null != mMediaPlayer){
                    if(0 == iStatus){
                        if(true != mFirstReceiver){
                            mMediaPlayer.pause();
                        }
                        mFirstReceiver = false;
                        mInfoText.setText(R.string.no_headset);
                    }
                    else if(1 == iStatus){
						mPassBtn.setEnabled(true);//starmen add for EARPHONE test
                        mMediaPlayer.start();
                        mInfoText.setText(R.string.palying_music);
                    }
                }
            }
        }
    }

    public class ChargerReceiver extends BroadcastReceiver{
    	@Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("android.intent.action.BATTERY_CHANGED")) {
                int istatus = intent.getIntExtra("status", 0);
                StringBuilder sstatus = new StringBuilder(getString(R.string.battery_info_status_label));
                switch(istatus) {
                    case 1:
                    {
                        sstatus.append(getString(R.string.battery_info_status_unknown));
                        break;
                    }
                    case 2:
                    {
                        sstatus.append(getString(R.string.battery_info_status_charging));
                        break;
                    }
                    case 3:
                    {
                        sstatus.append(getString(R.string.battery_info_status_discharging));
                        break;
                    }
                    case 4:
                    {
                        sstatus.append(getString(R.string.battery_info_status_not_charging));
                        break;
                    }
                    case 5:
                    {
                        sstatus.append(getString(R.string.battery_info_status_full));
                        break;
                    }
                }
                int iplugged = intent.getIntExtra("plugged", 0);
                switch(iplugged) {
	                case 1:
	                {
	                    sstatus.append(getString(R.string.battery_info_status_charging_ac));
	                    break;
	                }                
                    case 2:
                    {
                        sstatus.append(getString(R.string.battery_info_status_charging_usb));
                        break;
                    }

                }
                sstatus.append("\n" + getString(R.string.battery_info_health_label));
                int ihealth = intent.getIntExtra("health", 0);
                switch(ihealth) {
                    case 1:
                    {
                    	sstatus.append(getString(R.string.battery_info_status_unknown));
                        break;
                    }
                    case 2:
                    {
                    	sstatus.append(getString(R.string.battery_info_health_good));
                        break;
                    }
                    case 3:
                    {
                    	sstatus.append(getString(R.string.battery_info_health_overheat));
                        break;
                    }
                    case 4:
                    {
                    	sstatus.append(getString(R.string.battery_info_health_dead));
                        break;
                    }
                    case 5:
                    {
                    	sstatus.append(getString(R.string.battery_info_health_over_voltage));
                        break;
                    }
                    case 6:
                    {
                    	sstatus.append(getString(R.string.battery_info_health_unspecified_failure));
                        break;
                    }
                }
                sstatus.append("\n" + getString(R.string.battery_info_level_label));
                int ilevel = intent.getIntExtra("level", 0);
                sstatus.append(ilevel + "%");
                sstatus.append("\n" + getString(R.string.battery_info_scale_label));
                int iscale = intent.getIntExtra("scale", 0);
                sstatus.append(iscale + "%");
                sstatus.append("\n" + getString(R.string.battery_info_voltage_label));
                int ivoltage = intent.getIntExtra("voltage", 0);
                sstatus.append(ivoltage + "mV");
                sstatus.append("\n" + getString(R.string.battery_info_temperature_label));
                int item = (intent.getIntExtra("temperature", 0) /10);
                sstatus.append(item + getString(R.string.battery_info_temperature_units));
				//starme begin add for current 20140902
				sstatus.append("\n" + getString(R.string.ElectricCurrent));
				sstatus.append(doExec("cat /sys/class/power_supply/battery/BatteryAverageCurrent"));
				sstatus.append("mA");
				//starme end add for current 20140902
                mInfoText.setTextSize(17);
                mInfoText.setText(sstatus.toString());
            }
    	};    
    }    
    
    private class FactorySensorListener implements SensorEventListener {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                mInfoText.setText(  "X: " + event.values[0] + "\n" +
                                    "Y: " + event.values[1] + "\n" +
                                    "Z: " + event.values[2] + "\n");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//starmen add for screen off problem is that can not wake screen on
        setContentView(R.layout.detector);
        //zouguanbo 20161201 add for Jria BLUTANKXTREME-96 activity maybe destory
        if (savedInstanceState != null  
            && savedInstanceState.getInt("currentposition") != 0) {
            mFlag = savedInstanceState.getInt("currentposition");
        }

        mInfoText = (TextView)findViewById(R.id.id_info);
        mResultText = (TextView)findViewById(R.id.id_result);
        mReportImg = (ImageView)findViewById(R.id.id_report_image);

        mPassBtn = (Button)findViewById(R.id.id_pass);
        mPassBtn.setOnClickListener(this);
        mFailedBtn = (Button)findViewById(R.id.id_failed);
        mFailedBtn.setOnClickListener(this);
        mAbortBtn = (Button)findViewById(R.id.id_abort);
        mAbortBtn.setOnClickListener(this);

        mLayout = (View)findViewById(R.id.id_report_layout);

        Intent      intent = getIntent();
        mItemID = intent.getIntExtra(FactoryActivity.FACTOR_ITEM, 0);
        mItemName = intent.getStringExtra(FactoryActivity.FACTOR_NAME);
        mMediaPlayer = MediaPlayer.create(this, R.raw.music);

        if (mItemID == FactoryActivity.ITEM_EARPIECE) {
        	mZZZAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        	mZZZAudioManager.setMode(AudioManager.MODE_IN_CALL);
        	mZZZToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, DTMF_VOLUME);
        }

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mASensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mLSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);//starmen
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Intent              intent;
                Camera.Parameters   param;

                // TODO Auto-generated method stub
                switch (msg.what) {
                case MSGID_BACK:
                    setResult(0, new Intent().setFlags(mResult));
                    finish();
                    break;

                case MSGID_BACKLIGHT_OFF:
                case MSGID_BACKLIGHT_255:
                case MSGID_BACKLIGHT_OK:
                    WindowManager.LayoutParams lp = getWindow().getAttributes();
                    if(msg.what == MSGID_BACKLIGHT_OFF){
                        lp.screenBrightness = 0.1f;
                        getWindow().setAttributes(lp);
                    }
                    else if(msg.what == MSGID_BACKLIGHT_255){
                        lp.screenBrightness = 1.0f;
                        getWindow().setAttributes(lp);
                    }
                    else{
                        lp.screenBrightness = 0.6f;
                        getWindow().setAttributes(lp);
                    }
                    break;
                case MSGID_MICROPHONE:
                    intent = new Intent();
                    intent.setClassName("com.android.soundrecorder", "com.android.soundrecorder.SoundRecorder");
                    intent.setAction("com.android.soundrecorder.SoundRecorder");
                    //zouguanbo 20161201 modify for Jria BLUTANKXTREME-96 activity maybe destory
                    //startActivity(intent);
                    factoryStartActivity(intent);
                    break;
                case MSGID_BT:
                    intent = new Intent();
                    intent.setAction("android.intent.action.MAIN");
                    intent.setClassName("com.android.settings", "com.android.settings.bluetooth.BluetoothSettings");
                    //zouguanbo 20161201 modify for Jria BLUTANKXTREME-96 activity maybe destory
                    //startActivity(intent);
                    factoryStartActivity(intent);
                    break;
                case MSGID_WIFI:
                    intent = new Intent();
                    intent.setClassName("com.android.settings", "com.android.settings.wifi.WifiSettings");
                    //zouguanbo 20161201 modify for Jria BLUTANKXTREME-96 activity maybe destory
                    //startActivity(intent);
                    factoryStartActivity(intent);
                    break;
                case MSGID_FM:
                    /* zouguanbo 20150906 modify begin for new Fmradio */
                    try{
                    		intent = new Intent();
                    		intent.setClassName("com.mediatek.fmradio", "com.mediatek.fmradio.FmRadioActivity");
                    		//startActivity(intent);
                    		factoryStartActivity(intent);
                    }catch (ActivityNotFoundException e) {
                    		Log.i(TAG, "cannot launch com.mediatek.fmradio isn't installed.");
                    		intent = new Intent();
                    		intent.setClassName("com.android.fmradio", "com.android.fmradio.FmMainActivity");
                    		//startActivity(intent);
                    		factoryStartActivity(intent);
                    }
                    /* zouguanbo 20150906 modify end for new Fmradio */
                    break;
                case MSGID_CAMERA:
                    /* zouguanbo 20151030 modify for Jria LTDO-4 for google camera */
                    try{
                    		intent = new Intent();
                    		intent.setClassName("com.google.android.GoogleCamera", "com.android.camera.CameraLauncher");
                    		//zouguanbo 20161201 modify for Jria BLUTANKXTREME-96 activity maybe destory
                    		//startActivity(intent);
                    		factoryStartActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                    		Log.i(TAG, "cannot launch com.google.android.GoogleCamera isn't installed.");
                    		intent = new Intent();
                    		intent.setClassName("com.mediatek.camera", "com.android.camera.CameraActivity");
                    		//zouguanbo 20161201 modify for Jria BLUTANKXTREME-96 activity maybe destory
                    		//startActivity(intent);
                    		factoryStartActivity(intent);
                    }
                    /* zouguanbo 20151030 modify for Jria LTDO-4 for google camera */
                    break;
                case MSGID_FLASH_LAMP_ON:
                    if(null != mCamera){
                        mCamera.startPreview();
                        param = mCamera.getParameters();
                        param.setFlashMode("torch");
                        mCamera.setParameters(param);
                    }
                    break;
				case MSGID_FLASH_LAMP_OFF:
                    if(null != mCamera){
                        mCamera.startPreview();
                        param = mCamera.getParameters();
                        param.setFlashMode("off");
                        mCamera.setParameters(param);
                    }
                    break;
                /* zouguanbo 20161107 add begin for front flash */
                case MSGID_FRONT_FLASH_LAMP_OFF:
                    if(null != mCamera){
                        mCamera.startPreview();
                        param = mCamera.getParameters();
                        param.setFlashMode("off");
                        mCamera.setParameters(param);
                    }
                    break;
                case MSGID_FRONT_FLASH_LAMP_ON:
                    if(null != mCamera){
                        mCamera.startPreview();
                        param = mCamera.getParameters();
                        param.setFlashMode("torch");
                        mCamera.setParameters(param);
                    }
                    break;
                case MSGID_FRONT_FLASH_LAMP_OK:
                    if(null != mCamera){
                    		mCamera.release();
                    		mCamera = Camera.open(0);
                    }
                    break;
                /* zouguanbo 20161107 add end for front flash */
                case MSGID_FLASH_LAMP_OK:
                    mPassBtn.setEnabled(true);
                    mFailedBtn.setEnabled(true);
                    break;
                case MSGID_SD:
                    intent = new Intent();
                    intent.setClassName("com.android.factory", "com.android.factory.SDCardTesting");
                    //zouguanbo 20161201 modify for Jria BLUTANKXTREME-96 activity maybe destory
                    //startActivity(intent);
                    factoryStartActivity(intent);
                    break;
                case MSGID_GPS:
                    intent = new Intent();
					/// Tim modify for advance GPS test @ {
                    //intent.setClassName("com.android.factory", "com.android.factory.GPS");
					intent.setClassName("com.android.factory", "com.android.factory.GPSAdvance");
					/// Tim modify end @ }
                    //zouguanbo 20161201 modify for Jria BLUTANKXTREME-96 activity maybe destory
                    //startActivity(intent);
                    factoryStartActivity(intent);
                    break;
                case MSGID_A_SENSOR:
                    intent = new Intent();
                    intent.setClassName("com.android.factory", "com.android.factory.SensorTest");
                    //zouguanbo 20161201 modify for Jria BLUTANKXTREME-96 activity maybe destory
                    //startActivity(intent);
                    factoryStartActivity(intent);
                    break;
                case MSGID_GYROSCOPE_SENSOR:
                    intent = new Intent();
                    intent.setClassName("com.android.factory", "com.android.factory.GyroscopeSensorTest");
                    //zouguanbo 20161201 modify for Jria BLUTANKXTREME-96 activity maybe destory
                    //startActivity(intent);
                    factoryStartActivity(intent);
                    break;
                case MSGID_LCD:
                    intent = new Intent();
                    intent.setClass(getApplicationContext(), LcdActivity.class);
                    //zouguanbo 20161201 modify for Jria BLUTANKXTREME-96 activity maybe destory
                    //startActivity(intent);
                    factoryStartActivity(intent);
                    break;
                case MSGID_LED:
                    intent = new Intent();
                    intent.setClass(getApplicationContext(), LEDTest.class);
                    //zouguanbo 20161201 modify for Jria BLUTANKXTREME-96 activity maybe destory
                    //startActivity(intent);
                    factoryStartActivity(intent);
                    break;
				case MSGID_COMPASS:
                    intent = new Intent();
                    intent.setClassName("com.android.factory", "com.android.factory.CompassActivity");
                    //zouguanbo 20161201 modify for Jria BLUTANKXTREME-96 activity maybe destory
                    //startActivity(intent);
                    factoryStartActivity(intent);
                    break;
                case MSGID_SCREEN:
                    intent = new Intent();
					intent.setClassName("com.android.factory", "com.android.factory.TPTest");
                    //zouguanbo 20161201 modify for Jria BLUTANKXTREME-96 activity maybe destory
                    //startActivity(intent);
                    factoryStartActivity(intent);
                    break;
                case MSGID_EARPIECE:
                    try {
                    	if(null == mZZZAudioManager){
                    		mZZZToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, DTMF_VOLUME);
                    	}
                    	mZZZToneGenerator.startTone(ToneGenerator.TONE_DTMF_1, 6000);
                    } catch (Exception e) {}

                	break;
                case MSGID_KEYPADLIGHT:
                    if(wakeLock == null) {
                        PowerManager powermanager = (PowerManager)getSystemService("power");
                        wakeLock = powermanager.newWakeLock(PowerManager.FULL_WAKE_LOCK,"power");
                        wakeLock.acquire();
                    }
                    break;
                default:
                    break;
                }
            }
        };

        init();
    }
    //zouguanbo 20161201 add begin for Jria BLUTANKXTREME-96 activity maybe destory
    private void factoryStartActivity(Intent intent){
    		if(mFlag != 1){
    				return;
    		}
    		startActivity(intent);
        mFlag = 2;
		}
    
    @Override  
    protected void onSaveInstanceState(Bundle outState) {
    		// TODO Auto-generated method stub
    		outState.putInt("currentposition", mFlag);
    		super.onSaveInstanceState(outState);
    }
    //zouguanbo 20161201 add end for Jria BLUTANKXTREME-96 activity maybe destory
    //Tim add for FMRadio test begin
    /*
    private boolean fmRadioIsActive(){
	    final AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        if (am == null) {
            Log.w(TAG, "isFmActive: couldn't get AudioManager reference");
            return false;
        }
        return am.isFmActive();
    }
    */
    private void pauseFmRaidoBackground(){						
              //if(fmRadioIsActive()){
				//android.util.Log.d("Tim-wj","fmRadioIsActive and need to pause fmraido");
				final Intent i = new Intent("com.android.music.musicservicecommand");
                 i.putExtra("command", "pause");
                 sendBroadcast(i);
              //}
    }
    //Tim add end
	/*
    @Override
    public void onAttachedToWindow() {
        // TODO Auto-generated method stub
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED);
        super.onAttachedToWindow();
    }
	*/
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_HOME && mItemID == FactoryActivity.ITEM_SixItem_KEY){
        	//mReportImg4.setVisibility(View.VISIBLE);
			//mReportImg4.setImageResource(R.drawable.pass);
			//pass[3]=true;
            return true;
        }
        else{
            return super.dispatchKeyEvent(event);
        }
    }

    private void init() {
		int[] slotIds = ZechinFeatureOptions.getSlotIds();
		int[] subIds = new int[slotIds.length];
		for (int i = 0; i < slotIds.length; i++){
			subIds[i] = ZechinFeatureOptions.getSubId(slotIds[i]);
		}
        switch (mItemID) {
        case FactoryActivity.ITEM_SixItem_KEY:
            setContentView(R.layout.six_in_one);
            mbackbtn = (Button)findViewById(R.id.id_return);
            mbackbtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int flag=1;
					for(boolean a:pass){
						if(a==false)
							flag=0;
					}
					setResult(0, new Intent().setFlags(flag));
					finish();
				}
			});
            String[] mViewName = getResources().getStringArray(R.array.items);
            mInfoText1 = (TextView)findViewById(R.id.id_info1);
            mInfoText1.setText(mViewName[0]);
            mReportImg1 = (ImageView)findViewById(R.id.id_report_image1);
            mPassBtn1 = (Button)findViewById(R.id.id_pass1);
            mPassBtn1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mReportImg1.setVisibility(View.VISIBLE);
					mReportImg1.setImageResource(R.drawable.pass);
					pass[0]=true;
				}
			});
            mFailedBtn1 = (Button)findViewById(R.id.id_failed1);
            mFailedBtn1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mReportImg1.setVisibility(View.VISIBLE);
					mReportImg1.setImageResource(R.drawable.failed);
		            pass[0]=false;
				}
			});

            mInfoText2 = (TextView)findViewById(R.id.id_info2);
            mInfoText2.setText(mViewName[1]);
            mReportImg2 = (ImageView)findViewById(R.id.id_report_image2);
            mPassBtn2 = (Button)findViewById(R.id.id_pass2);
            mPassBtn2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mReportImg2.setVisibility(View.VISIBLE);
					mReportImg2.setImageResource(R.drawable.pass);
					pass[1]=true;
				}
			});
            mFailedBtn2 = (Button)findViewById(R.id.id_failed2);
            mFailedBtn2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mReportImg2.setVisibility(View.VISIBLE);
					mReportImg2.setImageResource(R.drawable.failed);
					pass[1]=false;
				}
			});

            mInfoText3 = (TextView)findViewById(R.id.id_info3);
            mInfoText3.setText(mViewName[2]);
            mReportImg3 = (ImageView)findViewById(R.id.id_report_image3);
            mPassBtn3 = (Button)findViewById(R.id.id_pass3);
            mPassBtn3.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mReportImg3.setVisibility(View.VISIBLE);
					mReportImg3.setImageResource(R.drawable.pass);
					pass[2]=true;
				}
			});
            mFailedBtn3 = (Button)findViewById(R.id.id_failed3);
            mFailedBtn3.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mReportImg3.setVisibility(View.VISIBLE);
					mReportImg3.setImageResource(R.drawable.failed);
					pass[2]=false;
				}
			});
        /**
            mInfoText4 = (TextView)findViewById(R.id.id_info4);
            mInfoText4.setText(mViewName[3]);
            mReportImg4 = (ImageView)findViewById(R.id.id_report_image4);
            mPassBtn4 = (Button)findViewById(R.id.id_pass4);
            mPassBtn4.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mReportImg4.setVisibility(View.VISIBLE);
					mReportImg4.setImageResource(R.drawable.pass);
					pass[3]=true;
				}
			});
            mFailedBtn4 = (Button)findViewById(R.id.id_failed4);
            mFailedBtn4.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mReportImg4.setVisibility(View.VISIBLE);
					mReportImg4.setImageResource(R.drawable.failed);
					pass[3]=false;
				}
			});

            mInfoText5 = (TextView)findViewById(R.id.id_info5);
            mInfoText5.setText(mViewName[4]);
            mReportImg5 = (ImageView)findViewById(R.id.id_report_image5);
            mPassBtn5 = (Button)findViewById(R.id.id_pass5);
            mPassBtn5.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mReportImg5.setVisibility(View.VISIBLE);
					mReportImg5.setImageResource(R.drawable.pass);
					pass[4]=true;
				}
			});
            mFailedBtn5 = (Button)findViewById(R.id.id_failed5);
            mFailedBtn5.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mReportImg5.setVisibility(View.VISIBLE);
					mReportImg5.setImageResource(R.drawable.failed);
					pass[4]=false;
				}
			});

            mInfoText6 = (TextView)findViewById(R.id.id_info6);
            mInfoText6.setText(mViewName[5]);
            mReportImg6 = (ImageView)findViewById(R.id.id_report_image6);
            mPassBtn6 = (Button)findViewById(R.id.id_pass6);
            mPassBtn6.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mReportImg6.setVisibility(View.VISIBLE);
					mReportImg6.setImageResource(R.drawable.pass);
					pass[5]=true;
				}
			});
            mFailedBtn6 = (Button)findViewById(R.id.id_failed6);
            mFailedBtn6.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mReportImg6.setVisibility(View.VISIBLE);
					mReportImg6.setImageResource(R.drawable.failed);
					pass[5]=false;
				}
			});*/

            mTestKey = true;
            break;
        case FactoryActivity.ITEM_BACKLIGHT:
            mInfoText.setText(mItemName);
            try {
                mOrgBackLight = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            } catch (Exception e) {
                // TODO: handle exception
            }
            mHandler.sendEmptyMessageDelayed(MSGID_BACKLIGHT_255, 1000);
            mHandler.sendEmptyMessageDelayed(MSGID_BACKLIGHT_OFF, 2000);
            mHandler.sendEmptyMessageDelayed(MSGID_BACKLIGHT_255, 3000);
            mHandler.sendEmptyMessageDelayed(MSGID_BACKLIGHT_OK,  4000);
            break;
        case FactoryActivity.ITEM_LCD:
            mInfoText.setText(mItemName);
            mHandler.sendEmptyMessageDelayed(MSGID_LCD,  100);
            break;
	
	 			/*case FactoryActivity.ITEM_LED:
            mInfoText.setText(mItemName);
            mHandler.sendEmptyMessageDelayed(MSGID_LED,  100);
            break;*/
		
        case FactoryActivity.ITEM_FLASH_LAMP:
            mPassBtn.setEnabled(false);
            mFailedBtn.setEnabled(false);
            mInfoText.setText(mItemName);
            /* zouguanbo 20161107 modify begin for front flash */
            mCamera = Camera.open(1);
            Camera.Parameters param = mCamera.getParameters();
            if(param.getSupportedFlashModes() != null && param.getSupportedFlashModes().size() > 1){
            		mSupportedFrontFlash = true;
            }
            mCamera.release();
            if(mSupportedFrontFlash){
				mCamera = Camera.open(1);
				mHandler.sendEmptyMessageDelayed(MSGID_FLASH_LAMP_ON,  50);
				mHandler.sendEmptyMessageDelayed(MSGID_FLASH_LAMP_OFF, 2000);
				mHandler.sendEmptyMessageDelayed(MSGID_FLASH_LAMP_ON,  3000);
				mHandler.sendEmptyMessageDelayed(MSGID_FLASH_LAMP_OFF, 4000);
				mHandler.sendEmptyMessageDelayed(MSGID_FRONT_FLASH_LAMP_OK, 4100);
				mHandler.sendEmptyMessageDelayed(MSGID_FRONT_FLASH_LAMP_ON,  5100);
				mHandler.sendEmptyMessageDelayed(MSGID_FRONT_FLASH_LAMP_OFF, 6100);
				mHandler.sendEmptyMessageDelayed(MSGID_FRONT_FLASH_LAMP_ON,  7100);
				mHandler.sendEmptyMessageDelayed(MSGID_FRONT_FLASH_LAMP_OFF, 8100);
				mHandler.sendEmptyMessageDelayed(MSGID_FLASH_LAMP_OK, 8200);
            }else{
            mCamera = Camera.open();
            mHandler.sendEmptyMessageDelayed(MSGID_FLASH_LAMP_ON,  50);
            mHandler.sendEmptyMessageDelayed(MSGID_FLASH_LAMP_OFF, 1000);
            mHandler.sendEmptyMessageDelayed(MSGID_FLASH_LAMP_ON,  2000);
            mHandler.sendEmptyMessageDelayed(MSGID_FLASH_LAMP_OFF, 3000);
            mHandler.sendEmptyMessageDelayed(MSGID_FLASH_LAMP_OK, 3100);
            }
			/* zouguanbo 20161107 modify ens for front flash */
            break;
        case FactoryActivity.ITEM_MICROPHONE:
            mInfoText.setText(mItemName);
            mHandler.sendEmptyMessageDelayed(MSGID_MICROPHONE,  500);
            break;
        case FactoryActivity.ITEM_LOUDSPEAKER:
            mInfoText.setText(mItemName);
            if(null != mMediaPlayer){
                mMediaPlayer.start();
            }
            break;
        case FactoryActivity.ITEM_EARPHONE:
            FileReader      fileReader = null;
            char            szBuf[] = new char[10];
            int             iStatus = 0;
			mPassBtn.setEnabled(false);//starmen add for EARPHONE test
            try {
                fileReader = new FileReader(HEADSET_STATE_PATH);
                fileReader.read(szBuf, 0, 10);
            } catch (Exception e) {
                // TODO: handle exception
            }

            iStatus = Integer.valueOf(new String(szBuf, 0, 1)).intValue();
            if(0 == iStatus){
                mInfoText.setText(R.string.no_headset);

            }
            else{
				mPassBtn.setEnabled(true);//starmen add for EARPHONE test
                mInfoText.setText(R.string.palying_music);
                mMediaPlayer.start();
            }
            mHeadReceiver = new HeadsetPlugReceiver();
            IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            registerReceiver(mHeadReceiver, filter);
            break;
        case FactoryActivity.ITEM_MOTO:
            mInfoText.setText(mItemName);
            mVibrator = (Vibrator)getSystemService("vibrator");
            mVibrator.vibrate(3000L);
            break;
        case FactoryActivity.ITEM_BLUETOOTH:
            mInfoText.setText(mItemName);
            mHandler.sendEmptyMessageDelayed(MSGID_BT,  500);
            break;
        case FactoryActivity.ITEM_WIFI:
            mInfoText.setText(mItemName);
            mHandler.sendEmptyMessageDelayed(MSGID_WIFI,  500);
            break;
		case FactoryActivity.ITEM_LIGHTSENSOR:
            mInfoText.setText(mItemName);
            //mResultText.setText(R.string.Lsensor);
            //mLightSensorListener = new LightSensorListener();
            //mSensorManager.registerListener(mLightSensorListener, mLSensor, SensorManager.SENSOR_DELAY_GAME);
			Uri localUri = Uri.fromParts("tel", "112", null);
            Intent localIntent = new Intent("android.intent.action.CALL_PRIVILEGED", localUri);
            startActivity(localIntent);
            break;

        case FactoryActivity.ITEM_GPS:
            mInfoText.setText(mItemName);
            mHandler.sendEmptyMessageDelayed(MSGID_GPS,  500);
            break;
        case FactoryActivity.ITEM_CAMERA:
            mInfoText.setText(mItemName);
            mHandler.sendEmptyMessageDelayed(MSGID_CAMERA,  500);
            break;
        case FactoryActivity.ITEM_A_SENSOR:
			            mInfoText.setText(mItemName);
            mHandler.sendEmptyMessageDelayed(MSGID_A_SENSOR,  500);
          //  mInfoText.setText(mItemName);
           // mResultText.setText(R.string.sensor);
          //  mFactorySensorListener = new FactorySensorListener();
          //  mSensorManager.registerListener(mFactorySensorListener, mASensor, SensorManager.SENSOR_DELAY_GAME);
            break;
        case FactoryActivity.ITEM_GYROSCOPE_SENSOR:
			            mInfoText.setText(mItemName);
            mHandler.sendEmptyMessageDelayed(MSGID_GYROSCOPE_SENSOR,  500);
          //  mInfoText.setText(mItemName);
           // mResultText.setText(R.string.sensor);
          //  mFactorySensorListener = new FactorySensorListener();
          //  mSensorManager.registerListener(mFactorySensorListener, mASensor, SensorManager.SENSOR_DELAY_GAME);
            break;
		case FactoryActivity.ITEM_COMPASS:
			mInfoText.setText(mItemName);
            mHandler.sendEmptyMessageDelayed(MSGID_COMPASS,  500);
            break;	
        case FactoryActivity.ITEM_SD_CARD:
			//starmen add for SDCard begin
			if(SDCardTesting.isSDCardExist()){
				mPassBtn.setEnabled(true);
			}else{
				mPassBtn.setEnabled(false);
			}
			//starmen add for SDCard end
            mInfoText.setText(mItemName);
            mHandler.sendEmptyMessageDelayed(MSGID_SD,  500);
            break;
        case FactoryActivity.ITEM_FM:
            mInfoText.setText(mItemName);
            mHandler.sendEmptyMessageDelayed(MSGID_FM,  500);
            break;
        case FactoryActivity.ITEM_SCREEN:
            mInfoText.setText(mItemName);
            mHandler.sendEmptyMessageDelayed(MSGID_SCREEN,  500);
            break;
        case FactoryActivity.ITEM_EARPIECE:
        	mInfoText.setText(mItemName);
        	mHandler.sendEmptyMessageDelayed(MSGID_EARPIECE,  500);
        	break;
       //tianyunfei 201604108 remove keypadlight for 4065
        /*case FactoryActivity.ITEM_KEYPADLIGHT:
            mInfoText.setText(mItemName);
            mHandler.sendEmptyMessageDelayed(MSGID_KEYPADLIGHT,  500);
            break;*/
        case FactoryActivity.ITEM_SIM:
			boolean isSimPass=false;
			mPassBtn.setEnabled(false);//starmen add for SIM test
        	StringBuilder sb = new StringBuilder();
        	TelephonyManager mGeminiPhone = (TelephonyManager)getSystemService("phone");
			if(ZechinFeatureOptions.isDualSim()){//starmen add
	    	 String mGeminiSim1 = mGeminiPhone.getSimOperator(subIds[0]);
	    	 String mGeminiSim2 = mGeminiPhone.getSimOperator(subIds[1]);
	    	 if((mGeminiSim1 !=null)&&!(mGeminiSim1.equals(""))) {
	    		 sb.append(getString(R.string.sim1_info_ok));
				 sb.append(",MCCMNC=");
				 sb.append(mGeminiSim1);
				 isSimPass=true;
	    	 }else{
	    		 sb.append(getString(R.string.sim1_info_failed));
				 isSimPass=false;
	    	 }
	    	 sb.append("\n");
	    	 if((mGeminiSim2 != null)&&!(mGeminiSim2.equals(""))) {
	    		 sb.append(getString(R.string.sim2_info_ok));
				 sb.append(",MCCMNC=");
				 sb.append(mGeminiSim2);
				 if(isSimPass){
				 	isSimPass=true;
				 }
	    	 }else{
	    		 sb.append(getString(R.string.sim2_info_failed));
				 isSimPass=false;
	    	 }
				if(isSimPass){
					mPassBtn.setEnabled(true);
				}
			 }else{
					String mGeminiSim = mGeminiPhone.getSimOperator();
			if((mGeminiSim !=null)&&!(mGeminiSim.equals(""))) {
	    		 sb.append(getString(R.string.sim_info_ok));
				 sb.append(",MCCMNC=");
				 sb.append(mGeminiSim);
				 isSimPass=true;
	    	 }else{
	    		 sb.append(getString(R.string.sim_info_failed));
				 isSimPass=false;
	    	 }
				if(isSimPass){
					mPassBtn.setEnabled(true);
				}
			 
			 }
            mInfoText.setText(sb.toString());
            break;
        case FactoryActivity.ITEM_CHARGER:
        	mchargerReceiver = new ChargerReceiver();           
            IntentFilter intentfilter = new IntentFilter();
            intentfilter.addAction("android.intent.action.BATTERY_CHANGED");
            registerReceiver(mchargerReceiver, intentfilter);
            break;
        default:
            break;
        }
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.id_pass:
            setResult(0, new Intent().setFlags(1));
            finish();
            break;
        case R.id.id_failed:
        case R.id.id_abort:
            setResult(0, new Intent().setFlags(0));
            finish();
            break;
        default:
            break;
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if(null != mHandler){//starmen add this
 			mHandler.removeMessages(MSGID_FLASH_LAMP_OFF);
			mHandler.removeMessages(MSGID_FLASH_LAMP_ON);
			/* zouguanbo 20161107 add begin for front flash */
			mHandler.removeMessages(MSGID_FRONT_FLASH_LAMP_OK);
 			mHandler.removeMessages(MSGID_FRONT_FLASH_LAMP_OFF);
			mHandler.removeMessages(MSGID_FRONT_FLASH_LAMP_ON);
			/* zouguanbo 20161107 add end for front flash */
		}
        if(null != mVibrator){
            mVibrator.cancel();
        }
        if(null != mMediaPlayer){
            mMediaPlayer.release();
        }
        if(null != mCamera){
            mCamera.release();
        }
        if(null != mHeadReceiver){
            unregisterReceiver(mHeadReceiver);
        }
        if(null != mchargerReceiver){
            unregisterReceiver(mchargerReceiver);
        }
        if(null != mFactorySensorListener){
            mSensorManager.unregisterListener(mFactorySensorListener);
        }
        if(null != mLightSensorListener){//starmen
            mSensorManager.unregisterListener(mLightSensorListener);
        }
		pauseFmRaidoBackground();//	Tim add for FMRadio test

        if(null != mZZZAudioManager) mZZZAudioManager.setMode(AudioManager.MODE_NORMAL);
        if(null != mZZZToneGenerator) mZZZToneGenerator.release();
        mZZZAudioManager = null;
        mZZZToneGenerator = null;
        try {
			if (null != mZZZRingtone && mZZZRingtone.isPlaying()) {
				mZZZRingtone.stop();
				mZZZRingtone = null;
			}
		} catch (Exception e) {}
        if((wakeLock != null) && (wakeLock.isHeld())) {
            wakeLock.release();
            wakeLock = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(true == mTestKey){
            if(KeyEvent.KEYCODE_POWER == keyCode){
                mReportImg1.setVisibility(View.VISIBLE);
				mReportImg1.setImageResource(R.drawable.pass);
				pass[0]=true;
            }
            if(KeyEvent.KEYCODE_VOLUME_UP == keyCode){
            	mReportImg2.setVisibility(View.VISIBLE);
				mReportImg2.setImageResource(R.drawable.pass);
				pass[1]=true;
            }
            if(KeyEvent.KEYCODE_VOLUME_DOWN == keyCode){
            	mReportImg3.setVisibility(View.VISIBLE);
				mReportImg3.setImageResource(R.drawable.pass);
				pass[2]=true;
            }
            if(KeyEvent.KEYCODE_MENU == keyCode){
            	//mReportImg5.setVisibility(View.VISIBLE);
				//mReportImg5.setImageResource(R.drawable.pass);
				//pass[4]=true;
            }
            if(KeyEvent.KEYCODE_BACK == keyCode){
            	//mReportImg6.setVisibility(View.VISIBLE);
				//mReportImg6.setImageResource(R.drawable.pass);
				//pass[5]=true;
            }
            return true;
        }
        else{
            return super.onKeyUp(keyCode, event);
        }
    }
	private class LightSensorListener implements SensorEventListener {//starmen
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		}
		public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
			if(event.sensor.getType() == Sensor.TYPE_LIGHT){
				mInfoText.setText(  "LM: " + event.values[0] + "\n");
			}
		}
	}
    @Override
    public void onPause() {//starmen add
    	super.onPause();
    	if(mItemID==FactoryActivity.ITEM_SixItem_KEY){
			finish();
		}
    }
	//starme begin add for current 20140902
	private String doExec(String cmd){
	   StringBuilder s = new StringBuilder();
       try{
           Process p = Runtime.getRuntime().exec(cmd);
           BufferedReader in = new BufferedReader(
                               new InputStreamReader(p.getInputStream()));
           String line = null;
           while ((line = in.readLine()) != null){
               s.append(line);
           }
       } catch (IOException e){
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
       return s.toString();
       }
	//starme end add for current 20140902
}
