package com.guanjiangjun2.util;

import com.example.guanjiangjun2.R;
import com.guanjiangjun2.activity.SettingActivity;
import com.guanjiangjun2.activity.SettingActivity.PriorityListener;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PasswordDialog extends Dialog{

	public PasswordDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public PasswordDialog(Context context, int theme) {  
        super(context, theme);  
    }  

	public static class Builder { 
		private Context context;
		private MyApplication app;
		private CustomDialog mdialog;
		private EditText PasswordOneEdit;
		private EditText PasswordTwoEdit;
		private EditText OriginalPasswordOneEdit;
		private Button PasswordOkButton;
		private Button PasswordCancelButton;
		private ProgressDialog progressDialog;
		private TimeOutClass timeoutphone;
		private PriorityListener listener;
		public Builder(Context context,PriorityListener listener) {  
            this.context = context;
            this.listener=listener;
        } 
		public CustomDialog create() {  
			LayoutInflater inflater = (LayoutInflater) context  
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
            // instantiate the dialog with the custom Theme  
            final CustomDialog dialog = new CustomDialog(context,R.style.Dialog);
            View layout = inflater.inflate(R.layout.password_layout, null); 
            dialog.addContentView(layout, new LayoutParams(  
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));  
            app = (MyApplication)context.getApplicationContext();
            mdialog=dialog;
            PasswordOneEdit=(EditText)layout.findViewById(R.id.PasswordOneEdit);
            PasswordTwoEdit=(EditText)layout.findViewById(R.id.PasswordTwoEdit);
            OriginalPasswordOneEdit=(EditText)layout.findViewById(R.id.OriginalPasswordOneEdit);
            
            PasswordOkButton=(Button)layout.findViewById(R.id.PasswordOkButton);
            PasswordCancelButton=(Button)layout.findViewById(R.id.PasswordCancelButton);
            
            PasswordOkButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					String psdone,psdtwo,orgpsd;
					orgpsd=OriginalPasswordOneEdit.getText().toString();
					psdone=PasswordOneEdit.getText().toString();
					psdtwo=PasswordTwoEdit.getText().toString();
					if(psdone.equals(psdtwo) && "".equals(psdone)){
						Toast.makeText(context, context.getString(R.string.password_dialog_newpsd_null), Toast.LENGTH_SHORT).show();
						return;
					}else if(!psdone.equals(psdtwo)){
						Toast.makeText(context, context.getString(R.string.password_dialog_psd_imparity), Toast.LENGTH_SHORT).show();
						return;
					}else if(psdone.length()>16 || psdone.length()<6){
						Toast.makeText(context, context.getString(R.string.password_dialog_psd_len), Toast.LENGTH_SHORT).show();
						return;
					}else if("".equals(orgpsd)){
						Toast.makeText(context, context.getString(R.string.password_dialog_oldpsd_null), Toast.LENGTH_SHORT).show();
						return;
					}else if(orgpsd.equals(psdone)){
						Toast.makeText(context, context.getString(R.string.password_dialog_oldnew_imparity), Toast.LENGTH_SHORT).show();
						return;
					}
					SendPasswordMessage("U"+ app.Getbackground().GetUsername() + "," + orgpsd + "," + psdone, Const.FAILPASSWORD);
					progressDialog =ProgressDialog.show(context, null, context.getString(R.string.all_activity_send));
				}
			});
            
            PasswordCancelButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					app.SetTopActivityHandler(null);
					listener.SetActivityHandle();
					dialog.cancel();
				}
			});
            
			app.SetTopActivityHandler(mHandler);
            dialog.setContentView(layout);  
            dialog.setCancelable(false);
            return dialog;  
		}
		
		private void SendPasswordMessage(String str,int msg) {
			timeoutphone=Const.SendMessageFunc(app, mHandler, str, msg);
		}
		
		Handler mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case Const.RECVPASSWORD:
					Const.StopTimeOut(timeoutphone);
					Const.CloseDialog(progressDialog);
					Toast.makeText(context, R.string.password_dialog_true, Toast.LENGTH_SHORT).show();
					app.Getsharepre().EditPutString(Const.PASSWORD, "");//本地密码为空
					app.Getsharepre().EditPutBoolean(Const.SAVEPASSWORD, false);//保存密码为空
					app.Getsharepre().EditPutBoolean(Const.AUTOLOGIN, false);//自动登录为空
					//app.SetTopActivityHandler(null);
					//listener.SetActivityHandle();
					Const.GotoLogin(app, context);
					mdialog.cancel();
					break;
				case Const.OCLPFAIL:
					Const.StopTimeOut(timeoutphone);
					Toast.makeText(context, context.getString(R.string.password_dialog_oldpsd_error), Toast.LENGTH_SHORT).show();
					OriginalPasswordOneEdit.setText("");
					Const.CloseDialog(progressDialog);
					break;
				case Const.FAILPASSWORD:// 超时
					Const.StopTimeOut(timeoutphone);
					Toast.makeText(context, R.string.all_activity_failsend, Toast.LENGTH_SHORT).show();
					Const.CloseDialog(progressDialog);
					break;
				}
			}
		};
		
		private void ResetLogin(){
			
		}
	}
}
