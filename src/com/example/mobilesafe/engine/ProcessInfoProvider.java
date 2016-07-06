package com.example.mobilesafe.engine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.mobilesafe.R;
import com.example.mobilesafe.db.bean.ProcessInfo;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;

public class ProcessInfoProvider {
       
	private static FileReader fileReader;
	private static BufferedReader bufferedReader;


	//获取手机运行的进程数目
	public static int getProgressCount(Context context){
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    List<RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
	   
	    return runningAppProcesses.size();
	}
    
	/**获取手机可用内存
	 * @param context
	 * @return 返回值是bytes
	 */
	public static long getAvailMemory(Context context){
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    
		MemoryInfo memoryInfo = new MemoryInfo();
		am.getMemoryInfo(memoryInfo);
	    return memoryInfo.availMem;
	}
	
	
	/**获取手机的总内存
	 * @return 返回0则有异常，返回单位是bytes
	 */
	public static long getTotalMemory(){
		//可以按getAvailMemory的方法获取总内存，但是只能在API14及以上才能用
		//从手机的配置文件里读取,读取proc/meminfo，将内容写入文件，读取文件的第一行，获取数字字符，转化成btyes返回
		try {
			fileReader = new FileReader("proc/meminfo");
			bufferedReader = new BufferedReader(fileReader);
			//读取文件的第一行(单位是kb )
			String firstLine = bufferedReader.readLine();
			//将字符转化成字符数组
			char[] charArray = firstLine.toCharArray();
		    StringBuffer stringBuffer = new StringBuffer();
			//遍历数组，取出数字	
		    for (char c : charArray) {
				if(c>='0'&&c<='9'){
					stringBuffer.append(c);
				}
			}
		    //将kb转化成bytes
		   return  Long.parseLong(stringBuffer.toString())*1024;
		    
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(fileReader!=null && bufferedReader!=null){
				try {
					bufferedReader.close();
					fileReader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return 0 ;
	}
	
	
	//获取手机的进程信息
	public static List<ProcessInfo> getProcessInfo(Context context){
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		PackageManager pm = context.getPackageManager();
	    
		List<RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
	    List<ProcessInfo> processInfoList = new ArrayList<ProcessInfo>();
		
	    for (RunningAppProcessInfo info : runningAppProcesses) {
			//获取进程的名称，包名，图标，占用，内存是否被选中，是否是系统进程
	    	ProcessInfo processInfo = new ProcessInfo();
	    	// 进程的名称 == 应用的包名
	    	processInfo.packageName = info.processName;
	    	android.os.Debug.MemoryInfo[] processMemoryInfo = am.getProcessMemoryInfo(new int[]{info.pid});
		    //索引值为0的才是当前进程的内存信息
	    	android.os.Debug.MemoryInfo memoryInfo = processMemoryInfo[0];
	    	//获取当前进程已使用的内存大小
	    	processInfo.memory = memoryInfo.getTotalPrivateDirty()*1024;
	    	try {
	    		//获取进程的名称,图标
				ApplicationInfo applicationInfo = pm.getApplicationInfo(processInfo.packageName, 0);
			    processInfo.name = applicationInfo.loadLabel(pm).toString();
	    	    processInfo.icon = applicationInfo.loadIcon(pm);
	    	    //判断是否是系统进程
	    	    if((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM){
	    	    	processInfo.isSystem = true;
	    	    }else{
	    	    	processInfo.isSystem = false;
	    	    }
	    	    
	    	} catch (NameNotFoundException e) {
				// 需要处理异常.将包名做名称，设置默认图标,是系统进程
				processInfo.name = processInfo.packageName;
				processInfo.icon = context.getResources().getDrawable(R.drawable.ic_android_black_48dp);
				processInfo.isSystem = true;
				e.printStackTrace();
			}
	    	 processInfoList.add(processInfo);
	    }
	    return processInfoList;
	}

	public static void killAll(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    List<RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        
	    for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
			if(runningAppProcessInfo.processName.equals(context.getPackageName())){
				continue;
			}
	    	am.killBackgroundProcesses(runningAppProcessInfo.processName);
		}
		
	}
}
