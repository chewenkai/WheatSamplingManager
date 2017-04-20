package com.aj.collection.activity;

import java.util.Date;

import com.aj.collection.activity.MyCalendarView.OnItemClickListener;
import com.aj.collection.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class Calendar_View extends Activity
{
	private MyCalendarView calendar;
	private int id;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendar_view);
		
		Intent intent = getIntent();
		id = intent.getIntExtra("btn_id", 0);
		
		//获取日历控件对象
		calendar = (MyCalendarView)findViewById(R.id.calendar);
		//获取日历中点击时的年月 
		String ya = calendar.getYearAndmonth(); 
		//点击上一月 同样返回年月 
		String leftYearAndmonth = calendar.clickLeftMonth(); 
		String[] lya = leftYearAndmonth.split("-");
		//点击下一月
		String rightYearAndmonth = calendar.clickRightMonth(); 
		String[] rya = rightYearAndmonth.split("-");
		//设置控件监听，可以监听到点击的每一天（大家也可以在控件中自行设定）
		calendar.setOnItemClickListener(new OnItemClickListener()
		{
			
			@Override
			public void OnItemClick(Date date)
			{

				int dateYear=date.getYear()+1900;
				int dateMonth=date.getMonth()+1;
				int dateDay=date.getDate();
				String ymd=Integer.toString(dateYear)+"-"+Integer.toString(dateMonth)+"-"+Integer.toString(dateDay);
				Intent i=new Intent();
				i.putExtra("ymd", ymd);
				i.putExtra("btn_id", id);
				setResult(GatherActivity.Companion.getREQUESTCODEFORDATE(), i);
				finish();
			}
		});
	}
}
