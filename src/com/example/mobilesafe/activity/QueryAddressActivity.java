package com.example.mobilesafe.activity;

import com.example.mobilesafe.R;
import com.example.mobilesafe.engine.AddressDao;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class QueryAddressActivity extends Activity {
	private EditText edt_address_number;
	private Button btn_query_number;
	private TextView tv_address;
	private String address;

	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			tv_address.setText(address);
		};
	};
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_query_address);
    	
    	initUI();
    }

	private void initUI() {
		edt_address_number = (EditText)findViewById(R.id.edt_address_number);
		btn_query_number = (Button)findViewById(R.id.btn_query_number);
		tv_address = (TextView)findViewById(R.id.tv_address);
		
		btn_query_number.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String phone = edt_address_number.getText().toString();
				if(!TextUtils.isEmpty(phone)){
					//查询是耗时操作，要放在子线程！
					query(phone);
				}else{
					//输入框抖动
					Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
			        edt_address_number.startAnimation(shake);
			        //手机震动
			        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				    //{不震动2秒，震动3秒，不震动2秒，2震动三秒}，重复次数0
			        vibrator.vibrate(new long[]{2000,3000,2000,3000}, 0);
				}
			}
		});
		
		edt_address_number.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				String phone = edt_address_number.getText().toString();
				query(phone);
			}
		});
		
	}
	
	private void query(final String phone){
		new Thread(){
			public void run() {
				address = AddressDao.getAddress(phone);
				//发送空消息 通知主线程更新textView
				mHandler.sendEmptyMessage(0);
			};
		}.start();
		
	}
}
