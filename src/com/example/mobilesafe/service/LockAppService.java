package com.example.mobilesafe.service;

import java.util.List;

import com.example.mobilesafe.activity.EnterPsdActivity;
import com.example.mobilesafe.db.dao.LockAppDao;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;

public class LockAppService extends Service {
     
	private LockAppDao mDao;
	private List<String> mLockPackageNameList;
	private boolean isWatch = true;
	private String mPackagename;
	private InnerReceiver receiver;
	private MyObserver observer;

	@Override
	public void onCreate() {
		mDao = LockAppDao.getInstance(getApplicationContext());
		
		watchDog();
		
		IntentFilter intentFilter = new IntentFilter("android.intent.action.SKIP");
		receiver = new InnerReceiver();
		registerReceiver(receiver, intentFilter);
		
		observer = new MyObserver(new Handler());
		getContentResolver().registerContentObserver(Uri.parse("content://app/change"), true, observer);
		super.onCreate();
	}
	
	class MyObserver extends ContentObserver{

		public MyObserver(Handler handler) {
			super(handler);
		}
		
		//观察到数据库发生改变时，重新获取数据库中已锁应用的集合
		@Override
		public void onChange(boolean selfChange) {
			mLockPackageNameList = mDao.findAll();
			
			super.onChange(selfChange);
		}
	}
	
	class InnerReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			mPackagename = intent.getStringExtra("packagename");
			
		}
	}
	
	
	/**
	 * 循环监测打开的应用的包名是否存在于数据库中，如果存在则跳转到输入密码界面
	 */
	private void watchDog() {
		final ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

		new Thread(){
			public void run() {
				mLockPackageNameList = mDao.findAll();
				while(isWatch){
					//获取运行的应用的任务栈,
				    List<RunningTaskInfo> runningTasks = am.getRunningTasks(1);
				    //获取栈顶的应用栈，就是当前点开的应用
					RunningTaskInfo taskInfo = runningTasks.get(0);
                    String packagename = taskInfo.topActivity.getPackageName();
                    
					if(mLockPackageNameList.contains(packagename)){
						//该应用如果已经输入正确的密码进入应用，停止对该应用的监测
						if(!packagename.equals(mPackagename)){
							//跳转到输入密码界面
							Intent intent = new Intent(getApplicationContext(),EnterPsdActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.putExtra("packagename", packagename);
							startActivity(intent);
						}
					}
					
					try {
						//子线程需要睡眠，不能时刻占着CPU，会导致手机很卡
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
		
	}
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		if(receiver!=null){
			unregisterReceiver(receiver);
		}
		
		if(observer!=null){
			getContentResolver().unregisterContentObserver(observer);
		}
		
		isWatch =false;
		
		super.onDestroy();
	}
}
