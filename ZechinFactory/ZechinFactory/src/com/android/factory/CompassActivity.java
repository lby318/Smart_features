package com.android.factory;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class CompassActivity extends Activity {
    
    private SensorManager mSensorManager;
    private Sensor aSensor, mSensor;
    private TextView valueView, directionView, compassView,descriptionView;
    
    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    private float   mRotation = 0;// 角度偏转 0 90 180 270 
	private String mOrientaionText[] = new String[]{"北","东北","东","东南","南","西南","西","西北"};
	float[] matrixValue = new float[9];
	float[] sensorValues = new float[3];
	
	final SensorEventListener compassListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				accelerometerValues = event.values.clone();
			}
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				magneticFieldValues = event.values.clone();
			}
			// 调用getRotaionMatrix获得变换矩阵matrixValue[]
			SensorManager.getRotationMatrix(matrixValue, null, accelerometerValues,
					magneticFieldValues);
			SensorManager.getOrientation(matrixValue, sensorValues);
			// 经过SensorManager.getOrientation(matrixValue, sensorValues);得到的values值为弧度
			// 转换为角度
			sensorValues[0] = (float) Math.toDegrees(sensorValues[0]);

			
			compassView.setRotation(-(Math.round(sensorValues[0]) / 5 * 5));
			if(sensorValues[0]<0){
			    valueView.setText(360+Math.round(sensorValues[0])+"°");
			}else{
				valueView.setText(Math.round(sensorValues[0])+"°");
			}
			setOrientaionText(Math.round(sensorValues[0]));
		}
	};
	
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass_layout);
        
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        aSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        
		descriptionView = (TextView)findViewById(R.id.description);
        valueView = (TextView)findViewById(R.id.values);
        directionView = (TextView)findViewById(R.id.direction);
		compassView = (TextView) findViewById(R.id.compass_text);
		
		descriptionView.setText("提示:请用手机正面和反面分别多次画8字.");
		
    }

    protected void onResume() {
        super.onResume();
		mSensorManager.registerListener(compassListener, aSensor,
				SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(compassListener, mSensor,
				SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(compassListener);
    }
	
	private void setOrientaionText(float degrees) {
        if(degrees >= -22.5 && degrees < 22.5) { 
		    directionView.setText(mOrientaionText[0]);
		}else if(degrees >= 22.5 && degrees < 67.5) { 
		    directionView.setText(mOrientaionText[1]);
		}
        else if(degrees >= 67.5 && degrees < 112.5) { 
		    directionView.setText(mOrientaionText[2]);
		}
        else if(degrees >= 112.5 && degrees < 157.5) { 
		    directionView.setText(mOrientaionText[3]);
		}
        else if(degrees >= 157.5 || degrees < -157.5) { 
		    directionView.setText(mOrientaionText[4]); 
		}
        else if(degrees >= -157.5 && degrees < -112.5) { 
		    directionView.setText(mOrientaionText[5]);
		}
        else if(degrees >= -112.5 && degrees < -67.5) { 
		    directionView.setText(mOrientaionText[6]);
		}
        else{
			directionView.setText(mOrientaionText[7]);
		}
    }
}    
