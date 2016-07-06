package com.example.mobilesafe.view;

import com.example.mobilesafe.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingClickView extends RelativeLayout {
	private TextView mClickTitle;
	private TextView mclickContent;


	public SettingClickView(Context context) {
		this(context,null);
	}

	public SettingClickView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public SettingClickView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        
		View.inflate(context, R.layout.click_item, this);
		
		mClickTitle = (TextView) findViewById(R.id.clickTitle);
		mclickContent = (TextView) findViewById(R.id.clickContent);
		
	}
   
	public void setTitle(String title){
		mClickTitle.setText(title);
	}
	
	public void setContent(String content){
		mclickContent.setText(content);
	}
	
}
