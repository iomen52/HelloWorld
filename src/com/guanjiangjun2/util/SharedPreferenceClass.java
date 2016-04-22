package com.guanjiangjun2.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreferenceClass {

	//private MyApplication app = null;
	private SharedPreferences msp;
	//private SharedPreferences.Editor editor;
	public SharedPreferenceClass(Context context) {
		//app = (MyApplication) context.getApplicationContext();
		msp = context.getSharedPreferences("SharedPreferences", 0);
	}
	
	public SharedPreferences GetSharePre(){
		return msp;
	}
	
	public void EditPutString(String key, String tmp) {
		Editor editor = msp.edit();
		editor.putString(key, tmp);
		editor.commit();
	}
	public void EditPutBoolean(String key, boolean tmp) {
		Editor editor = msp.edit();
		editor.putBoolean(key, tmp);
		editor.commit();
	}
	public void EditPutDouble(String key, double tmp) {
		Editor editor = msp.edit();
		editor.putFloat(key, (float) tmp);
		editor.commit();
	}
	public void EditPutInt(String key, int tmp) {
		Editor editor = msp.edit();
		editor.putInt(key, tmp);
		editor.commit();
	}
}
