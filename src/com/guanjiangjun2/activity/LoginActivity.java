package com.guanjiangjun2.activity;

import com.example.guanjiangjun2.R;
import com.guanjiangjun2.util.Const;
import com.guanjiangjun2.util.MyApplication;
import com.guanjiangjun2.util.TimeOutClass;
import com.scan.zxing.activity.CaptureActivity;

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
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class LoginActivity extends Activity {

	private Button LoginBtn;
	private ImageView imageView2;
	private EditText UserNameEdit;
	private EditText PassWordEdit;
	private CheckBox RadioRememberBtn;
	private CheckBox RadioLoginBtn;
	private ProgressDialog progressDialog;
	private String username="";
	private String password="";
	private boolean savepassword = false;
	private boolean autologin = false;
	private boolean savemarker = false;
	private boolean automarker = false;
	private TimeOutClass timeout;
	private MyApplication app;
	private final static String TAG = "LoginActivity";
	//private static final int SCAN_CODE = 1;
	private static final int REQUEST_CODE = 100;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_layout);
		app = (MyApplication) getApplication();

		UserNameEdit = (EditText) findViewById(R.id.UserNameEdit);
		PassWordEdit = (EditText) findViewById(R.id.PassWordEdit);
		LoginBtn = (Button) findViewById(R.id.LoginBtn);
		RadioRememberBtn = (CheckBox) findViewById(R.id.RadioRememberBtn);
		RadioLoginBtn = (CheckBox) findViewById(R.id.RadioLoginBtn);
		imageView2 = (ImageView) findViewById(R.id.imageView2);
		
		RadioRememberBtn
				.setOnCheckedChangeListener(new RadioButtonOnCheckedChangeListener());
		RadioLoginBtn
				.setOnCheckedChangeListener(new RadioButtonOnCheckedChangeListener());
		
		imageView2.setOnClickListener(new BtnOnClickListener());
		LoginBtn.setOnClickListener(new BtnOnClickListener());
		GetDataFromXml();
		//BindingButton();
		
		app.SetTopActivityHandler(mHandler);

		ForTheFirstTimeLogin();
	}

	private void ForTheFirstTimeLogin(){
		boolean bool=true;
		bool = app.Getsharepre().GetSharePre().getBoolean(Const.FIRSTLOGIN, true);
		if(bool){
			app.Getsharepre().EditPutBoolean(Const.FIRSTLOGIN, false);
			AskPopupDialog();
		}else{
			Const.StartSocketService(this);
		}
	}
	
	private void AskPopupDialog(){
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage(getString(R.string.login_activity_phone_model));
		builder.setTitle(getString(R.string.login_activity_make_sure));
		builder.setPositiveButton(getString(R.string.login_activity_miui), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				app.Getsharepre().EditPutBoolean(Const.MOBILEBRAND, true);
				Const.StartSocketService(LoginActivity.this);
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(getString(R.string.login_activity_other), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				app.Getsharepre().EditPutBoolean(Const.MOBILEBRAND, false);
				Const.StartSocketService(LoginActivity.this);
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	
	private class BtnOnClickListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			if (view.getId() == R.id.imageView2) {
				/*
				Intent intent = new Intent();
				intent.setClass(LoginActivity.this, CaptureActivity.class);
				startActivityForResult(intent, REQUEST_CODE);
				*/
				Intent intent = new Intent();
				intent.setClass(LoginActivity.this, CaptureActivity.class);
				startActivityForResult(intent, REQUEST_CODE);
			}else if (view.getId() == R.id.LoginBtn) {
				String sql;
				username = UserNameEdit.getText().toString();
				password = PassWordEdit.getText().toString();
				if (username.equals("") || password.equals("")) {
					Toast.makeText(LoginActivity.this, R.string.login_button_passwordempty,Toast.LENGTH_LONG).show();
				} else {
					SetControlEnable(false);
					progressDialog=ProgressDialog.show(LoginActivity.this, null, getString(R.string.login_button_send));
					progressDialog.show();
					
					sql = "B" + username + "." + password+","+Const.GetImei(app, LoginActivity.this);
					SendLoginMessage(sql,Const.LOGINTIMEOUT);
					Log.e(TAG, "LoginBtn "+sql);
				}
			}
		}
		
	}
	
	private void AutoLoginFunc() {
		if (autologin && !("".equals(password))) {
			String sql;
			sql = "B" + username + "." + password+","+Const.GetImei(app, this);
			SendLoginMessage(sql,Const.LOGINTIMEOUT);
			progressDialog=ProgressDialog.show(LoginActivity.this, null, getString(R.string.login_button_send));
			progressDialog.show();
		}
	}

	class RadioButtonOnCheckedChangeListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton btn, boolean bool) {
			// TODO Auto-generated method stub
			if (btn.getId() == R.id.RadioRememberBtn) {
				if (savemarker)
					return;
				savepassword = bool;
			} else if (btn.getId() == R.id.RadioLoginBtn) {
				if (automarker)
					return;
				autologin = bool;
				app.Getsharepre().EditPutBoolean(Const.AUTOLOGIN, autologin);
			}
		}

	}

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Const.LOGINSUESS:// 登录成功
				Const.StopTimeOut(timeout);
				//app.SetLoginState(true);
				InsertToSqllite();
				SaveUserNameToXml();// 保存用户名密码
				Const.CloseDialog(progressDialog);
				app.Getbackground().SetUsername(username);
				ToMainActivity();
				break;
			case Const.LOGINERROR:// 登录失败
				Const.StopTimeOut(timeout);
				Const.CloseDialog(progressDialog);
				Toast.makeText(LoginActivity.this, R.string.login_button_passworderror,Toast.LENGTH_SHORT).show();
				SetControlEnable(true);
				break;
			case Const.LOGINTIMEOUT:
				Const.StopTimeOut(timeout);
				Const.CloseDialog(progressDialog);
				Toast.makeText(LoginActivity.this, R.string.login_button_fail,
						Toast.LENGTH_SHORT).show();
				SetControlEnable(true);
				break;
			case Const.RepeatLogin:
				Const.StopTimeOut(timeout);
				Const.CloseDialog(progressDialog);
				Toast.makeText(LoginActivity.this, getString(R.string.login_activity_repeat_login),Toast.LENGTH_SHORT).show();
				SetControlEnable(true);
				break;
			case Const.INTERNETON:// 联网
				SetControlEnable(true);
				break;
			case Const.INTERNETOFF:// 断网
				SetControlEnable(false);
				break;	
			case Const.INTERNETCHANGE:
				SetControlEnable(true);
				break;
			default:

				break;
			}
			super.handleMessage(msg);
		}
	};
	
	private void InsertToSqllite(){
		String sql="select * from device where imei='"+username+"'";
		if(Const.GetCountFromSqlite(this, sql)==0){
			Const.InsertDeviceInfo(this,username, username, password);
		}
		Log.e("InsertToSqllite", "InsertToSqllite "+Const.GetCountFromSqlite(this, sql));
	}

	private void SaveUserNameToXml() {
		app.Getsharepre().EditPutString(Const.USERNAME, username);
		if (savepassword)
			app.Getsharepre().EditPutString(Const.PASSWORD, password);
		app.Getsharepre().EditPutBoolean(Const.SAVEPASSWORD, savepassword);
	}

	private void GetDataFromXml() {
		username = app.Getsharepre().GetSharePre()
				.getString(Const.USERNAME, "");
		password = app.Getsharepre().GetSharePre()
				.getString(Const.PASSWORD, "");
		savepassword = app.Getsharepre().GetSharePre()
				.getBoolean(Const.SAVEPASSWORD, false);
		autologin = app.Getsharepre().GetSharePre()
				.getBoolean(Const.AUTOLOGIN, false);
		if (username != null) {
			UserNameEdit.setText(username);
		}
		if (savepassword) {
			PassWordEdit.setText(password);
			savemarker = true;
			RadioRememberBtn.setChecked(true);
			savemarker = false;
		} else {
			if (password != null) {
				app.Getsharepre().EditPutString(Const.PASSWORD, "");
				password = null;
			}
		}
		if (autologin) {
			automarker = true;
			RadioLoginBtn.setChecked(true);
			automarker = false;
		}
	}

	private void ToMainActivity() {

		 app.SetTopActivityHandler(null); 
		 Intent intent=new Intent();
		 intent.setClass(LoginActivity.this, MainActivity.class);
		 LoginActivity.this.startActivity(intent);
		 LoginActivity.this.finish();

	}

	private void SetControlEnable(boolean bool) {
		UserNameEdit.setEnabled(bool);
		PassWordEdit.setEnabled(bool);
		LoginBtn.setEnabled(bool);
	}

	private void SendLoginMessage(String str,int msg) {

		timeout=Const.SendMessageFunc(app, mHandler, str,Const.LOGINTIMEOUT);
		
	}

	private void RestartActivity() {
		if (!Const.isNetworkAvailable(LoginActivity.this)) {
			Toast.makeText(this, getString(R.string.current_state_nointernet), Toast.LENGTH_SHORT).show();
			SetControlEnable(false);
		} else {
			AutoLoginFunc();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
		RestartActivity();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// 获取数据
				String result = data.getStringExtra("result");
				Log.e(TAG, "onActivityResult ="+result);
				// 显示扫描到的内容
				UserNameEdit.setText(result);
			}
		}
		
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}

}
