package com.guanjiangjun2.activity;


import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.guanjiangjun2.R;
import com.guanjiangjun2.service.BackgroundServer;
import com.guanjiangjun2.util.Const;
import com.guanjiangjun2.util.MyApplication;
import com.guanjiangjun2.util.ShowNotification;
import com.guanjiangjun2.util.TimeOutClass;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity  extends Activity{
	
	private MyApplication app;
	private TextView TextViewTitle;
	private ImageButton ImageViewTracking;//实时定位
	private ImageButton ImageViewAlarm;//防盗报警
	private ImageButton ImageViewMessage;//一键监听
	private ImageButton ImageViewNavi;//设备录音
	private ImageButton ImageViewOfflinemap;//播放语音
	private ImageButton ImageViewCommand;//系统设置
	private ImageButton ImageViewElectronic;//电子围栏
	private ImageButton ImageViewHistory;//历史记录
	private ImageButton OfflineMapImageBtn;//离线地图
	private ImageButton StandbyImageBtn;//待机省电
	private ImageButton MoreImageBtn;//一对多设置
	private RelativeLayout ContentTitle;//标题栏点击
	private TimeOutClass timeout;
	private ProgressDialog progressDialog;
	private long standbytime;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		app = (MyApplication) getApplication();
		
		TextViewTitle=(TextView)findViewById(R.id.TextViewTitle);
		ImageViewTracking=(ImageButton)findViewById(R.id.ImageViewTracking);
		ImageViewAlarm=(ImageButton)findViewById(R.id.ImageViewAlarm);
		ImageViewMessage=(ImageButton)findViewById(R.id.ImageViewMessage);
		ImageViewNavi=(ImageButton)findViewById(R.id.ImageViewNavi);
		ImageViewOfflinemap=(ImageButton)findViewById(R.id.ImageViewOfflinemap);
		ImageViewCommand=(ImageButton)findViewById(R.id.ImageViewCommand);
		ImageViewElectronic=(ImageButton)findViewById(R.id.ImageViewElectronic);
		ImageViewHistory=(ImageButton)findViewById(R.id.ImageViewHistory);
		OfflineMapImageBtn=(ImageButton)findViewById(R.id.OfflineMapImageBtn);
		StandbyImageBtn=(ImageButton)findViewById(R.id.StandbyImageBtn);
		MoreImageBtn=(ImageButton)findViewById(R.id.MoreImageBtn);
		ContentTitle=(RelativeLayout)findViewById(R.id.ContentTitle);
		
		ImageViewTracking.setOnClickListener(new BtnOnClickListener());
		ImageViewAlarm.setOnClickListener(new BtnOnClickListener());
		ImageViewMessage.setOnClickListener(new BtnOnClickListener());
		ImageViewNavi.setOnClickListener(new BtnOnClickListener());
		ImageViewOfflinemap.setOnClickListener(new BtnOnClickListener());
		ImageViewCommand.setOnClickListener(new BtnOnClickListener());
		ImageViewElectronic.setOnClickListener(new BtnOnClickListener());
		ImageViewHistory.setOnClickListener(new BtnOnClickListener());
		OfflineMapImageBtn.setOnClickListener(new BtnOnClickListener());
		ContentTitle.setOnClickListener(new BtnOnClickListener());
		StandbyImageBtn.setOnClickListener(new BtnOnClickListener());
		MoreImageBtn.setOnClickListener(new BtnOnClickListener());
		
		app.SetTopActivityHandler(mHandler);
	}
	
	private class BtnOnClickListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			if (view.getId() == R.id.ImageViewTracking) {
				GotoOtherActivity(GpsLoctionActivity.class);
			}else if(view.getId() == R.id.ImageViewAlarm){
				GotoOtherActivity(GuardActivity.class);
			}else if(view.getId() == R.id.ImageViewMessage){
				String mslavephone;
				mslavephone=app.Getsharepre().GetSharePre().getString(Const.KEY_SETMTKPHONE, "");
				Toast.makeText(MainActivity.this, mslavephone,Toast.LENGTH_SHORT).show();
				if (!("".equals(mslavephone))) {
					Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mslavephone));
					startActivity(intent);
				} else {
					Toast.makeText(MainActivity.this, getString(R.string.main_activity_noset_mtkphone),Toast.LENGTH_SHORT).show();
				}
			}else if(view.getId() == R.id.ImageViewNavi){
				GotoOtherActivity(RecordActivity.class);
			}else if(view.getId() == R.id.ImageViewOfflinemap){
				GotoOtherActivity(VoiceActivity.class);
			}else if(view.getId() == R.id.ImageViewCommand){
				GotoOtherActivity(SettingActivity.class);
			}else if(view.getId() == R.id.ContentTitle){
				if(!Const.GetStandbyMode(app)){
					if(!app.GetMtkonline() && Const.isNetworkAvailable(MainActivity.this)){//不在线 并且 没有正在连接
						TextViewTitle.setText(R.string.current_state_connecting);
						SetButtonEnable(false);
						app.Getbackground().ConnectMtk();
					}
				}
			}else if(view.getId() == R.id.ImageViewElectronic){
				GotoOtherActivity(GeofenceActivity.class);
			}else if(view.getId() == R.id.ImageViewHistory){
				GotoOtherActivity(HistoryActivity.class);
			}else if(view.getId() == R.id.OfflineMapImageBtn){
				GotoOtherActivity(OfflineMapActivity.class);
			}else if(view.getId() == R.id.StandbyImageBtn){
				if("".equals(GetMtkPhone())){
					Toast.makeText(MainActivity.this, getString(R.string.main_activity_noset_mtkphone),Toast.LENGTH_SHORT).show();
				}else{
					
					if(Const.GetStandbyMode(app))
						WorkPopupDialog();
					else
						StandbySmsPopupDialog();
				}
				
			}else if(view.getId() == R.id.MoreImageBtn){
				//GotoOtherActivity(MoreDeviceActivity.class);	
				//Toast.makeText(MainActivity.this, "打开一对多",Toast.LENGTH_SHORT).show();
			}
		}
		
	}

	
	private void SwitchStandbyMode(){//网络待机命令发送成功后
		//app.Getsharepre().EditPutBoolean(Const.STANDBYMODE, true);
		Const.SetStandbyMode(app, true);
		SetButtonEnable(false);
		SetStandbyMarker();
		SetStandbyEnable(false);
		app.SetShutdownAsync(true);
		TextViewTitle.setText(getString(R.string.main_activity_battery_saving));
		//app.SetTopActivityHandler(null);
		app.Getsharepre().EditPutBoolean(Const.KEY_MONITOR,false);
		app.SetMtkonline(false);
		app.Getbackground().StopDeviceOpinion();
		app.Getbackground().StopAlarmManager();
		app.SetMtkaddr("");
	}
	
	private void StandbySmsPopupDialog(){
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage(getString(R.string.main_activity_standby_dialog));
		builder.setTitle(getString(R.string.main_activity_battery_saving));
		builder.setPositiveButton(getString(R.string.dialog_phone_ok), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				standbytime=GetCurTime();
				Log.e("onClick", "onClick + "+standbytime);
				app.Getsharepre().EditPutString(Const.STANDBYTIME, standbytime+"");
				SendStandbyMessage("C" + app.GetMtkaddr() + "CSTANDBYMODE"+","+standbytime, Const.FAILSTANDBY);
				Const.SaveOperationInfo("NET","CSTANDBYMODE"+"_"+standbytime);
				progressDialog =ProgressDialog.show(MainActivity.this, null, getString(R.string.all_activity_send));
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
	
	private long GetCurTime(){
		
		long longcurtime;
		Date date = new java.util.Date();
		longcurtime = date.getTime();
		return longcurtime;
	}
	
	private void SendStandbyMessage(String str,int msg) {
		timeout=Const.SendMessageFunc(app, mHandler, str, msg);
	}
	
	private void GetWorkModeMaker(){
		standbytime=Long.valueOf(app.Getsharepre().GetSharePre().getString(Const.STANDBYTIME, "").trim()).longValue();
	}
	
	private void WorkPopupDialog(){
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage(getString(R.string.main_activity_work_dialog));
		builder.setTitle(getString(R.string.main_activity_work_title));
		builder.setPositiveButton(getString(R.string.dialog_phone_ok), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				GetWorkModeMaker();
				//app.Getsharepre().EditPutBoolean(Const.STANDBYMODE, false);
				Const.SetStandbyMode(app, false);
				app.SetTopActivityHandler(mHandler);
				SendSms(false);
				MainActivityAsync();
				app.Getbackground().ConnectMtk();
				TextViewTitle.setText(getString(R.string.main_activity_work_on));
				app.Getbackground().StartAlarmManager();
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
	
	private void SmsSetStandbyMode(){
		
		SendSms(true);
		SwitchStandbyMode();//命令发送成功后设置
		
	}
	
	private void SetStandbyMarker(){
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				app.SetShutdownAsync(false);
				if(StandbyImageBtn!=null){
					SetStandbyEnable(true);
				}
			}
		}, 1000 * 40);
	}
	
	private void MainActivityAsync() {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(TextViewTitle!=null)
					TextViewTitle.setText(R.string.current_state_connecting);
				app.Getbackground().ConnectMtk();
			}
		}, 1000 * 10);
	}
	
	private void SendSms(boolean bool){
		SmsManager sms = SmsManager.getDefault();
		if(bool){
			sms.sendTextMessage(GetMtkPhone(), null, "STANDBYMODE"+","+standbytime, null, null);
			Const.SaveOperationInfo("SMS","STANDBYMODE"+"_"+standbytime);
		}else{
			sms.sendTextMessage(GetMtkPhone(), null, "WORKMODE"+","+standbytime, null, null);
			Const.SaveOperationInfo("SMS","WORKMODE"+"_"+standbytime);
		}
	}
	
	private String GetMtkPhone(){
		String mslavephone;
		mslavephone=app.Getsharepre().GetSharePre().getString(Const.KEY_SETMTKPHONE, "");
		return mslavephone;
	}
	
	private void GotoOtherActivity(Class<?> cls){
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, cls);
		MainActivity.this.startActivity(intent);
	}
	
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Const.MTKONLINE:// 防盗器在线
				TextViewTitle.setText(R.string.current_state_online);
				Log.e("MainActivity", "MainActivity = MTKONLINE");
				SetButtonEnable(true);
				break;
			case Const.MTKNOONLINE:// 防盗器不在线
				if(!app.GetMtkonline()){
					TextViewTitle.setText(R.string.current_state_noonline);
					SetButtonEnable(false);
				}
				break;
			case Const.UPDATESIGNALLEVEL:// 更新信号强度
				SetButtonEnable(true);
				//app.Getsharepre().EditPutBoolean(Const.STANDBYMODE, false);
				Const.SetStandbyMode(app, false);
				String tmp = null;
				if(msg.getData().getInt("batterylevel")<=4)
					tmp=getString(R.string.main_activity_low);
				else if(msg.getData().getInt("batterylevel")==5)
					tmp=getString(R.string.main_activity_middle);
				else if(msg.getData().getInt("batterylevel")==6)
					tmp=getString(R.string.main_activity_high);
				if(msg.getData().getInt("chargestate")==1){
					TextViewTitle.setText(getString(R.string.current_state_signal) 
							+ msg.getData().getInt("signallevel")
							+getString(R.string.current_state_charging)
							+tmp);
				}else{
					TextViewTitle.setText(getString(R.string.current_state_signal) 
							+ msg.getData().getInt("signallevel")
							+getString(R.string.current_state_battery)
							+tmp);
				}
				break;
			case Const.INTERNETON:// 联网
				TextViewTitle.setText(R.string.current_state_connecting);
				SetButtonEnable(false);
				break;
			case Const.INTERNETOFF:// 断网
				TextViewTitle.setText(R.string.current_state_nointernet);
				SetButtonEnable(false);
				break;
			case Const.INTERNETCHANGE:
				TextViewTitle.setText(R.string.current_state_switchinternet);
				break;
			case Const.RECVSTANDBY:
				Const.StopTimeOut(timeout);
				Const.CloseDialog(progressDialog);
				SwitchStandbyMode();
				Toast.makeText(MainActivity.this, getString(R.string.main_activity_standby_mode), Toast.LENGTH_SHORT).show();
				break;
			case Const.FAILSTANDBY:
				Const.StopTimeOut(timeout);
				Const.CloseDialog(progressDialog);
				Toast.makeText(MainActivity.this, getString(R.string.main_activity_standby_fail), Toast.LENGTH_SHORT).show();
				SmsSetStandbyMode();
				break;
			}
		}
	};
	
	
	private void RestartActivity() {
		if (app.Getbackground() == null)
			Const.StartSocketService(this);

		if (!Const.GetStandbyMode(app)) {
			if (Const.isNetworkAvailable(this) && !app.GetMtkonline()) {
				TextViewTitle.setText(R.string.current_state_connecting);
				SetButtonEnable(false);
				if (app.Getbackground() != null)
					app.Getbackground().ConnectMtk();
			}
			if (!Const.isNetworkAvailable(this)) {
				TextViewTitle.setText(R.string.current_state_nointernet);
				SetButtonEnable(false);
			}
			if (Const.isNetworkAvailable(this) && app.GetMtkonline()) {
				if(app.GetVoltage()==0)
					TextViewTitle.setText(R.string.current_state_online);
				else{
					String tmp = null;
					if(app.GetVoltage()<=4)//1、2、3、4
						tmp=getString(R.string.main_activity_low);
					else if(app.GetVoltage()==5)
						tmp=getString(R.string.main_activity_middle);
					else if(app.GetVoltage()==6)
						tmp=getString(R.string.main_activity_high);
					
					if(app.GetCharging()==1){
						TextViewTitle.setText(getString(R.string.current_state_signal)
								+app.GetSignal()
								+getString(R.string.current_state_charging)
								+tmp);
					}else{
						TextViewTitle.setText(getString(R.string.current_state_signal)+app.GetSignal()+getString(R.string.current_state_battery)+tmp);
					}
				}
				SetButtonEnable(true);
			}
		} else {
			TextViewTitle.setText(getString(R.string.main_activity_battery_saving));
			SetButtonEnable(false);
		}
	}
	
	private void SetStandbyEnable(boolean bool){
		StandbyImageBtn.setEnabled(bool);
		if(bool){
			StandbyImageBtn.getBackground().setAlpha(255);
		}else{
			StandbyImageBtn.getBackground().setAlpha(100);
		}
	}
	
	private void SetButtonEnable(boolean bool){
		ImageViewTracking.setEnabled(bool);
		ImageViewAlarm.setEnabled(bool);
		ImageViewNavi.setEnabled(bool);
		ImageViewOfflinemap.setEnabled(bool);
		ImageViewCommand.setEnabled(bool);
		//StandbyImageBtn.setEnabled(bool);
		if(bool){
			ImageViewTracking.getBackground().setAlpha(255);
			ImageViewAlarm.getBackground().setAlpha(255);
			ImageViewNavi.getBackground().setAlpha(255);
			ImageViewOfflinemap.getBackground().setAlpha(255);
			ImageViewCommand.getBackground().setAlpha(255);
			//StandbyImageBtn.getBackground().setAlpha(255);
		}else{
			ImageViewTracking.getBackground().setAlpha(100);
			ImageViewAlarm.getBackground().setAlpha(100);
			ImageViewNavi.getBackground().setAlpha(100);
			ImageViewOfflinemap.getBackground().setAlpha(100);
			ImageViewCommand.getBackground().setAlpha(100);
			//StandbyImageBtn.getBackground().setAlpha(100);
		}
	}
	
	private void StartService() {
		//启动后台处理服务
		if (!Const.ServiceIsWorking("com.example.guanjiangjun.BackgroundServer",this)) {
			
			Intent it = new Intent(MainActivity.this, BackgroundServer.class);
			MainActivity.this.startService(it);
			
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
		app.SetTopActivityHandler(null);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		StartService();
		app.SetTopActivityHandler(mHandler);
		RestartActivity();
		
		NotificationManager notificationManager = (NotificationManager) this  
                .getSystemService(NOTIFICATION_SERVICE);  
		notificationManager.cancel(6789);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (!Const.isTopActivity("com.example.guanjiangjun2", this)) {// 主动显示主界面
			ShowNotification not=new ShowNotification(this);
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addCategory(Intent.CATEGORY_HOME);
		startActivity(i);
	}

}
