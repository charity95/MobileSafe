package com.example.mobilesafe.db.dao;

import java.util.ArrayList;
import java.util.List;

import com.example.mobilesafe.db.LockAppSQLiteOpenHelper;
import com.example.mobilesafe.db.bean.AppInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class LockAppDao {
	private LockAppSQLiteOpenHelper lockAppSQLiteOpenHelper;
	private Context context;

	//单例模式  
	//1.私有化构造方法
	private LockAppDao(Context context){
		lockAppSQLiteOpenHelper = new LockAppSQLiteOpenHelper(context);
		this.context = context;
	}
	//2.声明一个当前类的对象
	private static LockAppDao lockAppDao = null;
	//3.提供一个方法，如果当前对象为空，创建一个新对象
	public static LockAppDao getInstance(Context context){
		if(lockAppDao == null){
			lockAppDao = new LockAppDao(context);
		}
		return lockAppDao;
	}
	
     //增加
	 public void insert(String packageName){
		SQLiteDatabase db = lockAppSQLiteOpenHelper.getWritableDatabase();
			
		ContentValues values = new ContentValues();
		values.put("packagename", packageName);
		db.insert("lockapp", null, values);
		
		db.close();
		//自定义uri 当数据库数据发生改变时
		context.getContentResolver().notifyChange(Uri.parse("content://app/change"), null);
	 }
	 
	 //移除
	 public void delete(String packageName){
		 SQLiteDatabase db = lockAppSQLiteOpenHelper.getWritableDatabase();
		 
		 db.delete("lockapp", "packagename = ?", new String[]{packageName});
		 db.close();
		 context.getContentResolver().notifyChange(Uri.parse("content://app/change"), null);
	 }
	 
	 //查询所有的已加锁应用的包名
	 public List<String> findAll(){
		 SQLiteDatabase db = lockAppSQLiteOpenHelper.getWritableDatabase();
		 
		 Cursor cursor = db.query("lockapp",new String[]{"packagename"}, null, null, null, null, null);
		 List<String> appPackageNameList = new ArrayList<String>();
		 while(cursor.moveToNext()){
			 appPackageNameList.add(cursor.getString(0));
		 }
		 cursor.close();
		 db.close();
		 return appPackageNameList;
	 }
	 
}
