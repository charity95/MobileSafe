package com.example.mobilesafe.receiver;

import com.example.mobilesafe.R;
import com.example.mobilesafe.service.LocationService;
import com.example.mobilesafe.utils.ConstantValue;
import com.example.mobilesafe.utils.SpUtil;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
	private ComponentName mDeviceAdminSample;
	private DevicePolicyManager mDPM;
	private String tag = "SmsReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		//组件对象可以作为是否激活的判断标志
		mDeviceAdminSample = new ComponentName(context, DeviceAdmin.class);
	    mDPM = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE); 
		
		// 获取手机防盗总开关
        boolean open_security = SpUtil.getBoolean(context, ConstantValue.OPEN_SECURITY, false);
        if(open_security){
        	//获取短信内容
        	Object[] objects = (Object[]) intent.getExtras().get("pdus");
        	//循环遍历短信
        	for (Object object : objects) {
				//获取短信对象
        		SmsMessage message = SmsMessage.createFromPdu((byte[]) object);
        		//获取短信地址、内容
        		String address = message.getOriginatingAddress();
        		String body = message.getMessageBody();
        		//判断是否包含播放音乐的关键字
        		if(body.contains("#*alarm*#")){
        			MediaPlayer mediaPlayer = MediaPlayer.create(context,R.raw.ylzs);
        			mediaPlayer.setLooping(true);
        			mediaPlayer.start();
        		}
        		
        		//是否包含发送定位的关键字
        		if(body.contains("#*location*#")){
        			//开启服务
        			context.startService(new Intent(context,LocationService.class));
        		}
        		
        		//是否包含锁屏关键字(锁屏后重置密码为 123)
        		if(body.contains("#*lockscreen*#")){
        			if(mDPM.isAdminActive(mDeviceAdminSample)){
        				mDPM.lockNow();
        				mDPM.resetPassword("123", 0);
        			}else{
        				Toast.makeText(context, "请先激活", 0).show();
        			}
        		}
        		
        		//是否包含清除数据关键字
        		if(body.contains("#*wipedata*#")){
        			if(mDPM.isAdminActive(mDeviceAdminSample)){
        				mDPM.wipeData(0);//清除手机数据
        				mDPM.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);//手机sd卡数据
        			}else{
        				Toast.makeText(context, "请先激活", 0).show();
       			}
        			}
        			
        		}
        		
        		
			}
        }
	}
	
	


