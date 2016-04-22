package com.guanjiangjun2.service;

import com.guanjiangjun2.receiver.HandleMessageReceiver;
import com.guanjiangjun2.receiver.HeartMessageReceiver;
import com.guanjiangjun2.receiver.HistoryCleanReceiver;
import com.guanjiangjun2.socket.SocketInputThread;
import com.guanjiangjun2.socket.SocketOutputThread;
import com.guanjiangjun2.util.Const;
import com.guanjiangjun2.util.CreateInputOutputThread;
import com.guanjiangjun2.util.MyApplication;
import com.guanjiangjun2.util.TimeOutClass;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class BackgroundServer extends Service {

	private boolean heartbeatstatu=false;
	private boolean Opinioning=false;
	private WakeLock wakeLock;
	private TimeOutClass timeoutgps;
	private SocketInputThread inputthread;
	private SocketOutputThread outputthread;
	private TimeOutClass timeoutconnet;
	private TimeOutClass timeoutupdatepeer;
	private String username = "";
	private boolean mobilebrand=false;//false:标准android true:小米android
	private int heartquantity=20;
	private MyApplication app;
	private final static String TAG = "BackgroundServer";

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		app = (MyApplication) getApplication();
		GetDataFromXml();
		app.Setbackground(this);
		CreateInputOutputThread inout = new CreateInputOutputThread(this);
		inout.CreateSocketThread();
		StartAlarmManager();
		StartHistoryCleanAlarmManager();
	}
	public void SetUsername(String name){
		username=name;
	}
	
	public String GetUsername(){
		return username;
	}
	
	public SocketInputThread GetInputThread(){
		return inputthread;
	}
	
	public SocketOutputThread GetOutputThread(){
		return outputthread;
	}

	public boolean GetMobileBrand(){
		return mobilebrand;
	}
	
	public void SetMobileBrand(boolean bool){
		this.mobilebrand=bool;
	}	
	public void StartHistoryCleanAlarmManager(){
		Const.AddLoopAlarmManager(Const.HISTORYCLEAN, this, HistoryCleanReceiver.class,  24*60*60*1000);
	}
	
	public void StopAlarmManager() {
		Const.DeleteAlarmManager(Const.HEARTBEAT, this,HeartMessageReceiver.class);
		heartbeatstatu=false;
		ReleasePowerManager();
	}

	public void StartAlarmManager() {
		// 创建PowerManager对象
		if(mobilebrand)//如果是小米手机则唤醒时间定为6分钟
		{
			Const.AddLoopAlarmManager(Const.HEARTBEAT, this, HeartMessageReceiver.class,6 * 60 * 1000);
			Log.e(TAG, "StartAlarmManager 小米手机唤醒时间 6 分钟");
		}else{
			Const.AddLoopAlarmManager(Const.HEARTBEAT, this, HeartMessageReceiver.class,heartquantity * 1000);
			Log.e(TAG, "StartAlarmManager 标准手机唤醒时间 按照心跳设置");
		}
		heartbeatstatu=true;
		GetPowerManager();
	}
	
	public boolean GetOpinionFlags(){
		return Opinioning;
	}
	
	public void StartDeviceOpinion(){
		Opinioning=true;
		if(mobilebrand)//如果是小米手机则掉线检测定为6分钟
			Const.AddAlarmManager(Const.MTKOFFLINE, this,HandleMessageReceiver.class,6*60*1000);
		else
			Const.AddAlarmManager(Const.MTKOFFLINE, this,HandleMessageReceiver.class,3*60*1000);
	}

	public void StopDeviceOpinion(){
		Const.DeleteAlarmManager(Const.MTKOFFLINE, this,HandleMessageReceiver.class);
    	Opinioning=false;
	}
	private void GetPowerManager(){
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"com.task.TalkMessageService"); 
		wakeLock.acquire(); 
	}
	
	private void ReleasePowerManager(){
		if (wakeLock != null) { 
		     wakeLock.release(); 
		     wakeLock = null; 
		 }
	}
	
	public void SetSocketInputThread(SocketInputThread inputthread){
		this.inputthread = inputthread;
	}
	
	public void SetSocketOutputThread(SocketOutputThread outputthread){
		this.outputthread = outputthread;
	}
	
	public void SetSocketThread(SocketInputThread inputthread,
			SocketOutputThread outputthread) {
		this.inputthread = inputthread;
		this.outputthread = outputthread;
	}

	public void SendCmdToServer(String cmd) {
		if (this.outputthread != null) {
			outputthread.addMsgToSendList(cmd);
		}
	}

	public void OpenGpsCmd() {
		timeoutgps=Const.SendMessageFunc(app, mHandler, "C" + app.GetMtkaddr() + "CKQGPS", Const.FAILKQGPS);
	}

	public void CloseGpsCmd() {
		timeoutgps=Const.SendMessageFunc(app, mHandler,"C" + app.GetMtkaddr() + "CGBGPS", Const.FAILGBGPS);
	}

	public void UpdatePeerAddrToServer(){
		if(!"".equals(username))
			timeoutupdatepeer=Const.SendMessageFunc(app, mHandler,"N"+app.GetMtkaddr()+","+ username, Const.FAILPEER);
	}
	
	public Handler GetBackgroundHandler() {
		return mHandler;
	}

	// 获得xml文件中保存的数据
	public void GetDataFromXml() {
		String heart=app.Getsharepre().GetSharePre().getString(Const.KEY_SETHEARTQUANTITY, "20");
		username = app.Getsharepre().GetSharePre()
				.getString(Const.USERNAME, "");
		heartquantity=Integer.valueOf(heart.trim()).intValue();
		mobilebrand=app.Getsharepre().GetSharePre().getBoolean(Const.MOBILEBRAND, false);

	}
	
	public void SetHeartQuantity(int heartquantity){
		this.heartquantity=heartquantity;
	}

	public void ConnectMtk() {
		SendCmdToServer(Const.QUERYONLINE + username);
		timeoutconnet = new TimeOutClass(app, mHandler, Const.QUERYONLINE + username, Const.SENDCMD_TIMEOUT*2,Const.MTKNOONLINE);
		//timeoutconnet=Const.SendMessageFunc(app, mHandler, Const.QUERYONLINE + username, Const.MTKNOONLINE);
	}

	private void SendMessageToActivity(int what){

		if(app.GetTopActivityHandler()!=null){
			Message msg=new Message();
			msg.what=what;
			if(!app.GetTopActivityHandler().sendMessage(msg)){
				app.GetTopActivityHandler().sendMessage(msg);
			}
			
		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case Const.MTKONLINE:// 防盗器在线
				Const.StopTimeOut(timeoutconnet);
				SendMessageToActivity(Const.MTKONLINE);
				break;
			case Const.MTKNOONLINE:// 防盗器不在线
				Const.StopTimeOut(timeoutconnet);
				SendMessageToActivity(Const.MTKNOONLINE);
				break;
			case Const.RECVKQGPS:
				Const.StopTimeOut(timeoutgps);
				break;
			case Const.RECVPEER:
				Const.StopTimeOut(timeoutupdatepeer);
				break;
			case Const.RECVGBGPS:
				Const.StopTimeOut(timeoutgps);
				break;
			case Const.FAILGBGPS:
				break;
			case Const.FAILKQGPS:
				break;
			default:
				//Log.e(TAG, "message= " + msg.what);
				break;
			}
		}

	};

	public void SystemReset() {

		app = (MyApplication) getApplication();
		GetDataFromXml();
		app.Setbackground(this);
		if (outputthread != null && outputthread.isAlive()) {

		} else {
			CreateInputOutputThread inout = new CreateInputOutputThread(this);
			inout.CreateSocketThread();
		}
		StartAlarmManager();
		
		SendCmdToServer("Q"+username);
		//TimeOutClass timeout = new TimeOutClass(app, mHandler, "Q"+username, Const.SENDCMD_TIMEOUT,0);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
