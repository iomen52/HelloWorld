package com.guanjiangjun2.util;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.example.guanjiangjun2.R;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class UpdateElementAdapter  extends BaseAdapter {

	private Context context;
	private Handler mHandler;
    //private List<UpdateElement> lists;
	ArrayList<MKOLUpdateElement> localMapList;
    private LayoutInflater layoutInflater;
    private TextView update;
    private TextView cityname;
    private TextView ratio;
	private Button start;
    private Button remove;
    public UpdateElementAdapter(Handler mHandler,Context context, ArrayList<MKOLUpdateElement> localMapList) {
        this.context = context;
        this.mHandler = mHandler;
        this.localMapList = localMapList;
        layoutInflater = LayoutInflater.from(context);
    }
    
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return localMapList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return localMapList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.offlinemap_loc_item, null);
        }
        //注意findViewById的时候，要使用convertView的这个方法，因为是在它里面进行控件的寻找
        cityname = (TextView) convertView.findViewById(R.id.title);
        ratio = (TextView) convertView.findViewById(R.id.ratio);
        update = (TextView) convertView.findViewById(R.id.update);
        start=(Button) convertView.findViewById(R.id.start);
        remove=(Button) convertView.findViewById(R.id.remove);
        //将数据与控件进行绑定
        cityname.setText(localMapList.get(position).cityName);
        //Log.e("UpdateElementAdapter", "UpdateElementAdapter ratio="+ratio);
        ratio.setText(localMapList.get(position).ratio+"%");
        update.setText(localMapList.get(position).update?context.getString(R.string.update_element_renewable):context.getString(R.string.update_element_newest));
        if(localMapList.get(position).update){
        	start.setText(context.getString(R.string.update_element_update));
        	start.setOnClickListener(new BtnOnClickListener(localMapList.get(position).cityID,2));
        }else{
        	if(localMapList.get(position).ratio<100){
        		start.setText(context.getString(R.string.update_element_continue));
        		start.setEnabled(true);
        		start.setOnClickListener(new BtnOnClickListener(localMapList.get(position).cityID,3));
        	}
        	else{
        		start.setText(context.getString(R.string.update_element_start));
        		start.setEnabled(false);
        	}
        }
        remove.setOnClickListener(new BtnOnClickListener(localMapList.get(position).cityID,1));
        
        
        return convertView;
	}
	private class BtnOnClickListener implements OnClickListener {

		private int position;
		private int option;//1:删除 2：更新 3：继续
		public BtnOnClickListener(int position,int option){
			this.position=position;
			this.option=option;
		}
		
		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			Message message = new Message();  
			if (view.getId() == R.id.remove) {
                message.what = Const.REMOVEELEMENT;   
			}else if (view.getId() == R.id.start) {
				if(option==2)
					message.what = Const.UPDATEELEMENT;
				else if(option==3)
					message.what = Const.CONTINUEELEMENT;
			}
			message.arg1=this.position;
            if(!mHandler.sendMessage(message)){
            	mHandler.sendMessage(message);
            }
		}
		
	}

}
