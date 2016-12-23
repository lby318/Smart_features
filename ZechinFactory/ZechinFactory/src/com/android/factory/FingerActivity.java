package com.android.factory;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class FingerActivity extends Activity{
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        
        setContentView(new MyView(this));
    }    
    
    public class MyView extends View{
        private Bitmap      mBitmap;
        private Canvas      mCanvas;
        private Path        mPath;
        private Paint       mPaint;
        private float       mX;
        private float       mY;
        
        public MyView(Context context) {
            // TODO Auto-generated constructor stub
            super(context);
            
            mBitmap = Bitmap.createBitmap(getResources().getDisplayMetrics().widthPixels, 
                                          getResources().getDisplayMetrics().heightPixels,
                                          Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            
            mPath = new Path();
            
            mPaint = new Paint();
            mPaint.setColor(Color.BLUE);
            mPaint.setStrokeWidth(18);
            mPaint.setStyle(Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.MITER);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setAntiAlias(true);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);
            
            canvas.drawColor(0xFFCCCCCC);
            mCanvas.drawPath(mPath, mPaint);
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // TODO Auto-generated method stub
            
            float     x = event.getX();
            float     y = event.getY();
            
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPath.reset();
                    mPath.moveTo(x, y);
                    invalidate();
                    mX = x;
                    mY = y;
                    return true;
                    
                case MotionEvent.ACTION_MOVE:
                    float   dx = Math.abs(mX-x);
                    float   dy = Math.abs(mY-y);
                    if(dx>4 || dy>4){ 
                        mPath.quadTo(mX, mY, (mX+x)/2, (mY+y)/2);
                        mX = x;
                        mY = y;
                    }                    
                    invalidate();
                    return true;
                    
                case MotionEvent.ACTION_UP:
                    mPath.lineTo(x, y);
                    invalidate();
                    mPath.reset();
                    return true;
                    
                default:
                    break;
            }
            
            return super.onTouchEvent(event);
        }
    }
}
