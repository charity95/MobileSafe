package com.example.mobilesafe.receiver;

import com.example.mobilesafe.utils.ConstantValue;
import com.example.mobilesafe.utils.SpUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.telephony.gsm.SmsManager;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//监听开机广播，检测sim卡序列号是否变更，如果变更则发送短信到安全手机号码上
        //获取本地存储的sim卡序列号
		String serialNumber = SpUtil.getString(context,ConstantValue.BIND_SIM, "");
		//获取手机的sim卡序列号
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	    String simSerialNumber = tm.getSimSerialNumber()+"123";
	    if(!simSerialNumber.equals(serialNumber)){
	    	//序列号不一致，发送sim 变更报警短信
	        android.telephony.SmsManager manager = android.telephony.SmsManager.getDefault();
	    	//获取安全手机号码
	    	String phoneNumber = SpUtil.getString(context, ConstantValue.SECURITY_PHONE, "");
	    	manager.sendTextMessage(phoneNumber, null, "sim changed !", null, null);
	    }
	}

}
