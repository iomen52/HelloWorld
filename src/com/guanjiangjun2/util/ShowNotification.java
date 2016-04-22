package com.guanjiangjun2.util;

import com.example.guanjiangjun2.R;
import com.guanjiangjun2.activity.MainActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

public class ShowNotification {
	
	private Context context;
	public ShowNotification(Context context){
		this.context=context;
		CreateNotification();
	}
	
	private void CreateNotification(){
		// ����һ��NotificationManager������  
		NotificationManager notificationManager = (NotificationManager)   
		    context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		
		// ����Notification�ĸ�������  
		Notification notification =new Notification(R.drawable.ic_logo_96,  
		        context.getString(R.string.popup_dialog_z9), System.currentTimeMillis());  
		notification.flags |= Notification.FLAG_ONGOING_EVENT; // ����֪ͨ�ŵ�֪ͨ����"Ongoing"��"��������"����  
		notification.flags |= Notification.FLAG_NO_CLEAR; // �����ڵ����֪ͨ���е�"���֪ͨ"�󣬴�֪ͨ�������������FLAG_ONGOING_EVENTһ��ʹ��  
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;  
		notification.defaults = Notification.DEFAULT_LIGHTS;  
		notification.ledARGB = Color.BLUE;  
		notification.ledOnMS =5000;  
		
		// ����֪ͨ���¼���Ϣ  
		CharSequence contentTitle =context.getString(R.string.popup_dialog_z9); // ֪ͨ������  
		CharSequence contentText =context.getString(R.string.popup_dialog_into_app); // ֪ͨ������  
		Intent notificationIntent =new Intent(context, MainActivity.class); // �����֪ͨ��Ҫ��ת��Activity  
		PendingIntent contentItent = PendingIntent.getActivity(context, 0,  
		        notificationIntent, 0);  
		notification.setLatestEventInfo(context, contentTitle, contentText,  
		        contentItent);  
		  
		// ��Notification���ݸ�NotificationManager  
		notificationManager.notify(6789, notification);  
		
	}
}
