package com.example.mobilesafe.service;

import com.example.mobilesafe.engine.ProcessInfoProvider;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class LockscreenCleanService extends Service {
   
	private LockscreenCleanReceiver receiver;

	@Override
	public void onCreate() {
		//监听锁屏广播
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		receiver = new LockscreenCleanReceiver();
		registerReceiver(receiver, intentFilter);
		super.onCreate();
	}
	
	class LockscreenCleanReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//清理全部可以清除的进程
			ProcessInfoProvider.killAll(context);
		}
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
    
	@Override
	public void onDestroy() {
		if(receiver!=null){
			unregisterReceiver(receiver);
		}
		super.onDestroy();
	}
}
