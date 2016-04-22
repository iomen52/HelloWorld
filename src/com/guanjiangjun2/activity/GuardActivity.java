package com.guanjiangjun2.activity;

import com.example.guanjiangjun2.R;
import com.guanjiangjun2.util.Const;
import com.guanjiangjun2.util.MyApplication;
import com.guanjiangjun2.util.SwitchButton;
import com.guanjiangjun2.util.TimeOutClass;

import android.R.array;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class GuardActivity extends Activity{

	private ArrayAdapter<CharSequence> adapter;
	//private String[] SpinnerText={getString(R.string.guard_activity_high),getString(R.string.guard_activity_middle),getString(R.string.guard_activity_low)};;
	private Spinner VoiceSpinner;
	private TimeOutClass timeoutfd;
	private boolean FDSwitchEvent = false; // 获得人为点击还是代码点击的标志
	private boolean IsOpenAlarm = false;// 是否打开防盗开关
	private ProgressDialog progressDialog;
	private int SPLnum = 0;
	private MyApplication app;
	//private SwitchButton GuardActionSwitch;
	private ToggleButton GuardActionToggle;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guarda_layout);
		app = (MyApplication) getApplication();
		VoiceSpinner = (Spinner) findViewById(R.id.VoiceSpinner);
		//GuardActionSwitch=(SwitchButton)findViewById(R.id.GuardActionSwitch);
		//GuardActionSwitch.setOnCheckedChangeListener(onCheckedChange);
		
		GuardActionToggle=(ToggleButton)findViewById(R.id.GuardActionToggle);
		GuardActionToggle.setOnCheckedChangeListener(onCheckedChange);
		
		SetVoiceSpinnerContext();//初始化下拉菜单
		GetDataFromXml();
		
		app.SetTopActivityHandler(mHandler);
	}
	
	public OnCheckedChangeListener onCheckedChange = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
			// TODO Auto-generated method stub
			if (FDSwitchEvent == true)
				return;
			if (isChecked) {
				// 打开
				if (!("".equals(app.GetMtkaddr()))) {
					SendGuardMessage("C" + app.GetMtkaddr() + "CKQFD"+ SPLnum,Const.FAILKQFD);
				}
				IsOpenAlarm = true;
			} else {
				if (!("".equals(app.GetMtkaddr()))) {
					SendGuardMessage("C" + app.GetMtkaddr() + "CGBFD"+ SPLnum,Const.FAILGBFD);
				}
				IsOpenAlarm = false;
			}
			GuardActionToggle.setEnabled(false);
			progressDialog =ProgressDialog.show(GuardActivity.this, null, getString(R.string.all_activity_send));
			
		}
	};
	
	private void DelayUpdateSwitch(){
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(null!=GuardActionToggle)
					GuardActionToggle.setEnabled(true);
			}
			
		}, 15*1000);
	}
	
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Const.RECVKQFD:
				Const.StopTimeOut(timeoutfd);
				Toast.makeText(GuardActivity.this, getString(R.string.guard_activity_openguard), Toast.LENGTH_SHORT).show();
				app.Getsharepre().EditPutBoolean(Const.KEY_ALARM,IsOpenAlarm);
				GuardActionToggle.setEnabled(true);
				Const.CloseDialog(progressDialog);
				break;
			case Const.RECVGBFD:
				Const.StopTimeOut(timeoutfd);
				Toast.makeText(GuardActivity.this,  getString(R.string.guard_activity_closeguard), Toast.LENGTH_SHORT).show();
				app.Getsharepre().EditPutBoolean(Const.KEY_ALARM,IsOpenAlarm);
				GuardActionToggle.setEnabled(true);
				Const.CloseDialog(progressDialog);
				break;
			case Const.FAILKQFD:
				Const.StopTimeOut(timeoutfd);
				Toast.makeText(GuardActivity.this,  getString(R.string.guard_activity_failopenguard), Toast.LENGTH_SHORT).show();
				SetGuardActionSwitch(false);
				GuardActionToggle.setEnabled(true);
				Const.CloseDialog(progressDialog);
				break;
			case Const.FAILGBFD:
				Const.StopTimeOut(timeoutfd);
				Toast.makeText(GuardActivity.this,  getString(R.string.guard_activity_failcloseguard), Toast.LENGTH_SHORT).show();
				SetGuardActionSwitch(true);
				GuardActionToggle.setEnabled(true);
				Const.CloseDialog(progressDialog);
				break;
			}
		}
	};
	
	private void SetGuardActionSwitch(boolean bool){
		FDSwitchEvent =true;
		//GuardActionSwitch.setChecked(bool);
		GuardActionToggle.setChecked(bool);
		FDSwitchEvent =false;
	}
	
	private void SendGuardMessage(String str,int msg) {
		timeoutfd=Const.SendMessageFunc(app, mHandler, str, msg);
	}
	
	private void SetVoiceSpinnerContext() {

		adapter=ArrayAdapter.createFromResource(this, R.array.sensitivity, android.R.layout.simple_spinner_item);
				//new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, R.array.sensitivity);
		//adapter = new ArrayAdapter<String>(this,
				//android.R.layout.simple_spinner_item, SpinnerText);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		VoiceSpinner.setAdapter(adapter);
		// 添加事件Spinner事件监听
		VoiceSpinner.setOnItemSelectedListener(new SpinnerSelectedListener());
		// 设置默认值
		VoiceSpinner.setVisibility(View.VISIBLE);
	}
	
	class SpinnerSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			if(arg2==0){
				SPLnum = 24;
			}else if(arg2==1){
				SPLnum = 48;
			}else if(arg2 ==2){
				SPLnum = 99;
			}
			//Toast.makeText(GuardActivity.this, ""+SPLnum, Toast.LENGTH_SHORT).show();
			app.Getsharepre().EditPutInt(Const.SPLNUM, SPLnum);
			app.Getsharepre().EditPutInt(Const.KEY_ALARMLEVEL, arg2);
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}

	}
	
	private void GetDataFromXml() {
		int level= app.Getsharepre().GetSharePre().getInt(Const.KEY_ALARMLEVEL, 0);
		VoiceSpinner.setSelection(level);
		SPLnum = app.Getsharepre().GetSharePre().getInt(Const.SPLNUM, 24);
		IsOpenAlarm=app.Getsharepre().GetSharePre().getBoolean(Const.KEY_ALARM, false);
		SetGuardActionSwitch(IsOpenAlarm);
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
		app.SetTopActivityHandler(mHandler);
	}

	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}
}
