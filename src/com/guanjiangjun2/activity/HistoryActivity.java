package com.guanjiangjun2.activity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.example.guanjiangjun2.R;
import com.guanjiangjun2.util.Const;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class HistoryActivity extends Activity {

	private RadioGroup DateTimeRadioGroup;
	private RadioGroup DataModeRadioGroup;
	private EditText EndDateText;
	private EditText EndTimeText;
	private EditText StartDateText;
	private EditText StartTimeText;
	private Button SelectBtn;
	private int curmode=Const.GPSMODE;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_layout);

		DateTimeRadioGroup = (RadioGroup) findViewById(R.id.DateTimeRadioGroup);
		DataModeRadioGroup = (RadioGroup) findViewById(R.id.DataModeRadioGroup);
		EndDateText = (EditText) findViewById(R.id.EndDateText);
		EndTimeText = (EditText) findViewById(R.id.EndTimeText);
		StartDateText = (EditText) findViewById(R.id.StartDateText);
		StartTimeText = (EditText) findViewById(R.id.StartTimeText);
		SelectBtn = (Button) findViewById(R.id.SelectBtn);
		
		EndDateText.setOnClickListener(new BtnOnClickListener());
		EndTimeText.setOnClickListener(new BtnOnClickListener());
		StartDateText.setOnClickListener(new BtnOnClickListener());
		StartTimeText.setOnClickListener(new BtnOnClickListener());
		SelectBtn.setOnClickListener(new BtnOnClickListener());

		DateTimeRadioGroup
				.setOnCheckedChangeListener(new GroupCheckedChangeListener());
		DataModeRadioGroup.setOnCheckedChangeListener(new GroupCheckedChangeListener());
		// InitTimeEditText();
		DateTimeRadioGroup.check(R.id.TadayCheckRadio);
		SetEditTextEnable(false);
	}

	private class BtnOnClickListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			if (view.getId() == R.id.EndDateText) {
				ShowDatePickDailog(EndDateText);
			} else if (view.getId() == R.id.EndTimeText) {
				ShowTimePickDailog(EndTimeText);
			} else if (view.getId() == R.id.StartDateText) {
				ShowDatePickDailog(StartDateText);
			} else if (view.getId() == R.id.StartTimeText) {
				ShowTimePickDailog(StartTimeText);
			} else if (view.getId() == R.id.SelectBtn) {
				String starttime=StartDateText.getText().toString()+" "+ StartTimeText.getText().toString();
				String endtime=EndDateText.getText().toString()+" "+ EndTimeText.getText().toString();
				if(!DateTimeCompare(starttime,endtime)){
					Toast.makeText(HistoryActivity.this, getString(R.string.history_activity_comparator_time),Toast.LENGTH_SHORT).show();
					return ;
				}
				String sql;
				sql="select * from history where updatetime<datetime('"+endtime+"')"
						+" and updatetime>datetime('"+starttime+"')"
						+ " and mode="+curmode;
				if (Const.GetCountFromSqlite(HistoryActivity.this,sql) > 2) {
					GotoOtherActivity(HistoryShowActivity.class);
				} else {
					Toast.makeText(HistoryActivity.this, getString(R.string.history_activity_no_data),
							Toast.LENGTH_SHORT).show();
				}
				
			}
			
		}

	}

	private boolean DateTimeCompare(String starttime,String endtime){
		DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Log.e("HistoryActivity", "HistoryActivity starttime="+starttime);
		Log.e("HistoryActivity", "HistoryActivity endtime="+endtime);
		try {
			Date date1 = df.parse(starttime);
			Date date2 = df.parse(endtime);
		
			long diff=date2.getTime()-date1.getTime();

			if(diff>0)
				return true;
			else
				return false;
			//Toast.makeText(this, "DateTimeCompare "+diff, Toast.LENGTH_SHORT).show();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	private void ShowDatePickDailog(final EditText editid) {
		// TODO Auto-generated method stub
		final Calendar calendar1 = Calendar.getInstance();
		DatePickerDialog dateDialog = new DatePickerDialog(this, new OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				
				String mm = "";
				String dd = "";
				if (monthOfYear <= 9) {
					mm = "0" + (monthOfYear + 1);
				} else {
					mm = String.valueOf(monthOfYear + 1);
				}
				if (dayOfMonth <= 9) {
					dd = "0" + dayOfMonth;
				} else {
					dd = String.valueOf(dayOfMonth);
				}
				editid.setText(String.valueOf(year) + "-" + mm + "-" + dd);
				
			}
		}, calendar1.get(Calendar.YEAR), calendar1.get(Calendar.MONTH),
				calendar1.get(Calendar.DAY_OF_MONTH));
		dateDialog.show();
	}

	private void ShowTimePickDailog(final EditText editid) {
		final Calendar calendar1 = Calendar.getInstance();
		TimePickerDialog timedialog=new TimePickerDialog(this, new OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker arg0, int hour, int minute) {
				// TODO Auto-generated method stub
				String strhour,strminute;
				if(hour<=9)
					strhour="0"+hour;
				else
					strhour=""+hour;
				
				if(minute<=9)
					strminute="0"+minute;
				else
					strminute=""+minute;
				editid.setText(strhour+":"+strminute+":"+"00");
			}
		}, calendar1.get(Calendar.HOUR_OF_DAY),calendar1.get(Calendar.MINUTE),true);
		timedialog.show();
	}
	
	private void GotoOtherActivity(Class<?> cls) {
		Intent intent = new Intent();
		intent.setClass(HistoryActivity.this, cls);
		intent.putExtra("starttime", StartDateText.getText().toString()
				+ StartTimeText.getText().toString());
		intent.putExtra("endtime", EndDateText.getText().toString()
				+ EndTimeText.getText().toString());
		intent.putExtra("mode", curmode);
		HistoryActivity.this.startActivity(intent);
	}

	private class GroupCheckedChangeListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(RadioGroup group, int num) {
			// TODO Auto-generated method stub
			if (num == R.id.TadayCheckRadio) {
				EndDateText.setText(GetCurData(1));
				StartDateText.setText(GetCurData(1));
				SetTimeEditText();
				SetEditTextEnable(false);
			} else if (num == R.id.YesterdayCheckRadio) {
				EndDateText.setText(GetCurData(2));
				StartDateText.setText(GetCurData(2));
				SetTimeEditText();
				SetEditTextEnable(false);
			} else if (num == R.id.MoreCheckRadio) {
				SetEditTextEnable(true);
			}else if (num == R.id.GpsCheckRadio) {
				curmode=Const.GPSMODE;
			}else if (num == R.id.LbsCheckRadio) {
				curmode=Const.LBSMODE;
			}
		}

	}

	private String GetCurData(int num) {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new java.util.Date();
		if (num == 1) {

		} else if (num == 2) {
			date.setDate(date.getDate() - 1);
		}
		return sDateFormat.format(date);
	}

	private void SetTimeEditText() {
		StartTimeText.setText("00:00:00");
		EndTimeText.setText("24:00:00");
	}

	private void InitTimeEditText() {
		EndDateText.setText(GetCurData(1));
		StartDateText.setText(GetCurData(1));
		StartTimeText.setText("00:00:00");
		EndTimeText.setText("24:00:00");
	}

	private void SetEditTextEnable(boolean bool) {
		StartDateText.setEnabled(bool);
		StartTimeText.setEnabled(bool);
		EndDateText.setEnabled(bool);
		EndTimeText.setEnabled(bool);
	}

}
