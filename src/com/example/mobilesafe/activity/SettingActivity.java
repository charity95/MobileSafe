package com.example.mobilesafe.activity;

import com.example.mobilesafe.R;
import com.example.mobilesafe.service.AddressService;
import com.example.mobilesafe.service.BlackNumberService;
import com.example.mobilesafe.service.LockAppService;
import com.example.mobilesafe.utils.ConstantValue;
import com.example.mobilesafe.utils.ServiceUtil;
import com.example.mobilesafe.utils.SpUtil;
import com.example.mobilesafe.view.SettingClickView;
import com.example.mobilesafe.view.SettingItemView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class SettingActivity extends Activity {
    protected String[] mStyleColors = new String[]{"透明","橙色","蓝色","灰色","绿色"};
	private int checkedItem;
	private SettingClickView siv_toast_style;
	private SettingClickView siv_toast_location;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_setting);
    	
    	//自动更新设置
    	initUpdate();
    	//归属地是否显示设置
    	initAddress();
    	//归属地吐司显示风格设置
    	initToastStyle();
    	//设置归属地吐司显示位置
    	initToastLocation();
    	//黑名单拦截设置
    	initBlackNumber();
    	//程序锁设置
    	initAppLock();
    }

	/**
	 * 程序锁设置
	 */
	private void initAppLock() {
		final SettingItemView siv_app_lock = (SettingItemView) findViewById(R.id.siv_app_lock);
		//通过服务是否正在运行 来设置item的状态。不能通过SP去存储Item的状态，因为有可能服务已经被杀死了
		boolean isRunning = ServiceUtil.getService(this, "com.example.mobilesafe.service.LockAppService");
		siv_app_lock.setCheck(isRunning);
		
		siv_app_lock.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean isCheck = siv_app_lock.isCheck();
				siv_app_lock.setCheck(!isCheck);
				if(!isCheck){
					//开启服务
					startService(new Intent(getApplicationContext(),LockAppService.class));
				}else{
					//关闭服务
					stopService(new Intent(getApplicationContext(),LockAppService.class));
				}
			}
		});
		
		
	}

	/**
	 * 黑名单拦截设置
	 */
	private void initBlackNumber() {
		final SettingItemView siv_blacknumber = (SettingItemView) findViewById(R.id.siv_blacknumber);
		//通过服务是否正在运行 来设置item的状态。不能通过SP去存储Item的状态，因为有可能服务已经被杀死了
		boolean isRunning = ServiceUtil.getService(this, "com.example.mobilesafe.service.BlackNumberService");
		siv_blacknumber.setCheck(isRunning);
		
		siv_blacknumber.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean isCheck = siv_blacknumber.isCheck();
				siv_blacknumber.setCheck(!isCheck);
				if(!isCheck){
					//开启服务
					startService(new Intent(getApplicationContext(),BlackNumberService.class));
				}else{
					//关闭服务
					stopService(new Intent(getApplicationContext(),BlackNumberService.class));
				}
			}
		});
		
	}

	/**
	 * 设置归属地吐司显示位置
	 */
	private void initToastLocation() {
		siv_toast_location = (SettingClickView) findViewById(R.id.siv_toast_location);
		siv_toast_location.setTitle("归属地提示框位置");
		siv_toast_location.setContent("设置归属地提示框位置");
		
		siv_toast_location.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),ToastLocationActivity.class));
				
			}
		});
	}

	/**
	 * 归属地显示风格设置
	 */
	private void initToastStyle() {
		siv_toast_style = (SettingClickView) findViewById(R.id.siv_toast_style);
		siv_toast_style.setTitle("设置归属地显示风格");
		checkedItem = SpUtil.getInt(this, ConstantValue.ADDRESS_STYLE, 0);
		siv_toast_style.setContent(mStyleColors[checkedItem]);
		
		siv_toast_style.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showToastStyleDialog();
		    }
		});
	}
	
	

	/**
	 * 显示吐司风格的对话框
	 */
	protected void showToastStyleDialog() {
		Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle("请选择归属地样式");
		
		/*三个参数：1.描述颜色文字的数组  
		 * 2.弹出对话框时已选中的条目索引
		 * 3.点击某个条目后触发的事件（记录选中的索引值，关闭对话框，显示选中的文字）
		 * */
		builder.setSingleChoiceItems(mStyleColors, checkedItem, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//which 是选中的索引值
				SpUtil.putInt(getApplicationContext(), ConstantValue.ADDRESS_STYLE, which);
				dialog.dismiss();
				siv_toast_style.setContent(mStyleColors[which]);
			}
		});
		
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();		
			}
		});
		
		builder.show();
	}
		

	/**
	 * 归属地是否显示设置
	 */
	private void initAddress() {
		final SettingItemView siv_address = (SettingItemView) findViewById(R.id.siv_address);
		//通过服务是否正在运行 来设置item的状态。不能通过SP去存储Item的状态，因为有可能服务已经被杀死了
		boolean isRunning = ServiceUtil.getService(this, "com.example.mobilesafe.service.AddressService");
		siv_address.setCheck(isRunning);
		
		siv_address.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean isCheck = siv_address.isCheck();
				siv_address.setCheck(!isCheck);
				if(!isCheck){
					//开启服务
					startService(new Intent(getApplicationContext(),AddressService.class));
				}else{
					//关闭服务
					stopService(new Intent(getApplicationContext(),AddressService.class));
				}
			}
		});
	}

	/**
	 * 自动更新设置
	 */
	private void initUpdate() {
		final SettingItemView siv_update = (SettingItemView) findViewById(R.id.siv_update);
		boolean open_update = SpUtil.getBoolean(this, ConstantValue.OPEN_UPDATE, false);
		siv_update.setCheck(open_update);
		
		siv_update.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean isCheck = siv_update.isCheck();
				siv_update.setCheck(!isCheck);
				
				SpUtil.putBoolean(getApplicationContext(), ConstantValue.OPEN_UPDATE, !isCheck);
			}
		});
		
	}
}
