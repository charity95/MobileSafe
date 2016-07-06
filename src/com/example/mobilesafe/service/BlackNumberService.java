package com.example.mobilesafe.service;

import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;
import com.example.mobilesafe.db.dao.BlackNumberDao;
import com.example.mobilesafe.service.AddressService.MyPhoneStateListener;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

public class BlackNumberService extends Service {
    private InnerSmsReceiver innerSmsReceiver;
	private BlackNumberDao mBlackNumberDao;
	private TelephonyManager mTM;
	private MyPhoneStateListener listener;
	private ContentResolver resolver;
	private MyObserver observer;
	
	@Override
    public void onCreate() {
    	//短信在接受的时候,广播发送,监听广播接受者,拦截短信(有序)
    	//将广播的优先级级别提高到最高 (1000)
    	IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
    	intentFilter.setPriority(1000);
    	innerSmsReceiver = new InnerSmsReceiver();
    	registerReceiver(innerSmsReceiver, intentFilter);
    	
    	mBlackNumberDao = BlackNumberDao.getInstance(this);
    	
    	//监听电话状态，响铃的时候拦截电话
    	mTM = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    	listener = new MyPhoneStateListener();
    	mTM.listen(listener,PhoneStateListener.LISTEN_CALL_STATE);
    	
    	super.onCreate();
    }
    
    class InnerSmsReceiver extends BroadcastReceiver{
        @Override
		public void onReceive(Context context, Intent intent) {
        	//获取短信内容
        	Object[] objects = (Object[]) intent.getExtras().get("pdus");
        	//循环遍历短信
        	for (Object object : objects) {
				//获取短信对象
        		SmsMessage message = SmsMessage.createFromPdu((byte[]) object);
        		//获取接收到的短信号码
        		String phone = message.getOriginatingAddress();
        		
        		int mode = mBlackNumberDao.getMode(phone);
        		if(mode == 1 || mode == 3){
        			abortBroadcast();
        		}
        	}
			
		}	
    }
    
    class MyPhoneStateListener extends PhoneStateListener{
        @Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				//空闲状态，没有任何活动,移除吐司
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				//响铃状态  拦截电话
				endCall(incomingNumber);
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				//摘机状态，至少有一个电话活动
				break;
			}
			super.onCallStateChanged(state, incomingNumber);

		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
 
	/**根据来电号码查询数据库中的黑名单模式，如果为2或3，则拦截电话
	 * @param incomingNumber
	 */
	public void endCall(String incomingNumber) {
		int mode = mBlackNumberDao.getMode(incomingNumber);
		if(mode == 2 || mode ==3){
			//反射电话
			//1,获取ServiceManager字节码文件
			Class<?> clazz;
			try {
				clazz = Class.forName("android.os.ServiceManager");
				//2,获取方法
				Method method = clazz.getMethod("getService", String.class);
				//3,反射调用此方法
				IBinder iBinder = (IBinder) method.invoke(null, Context.TELEPHONY_SERVICE);
				//4,调用获取aidl文件对象方法
				ITelephony iTelephony = ITelephony.Stub.asInterface(iBinder);
				//5,调用在aidl中隐藏的endCall方法
				iTelephony.endCall();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			//清除该号码的通话记录
			observer = new MyObserver(new Handler(),incomingNumber);
		    resolver = getContentResolver();
		    resolver.registerContentObserver(Uri.parse("content://call_log/calls"),
		    		true, observer); 
		}
		
	}
	
	class MyObserver extends ContentObserver{
        String phone;
		public MyObserver(Handler handler,String phone) {
			super(handler);
			this.phone = phone;
		}
		//当观察到数据库中的内容变化了，调用这个方法
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			getContentResolver().delete(Uri.parse("content://call_log/calls"),
					"number = ?", new String[]{phone});
		}
	}

	@Override
	public void onDestroy() {
		//取消短信拦截
		if(innerSmsReceiver!=null){
			unregisterReceiver(innerSmsReceiver);
		}
		//注销内容观察者
		if(resolver!=null && observer!=null){
			resolver.unregisterContentObserver(observer);
		}
		//取消对电话状态的监听
		if(mTM!=null && listener!=null){
			mTM.listen(listener,PhoneStateListener.LISTEN_NONE);
		}
		
		super.onDestroy();
	}
}
