package com.android.factory;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;


public class GyroscopeSensorTest extends Activity {
	
	private float mCurrentShiftX;
    private float mCurrentShiftY;
	
	private int mShiftDistancePX;
	private int mViewWidth;
    private int mViewHeight;
	private static float mTransformFactor = 0.04f;/*设置移动的补偿变量，越高移动越快*/
	private View mParallelView;
	private ViewGroup.LayoutParams mLayoutParams;
	private TextView gyroscopeView;
	private SensorManager mSensorManager;
    private Sensor mSensor;	
	final SensorEventListener mSensorListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSensorChanged(SensorEvent event) {
		    float x = event.values[SensorManager.DATA_X];
            float y = event.values[SensorManager.DATA_Y];

            mCurrentShiftX += mShiftDistancePX * y * mTransformFactor;
            mCurrentShiftY += mShiftDistancePX * x * mTransformFactor;		
			
			if (Math.abs(mCurrentShiftX) > mShiftDistancePX)
                mCurrentShiftX = mCurrentShiftX < 0 ? -mShiftDistancePX : mShiftDistancePX;

            if (Math.abs(mCurrentShiftY) > mShiftDistancePX)
                mCurrentShiftY = mCurrentShiftY < 0 ? -mShiftDistancePX : mShiftDistancePX;

            //默认就margin 负的边距尺寸，因此 margin的最大值是 负的边距尺寸*2 ~ 0
            mParallelView.setX((int) mCurrentShiftX - mShiftDistancePX);
            mParallelView.setY((int) mCurrentShiftY - mShiftDistancePX);
		}
	};
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.gyroscope_layout);
		
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		
		mShiftDistancePX = this.getResources().getDimensionPixelSize(R.dimen.image_shift);
        gyroscopeView = (TextView)findViewById(R.id.description);
		
		mParallelView = findViewById(R.id.gyroscope_image_background);		
		mParallelView.setX(-mShiftDistancePX);
        mParallelView.setY(-mShiftDistancePX);
        mLayoutParams = mParallelView.getLayoutParams();
        mViewWidth = mParallelView.getWidth();
        mViewHeight = mParallelView.getHeight();
		
		gyroscopeView.setText(R.string.gyroscopeDescription);
		
		if (mViewWidth > 0 && mViewHeight > 0) {
            bindView();
            return;
        }

        ViewTreeObserver vto = mParallelView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mParallelView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mViewWidth = mParallelView.getWidth();
                mViewHeight = mParallelView.getHeight();
                bindView();
            }
        });
    }

	void bindView() {
        mLayoutParams.width = mViewWidth + mShiftDistancePX * 2;
        mLayoutParams.height = mViewHeight + mShiftDistancePX * 2;
        mParallelView.setLayoutParams(mLayoutParams);
    }
	
    protected void onResume() {
        super.onResume();		
		mSensorManager.registerListener(mSensorListener, mSensor,
				SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause() {
        super.onPause();		
		mSensorManager.unregisterListener(mSensorListener);
    }	
}    
