package com.guanjiangjun2.util;

import java.net.DatagramSocket;
import java.net.SocketException;

import com.guanjiangjun2.socket.SocketInputThread;
import com.guanjiangjun2.socket.SocketOutputThread;

import android.content.Context;

public class CreateInputOutputThread {
	
	
	private Context context;
	private DatagramSocket socket;
	private SocketInputThread inputthread;
	private SocketOutputThread outputthread;
	private MyApplication app;
	private final static String TAG = "com.guanjiangjun.socket";
	public CreateInputOutputThread(Context context) {
		this.context = context;
		app = (MyApplication) context.getApplicationContext();
	}

	public void CreateSocketThread() {
		CreateSocket();
	}
	
	public SocketInputThread GetSocketInputThread(){
		return inputthread;
	}
	
	public SocketOutputThread GetSocketOutputThread(){
		return outputthread;
	}
	
	private void CreateSocket() {
		SocketInitializeThread mThread=new SocketInitializeThread();
		mThread.start();
	}

	class SocketInitializeThread extends Thread {

		@Override
		public void run() {
			try {
				socket=new DatagramSocket(3456);
				app.SetDatagramSocket(socket);
				outputthread=new SocketOutputThread(socket,context);
				outputthread.start();
				app.SetSocketOutputThread(outputthread);
				inputthread=new SocketInputThread(socket, context);
				inputthread.start();
				app.SetSocketInputThread(inputthread);
				app.Getbackground().SetSocketThread(inputthread, outputthread);
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		
	}
}
