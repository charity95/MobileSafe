package com.example.mobilesafe.engine;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CommonNumDao {
	 //数据库的访问路径
    static String path = "data/data/com.example.mobilesafe/files/commonnum.db";
	private static SQLiteDatabase db;
    
    public List<Group> getGroup(){
    	db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
    	Cursor cursor = db.query("classlist", new String[]{"idx","name"}, null, null, null, null, null);
        List<Group> groupList = new ArrayList<Group>();
    	while(cursor.moveToNext()){
    		Group group = new Group();
    		group.idx = cursor.getString(0);
    		group.name = cursor.getString(1);
    		group.childList = getChild(group.idx);
    		groupList.add(group);
    	}
    	cursor.close();
    	db.close();
       return groupList;
    }
    
    public List<Child> getChild(String idx){
    	db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
    	Cursor rawQuery = db.rawQuery("select * from table"+idx, null);
    	List<Child> childList = new ArrayList<Child>();
    	while(rawQuery.moveToNext()){
    		Child child = new Child();
    		child.number = rawQuery.getString(1);
    		child.name = rawQuery.getString(2);
    		childList.add(child);
    	}
    	rawQuery.close();
    	db.close();
    	return childList;
    }
    
    public class Group{
    	public String name;
    	public String idx;
    	public List<Child> childList;
    }
    public class Child{
    	public String name,number;
    }
   
}
