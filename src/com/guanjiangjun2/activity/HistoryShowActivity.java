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
	/* 百度地图 */
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	/* 地图定位部分 */
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
		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);// 普通地图

		// 本地定位
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
				/*--描点、加入list、显示文字*/
				DrawDotOptions(GetCoordinateConverter(lat,lng));
				pts.add(GetCoordinateConverter(lat,lng));
				DrawTextOptions(GetCoordinateConverter(lat,lng),cursor.getString(cursor.getColumnIndex("updatetime")).trim());
				/*--描点、加入list、显示文字*/
				SetStartEndIcon(GetCoordinateConverter(lat,lng), 1);//开始图片
				GotoHistory(newpoint);
				i=1;
			}
			if(oldpoint!=null){
				if(Const.getDistance(newpoint.longitude, newpoint.latitude, oldpoint.longitude,oldpoint.latitude)>80){
					//大于500米则画出该点，小于则忽略
					/*--描点、加入list、显示文字*/
					DrawDotOptions(GetCoordinateConverter(lat,lng));
					pts.add(GetCoordinateConverter(lat,lng));
					DrawTextOptions(GetCoordinateConverter(lat,lng),cursor.getString(cursor.getColumnIndex("updatetime")).trim());
					/*--描点、加入list、显示文字*/
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
		//构建文字Option对象，用于在地图上添加文字  
		OverlayOptions textOption = new TextOptions()  
		    .fontSize(24)  
		    .fontColor(0xFFF94BD6)  
		    .text(text)  
		    .rotate(0)  
		    .position(point);  
		//在地图上添加该文字对象并显示  
		mBaiduMap.addOverlay(textOption);
	}
	
	private LatLng GetCoordinateConverter(double lat, double lng){
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordType.GPS);
		LatLng sourceLatLng = new LatLng(lat, lng);// 定义坐标点
		converter.coord(sourceLatLng);
		LatLng point = converter.convert();
		return point;
	}
	
	private void SetStartEndIcon(LatLng point,int num) {
		BitmapDescriptor bitmap = null;
		if(num==1){
			bitmap = BitmapDescriptorFactory
					.fromResource(R.drawable.ic_history_start);// 创建图标
		}else if(num==2){
			bitmap = BitmapDescriptorFactory
					.fromResource(R.drawable.ic_history_end);// 创建图标
		}
		OverlayOptions option = new MarkerOptions()// 设置图标到指定位置并设置是否可以拖拽选中
				.position(point).icon(bitmap).zIndex(9).draggable(true);
		mBaiduMap.addOverlay(option);
	}
	
	private void DrawDotOptions(LatLng point) {
		//Log.e("HistoryShowActivity", "DrawDotOptions lat="+point.latitude +"lng="+point.longitude);
		OverlayOptions dotOption = new DotOptions()
				.center(point)
				.radius(8)
				.color(0xAAFF4444);
		// 在地图上添加多边形Option，用于显示
		mBaiduMap.addOverlay(dotOption);
	}

	private void DrawPolyline() {
		OverlayOptions polygonOption = new PolylineOptions().width(6)
				.color(0xAA2222FF)
				.points(pts);
		mBaiduMap.addOverlay(polygonOption);
	}

	// 本地定位初始化函数
	private void InitLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // 是否打开GPS
		option.setLocationMode(tempMode);// 设置定位模式
		option.setProdName("LocationDemo");
		option.setCoorType("bd09ll");// 设置定位结果反馈
		option.setAddrType("all");
		int span = 5000;

		option.setScanSpan(span);// 设置每次定位时间间隔
		option.setIsNeedAddress(false);// 设置是否需要反地理编码

		mLocationClient.setLocOption(option);
	}

	private void GotoHistory(LatLng point) {
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(point);
		mBaiduMap.animateMapStatus(u);
	}

	// 本地位置监听类
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
					// 此处设置开发者获取到的方向信息，顺时针0-360
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
