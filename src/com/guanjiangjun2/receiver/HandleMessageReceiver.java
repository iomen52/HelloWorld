package com.guanjiangjun2.receiver;

import com.guanjiangjun2.activity.MainActivity;
import com.guanjiangjun2.util.Const;
import com.guanjiangjun2.util.MyApplication;
import com.guanjiangjun2.util.PopUpDialog;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

public class HandleMessageReceiver extends BroadcastReceiver {
	/* 回调接口 */
	private Context context;
	private PopUpDialog dlg = null;
	private final static String TAG = "BootUpSMSReceiver";
	private MyApplication app;
	private int curmode = 0;// 1:防盗报警2:录音无卡3:离线4:围栏报警

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		this.context = context;
		app = (MyApplication) context.getApplicationContext();
		if (!GetPowerStatus())// 判断是否屏幕状态
			LcdAndUnlock();
		// 监听服务发来广播
		if (intent.getAction().equals(Const.ALARMACTION)) {
			curmode = 1;
		} else if (intent.getAction().equals(Const.NOCARTACTION)) {
			curmode = 2;
		} else if (intent.getAction().equals(Const.MTKOFFLINE)) {
			curmode = 3;
		} else if (intent.getAction().equals(Const.GEOFENCEACTION)) {
			curmode = 4;
		}
		StartMainActivity();
	}

	private void StartMainActivity() {// 如果主界面在后台则前台显示

		if (Const.isTopActivity("com.example.guanjiangjun2", context)) {// 主动显示主界面
			ExecuteRece();
		} else {
			Intent it = new Intent();
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			it.setClass(context, MainActivity.class);
			context.startActivity(it);

			ActivityAsync();
		}
	}

	private void ExecuteGeoFence() {

		if (app.GetGeoFenceMarker()) {
			Log.e(TAG, "ExecuteGeoFence ALARM =" + app.GetGeoFenceMarker()
					+ ":::" + app.GetGeoFenceKnowMarker());
			return;
		}
		if (app.GetGeofencedlg() != null) {
			app.StopPlayAlarmSound();
			app.GetGeofencedlg().dismiss();
			app.SetGeofencedlg(null);
		}
		// Const.AddLoopAlarmManager(Const.GEOFENCEACTION, context,
		// HandleMessageReceiver.class, 1000 * 60 * 5);

		if (app.GetGeofencedlg() == null) {
			dlg = new PopUpDialog(context, app);
			app.SetGeofencedlg(dlg.PopUpGeoFenceDialog());
		}
		app.PlaySounds();
		app.SetGeoFenceMarker(true);

		SetMarkerFalse();
	}

	private void ExecuteReceAlarm() {
		Log.e(TAG, "ExecuteReceAlarm ALARM Marker=" + app.GetAlarmMarker());
		if (app.GetAlarmMarker() == true)
			return;
		if (app.GetAlarmdlg() != null) {
			app.StopPlayAlarmSound();
			app.GetAlarmdlg().dismiss();
			app.SetAlarmdlg(null);
		}
		Const.AddLoopAlarmManager(Const.ALARMACTION, context,
				HandleMessageReceiver.class, 1000 * 60 * 5);
		if (app.GetAlarmdlg() == null) {
			dlg = new PopUpDialog(context, app);
			app.SetAlarmdlg(dlg.PopUpAlertDialog());
		}
		app.PlaySounds();
		app.SetAlarmMarker(true);
		SetMarkerFalse();
	}
	
	private void ExecuteReceNocard() {
		if (app.GetNocardMarker() == true)
			return;

		//dlg.PopUpRecordDialog();
		dlg.PlaySystemTone();
		dlg.SmsAlarmVibrator();
		app.SetNocardMarker(true);

		SetMarkerFalse();
	}

	private void ExecuteReceOffline() {
		
		dlg = new PopUpDialog(context, app);
		if (app.GetOfflinedlg() == null) {
			app.SetOfflinedlg(dlg.PopUpOfflineDialog());
		}
		dlg.PlaySystemTone();
		dlg.SmsAlarmVibrator();
		app.SetMtkonline(false);

		SetMarkerFalse();

		Const.SendMessageToActivity(app,Const.MTKNOONLINE);
		
		if (app.Getbackground() != null)
			if (app.Getbackground().GetOpinionFlags())
				app.Getbackground().StopDeviceOpinion();
	}

	private void ExecuteRece() {
		// 1:防盗报警2:录音无卡3:离线4:围栏报警
		if (curmode == 1)
			ExecuteReceAlarm();
		else if (curmode == 2)
			ExecuteReceNocard();
		else if (curmode == 3)
			ExecuteReceOffline();
		else if (curmode == 4) {
			//if (app.GetLoginState()) {
				ExecuteGeoFence();
			//}
		}
	}

	private void ActivityAsync() {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				ExecuteRece();
			}
		}, 1000 * 1);
	}

	private void SetMarkerFalse() {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 1:防盗报警2:录音无卡3:离线4:围栏报警
				if (curmode == 1)
					app.SetAlarmMarker(false);
				else if (curmode == 2)
					app.SetNocardMarker(false);
				else if (curmode == 3)
					app.SetMtkonline(false);
				else if (curmode == 4)
					app.SetGeoFenceMarker(false);
			}
		}, 1000 * 15);
	}
	
	// 判断屏幕是否休眠
	public boolean GetPowerStatus() {
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		boolean screen = pm.isScreenOn();
		return screen;
	}

	// 亮屏、解锁接口
	@SuppressWarnings("deprecation")
	public void LcdAndUnlock() {

		// 获取电源管理器对象
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		// 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的MyApplication.TAG
		@SuppressWarnings("deprecation")
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.ACQUIRE_CAUSES_WAKEUP
						| PowerManager.SCREEN_DIM_WAKE_LOCK, "Simple Timer");
		// 点亮屏幕
		wl.acquire();
		// 释放
		wl.release();

		// 得到键盘锁管理器对象
		KeyguardManager km = (KeyguardManager) context
				.getSystemService(Context.KEYGUARD_SERVICE);
		// 参数是LogCat里用的MyApplication.TAG
		@SuppressWarnings("deprecation")
		KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
		// 解锁
		kl.disableKeyguard();
	}

}
