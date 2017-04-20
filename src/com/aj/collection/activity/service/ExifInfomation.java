package com.aj.collection.activity.service;

import java.io.IOException;

import android.media.ExifInterface;

/**
 * Exif信息类，实现Exif的写入并实现数字水印
 * @author Administrator
 *
 */
public class ExifInfomation extends ExifInterface
{

	public ExifInfomation(String filename) throws IOException
	{
		super(filename);
		// TODO Auto-generated constructor stub
	}
	//获得拍照时间
	public String mGetDate()
	{
		String time="";
		time=getAttribute(ExifInterface.TAG_DATETIME);
		return time;
	}
	//获得经纬度信息
	public String mGetGps()
	{
		String gps="";
		gps=getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
			+getAttribute(ExifInterface.TAG_GPS_LATITUDE);
		return gps;
	}
	//设置经纬度信息
	public void mSetGps(String longitude,String latitude) throws IOException
	{
		
		setAttribute(TAG_GPS_LONGITUDE, longitude);
		setAttribute(TAG_GPS_LATITUDE, latitude);
		saveAttributes();
	}
}

