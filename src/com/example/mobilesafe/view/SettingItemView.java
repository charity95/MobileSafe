package com.example.mobilesafe.view;

import com.example.mobilesafe.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingItemView extends RelativeLayout {
	
	private static final String NAMESPACE = "http://schemas.android.com/apk/res/com.example.mobilesafe";
	private TextView mDesTitle;
	private TextView mDesContent;
	private CheckBox mCheckBox;
	private String desoff;
	private String deson;

	public SettingItemView(Context context) {
		this(context,null);
	}

	public SettingItemView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public SettingItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        
		View.inflate(context, R.layout.setting_item, this);
		
		mDesTitle = (TextView) findViewById(R.id.desTitle);
		mDesContent = (TextView) findViewById(R.id.desContent);
		mCheckBox = (CheckBox) findViewById(R.id.checkBox);
		
		String destitle = attrs.getAttributeValue(NAMESPACE, "destitle");
	    desoff = attrs.getAttributeValue(NAMESPACE, "desoff");
		deson = attrs.getAttributeValue(NAMESPACE, "deson");
		
		mDesTitle.setText(destitle);
	    mDesContent.setText(desoff);
		
	}
   
	/**
	 * @return 当前SettingItemView的选中状态（与checkBox一致）
	 */
	public boolean isCheck(){
		//checkbox的选中结果决定当前条目是否开启
		return mCheckBox.isChecked();
	}
	
	/**根据当前SettingItemView的选中状态设置checkBox的选中状态
	 * @param isCheck
	 */
	public void setCheck(boolean isCheck){
		mCheckBox.setChecked(isCheck);
		if(isCheck){
			mDesContent.setText(deson);
		}else
		{
			mDesContent.setText(desoff);
		}
	}

	
}

