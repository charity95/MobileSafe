package com.example.mobilesafe.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

public class ServiceUtil {
	
	/**检测服务是否正在运行
	 * @param context
	 * @param serviceName 需要检测的服务名称
	 * @return
	 */
	public static boolean getService(Context context,String serviceName){
		//通过活动 管理者，可以获取应用的所有正在运行的服务
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> runningServices = am.getRunningServices(100);
		for (RunningServiceInfo runningServiceInfo : runningServices) {
			if(serviceName.equals(runningServiceInfo.service.getClassName())){
				return true;
			}
		}
		return false;
	}
}
