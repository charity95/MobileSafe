package com.example.mobilesafe.global;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Application;
import android.os.Environment;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
    	super.onCreate();
    	
    	//捕获异常（应用任意模块）
    	Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			//获取到未捕获的异常后
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				//将捕获到的异常存储到SD卡
				String path = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"MobileSafeError.log";
				File file = new File(path);
				try {
					PrintWriter printWriter = new PrintWriter(file);
					ex.printStackTrace(printWriter);
					printWriter.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				//退出
				System.exit(0);
			}
		});
    }
}
