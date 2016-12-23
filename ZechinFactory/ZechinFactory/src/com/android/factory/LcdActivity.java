package com.android.factory;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class LcdActivity extends Activity{
    private TextView        mLcdTextView;
    private Handler         mHandler;
    
    private final int       MSGID_LCD_RED       = 0;
    private final int       MSGID_LCD_GREEN     = 1;
    private final int       MSGID_LCD_BLUE      = 2;
    private final int       MSGID_LCD_YELLOW    = 4;
    private final int       MSGID_LCD_WHITE     = 5;
    private final int       MSGID_LCD_OK        = 6;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.lcd);
        
        mLcdTextView = (TextView)findViewById(R.id.id_lcd);
        
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                case MSGID_LCD_RED:     mLcdTextView.setBackgroundColor(0xFFFF0000);    break;
                case MSGID_LCD_GREEN:   mLcdTextView.setBackgroundColor(0xFF00FF00);    break;
                case MSGID_LCD_BLUE:    mLcdTextView.setBackgroundColor(0xFF0000FF);    break;
                case MSGID_LCD_YELLOW:  mLcdTextView.setBackgroundColor(0xFFFFFF00);    break;
                case MSGID_LCD_WHITE:   mLcdTextView.setBackgroundColor(0xFFFFFFFF);    break;
                case MSGID_LCD_OK:      finish();                                       break;
                default:
                    break;
                }
            }
        };
    }
    
    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        mHandler.sendEmptyMessageDelayed(MSGID_LCD_RED,     1000);
        mHandler.sendEmptyMessageDelayed(MSGID_LCD_GREEN,   2000);
        mHandler.sendEmptyMessageDelayed(MSGID_LCD_BLUE,    3000);
        mHandler.sendEmptyMessageDelayed(MSGID_LCD_YELLOW,  4000);
        mHandler.sendEmptyMessageDelayed(MSGID_LCD_WHITE,   5000);
        mHandler.sendEmptyMessageDelayed(MSGID_LCD_OK,      6000);
        super.onStart();
    }
}
