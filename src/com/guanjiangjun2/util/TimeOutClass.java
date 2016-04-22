package com.guanjiangjun2.util;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TimeOutClass {

	private Handler handler;
	private int msg;
	private String msgstr;
	private boolean status=false;
	private ResultTimerTask resulttask;
	private Timer resulttimer;  
	private SendMessageThread thread;
	private MyApplication app;
	private final static String TAG = "TimeOutClass";
	public TimeOutClass(MyApplication app,Handler handler,String msgstr,int timeout,int msg){
		this.handler=handler;
		this.msg=msg;
		this.app=app;
		if(msgstr!=null){
			this.msgstr=msgstr;
			status=true;
			thread=new SendMessageThread();
			thread.start();
		}
		resulttask=new ResultTimerTask();
		resulttimer=new Timer();
		resulttimer.schedule(resulttask, timeout);
	}
	
	public void stopTimer(){ 
		status=false;
		if(thread!=null)
		{
			thread.interrupt();
		}
		if (resulttimer != null) {  
			resulttimer.cancel();  
			resulttimer = null;  
        }  
        if (resulttask != null) {  
        	resulttask.cancel();  
        	resulttask = null;  
        } 
        
	}
	
    
	class ResultTimerTask extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			status=false;
			if(thread!=null)
			{
				thread.interrupt();
			}
			if (handler != null) {
				Message message = new Message();
				message.what = msg;
				if(!handler.sendMessage(message)){
					handler.sendMessage(message);
				}
				//Log.e(TAG, "handler="+handler);
			}
		}
    	
    }
	
	
	class SendMessageThread extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			//super.run();
			while (status) {
					
				try {
					sleep(2000);
					if(app.Getbackground()!=null && app.Getbackground().GetOutputThread()!=null){
						app.Getbackground().GetOutputThread().addMsgToSendList(msgstr);
						Log.e(TAG, "TimeOutClass send="+msgstr);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
	}

}
