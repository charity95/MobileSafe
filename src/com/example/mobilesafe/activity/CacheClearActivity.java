package com.example.mobilesafe.activity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

import com.example.mobilesafe.R;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CacheClearActivity extends Activity {
    protected static final int UPDATA_CACHE = 100;
	protected static final int SCANNING = 101;
	protected static final int FINISH = 102;
	protected static final int REMOVE_ALL = 103;
	private Button btn_clear;
	private TextView tv_name;
	private ProgressBar pb_cache;
	private LinearLayout ll_add_text;
	private PackageManager mPm;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UPDATA_CACHE:
				//扫描到有缓存的应用，更新UI
				
				View view = View.inflate(getApplicationContext(), R.layout.linearlayout_cache_item, null);
				
				TextView tv_item_name = (TextView) view.findViewById(R.id.tv_item_name);
				TextView tv_cache = (TextView) view.findViewById(R.id.tv_cache);
				ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
				ImageView iv_delete = (ImageView) view.findViewById(R.id.iv_delete);
				
				final CacheInfo info  = (CacheInfo) msg.obj;
				
				iv_icon.setBackgroundDrawable(info.icon);
				tv_item_name.setText(info.name);
				String cache = Formatter.formatFileSize(getApplicationContext(), info.cacheSize);
				tv_cache.setText("占用缓存:"+cache);
				
				ll_add_text.addView(view, 0);
	
				
				iv_delete.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						//点击弹出系统清除缓存的界面
						Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
						intent.setData(Uri.parse("package:"+info.packageName));
						startActivity(intent);
					}
				});
			
				break;
			case SCANNING:
				
				String name = (String) msg.obj;
				tv_name.setText("正在扫描："+name);
				break;
			case FINISH:
				tv_name.setText("扫描完成");
				break;
			case REMOVE_ALL:
				//一键清理
				ll_add_text.removeAllViews();
			}
		};
	};
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_cache_clear);
    	
    	initUI();
    	initData();
    }

	private void initData() {
		new Thread(){
			private int index;

			public void run() {
				//遍历手机应用,获取有缓存的应用
				mPm = getPackageManager();
				List<PackageInfo> packages = mPm.getInstalledPackages(0);
				
				pb_cache.setMax(packages.size());
				
				for (PackageInfo packageInfo : packages) {
					String packageName = packageInfo.packageName;
					getPackageCache(packageName);
					String name = null;
					try {
						name = mPm.getApplicationInfo(packageName, 0).loadLabel(mPm).toString();
						index ++;
						pb_cache.setProgress(index);
						
						Thread.sleep(50+new Random().nextInt(100));
					} catch (Exception e) {
						e.printStackTrace();
					}
					Message msg = Message.obtain();
					msg.what = SCANNING;
					msg.obj = name;
					mHandler.sendMessage(msg);
					
				}
				Message msg = Message.obtain();
				msg.what = FINISH;
				mHandler.sendMessage(msg);
			};
		}.start();
		
	}

	protected void getPackageCache(String packageName) {
		IPackageStatsObserver.Stub mStatsObserver = new IPackageStatsObserver.Stub() {
			
			@Override //该方法在子线程中进行
			public void onGetStatsCompleted(PackageStats Stats, boolean succeeded)
					throws RemoteException {
				    //获取到的缓存大小
				    long cacheSize = Stats.cacheSize;
				    if(cacheSize>0){
				    	CacheInfo cacheInfo = new CacheInfo();
				    	try {
				    		cacheInfo.cacheSize = cacheSize;
				    		cacheInfo.packageName = Stats.packageName;
							cacheInfo.name = mPm.getApplicationInfo(cacheInfo.packageName, 0).loadLabel(mPm).toString();
						    cacheInfo.icon = mPm.getApplicationInfo(cacheInfo.packageName, 0).loadIcon(mPm);
				    	} catch (NameNotFoundException e) {
							e.printStackTrace();
						}
				    	Message msg = Message.obtain();
				    	msg.what = UPDATA_CACHE;
				    	msg.obj = cacheInfo;
				    }
			}
		};
		//反射，获取缓存大小
	    try {
	    	//获取指定类的字节码文件
			Class<?> clazz = Class.forName("android.content.pm.PackageManager");
			//获取调用方法对象
			Method method = clazz.getMethod("getPackageSizeInfo", String.class,IPackageStatsObserver.class);
			//对象调用方法
			method.invoke(mPm, packageName,mStatsObserver);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public class CacheInfo{
		public String name;
		public String packageName;
		public long cacheSize;
		public Drawable icon;
	}

	private void initUI() {
	    btn_clear = (Button) findViewById(R.id.btn_clear);
	    tv_name = (TextView) findViewById(R.id.tv_name);
		pb_cache = (ProgressBar) findViewById(R.id.pb_cache);
		ll_add_text = (LinearLayout) findViewById(R.id.ll_add_text);
		
		btn_clear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
				//获取指定类的字节码文件
				Class<?> clazz = Class.forName("android.content.pm.PackageManager");
				//获取调用方法对象
				Method method = clazz.getMethod("freeStorageAndNotify", long.class,IPackageStatsObserver.class);
				//对象调用方法
				method.invoke(mPm, Long.MAX_VALUE,new IPackageDataObserver.Stub() {
						
						@Override
						public void onRemoveCompleted(String packageName, boolean succeeded)
								throws RemoteException {
							//清理完后调用的方法（子线程中执行）
							Message msg = Message.obtain();
					    	msg.what = REMOVE_ALL;
                            mHandler.sendMessage(msg);
						}
					});
				} catch (Exception e) {				
					e.printStackTrace();
				}
				
			}
		});
	}
}
