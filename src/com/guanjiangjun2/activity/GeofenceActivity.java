package com.guanjiangjun2.activity;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.guanjiangjun2.R;
import com.guanjiangjun2.util.Const;
import com.guanjiangjun2.util.MyApplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class GeofenceActivity extends Activity {

	/* �ٶȵ�ͼ */
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;

	/* ��ͼ��λ���� */
	public LocationClient mLocationClient;
	public BDLocationListener myListener = new MyLocationListener();
	private LocationMode tempMode = LocationMode.Hight_Accuracy;
	private String tempcoor = "bd09ll";
	private SeekBar seekBar_Radius;
	private TextView textView_radius;
	private Button button_reduce;
	private Button button_increase;
	private Button button_location;
	private RelativeLayout CoordinateRelative;
	private TextView GeofenceInfoView;
	private boolean IsHave=false;
	private int mfold = 100;
	private int marker=1;//1: ����λ��  2: С��λ�� 3: ����ģʽ
	private double mlat,mlng;
	private MyApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		SDKInitializer.initialize(getApplicationContext());
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geofence_layout);
		app = (MyApplication) getApplication();

		seekBar_Radius = (SeekBar) findViewById(R.id.seekBar_Radius);
		button_reduce = (Button) findViewById(R.id.button_reduce);
		button_increase = (Button) findViewById(R.id.button_increase);
		button_location = (Button) findViewById(R.id.button_location);
		textView_radius = (TextView) findViewById(R.id.textView_radius);
		GeofenceInfoView = (TextView) findViewById(R.id.GeofenceInfoView);
		CoordinateRelative = (RelativeLayout) findViewById(R.id.CoordinateRelative);
		
		button_reduce.setOnClickListener(new BtnOnClickListener());
		button_increase.setOnClickListener(new BtnOnClickListener());
		button_location.setOnClickListener(new BtnOnClickListener());
		seekBar_Radius.setOnSeekBarChangeListener(new SeekBarChangeListener());

		// ��ȡ��ͼ�ؼ�����
		mMapView = (MapView) findViewById(R.id.bmapView);		
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);// ��ͨ��ͼ
		mBaiduMap.setOnMapStatusChangeListener(changelistener);// Ų����ͼ�����¼�

		// ���ض�λ
		mLocationClient = new LocationClient(this);
		mLocationClient.registerLocationListener(myListener);
		
		InitLocation();
		mLocationClient.start();
		InitGeoFence();
	}
	
	private class SeekBarChangeListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekbar, int arg1, boolean arg2) {
			/*
			if (arg1 < 10)
				arg1 = 10;
			else
				arg1 += 1;
			*/
			arg1 += 1;
			if (arg1 * 100 < 1000)
				textView_radius.setText(getString(R.string.string_radius) + arg1 * 100 + getString(R.string.string_metre));
			else
				textView_radius.setText(getString(R.string.string_radius) + arg1 / 10 + "." + arg1 % 10
						+ getString(R.string.string_kilometre));
			
			mfold = arg1 * 100;
			InitRadius(mfold);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekbar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekbar) {
			// TODO Auto-generated method stub

		}

	}

	private class BtnOnClickListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			if (view.getId() == R.id.button_increase) {// �Ӻ�
				app.Getsharepre().EditPutString(Const.KEY_GEOLAT,
						mBaiduMap.getMapStatus().target.latitude + "");
				app.Getsharepre().EditPutString(Const.KEY_GEOLON,
						mBaiduMap.getMapStatus().target.longitude + "");
				app.Getsharepre().EditPutInt(Const.KEY_DISTANCE, mfold);
				SetRelativeVisibility(false);
				CreateTextOption(mBaiduMap.getMapStatus().target.latitude, mBaiduMap.getMapStatus().target.longitude);
				IsHave=true;
				Toast.makeText(GeofenceActivity.this, getString(R.string.geofence_activity_create_fence),
						Toast.LENGTH_SHORT).show();
			} else if (view.getId() == R.id.button_reduce) {// ����
				app.Getsharepre().EditPutString(Const.KEY_GEOLAT, "");
				app.Getsharepre().EditPutString(Const.KEY_GEOLON, "");
				app.Getsharepre().EditPutInt(Const.KEY_DISTANCE, 0);
				SetRelativeVisibility(true);
				IsHave=false;
				mBaiduMap.clear();
				Toast.makeText(GeofenceActivity.this, getString(R.string.geofence_activity_delete_fence),
						Toast.LENGTH_SHORT).show();
			}else if(view.getId() == R.id.button_location){
				if(marker==1){//����λ��
					button_location.setBackgroundResource(R.drawable.location_car);
					if(mlat!=0)
						GotoGeoFencePoint(mlat, mlng);
				}else if(marker==2){//С��λ��
					button_location.setBackgroundResource(R.drawable.location_person);
				}
				if(marker==2)
					marker=1;
				else
					marker+=1;
			}
		}

	}

	private  void InitGeoFence(){
		int mgeoDistance=app.Getsharepre().GetSharePre().getInt(Const.KEY_DISTANCE, 0);
		if(mgeoDistance==0){
			GeofenceInfoView.setText(getString(R.string.geofence_activity_noset_fence));
			return;
		}
		double geolat=Double.valueOf(app.Getsharepre().GetSharePre().getString(Const.KEY_GEOLAT, "").trim()).doubleValue();
		double geolng=Double.valueOf(app.Getsharepre().GetSharePre().getString(Const.KEY_GEOLON, "").trim()).doubleValue();
		
		IsHave=true;
		CreateRadius(geolat, geolng, mgeoDistance);
		//mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(14));
		
		CreateTextOption(geolat, geolng);
		GotoGeoFencePoint(geolat, geolng);
		
		SetRelativeVisibility(false);
	}
	
	private void CreateTextOption(double lat, double lon){
		LatLng llText = new LatLng(lat, lon);  
		//��������Option���������ڵ�ͼ���������  
		OverlayOptions textOption = new TextOptions()  
		    .fontSize(36)  
		    .fontColor(0xFFFF3333)  
		    .text(getString(R.string.geofence_area))  
		    .rotate(0)  
		    .position(llText);  
		//�ڵ�ͼ����Ӹ����ֶ�����ʾ  
		mBaiduMap.addOverlay(textOption);
	}
	
	private void GotoGeoFencePoint(double lat, double lon){
		LatLng ll = new LatLng(lat,lon);
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		mBaiduMap.animateMapStatus(u);
	}
	
	private void SetRelativeVisibility(boolean bool){
		if(bool){
			CoordinateRelative.setVisibility(View.VISIBLE);
			GeofenceInfoView.setText(getString(R.string.geofence_activity_noset_fence));
		}
		else{
			CoordinateRelative.setVisibility(View.GONE);
			int mgeoDistance=app.Getsharepre().GetSharePre().getInt(Const.KEY_DISTANCE, 0);
			if(mgeoDistance>1000)
				GeofenceInfoView.setText(getString(R.string.geofence_activity_haveset_fence)+mgeoDistance / 1000 + "." + mgeoDistance % 10+getString(R.string.string_kilometre));
			else
				GeofenceInfoView.setText(getString(R.string.geofence_activity_haveset_fence)+mgeoDistance +getString(R.string.string_metre));
		}
		seekBar_Radius.setEnabled(bool);
		button_increase.setEnabled(bool);
		button_location.setEnabled(bool);
		if(bool){
			button_location.getBackground().setAlpha(255);
			button_increase.getBackground().setAlpha(255);
		}else{
			button_location.getBackground().setAlpha(100);
			button_increase.getBackground().setAlpha(100);
		}
	}
	
	private void InitRadius(int fivefold){
		
		mBaiduMap.clear();
		CreateRadius(mBaiduMap.getMapStatus().target.latitude, mBaiduMap.getMapStatus().target.longitude, fivefold);
	}
	
	private void CreateRadius(double lat, double lon,int fivefold) {

		//mBaiduMap.clear();
		// ����Բ
		OverlayOptions polygonOption = new CircleOptions()
				.center(new LatLng(lat, lon))
				.radius(fivefold)
				.fillColor(0xAAFFFFAA)
				.stroke(new Stroke(2, 0xAAFF3333));

		mBaiduMap.addOverlay(polygonOption);
	}

	OnMapStatusChangeListener changelistener = new OnMapStatusChangeListener() {
		public void onMapStatusChangeStart(MapStatus status) {
			if(!IsHave)
				InitRadius(mfold);
		}
		public void onMapStatusChange(MapStatus status) {
			if(!IsHave)
				InitRadius(mfold);
		}
		public void onMapStatusChangeFinish(MapStatus status) {
			if(!IsHave)
				InitRadius(mfold);
		}
	};

	// ���ض�λ��ʼ������
	private void InitLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // �Ƿ��GPS
		option.setLocationMode(tempMode);// ���ö�λģʽ
		option.setProdName("LocationDemo");
		option.setCoorType(tempcoor);// ���ö�λ�������
		option.setAddrType("all");
		int span = 5000;

		option.setScanSpan(span);// ����ÿ�ζ�λʱ����
		option.setIsNeedAddress(false);// �����Ƿ���Ҫ���������

		mLocationClient.setLocOption(option);
	}

	// ����λ�ü�����
	private class MyLocationListener implements BDLocationListener {
		MyLocationData locData;
		boolean isFirstLoc = true;

		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			if (location == null || mMapView == null)
				return;

			locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
					.direction(100)
					.latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();

			mBaiduMap.setMyLocationData(locData);
			
			mlat=location.getLatitude();
			mlng=location.getLongitude();
			
			mBaiduMap.setMyLocationEnabled(true);
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
				//mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16));
			}
		}

	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
            if (myListener != null) {
                mLocationClient.unRegisterLocationListener(myListener);
            }
        }
		mMapView.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mMapView.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mMapView.onResume();
	}
}
