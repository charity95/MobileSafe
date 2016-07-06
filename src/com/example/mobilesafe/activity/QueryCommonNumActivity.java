package com.example.mobilesafe.activity;

import java.util.List;

import com.example.mobilesafe.R;
import com.example.mobilesafe.engine.CommonNumDao;
import com.example.mobilesafe.engine.CommonNumDao.Child;
import com.example.mobilesafe.engine.CommonNumDao.Group;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

public class QueryCommonNumActivity extends Activity {
     private ExpandableListView elv_commonnum;
	private List<Group> mGroupList;
	private MyAdapter adapter;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_query_commonnum);
    	
    	initUI();
    	initData();
    	
    	
    }

	private void initData() {
		CommonNumDao dao = new CommonNumDao();
		mGroupList = dao.getGroup();
        
		adapter = new MyAdapter();
		elv_commonnum.setAdapter(adapter);
	}

	private void initUI() {
		elv_commonnum = (ExpandableListView) findViewById(R.id.elv_commonnum);
		
		elv_commonnum.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				//打电话
				Intent intent = new Intent(Intent.ACTION_CALL);
				intent.setData(Uri.parse("tel:"+adapter.getChild(groupPosition, childPosition).number));
				startActivity(intent);
				
				return false;
			}
		});
	}
	
	class MyAdapter extends BaseExpandableListAdapter{

		@Override
		public int getGroupCount() {
			return mGroupList.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return getGroup(groupPosition).childList.size();
		}

		@Override
		public Group getGroup(int groupPosition) {
			return mGroupList.get(groupPosition);
		}

		@Override
		public Child getChild(int groupPosition, int childPosition) {
			return getGroup(groupPosition).childList.get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			TextView textView = new TextView(getApplicationContext());
			textView.setText("         "+getGroup(groupPosition).name);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
			textView.setTextColor(Color.GRAY);
			return textView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			//item的布局用之前联系人的布局
			View view = View.inflate(getApplicationContext(), R.layout.contact_listview_item, null);
			TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
			TextView tv_phone = (TextView) view.findViewById(R.id.tv_phone);
			
			tv_phone.setText(getChild(groupPosition, childPosition).number);
			tv_name.setText(getChild(groupPosition, childPosition).name);
			
			return view;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return true;
		}
		
	}
}
