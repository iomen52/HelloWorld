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
            //�����ȥ�磨������  
			//Toast.makeText(context, "����", Toast.LENGTH_SHORT).show();
		}else{  
            //������android�ĵ���ò��û��ר�����ڽ��������action,���ԣ���ȥ�缴����  
			if (!isListen) {
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Service.TELEPHONY_SERVICE);
				tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
				// ����һ��������
			}
		}  
	}

	
	PhoneStateListener listener = new PhoneStateListener() {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// TODO Auto-generated method stub
			// state ��ǰ״̬ incomingNumber,ò��û��ȥ���API
			isListen = true;// �ѳɹ���Ӽ���
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				//Toast.makeText(context, "�Ҷ�", Toast.LENGTH_SHORT).show();
				if(app.Getbackground()!=null && !Const.GetStandbyMode(app)){
					//Toast.makeText(context, "�Ҷϵ绰,�������߼��", Toast.LENGTH_SHORT).show();
					app.Getbackground().StartDeviceOpinion();
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				//Toast.makeText(context, "����", Toast.LENGTH_SHORT).show();
				if(IsPhoneNetwork()&& app.Getbackground()!=null){
					app.Getbackground().StopDeviceOpinion();
				}
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				//Toast.makeText(context, "����", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	private boolean IsPhoneNetwork(){
		
		ConnectivityManager connManager  = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
        NetworkInfo gprs = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if((gprs!=null && gprs.isConnected() && gprs.isAvailable())){
        	//Toast.makeText(context, "ʹ��GPRS", Toast.LENGTH_SHORT).show();
        	
        	return true;
        }
        if((wifi!=null && wifi.isConnected() && wifi.isAvailable())){
        	//Toast.makeText(context, "ʹ��wifi", Toast.LENGTH_SHORT).show();
        	return false;
        }
        return false;
	}
}
