package com.example.mobilesafe.activity;

import com.example.mobilesafe.R;

import android.app.Activity;
import android.net.TrafficStats;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.TextView;


public class TrafficActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_traffic);
    	
    	initUI();
    }

	private void initUI() {
		TextView tv_mobileRxBytes = (TextView) findViewById(R.id.mobileRxBytes);
		TextView tv_mobileTxBytes = (TextView) findViewById(R.id.mobileTxBytes);
		TextView tv_totalRxBytes = (TextView) findViewById(R.id.totalRxBytes);
		TextView tv_totalTxBytes = (TextView) findViewById(R.id.totalTxBytes);
		
		//获取手机的下载流量(数据流量)
		long mobileRxBytes = TrafficStats.getMobileRxBytes();
		String strMobileRxBytes = Formatter.formatFileSize(this, mobileRxBytes);
		
		//获取手机的数据总流量 （上传+下载）
		long mobileTxBytes = TrafficStats.getMobileTxBytes();
		String strMobileTxBytes = Formatter.formatFileSize(this, mobileTxBytes);

		//获取手机下载流量总和  数据流量+WiFi
		long totalRxBytes = TrafficStats.getTotalRxBytes();
		String strTotalRxBytes = Formatter.formatFileSize(this, totalRxBytes);

		//获取手机上传+下载的流量总和  数据流量+WiFi
		long totalTxBytes = TrafficStats.getTotalTxBytes();
		String strTotalTxBytes = Formatter.formatFileSize(this, totalTxBytes);
		
		tv_mobileRxBytes.setText("数据流量下载流量："+strMobileRxBytes);
		tv_mobileTxBytes.setText("数据流量总流量（上传+下载）："+strMobileTxBytes);
		tv_totalRxBytes.setText("手机流量下载流量（数据流量+wifi）："+strTotalRxBytes);
		tv_totalTxBytes.setText("手机流量总流量："+strTotalTxBytes);

	}
}
