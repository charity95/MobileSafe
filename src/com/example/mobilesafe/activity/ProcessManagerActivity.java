package com.example.mobilesafe.activity;

import java.util.ArrayList;
import java.util.List;

import com.example.mobilesafe.R;
import com.example.mobilesafe.activity.AppManagerActivity.MyAdapter;
import com.example.mobilesafe.activity.AppManagerActivity.ViewHolderA;
import com.example.mobilesafe.activity.AppManagerActivity.ViewHolderB;
import com.example.mobilesafe.db.bean.AppInfo;
import com.example.mobilesafe.db.bean.ProcessInfo;
import com.example.mobilesafe.engine.AppInfoProvider;
import com.example.mobilesafe.engine.ProcessInfoProvider;
import com.example.mobilesafe.utils.ConstantValue;
import com.example.mobilesafe.utils.SpUtil;
import com.example.mobilesafe.utils.ToastUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class ProcessManagerActivity extends Activity implements OnClickListener {
    private ListView lv_process;
	private TextView mFrameTitle;
    private static List<ProcessInfo> mSystemAppList;
	private static List<ProcessInfo> mCustomerAppList;
	private MyAdapter adapter;
	protected ProcessInfo processInfo;
	
	
	private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
        	adapter = new MyAdapter();
			lv_process.setAdapter(adapter);
			
			if (mFrameTitle!=null && mCustomerAppList!=null) {
				mFrameTitle.setText("用户进程(" + mCustomerAppList.size() + ")");
			}
		};
	};
	private int mProcessCount;
	private TextView tv_memory;
	private TextView tv_process_count;
	private long availMemory;
	private String strTotalMemory;
	
	  @Override
      protected void onCreate(Bundle savedInstanceState) {
	     // TODO Auto-generated method stub
	     super.onCreate(savedInstanceState);
	     setContentView(R.layout.activity_progress_manager);
	   
	     //初始化标题（进程总数，可用空间）
	     initProgressTitle();
	     //初始化进程信息
	     initProcessInfo();
      }
	  
	   @Override
	    protected void onResume() {
	    	super.onResume();
	    	//每次获取焦点时重新载入数据，相当于刷新
	    	getData();
	    }

    private void initProcessInfo() {
		lv_process = (ListView) findViewById(R.id.lv_process);
		mFrameTitle = (TextView) findViewById(R.id.frameTitle);
		
		Button select_all = (Button) findViewById(R.id.select_all);
		Button select_reverse = (Button) findViewById(R.id.select_reverse);
		Button clean_all = (Button) findViewById(R.id.clean_all);
		Button setting = (Button) findViewById(R.id.setting);
		
		select_all.setOnClickListener(this);
		select_reverse.setOnClickListener(this);
		clean_all.setOnClickListener(this);
		setting.setOnClickListener(this);

		//监听listview的滑动状态
		lv_process.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (mCustomerAppList != null && mSystemAppList != null) {
					if (firstVisibleItem < mCustomerAppList.size() + 1) {
						mFrameTitle.setText("用户进程(" + mCustomerAppList.size()
								+ ")");
					} else {
						mFrameTitle.setText("系统进程(" + mSystemAppList.size()
								+ ")");
					}
				}

			}
		});
		
		lv_process.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(position==0 || position ==mCustomerAppList.size()+1){
	        		return;
	        	}
				if(position<=mCustomerAppList.size()){
					processInfo = mCustomerAppList.get(position-1);
				}else{
					processInfo = mSystemAppList.get(position-mCustomerAppList.size()-2);
				}
				if(processInfo!=null){
					if(!processInfo.packageName.equals(getPackageName())){
						processInfo.isCheck = !processInfo.isCheck;
						//切换checkbox的状态。view就是当前点击条目对象
						CheckBox ckBox = (CheckBox) view.findViewById(R.id.checkBox);
						ckBox.setChecked(processInfo.isCheck);
					}
				}
			}
		});
	}
    
    private void getData() {
		new Thread(){

			public void run() {
				List<ProcessInfo> appList = ProcessInfoProvider.getProcessInfo(getApplicationContext());
				//获取系统应用集合和用户应用集合对象
				mSystemAppList = new ArrayList<ProcessInfo>();
				mCustomerAppList = new ArrayList<ProcessInfo>();
				//将系统应用和用户应用分别装入两个集合中
				for (ProcessInfo appInfo : appList) {
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

	private void initProgressTitle() {
	      tv_process_count = (TextView) findViewById(R.id.tv_process_count);
	      tv_memory = (TextView) findViewById(R.id.tv_memory);
          
	      //获取进程总数
	      mProcessCount = ProcessInfoProvider.getProgressCount(this);
	      tv_process_count.setText("进程总数："+mProcessCount);
	      //获取手机运行总内存
	      long totalMemory = ProcessInfoProvider.getTotalMemory();
	      //获取手机可用内存
	      availMemory = ProcessInfoProvider.getAvailMemory(this);
	      //格式化
	      strTotalMemory = Formatter.formatFileSize(this, totalMemory);
	      String strAvailMemory = Formatter.formatFileSize(this, availMemory);
	      
	      tv_memory.setText("剩余/总共："+strAvailMemory+"/"+strTotalMemory);
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
		
        
        //要加2！！算上两个标题条目！当只有一个当前进程时就不会显示了。它的返回值就是listview会显示出来的条目值！
		@Override
		public int getCount() {
			if(SpUtil.getBoolean(getApplicationContext(), ConstantValue.SHOW_SYSTEM_PROCESS, true)){
				return mCustomerAppList.size()+mSystemAppList.size()+2;
			}else{
				//不显示系统进程
				return mCustomerAppList.size()+1;
			}
			
		}
        
		//根据条目索引值返回条目的信息
		@Override
		public ProcessInfo getItem(int position) {
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
					holderB.title.setText("系统进程("+mSystemAppList.size()+")");
				}
			}else{
				//图片文字普通条目
				if(convertView == null){
					convertView = View.inflate(getApplicationContext(), R.layout.listview_process_item, null);
					holderA = new ViewHolderA();
					holderA.name = (TextView) convertView.findViewById(R.id.name);
					holderA.memory = (TextView) convertView.findViewById(R.id.memory);
					holderA.icon = (ImageView) convertView.findViewById(R.id.icon);
					holderA.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
					convertView.setTag(holderA);
				}else{
					holderA = (ViewHolderA) convertView.getTag();
				}
				holderA.name.setText(getItem(position).name);
				holderA.icon.setBackgroundDrawable(getItem(position).icon);
				String memory = Formatter.formatFileSize(getApplicationContext(), getItem(position).memory);
				holderA.memory.setText("内存占用："+memory);
				
				//如果进程是手机安全卫士，则隐藏checkbox,不能关闭它
				if(getItem(position).packageName.equals(getPackageName())){
					holderA.checkBox.setVisibility(View.GONE);
				}else{
					holderA.checkBox.setVisibility(View.VISIBLE);
				
				}
				
				holderA.checkBox.setChecked(getItem(position).isCheck);
			}
			return convertView;
		}
		
	}
	
	static class ViewHolderA{
		TextView name,memory;
		ImageView icon;
		CheckBox checkBox;
	}
	
	static class ViewHolderB{
		TextView title;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.select_all:
			//全选
			selectAll();
			break;
		case R.id.select_reverse:
			//反选
			selectReverse();
			break;
		case R.id.clean_all:
			//一键清理
			cleanAll();
			break;
		case R.id.setting:
			//设置
			processSetting();
			break;

		}
		
	}

	private void processSetting() {
		Intent intent = new Intent(this,ProcessSettingActivity.class);
		startActivityForResult(intent, 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(adapter!=null){
			adapter.notifyDataSetChanged();
		}
	}

	private void cleanAll() {
		//存放要杀死的进程信息
		List<ProcessInfo> killProcess = new ArrayList<ProcessInfo>();
		long memReleaseSize = 0;
		
		for (ProcessInfo process : mCustomerAppList) {
			//当前应用不参与
			if(process.packageName.equals(getPackageName())){
				continue;
			}
			if(process.isCheck){
				//不能mAppList.remove(process)直接把process移除，不能对正在遍历的集合进行移除操作
				killProcess.add(process);
			}
		}
		for (ProcessInfo process : mSystemAppList) {
			if(process.isCheck){
				//不能mAppList.remove(process)直接把process移除，不能对正在遍历的集合进行移除操作
				killProcess.add(process);
			}
		}
		
		for (ProcessInfo processInfo : killProcess) {
			if(mCustomerAppList.contains(processInfo)){
				mCustomerAppList.remove(processInfo);
			}
			if(mSystemAppList.contains(processInfo)){
				mSystemAppList.remove(processInfo);
			}
			
			memReleaseSize += processInfo.memory;
		}
		//更新进程数  和可用内存
		mProcessCount -= killProcess.size();
		tv_process_count.setText("进程总数："+mProcessCount);
		
		availMemory += memReleaseSize;
		String strAvailMemory = Formatter.formatFileSize(this, availMemory);
		tv_memory.setText("剩余/总共："+strAvailMemory+"/"+strTotalMemory);
		
		String memRelease = Formatter.formatFileSize(this, memReleaseSize);
		ToastUtil.show(this,String.format("杀死了%d个进程,共释放%s内存", killProcess.size(),memRelease));
		
		if(adapter!=null){
			adapter.notifyDataSetChanged();
		}
		
	}

	private void selectReverse() {
		for (ProcessInfo process : mCustomerAppList) {
			//当前应用不参与
			if(process.packageName.equals(getPackageName())){
				continue;
			}
			process.setCheck(!process.isCheck);
		}
		for (ProcessInfo process : mSystemAppList) {
			process.setCheck(!process.isCheck);
		}
		if(adapter!=null){
			adapter.notifyDataSetChanged();
		}
		
	}

	private void selectAll() {
		for (ProcessInfo process : mCustomerAppList) {
			//当前应用不参与
			if(process.packageName.equals(getPackageName())){
				continue;
			}
			process.setCheck(true);
		}
		for (ProcessInfo process : mSystemAppList) {
			process.setCheck(true);
		}
		if(adapter!=null){
			adapter.notifyDataSetChanged();
		}
		
	}
	
	
	
} 


