package com.example.mobilesafe.activity;

import com.example.mobilesafe.R;
import com.example.mobilesafe.utils.ConstantValue;
import com.example.mobilesafe.utils.SpUtil;
import com.example.mobilesafe.utils.ToastUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Setup3Activity extends Activity {
	  private EditText edt_phone_number;
	  private Button btn_bind_number;

	@Override
	    protected void onCreate(Bundle savedInstanceState) {
	    	// TODO Auto-generated method stub
	    	super.onCreate(savedInstanceState);
	    	setContentView(R.layout.activity_setup3);
	    	
	    	initUI();
	    }
	  
	  private void initUI() {
		edt_phone_number = (EditText) findViewById(R.id.edt_phone_number);
		btn_bind_number = (Button) findViewById(R.id.btn_bind_number);
		//回显安全号码
		String phone_number = SpUtil.getString(this,ConstantValue.SECURITY_PHONE, "");
		edt_phone_number.setText(phone_number);
		btn_bind_number.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				Intent intent = new  Intent(getApplicationContext(),ContactsListActivity.class);
				startActivityForResult(intent, 0);
			}
		});
	}

	public void prePage(View view){
	    	String security_phone = edt_phone_number.getText().toString().trim();
		    if(!TextUtils.isEmpty(security_phone)){
		    	Intent intent = new Intent(this,Setup2Activity.class);
		    	SpUtil.putString(this, ConstantValue.SECURITY_PHONE, security_phone);
		    	startActivity(intent);
		    	finish();
		    }
	    }
	  
	  public void nextPage(View view){
		    String security_phone = edt_phone_number.getText().toString().trim();
		    if(!TextUtils.isEmpty(security_phone)){
		    	Intent intent = new Intent(this,Setup4Activity.class);
		    	SpUtil.putString(this, ConstantValue.SECURITY_PHONE, security_phone);
		    	startActivity(intent);
		    	finish();
		    }else{
		    	ToastUtil.show(this, "请选择安全号码");
		    }
	    }
	  
	  @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(data != null){
			String phone = data.getStringExtra("phone");
			phone = phone.replace("-","").replace(" ", "").trim();
			edt_phone_number.setText(phone);
			
		}
	}
}
