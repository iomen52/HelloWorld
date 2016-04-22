package com.guanjiangjun2.activity;

import com.example.guanjiangjun2.R;
import com.guanjiangjun2.util.Const;
import com.guanjiangjun2.util.CustomDialog;
import com.guanjiangjun2.util.MyApplication;
import com.guanjiangjun2.util.PasswordDialog;
import com.guanjiangjun2.util.TimeOutClass;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity  extends Activity{

	private RelativeLayout ContentPhoneOne;
	private RelativeLayout ContentPhoneTwo;
	private RelativeLayout ContentOffPower;
	private RelativeLayout ContentSetAndroidPhone;
	private RelativeLayout ContentSetMtkPhone;
	private RelativeLayout ContentPassword;
	private RelativeLayout HeartQuantityRelative;//心跳频率设置
	private RelativeLayout ContentMobileBrand;//手机品牌切换
	private RelativeLayout ContentCancel;//清除登陆
	
	private TextView MobileBrandTextView;
	private TimeOutClass timeout;
	private ProgressDialog progressDialog;
	private MyApplication app;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_layout);
		app = (MyApplication) getApplication();
		ContentPhoneOne=(RelativeLayout)findViewById(R.id.ContentPhoneOne);
		ContentPhoneTwo=(RelativeLayout)findViewById(R.id.ContentPhoneTwo);
		ContentOffPower=(RelativeLayout)findViewById(R.id.ContentOffPower);
		ContentSetAndroidPhone=(RelativeLayout)findViewById(R.id.ContentSetAndroidPhone);
		ContentSetMtkPhone=(RelativeLayout)findViewById(R.id.ContentSetMtkPhone);
		ContentPassword=(RelativeLayout)findViewById(R.id.ContentPassword);
		HeartQuantityRelative=(RelativeLayout)findViewById(R.id.HeartQuantityRelative);
		ContentMobileBrand=(RelativeLayout)findViewById(R.id.ContentMobileBrand);
		ContentCancel=(RelativeLayout)findViewById(R.id.ContentCancel);
		MobileBrandTextView=(TextView)findViewById(R.id.MobileBrandTextView);		
		
		ContentPhoneOne.setOnClickListener(new RelativeLayoutOnClickListener());
		ContentPhoneTwo.setOnClickListener(new RelativeLayoutOnClickListener());
		ContentOffPower.setOnClickListener(new RelativeLayoutOnClickListener());
		ContentSetMtkPhone.setOnClickListener(new RelativeLayoutOnClickListener());
		ContentSetAndroidPhone.setOnClickListener(new RelativeLayoutOnClickListener());
		ContentPassword.setOnClickListener(new RelativeLayoutOnClickListener());
		HeartQuantityRelative.setOnClickListener(new RelativeLayoutOnClickListener());
		ContentMobileBrand.setOnClickListener(new RelativeLayoutOnClickListener());
		ContentCancel.setOnClickListener(new RelativeLayoutOnClickListener());
		
		app.SetTopActivityHandler(mHandler);
		
		InitContral();
	}
	
	private void GetDataFromXml(){
		//boolean bool=false;
		//bool=app.Getsharepre().GetSharePre().getBoolean(Const.STANDBYMODE, false);
		if(Const.GetStandbyMode(app)){
			ContentPhoneOne.setEnabled(false);
			ContentPhoneOne.setBackgroundResource(R.color.setting_background);
			ContentPhoneTwo.setEnabled(false);
			ContentPhoneTwo.setBackgroundResource(R.color.setting_background);
			HeartQuantityRelative.setEnabled(false);
			HeartQuantityRelative.setBackgroundResource(R.color.setting_background);
			ContentOffPower.setEnabled(false);
			ContentOffPower.setBackgroundResource(R.color.setting_background);
		}
	}
	
	private void InitContral(){
		boolean bool;
		bool=app.Getsharepre().GetSharePre().getBoolean(Const.MOBILEBRAND, false);
		if(bool)
			MobileBrandTextView.setText(getString(R.string.setting_activity_miui));
		else
			MobileBrandTextView.setText(getString(R.string.setting_activity_other));
	}
	
	private class RelativeLayoutOnClickListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			if (view.getId() == R.id.ContentPhoneOne) {
				CustomDialog.Builder builder = new CustomDialog.Builder(SettingActivity.this,1,new UpdateSettingPriorityListener());
				builder.create().show();
			}else if(view.getId() == R.id.ContentPhoneTwo){
				CustomDialog.Builder builder = new CustomDialog.Builder(SettingActivity.this,2,new UpdateSettingPriorityListener());
				builder.create().show();
			}else if(view.getId() == R.id.ContentOffPower){
				dialog();
			}
			else if(view.getId() == R.id.ContentSetMtkPhone){
				CustomDialog.Builder builder = new CustomDialog.Builder(SettingActivity.this,3,new UpdateSettingPriorityListener());
				builder.create().show();
			}
			else if(view.getId() == R.id.ContentSetAndroidPhone){
				CustomDialog.Builder builder = new CustomDialog.Builder(SettingActivity.this,4,new UpdateSettingPriorityListener());
				builder.create().show();
			}else if(view.getId() == R.id.ContentPassword){
				PasswordDialog.Builder builder = new PasswordDialog.Builder(SettingActivity.this,new UpdateSettingPriorityListener());
				builder.create().show();
			}else if(view.getId() == R.id.HeartQuantityRelative){
				CustomDialog.Builder builder = new CustomDialog.Builder(SettingActivity.this,5,new UpdateSettingPriorityListener());
				builder.create().show();
			}else if(view.getId() == R.id.ContentMobileBrand){
				AskPopupDialog();
			}else if(view.getId() == R.id.ContentCancel){
				CancelLoginPopupDialog();
			}
		}
		
	}

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case Const.SYSTEMREST:
					Toast.makeText(SettingActivity.this, "SYSTEMREST", Toast.LENGTH_SHORT).show();
					break;
				case Const.CancelLoginError:
				case Const.CancelLoginSuccess:
					HandleEvent(msg.what);
					break;
			}
		}
	};
	
	private void HandleEvent(int msg){
		Const.StopTimeOut(timeout);
		Const.CloseDialog(progressDialog);
		if(msg==Const.CancelLoginSuccess){
			Toast.makeText(SettingActivity.this, getString(R.string.setting_activity_logout_success), Toast.LENGTH_SHORT).show();
			app.Getbackground().StopAlarmManager();
			app.Getbackground().StopDeviceOpinion();
			app.SetMtkonline(false);
			String sql = "delete from device where imei='"+app.Getbackground().GetUsername()+"'";;
			Const.ExecDatabaseSqlite(this, sql);
			//GotoLogin();
			Const.GotoLogin(app, SettingActivity.this);
		}else if(msg==Const.CancelLoginSuccess){
			Toast.makeText(SettingActivity.this, getString(R.string.setting_activity_logout_fail), Toast.LENGTH_SHORT).show();
		}
	}
		
	private void CancelLoginPopupDialog(){
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage(getString(R.string.setting_activity_logout_is));
		builder.setTitle(getString(R.string.setting_activity_logout));
		builder.setPositiveButton(getString(R.string.setting_activity_logout), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				SendMessage("O"+app.Getbackground().GetUsername()+","+Const.GetImei(app, SettingActivity.this), Const.CancelLoginError);
				progressDialog =ProgressDialog.show(SettingActivity.this, null, SettingActivity.this.getString(R.string.all_activity_send));
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(getString(R.string.dialog_phone_cancel), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	private void ShutdownOrResetAsync() {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				app.SetShutdownAsync(false);
				//ExecuteRece();
			}
		}, 1000 * 15);
	}
	
	private void SendMessage(String str,int msg) {
		timeout=Const.SendMessageFunc(app, mHandler, str, msg);
	}
		
	private void AskPopupDialog(){
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage(getString(R.string.login_activity_make_sure));
		builder.setTitle(getString(R.string.login_activity_make_sure));
		builder.setPositiveButton(getString(R.string.login_activity_miui), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				app.Getsharepre().EditPutBoolean(Const.MOBILEBRAND, true);
				ResetBackService(true);
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(getString(R.string.login_activity_other), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				app.Getsharepre().EditPutBoolean(Const.MOBILEBRAND, false);
				ResetBackService(false);
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	
	private void ResetBackService(boolean bool){
		app.Getbackground().GetInputThread().SetMobilebrand(bool);
		app.Getbackground().SetMobileBrand(bool);
		if(bool)
			MobileBrandTextView.setText(getString(R.string.setting_activity_miui));
		else
			MobileBrandTextView.setText(getString(R.string.setting_activity_other));
		app.Getbackground().StopAlarmManager();
		app.Getbackground().StartAlarmManager();
	}
	
	private void dialog() {
		AlertDialog.Builder builder = new Builder(SettingActivity.this);
		builder.setMessage(getString(R.string.setting_activity_shutdown));
		builder.setTitle(getString(R.string.setting_activity_hint));
		builder.setPositiveButton(getString(R.string.dialog_phone_ok), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				SendOffPower();
				app.SetShutdownAsync(true);
				app.SetMtkonline(false);
				ShutdownOrResetAsync();
				app.Getbackground().StopDeviceOpinion();
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(getString(R.string.dialog_phone_cancel), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	
	private void SendOffPower(){
		Toast.makeText(SettingActivity.this, "shutdown", Toast.LENGTH_SHORT).show();
		Const.SendMessageFunc(app, null, "C" + app.GetMtkaddr() + "CSHUTDOWN", 0);
	}
	
	private class UpdateSettingPriorityListener implements PriorityListener{
		@Override
		public void SetActivityHandle() {
			// TODO Auto-generated method stub
			app.SetTopActivityHandler(mHandler);
		}
	}
	public interface PriorityListener {
        public void SetActivityHandle();
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
		app.SetTopActivityHandler(null);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		app.SetTopActivityHandler(mHandler);
		//GetDataFromXml();
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

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.e("onResume", "onResume SettingActivity");
	}

}
