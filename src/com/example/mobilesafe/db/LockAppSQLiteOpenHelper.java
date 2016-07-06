package com.example.mobilesafe.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class LockAppSQLiteOpenHelper extends SQLiteOpenHelper {

	public LockAppSQLiteOpenHelper(Context context) {
		super(context, "lockapp.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table lockapp" +
				"(_id integer primary key autoincrement," +
				"packagename varchar(20));");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
