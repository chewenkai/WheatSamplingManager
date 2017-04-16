package com.aj.bean;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.aj.tools.StringUtils;

public class ImageInfo implements Parcelable {
	final public static int PICTURE=0;
	final public static int VIDEO=1;
	final public static int SIGN=2;
	final public static int DEFAULT=-1;
	final public static int VIDEOBMP=3;

	String mUrl="";//服务器图片的url
	boolean mIsLocal=false;//是否是本地图片
	String mLocalPath="";//本地图片的地址路径

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	Bitmap bitmap;//图片类

	public int getMediaType() {
		return mediaType;
	}

	public void setMediaType(int mediaType) {
		this.mediaType = mediaType;
	}

	int mediaType=ImageInfo.DEFAULT;//设置媒体类型

	public int getDrwable() {
		return drwable;
	}

	public void setDrwable(int drwable) {
		this.drwable = drwable;
	}

	int drwable=0;

	public boolean isDrwable() {
		return isDrwable;
	}

	boolean isDrwable=false;

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}

	public String getThumbUrl() {
		String tUrl = mUrl;
		if (mUrl != null) {
			int idx = mUrl.lastIndexOf(".");
			if(idx != -1)
				tUrl = mUrl.substring(0, idx) + "_t" + mUrl.substring(idx);
		}
		return tUrl;
	}


	public String getPrefixLocalPath() {
		return "file://" + mLocalPath;
	}
	public String getLocalPath() {
		return mLocalPath;
	}

	public void setLocalPath(String path) {
		if(path != null)
			mIsLocal = true;
		mLocalPath = path;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mUrl);
		dest.writeString(mLocalPath);
		dest.writeString(String.valueOf(mIsLocal));

	}

	public static final Creator<ImageInfo> CREATOR =
			new Creator<ImageInfo>() {
		public ImageInfo createFromParcel(Parcel in) {
			return new ImageInfo(in);
		}

		public ImageInfo[] newArray(int size) {
			return new ImageInfo[size];
		}
	};

	private ImageInfo(Parcel in) {
		mUrl = in.readString();
		mLocalPath = in.readString();
		mIsLocal = Boolean.valueOf(in.readString());
	}
	public ImageInfo(String file){
		mLocalPath=file;
		isDrwable=false;
	}

	/**
	 * 设置媒体模式
	 * @param Type
	 */
	public ImageInfo(int Type){
		mediaType=Type;
	}

	public ImageInfo(Bitmap bitmap){
		this.bitmap=bitmap;
	}

	public boolean isLocal() {
		// 
		return mIsLocal;
	}

	public boolean hasData() {
		return !StringUtils.isEmpty(mUrl) && !"0".equals(mUrl);
	}
	
	public String getValidURL(){
		if(!mUrl.equals("")){
			return mUrl;
		}else if(!mLocalPath.equals("")){
			return  mLocalPath;
		}else if(drwable!=0){
			return String.valueOf(drwable);
		}else {
			return null;
		}
	}

}