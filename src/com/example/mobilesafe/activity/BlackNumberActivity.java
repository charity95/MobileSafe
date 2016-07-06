package com.example.mobilesafe.activity;

import java.util.ArrayList;
import java.util.List;

import com.example.mobilesafe.R;
import com.example.mobilesafe.db.bean.BlackNumberInfo;
import com.example.mobilesafe.db.dao.BlackNumberDao;
import com.example.mobilesafe.utils.ToastUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class BlackNumberActivity extends Activity {
    private ListView lv_blacknumber;
    private List<BlackNumberInfo> mBlackNumberList = new ArrayList<BlackNumberInfo>();
    private MyAdapter adapter;
    private BlackNumberDao mBlackNumberDao;
    private int mode = 1;
    private boolean mIsLoad = false;
    private int count;
    
    private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
    		if(adapter == null){
    			adapter = new MyAdapter();
    			lv_blacknumber.setAdapter(adapter);
    		}else{
    			adapter.notifyDataSetChanged();
    		}
    	};
    };
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_blacknumber);
    	
    	initUI();
    	initData();
    }

	private void initData() {
		new Thread(){
         public void run() {
        	    mBlackNumberDao = BlackNumberDao.getInstance(getApplicationContext());
			    //第一次倒序加载20条数据
				mBlackNumberList = mBlackNumberDao.find(0);
				count = mBlackNumberDao.getCount();
			    //查询黑名单完毕通知主线程更新UI
			    mHandler.sendEmptyMessage(0);
			};
		}.start();
		
	}
	
	class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mBlackNumberList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mBlackNumberList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = null;
			ViewHolder viewHolder;
			if(convertView == null){
				view = view.inflate(getApplicationContext(), R.layout.listiview_blcaknumber_item, null);
			    viewHolder = new ViewHolder();
			    viewHolder.phone = (TextView) view.findViewById(R.id.phone);
			    viewHolder.mode = (TextView) view.findViewById(R.id.mode);
			    viewHolder.delete = (ImageView) view.findViewById(R.id.delete);
			    view.setTag(viewHolder);
			}else{
				view = convertView;
				viewHolder = (ViewHolder) view.getTag();
			}
			
			final String phone = mBlackNumberList.get(position).phone;
			viewHolder.phone.setText(phone);
			
			int mode = Integer.parseInt(mBlackNumberList.get(position).mode);
			switch (mode) {
			case 1:
				viewHolder.mode.setText("拦截短信");
				break;
			case 2:
				viewHolder.mode.setText("拦截电话");
				break;
			case 3:
				viewHolder.mode.setText("拦截所有");
				break;
			}
			
			//删除黑名单
			viewHolder.delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//从数据库删除
					mBlackNumberDao.delete(phone);
					//从集合中删除
					mBlackNumberList.remove(position);
					//通知适配器更新
					if(adapter!=null){
						adapter.notifyDataSetChanged();	
					}
				}
			});
			
			return view;
		}
		
	}
	//不会创建多个对象
	static class ViewHolder{
		TextView phone,mode;
		ImageView delete;
	}

	private void initUI() {
		ImageView add = (ImageView) findViewById(R.id.add);
		lv_blacknumber = (ListView) findViewById(R.id.lv_blacknumber);
		
		//添加黑名单
		add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddBlackNumberDialog();
			}
		});
		
		//修改黑名单号码(需要完善)
		
		//监听滑动状态
		lv_blacknumber.setOnScrollListener(new OnScrollListener() {
			//滑动状态改变时调用
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//有数据才去加载
				if(mBlackNumberList!=null){
					// 如果滑动停止（空闲）,并且listview的最后一条可见，加载标志为false,则加载数据
					//正在加载时mIsLoad为true，则不会进行第二次加载，避免加载重复数据
					if(scrollState ==OnScrollListener.SCROLL_STATE_IDLE && 
							lv_blacknumber.getLastVisiblePosition()>=mBlackNumberList.size()-1 
							&& !mIsLoad){
						//还有数据能够加载时才去加载
						if(count>mBlackNumberList.size()){
							new Thread(){
								public void run() {
									mBlackNumberDao = BlackNumberDao.getInstance(getApplicationContext());
									List<BlackNumberInfo> moreInfo = mBlackNumberDao.find(mBlackNumberList.size());
									mBlackNumberList.addAll(moreInfo);
									mHandler.sendEmptyMessage(0);
								};
							}.start();
						}
					}
					
				}
				
				
			}
			//滑动过程中调用
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	protected void showAddBlackNumberDialog() {
		Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dialog = builder.create();
		View view = View.inflate(this, R.layout.dialog_add_blacknumber, null);
		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();
		
		Button btn_submit = (Button) view.findViewById(R.id.btn_submit);
		Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
		final EditText edt_blacknumber = (EditText) view.findViewById(R.id.edt_blacknumber);
	    RadioGroup rg = (RadioGroup) view.findViewById(R.id.rg);
		
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.rb_sms:
					//拦截短信
					mode = 1;
					break;
				case R.id.rb_call:
					//拦截电话
					mode = 2;
					break;
				case R.id.rb_all:
					//拦截所有
					mode = 3;
					break;
				}
				
			}
		});
		
		//添加黑名单号码
		btn_submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String phone = edt_blacknumber.getText().toString();
				if(!TextUtils.isEmpty(phone)){
					//插入数据库
					mBlackNumberDao.insert(phone, mode+"");
					//插入集合头部
					BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
					blackNumberInfo.phone = phone;
					blackNumberInfo.mode = mode+"";
					mBlackNumberList.add(0,blackNumberInfo);
					//刷新适配器
					if(adapter!=null){
						adapter.notifyDataSetChanged();
					}
					dialog.dismiss();
				}else{
					ToastUtil.show(getApplicationContext(), "请输入号码");
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
}
