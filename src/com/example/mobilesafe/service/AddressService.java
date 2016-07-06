package com.example.mobilesafe.service;

import com.example.mobilesafe.R;
import com.example.mobilesafe.engine.AddressDao;
import com.example.mobilesafe.utils.ConstantValue;
import com.example.mobilesafe.utils.SpUtil;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.drm.DrmStore.Action;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

public class AddressService extends Service {
    public static final String tag = "AddressService";
	private TelephonyManager mTM;
    private MyPhoneStateListener listener;
    private WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
	private WindowManager mWM;
	private View view;
	private String address;
	private TextView tv_toast;
	private int mHeight;
    private int mWidth;

	private Handler mHandler = new Handler(){

		public void handleMessage(android.os.Message msg) {
			tv_toast.setText(address);
		};
	};
	private InnerOutcallReceiver outcallReceiver;
	
	@Override
    public void onCreate() {
    	mTM = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    	//获取窗体管理者
    	mWM = (WindowManager) getSystemService(WINDOW_SERVICE);
    	//屏幕宽高
		mHeight = mWM.getDefaultDisplay().getHeight();
		mWidth = mWM.getDefaultDisplay().getWidth();
    	
    	listener = new MyPhoneStateListener();
    	mTM.listen(listener,PhoneStateListener.LISTEN_CALL_STATE);
    	
    	//通过广播接收者监听到去电的广播
    	//监听去电广播的过滤条件(加权限)
    	IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
    	outcallReceiver = new InnerOutcallReceiver();
        registerReceiver(outcallReceiver, intentFilter);
    	
    	super.onCreate();
    }
	
	class InnerOutcallReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			//接收到去电广播，弹出吐司
			showToast();
			//获取播出的电话号码
			String phone = getResultData();
			query(phone);
		}
		
	}
	
	class MyPhoneStateListener extends PhoneStateListener{
        @Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				//空闲状态，没有任何活动,移除吐司
				if(mWM !=null && view !=null){
					mWM.removeView(view);	
				}
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				//响铃状态。显示吐司
				showToast();
				query(incomingNumber);
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				//摘机状态，至少有一个电话活动
				break;
			}
			super.onCallStateChanged(state, incomingNumber);

		}

	}

	/**
	 * 查询来电归属地
	 */
private void query(final String incomingNumber) {
		new Thread(){
			public void run() {
				address = AddressDao.getAddress(incomingNumber);
				mHandler.sendEmptyMessage(0);
			};
		}.start();
		
	}

private void showToast() {
		//1.看吐司源码

		final WindowManager.LayoutParams params = mParams;
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
	                //| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE	默认能够被触摸
			| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		params.format = PixelFormat.TRANSLUCENT;
		//在响铃的时候显示吐司,和电话类型一致
		params.type = WindowManager.LayoutParams.TYPE_PHONE;
		params.setTitle("Toast");
		//指定左上角
		params.gravity = Gravity.LEFT+Gravity.TOP;
		
		//自定义吐司样式
		view = View.inflate(getApplicationContext(), R.layout.toast_view, null);
		tv_toast = (TextView) view.findViewById(R.id.tv_toast);
		
		//吐司拖拽
		view.setOnTouchListener(new OnTouchListener() {
			private int startX;
			private int startY;

			@Override
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
					params.x= disX + params.x;
					params.y = disY + params.y;
					
					//如果拖拽超出宽度，拖拽结束
					if(params.x<0 ){
						params.x = 0;
					}
					if(params.x>mWidth-view.getWidth()){
						params.x = mWidth-view.getWidth();
					}
					if(params.y<0){
						params.y = 0;
					}
					if(params.y>mHeight-view.getHeight()-22){
						params.y = mHeight-view.getHeight()-22;
					}
					
					//告知窗体做更新
					mWM.updateViewLayout(view, params);
					//重置初始坐标
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					
					break;
				case MotionEvent.ACTION_UP:
				    //记录拖拽后的坐标
					SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_X, params.x);
					SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_Y, params.y);
					
					break;
				}
				return true;
			}
		});
	    
		
		//获取已设置好的吐司的位置
		params.x = SpUtil.getInt(getApplicationContext(), ConstantValue.LOCATION_X, 0);
	    params.y = SpUtil.getInt(getApplicationContext(), ConstantValue.LOCATION_Y, 0);
	   
		//吐司风格颜色数组
		int[] toastStyleDrawble = new int[]{R.drawable.call_locate_white,
				R.drawable.call_locate_orange,R.drawable.call_locate_blue,
				R.drawable.call_locate_gray,R.drawable.call_locate_green};
		int toastIndex = SpUtil.getInt(getApplicationContext(), ConstantValue.ADDRESS_STYLE, 0);
		tv_toast.setBackgroundResource(toastStyleDrawble[toastIndex]);
		
		//在窗体上挂一个view，加权限
		//为什么这里用params 或者 mParams都可以？！
		mWM.addView(view, params);
	}


	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
    
	@Override
	public void onDestroy() {
		//关闭服务 时停止监听电话状态
		super.onDestroy();
		if(mTM!=null && listener!=null){
			mTM.listen(listener,PhoneStateListener.LISTEN_NONE);
			
		}
		if(outcallReceiver !=null){
			unregisterReceiver(outcallReceiver);
		}
	}
}
