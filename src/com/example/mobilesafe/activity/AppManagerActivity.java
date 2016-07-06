package com.example.mobilesafe.activity;

import java.util.ArrayList;
import java.util.List;

import com.example.mobilesafe.R;
import com.example.mobilesafe.db.bean.AppInfo;
import com.example.mobilesafe.engine.AppInfoProvider;
import com.example.mobilesafe.utils.ToastUtil;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.drm.DrmStore.Action;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class AppManagerActivity extends Activity implements OnClickListener{
	
    private static List<AppInfo> mSystemAppList;
	private static List<AppInfo> mCustomerAppList;
	private ListView lv_app;
	private MyAdapter adapter;
	private TextView mFrameTitle;
	private TextView uninstall;
	private TextView start;
	private TextView share;
	private AppInfo appInfo;
	private PopupWindow popupWindow;
	
	private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
			adapter = new MyAdapter();
			lv_app.setAdapter(adapter);
			
			if (mFrameTitle!=null && mCustomerAppList!=null) {
				mFrameTitle.setText("用户应用(" + mCustomerAppList.size() + ")");
			}
		};
	};
	
	
     @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_app_manager);
    	
    	//初始化可用内存显示
    	initMemory();
    	//初始化手机应用信息
    	initApp();
    }
     
     @Override
    protected void onResume() {
    	super.onResume();
    	//每次获取焦点时重新载入数据，相当于刷新
    	getData();
    }

	private void initApp() {
		lv_app = (ListView) findViewById(R.id.lv_app);
		mFrameTitle = (TextView) findViewById(R.id.frameTitle);
		
		//监听listview的滑动状态
		lv_app.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (mCustomerAppList!=null && mSystemAppList!=null) {
					if (firstVisibleItem < mCustomerAppList.size() + 1) {
						mFrameTitle.setText("用户应用(" + mCustomerAppList.size()
								+ ")");
					} else {
						mFrameTitle.setText("系统应用(" + mSystemAppList.size()
								+ ")");
					}
				}
				
			}
		});
		
		lv_app.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(position==0 || position ==mCustomerAppList.size()+1){
	        		return;
	        	}
				if(position<=mCustomerAppList.size()){
					appInfo = mCustomerAppList.get(position-1);
				}else{
					appInfo = mSystemAppList.get(position-mCustomerAppList.size()-2);
				}
					//弹出窗体
					showPopuWindow(view);	
			}
		});
	}

	private void getData() {
		new Thread(){
			public void run() {
				//获取手机全部应用的集合
				List<AppInfo> appList = AppInfoProvider.getAppInfo(getApplicationContext());
				//获取系统应用集合和用户应用集合对象
				mSystemAppList = new ArrayList<AppInfo>();
				mCustomerAppList = new ArrayList<AppInfo>();
				//将系统应用和用户应用分别装入两个集合中
				for (AppInfo appInfo : appList) {
					if(appInfo.isSystem){
						mSystemAppList.add(appInfo);
					}else{
						mCustomerAppList.add(appInfo);
					}
				}
				mHandler.sendEmptyMessage(0);
			};
		}.start();
	}
    
	protected void showPopuWindow(View view) {
		View popuView = view.inflate(this, R.layout.popuwindow, null);
		
		uninstall = (TextView) popuView.findViewById(R.id.uninstall);
		start = (TextView) popuView.findViewById(R.id.start);
		share = (TextView) popuView.findViewById(R.id.share);
		
		uninstall.setOnClickListener(this);
		start.setOnClickListener(this);
		share.setOnClickListener(this);
		
		AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
		alphaAnimation.setDuration(1000);
		alphaAnimation.setFillAfter(true);
		
		ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		scaleAnimation.setDuration(1000);
		scaleAnimation.setFillAfter(true);
		
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(scaleAnimation);
		
		popupWindow = new PopupWindow(popuView,LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT,true);
		//给popuwindow设置一个透明的背景，不然返回键无法取消窗口
		popupWindow.setBackgroundDrawable(new ColorDrawable());
		popupWindow.showAsDropDown(view, 150, -view.getHeight());
		
		view.setAnimation(animationSet);
	}

	class MyAdapter extends BaseAdapter{
        //根据positon判断条目类型，0是纯文字标题类型，1是图片文字普通条目类型
		@Override
        public int getItemViewType(int position) {
        	if(position==0 || position ==mCustomerAppList.size()+1){
        		return 0;
        	}else{
        		return 1;
        	}
        }
        
        //listview条目类型 ，2种
        @Override
        public int getViewTypeCount() {
        	// TODO Auto-generated method stub
        	return super.getViewTypeCount()+1;
        }
		
		@Override
		public int getCount() {
			return mCustomerAppList.size()+mSystemAppList.size()+2;
		}
        
		//根据条目索引值返回条目的信息
		@Override
		public AppInfo getItem(int position) {
			if(position==0 || position ==mCustomerAppList.size()+1){
        		return null;
        	}
			if(position<=mCustomerAppList.size()){
				return mCustomerAppList.get(position-1);
			}else{
				return mSystemAppList.get(position-mCustomerAppList.size()-2);
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolderA holderA;
			ViewHolderB holderB;
			if(getItemViewType(position)==0){
				//纯文字标题条目
				if(convertView == null){
					convertView = View.inflate(getApplicationContext(), R.layout.listview_app_title_item, null);
					holderB = new ViewHolderB();
					holderB.title = (TextView) convertView.findViewById(R.id.title);
					convertView.setTag(holderB);
				}else{
					holderB = (ViewHolderB) convertView.getTag();
				}
				if(position != 0){
					holderB.title.setText("系统应用("+mSystemAppList.size()+")");
				}
				
			}else{
				//图片文字普通条目
				if(convertView == null){
					convertView = View.inflate(getApplicationContext(), R.layout.listview_app_item, null);
					holderA = new ViewHolderA();
					holderA.name = (TextView) convertView.findViewById(R.id.name);
					holderA.path = (TextView) convertView.findViewById(R.id.path);
					holderA.icon = (ImageView) convertView.findViewById(R.id.icon);
					convertView.setTag(holderA);
				}else{
					holderA = (ViewHolderA) convertView.getTag();
				}
				holderA.name.setText(getItem(position).name);
				holderA.icon.setBackgroundDrawable(getItem(position).icon);
				if(getItem(position).isSdCard){
					holderA.path.setText("SD卡应用");
				}else{
					holderA.path.setText("手机应用");
				}
			}
			return convertView;
		}
		
	}
	
	static class ViewHolderA{
		TextView name,path;
		ImageView icon;
	}
	
	static class ViewHolderB{
		TextView title;
	}
	
	
	private void initMemory() {
		//获取内存的路径
		String path = Environment.getDataDirectory().getAbsolutePath();
		String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		
		TextView tv_memory = (TextView) findViewById(R.id.tv_memory);
		TextView tv_sd_memory = (TextView) findViewById(R.id.tv_sd_memory);
		
        //获取以上两个路径文件夹的可用大小(将long转化成 K，M等形式)
		String memoryAvail = Formatter.formatFileSize(this, getAvailSpace(path));
		String sdMemoryAvail = Formatter.formatFileSize(this, getAvailSpace(sdPath));

		
		tv_memory.setText("磁盘可用内存："+memoryAvail);
		tv_sd_memory.setText("sd卡可用内存："+sdMemoryAvail);
	}
    
	//返回值是bytes
	private long getAvailSpace(String path) {
		//获取可用磁盘大小的类
		StatFs statFs = new StatFs(path);
		//可用区块的个数
		long count = statFs.getBlockCount();
		//可用区块的大小
		long size = statFs.getBlockSize();
		return count*size;
	}
    
	//popuwindow的点击事件
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.uninstall:
			if (!appInfo.isSystem) {
				Intent intent = new Intent("android.intent.action.DELETE");
				intent.addCategory("android.intent.category.DEFAULT");
				intent.setData(Uri.parse("package:" + appInfo.packageName));
				startActivity(intent);
			}else{
				ToastUtil.show(getApplicationContext(), "此应用不能卸载");
			}
			break;
		case R.id.start:
			//通过桌面启动指定包名应用
			PackageManager pm = getPackageManager();
			Intent intent = pm.getLaunchIntentForPackage(appInfo.packageName);
			if(intent!=null){
				startActivity(intent);
			}else{
				ToastUtil.show(getApplicationContext(), "此应用不能打开");
			}
			break;
		case R.id.share:
			Intent intentShare = new Intent(Intent.ACTION_SEND);
			intentShare.putExtra(Intent.EXTRA_TEXT, "分享一个应用，应用名称为"+appInfo.name);
			intentShare.setType("text/plain");
			startActivity(intentShare);
			break;
		}
		
		if(popupWindow!=null){
			popupWindow.dismiss();
		}
	}
}
