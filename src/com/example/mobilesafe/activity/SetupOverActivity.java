package com.example.mobilesafe.activity;

import com.example.mobilesafe.R;
import com.example.mobilesafe.utils.ConstantValue;
import com.example.mobilesafe.utils.SpUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SetupOverActivity extends Activity {
    private TextView tv_safe_number;
	private TextView tv_reset_setup;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	boolean setup_over = SpUtil.getBoolean(this, ConstantValue.SETUP_OVER, false);
    	if(setup_over){
    		setContentView(R.layout.activity_setup_over);
    		initUI();
    	}else{
    		startActivity(new Intent(this,Setup1Activity.class));
    		finish();
    	}
    	
    }

	private void initUI() {
		tv_safe_number =(TextView) findViewById(R.id.tv_safe_number);
		String safe_number = SpUtil.getString(this, ConstantValue.SECURITY_PHONE, "");
		tv_safe_number.setText(safe_number);
		
		tv_reset_setup = (TextView) findViewById(R.id.tv_reset_setup);
		//textView默认没有点击事件
		tv_reset_setup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),Setup1Activity.class));
				finish();
			}
		});
	}
}
