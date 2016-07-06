package com.example.mobilesafe.activity;

import com.example.mobilesafe.R;
import com.example.mobilesafe.service.LockscreenCleanService;
import com.example.mobilesafe.utils.ConstantValue;
import com.example.mobilesafe.utils.ServiceUtil;
import com.example.mobilesafe.utils.SpUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ProcessSettingActivity extends Activity {
    private CheckBox cb_show_system_process;
	private CheckBox cb_lockscreen_clean;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_process_setting);
    	
    	initShowSystemProcess();
    	initLockscreenCLean();
    }

	private void initLockscreenCLean() {
		cb_lockscreen_clean = (CheckBox) findViewById(R.id.cb_lockscreen_clean);
		//回显，根据服务是否在运行的状态
		boolean isClean = ServiceUtil.getService(this, "com.example.mobilesafe.service.LockscreenCleanService");
		if(isClean){
			cb_lockscreen_clean.setText("开启锁屏清理");
		}else{
			cb_lockscreen_clean.setText("关闭锁屏清理");
		}
		cb_lockscreen_clean.setChecked(isClean);
		
		cb_lockscreen_clean.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					//开启服务，清理进程
					cb_lockscreen_clean.setText("开启锁屏清理");
					startService(new Intent(getApplicationContext(),LockscreenCleanService.class));
				}else{
					//关闭服务
					cb_lockscreen_clean.setText("关闭锁屏清理");
					stopService(new Intent(getApplicationContext(),LockscreenCleanService.class));
				}
			}
		});
	    
	    
		
	}

	private void initShowSystemProcess() {
		cb_show_system_process = (CheckBox) findViewById(R.id.cb_show_system_process);
		
		//回显
		boolean isShow = SpUtil.getBoolean(this, ConstantValue.SHOW_SYSTEM_PROCESS, true);
	    if(isShow){
	    	cb_show_system_process.setText("显示系统进程");
	    }else{
	    	cb_show_system_process.setText("隐藏系统进程");
	    }
		cb_show_system_process.setChecked(isShow);
				
		cb_show_system_process.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cb_show_system_process.setChecked(isChecked);
				if(isChecked){
					//显示系统进程
					cb_show_system_process.setText("显示系统进程");
				}else{
					//隐藏系统进程
					cb_show_system_process.setText("隐藏系统进程");
				}
				SpUtil.putBoolean(getApplicationContext(), ConstantValue.SHOW_SYSTEM_PROCESS, isChecked);
			}
		});
	}
}
