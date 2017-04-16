package com.aj.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;


public class UserInfo {

    public final static String USER_TYPE_UNKNOWN = "0";
    public final static String USER_TYPE_TEACHER = "11";
    public final static String USER_TYPE_MANAGER = "12";
    public final static String USER_TYPE_FATHER = "1";
    public final static String USER_TYPE_MOTHER = "2";

    public String getId() {
        return mId;
    }

    private String mId;// 电话

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public void setmUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    private String mUserId;
    private String mName;

    public String getUserId() {
        return mUserId;
    }

    public void setId(String id) {
        mId = id;
    }

    public void setName(String name) {
        mName = name;
    }


}
