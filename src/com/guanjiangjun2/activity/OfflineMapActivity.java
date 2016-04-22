package com.guanjiangjun2.activity;

import java.util.ArrayList;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.example.guanjiangjun2.R;
import com.guanjiangjun2.util.Const;
import com.guanjiangjun2.util.OfflineMapAdapter;
import com.guanjiangjun2.util.UpdateElementAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class OfflineMapActivity extends Activity implements
		MKOfflineMapListener {

	private ArrayList<MKOLSearchRecord> allMapList = null;
	private ArrayList<MKOLUpdateElement> localMapList = null;

	private MKOfflineMap mOffline = null;
	private ListView allcitylist;
	private ListView LocList;

	private Button clButton;
	private Button localButton;
	private LinearLayout citylist_layout;
	private LinearLayout localmap_layout;
	private int downcityid=0;
	private UpdateElementAdapter updateadapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.offlinemap);
		//SDKInitializer.initialize(getApplicationContext());
		allcitylist = (ListView) findViewById(R.id.allcitylist);
		LocList = (ListView) findViewById(R.id.localmaplist);

		clButton = (Button) findViewById(R.id.clButton);
		localButton = (Button) findViewById(R.id.localButton);
		citylist_layout = (LinearLayout) findViewById(R.id.citylist_layout);
		localmap_layout = (LinearLayout) findViewById(R.id.localmap_layout);

		InitLists();
		
	}

	private void InitLists() {
		mOffline = new MKOfflineMap();
		mOffline.init(this);
		allMapList = mOffline.getOfflineCityList();
		localMapList = mOffline.getAllUpdateInfo();
		if(allMapList==null){
			allMapList=new ArrayList<MKOLSearchRecord>();
		}
		if(localMapList==null){
			localMapList=new ArrayList<MKOLUpdateElement>();
		}
		// 设置适配器
		allcitylist.setAdapter(new OfflineMapAdapter(this, allMapList));
				
		updateadapter=new UpdateElementAdapter(mHandler,this, localMapList);
			
		LocList.setAdapter(updateadapter);
		// 设置监听
		allcitylist.setOnItemClickListener(new ListViewOnItemClickListener());
	}

	private class ListViewOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> view, View arg1, int position,
				long arg3) {
			// TODO Auto-generated method stub
			if(Const.isNetworkAvailable(OfflineMapActivity.this)){
				if(OptionNetIsGprs()){
					Toast.makeText(OfflineMapActivity.this, getString(R.string.main_activity_gprs_hint),Toast.LENGTH_SHORT).show();
				}else{
					downcityid=((MKOLSearchRecord) view.getItemAtPosition(position)).cityID;
					ShowPopupDialog(((MKOLSearchRecord) view.getItemAtPosition(position)).cityName);
				}
			}else{
				Toast.makeText(OfflineMapActivity.this, getString(R.string.main_activity_nonetwork),Toast.LENGTH_SHORT).show();
			}
		}

	}
	
	private boolean OptionNetIsGprs(){
		ConnectivityManager connManager  = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);  
		NetworkInfo gprs = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		return gprs.isConnected();
	}
	
	private void ShowPopupDialog(String cityname){
		
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage(getString(R.string.main_activity_down)+cityname+getString(R.string.main_activity_offline_down));
		builder.setTitle(getString(R.string.main_activity_offline));
		builder.setPositiveButton(getString(R.string.dialog_phone_ok), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Thread downthread=new Thread(new Runnable() {  
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mOffline.start(downcityid);
					}
				}, "downthread");
				downthread.start();
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(getString(R.string.dialog_phone_cancel), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case Const.REMOVEELEMENT:
					mOffline.pause(msg.arg1);
					mOffline.remove(msg.arg1);
					updateView();
					break;
				case Const.UPDATEELEMENT:
					if(Const.isNetworkAvailable(OfflineMapActivity.this)){
						if(OptionNetIsGprs()){
							Toast.makeText(OfflineMapActivity.this, getString(R.string.main_activity_gprs_hint),Toast.LENGTH_SHORT).show();
						}else{
							mOffline.remove(msg.arg1);
							mOffline.start(msg.arg1);
							updateView();
						}
					}else{
						Toast.makeText(OfflineMapActivity.this, getString(R.string.main_activity_nonetwork),Toast.LENGTH_SHORT).show();
					}
					break;
				case Const.CONTINUEELEMENT:
					if(Const.isNetworkAvailable(OfflineMapActivity.this)){
						if(OptionNetIsGprs()){
							Toast.makeText(OfflineMapActivity.this, getString(R.string.main_activity_gprs_hint),Toast.LENGTH_SHORT).show();
						}else{
							//mOffline.remove(msg.arg1);
							mOffline.start(msg.arg1);
							updateView();
						}
					}else{
						Toast.makeText(OfflineMapActivity.this, getString(R.string.main_activity_nonetwork),Toast.LENGTH_SHORT).show();
					}
					break;
			}
		}
		
	};

	public void clickCityListButton(View view) {
		citylist_layout.setVisibility(View.VISIBLE);
		localmap_layout.setVisibility(View.GONE);
		clButton.setBackgroundResource(R.drawable.list_tab2);
		localButton.setBackgroundResource(R.drawable.list_tab);
		clButton.setTextColor(Color.rgb(0xff, 0xff, 0xff));
		localButton.setTextColor(Color.rgb(0x0, 0x0, 0x0));
	}

	public void clickLocalMapListButton(View view) {
		localmap_layout.setVisibility(View.VISIBLE);
		citylist_layout.setVisibility(View.GONE);
		localButton.setBackgroundResource(R.drawable.list_tab2);
		clButton.setBackgroundResource(R.drawable.list_tab);
		clButton.setTextColor(Color.rgb(0x0, 0x0, 0x0));
		localButton.setTextColor(Color.rgb(0xff, 0xff, 0xff));
	}


	private boolean DetectionAllTastFinish(){
		ArrayList<MKOLUpdateElement> updateMapList = null;
		updateMapList=mOffline.getAllUpdateInfo();
		if (null != updateMapList) {
			for (MKOLUpdateElement upel : updateMapList) {
				if (upel.ratio < 100) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//mOffline.destroy();
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if(DetectionAllTastFinish()){
			Log.e("onGetOfflineMapState", "onBackPressed =");
			mOffline.destroy();
		}
	}

	@Override
	public void onGetOfflineMapState(int type, int state) {
		// TODO Auto-generated method stub
		switch (type) {
		case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
			MKOLUpdateElement update = mOffline.getUpdateInfo(state);
			// 处理下载进度更新提示
			if (update != null) {
				Log.e("onGetOfflineMapState", "onGetOfflineMapState="+update.cityName + update.ratio);
				if(update.ratio==100){
					if (DetectionAllTastFinish()) {
						int num = mOffline.importOfflineData();
						//mOffline.destroy();
						Toast.makeText(OfflineMapActivity.this, getString(R.string.main_activity_down_complete),Toast.LENGTH_SHORT).show();
					}
				}
				updateView();
			}
		}
			break;
		case MKOfflineMap.TYPE_NEW_OFFLINE:
			// 有新离线地图安装
			break;
		case MKOfflineMap.TYPE_VER_UPDATE:
			// 版本更新提示
			break;
		}
	}
	
	/**
	 * 更新状态显示
	 */
	public void updateView() {
		localMapList.clear();
		if(null==mOffline.getAllUpdateInfo()){
			
		}else{
			localMapList.addAll(mOffline.getAllUpdateInfo());
		}
		//Log.e("OfflineMapActivity", "updateView ="+mOffline.getAllUpdateInfo());
		if (localMapList == null) {
			localMapList = new ArrayList<MKOLUpdateElement>();
		}
		updateadapter.notifyDataSetChanged();
	}

}
