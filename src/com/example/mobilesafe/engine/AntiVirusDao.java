package com.example.mobilesafe.engine;


import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class AntiVirusDao {
     //数据库的访问路径
    static String path = "data/data/com.example.mobilesafe/files/antivirus.db";

     //查询数据库中所有病毒的md5码
	public static List<String> getVirus(){
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
	    Cursor cursor = db.query("datable", new String[]{"md5"}, null, null, null, null, null);
	    List<String> virusList = new ArrayList<String>();
	    while(cursor.moveToNext()){
	    	virusList.add(cursor.getString(0));
	    }
	    cursor.close();
	    db.close();
	    
	    return virusList;
	}
}
