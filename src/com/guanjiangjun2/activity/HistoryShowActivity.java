package com.guanjiangjun2.activity;

import java.util.ArrayList;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.example.guanjiangjun2.R;
import com.guanjiangjun2.util.Const;
import com.guanjiangjun2.util.MyApplication;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

public class HistoryShowActivity extends Activity {
	/* �ٶȵ�ͼ */
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	/* ��ͼ��λ���� */
	public LocationClient mLocationClient;
	public BDLocationListener myListener = new MyLocationListener();
	private LocationMode tempMode = LocationMode.Hight_Accuracy;

	List<LatLng> pts = new ArrayList<LatLng>();
	private MyApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		//SDKInitializer.initialize(getApplicationContext());
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.historyshow_layout);

		app = (MyApplication) getApplication();
		// ��ȡ��ͼ�ؼ�����
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);// ��ͨ��ͼ

		// ���ض�λ
		/*
		mLocationClient = new LocationClient(this);
		mLocationClient.registerLocationListener(myListener);
		InitLocation();
		mLocationClient.start();
		*/
		
		String starttime = this.getIntent().getStringExtra("starttime");
		String endtime = this.getIntent().getStringExtra("endtime");
		int mode = this.getIntent().getIntExtra("mode", Const.GPSMODE);
		GetCursor(starttime, endtime,mode);
		
		//Toast.makeText(this, starttime, Toast.LENGTH_SHORT).show();
	}

	private void GetCursor(String starttime, String endtime,int mode) {
		String sql="select * from history where updatetime<datetime('"+endtime+"')"
				+" and updatetime>datetime('"+starttime+"') "
				+" and mode="+mode+" order by updatetime asc";
		Cursor cursor = Const.GetCursorResult(HistoryShowActivity.this,sql);
		double lat = 0, lng = 0;
		LatLng oldpoint=null;
		LatLng newpoint=null;
		int i=0;
		while (cursor.moveToNext()) {
			lat = Double.valueOf(cursor.getString(cursor.getColumnIndex("lat")).trim()).doubleValue();
			lng = Double.valueOf(cursor.getString(cursor.getColumnIndex("lng")).trim()).doubleValue();
			
			newpoint=GetCoordinateConverter(lat,lng);
			if(i==0){
				/*--��㡢����list����ʾ����*/
				DrawDotOptions(GetCoordinateConverter(lat,lng));
				pts.add(GetCoordinateConverter(lat,lng));
				DrawTextOptions(GetCoordinateConverter(lat,lng),cursor.getString(cursor.getColumnIndex("updatetime")).trim());
				/*--��㡢����list����ʾ����*/
				SetStartEndIcon(GetCoordinateConverter(lat,lng), 1);//��ʼͼƬ
				GotoHistory(newpoint);
				i=1;
			}
			if(oldpoint!=null){
				if(Const.getDistance(newpoint.longitude, newpoint.latitude, oldpoint.longitude,oldpoint.latitude)>80){
					//����500���򻭳��õ㣬С�������
					/*--��㡢����list����ʾ����*/
					DrawDotOptions(GetCoordinateConverter(lat,lng));
					pts.add(GetCoordinateConverter(lat,lng));
					DrawTextOptions(GetCoordinateConverter(lat,lng),cursor.getString(cursor.getColumnIndex("updatetime")).trim());
					/*--��㡢����list����ʾ����*/
					oldpoint=newpoint;
				}
				
			}else{
				oldpoint=newpoint;
			}
		}
		if (pts.size()>=2) {
			DrawPolyline();
			SetStartEndIcon(newpoint, 2);
			Log.e("HistoryShowActivity", "HistoryShowActivity"+pts.size());
		}
		
	}

	private void DrawTextOptions(LatLng point,String text){
		//��������Option���������ڵ�ͼ���������  
		OverlayOptions textOption = new TextOptions()  
		    .fontSize(24)  
		    .fontColor(0xFFF94BD6)  
		    .text(text)  
		    .rotate(0)  
		    .position(point);  
		//�ڵ�ͼ����Ӹ����ֶ�����ʾ  
		mBaiduMap.addOverlay(textOption);
	}
	
	private LatLng GetCoordinateConverter(double lat, double lng){
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordType.GPS);
		LatLng sourceLatLng = new LatLng(lat, lng);// ���������
		converter.coord(sourceLatLng);
		LatLng point = converter.convert();
		return point;
	}
	
	private void SetStartEndIcon(LatLng point,int num) {
		BitmapDescriptor bitmap = null;
		if(num==1){
			bitmap = BitmapDescriptorFactory
					.fromResource(R.drawable.ic_history_start);// ����ͼ��
		}else if(num==2){
			bitmap = BitmapDescriptorFactory
					.fromResource(R.drawable.ic_history_end);// ����ͼ��
		}
		OverlayOptions option = new MarkerOptions()// ����ͼ�굽ָ��λ�ò������Ƿ������קѡ��
				.position(point).icon(bitmap).zIndex(9).draggable(true);
		mBaiduMap.addOverlay(option);
	}
	
	private void DrawDotOptions(LatLng point) {
		//Log.e("HistoryShowActivity", "DrawDotOptions lat="+point.latitude +"lng="+point.longitude);
		OverlayOptions dotOption = new DotOptions()
				.center(point)
				.radius(8)
				.color(0xAAFF4444);
		// �ڵ�ͼ����Ӷ����Option��������ʾ
		mBaiduMap.addOverlay(dotOption);
	}

	private void DrawPolyline() {
		OverlayOptions polygonOption = new PolylineOptions().width(6)
				.color(0xAA2222FF)
				.points(pts);
		mBaiduMap.addOverlay(polygonOption);
	}

	// ���ض�λ��ʼ������
	private void InitLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // �Ƿ��GPS
		option.setLocationMode(tempMode);// ���ö�λģʽ
		option.setProdName("LocationDemo");
		option.setCoorType("bd09ll");// ���ö�λ�������
		option.setAddrType("all");
		int span = 5000;

		option.setScanSpan(span);// ����ÿ�ζ�λʱ����
		option.setIsNeedAddress(false);// �����Ƿ���Ҫ���������

		mLocationClient.setLocOption(option);
	}

	private void GotoHistory(LatLng point) {
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(point);
		mBaiduMap.animateMapStatus(u);
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
				// mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16));
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
