package com.example.mobilesafe.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.example.mobilesafe.R;
import com.example.mobilesafe.db.bean.AppInfo;
import com.example.mobilesafe.engine.AntiVirusDao;
import com.example.mobilesafe.engine.AppInfoProvider;
import com.example.mobilesafe.utils.MD5Util;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AntiVirusActivity extends Activity {
    protected static final int SCANNING = 100;
	protected static final int SCANN_FINISH = 200;
	private TextView tv_name;
	private ImageView iv_scanning;
	private ProgressBar progressBar;
	private LinearLayout ll_add_text;
	private List<String> mVirusList;
	private List<ScanInfo> mVirusScanInfo;
	private List<ScanInfo> mScanInfo;
	protected int index = 0;
	
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SCANNING:
				ScanInfo scanInfo = (ScanInfo) msg.obj;
				tv_name.setText(scanInfo.name);
				
				TextView textView = new TextView(getApplicationContext());
				if(scanInfo.isVirus){
					textView.setTextColor(Color.RED);
					textView.setText("发现病毒："+scanInfo.name);					
				}else{
					textView.setTextColor(Color.BLACK);
					textView.setText("扫描安全："+scanInfo.name);	
				}
				ll_add_text.addView(textView, 0);
				
				break;
			case SCANN_FINISH:
				tv_name.setText("扫描完成");
				iv_scanning.clearAnimation();
				uninstallVirtua();
				break;
			}
		};
	};
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_anti_virus);
    	
    	AntiVirusDao dao = new AntiVirusDao();
    	mVirusList = dao.getVirus();
    	
    	initUI();
    	initAnimation();
    	checkVirus();
    }
    
	
	private void initAnimation() {
		RotateAnimation animation = new RotateAnimation(0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f, 
				Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(500);
		animation.setRepeatCount(Animation.INFINITE);
		animation.setFillAfter(true);
		
		iv_scanning.startAnimation(animation);
	}


	protected void uninstallVirtua() {
		for (ScanInfo info : mVirusScanInfo) {
			Intent intent = new Intent("android.intent.action.DELETE");
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setData(Uri.parse("package:" + info.packagename));
			startActivity(intent);		
			
		}
	}


	/**
	 * 遍历手机 应用，比较签名文件的MD5码与数据库中的MD5码，以此检测病毒
	 */
	private void checkVirus() {
		 new Thread(){

			public void run() {
				 //获取包管理者对象
				 PackageManager pm = getPackageManager();
				 //获取手机所有应用的签名文件，已安装的和卸载后残留的
				 List<PackageInfo> packageInfoList = pm.getInstalledPackages(PackageManager.GET_SIGNATURES
						 +PackageManager.GET_UNINSTALLED_PACKAGES);
			     
				 //存放病毒应用的集合
				 mVirusScanInfo = new ArrayList<ScanInfo>();
				 //存放所有应用的集合			 
				 mScanInfo = new ArrayList<ScanInfo>();
				 
				 progressBar.setMax(packageInfoList.size());
				 
				 for (PackageInfo packageInfo : packageInfoList) {
					 ScanInfo scanInfo = new ScanInfo();
			    	 //签名数组  
			    	 Signature[] signatures = packageInfo.signatures;
			    	 //获取第一位进行MD5转换
			    	 Signature signature = signatures[0];
			    	 String charsString = signature.toCharsString();
			    	 String encoder = MD5Util.encoder(charsString);
			    	 
			    	 scanInfo.name = packageInfo.applicationInfo.loadLabel(pm).toString();
			    	 scanInfo.packagename = packageInfo.packageName;
			    	 if(mVirusList.contains(encoder)){
			    		 scanInfo.isVirus = true;
			    		 mVirusScanInfo.add(scanInfo);
			    	 }else{
			    		 scanInfo.isVirus = false;
			    	 }
			    	 
			    	 index++;
			    	 progressBar.setProgress(index);
			    	 try {
			    		 //休眠随机的时间，让用户感觉到扫描的进度
						Thread.sleep(50+new Random().nextInt(100));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			    	 
				     mScanInfo.add(scanInfo);
				     
				     Message msg = Message.obtain();
				     msg.what = SCANNING;
				     msg.obj = scanInfo;
				     mHandler.sendMessage(msg);
				 }
				 Message msg = Message.obtain();
			     msg.what = SCANN_FINISH;
			     mHandler.sendMessage(msg);
			 };
		 }.start();
		
	}
	
	public class ScanInfo{
		public String name,packagename;
		public boolean isVirus;
	}

	private void initUI() {
		tv_name = (TextView) findViewById(R.id.tv_name);
		iv_scanning = (ImageView) findViewById(R.id.iv_scanning);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		ll_add_text = (LinearLayout) findViewById(R.id.ll_add_text);
		
	}
}
