package com.example.mobilesafe.activity;

import java.io.File;


import com.example.mobilesafe.R;
import com.example.mobilesafe.engine.BackupSms;
import com.example.mobilesafe.engine.BackupSms.CallbackSms;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AToolActivity extends Activity {
     @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_atool);
    	
    	//归属地查询
    	initQueryAddress();
    	//短信备份
    	initBackupSms();
    	//常用号码查询
    	initQueryCommonNum();
    	//程序锁
    	initLockApp();
    	
    }

	private void initLockApp() {
		TextView tv_lock_app =  (TextView) findViewById(R.id.tv_lock_app);
		tv_lock_app.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),LockAppActivity.class));
			}
		});
		
	}

	private void initQueryCommonNum() {
		TextView tv_query_commonnum =  (TextView) findViewById(R.id.tv_query_commonnum);
		tv_query_commonnum.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),QueryCommonNumActivity.class));
			}
		});
	}
		

	private void initBackupSms() {
		TextView tv_backup_sms =  (TextView) findViewById(R.id.tv_backup_sms);
		tv_backup_sms.setOnClickListener(new OnClickListener(){
            @Override
			public void onClick(View v) {
			     showProgressDialog();
            }
			
		});
		
	}

	protected void showProgressDialog() {
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("短信备份");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setIcon(R.drawable.ic_launcher);
		progressDialog.show();
		//开启子线程备份短信.progressdialog可以在子线程中更新UI
		new Thread(){
			public void run() {
				String path = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"sms.xml";
				//回调机制
				BackupSms.backup(getApplicationContext(), path,new CallbackSms() {
					
					@Override
					public void setProgress(int index) {
						progressDialog.setProgress(index);
						
					}
					
					@Override
					public void setMax(int max) {
						progressDialog.setMax(max);
						
					}
				});
				progressDialog.dismiss();
			};
		}.start();
	}

	private void initQueryAddress() {
		TextView tv_query_phone_number =  (TextView) findViewById(R.id.tv_query_phone_number);
		tv_query_phone_number.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(getApplicationContext(),QueryAddressActivity.class));
			}
		});
	}
}
