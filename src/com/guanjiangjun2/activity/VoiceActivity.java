package com.guanjiangjun2.activity;

import com.example.guanjiangjun2.R;
import com.guanjiangjun2.util.Const;
import com.guanjiangjun2.util.MyApplication;
import com.guanjiangjun2.util.TimeOutClass;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class VoiceActivity extends Activity{


	private ArrayAdapter<CharSequence> adapter;
	private Spinner VoiceOptionSpinner;
	//private ImageButton VoiceImageViewTracking;
	private TimeOutClass timeoutbj;
	private ProgressDialog progressDialog;
	private Button VoiceOptionBtn;
	private int voicenum = 0;
	private MyApplication app;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.voice_layout);
		app = (MyApplication) getApplication();
		VoiceOptionSpinner = (Spinner) findViewById(R.id.VoiceOptionSpinner);
		VoiceOptionBtn=(Button)findViewById(R.id.VoiceOptionBtn);
		VoiceOptionBtn.setOnClickListener(new BtnOnClickListener());
		SetVoiceSpinnerContext();//初始化下拉菜单
		//BandingButton();
		
		InitVoiceSelect();
		app.SetTopActivityHandler(mHandler);
	}
	
	private class BtnOnClickListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			if (view.getId() == R.id.VoiceOptionBtn) {
				if(!GetRecordState()){
					SendGuardMessage("C"+ app.GetMtkaddr() + "CBFYY" + voicenum,Const.FAILBFYY);
					progressDialog =ProgressDialog.show(VoiceActivity.this, null, getString(R.string.all_activity_send));
				}else{
					Toast.makeText(VoiceActivity.this,getString(R.string.setting_activity_notallowed_voice), Toast.LENGTH_SHORT).show();
				}
			}
		}
		
	}
	
	private boolean GetRecordState(){
		boolean state=false;
		state=app.Getsharepre().GetSharePre().getBoolean(Const.KEY_MONITOR, false);
		return state;
	}
		
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Const.FAILBFYY:
				StopTimeOut(timeoutbj);
				Toast.makeText(VoiceActivity.this, R.string.all_activity_failsend, Toast.LENGTH_SHORT).show();
				Const.CloseDialog(progressDialog);
				break;
			case Const.RECVBFYY:
				StopTimeOut(timeoutbj);
				Toast.makeText(VoiceActivity.this, R.string.voice_activity_paly, Toast.LENGTH_SHORT).show();
				Const.CloseDialog(progressDialog);
				break;
			}
		}
	};
		
	private void StopTimeOut(TimeOutClass timeout) {
		if (timeout != null) {
			timeout.stopTimer();
			timeout = null;
		}
	}
	
	private void SendGuardMessage(String str,int msg) {
		timeoutbj=Const.SendMessageFunc(app, mHandler, str, msg);
	}
	
	private void SetVoiceSpinnerContext() {

		adapter = ArrayAdapter.createFromResource(this, R.array.voicearray, android.R.layout.simple_spinner_item);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		VoiceOptionSpinner.setAdapter(adapter);
		// 添加事件Spinner事件监听
		VoiceOptionSpinner.setOnItemSelectedListener(new SpinnerSelectedListener());
		// 设置默认值
		VoiceOptionSpinner.setVisibility(View.VISIBLE);
	}
	
	private void InitVoiceSelect(){
		int select= app.Getsharepre().GetSharePre().getInt(Const.KEY_VOICESELECT, 0);
		
		VoiceOptionSpinner.setSelection(select);
	}
	
	class SpinnerSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			voicenum=arg2;
			app.Getsharepre().EditPutInt(Const.KEY_VOICESELECT, voicenum);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}

	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
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
