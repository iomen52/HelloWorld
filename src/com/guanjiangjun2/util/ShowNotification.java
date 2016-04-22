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
		// 创建一个NotificationManager的引用  
		NotificationManager notificationManager = (NotificationManager)   
		    context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		
		// 定义Notification的各种属性  
		Notification notification =new Notification(R.drawable.ic_logo_96,  
		        context.getString(R.string.popup_dialog_z9), System.currentTimeMillis());  
		notification.flags |= Notification.FLAG_ONGOING_EVENT; // 将此通知放到通知栏的"Ongoing"即"正在运行"组中  
		notification.flags |= Notification.FLAG_NO_CLEAR; // 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用  
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;  
		notification.defaults = Notification.DEFAULT_LIGHTS;  
		notification.ledARGB = Color.BLUE;  
		notification.ledOnMS =5000;  
		
		// 设置通知的事件消息  
		CharSequence contentTitle =context.getString(R.string.popup_dialog_z9); // 通知栏标题  
		CharSequence contentText =context.getString(R.string.popup_dialog_into_app); // 通知栏内容  
		Intent notificationIntent =new Intent(context, MainActivity.class); // 点击该通知后要跳转的Activity  
		PendingIntent contentItent = PendingIntent.getActivity(context, 0,  
		        notificationIntent, 0);  
		notification.setLatestEventInfo(context, contentTitle, contentText,  
		        contentItent);  
		  
		// 把Notification传递给NotificationManager  
		notificationManager.notify(6789, notification);  
		
	}
}
