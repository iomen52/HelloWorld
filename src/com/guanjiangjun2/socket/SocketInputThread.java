package com.guanjiangjun2.socket;

import java.io.IOException;
import java.io.OptionalDataException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.guanjiangjun2.activity.GpsLoctionActivity;
import com.guanjiangjun2.util.Const;
import com.guanjiangjun2.util.MyApplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class SocketInputThread extends Thread {

	private boolean isStart = true;
	private DatagramSocket mSocket;
	private Context context;
	private boolean gpsdatabasemarker = false;
	private boolean lbsdatabasemarker = false;
	private MyApplication app;
	private final static String TAG = "InputThread";
	private boolean mobilebrand = false;// false:标准android true:小米android

	public SocketInputThread(DatagramSocket socket, Context context) {
		this.mSocket = socket;
		this.context = context;
		app = (MyApplication) context.getApplicationContext();
		mobilebrand = app.Getbackground().GetMobileBrand();
		this.setName(TAG);
	}

	public void setStart() {
		this.isStart = true;
	}

	public void setStop() {
		this.isStart = false;
	}

	public void SetMobilebrand(boolean bool) {
		mobilebrand = bool;
	}

	public void NotifyInputThread() {
		notify();
	}

	public void WaitInputThread() {
		try {
			wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (isStart) {
			byte data[] = new byte[64];
			String str;
			DatagramPacket pack = new DatagramPacket(data, 64);
			try {

				mSocket.receive(pack);
				str = new String(data);
				Log.e(TAG, "callback=" + str);
				if (!app.GetShutdownAsync()) {
					if (str.indexOf(Const.LOGINTRUE) >= 0) {
						// 登陆成功
						Const.SendMessageToActivity(app, Const.LOGINSUESS);
					}
					if (str.indexOf(Const.STANDBYMODE) >= 0) {
						// 待机模式
						Const.SendMessageToActivity(app, Const.RECVSTANDBY);
					}
					if (str.indexOf(Const.LOGINFALSE) >= 0) {
						// 登陆失败
						Const.SendMessageToActivity(app, Const.LOGINERROR);
					}
					if (str.indexOf(Const.MTKTRUE) >= 0) {
						// 成功连接防盗器
						Log.e(TAG,
								"UpdateGuardStatu len=" + (str.trim()).length());
						if ((str.trim()).length() > 7)
							UpdateGuardStatu(str);
						sendtomtk(Const.MTKONLINE);// 回复防盗器信息
						app.SetMtkonline(true);// 设置在线标记
						Const.SetStandbyMode(app, false);
						Const.SendMessageToActivity(app, Const.MTKONLINE);
						SendMessageBackground(Const.MTKONLINE);
						ClearOfflineDialog();
					}
					if (str.indexOf("LMCHE") >= 0) {
						// LMCHE183.12.163.123:48869,100,50,1
						int addrind = str.indexOf(",");
						int volageind = str.indexOf(",", addrind + 1);
						int chanrge=str.indexOf(",", volageind + 1);
						app.SetMtkonline(true);// 设置在线标记
						Const.SetStandbyMode(app, false);
						
						if (str.indexOf(app.GetMtkaddr()) >= 0) {
							Log.e(TAG, "SocketInputThread LACHE 地址没变");
						} else {
							Log.e(TAG, "SocketInputThread LACHE -- "
									+ str.substring(5, addrind).trim());
							app.SetMtkaddr(str.substring(5, addrind).trim());
							app.Getbackground().UpdatePeerAddrToServer();
							Log.e(TAG, "SocketInputThread LACHE 地址变化");
						}

						sendtomtk(Const.RECVONLINE);
						/* 更新电量、信号强度、充电状态 */
						SendMessageToMainActivity(
								str.substring(addrind + 1, volageind),
								str.substring(volageind + 1, chanrge),
								str.substring(chanrge + 1, str.length()));

						app.SetSignal(Integer.parseInt(str.substring(
								addrind + 1, volageind).trim()));
						app.SetVoltage(Integer.parseInt(str.substring(
								volageind + 1, chanrge).trim()));
						app.SetCharging(Integer.parseInt(str.substring(chanrge + 1, str.length()).trim()));
						/* 更新掉线检测 */
						if (app.Getbackground() != null) {
							if (!app.GetIsStandby()) {
								if (app.Getbackground().GetOpinionFlags()) {// 正在检查状态
									app.Getbackground().StopDeviceOpinion();
									app.Getbackground().StartDeviceOpinion();
								} else {
									app.Getbackground().StartDeviceOpinion();
								}
							}
						}
						
						ClearOfflineDialog();
						
						KeepUpHeart();
					}
					if (str.indexOf("DACLTRUE") >= 0) {
						Const.SendMessageToActivity(app,
								Const.CancelLoginSuccess);
					}
					if (str.indexOf("REPEATLOGIN") >= 0) {
						Const.SendMessageToActivity(app, Const.RepeatLogin);
					}
					if (str.indexOf("CMCIPTRUE") >= 0) {
						SendMessageBackground(Const.RECVPEER);
					}
					if (str.indexOf("OCLPFAIL") >= 0) {// 修改密码时原始密码验证失败
						Const.SendMessageToActivity(app, Const.OCLPFAIL);
					}
					if ("IP".equals(str.substring(0, 2))) {
						Log.e(TAG, "SocketInputThread LACHE IPPPPPP STR=" + str);

						app.SetMtkaddr(str.substring(2, str.length()).trim());
					}
					if (str.indexOf("PHONE") >= 0) {
						// PHONE15920036275
						int point = str.indexOf(",");
						app.Getsharepre().EditPutString(Const.KEY_SETMTKPHONE,
								str.substring(6, point));
						app.Getsharepre().EditPutString(
								Const.KEY_SETANDROIDPHONE,
								str.substring(point + 1, str.length()).trim());
					}
					if (str.indexOf(Const.MTKFALSE) >= 0) {
						// 防盗器不在线
						app.SetMtkonline(false);
						Const.SendMessageToActivity(app, Const.MTKNOONLINE);
					}
					if (str.indexOf(Const.ONLINE) >= 0) {
						sendtomtk(Const.RECVONLINE);
						SendMessageToMainActivity(
								str.substring(7, str.indexOf("#")),
								str.substring(str.indexOf("#") + 1,
										str.length()),"0");
					}
					if (str.indexOf("LBS") >= 0) {
						// LBS22.725484,113.788399
						Log.e(TAG, "LBS = " + str);
						Log.e(TAG,
								"lat = " + str.substring(3, str.indexOf(",")));
						Log.e(TAG,
								"lon = "
										+ str.substring(str.indexOf(",") + 1,
												str.length()));
						SendMessageToGpsActivity(
								Double.valueOf(
										str.substring(str.indexOf(",") + 1,
												str.length()).trim())
										.doubleValue(),
								Double.valueOf(
										str.substring(3, str.indexOf(","))
												.trim()).doubleValue(),
								Const.LBSMODE);
						if (app.GetIsOpenGps() == false) {
							GeoFenceJudge(
									Double.valueOf(
											str.substring(str.indexOf(",") + 1,
													str.length()).trim())
											.doubleValue(),
									Double.valueOf(
											str.substring(3, str.indexOf(","))
													.trim()).doubleValue(),
									Const.LBSMODE);
						}
						
						ClearOfflineDialog();
						Const.SetStandbyMode(app, false);
						
						KeepUpHeart();
					}
					if (str.indexOf("GBLY") >= 0) {
						sendtomtk(Const.RECVGBLY);
						Const.SendMessageToActivity(app, Const.RECVGBLY);
					}
					if (str.indexOf("KQLY") >= 0) {
						sendtomtk(Const.RECVKQLY);
						Const.SendMessageToActivity(app, Const.RECVKQLY);
					}
					if (str.indexOf("GBFD") >= 0) {
						sendtomtk(Const.RECVGBFD);
						Const.SendMessageToActivity(app, Const.RECVGBFD);
					}
					if (str.indexOf("KQFD") >= 0) {
						sendtomtk(Const.RECVKQFD);
						Const.SendMessageToActivity(app, Const.RECVKQFD);
					}
					if (str.indexOf("GBBJ") >= 0) {
						sendtomtk(Const.RECVGBBJ);
						Const.SendMessageToActivity(app, Const.RECVGBBJ);
					}
					if (str.indexOf("BFBJ") >= 0) {
						sendtomtk(Const.RECVBFBJ);
						Const.SendMessageToActivity(app, Const.RECVBFBJ);
					}
					if (str.indexOf("KQGPS") >= 0) {
						sendtomtk(Const.RECVKQGPS);
						SendMessageBackground(Const.RECVKQGPS);
					}
					if (str.indexOf("GBGPS") >= 0) {
						sendtomtk(Const.RECVGBGPS);
						SendMessageBackground(Const.RECVGBGPS);
					}
					if (str.indexOf("KJKZONE") >= 0) {
						sendtomtk(Const.RECVKJKZONE);
						Const.SendMessageToActivity(app, Const.RECVKJKZONE);
					}
					if (str.indexOf("TJKZ") >= 0) {
						sendtomtk(Const.RECVTJKZ);
						Const.SendMessageToActivity(app, Const.RECVTJKZ);
					}
					if (str.indexOf("KJKZTWO") >= 0) {
						sendtomtk(Const.RECVKJKZTWO);
						Const.SendMessageToActivity(app, Const.RECVKJKZTWO);
					}
					if (str.indexOf("BFYY") >= 0) {
						sendtomtk(Const.RECVBFYY);
						Const.SendMessageToActivity(app, Const.RECVBFYY);
					}
					if (str.indexOf("GBYY") >= 0) {
						sendtomtk(Const.RECVGBYY);
						Const.SendMessageToActivity(app, Const.RECVGBYY);
					}
					if (str.indexOf("CMPTRUE") >= 0) {
						Const.SendMessageToActivity(app, Const.RECVMTKPHONE);
					}
					if (str.indexOf("CAPTRUE") >= 0) {
						Const.SendMessageToActivity(app, Const.RECVANDROID);
					}
					if (str.indexOf("CLPTRUE") >= 0) {
						Const.SendMessageToActivity(app, Const.RECVPASSWORD);
					}
					if (str.indexOf("HEART") >= 0) {
						Const.SendMessageToActivity(app, Const.RECVHEART);
					}
					if (str.indexOf("MY") >= 0) {
						if ("".equals(app.GetMyselfaddr())) {
							app.SetMyselfaddr(str.substring(2, str.length())
									.trim());
						} else if (str.indexOf(app.GetMyselfaddr()) < 0) {
							app.SetMyselfaddr(str.substring(2, str.length())
									.trim());
							app.Getbackground().ConnectMtk();
						}
					}
					if (str.indexOf(Const.ALARM) >= 0) {
						sendtomtk(Const.RECVALA);
						// Const.AddLoopAlarmManager(Const.ALARMACTION,context,HandleMessageReceiver.class,1000*60*5);
						Intent intent = new Intent(Const.ALARMACTION);
						intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
						context.sendBroadcast(intent);
					}
					if (str.indexOf(Const.NOCARD) >= 0) {
						sendtomtk(Const.RECVNOCA);
						Const.SendMessageToActivity(app, Const.RECVNOCA);
						/*
						 * Intent intent = new Intent(Const.NOCARTACTION);
						 * intent
						 * .setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
						 * context.sendBroadcast(intent);
						 */
					}
					if (str.indexOf(Const.GPS) >= 0 && str.indexOf("GBGPS") < 0
							&& str.indexOf("KQGPS") < 0) {
						sendtomtk(Const.RECVSTR);
						StringToDouble(str);
					}

				}
			} catch (OptionalDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void ClearOfflineDialog(){
		if (app.GetOfflinedlg() != null) {
			app.GetOfflinedlg().dismiss();
			app.SetOfflinedlg(null);
		}
	}
	
	private void KeepUpHeart() {
		if (mobilebrand) {
			WakeUpPowerManager();// 唤醒cpu
			UpdatePeerAddr();
			Log.e(TAG, "StartAlarmManager 小米  线程");
		}
	}

	private void UpdatePeerAddr() {
		if (!("".equals(app.GetMtkaddr()))) {
			app.Getbackground().SendCmdToServer("K" + app.GetMtkaddr());
			Log.e(TAG, "UpdatePeerAddr 更新对方地址" + app.GetMtkaddr());
		} else {
			app.Getbackground().SendCmdToServer(
					"Q" + app.Getbackground().GetUsername());
		}
	}

	private void WakeUpPowerManager() {
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"com.task.TalkMessageService");
		wakeLock.acquire();
		wakeLock.release();
		// Log.e(TAG, "SocketInputThread 唤醒CPU");
	}

	private void UpdateGuardStatu(String str) {
		int statu = 0;
		// MTKTRUE0
		statu = Integer.valueOf(str.substring(7, str.length()).trim())
				.intValue();

		app.Getsharepre().EditPutBoolean(Const.KEY_ALARM,
				(statu == 1) ? true : false);
		Log.e(TAG, "UpdateGuardStatu = " + statu);
	}

	private void GetCurDate(double lat, double lon, int mode) {

		long insertid, updateid;
		ContentValues values = new ContentValues();

		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		Date date = new java.util.Date();
		insertid = date.getTime();
		String endtime = sDateFormat.format(date);
		date.setMinutes(0);
		date.setSeconds(0);
		String starttime = sDateFormat.format(date);
		Log.e(TAG, "GetCurDate = " + endtime);
		Log.e(TAG, "GetCurDate = " + starttime);
		Log.e(TAG, "GetCurDate lat = " + lat + "lng" + lon);

		values.put("lat", lat);
		values.put("lng", lon);
		values.put("mode", mode);
		values.put("updatetime", endtime);

		values.put("id", insertid);
		Const.InsertDatabaseSqlite(context, Const.TABLENAME, values);

		OptionDatabaseAsyncTask task = new OptionDatabaseAsyncTask();
		task.execute(mode);

	}

	private void GeoFenceJudge(double lon, double lat, int mode) {
		if (!gpsdatabasemarker || !lbsdatabasemarker) {
			if (!gpsdatabasemarker)
				gpsdatabasemarker = true;
			if (!lbsdatabasemarker)
				lbsdatabasemarker = true;
			GetCurDate(lat, lon, mode);
		}
		if (app.GetIsOpenGps()) {
			GetCurDate(lat, lon, mode);
		}
		if (GpsLoctionActivity.IsOutOfRange(context, app, lat, lon)
				&& !app.GetGeoFenceKnowMarker()) {
			Intent intent = new Intent(Const.GEOFENCEACTION);
			intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			context.sendBroadcast(intent);
		}
	}

	private void sendtomtk(int cmdmsg) {
		if ("".equals(app.GetMtkaddr()) == false) {
			String str = "C" + app.GetMtkaddr() + "C";
			if (Const.RECVKQFD == cmdmsg || Const.RECVGBFD == cmdmsg)
				str += "RECVFD";
			if (Const.RECVGBLY == cmdmsg || Const.RECVKQLY == cmdmsg)
				str += "RECVLY";
			if (Const.RECVBFBJ == cmdmsg || Const.RECVGBBJ == cmdmsg)
				str += "RECVBJ";
			if (Const.RECVBFYY == cmdmsg || Const.RECVGBYY == cmdmsg)
				str += "RECVYY";
			if (Const.RECVKQGPS == cmdmsg || Const.RECVGBGPS == cmdmsg)
				str += "RECVGPS";
			if (Const.RECVKJKZONE == cmdmsg || Const.RECVKJKZTWO == cmdmsg)
				str += "RECVKJKZ";
			if (Const.RECVALA == cmdmsg)
				str += "RECVALA";
			if (Const.RECVNOCA == cmdmsg)
				str += "RECVNOCA";
			if (Const.RECVSTR == cmdmsg)
				str += "RECVSTR";
			if (Const.MTKONLINE == cmdmsg)
				str += "RECVMKTRUE";
			if (Const.RECVONLINE == cmdmsg)
				str += "RECVLINE";
			app.Getbackground().SendCmdToServer(str);
		}
	}

	private void SendMessageBackground(int cmdmsg) {
		if (app.Getbackground() != null) {
			Message msg = new Message();
			msg.what = cmdmsg;
			if (!app.Getbackground().GetBackgroundHandler().sendMessage(msg)) {
				app.Getbackground().GetBackgroundHandler().sendMessage(msg);
			}
		}
	}

	/*
	 * private void Const.SendMessageToActivity(app,int cmdmsg) { if
	 * (app.GetTopActivityHandler() != null) { Message msg = new Message();
	 * msg.what = cmdmsg; app.GetTopActivityHandler().sendMessage(msg); } }
	 */
	private void SendMessageToMainActivity(String strsignallevel,
			String strbatterylevel,String chargestate) {
		if (app.GetTopActivityHandler() != null) {
			int signallevel;
			int batterylevel;
			int charging;
			signallevel = Integer.parseInt(strsignallevel.trim());
			batterylevel = Integer.parseInt(strbatterylevel.trim());
			charging=Integer.parseInt(chargestate.trim());
			
			Log.e("SendMessageToMainActivity", "SendMessageToMainActivity "+signallevel+","+batterylevel+","+charging);
			Message msg = new Message();
			msg.what = Const.UPDATESIGNALLEVEL;
			Bundle bundle = new Bundle();
			bundle.putInt("signallevel", signallevel);
			bundle.putInt("batterylevel", batterylevel); // 往Bundle中put数据
			bundle.putInt("chargestate", charging);
			msg.setData(bundle);// mes利用Bundle传递数据
			app.GetTopActivityHandler().sendMessage(msg);
		}
	}

	private void SendMessageToGpsActivity(double lon, double lat, int mode) {
		if (app.GetTopActivityHandler() != null) {

			Message msg = new Message();
			msg.what = Const.UPDATELATLON;
			Bundle bundle = new Bundle();
			bundle.putDouble("lon", lon);
			bundle.putDouble("lat", lat);
			bundle.putInt("mode", mode);// 往Bundle中put数据
			msg.setData(bundle);// mes利用Bundle传递数据
			if (!app.GetTopActivityHandler().sendMessage(msg)) {
				app.GetTopActivityHandler().sendMessage(msg);
			}
		}
	}

	private class OptionDatabaseAsyncTask extends
			AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... arg0) {
			// TODO Auto-generated method stub
			Integer tmp = arg0[0].intValue();
			try {
				Thread.sleep(1000 * 30);
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
			if (result == Const.LBSMODE)
				lbsdatabasemarker = false;
			if (result == Const.GPSMODE)
				gpsdatabasemarker = false;
		}

	}

	// 经纬度运算
	public void StringToDouble(String content) {
		String tmp1, tmp2;
		int lonindex = content.indexOf("lon");
		int latindex = content.indexOf("lat");
		int starindex = content.indexOf("stars");

		tmp1 = content.substring(lonindex + 3, latindex);
		tmp2 = content.substring(latindex + 3, starindex);

		SendMessageToGpsActivity(GetLatLng(tmp1), GetLatLng(tmp2),
				Const.GPSMODE);
		GeoFenceJudge(GetLatLng(tmp1), GetLatLng(tmp2), Const.GPSMODE);
	}

	// 经纬度运算函数
	public double GetLatLng(String temp) {
		int limit;
		double second, minute;
		String t;
		double d = 0;
		DecimalFormat df = new DecimalFormat();

		int point = temp.indexOf(".");
		int length = temp.length();
		if (point < 2)
			return d = 0;
		t = temp.substring(0, point - 2);
		limit = Integer.parseInt(t);// 度
		d += limit;

		t = temp.substring(point - 2, point);// 分
		minute = Double.parseDouble(t);
		minute = minute / 60;
		d += minute;

		t = temp.substring(point, length);
		t = "0" + t;// 秒
		second = Double.parseDouble(t);
		second = second / 60;
		d += second;

		df.setMaximumFractionDigits(6);
		df.setMinimumFractionDigits(6);
		t = df.format(d);
		d = Double.valueOf(t).doubleValue();
		return d;
	}

}
