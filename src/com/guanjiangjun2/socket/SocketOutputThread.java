package com.guanjiangjun2.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.guanjiangjun2.util.Const;
import com.guanjiangjun2.util.MyApplication;

import android.content.Context;
import android.util.Log;


public class SocketOutputThread extends Thread {

	private boolean isStart = true;
	private DatagramSocket mSocket;
	private List<String> sendMsgList;
	private InetAddress serAddress;
	private MyApplication app;
	private final static String TAG = "OutputThread"; 
	public SocketOutputThread(DatagramSocket socket,Context context) {
		this.mSocket = socket;
		sendMsgList = new CopyOnWriteArrayList<String>();
		app = (MyApplication) context.getApplicationContext();
		this.setName(TAG);
	}

	public void setStart() {
		this.isStart = true;
	}
	
	public void setStop() {
		this.isStart = false;
		synchronized (this) {
			notify();
		}
	}

	// 使用socket发送消息
	public void addMsgToSendList(String msg) {

		synchronized (this) {
			this.sendMsgList.add(msg);
			notify();
		}

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			serAddress=InetAddress.getByName(Const.SOCKET_SERVER);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (isStart) {
			for (String msg : sendMsgList) {
				// 发送函数
				try {
					byte data[]=msg.getBytes();
					DatagramPacket pack=new DatagramPacket(data, data.length, serAddress, Const.SOCKET_PORT);
					mSocket.send(pack);
					Log.e(TAG, "SocketOutputThread send="+msg);
					//sendMsgList.remove(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
				sendMsgList.remove(msg);
			}

			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
