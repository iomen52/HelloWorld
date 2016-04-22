package com.guanjiangjun2.receiver;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.guanjiangjun2.util.Const;
import com.guanjiangjun2.util.MyApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HistoryCleanReceiver  extends BroadcastReceiver{

	private Context context;
	private MyApplication app;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		this.context=context;
		app = (MyApplication) context.getApplicationContext();
		String sql="delete from history where updatetime<datetime('"+GetCutTime()+"')";
		Const.ExecDatabaseSqlite(context,sql);
	}
	
	private String GetCutTime(){
		String cuttime;
		
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new java.util.Date();
		date.setDate(date.getDate() - 1);
		cuttime=sDateFormat.format(date)+" "+"00:00:00";
		
		Log.e("GetCutTime", "GetCutTime = "+cuttime);
		
		return cuttime;
	}

}

