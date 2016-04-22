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
     * 构造函数，进行初始化
     * 
     * @param context
     * @param lists
     */
    public OfflineMapAdapter(Context context, ArrayList<MKOLSearchRecord> allMapList) {
        this.context = context;
        this.allMapList = allMapList;
        layoutInflater = LayoutInflater.from(context);
    }
 
    // 获得长度，一般返回数据的长度即可
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
     * 最重要的方法，每一个item生成的时候，都会执行这个方法，在这个方法中实现数据与item中每个控件的绑定
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // convertView对象就是item的界面对象，只有为空的时候我们才需要重新赋值一次，这样可以提高效率，如果有这个对象的话，系统会自动复用
        //item_listview就是自定义的item的布局文件
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.drawable.offlinemap_all_item, null);
        }
        //注意findViewById的时候，要使用convertView的这个方法，因为是在它里面进行控件的寻找
        cityname = (TextView) convertView.findViewById(R.id.textView_title);
        datasize = (TextView) convertView.findViewById(R.id.textView_size);
        //将数据与控件进行绑定
        cityname.setText(allMapList.get(position).cityName);
        datasize.setText(Const.formatDataSize(allMapList.get(position).size));
        
        return convertView;
    }
    
 
}
