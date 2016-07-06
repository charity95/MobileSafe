package com.example.mobilesafe.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.mobilesafe.db.BlackNumberSQLiteOpenHelper;
import com.example.mobilesafe.db.bean.BlackNumberInfo;
import com.example.mobilesafe.utils.ConstantValue;

public class BlackNumberDao {
    private BlackNumberSQLiteOpenHelper blackNumberSQLiteOpenHelper;

	//单例模式  
	//1.私有化构造方法
	private BlackNumberDao(Context context){
		blackNumberSQLiteOpenHelper = new BlackNumberSQLiteOpenHelper(context);
	}
	//2.声明一个当前类的对象
	private static BlackNumberDao blackNumberDao = null;
	//3.提供一个方法，如果当前对象为空，创建一个新对象
	public static BlackNumberDao getInstance(Context context){
		if(blackNumberDao == null){
            blackNumberDao = new BlackNumberDao(context);
		}
		return blackNumberDao;
	}
	
	//增加
	public void insert(String phone,String mode){
		SQLiteDatabase db = blackNumberSQLiteOpenHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put("phone", phone);
		values.put("mode", mode);
		db.insert("blacknumber", null, values);
		
		db.close();
	}
	
	//删除
	public void delete(String phone){
		SQLiteDatabase db = blackNumberSQLiteOpenHelper.getWritableDatabase();
		
		db.delete("blacknumber", "phone =?", new String[]{phone});
		
		db.close();
	}
	
	//修改
	public void update(String phone,String mode){
		SQLiteDatabase db = blackNumberSQLiteOpenHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put("mode", mode);
		
		db.update("blacknumber", values, "phone =?", new String[]{phone});
		
		db.close();
	}
	
	//查询数据库所有的数据
	public List<BlackNumberInfo> findAll(){
		SQLiteDatabase db = blackNumberSQLiteOpenHelper.getWritableDatabase();
        
		Cursor cursor = db.query("blacknumber", new String[]{"phone","mode"}, null, null, null, null, "_id desc");
		List<BlackNumberInfo> blackNumberList = new ArrayList<BlackNumberInfo>();
		while(cursor.moveToNext()){
			String phone = cursor.getString(0);
			String mode = cursor.getString(1);
			BlackNumberInfo blackNumberInfo = new BlackNumberInfo(); 
			blackNumberInfo.phone = phone;
			blackNumberInfo.mode = mode;
			blackNumberList.add(blackNumberInfo);
		}	
		cursor.close();
		db.close();
		return blackNumberList;
	}
	
	/**分页查询，查询倒数20条数据
	 * @param index 开始查询的索引值（倒序）
	 * @return 查询到的黑名单数据
	 */
	public List<BlackNumberInfo> find(int index){
		SQLiteDatabase db = blackNumberSQLiteOpenHelper.getWritableDatabase();
	    
		Cursor cursor = db.rawQuery("select phone,mode from blacknumber order by _id desc limit ?,20;", new String[]{index+""});
		List<BlackNumberInfo> blackNumberList = new ArrayList<BlackNumberInfo>();
		while(cursor.moveToNext()){
			String phone = cursor.getString(0);
			String mode = cursor.getString(1);
			BlackNumberInfo blackNumberInfo = new BlackNumberInfo(); 
			blackNumberInfo.phone = phone;
			blackNumberInfo.mode = mode;
			blackNumberList.add(blackNumberInfo);
		}	
		cursor.close();
		db.close();
		return blackNumberList;
	}
	
	/**获取数据库数据的条目总数
	 * @return 返回为零则是异常或者数据库为空
	 */
	public int getCount(){
		SQLiteDatabase db = blackNumberSQLiteOpenHelper.getWritableDatabase();
		
		Cursor cursor = db.rawQuery("select count(*) from blacknumber;", null);
		int count = 0;
		if(cursor.moveToNext()){
			count = Integer.parseInt(cursor.getString(0));
		}
		cursor.close();
		db.close();
		return count;
	}
	
	/**通过电话号码查询号码的拦截模式
	 * @param phone 查询的电话号码
	 * @return 返回拦截模式  1拦截短信 2拦截电话 3拦截所有 0无信息
	 */
	public int getMode(String phone){
		int mode = 0;
		SQLiteDatabase db = blackNumberSQLiteOpenHelper.getWritableDatabase();
		
		Cursor cursor = db.query("blacknumber", new String[]{"mode"}, "phone = ?", new String[]{phone}, null, null, null);
		if(cursor.moveToNext()){
			mode = Integer.parseInt(cursor.getString(0));
		}
		
		cursor.close();
		db.close();
		return mode;
	}
}
