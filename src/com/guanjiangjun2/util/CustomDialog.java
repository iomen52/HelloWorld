package com.guanjiangjun2.util;

import com.example.guanjiangjun2.R;
import com.guanjiangjun2.activity.SettingActivity;
import com.guanjiangjun2.activity.SettingActivity.PriorityListener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CustomDialog  extends Dialog{

	public CustomDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public CustomDialog(Context context, int theme) {  
        super(context, theme);  
    }  
	public static class Builder { 
		private Context context;
		private TextView DialogPhoneView;
		private EditText DialogPhoneEdit;
		private Button DialogPhoneOkButton;
        private Button DialogPhoneCancelButton;
        private TimeOutClass timeoutphone;
        private ProgressDialog progressDialog;
        private MyApplication app;
        private CustomDialog mdialog;
        private PriorityListener listener;
        private int num;
        private String phone="";
		public Builder(Context context,int num,PriorityListener listener) {  
            this.context = context;  
            this.num=num;
            this.listener=listener;
        } 
		public CustomDialog create() {  
			LayoutInflater inflater = (LayoutInflater) context  
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
            // instantiate the dialog with the custom Theme  
            final CustomDialog dialog = new CustomDialog(context,R.style.Dialog);
            View layout = inflater.inflate(R.layout.dialog_phone_layout, null); 
            dialog.addContentView(layout, new LayoutParams(  
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));  
            app = (MyApplication)context.getApplicationContext();
            mdialog=dialog;
            DialogPhoneView=(TextView)layout.findViewById(R.id.DialogPhoneView);
            DialogPhoneEdit=(EditText)layout.findViewById(R.id.DialogPhoneEdit);
            DialogPhoneOkButton=(Button)layout.findViewById(R.id.DialogPhoneOkButton);
            DialogPhoneCancelButton=(Button)layout.findViewById(R.id.DialogPhoneCancelButton);
            
            DialogPhoneOkButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					String phonestr;
					phonestr=DialogPhoneEdit.getText().toString();
					if(num!=5 && phonestr.length()!=11){
						Toast.makeText(context, R.string.phone_dialog_errorphone, Toast.LENGTH_SHORT).show();
						return;
					}
					if(phonestr.equals(phone)){
						//Toast.makeText(context, "phonestr="+phonestr+"phone="+phone, Toast.LENGTH_SHORT).show();
						app.SetTopActivityHandler(null);
						listener.SetActivityHandle();
						dialog.cancel();
						return;
					}
					if(num==1)
						SendGuardMessage("C"+ app.GetMtkaddr() + "CKJKZONE" + phonestr, Const.FAILKJKZONE);
					else if(num==2)
						SendGuardMessage("C"+ app.GetMtkaddr() + "CKJKZTWO" + phonestr, Const.FAILKJKZTWO);
					else if(num==3)
						SendGuardMessage("I"+ app.Getbackground().GetUsername() + "," + phonestr, Const.FAILMTKPHONE);
					else if(num==4){
						SendGuardMessage("H"+ app.Getbackground().GetUsername() + "," + phonestr, Const.FAILANDROID);
					}else if(num==5){
						if(Integer.valueOf(phonestr.trim()).intValue()>20 || Integer.valueOf(phonestr.trim()).intValue()<3 ){
							Toast.makeText(context, context.getString(R.string.custom_dialog_heart_limit), Toast.LENGTH_SHORT).show();
							return;
							
						}else{
							SendGuardMessage("C"+ app.GetMtkaddr() + "CHEART" + phonestr, Const.FAILHEART);
						}
					}
					progressDialog =ProgressDialog.show(context, null, context.getString(R.string.all_activity_send));
				}
			});
			
			DialogPhoneCancelButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					app.SetTopActivityHandler(null);
					listener.SetActivityHandle();
					dialog.cancel();
				}
			});
			app.SetTopActivityHandler(mHandler);
			GetDataFromXml();
            dialog.setContentView(layout);  
            dialog.setCancelable(false);
            return dialog;  
		}
		private void SendGuardMessage(String str,int msg) {
			timeoutphone=Const.SendMessageFunc(app, mHandler, str, msg);
		}
		Handler mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case Const.RECVKJKZONE:// 设置成功
				case Const.RECVKJKZTWO:// 设置成功
				case Const.RECVMTKPHONE:
				case Const.RECVANDROID:
				case Const.RECVHEART:
					SetRECVEvent(msg.what);
					break;
				case Const.FAILKJKZONE:// 超时
				case Const.FAILKJKZTWO:// 超时
				case Const.FAILMTKPHONE:
				case Const.FAILANDROID:
				case Const.FAILHEART:
					SetFailEvent(msg.what);
					break;
				}
			}
		};
		
		private void SetRECVEvent(int msg){
			Const.StopTimeOut(timeoutphone);
			if(msg==Const.RECVKJKZONE){
				Toast.makeText(context, R.string.phone_dialog_phoneone, Toast.LENGTH_SHORT).show();
				app.Getsharepre().EditPutString(Const.KEY_BURGLARFIRST,DialogPhoneEdit.getText().toString());
			}else if(msg==Const.RECVKJKZTWO){
				Toast.makeText(context, R.string.phone_dialog_phonetwo, Toast.LENGTH_SHORT).show();
				app.Getsharepre().EditPutString(Const.KEY_BURGLARSECOND,DialogPhoneEdit.getText().toString());
			}else if(msg==Const.RECVMTKPHONE){
				Toast.makeText(context, R.string.phone_dialog_phonemtk, Toast.LENGTH_SHORT).show();
				app.Getsharepre().EditPutString(Const.KEY_SETMTKPHONE,DialogPhoneEdit.getText().toString());
			}
			else if(msg==Const.RECVANDROID){
				Toast.makeText(context, R.string.phone_dialog_phoneandroid, Toast.LENGTH_SHORT).show();
				app.Getsharepre().EditPutString(Const.KEY_SETANDROIDPHONE,DialogPhoneEdit.getText().toString());
				PopUpDialog();
			}
			else if(msg==Const.RECVHEART){
				Toast.makeText(context, context.getString(R.string.custom_dialog_heart_set), Toast.LENGTH_SHORT).show();
				app.Getbackground().SetHeartQuantity(Integer.valueOf(DialogPhoneEdit.getText().toString().trim()).intValue());
				app.Getsharepre().EditPutString(Const.KEY_SETHEARTQUANTITY,DialogPhoneEdit.getText().toString());
				app.Getbackground().StopAlarmManager();
				app.Getbackground().StartAlarmManager();
			}
			Const.CloseDialog(progressDialog);
			app.SetTopActivityHandler(null);
			listener.SetActivityHandle();
			mdialog.cancel();
		}
		
		private void ShutdownOrResetAsync() {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					app.SetShutdownAsync(false);
					//ExecuteRece();
				}
			}, 1000 * 15);
		}
		/*
		private boolean GetStandbyMode(){
			boolean bool=false;
			bool=app.Getsharepre().GetSharePre().getBoolean(Const.STANDBYMODE, false) 
			return bool;
		}
		*/
		private void PopUpDialog(){
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(context.getString(R.string.custom_dialog_oneway_audio));
			builder.setTitle(context.getString(R.string.custom_dialog_reset));
			builder.setPositiveButton(
					context.getString(R.string.custom_dialog_reset_now),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							SendSystemReset();
							app.SetShutdownAsync(true);
							ShutdownOrResetAsync();
							app.SetMtkonline(false);
							app.Getbackground().StopDeviceOpinion();
							dialog.dismiss();
						}
					});
			builder.setNegativeButton(context.getString(R.string.custom_dialog_reset_manual), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
			builder.create().show();
		}
		
		private void SendSystemReset(){
			Toast.makeText(context, "system reset", Toast.LENGTH_SHORT).show();
			Const.SendMessageFunc(app, null, "C" + app.GetMtkaddr() + "CSYSTEMRESET", 0);
		}
		
		private void SetFailEvent(int msg){
			Const.StopTimeOut(timeoutphone);
			Toast.makeText(context, R.string.all_activity_failsend, Toast.LENGTH_SHORT).show();
			Const.CloseDialog(progressDialog);
		}
		
		private void GetDataFromXml() {
			if(num==1){
				phone=app.Getsharepre().GetSharePre()
						.getString(Const.KEY_BURGLARFIRST, "");
				DialogPhoneView.setText(R.string.phone_dialog_stateone);
			}else if(num==2){
				phone=app.Getsharepre().GetSharePre()
						.getString(Const.KEY_BURGLARSECOND, "");
				DialogPhoneView.setText(R.string.phone_dialog_statetwo);
			}else if(num==3){//3设置mtk号码
				phone=app.Getsharepre().GetSharePre()
						.getString(Const.KEY_SETMTKPHONE, "");
				DialogPhoneView.setText(R.string.phone_dialog_statethree);
			}else if(num==4){//4设置android号码
				phone=app.Getsharepre().GetSharePre().getString(Const.KEY_SETANDROIDPHONE, "");
				DialogPhoneView.setText(R.string.phone_dialog_statefour);
			}else if(num==5){//4设置心跳频率
				phone=app.Getsharepre().GetSharePre().getString(Const.KEY_SETHEARTQUANTITY, "20");
				DialogPhoneView.setText(context.getString(R.string.custom_dialog_heart_setting));
			}
			
			if(!("".equals(phone))){
				DialogPhoneEdit.setText(phone);
			}
		}
	}
}
