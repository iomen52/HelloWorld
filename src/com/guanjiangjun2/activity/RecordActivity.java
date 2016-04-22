package com.guanjiangjun2.activity;

import com.example.guanjiangjun2.R;
import com.guanjiangjun2.util.Const;
import com.guanjiangjun2.util.MyApplication;
import com.guanjiangjun2.util.TimeOutClass;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class RecordActivity extends Activity{

	//private ImageButton RecordImageViewTracking;
	private TimeOutClass timeoutly;
	private boolean RecordSwitchEvent = false; // 获得人为点击还是代码点击的标志
	private boolean IsOpenRecord = false;// 是否打开录音开关
	private ProgressDialog progressDialog;
	private TextView RecordActionText;
	private MyApplication app;
	private ToggleButton RecordActionToggle;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record_layout);
		app = (MyApplication) getApplication();
		RecordActionText=(TextView)findViewById(R.id.RecordActionText);
		//RecordImageViewTracking=(ImageButton)findViewById(R.id.RecordImageViewTracking);
		RecordActionToggle=(ToggleButton)findViewById(R.id.RecordActionToggle);
		RecordActionToggle.setOnCheckedChangeListener(onCheckedChange);
		
		//BandingButton();
		GetDataFromXml();
		app.SetTopActivityHandler(mHandler);
	}


	public OnCheckedChangeListener onCheckedChange = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			// TODO Auto-generated method stub
			if (RecordSwitchEvent == true)
				return;
			if (isChecked) {
				// 打开
				if (!("".equals(app.GetMtkaddr()))) {
					SendGuardMessage("C" + app.GetMtkaddr() + "CKQLY",Const.FAILKQLY);
				}
				IsOpenRecord = true;
			} else {
				if (!("".equals(app.GetMtkaddr()))) {
					SendGuardMessage("C" + app.GetMtkaddr() + "CGBLY",Const.FAILGBLY);
				}
				IsOpenRecord = false;
			}
			RecordActionToggle.setEnabled(false);
			RecordActionToggle.getBackground().setAlpha(100);
			RecordActionText.setText("");
			progressDialog =ProgressDialog.show(RecordActivity.this, null, getString(R.string.all_activity_send));
			//DelayUpdateSwitch();
		}
	};
	
	private void DelayUpdateSwitch(){
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(null!=RecordActionToggle){
					RecordActionToggle.setEnabled(true);
					RecordActionToggle.getBackground().setAlpha(255);
					Log.e("RecordActivity", "RecordActivity = true");
				}
				Log.e("RecordActivity", "RecordActivity");
			}
			
		}, 15*1000);
	}
	
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Const.RECVKQLY:
				Const.StopTimeOut(timeoutly);
				Toast.makeText(RecordActivity.this, R.string.record_activity_open, Toast.LENGTH_SHORT).show();
				app.Getsharepre().EditPutBoolean(Const.KEY_MONITOR,IsOpenRecord);
				RecordActionText.setText(R.string.record_activity_recording);
				RecordActionToggle.setEnabled(true);
				Const.CloseDialog(progressDialog);
				break;
			case Const.RECVGBLY:
				Const.StopTimeOut(timeoutly);
				Toast.makeText(RecordActivity.this,R.string.record_activity_close, Toast.LENGTH_SHORT).show();
				app.Getsharepre().EditPutBoolean(Const.KEY_MONITOR,IsOpenRecord);
				RecordActionText.setText(R.string.record_activity_close);
				RecordActionToggle.setEnabled(true);
				Const.CloseDialog(progressDialog);
				break;
			case Const.RECVNOCA:
				Const.StopTimeOut(timeoutly);
				RecordActionText.setText(R.string.record_activity_nosdcard);
				app.Getsharepre().EditPutBoolean(Const.KEY_MONITOR, false);
				RecordActionToggle.setEnabled(true);
				SetGuardActionSwitch(false);
				Const.CloseDialog(progressDialog);
				break;
			case Const.FAILKQLY:
				Const.StopTimeOut(timeoutly);
				Toast.makeText(RecordActivity.this, R.string.record_activity_failopen, Toast.LENGTH_SHORT).show();
				SetGuardActionSwitch(false);
				RecordActionToggle.setEnabled(true);
				Const.CloseDialog(progressDialog);
				break;
			case Const.FAILGBLY:
				Const.StopTimeOut(timeoutly);
				Toast.makeText(RecordActivity.this, R.string.record_activity_failclose, Toast.LENGTH_SHORT).show();
				SetGuardActionSwitch(true);
				RecordActionToggle.setEnabled(true);
				Const.CloseDialog(progressDialog);
				break;
			}
		}
	};
	
	private void SendGuardMessage(String str,int msg) {
		timeoutly=Const.SendMessageFunc(app, mHandler, str, msg);
		
	}
	
	private void GetDataFromXml() {
		IsOpenRecord=app.Getsharepre().GetSharePre().getBoolean(Const.KEY_MONITOR, false);
		SetGuardActionSwitch(IsOpenRecord);
		if(IsOpenRecord)
			RecordActionText.setText(R.string.record_activity_recording);
		else
			RecordActionText.setText("");
	}
	
	private void SetGuardActionSwitch(boolean bool){
		RecordSwitchEvent =true;
		RecordActionToggle.setChecked(bool);
		RecordSwitchEvent =false;
	}
		
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}
}
