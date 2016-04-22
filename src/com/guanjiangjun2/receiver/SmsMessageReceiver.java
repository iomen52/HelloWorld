package com.guanjiangjun2.receiver;

import com.guanjiangjun2.util.MyApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SmsMessageReceiver extends BroadcastReceiver {

	private MyApplication app = null;
	private Context context;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		this.context=context;
		app = (MyApplication) context.getApplicationContext();
		
	}


}
