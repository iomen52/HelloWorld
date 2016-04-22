package com.guanjiangjun2.activity;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.example.guanjiangjun2.R;
import com.guanjiangjun2.util.Const;
import com.guanjiangjun2.util.MyApplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

public class GpsLoctionActivity extends Activity {
	/* �ٶȵ�ͼ */
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	private Marker marker = null;
	private InfoWindow mInfoWindow = null;
	/* ��ͼ��λ���� */
	public LocationClient mLocationClient;
	public BDLocationListener myListener = new MyLocationListener();
	private LocationMode tempMode = LocationMode.Hight_Accuracy;
	private String tempcoor = "bd09ll";
	private TextView AddressView;
	private TextView UpdateView;
	private ImageView UpdateImageView;
	
	private String addres;
	private final static int UPDATETIME = 301;// ����
	private int updatetime = 15;
	private int modenum=0;
	private int curmode=0;
	private GpsLoctionUpdateTimeThread thread;
	
	private int mgeoDistance;
	
	private double mlon,mlat;
	private GeoCoder mSearch;
	private boolean IsBuild=false;
	private MyApplication app;
	private final static String TAG = "GpsLoctionActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		//SDKInitializer.initialize(getApplicationContext());
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gpslocation_layout);
		app=(MyApplication)getApplication();
		AddressView = (TextView) findViewById(R.id.AddressView);
		UpdateView = (TextView) findViewById(R.id.UpdateView);
		UpdateImageView = (ImageView) findViewById(R.id.UpdateImageView);

		// ��ȡ��ͼ�ؼ�����
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);// ��ͨ��ͼ

		// ���ض�λ
		mLocationClient = new LocationClient(this);
		mLocationClient.registerLocationListener(myListener);
		
		InitLocation();
		mLocationClient.start();
		
		mgeoDistance=app.Getsharepre().GetSharePre().getInt(Const.KEY_DISTANCE, 0);
		if(mgeoDistance>0){
			CreateRadius(mgeoDistance);
			IsBuild=true;
		}else{
			IsBuild=false;
		}
		SetImageAnimation();
	}
	
	private void SetBaiduMapMarker(double lat, double lng) {
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordType.GPS);
		LatLng sourceLatLng = new LatLng(lat, lng);// ���������
		converter.coord(sourceLatLng);
		LatLng point = converter.convert();
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.ic_marker_100);// ����ͼ��
		OverlayOptions option = new MarkerOptions()// ����ͼ�굽ָ��λ�ò������Ƿ������קѡ��
				.position(point).icon(bitmap).zIndex(9).draggable(true);
		marker = (Marker) (mBaiduMap.addOverlay(option));
	}

	private void SetImageAnimation() {

		Animation operatingAnim = AnimationUtils.loadAnimation(this,
				R.drawable.animation_set);
		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);
		UpdateImageView.startAnimation(operatingAnim);

	}

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
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();

			mBaiduMap.setMyLocationData(locData);
			
			mBaiduMap.setMyLocationEnabled(true);
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
		}

	}

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {

			switch (msg.what) {
			case UPDATETIME:// ����
				updatetime -= 1;
				UpdateView.setText(updatetime + getString(R.string.gps_activity_update));
				if (updatetime == 1){
					if(mBaiduMap!=null){
						mBaiduMap.clear();
						if(IsBuild)
							CreateRadius(mgeoDistance);
					}
					if (mlon!= 0 || mlat!= 0){
						FindAddress(mlat, mlon);
						SetBaiduMapMarker(mlat, mlon);
						//CreateRadius(mgeoDistance);
						GotoGeoFencePoint(mlat, mlon);
						if(IsBuild)
							AddressView.setText(getString(R.string.gps_activity_postion) +getString(R.string.gpsloction_geofence_haveset)+ "\n" + addres);
						else{
							AddressView.setText(getString(R.string.gps_activity_postion) +getString(R.string.gpsloction_geofence_noset)+ "\n" + addres);
						}
					}
					if(modenum==5){
						modenum=0;
						curmode=0;
						Log.e(TAG, "modenum====0");
					}
					
					modenum+=1;
					Log.e(TAG, "modenum="+modenum);
					updatetime = 15;
				}
				break;
			case Const.UPDATELATLON:
				int mode;
				mode=msg.getData().getInt("mode");
				if(mode==Const.GPSMODE){//GPS����
					modenum=0;
					curmode=Const.GPSMODE;
					Log.e(TAG, "Come GPS data");
					mlon=msg.getData().getDouble("lon");
					mlat=msg.getData().getDouble("lat");
				}
				if(curmode==0 && mode==Const.LBSMODE){//LBS����
					mlon=msg.getData().getDouble("lon");
					mlat=msg.getData().getDouble("lat");
					Log.e(TAG, "Come LBS data");
				}else if(curmode==Const.GPSMODE && mode==Const.LBSMODE){
					
				}
				//Const.GetDistance(mlat, mlon);
				break;
			default:
				break;
			}
		}

	};
	
	
	public static LatLng GpsToBaidumap(Context context,double lat, double lng){
		//SDKInitializer.initialize(context.getApplicationContext());
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordType.GPS);
		LatLng sourceLatLng = new LatLng(lat, lng);// ���������
		//converter.coord(sourceLatLng);
		converter.coord(sourceLatLng);
		LatLng point = converter.convert();
		
		return point;
	}
	
	public static boolean IsOutOfRange(Context context,MyApplication app,double lat, double lng){
		double Distance;
		LatLng baidulatlng;
		//SDKInitializer.initialize(context.getApplicationContext());
		int geoDistance=app.Getsharepre().GetSharePre().getInt(Const.KEY_DISTANCE, 0);
		if (geoDistance != 0) {
			double geolat = Double.valueOf(
					app.Getsharepre().GetSharePre()
							.getString(Const.KEY_GEOLAT, "").trim())
					.doubleValue();
			double geolng = Double.valueOf(
					app.Getsharepre().GetSharePre()
							.getString(Const.KEY_GEOLON, "").trim())
					.doubleValue();
			baidulatlng = GpsToBaidumap(context,lat, lng);
			Distance = Const.getDistance(geolng, geolat, baidulatlng.longitude,
					baidulatlng.latitude);
			if (geoDistance > Distance)
				return false;
			else
				return true;
		}
		return false;
	}
	private void CreateRadius(int fivefold){
		/*
		if(fivefold==0){
			AddressView.setText(getString(R.string.gps_activity_postion)+getString(R.string.gpsloction_geofence_noset));
			IsBuild=false;
			return;
		}
		*/
		//mBaiduMap.clear();
		//AddressView.setText(getString(R.string.gps_activity_postion)+getString(R.string.gpsloction_geofence_haveset));
		
		double geolat=Double.valueOf(app.Getsharepre().GetSharePre().getString(Const.KEY_GEOLAT, "").trim()).doubleValue();
		double geolng=Double.valueOf(app.Getsharepre().GetSharePre().getString(Const.KEY_GEOLON, "").trim()).doubleValue();
		//����Բ
		OverlayOptions polygonOption =new CircleOptions()
			.center(new LatLng(geolat, geolng))
			.radius((int)fivefold)
			.fillColor(0xAAFFFFAA)
			.stroke(new Stroke(2, 0xAAFF3333));
		
		mBaiduMap.addOverlay(polygonOption);
		
		LatLng llText = new LatLng(geolat, geolng);  
		//��������Option���������ڵ�ͼ���������  
		OverlayOptions textOption = new TextOptions()  
		    .fontSize(36)  
		    .fontColor(0xFFFF3333)  
		    .text(getString(R.string.geofence_area))  
		    .rotate(0)  
		    .position(llText);  
		//�ڵ�ͼ����Ӹ����ֶ�����ʾ  
		mBaiduMap.addOverlay(textOption);
		//GotoGeoFencePoint(geolat, geolng);
	}
	
	private void GotoGeoFencePoint(double lat, double lon){
		LatLng ll = GpsToBaidumap(this, lat, lon);
		
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		mBaiduMap.animateMapStatus(u);
	}
	
	private void FindAddress(double lat, double lng){
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(listener);
		mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(new LatLng(lat, lng)));
	}
	
	OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {  
	    public void onGetGeoCodeResult(GeoCodeResult result) {  
	        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {  
	            //û�м��������  
	        }  
	        //��ȡ���������  
	    }  
	 
	    @Override  
	    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {  
	        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {  
	            //û���ҵ��������  
	        }else{
	        	//��ȡ������������  
	        	addres=result.getAddress();
	        }
	    }  
	};
	
	class GpsLoctionUpdateTimeThread extends Thread {

		private boolean flag = true;

		public void GpsLoctionSetFlag(boolean flag) {
			this.flag = flag;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// super.run();
			while (flag) {
				try {
					Thread.sleep(1000);
					//Log.e(TAG, "GpsLoctionActivity UPDATETIME ��ͼ");
					if(mHandler!=null){
						Message msg = new Message();
						msg.what = UPDATETIME;
						mHandler.sendMessage(msg);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
		app.SetTopActivityHandler(null);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mMapView.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		app.SetTopActivityHandler(mHandler);
		if(app.Getbackground()!=null){
			app.Getbackground().OpenGpsCmd();
			//Log.e(TAG, "Gpsloction OpenGpsCmd");
			app.SetIsOpenGps(true);
		}
		thread=new GpsLoctionUpdateTimeThread();
		thread.start(); 
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if(app.Getbackground()!=null){
			app.Getbackground().CloseGpsCmd();
			app.SetIsOpenGps(false);
		}
		if(mSearch!=null)
			mSearch.destroy();
		if(thread!=null){
			thread.GpsLoctionSetFlag(false);
			thread.interrupt();
			thread=null;
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		app.SetTopActivityHandler(null);
		//super.onBackPressed();
		Intent it = new Intent();
		it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		it.setClass(this, MainActivity.class);
		startActivity(it);
		this.finish();
	}

}
