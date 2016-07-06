package com.example.mobilesafe.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class SpUtil {
	
	private static SharedPreferences sp;
     //写
	public static void putBoolean(Context context,String key,Boolean value){
		if(sp == null){
			sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		}
		sp.edit().putBoolean(key, value).commit();
	}
	
	//读
	public static Boolean getBoolean(Context context,String key,Boolean defValue){
		if(sp == null){
			sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		}
		return sp.getBoolean(key, defValue);
	}
	
	 //写
		public static void putString(Context context,String key,String value){
			if(sp == null){
				sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
			}
			sp.edit().putString(key, value).commit();
		}
		
		//读
		public static String getString(Context context,String key,String defValue){
			if(sp == null){
				sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
			}
			return sp.getString(key, defValue);
		}
        
		//移除
		public static void remove(Context context, String key) {
			if(sp == null){
				sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
			}
			sp.edit().remove(key).commit();
			
		}
		
		public static void putInt(Context context,String key,int value){
			if(sp == null){
				sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
			}
			sp.edit().putInt(key, value).commit();
		}
		
		public static int getInt(Context context,String key,int defValue){
			if(sp == null){
				sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
			}
			return sp.getInt(key, defValue);
		}
		
}
