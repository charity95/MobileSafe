package com.example.mobilesafe.engine;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class AddressDao {
     //数据库的访问路径
     static String path = "data/data/com.example.mobilesafe/files/address.db";
	private static SQLiteDatabase db;
	private static  String mAddress;
	
	/**根据号码进行归属地查询，打开数据库，返回归属地地址
	 * @param phone 要查询的号码
	 */
	public static String getAddress(String phone){
		mAddress="未知号码";
		//用正则表达式去匹配手机号
		String regularExpression = "^1[3-8]\\d{9}";
		if (phone.matches(regularExpression)) {
			phone = phone.substring(0, 7);
			db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
			Cursor cursor = db.query("data1", new String[]{"outkey"}, "id = ?",new String[]{phone} , null, null, null);
			if(cursor.moveToNext()){
				String outkey = cursor.getString(0);
				Cursor indexCursor = db.query("data2", new String[]{"location"}, "area =?",new String[]{outkey}, null, null, null);
				if(indexCursor.moveToNext()){
					mAddress = indexCursor.getString(0);
				}
				indexCursor.close();
			}else{
				mAddress = "未知号码";
			}
			cursor.close();
		}else{
			int length = phone.length();
			switch (length) {
			case 3:
				mAddress = "报警电话";
				break;
			case 4:
				mAddress = "模拟器";
				break;
			case 5:
				mAddress = "服务电话";
				break;
			case 7:
				mAddress = "本地电话";
				break;
			case 8:
				mAddress = "本地电话";
				break;
			case 11:
				//(3+8) 区号+座机号（外地）
				phone = phone.substring(1,3);
				Cursor cursor1 = db.query("data2", new String[]{"location"}, "area =?", new String[]{phone}, null, null, null);
				if(cursor1.moveToNext()){
					mAddress = cursor1.getString(0);
				}else{
					mAddress = "未知号码";
				}
				cursor1.close();
				break;
			case 12:
				//4+8  区号+座机号（外地）
				phone = phone.substring(1,4);
				Cursor cursor2 = db.query("data2", new String[]{"location"}, "area =?", new String[]{phone}, null, null, null);
				if(cursor2.moveToNext()){
					mAddress = cursor2.getString(0);
				}else{
					mAddress = "未知号码";
				}
				cursor2.close();
				break;
			}
		}
		return mAddress;
	}
}
