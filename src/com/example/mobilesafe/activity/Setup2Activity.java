package com.example.mobilesafe.activity;

import com.example.mobilesafe.R;
import com.example.mobilesafe.utils.ConstantValue;
import com.example.mobilesafe.utils.SpUtil;
import com.example.mobilesafe.utils.ToastUtil;
import com.example.mobilesafe.view.SettingItemView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

public class Setup2Activity extends Activity {
	  @Override
	    protected void onCreate(Bundle savedInstanceState) {
	    	// TODO Auto-generated method stub
	    	super.onCreate(savedInstanceState);
	    	setContentView(R.layout.activity_setup2);
	    	
	    	initUI();
	    }
	  
	  private void initUI() {
		final SettingItemView siv_bind_number = (SettingItemView) findViewById(R.id.siv_bind_number);
		//回显 ，看sim是否绑定
		String bind_sim = SpUtil.getString(this, ConstantValue.BIND_SIM, "");
		if(!TextUtils.isEmpty(bind_sim)){
			//已绑定sim卡
			siv_bind_number.setCheck(true);
		}else{
			//没有绑定
			siv_bind_number.setCheck(false);
		}
		//获取当前的绑定状态
		final boolean isCheck = siv_bind_number.isCheck();
		siv_bind_number.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				siv_bind_number.setCheck(!isCheck);
				if(!isCheck){
					//点击绑定sim卡，存储sim卡序列号
					TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    String simSerialNumber = manager.getSimSerialNumber();	
                    SpUtil.putString(getApplicationContext(), ConstantValue.BIND_SIM, simSerialNumber);
				}else{
					//点击取消绑定sim卡,移除sp中的BIND_SIM结点
					SpUtil.remove(getApplicationContext(),ConstantValue.BIND_SIM);
				}
				
			}
		});
	}

	public void prePage(View view){
	    	Intent intent = new Intent(this,Setup1Activity.class);
	    	startActivity(intent);
	    	finish();
	    }
	  
	  public void nextPage(View view){
		  String bind_sim = SpUtil.getString(this, ConstantValue.BIND_SIM, "");
		  if(!TextUtils.isEmpty(bind_sim)){
			  Intent intent = new Intent(this,Setup3Activity.class);
			  startActivity(intent);
			  finish();
		  }else{
			  ToastUtil.show(this, "没有绑定sim卡！");
		  }
	    }
}
