package com.munchkin.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBhelper extends SQLiteOpenHelper{

	public static final String TABLE_DOOR = "door";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_NAME = "name";
	
	
	public static final String DATABASE_NAME = "Munchkin.db";
	private static final int DATABASE_VERSION = 1;
	
	// sql create database statement
	private static final String DATABASE_CREATE = "create table "
				+ TABLE_DOOR + " (" + COLUMN_ID 
				+ " integer primary key autoincrement, " + COLUMN_NAME
				+ " text not null);";
	
	public DBhelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldv, int newv) {
		Log.w(DBhelper.class.getName(),
				"Upgrading database from version " + oldv + " to "
				+ newv + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOOR);
		onCreate(db);
		
	}
	
	
	
	
	
	
	
	
	
	
}
