package com.android.factory;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.util.Log;
import android.view.View;
import android.content.Context;
import android.app.Notification;
import android.app.NotificationManager;

public class LEDTest extends Activity implements View.OnClickListener{

	private Button mRedButton;
	private Button mGreenButton;
	private Button mBlueButton;
	private Button mWhiteButton;
	private Handler mHandler;
	private int status;
	
	private static int NONE_STATUS = 0;
	private static int RED_STATUS = 1;
	private static int GREEN_STATUS = 2;
	private static int BLUE_STATUS = 3;
	
	private static int RED_NOTIFICATION_ID = 6555;
	private static int GREEN_NOTIFICATION_ID = 6556;
	private static int BLUE_NOTIFICATION_ID = 6557;
	
	
    private final int       MSGID_LED_RED       = 4;
    private final int       MSGID_LED_GREEN     = 5;
    private final int       MSGID_LED_BLUE      = 6;
    private final int       MSGID_LED_OK      = 7;
	
	private Context mContext;
	private Notification mNotification;
	private NotificationManager mNotificationManager;

	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.led_layout);
		mRedButton = (Button)findViewById(R.id.led_button);
		mRedButton.setOnClickListener(this);
	    
		//mGreenButton = (Button)findViewById(R.id.green_button);
		//mGreenButton.setOnClickListener(this);
		//mBlueButton = (Button)findViewById(R.id.blue_button);
		//mBlueButton.setOnClickListener(this);
		
		mContext = this;
		/* zouguanbo 20160330 modify for Jria AM-130 LED need icon */
		//mNotification = new Notification();
		mNotification = new Notification.Builder(this).setSmallIcon(R.drawable.ic_launcher).getNotification();
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		
		status = NONE_STATUS;
		
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                case MSGID_LED_RED:   
                	closeLED();
                	//Log.e("MOKA",">>>>>MSGID_LED_RED");
                	//mLcdTextView.setBackgroundColor(0xFFFF0000);
    			  mNotification.flags |= Notification.FLAG_SHOW_LIGHTS;
	                mNotification.ledARGB = 0xffff0000;
	                //mNotification.ledOnMS = 400;
	               // mNotification.ledOffMS = 400;
	                mNotificationManager.notify(RED_NOTIFICATION_ID, mNotification);
			        status = RED_STATUS;

                	break;
                	
                case MSGID_LED_GREEN:  
                	closeLED();
                	//Log.e("MOKA",">>>>>MSGID_LED_GREEN");
                	mNotification.flags |= Notification.FLAG_SHOW_LIGHTS;
                	mNotification.ledARGB = 0xff00ff00;
	                //mNotification.ledOnMS = 400;
	               // mNotification.ledOffMS = 400;
	                mNotificationManager.notify(GREEN_NOTIFICATION_ID, mNotification);
	                status = GREEN_STATUS;
                	break;
                
                case MSGID_LED_BLUE: 
                	closeLED();
                	//Log.e("MOKA",">>>>>MSGID_LED_BLUE");
                	mNotification.flags |= Notification.FLAG_SHOW_LIGHTS;
	                mNotification.ledARGB = 0xff0000ff;
	                //mNotification.ledOnMS = 400;
	                //mNotification.ledOffMS = 400;
	                mNotificationManager.notify(BLUE_NOTIFICATION_ID, mNotification);
	                status = BLUE_STATUS;
                	break;
                	
                case MSGID_LED_OK:  
                	/* zouguanbo 20150915 add for Jria TG-25 when test finsh need close led */
                	closeLED();
                	finish();                                  
                	break;
                default:
                    break;
                }
            }
        };
	}
	public void onClick(View v){
		int viewID=v.getId();
		if(status !=NONE_STATUS){
			closeLED();
		}
		switch(viewID){
		
		case R.id.led_button:
			
		mHandler.sendEmptyMessageDelayed(MSGID_LED_RED,     1000);
        	mHandler.sendEmptyMessageDelayed(MSGID_LED_GREEN,   2000);
        	mHandler.sendEmptyMessageDelayed(MSGID_LED_BLUE,    3000);
        	mHandler.sendEmptyMessageDelayed(MSGID_LED_OK,  4000);
		
		}
		
	}
	
	private void closeLED() {
		if (status == RED_STATUS) {
		    mNotificationManager.cancel(RED_NOTIFICATION_ID);
		}else if (status == GREEN_STATUS) {
		    mNotificationManager.cancel(GREEN_NOTIFICATION_ID);
		}else if (status == BLUE_STATUS) {
		    mNotificationManager.cancel(BLUE_NOTIFICATION_ID);
		}
		status = NONE_STATUS;
	}

	public void onPause(){
		super.onPause();
		closeLED();                                  
	}
	
}


