package com.example.mobilesafe.engine;

import java.util.ArrayList;
import java.util.List;

import com.example.mobilesafe.db.bean.AppInfo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AppInfoProvider {

	private static List<AppInfo> mAppInfoAppList;

	public static List<AppInfo> getAppInfo(Context context){
    	 //获取包管理者对象
    	 PackageManager pm = context.getPackageManager();
    	 //获取手机上已安装的app信息的集合
         List<PackageInfo> packages = pm.getInstalledPackages(0);
         
         mAppInfoAppList = new ArrayList<AppInfo>();
         for (PackageInfo packageInfo : packages) {
			 AppInfo appInfo = new AppInfo(); 
             //获取应用包名、图标、名称
        	 appInfo.packageName = packageInfo.packageName;
        	 ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        	 appInfo.icon = applicationInfo.loadIcon(pm);
        	 appInfo.name = applicationInfo.loadLabel(pm).toString();
        	 //判断是否为sd中安装的应用
        	 if((applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE){
        		 //是sd卡中安装的应用
        		 appInfo.isSdCard = true;
        	 }else{
        		 //是手机磁盘中安装的应用
        		 appInfo.isSdCard = false;
        	 }
        			 
        	 //判断是否是系统应用
        	 if((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM){
        		 //是系统应用
        		 appInfo.isSystem = true;
        	 }else{
        		 //是用户应用
        		 appInfo.isSystem = false;
        	 }
        	 
        	 mAppInfoAppList.add(appInfo);
		}
            return mAppInfoAppList;
     }
}
