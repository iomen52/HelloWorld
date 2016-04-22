package com.guanjiangjun2.receiver;


import com.guanjiangjun2.service.BackgroundServer;
import com.guanjiangjun2.socket.SocketInputThread;
import com.guanjiangjun2.socket.SocketOutputThread;
import com.guanjiangjun2.util.Const;
import com.guanjiangjun2.util.MyApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HeartMessageReceiver extends BroadcastReceiver{
	private final static String TAG = "HeartMessageReceiver";
	private Context context;
	private MyApplication app;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		this.context=context;
		app = (MyApplication) context.getApplicationContext();
		//Log.e(TAG, "HeartMessageReceiver Z9����");
		if (app.Getbackground() != null) {
			
			
			/*
			if(app.GetSocketOutputThread()!=null){
				if(!app.GetSocketOutputThread().isAlive()){//��������̲߳�����
					SocketOutputThread outputthread=new SocketOutputThread(app.GetDatagramSocket(),context);
					outputthread.start();
					app.SetSocketOutputThread(outputthread);
				}
				if(!app.GetSocketInputThread().isAlive()){//��������̲߳�����
					SocketInputThread inputthread=new SocketInputThread(app.GetDatagramSocket(), context);
					inputthread.start();
					app.SetSocketInputThread(inputthread);
				}
				app.Getbackground().SetSocketThread(app.GetSocketInputThread(), app.GetSocketOutputThread());
			}
			*/
			if(!Const.isNetworkAvailable(context)){
				return;
			}
			if("".equals(app.Getbackground().GetUsername())){
				app.Getbackground().GetDataFromXml();
			}
			if("".equals(app.GetMtkaddr())){
				if(!("".equals(app.Getbackground().GetUsername()))){
					app.Getbackground().SendCmdToServer("Q"+app.Getbackground().GetUsername());
				}
			}
			if(intent.getAction().equals(Const.HEARTBEAT)){
				
				UpdatePeerAddr();
				
				/*
				Log.e(TAG, "GetHeartCount="+app.GetHeartCount());
				if(app.GetHeartCount()==3){
					SendCmdToServer("N"+app.GetMtkaddr()+","+ app.Getbackground().GetUsername());
					app.SetHeartCountZero();
				}else{
					UpdatePeerAddr();
					app.SetAddHeartCount();
				}
				*/
				
	        }
		}else{
			StartMainActivity();//�����̨����ɱ��������̨����
		}
		
	}
	
	private void StartMainActivity() {//����������ں�̨��ǰ̨��ʾ
		
		if (!Const.ServiceIsWorking("com.guanjiangjun2.service.BackgroundServer", context)) {
			Intent it = new Intent(context, BackgroundServer.class);
			context.startService(it);
			
		}
	}
	
	private void SendHeartBeatToServer(){
		if ( !("".equals(app.Getbackground().GetUsername()))){
			SendCmdToServer("E" + app.Getbackground().GetUsername());
			Log.e(TAG, "HeartMessageReceiver ������������");
		}else{
			//MyApplication.username=MyApplication.sharepre.GetSharePre().getString(Const.USERNAME, "");
			app.Getbackground().GetDataFromXml();
		}
	}
	
	private void ConnectMtk(){
		if ( !("".equals(app.Getbackground().GetUsername()))){
			SendCmdToServer("Q" + app.Getbackground().GetUsername());
			Log.e(TAG, "HeartMessageReceiver ����mtk");
		}else{
			app.Getbackground().GetDataFromXml();
		}
	}
	
	private void UpdatePeerAddr(){
		if(!("".equals(app.GetMtkaddr()))){
			SendCmdToServer("K" + app.GetMtkaddr()+","+ ((app.GetIsOpenGps())?1:0));
			//Log.e(TAG, "HeartMessageReceiver ���¶Է���ַ");
			Log.e(TAG, "HeartMessageReceiver ���¶Է���ַ"+app.GetMtkaddr());
		}else{
			app.Getbackground().SendCmdToServer("Q"+app.Getbackground().GetUsername());
		}
	}
	
	private void CheckMtkOnline(){
		if(!("".equals(app.GetMtkaddr()))){
			SendCmdToServer("C" + app.GetMtkaddr() + "CONLINE");
			Log.e(TAG, "HeartMessageReceiver ���߼������");
		}else{
			app.Getbackground().SendCmdToServer("Q"+app.Getbackground().GetUsername());
		}
		
	}
	
	private void SendCmdToServer(String str){
		
		app.Getbackground().SendCmdToServer(str);
		
	}

}
