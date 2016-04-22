package com.guanjiangjun2.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.guanjiangjun2.R;
import com.guanjiangjun2.activity.LoginActivity;
import com.guanjiangjun2.activity.MainActivity;
import com.guanjiangjun2.activity.SettingActivity;
import com.guanjiangjun2.service.BackgroundServer;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Const {

	public final static String SOCKET_SERVER = "112.74.124.116";
	public final static int SOCKET_PORT = 5678;
	//public final static int SOCKET_PORT = 3456;

	public final static int SENDCMD_TIMEOUT = 1000 * 16;

	public final static String QUERYONLINE = "Q";// Android端主动连接防盗器
	/* MainActivity定义 */
	//public final static String KEY_MTKPHONE = "KEY_PHONE";
	public final static String KEY_ANDROIDPHONE = "KEY_PHONE";
	public final static String KEY_ALARM = "KEY_ALARM";
	public final static String KEY_MONITOR = "KEY_MONITOR";
	public final static String KEY_PLAYTONE = "KEY_PLAYTONE";
	public final static String KEY_ALARMLEVEL = "KEY_ALARMLEVEL";
	public final static String KEY_GPS = "KEY_GPS";
	public final static String KEY_SMSCONTENT = "KEY_SMSCONTENT";
	public final static String KEY_GPSCONTENT = "KEY_GPSCONTENT";
	public final static String KEY_LAT = "KEY_LAT";
	public final static String KEY_LNG = "KEY_LNG";
	public final static String KEY_VOICE = "KEY_VOICE";
	public final static String KEY_VOICESELECT = "KEY_VOICESELECT";
	
	public final static String KEY_GEOLAT = "KEY_GEOLAT";
	public final static String KEY_GEOLON = "KEY_GEOLON";
	public final static String KEY_DISTANCE = "KEY_DISTANCE";

	public final static String TABLENAME = "history";
	// 防盗器在线与否
	public final static String MTKFALSE = "MTKFALSE";
	public final static String MTKTRUE = "MTKTRUE";
	// 是否登陆成功
	public final static String LOGINFALSE = "LOGINFALSE";
	public final static String LOGINTRUE = "LOGINTRUE";
	
	//public final static String STANDBYMODE = "STANDBYMODE";

	public final static String USERNAME = "USERNAME";// 用户名保存字段
	public final static String SAVEIMEI = "SAVEIMEI";// IMEI号
	public final static String PASSWORD = "PASSWORD";// 密码保存字段
	public final static String SAVEPASSWORD = "SAVEPASSWORD";// 保存密码
	public final static String AUTOLOGIN = "AUTOLOGIN";// 自动登录
	public final static String FIRSTLOGIN = "FIRSTLOGIN";// 第一次登录
	public final static String MOBILEBRAND= "MOBILEBRAND";// 手机品牌判别
	public final static String STANDBYMODE= "STANDBYMODE";// 待机模式判别
	public final static String STANDBYTIME= "STANDBYTIME";// 待机模式时间标记
	/* GuardActivity */
	public final static String SPLNUM = "SPLNUM";// 自动登录

	/* BootUpReceiver定义 */
	public final static String ALARM = "ALARM";
	public final static String GPS = "GPS";
	public final static String NOCARD = "NOCARD";
	public final static String ServiceName = "com.example.guanjiangjun.Deamonservice";// 用于判断服务是否运行
	public final static String Process_Name = "com.example.guanjiangjun:Deamonservice"; // 用于判断进程是否运行
	public final static String ActivityProcess_Name = "com.example.guanjiangjun:MainActivity"; // 用于判断进程是否运行
	/* SettingActivity定义 */
	public final static String KEY_BURGLARFIRST = "KEY_BURGLARFIRST";
	public final static String KEY_BURGLARSECOND = "KEY_BURGLARSECOND";
	public final static String KEY_SETMTKPHONE = "KEY_SETMTKPHONE";
	public final static String KEY_SETANDROIDPHONE = "KEY_SETANDROIDPHONE";
	public final static String KEY_SETHEARTQUANTITY = "KEY_SETHEARTQUANTITY";

	public final static String HEARTBEAT = "com.example.guanjiangjun.HEART_BEAT";
	public final static String HISTORYCLEAN = "com.example.guanjiangjun.HISTORY_CLEAN";
	public final static String MTKOFFLINE = "com.example.guanjiangjun.DEVICE_OFFLINE";
	public final static String ONLINE = "ONLINE";

	public final static String ALARMACTION = "com.example.guanjiangjun.ALARM_ACTION";
	public final static String GPSACTION = "com.example.guanjiangjun.GPS_ACTION";
	public final static String NOCARTACTION = "com.example.guanjiangjun.NOCARD_ACTION";
	public final static String GEOFENCEACTION = "com.example.guanjiangjun.GEOFENCE_ACTION";

	public final static int STATUS_WIFI = 2;
	public final static int STATUS_GPRS = 1;
	public final static int STATUS_ERROR = 0;
	/*
	 * app命令集
	 */
	// 服务器与防盗器之间的连接
	// public final static int STOPTHREAD = 100;// Android端主动断开连接，停止out\in线程
	// public final static int RECONNECT = 101;// Android端重新连接服务器
	public final static int SENDCMD = 102;// Android端发送命令
	// public final static int REONLINE = 103;//Android端发起重新连接防盗器请求
	public final static int LOGINERROR = 104;// 登陆服务器失败消息
	public final static int LOGINSUESS = 105;// 登陆服务器成功消息
	public final static int MTKONLINE = 106;// 防盗器在线消息
	public final static int MTKNOONLINE = 107;// 防盗器不在线消息
	public final static int LOGINTIMEOUT = 108;// 登陆超时
	public final static int CONNECTMTK = 109;// 重新连接MTK
	public final static int INTERNETON = 110;// 联网
	public final static int INTERNETOFF = 111;// 断网
	public final static int HEARTONLINE = 112;// 心跳检测MTK是否在线
	public final static int HEARTOFFLINE = 113;// 网络切换失败时连接MTK失败
	public final static int INTERNETCHANGE = 114;// 切换网络

	public final static int RECVKJKZONE = 150;// 客户端收到KJKZONE命令
	public final static int RECVKJKZTWO = 151;// 客户端收到KJKZTWO命令
	public final static int RECVKQGPS = 152;// 客户端收到KQGPS命令
	public final static int RECVGBGPS = 153;// 客户端收到GBGPS命令
	public final static int RECVBFBJ = 154;// 客户端收到BFBJ命令
	public final static int RECVGBBJ = 155;// 客户端收到GBBJ命令
	public final static int RECVKQFD = 156;// 客户端收到KQFD命令
	public final static int RECVGBFD = 157;// 客户端收到GBFD命令
	public final static int RECVKQLY = 158;// 客户端收到KQLY命令
	public final static int RECVGBLY = 159;// 客户端收到GBFD命令
	public final static int RECVTJKZ = 160;// 客户端收到TJKZ命令
	public final static int RECVBFYY = 161;// 客户端收到BJYY命令
	public final static int RECVGBYY = 162;// 客户端收到GBYY命令
	public final static int RECVALA = 163;// 客户端收到GBYY命令
	public final static int RECVNOCA = 164;// 客户端收到GBYY命令
	public final static int RECVSTR = 165;// 客户端收到GBYY命令
	public final static int RECVONLINE = 165;// 客户端收到GBYY命令
	public final static int RECVMTKPHONE = 166;// 客户端收到GBYY命令
	public final static int RECVANDROID= 167;// 客户端收到GBYY命令
	public final static int RECVPASSWORD= 168;// 客户端收到GBYY命令
	public final static int RECVHEART= 169;//
	public final static int RECVPEER= 170;//
	public final static int RECVSTANDBY= 171;//

	public final static int FAILKJKZONE = 210;// 客户端收到KJKZONE命令
	public final static int FAILKJKZTWO = 211;// 客户端收到KJKZTWO命令
	public final static int FAILKQGPS = 212;// 客户端收到KQGPS命令
	public final static int FAILGBGPS = 213;// 客户端收到GBGPS命令
	public final static int FAILBFBJ = 214;// 客户端收到BFBJ命令
	public final static int FAILGBBJ = 215;// 客户端收到GBBJ命令
	public final static int FAILKQFD = 216;// 客户端收到KQFD命令
	public final static int FAILGBFD = 217;// 客户端收到GBFD命令
	public final static int FAILKQLY = 218;// 客户端收到KQLY命令
	public final static int FAILGBLY = 219;// 客户端收到GBFD命令
	public final static int FAILTJKZ = 220;// 客户端收到TJKZ命令
	public final static int FAILGBYY = 221;// 客户端收到GBYY命令
	public final static int FAILBFYY = 222;// 客户端收到BJYY命令
	public final static int FAILMTKPHONE = 223;
	public final static int FAILANDROID= 224;
	public final static int FAILPASSWORD= 225;
	public final static int FAILHEART= 226;//
	public final static int FAILPEER= 227;//
	public final static int FAILSTANDBY= 228;//
	
	
	
	
	public final static int UPDATETIME = 250;// 时间更新
	public final static int STOPUPDATETIME = 251;// 时间更新
	public final static int UPDATESIGNALLEVEL = 252;// 时间更新
	public final static int UPDATELATLON = 253;// 更新gps地址信息
	
	public final static int GPSMODE = 254;// 更新gps地址信息
	public final static int LBSMODE = 255;// 更新gps地址信息
	public final static int SYSTEMREST = 256;// 更新gps地址信息
	
	
	public final static int UPDATEELEMENT = 257;// 
	public final static int REMOVEELEMENT = 258;// 
	public final static int CONTINUEELEMENT = 259;// 
	public final static int CancelLoginSuccess = 260;// 更新gps地址信息
	public final static int CancelLoginError = 261;// 更新gps地址信息
	public final static int RepeatLogin = 262;
	public final static int OCLPFAIL = 263;

	private static final double EARTH_RADIUS = 6378137.0;
	
	public static double getDistance(double longitude1, double latitude1,
			double longitude2, double latitude2) {
		double Lat1 = rad(latitude1);
		double Lat2 = rad(latitude2);
		double a = Lat1 - Lat2;
		double b = rad(longitude1) - rad(longitude2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(Lat1) * Math.cos(Lat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;
	}
	
	//删除、修改语句执行函数
	public static void ExecDatabaseSqlite(Context context,String sql){
		/*创建数据库*/
		DatabaseHelper dbhelper=new DatabaseHelper(context, "GpsHistory");
		SQLiteDatabase db=dbhelper.getWritableDatabase();

		db.execSQL(sql);//执行删除操作
		db.close();
	}
	
	//添加语句执行函数
	public static void InsertDatabaseSqlite(Context context,String tablename,ContentValues values){
		
		DatabaseHelper dbhelper=new DatabaseHelper(context, "GpsHistory");
		SQLiteDatabase db=dbhelper.getWritableDatabase();
		//第二个参数是不允许空列
		db.insert(tablename, null, values);
		
		db.close();
	}
	
	//获取符合条件数据行数
	public static long GetCountFromSqlite(Context context,String sql){
		int count=0;
		DatabaseHelper dbhelper=new DatabaseHelper(context, "GpsHistory");
		SQLiteDatabase db=dbhelper.getReadableDatabase();
		
		Cursor cursor = db.rawQuery(sql,null);
		
		count=cursor.getCount();
		
		db.close();

		return count;
	}
	
	//获取数据集
	public static Cursor GetCursorResult(Context context,String sql){
		
		DatabaseHelper dbhelper=new DatabaseHelper(context, "GpsHistory");
		SQLiteDatabase db=dbhelper.getReadableDatabase();		
		Cursor cursor = db.rawQuery(sql,null);
		
		return cursor;
	}
	
	public static void InsertDeviceInfo(Context context,String nick,String Imei,String psd){
		
		ContentValues values = new ContentValues();
		
		values.put("nickname", nick);
		values.put("imei", Imei);
		values.put("password", psd);
		
		Const.InsertDatabaseSqlite(context, "device", values);
	}
	
	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	// 进程是否在运行
	public static boolean isProessRunning(String proessName, Context context) {
		// String proessName = "com.example.guanjiangjun";
		boolean isRunning = false;
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		List<RunningAppProcessInfo> lists = am.getRunningAppProcesses();
		for (RunningAppProcessInfo info : lists) {
			if (info.processName.equals(proessName)) {
				// Log.i("Service2进程", ""+info.processName);
				isRunning = true;
			}
		}

		return isRunning;
	}

	// activity是否在栈顶
	public static boolean isTopActivity(String packageName, Context context) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
		if (tasksInfo.size() > 0) {
			if (packageName.equals(tasksInfo.get(0).topActivity
					.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	// 查看服务是否启动
	public static boolean ServiceIsWorking(String str, Context context) {
		ActivityManager myManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) myManager
				.getRunningServices(50);
		for (int i = 0; i < runningService.size(); i++) {
			if (runningService.get(i).service.getClassName().toString()
					.equals(str)) {
				return true;
			}
		}
		return false;
	}

	public static Dialog createLoadingDialog(Context context, String msg) {

		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
		LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
		// main.xml中的ImageView
		ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
		TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
		// 加载动画
		Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
				context, R.layout.loading_animation);
		// 使用ImageView显示动画
		spaceshipImage.startAnimation(hyperspaceJumpAnimation);
		tipTextView.setText(msg);// 设置加载信息

		Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

		loadingDialog.setCancelable(false);// 不可以用“返回键”取消
		loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT));// 设置布局
		return loadingDialog;

	}

	// 检查设备当前网络状况
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager == null) {
			return false;
		} else {
			// 获取NetworkInfo对象
			NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

			if (networkInfo != null && networkInfo.length > 0) {
				for (int i = 0; i < networkInfo.length; i++) {
					// 判断当前网络状态是否为连接状态
					if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
						// IS_INTERNET = true;
						// Log.i(Const.TAG, i + "myapplication.IS_INTERNET="+
						// IS_INTERNET);
						return true;
					}
				}
			}
		}
		return false;
	}

	public static void DeleteAlarmManager(String action, Context context,
			Class<?> cls) {

		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, cls);
		intent.setAction(action);
		PendingIntent sender = PendingIntent
				.getBroadcast(context, 0, intent, 0);
		am.cancel(sender);

	}

	public static void AddLoopAlarmManager(String action, Context context,
			Class<?> cls, long time) {

		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, cls);
		intent.setAction(action);
		PendingIntent sender = PendingIntent
				.getBroadcast(context, 0, intent, 0);
		long firstime = SystemClock.elapsedRealtime();// 开始时间
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime, time,
				sender);
	}

	public static void AddAlarmManager(String action, Context context,
			Class<?> cls, int time) {

		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, cls);
		intent.setAction(action);
		PendingIntent sender = PendingIntent
				.getBroadcast(context, 0, intent, 0);
		// long firstime = SystemClock.elapsedRealtime();// 开始时间
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + time,
				sender);
	}
	
	public static TimeOutClass SendMessageFunc(MyApplication app,Handler mHandler,String str,int msg){
		
		if (app.Getbackground() != null) {
			if(str.subSequence(0, 1).equals("C")){
				//Toast.makeText(app.Getbackground(), "命令C", Toast.LENGTH_SHORT).show();
				if(str.length()<10){
					//Toast.makeText(app.Getbackground(), "mtk地址为空,尝试修复", Toast.LENGTH_SHORT).show();
					app.Getbackground().SystemReset();
				}
			}
			app.Getbackground().SendCmdToServer(str);
			TimeOutClass timeout = new TimeOutClass(app, mHandler, str, Const.SENDCMD_TIMEOUT,msg);
			return timeout;
		}
		
		return null;
		
	}

	public static void StopTimeOut(TimeOutClass timeout) {
		if (timeout != null) {
			timeout.stopTimer();
			timeout = null;
		}
	}
	
	public static void GoToMainActivityFunc(MyApplication app,Context context) {
		app.SetTopActivityHandler(null);
		Intent it = new Intent();
		it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		it.setClass(context, MainActivity.class);
		context.startActivity(it);
		//context.finish();
	}
	
	public static void CloseDialog(ProgressDialog progressDialog){
		if(progressDialog!=null){
			progressDialog.dismiss();
			progressDialog=null;
		}
	}
	
	public static void SendMessageToActivity(MyApplication app,int cmdmsg) {
		if (app.GetTopActivityHandler() != null) {
			Message msg = new Message();
			msg.what = cmdmsg;
			Log.e("SendMessageToActivity", "SendMessageToActivity cmdmsg="+cmdmsg);//MTKTRUE 106
			if(!app.GetTopActivityHandler().sendMessage(msg)){
				app.GetTopActivityHandler().sendMessage(msg);
			}
		}
	}
	
	/**
	 * 离线地图所占的容量大小
	 * 
	 * @param size
	 * @return
	 */
	public static String formatDataSize(int size) {
		String ret = "";
		if (size < (1024 * 1024)) {
			ret = String.format("%dK", size / 1024);
		} else {
			ret = String.format("%.1fM", size / (1024 * 1024.0));
		}
		return ret;
	}
	
	public static void StartSocketService(Context context){

		if (!Const.ServiceIsWorking(
			"com.guanjiangjun.socket.HeartBeatServer", context)) {
			Intent it = new Intent(context, BackgroundServer.class);
			context.startService(it);
			Log.e("StartSocketService", "StartSocketService");
		}
	}
	
	public static boolean GetStandbyMode(MyApplication app){
		boolean bool=false;
		bool=app.Getsharepre().GetSharePre().getBoolean(Const.STANDBYMODE, false);
		return bool;
	}
	
	public static void SetStandbyMode(MyApplication app,boolean bool){//true:开启待机模式 false:工作模式
			app.Getsharepre().EditPutBoolean(Const.STANDBYMODE, bool);
	}
	
	public static String GetImei(MyApplication app,Context context){
		String Imei; 
		Imei=app.Getsharepre().GetSharePre().getString(Const.SAVEIMEI, "");
		if("".equals(Imei)){
			Imei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
			app.Getsharepre().EditPutString(Const.SAVEIMEI, Imei);
		}
		return Imei;
	}
	
	public static void GotoLogin(MyApplication app,Context context){
		app.SetTopActivityHandler(null); 
		Intent intent=new Intent();
		intent.setClass(context, LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
		//finish();
	}
	
	public static void SaveOperationInfo(String cmd,String str){
		
		StringBuffer sb = new StringBuffer(); 
		sb.append(str);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		long timestamp = System.currentTimeMillis();  
        String time = formatter.format(new Date());  
        String fileName = cmd + time + "-" + str + ".log";  
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {  
            String path = "/sdcard/crash/";  
            File dir = new File(path);  
            if (!dir.exists()) {  
                dir.mkdirs();  
            }  
            try {
            	FileOutputStream fos = new FileOutputStream(path + fileName);  
				fos.write(sb.toString().getBytes());
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
              
        }  
	}
}
