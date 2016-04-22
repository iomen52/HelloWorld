package com.guanjiangjun2.util;

import com.example.guanjiangjun2.R;
import com.guanjiangjun2.receiver.HandleMessageReceiver;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.Log;
import android.view.WindowManager;

public class PopUpDialog {

	private Context context;
	private MyApplication app;

	public PopUpDialog(Context context, MyApplication app) {
		this.context = context;
		this.app = app;
	}
	/*
	public void PopUpGpsLatLngDialog(double dlon, double dlat) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setMessage(
				"GPS定位成功" + "\n" + "经度:" + dlon + "\n" + "纬度:" + dlat + "\n"
						+ "可点击地图显示防盗器当前位置")
				.setTitle("GPS定位结果")
				.setPositiveButton(context.getString(R.string.popup_dialog_know),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						});
		AlertDialog ad = builder.create();
		// ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
		// //系统中关机对话框就是这个属性
		ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		ad.setCanceledOnTouchOutside(false); // 点击外面区域不会让dialog消失
		ad.show();
	}
	*/
	
	// 报警提示框
	public AlertDialog PopUpAlertDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(context.getString(R.string.popup_dialog_guard_alarm))
				.setTitle(context.getString(R.string.popup_dialog_guard))
				.setPositiveButton(context.getString(R.string.popup_dialog_know),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (app.GetAlarmdlg() != null)
									app.SetAlarmdlg(null);
								Const.DeleteAlarmManager(Const.ALARMACTION,
										context, HandleMessageReceiver.class);
								app.StopPlayAlarmSound();
							}
						});
		AlertDialog ad = builder.create();
		// ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
		// ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);
		// //系统中关机对话框就是这个属性
		ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		ad.setCanceledOnTouchOutside(false); // 点击外面区域不会让dialog消失
		ad.show();

		return ad;
	}

	// 报警提示框
	public AlertDialog PopUpGeoFenceDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(context.getString(R.string.popup_dialog_geofence))
				.setTitle(context.getString(R.string.popup_dialog_geofence_alarm))
				.setPositiveButton(context.getString(R.string.popup_dialog_know),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (app.GetAlarmdlg() != null)
									app.SetAlarmdlg(null);
								Const.DeleteAlarmManager(Const.GEOFENCEACTION,
										context, HandleMessageReceiver.class);
								app.StopPlayAlarmSound();
								app.SetGeoFenceKnowMarker(true);
								GeofenceknowAsyncTask task=new GeofenceknowAsyncTask();
								task.execute(0);
							}
						});
		AlertDialog ad = builder.create();
		// ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
		// ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);
		// //系统中关机对话框就是这个属性
		ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		ad.setCanceledOnTouchOutside(false); // 点击外面区域不会让dialog消失
		ad.show();

		return ad;
	}
	
	/*
	// 录音提示框
	public void PopUpRecordDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("防盗器内没有装SD卡，请插入SD卡并重新开机进行监听录音工作")
				.setTitle("监听失败")
				.setPositiveButton(context.getString(R.string.popup_dialog_know),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						});
		AlertDialog ad = builder.create();
		// ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
		// //系统中关机对话框就是这个属性
		ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		ad.setCanceledOnTouchOutside(false); // 点击外面区域不会让dialog消失
		ad.show();
	}
	*/
	
	public AlertDialog PopUpOfflineDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(context.getString(R.string.popup_dialog_offline))
				.setTitle(context.getString(R.string.popup_dialog_offline_alarm))
				.setPositiveButton(context.getString(R.string.popup_dialog_know),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (app.GetOfflinedlg() != null)
									app.SetOfflinedlg(null);
							}
						});
		AlertDialog ad = builder.create();
		// ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
		// //系统中关机对话框就是这个属性
		ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		ad.setCanceledOnTouchOutside(false); // 点击外面区域不会让dialog消失
		ad.show();

		return ad;
	}

	private class GeofenceknowAsyncTask extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... arg0) {
			// TODO Auto-generated method stub
			Integer tmp = arg0[0].intValue();
			try {
				Thread.sleep(1000 * 60 * 3);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return tmp;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			app.SetGeoFenceKnowMarker(false);
			Log.e("PopUpDialog", "PopUpDialog SetGeoFenceKnowMarker");
		}
		
	}
	public void PlaySystemTone() {
		Uri notification = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone r = RingtoneManager.getRingtone(
				context.getApplicationContext(), notification);
		r.play();
	}

	// 震动
	public void SmsAlarmVibrator() {
		// 这里震动一秒钟,用来感受下刚开机是否马上收到开机消息,并启动Service
		((Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE))
				.vibrate(1500);
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		Log.i("ganjiangjun", "finalize");
	}
}
