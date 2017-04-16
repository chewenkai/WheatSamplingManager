package com.aj.bean;

import java.util.Date;

public class Session {
	
	private String mSid = "";
	private Date mCreateTime;
	private Date mLastActiveTime;
	
	public Session(){
		mCreateTime = new Date();
	}
	public Session(String sid){
		mSid = sid;
		mCreateTime = new Date();
	}
	
	public String getSid() {
		return mSid;
	}
	public void setSid(String sid) {
		this.mSid = sid;
	}
	public Date getCreateTime() {
		return mCreateTime;
	}
	public void setCreateTime(Date createTime) {
		this.mCreateTime = createTime;
	}
	public Date getLastActiveTime() {
		return mLastActiveTime;
	}
	public void setLastActiveTime(Date lastActiveTime) {
		this.mLastActiveTime = lastActiveTime;
	}
}
