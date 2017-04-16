package com.aj.adapters;

/**
 * Created by kevin on 15-10-6.
 */
public class ParentListItem {

    //每条显示的构造方法
    private final String mTitle;
    private final String mRightText;
    private final int mResource;

    public ParentListItem(String title, String rightText, int resource) {
        mResource = resource;
        mTitle = title;
        mRightText=rightText;
    }

    public String getTitle() {
        return mTitle;
    }
    public String getmRightText() {
        return mRightText;
    }
    public int getResource() {
        return mResource;
    }
}

