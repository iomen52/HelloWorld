package com.guanjiangjun2.receiver;

import com.example.guanjiangjun2.R;
import com.guanjiangjun2.util.Const;
import com.guanjiangjun2.util.MyApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class NetworkStateReceiver extends BroadcastReceiver {
	
	private Context context;
	private MyApplication app;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		this.context=context;
		app = (MyApplication) context.getApplicationContext();
		if(intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")){
			// 网络状态改变
			ConnectivityChage();
		}
	}
	
	private void ConnectivityChage(){

		int tempStatus = -1;
        ConnectivityManager connManager  = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
        NetworkInfo gprs = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 	   	NetworkInfo info = connManager.getActiveNetworkInfo();  
        
		if(wifi.isConnected()){
			tempStatus=Const.STATUS_WIFI;
		}else if(gprs.isConnected())
		{
			tempStatus=Const.STATUS_GPRS;
		}else{
			tempStatus=Const.STATUS_ERROR;
		}
        
		if (app.Getnetstatus()!= tempStatus) {
			if (app.Getnetstatus() == Const.STATUS_ERROR
					&& (tempStatus == Const.STATUS_GPRS || tempStatus == Const.STATUS_WIFI)) {
				//从没有网络变为有网络
				Toast.makeText(context, context.getString(R.string.network_state_open_network), Toast.LENGTH_SHORT).show();
				SwitchInternetwork(Const.INTERNETON);
			}else if ((app.Getnetstatus() == Const.STATUS_GPRS || app.Getnetstatus() ==Const.STATUS_WIFI)
					&& tempStatus == Const.STATUS_ERROR ) {
				//从有网络变为没有网络
				Toast.makeText(context,  context.getString(R.string.network_state_close_network), Toast.LENGTH_SHORT).show();
				SwitchInternetwork(Const.INTERNETOFF);
			}else if(app.Getnetstatus() == Const.STATUS_GPRS && tempStatus == Const.STATUS_WIFI){
				//从GPRS变为WIFI
				Toast.makeText(context, context.getString(R.string.network_state_gprs_wifi), Toast.LENGTH_SHORT).show();
				SwitchInternetwork(Const.INTERNETCHANGE);
			}else if(app.Getnetstatus() == Const.STATUS_WIFI && tempStatus == Const.STATUS_GPRS){
				//从WIFI变为GPRS
				Toast.makeText(context, context.getString(R.string.network_state_wifi_gprs), Toast.LENGTH_SHORT).show();
				SwitchInternetwork(Const.INTERNETCHANGE);
			}
		} else {
		}
		app.Setnetstatus(tempStatus);

	}
	
	private  void SwitchInternetwork(int msg){
		if(!Const.GetStandbyMode(app)){
			if(msg==Const.INTERNETON){//从无网络变为有网络
				//MyApplication.IS_INTERNET=true;
				app.SetMtkonline(false);
			}else if(msg==Const.INTERNETOFF){//从有网络变为无网络
				//MyApplication.IS_INTERNET=false;
				app.SetMtkonline(false);
			}else if(msg==Const.INTERNETCHANGE){//网络切换
				//MyApplication.IS_INTERNET=true;
				//MyApplication.IS_ONLINE=true;
			}
			StopOrAcitonDeviceOpinion(msg); //掉线检测
			InternetChagedTransact(msg);	//重新连接
			StopOrAcitonHeart(msg);			//心跳
			SendMessageFunc(msg);			//发送命令
		}
		
	}
	
	private void InternetChagedTransact(int msg){
		if (msg==Const.INTERNETON || msg==Const.INTERNETCHANGE){
			if(app.Getbackground()!=null){
				app.Getbackground().ConnectMtk();
			}
		}else if (msg==Const.INTERNETOFF){
			
		}
		
	}
	
	private void StopOrAcitonDeviceOpinion(int msg) {
		if (app.Getbackground()!= null) {
			if (msg == Const.INTERNETON) { //掉线检测
			} else if (msg == Const.INTERNETOFF) {
				if(app.Getbackground().GetOpinionFlags())//正在检查状态
					app.Getbackground().StopDeviceOpinion();
			} else if (msg == Const.INTERNETCHANGE) {

			}
		}
	}
	
	private void StopOrAcitonHeart(int msg){
		if (msg==Const.INTERNETON){ //重新联网则开始心跳线程
			/*
			if(MyApplication.heartbeatserver!=null)
				MyApplication.heartbeatserver.StartAlarmManager();
				*/
		}else if (msg==Const.INTERNETOFF){
			//if(MyApplication.heartbeatserver!=null)
				//MyApplication.heartbeatserver.StopAlarmManager();
		}else if (msg==Const.INTERNETCHANGE){
			
		}
	}
	
	private void SendMessageFunc(int cmdmsg){
		if(app.GetTopActivityHandler()!=null){
			Message msg=new Message();
			msg.what=cmdmsg;
			if(!app.GetTopActivityHandler().sendMessage(msg)){
				app.GetTopActivityHandler().sendMessage(msg);
			}
			//Log.e("NetworkStateReceiver", "SendMessageFunc");
		}
	}
}
