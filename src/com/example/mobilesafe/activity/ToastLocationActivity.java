package com.example.mobilesafe.activity;

import com.example.mobilesafe.R;
import com.example.mobilesafe.utils.ConstantValue;
import com.example.mobilesafe.utils.SpUtil;

import android.app.Activity;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class ToastLocationActivity extends Activity {
      private ImageView dragToast;
      private LayoutParams layoutParam;
	  private WindowManager mWM;
	  private int mHeight;
	  private int mWidth;
	  private Button btn_top;
	  private Button btn_bottom;
	  private long[] mHits = new long[2];

	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_toast_location);
    	
    	initUI();
    }

	private void initUI() {
		btn_top = (Button) findViewById(R.id.btn_top);
		btn_bottom = (Button) findViewById(R.id.btn_bottom);
		dragToast = (ImageView) findViewById(R.id.dragToast);
		
		mWM = (WindowManager)getSystemService(WINDOW_SERVICE);
		//屏幕宽高
		mHeight = mWM.getDefaultDisplay().getHeight();
		mWidth = mWM.getDefaultDisplay().getWidth();
		
		//回显
		int locationX = SpUtil.getInt(getApplicationContext(), ConstantValue.LOCATION_X, 0);
		int locationY = SpUtil.getInt(getApplicationContext(), ConstantValue.LOCATION_Y, 0);
		layoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParam.leftMargin = locationX;
		layoutParam.topMargin = locationY;
		dragToast.setLayoutParams(layoutParam);
		if(locationY>mHeight/2){
			btn_bottom.setVisibility(View.INVISIBLE);
			btn_top.setVisibility(View.VISIBLE);
		}else{
			btn_bottom.setVisibility(View.VISIBLE);
			btn_top.setVisibility(View.INVISIBLE);
		}
		
		dragToast.setOnTouchListener(new OnTouchListener() {
			private int startX;
			private int startY;

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					
					break;
				case MotionEvent.ACTION_MOVE:
					int moveX = (int) event.getRawX();
					int moveY = (int) event.getRawY();
					
					//移动后相对于初始化坐标的偏移量
					int disX = moveX - startX;
					int disY = moveY - startY;
					
					//当前控件所在屏幕的位置
					int left = disX + dragToast.getLeft();
					int right = disX + dragToast.getRight();
					int top = disY + dragToast.getTop();
					int bottom = disY + dragToast.getBottom();
					
					//如果拖拽超出宽度，拖拽结束
					if(left<0 || right>mWidth || top<0 || bottom>mHeight-20){
						return true;
					}
					
					//如果控件的位置超过屏幕高度的一半，则底部的button会消失，顶部的button出现，反之
					if(top>mHeight/2){
						btn_bottom.setVisibility(View.INVISIBLE);
						btn_top.setVisibility(View.VISIBLE);
					}else{
						btn_bottom.setVisibility(View.VISIBLE);
						btn_top.setVisibility(View.INVISIBLE);
					}
					
					//告知移动的控件，按计算出来的坐标做展示
					dragToast.layout(left, top, right, bottom);
					
					//重置初始坐标
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					
					break;
				case MotionEvent.ACTION_UP:
				    //记录拖拽后的坐标
					SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_X, dragToast.getLeft());
					SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_Y, dragToast.getTop());
					
					break;
				}
				//又有拖拽又有点击时,返回false点击才会有效
				return false;
			}
		});
		
		//双击居中
		dragToast.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);
				mHits[mHits.length-1] = SystemClock.uptimeMillis();
				if(mHits[mHits.length-1]-mHits[0]<500){
					int top = mHeight/2-dragToast.getHeight()/2;
					int left = mWidth/2-dragToast.getWidth()/2;
					int right = mWidth/2+dragToast.getWidth()/2;
					int bottom = mHeight/2+dragToast.getHeight()/2;
					
					dragToast.layout(left, top, right, bottom);
					
					SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_X,left);
					SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_Y,top);
				}
				
			}
		});
		
	}
}
