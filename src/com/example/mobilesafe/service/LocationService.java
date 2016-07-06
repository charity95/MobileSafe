package com.example.mobilesafe.service;

import com.example.mobilesafe.utils.ConstantValue;
import com.example.mobilesafe.utils.SpUtil;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class LocationService extends Service {
    @Override
    public void onCreate() {
    	super.onCreate();
    	//获取手机的经纬度
    	//获取位置管理者对象
    	LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	Criteria criteria = new Criteria();
    	//允许花费流量
    	criteria.setCostAllowed(true);
    	criteria.setAccuracy(Criteria.ACCURACY_FINE);
    	//以最优方式去定位
    	String bestProvider = lm.getBestProvider(criteria, true);
    	lm.requestLocationUpdates(bestProvider, 0, 0, new LocationListener() {
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				
			}
			
			@Override
			public void onProviderEnabled(String provider) {
	
			}
			
			@Override
			public void onProviderDisabled(String provider) {
				
			}
			
			@Override
			public void onLocationChanged(Location location) {
				//获取经纬度
				double latitude = location.getLatitude();
				double longitude = location.getLongitude();
				android.telephony.SmsManager manager = android.telephony.SmsManager.getDefault();
			   //获取安全手机号码
			   String phoneNumber = SpUtil.getString(getApplicationContext(), ConstantValue.SECURITY_PHONE, "");
			   manager.sendTextMessage(phoneNumber, null, "longitude ="+longitude+",latitude = "+latitude, null, null);
			}
		});
    }
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
  
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
