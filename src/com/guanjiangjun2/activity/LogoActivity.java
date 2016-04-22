package com.guanjiangjun2.activity;

import java.util.Timer;
import java.util.TimerTask;

import com.example.guanjiangjun2.R;
import com.guanjiangjun2.service.BackgroundServer;
import com.guanjiangjun2.util.Const;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

public class LogoActivity extends Activity {

	private final static String TAG = "LogoActivity"; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0){
			LogoActivity.this.finish();
            return; 
		}		
		setContentView(R.layout.logo_layout);
		//StartSocketService();//Æô¶¯SocketService
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				Intent intent = new Intent();
				intent.setClass(LogoActivity.this, LoginActivity.class);
				LogoActivity.this.startActivity(intent);
				LogoActivity.this.finish();
			}
		};
		timer.schedule(task, 1000 * 2);

	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
