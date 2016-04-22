package com.guanjiangjun2.receiver;

import com.guanjiangjun2.util.Const;
import com.guanjiangjun2.util.MyApplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class PhoneStateReceiver extends BroadcastReceiver {
	
	private static boolean isListen = false;
	private Context context;
	private MyApplication app;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		this.context=context;
		app = (MyApplication) context.getApplicationContext();
		if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){  
            //如果是去电（拨出）  
			//Toast.makeText(context, "拨出", Toast.LENGTH_SHORT).show();
		}else{  
            //查了下android文档，貌似没有专门用于接收来电的action,所以，非去电即来电  
			if (!isListen) {
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Service.TELEPHONY_SERVICE);
				tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
				// 设置一个监听器
			}
		}  
	}

	
	PhoneStateListener listener = new PhoneStateListener() {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// TODO Auto-generated method stub
			// state 当前状态 incomingNumber,貌似没有去电的API
			isListen = true;// 已成功添加监听
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				//Toast.makeText(context, "挂断", Toast.LENGTH_SHORT).show();
				if(app.Getbackground()!=null && !Const.GetStandbyMode(app)){
					//Toast.makeText(context, "挂断电话,启动掉线检测", Toast.LENGTH_SHORT).show();
					app.Getbackground().StartDeviceOpinion();
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				//Toast.makeText(context, "接听", Toast.LENGTH_SHORT).show();
				if(IsPhoneNetwork()&& app.Getbackground()!=null){
					app.Getbackground().StopDeviceOpinion();
				}
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				//Toast.makeText(context, "来电", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	private boolean IsPhoneNetwork(){
		
		ConnectivityManager connManager  = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
        NetworkInfo gprs = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if((gprs!=null && gprs.isConnected() && gprs.isAvailable())){
        	//Toast.makeText(context, "使用GPRS", Toast.LENGTH_SHORT).show();
        	
        	return true;
        }
        if((wifi!=null && wifi.isConnected() && wifi.isAvailable())){
        	//Toast.makeText(context, "使用wifi", Toast.LENGTH_SHORT).show();
        	return false;
        }
        return false;
	}
}
