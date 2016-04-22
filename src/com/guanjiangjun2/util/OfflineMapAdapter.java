package com.guanjiangjun2.util;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.example.guanjiangjun2.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
 
public class OfflineMapAdapter extends BaseAdapter {
 
    private Context context;
    //private List<Thing> lists;
    ArrayList<MKOLSearchRecord> allMapList;
    private LayoutInflater layoutInflater;
    //private ImageView img;
    private TextView cityname;
    private TextView datasize;
 
    /**
     * ���캯�������г�ʼ��
     * 
     * @param context
     * @param lists
     */
    public OfflineMapAdapter(Context context, ArrayList<MKOLSearchRecord> allMapList) {
        this.context = context;
        this.allMapList = allMapList;
        layoutInflater = LayoutInflater.from(context);
    }
 
    // ��ó��ȣ�һ�㷵�����ݵĳ��ȼ���
    @Override
    public int getCount() {
        return allMapList.size();
    }
 
    @Override
    public Object getItem(int position) {
        return allMapList.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    /**
     * ����Ҫ�ķ�����ÿһ��item���ɵ�ʱ�򣬶���ִ����������������������ʵ��������item��ÿ���ؼ��İ�
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // convertView�������item�Ľ������ֻ��Ϊ�յ�ʱ�����ǲ���Ҫ���¸�ֵһ�Σ������������Ч�ʣ�������������Ļ���ϵͳ���Զ�����
        //item_listview�����Զ����item�Ĳ����ļ�
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.drawable.offlinemap_all_item, null);
        }
        //ע��findViewById��ʱ��Ҫʹ��convertView�������������Ϊ������������пؼ���Ѱ��
        cityname = (TextView) convertView.findViewById(R.id.textView_title);
        datasize = (TextView) convertView.findViewById(R.id.textView_size);
        //��������ؼ����а�
        cityname.setText(allMapList.get(position).cityName);
        datasize.setText(Const.formatDataSize(allMapList.get(position).size));
        
        return convertView;
    }
    
 
}
