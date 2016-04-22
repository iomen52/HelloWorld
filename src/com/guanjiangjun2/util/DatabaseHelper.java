package com.guanjiangjun2.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DatabaseHelper extends SQLiteOpenHelper{

	public DatabaseHelper(Context conext,String name,CursorFactory factory,int version){
		super(conext, name, factory, version);
	}

	public DatabaseHelper(Context conext,String name,int version){
		this(conext, name, null, version);
	}
	
	public DatabaseHelper(Context conext,String name){
		this(conext, name, 1);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		//db.execSQL("create table history(id int,lat double,lng double,updatetime DATETIME DEFAULT CURRENT_TIMESTAMP)");
		//db.execSQL("create table history(id long,lat DOUBLE DECIMAL(10,6),lng DOUBLE DECIMAL(10,6),updatetime DATETIME)");
		db.execSQL("create table history(id long,mode int,lat varchar(12),lng varchar(12),updatetime DATETIME)");
		db.execSQL("create table device(nickname varchar(12),imei varchar(15),password varchar(16))");
		///data/data/com.example.guanjiangjun2/databases
		// sqlite3 history
		// .schema //查看数据库中有哪些表
		// 
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
}
