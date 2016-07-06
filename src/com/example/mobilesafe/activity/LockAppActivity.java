package com.example.mobilesafe.activity;

import java.util.ArrayList;
import java.util.List;

import com.example.mobilesafe.R;
import com.example.mobilesafe.db.bean.AppInfo;
import com.example.mobilesafe.db.dao.LockAppDao;
import com.example.mobilesafe.engine.AppInfoProvider;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class LockAppActivity extends Activity {
    private List<AppInfo> mAppInfo;
	private LockAppDao mDao;
	private List<String> mLockPackageNameList;
	private List<AppInfo> mLockList;
	private List<AppInfo> mUnlockList;
	private MyAdapter myLockAdapter;
	private MyAdapter myUnlockAdapter;
	private ListView lv_lock;
	private ListView lv_unlock;
	private TextView tv_lock_count;
	private TextView tv_unlock_count;
	private LinearLayout ll_unlock;
	private LinearLayout ll_lock;
	private Button btn_lock;
	private Button btn_unlock;
	
    
	private Handler mHandler = new Handler(){

		public void handleMessage(android.os.Message msg) {
			myLockAdapter = new MyAdapter(true);
			myUnlockAdapter = new MyAdapter(false);
			
			lv_lock.setAdapter(myLockAdapter);
			lv_unlock.setAdapter(myUnlockAdapter);
		};
	};
	private TranslateAnimation animation;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_lock_app);
    	
    	initUI();
    	initData();
    	initAnimation();
    }

	private void initAnimation() {
		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 1, 
				Animation.RELATIVE_TO_SELF, 0, 
				Animation.RELATIVE_TO_SELF, 0);
		animation.setDuration(500);
		
	}

	private void initData() {
		new Thread(){
			public void run() {
				//获取数据库中的已锁应用的集合
				mDao = LockAppDao.getInstance(getApplicationContext());
				mLockPackageNameList = mDao.findAll();
				//获取手机所有应用的集合
				mAppInfo = AppInfoProvider.getAppInfo(getApplicationContext());
				//创建存储已锁应用和未锁应用的集合
				mLockList = new ArrayList<AppInfo>();
				mUnlockList = new ArrayList<AppInfo>();
				for (AppInfo appInfo : mAppInfo) {
					//遍历所有应用的集合，判断包名是否存在于数据库中，如果存在则加入已锁应用集合
					if(mLockPackageNameList.contains(appInfo.packageName)){
						mLockList.add(appInfo);
					}else{
						mUnlockList.add(appInfo);
					}
				}
				mHandler.sendEmptyMessage(0);
			};
		}.start();
		
		
	}

	private void initUI() {
		ll_unlock = (LinearLayout) findViewById(R.id.ll_unlock);
		ll_lock = (LinearLayout) findViewById(R.id.ll_lock);
		
		btn_lock = (Button) findViewById(R.id.btn_lock);
		btn_unlock = (Button) findViewById(R.id.btn_unlock);
		
		tv_lock_count = (TextView) findViewById(R.id.tv_lock_count);
		tv_unlock_count = (TextView) findViewById(R.id.tv_unlock_count);
		
		lv_lock = (ListView) findViewById(R.id.lv_lock);
		lv_unlock = (ListView) findViewById(R.id.lv_unlock);
			
		btn_unlock.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ll_unlock.setVisibility(View.VISIBLE);
				ll_lock.setVisibility(View.GONE);
				btn_unlock.setBackgroundResource(R.drawable.tab_left_pressed);
				btn_lock.setBackgroundResource(R.drawable.tab_right_default);
			}
		});

		btn_lock.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ll_lock.setVisibility(View.VISIBLE);
				ll_unlock.setVisibility(View.GONE);
				btn_unlock.setBackgroundResource(R.drawable.tab_left_default);
				btn_lock.setBackgroundResource(R.drawable.tab_right_pressed);
			}
		});
}
	
	class MyAdapter extends BaseAdapter{
        //通过isLock 变量区别是加锁适配器还是未加锁适配器
		private  boolean isLock;
		
		public MyAdapter(boolean isLock){
			this.isLock = isLock;
		}
		
		@Override
		public int getCount() {
			if(isLock){
				tv_lock_count.setText("加锁应用:"+mLockList.size());
			   return mLockList.size();
			}else{
				tv_unlock_count.setText("未加锁应用:"+mUnlockList.size());
				return mUnlockList.size();
			}
		}

		@Override
		public AppInfo getItem(int position) {
			if(isLock){
				   return mLockList.get(position);
				}else{
					return mUnlockList.get(position);
				}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
			if(convertView == null){
				convertView = View.inflate(getApplicationContext(), R.layout.listview_lock_app_item,null);
                holder = new ViewHolder();
                holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
                holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			    holder.iv_lock = (ImageView) convertView.findViewById(R.id.iv_lock);
			    convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.iv_icon.setBackgroundDrawable(getItem(position).icon);
			holder.tv_name.setText(getItem(position).name);
			if(isLock){
				holder.iv_lock.setBackgroundResource(R.drawable.lock);
			}else{
				holder.iv_lock.setBackgroundResource(R.drawable.unlock);
				
			}
			final View animationView = convertView;
			
			holder.iv_lock.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					animationView.startAnimation(animation);
					//监听动画完成后再执行移除
					animation.setAnimationListener(new AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {
							
						}
						
						@Override
						public void onAnimationRepeat(Animation animation) {
							
						}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							if(isLock){
								//从数据库中移除
								mDao.delete(getItem(position).packageName);
								//从集合中移除(先添加再移除)
								mUnlockList.add(getItem(position));
								mLockList.remove(position);
								//通知适配器刷新
		                        if(myLockAdapter!=null && myUnlockAdapter!=null){
		                        	myLockAdapter.notifyDataSetChanged();
		                        	myUnlockAdapter.notifyDataSetChanged();
		                        }
							}else{
								//加入数据库
								mDao.insert(getItem(position).packageName);
								mLockList.add(getItem(position));
								mUnlockList.remove(position);
								//通知适配器刷新
								if(myUnlockAdapter!=null && myLockAdapter!=null){
									myUnlockAdapter.notifyDataSetChanged();
									myLockAdapter.notifyDataSetChanged();
								}
							}
							
						}
					});
	
				
				}
			});
			
			return convertView;
		}
		
	}
	
	static class ViewHolder{
		ImageView iv_icon,iv_lock;
		TextView tv_name;
	}
}
