package com.example.mobilesafe.activity;

import com.example.mobilesafe.R;
import com.example.mobilesafe.utils.ConstantValue;
import com.example.mobilesafe.utils.SpUtil;
import com.example.mobilesafe.utils.ToastUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Setup4Activity extends Activity {
	  private CheckBox checkBox;

	@Override
	    protected void onCreate(Bundle savedInstanceState) {
	    	// TODO Auto-generated method stub
	    	super.onCreate(savedInstanceState);
	    	setContentView(R.layout.activity_setup4);
	    	
	    	initUI();
	    }
	  
	  private void initUI() {
		checkBox = (CheckBox) findViewById(R.id.checkBox);
		//获取回显的checkbox的状态
		boolean isCheck = SpUtil.getBoolean(this, ConstantValue.OPEN_SECURITY, false);
		checkBox.setChecked(isCheck);
		if(isCheck){
			checkBox.setText("你已开启防盗保护");
		}else{
			checkBox.setText("你没有开启防盗保护");
		}
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//isChecked是checkbox点击后已改变的状态
				SpUtil.putBoolean(getApplicationContext(), ConstantValue.OPEN_SECURITY, isChecked);
				if(isChecked){
					checkBox.setText("你已开启防盗保护");
				}else{
					checkBox.setText("你没有开启防盗保护");
				}
				
			}
		});
		
	}

	public void prePage(View view){
	    	Intent intent = new Intent(this,Setup3Activity.class);
	    	startActivity(intent);
	    	finish();
	    }
	  
	  public void nextPage(View view){
		  boolean open_security = SpUtil.getBoolean(getApplicationContext(), ConstantValue.OPEN_SECURITY, false);
		  if(open_security){
			  Intent intent = new Intent(this,SetupOverActivity.class);
			  startActivity(intent);
			  SpUtil.putBoolean(this,ConstantValue.SETUP_OVER, true);
			  finish();
		  }else{
			  ToastUtil.show(this, "请开启防盗保护");
		  }
	    }
}
