package com.example.mobilesafe.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.mobilesafe.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ContactsListActivity extends Activity {
      protected static final String tag = "ContactsListActivity";
	private ListView listView;
      private MyAdapter adapter;
      private List<HashMap<String,String>> contactList = new ArrayList<HashMap<String,String>>();
      
      private Handler mHandler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		adapter = new MyAdapter();
    		listView.setAdapter(adapter);
    	};  
      };

	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.contact_listview);
    	
    	initUI();
    	initData();
    }
	
	class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return contactList.size();
		}

		@Override
		public HashMap<String,String> getItem(int position) {
			return contactList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	        View view = View.inflate(getApplicationContext(), R.layout.contact_listview_item, null);
			
	        TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
	        TextView tv_phone = (TextView) view.findViewById(R.id.tv_phone);
	        
	        tv_name.setText(getItem(position).get("name"));
	        tv_phone.setText(getItem(position).get("phone"));
	        Log.i(tag, "name = "+tv_name);
	        Log.i(tag, "phone = "+tv_phone);
			return view;
		}
		
	}
	
	/**
	 * 获取系统联系人
	 */
	private void initData() {
		//读取系统联系人可能是耗时操作，要放到子线程中
		new Thread(){
			public void run() {
				ContentResolver contentResolver = getContentResolver();
				//查询到系统联系人的id。
				Cursor cursor = contentResolver.query(
						Uri.parse("content://com.android.contacts/raw_contacts"),
						new String[]{"contact_id"},
						null, null, null);
				contactList.clear();
				while(cursor.moveToNext()){
					String id = cursor.getString(0);
					Cursor cursor2 = contentResolver.query(
							Uri.parse("content://com.android.contacts/data"),
							new String[]{"data1","mimetype"}, 
							"raw_contact_id = ?", 
							new String[]{id}, 
							null);
					HashMap<String,String> map = new HashMap<String, String>();
					while(cursor2.moveToNext()){
						String data = cursor2.getString(0);
						String mimetype = cursor2.getString(1);
						Log.i(tag, "data="+data);
						Log.i(tag, "mimetype="+mimetype);
						if(mimetype.equals("vnd.android.cursor.item/phone_v2")){
							//data类型是电话号码
							if(!TextUtils.isEmpty(data)){
								map.put("phone", data);
							}
						}else if(mimetype.equals("vnd.android.cursor.item/name")){
							//data类型是姓名
							if(!TextUtils.isEmpty(data)){
								map.put("name", data);
							}
						}
					}
					cursor2.close();
					contactList.add(map);
				}
				cursor.close();
				//发送一个空消息给主线程，通知数据源已经准备好了
				mHandler.sendEmptyMessage(0);
			};
		}.start();
		
	}

	private void initUI() {
		listView = (ListView) findViewById(R.id.lv_contact);
		listView.setOnItemClickListener(new OnItemClickListener() {
        	@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				HashMap<String,String> map = new HashMap<String, String>();
				map = contactList.get(position);
				String phone = map.get("phone");
				Intent intent = new Intent();
				intent.putExtra("phone",phone);
				setResult(0, intent);
				finish();
			}
		});
	}
}
