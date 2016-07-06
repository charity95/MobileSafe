package com.example.mobilesafe.activity;

import com.example.mobilesafe.R;
import com.example.mobilesafe.engine.AppInfoProvider;
import com.example.mobilesafe.utils.ToastUtil;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.drm.DrmStore.Action;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class EnterPsdActivity extends Activity {
    /*设置该activity的启动模式
     * android:launchMode="singleInstance"
      android:excludeFromRecents="true"
     * */
	
	private TextView tv_app_name;
	private ImageView iv_app_icon;
	private EditText et_psd;
	private Button btn_submit;
	private String packagename;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_enter_psd);
    	
    	Intent intent = getIntent();
		packagename = intent.getStringExtra("packagename");
    	
    	initUI();
    	initData();
    	
    }

	private void initData() {
		//根据包名获取应用信息
		PackageManager pm = getPackageManager();
		try {
			ApplicationInfo Info = pm.getApplicationInfo(packagename, 0);
			String name = Info.loadLabel(pm).toString();
			Drawable icon = Info.loadIcon(pm);
			
			tv_app_name.setText(name);
			iv_app_icon.setBackgroundDrawable(icon);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	
	private void initUI() {
		tv_app_name = (TextView) findViewById(R.id.tv_app_name);
		iv_app_icon = (ImageView) findViewById(R.id.iv_app_icon);
		et_psd = (EditText) findViewById(R.id.et_psd);
		btn_submit = (Button) findViewById(R.id.btn_submit);	
		
		btn_submit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String psd = et_psd.getText().toString();
				if (!TextUtils.isEmpty(psd)) {
					if (psd.equals("123")) {
						//发送广播给看门狗，停止监测该应用,自定义广播
						Intent intent = new Intent("android.intent.action.SKIP");
						intent.putExtra("packagename", packagename);
						sendBroadcast(intent);
						finish();
					} else {
						ToastUtil.show(getApplicationContext(), "密码错误");
					}
				}else{
					ToastUtil.show(getApplicationContext(), "请输入密码");
				}
				
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		//重写物理返回键，返回到桌面
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
		super.onBackPressed();
	}
}
