package com.example.mobilesafe.activity;

import org.w3c.dom.Text;

import com.example.mobilesafe.R;
import com.example.mobilesafe.utils.ConstantValue;
import com.example.mobilesafe.utils.MD5Util;
import com.example.mobilesafe.utils.SpUtil;
import com.example.mobilesafe.utils.ToastUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeActivity extends Activity {
	
	private GridView mGridView;
	private int[] mImages;
	private String[] mTexts;
	
	@Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_home);
	        
	        initUI();
	        initData();
	 }

	private void initUI() {
		mGridView = (GridView) findViewById(R.id.gv_home);		
	}

	private void initData() {
		mTexts = new String[]{"手机防盗","通信卫士","软件管理","进程管理",
				"流量统计","手机杀毒","缓存清理","高级工具","设置中心"};
		
		mImages =new int[]{R.drawable.home_safe,R.drawable.home_callmsgsafe,
				R.drawable.home_apps,R.drawable.home_taskmanager,
				R.drawable.home_netmanager,R.drawable.home_trojan,
				R.drawable.home_sysoptimize,R.drawable.home_tools,
				R.drawable.home_settings};
		
		mGridView.setAdapter(new MyAdapter());
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					showDialog();
					break;
				case 1:
					startActivity(new Intent(getApplicationContext(),BlackNumberActivity.class));
					break;
				case 2:
					startActivity(new Intent(getApplicationContext(),AppManagerActivity.class));
					break;
				case 3:
					startActivity(new Intent(getApplicationContext(),ProcessManagerActivity.class));
					break;
				case 4:
					startActivity(new Intent(getApplicationContext(),TrafficActivity.class));
					break;
				case 5:
					startActivity(new Intent(getApplicationContext(),AntiVirusActivity.class));
					break;
				case 6:
					startActivity(new Intent(getApplicationContext(),CacheClearActivity.class));
					break;
				case 7:
					startActivity(new Intent(getApplicationContext(),AToolActivity.class));
					break;
                case 8:
					Intent intent = new Intent(getApplicationContext(),SettingActivity.class);
					startActivity(intent);
					break;
				}
				
			}
		});
	}
	
	protected void showDialog() {
		String psd = SpUtil.getString(this,ConstantValue.MOBILE_SAFE_PSD, "");
		if(TextUtils.isEmpty(psd)){
			//第一次进入弹出设置密码对话框
			showSetPsdDialog();
			}else{
				//以后进入弹出输入密码对话框
				showConfirmPsdDialog();	
			}
		
	}

	private void showConfirmPsdDialog() {
		Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dialog = builder.create();
		final View view = View.inflate(this,R.layout.confirm_safe_psd, null);
		dialog.setView(view,0,0,0,0);
		dialog.show();

		Button btn_submit = (Button) view.findViewById(R.id.btn_submit);
		Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
		
		btn_submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//点击确定，验证密码，正确则跳转到新界面
				EditText edt_confirm_psd = (EditText) view.findViewById(R.id.edt_second_psd);
				
				String confirm_psd = edt_confirm_psd.getText().toString().trim();
				
				if(!TextUtils.isEmpty(confirm_psd)){
					String psd = SpUtil.getString(getApplicationContext(), ConstantValue.MOBILE_SAFE_PSD, "");
					if(psd.equals(MD5Util.encoder(confirm_psd))){
						Intent intent = new Intent(getApplicationContext(),SetupOverActivity.class);
						startActivity(intent);
						dialog.dismiss();
						}
					else{
						ToastUtil.show(getApplicationContext(), "密码错误");
					}
				}else{
					ToastUtil.show(getApplicationContext(), "输入不能为空");
				}
			}
		});
		
		btn_cancel.setOnClickListener(new OnClickListener() {
		    @Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});		
	}

	private void showSetPsdDialog() {
		Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dialog = builder.create();
		final View view = View.inflate(this,R.layout.set_safe_psd, null);
		dialog.setView(view,0,0,0,0);
		dialog.show();

		Button btn_submit = (Button) view.findViewById(R.id.btn_submit);
		Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
		
		btn_submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//点击确定，验证是否为空，两次密码是否一致，保存密码，跳转到新界面
				EditText edt_psd = (EditText) view.findViewById(R.id.edt_first_psd);
				EditText edt_confirm_psd = (EditText) view.findViewById(R.id.edt_second_psd);
				
				String psd = edt_psd.getText().toString().trim();
				String confirm_psd = edt_confirm_psd.getText().toString().trim();
				
				if(!TextUtils.isEmpty(psd) && !TextUtils.isEmpty(confirm_psd)){
					if(psd.equals(confirm_psd)){
						SpUtil.putString(getApplicationContext(), ConstantValue.MOBILE_SAFE_PSD,MD5Util.encoder(psd));
						Intent intent = new Intent(getApplicationContext(),SetupOverActivity.class);
						startActivity(intent);
						dialog.dismiss();
					}else{
						ToastUtil.show(getApplicationContext(), "两次密码不一致");
					}
				}else{
					ToastUtil.show(getApplicationContext(), "输入不能为空");
				}
			}
		});
		
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

	class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mImages.length;
		}

		@Override
		public Object getItem(int arg0) {
			return mImages[arg0];
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int postion, View convertView, ViewGroup arg2) {
			View view =null;
			ViewHolder viewHolder;
			if(convertView == null){
				view= View.inflate(getApplicationContext(), R.layout.gridview_item, null);
			    viewHolder = new ViewHolder();
			    viewHolder.imageView = (ImageView) view.findViewById(R.id.image_item);
			    viewHolder.textView = (TextView) view.findViewById(R.id.text_item);
			    view.setTag(viewHolder);
			}else{
				view = convertView;
				viewHolder = (ViewHolder) view.getTag();
			}
			
			viewHolder.imageView.setImageResource(mImages[postion]);
			viewHolder.textView.setText(mTexts[postion]);
			
			return view;
		}
	}
	
	class ViewHolder{
		ImageView imageView;
		TextView textView;
	}
}
