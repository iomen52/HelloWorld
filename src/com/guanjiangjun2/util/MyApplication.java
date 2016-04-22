package com.guanjiangjun2.util;

import java.net.DatagramSocket;

import com.baidu.mapapi.SDKInitializer;
import com.example.guanjiangjun2.R;
import com.guanjiangjun2.service.BackgroundServer;
import com.guanjiangjun2.socket.SocketInputThread;
import com.guanjiangjun2.socket.SocketOutputThread;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;

public class MyApplication extends Application {
	
	//private double lbslon, lbslat; // LBS经纬坐标
	//private double gpslon, gpslat; // GPS经纬坐标
	private boolean shutdownasync=false;
	public boolean GetShutdownAsync(){
		return shutdownasync;
	}
	public void SetShutdownAsync(boolean shutdownasync){
		this.shutdownasync=shutdownasync;
	}
	
	private int signal=0;
	public int GetSignal(){
		return signal;
	}
	public void SetSignal(int signal){
		this.signal=signal;
	}
	private int voltage=0;
	public int GetVoltage(){
		return voltage;
	}
	public void SetVoltage(int voltage){
		this.voltage=voltage;
	}
	
	private int charging=0;
	public int GetCharging(){
		return charging;
	}
	public void SetCharging(int charging){
		this.charging=charging;
	}
	
	private DatagramSocket socket;
	public DatagramSocket GetDatagramSocket(){
		return socket;
	}
	public void SetDatagramSocket(DatagramSocket socket){
		this.socket=socket;
	}
	private boolean IsOpenGps=false;
	public boolean GetIsOpenGps(){
		return IsOpenGps;
	}
	public void SetIsOpenGps(boolean bool){
		this.IsOpenGps=bool;
	}
	
	private boolean IsStandby=false;//IsStandby = true;表示待机状态
	public boolean GetIsStandby(){
		return IsStandby;
	}
	public void SetIsStandby(boolean bool){
		this.IsStandby=bool;
	}
	
	private boolean LOGINSTATE=false;
	public boolean GetLoginState(){
		return LOGINSTATE;
	}
	public void SetLoginState(boolean bool){
		this.LOGINSTATE=bool;
	}
	private int netstatus = 2;
	public int Getnetstatus(){
		return netstatus;
	}
	public void Setnetstatus(int netstatus){
		this.netstatus=netstatus;
	}
	/*
	public void SetGpsLonLat(double gpslon, double gpslat){
		this.gpslon=gpslon;
		this.gpslat=gpslat;
	}
	public void SetLbsLonLat(double lbslon, double lbslat){
		this.lbslon=lbslon;
		this.lbslat=lbslat;
	}
	public double GetGpsLon(){
		return this.gpslon;
	}
	public double GetGpsLat(){
		return this.gpslat;
	}
	public double GetLbsLon(){
		return this.lbslon;
	}
	public double GetLbsLat(){
		return this.lbslat;
	}
	*/
	private SoundPool sp;
	
	private int heartcount=0;
	private int currentStreamId;// 当前播放的StreamId
	private AlertDialog Offlinedlg;
	private AlertDialog Alarmdlg;
	private AlertDialog Geofencedlg;
	private boolean MTKONLINE = false;// 是否连接服务器标志
	private boolean ALARMMARKER = false;
	private boolean NOCARDMARKER = false;
	private boolean GEOFENCEMARKER = false;//电子围栏超出范围报警标志
	private boolean GEOFENCEKNOWMARKER = false;//电子围栏报警响铃知道了标志
	private String myselfaddr = "";// 客户端自身地址
	private String mtkaddr = "";// 防盗器IP地址

	private SharedPreferenceClass sharepre = null;
	private Handler TopActivityHandler;
	private BackgroundServer background;
	private SocketInputThread inputthread;
	private SocketOutputThread outputthread;
	private static Application mInstance;
	private final static String TAG = "MyApplication";
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mInstance=this;
		CrashHandler handler = CrashHandler.getInstance();
        handler.init(getApplicationContext()); //在Appliction里面设置我们的异常处理器为UncaughtExceptionHandler处理器
        Thread.setDefaultUncaughtExceptionHandler(handler);
		this.sharepre = new SharedPreferenceClass(this);
		PlayAlarmInit(this);
		SDKInitializer.initialize(getApplicationContext());
	}
	public static Application getInstance() {
        return mInstance;
    }
	public void PlayAlarmInit(Context context) {
		if (this.sp!= null) {
			return;
		} else {
			this.sp=new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
			this.sp.load(context, R.raw.alarm, 1);
		}
	}
	public void StopPlayAlarmSound(){
		this.sp.stop(currentStreamId);
	}
	public void PlaySounds() {
		this.currentStreamId=this.sp.play(1, 1, 1, 0, 0, 1);
		// Log.i(app.TAG, "currentStreamId="+currentStreamId);
	}
	public AlertDialog GetOfflinedlg(){
		return Offlinedlg;
	}
	
	public AlertDialog GetAlarmdlg(){
		return Alarmdlg;
	}
	public void SetGeofencedlg(AlertDialog Geofencedlg){
		this.Geofencedlg=Geofencedlg;
	}
	public AlertDialog GetGeofencedlg(){
		return Geofencedlg;
	}
	public void SetOfflinedlg(AlertDialog Offlinedlg){
		this.Offlinedlg=Offlinedlg;
	}
	
	public void SetAlarmdlg(AlertDialog Alarmdlg){
		this.Alarmdlg=Alarmdlg;
	}
	public void SetAlarmMarker(boolean bool){
		this.ALARMMARKER=bool;
	}
	public boolean GetAlarmMarker(){
		return this.ALARMMARKER;
	}
	public void SetNocardMarker(boolean bool){
		this.NOCARDMARKER=bool;
	}
	public void SetGeoFenceKnowMarker(boolean bool){
		this.GEOFENCEKNOWMARKER=bool;
	}
	public boolean GetGeoFenceKnowMarker(){
		return this.GEOFENCEKNOWMARKER;
	}
	public void SetGeoFenceMarker(boolean bool){
		this.GEOFENCEMARKER=bool;
	}
	public boolean GetGeoFenceMarker(){
		return this.GEOFENCEMARKER;
	}
	public boolean GetNocardMarker(){
		return this.NOCARDMARKER;
	}
	public int GetHeartCount(){
		return this.heartcount;
	}
	public void SetHeartCountZero(){
		this.heartcount=0;
	}
	public void SetAddHeartCount(){
		this.heartcount+=1;
	}
	public boolean GetMtkonline(){
		return this.MTKONLINE;
	}
	public void SetMtkonline(boolean bool){
		this.MTKONLINE=bool;
	}
	public String GetMyselfaddr(){
		return this.myselfaddr;
	}
	public void SetMyselfaddr(String myselfaddr){
		this.myselfaddr=myselfaddr;
	}
	public String GetMtkaddr(){
		return this.mtkaddr;
	}
	public void SetMtkaddr(String mtkaddr){
		this.mtkaddr=mtkaddr;
	}
	public SharedPreferenceClass Getsharepre(){
		return sharepre;
	}
	public void SetTopActivityHandler(Handler TopActivityHandler){
		this.TopActivityHandler=TopActivityHandler;
	}
	public Handler GetTopActivityHandler(){
		return TopActivityHandler;
	}
	public void Setbackground(BackgroundServer background){
		this.background=background;
	}
	public void SetSocketInputThread(SocketInputThread inputthread) {
		this.inputthread=inputthread;
    }
	public void SetSocketOutputThread(SocketOutputThread outputthread) {
		this.outputthread=outputthread;
    }
	public BackgroundServer Getbackground(){
		return this.background;
	}
	public SocketInputThread GetSocketInputThread() {
		return this.inputthread;
    }
	public SocketOutputThread GetSocketOutputThread() {
		return this.outputthread;
    }
}
