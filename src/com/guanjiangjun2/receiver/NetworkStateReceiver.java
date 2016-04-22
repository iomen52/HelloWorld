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
			// ����״̬�ı�
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
				//��û�������Ϊ������
				Toast.makeText(context, context.getString(R.string.network_state_open_network), Toast.LENGTH_SHORT).show();
				SwitchInternetwork(Const.INTERNETON);
			}else if ((app.Getnetstatus() == Const.STATUS_GPRS || app.Getnetstatus() ==Const.STATUS_WIFI)
					&& tempStatus == Const.STATUS_ERROR ) {
				//���������Ϊû������
				Toast.makeText(context,  context.getString(R.string.network_state_close_network), Toast.LENGTH_SHORT).show();
				SwitchInternetwork(Const.INTERNETOFF);
			}else if(app.Getnetstatus() == Const.STATUS_GPRS && tempStatus == Const.STATUS_WIFI){
				//��GPRS��ΪWIFI
				Toast.makeText(context, context.getString(R.string.network_state_gprs_wifi), Toast.LENGTH_SHORT).show();
				SwitchInternetwork(Const.INTERNETCHANGE);
			}else if(app.Getnetstatus() == Const.STATUS_WIFI && tempStatus == Const.STATUS_GPRS){
				//��WIFI��ΪGPRS
				Toast.makeText(context, context.getString(R.string.network_state_wifi_gprs), Toast.LENGTH_SHORT).show();
				SwitchInternetwork(Const.INTERNETCHANGE);
			}
		} else {
		}
		app.Setnetstatus(tempStatus);

	}
	
	private  void SwitchInternetwork(int msg){
		if(!Const.GetStandbyMode(app)){
			if(msg==Const.INTERNETON){//���������Ϊ������
				//MyApplication.IS_INTERNET=true;
				app.SetMtkonline(false);
			}else if(msg==Const.INTERNETOFF){//���������Ϊ������
				//MyApplication.IS_INTERNET=false;
				app.SetMtkonline(false);
			}else if(msg==Const.INTERNETCHANGE){//�����л�
				//MyApplication.IS_INTERNET=true;
				//MyApplication.IS_ONLINE=true;
			}
			StopOrAcitonDeviceOpinion(msg); //���߼��
			InternetChagedTransact(msg);	//��������
			StopOrAcitonHeart(msg);			//����
			SendMessageFunc(msg);			//��������
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
			if (msg == Const.INTERNETON) { //���߼��
			} else if (msg == Const.INTERNETOFF) {
				if(app.Getbackground().GetOpinionFlags())//���ڼ��״̬
					app.Getbackground().StopDeviceOpinion();
			} else if (msg == Const.INTERNETCHANGE) {

			}
		}
	}
	
	private void StopOrAcitonHeart(int msg){
		if (msg==Const.INTERNETON){ //����������ʼ�����߳�
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
