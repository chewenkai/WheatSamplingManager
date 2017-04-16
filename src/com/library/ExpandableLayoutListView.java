/***********************************************************************************
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2014 Robin Chutaux
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ***********************************************************************************/
package com.library;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import java.io.File;

/**
 * Author :    Chutaux Robin
 * Date :      9/17/2014
 */
public class ExpandableLayoutListView extends ListView {

    private ExpandableLayoutListView expandList = this;
    public ExpandableLayoutItem currentExpandableLayout;//指向当前打开的Item或如果现在没有打开就指向最后一次打开的
    public int currentPosition = 0;//last click position
    private ExpandableLayoutListViewItemListener listener;
    private Context mContext;
    public boolean isParentListView = true;

    public boolean clickFromDelete = false;

    /**
     * Configures a ExpandableSelectorListener instance to be notified when different collapse/expand
     * animations be performed.
     */
    public void setExpandableLayoutListViewItemListener(ExpandableLayoutListViewItemListener listener) {
        this.listener = listener;
    }

    public ExpandableLayoutListView(Context context) {
        super(context);
        mContext = context;
        setOnScrollListener(new OnExpandableLayoutScrollListener());
    }

    public ExpandableLayoutListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setOnScrollListener(new OnExpandableLayoutScrollListener());
    }

    public ExpandableLayoutListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        setOnScrollListener(new OnExpandableLayoutScrollListener());
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent me) {
//        try {
//            if (isParentListView) {
//
//                int action = me.getAction();
//                boolean toreturn = false;
//                ////            onTouchEvent(me);
//                switch (action) {
//                    case MotionEvent.ACTION_DOWN:
//                        onTouchEvent(me);
//                        toreturn = false;
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        onTouchEvent(me);
//                        toreturn = false;
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        onTouchEvent(me);
//                        if (expandList.currentExpandableLayout == null || !expandList.currentExpandableLayout.isOpened()) {
//
//                            if (me.getHistorySize() > 0) {
//                                if (Math.abs(me.getHistoricalY(0) - me.getY()) > 5) {
//                                    toreturn = true;
//                                } else {
//                                    toreturn = false;
//                                }
//                            } else
//                                toreturn = false;
//                            //                        toreturn = true;
//                        } else {
//                            toreturn = false;
//                        }
//                        break;
//                    default:
//                        toreturn = false;
//                }
//                return toreturn;
//
//            } else {
//                int action = me.getAction();
//                boolean toreturn = true;
//                switch (action) {
//                    case MotionEvent.ACTION_DOWN:
//                        onTouchEvent(me);
//                        toreturn = false;
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        toreturn = false;
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        onTouchEvent(me);
//                        if (me.getHistorySize() > 0) {
//                            if (Math.abs(me.getHistoricalY(0) - me.getY()) > 5) {
//                                toreturn = true;
//                            } else {
//                                toreturn = false;
//                            }
//                        } else
//                            toreturn = false;
//
//                        break;
//                    default:
//                        onTouchEvent(me);
//                        toreturn = false;
//                }
//                return toreturn;
//            }
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }


    @Override
    public boolean onInterceptTouchEvent(final MotionEvent me) {

        try {
            if (isParentListView) {

                int action = me.getAction();
                boolean toreturn = false;
                ////            onTouchEvent(me);
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        onTouchEvent(me);
                        toreturn = false;
                        break;
                    case MotionEvent.ACTION_UP:
//                        onTouchEvent(me);
                        toreturn = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (expandList.currentExpandableLayout == null || !expandList.currentExpandableLayout.isOpened()) {
                            onTouchEvent(me);
                            if (me.getHistorySize() > 0) {
                                if (Math.abs(me.getHistoricalY(0) - me.getY()) > 5) {
                                    toreturn = true;
                                } else {
                                    toreturn = false;
                                }
                            } else
                                toreturn = false;
                            //                        toreturn = true;
                        } else {
                            toreturn = false;
                        }
                        break;
                    default:
                        toreturn = false;
                }
                return toreturn;

            } else {
                int action = me.getAction();
                boolean toreturn;
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        onTouchEvent(me);
                        toreturn = false;
                        break;
                    case MotionEvent.ACTION_UP:
//                        onTouchEvent(me);
                        toreturn = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        onTouchEvent(me);
                        if (me.getHistorySize() > 0) {
                            if (Math.abs(me.getHistoricalY(0) - me.getY()) > 5) {
                                toreturn = true;
                            } else {
                                toreturn = false;
                            }
                        } else
                            toreturn = false;

                        break;
                    default:
//                            onTouchEvent(me);
                        toreturn = false;
                }
                return toreturn;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return true;
        }

    }

    @Override
    public boolean performItemClick(View view, int position, long id) {
//        if(isParentListView) {
//            this.position = position;
//            for (int index = 0; index < getChildCount(); ++index) {
//                if (index != (position - getFirstVisiblePosition())) {
//                    ExpandableLayoutItem currentExpandableLayout = (ExpandableLayoutItem) getChildAt(index).findViewWithTag(ExpandableLayoutItem.class.getName());
//                    currentExpandableLayout.setFirstListview(expandList);
//                    currentExpandableLayout.hide();
//                }
//            }
//
//            ExpandableLayoutItem expandableLayout = (ExpandableLayoutItem) getChildAt(position - getFirstVisiblePosition()).findViewWithTag(ExpandableLayoutItem.class.getName());
//            expandableLayout.setFirstListview(expandList);
//            currentExpandableLayout=expandableLayout;
//            if (expandableLayout.isOpened()) {
//                expandableLayout.hide();
//            } else {
//                expandableLayout.show();
//            }
//        }
        return super.performItemClick(view, position, id);
    }

    /**
     * 指定位置展开子界面
     *
     * @param position
     */
    public void expandChildView(int position) {

        currentPosition = position;

        for (int index = 0; index < getChildCount(); ++index) {
            if (index != (position - getFirstVisiblePosition())) {
                ExpandableLayoutItem currentExpandableLayout = (ExpandableLayoutItem) getChildAt(index).findViewWithTag(ExpandableLayoutItem.class.getName());
                currentExpandableLayout.setFirstListview(expandList);
                currentExpandableLayout.hide();
            }
        }

        ExpandableLayoutItem expandableLayout = (ExpandableLayoutItem) getChildAt(position - getFirstVisiblePosition()).findViewWithTag(ExpandableLayoutItem.class.getName());
        expandableLayout.setFirstListview(expandList);
        currentExpandableLayout = expandableLayout;
        if (expandableLayout.isOpened()) {
            expandableLayout.hide();
        } else {
            expandableLayout.show();
        }


    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        if (!(l instanceof OnExpandableLayoutScrollListener))
            throw new IllegalArgumentException("OnScrollListner must be an OnExpandableLayoutScrollListener");

        super.setOnScrollListener(l);
    }


    public class OnExpandableLayoutScrollListener implements OnScrollListener {
        private int scrollState = 0;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            this.scrollState = scrollState;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (scrollState != SCROLL_STATE_IDLE) {
//                for (int index = 0; index < getChildCount(); ++index) {
//                    ExpandableLayoutItem currentExpandableLayout = (ExpandableLayoutItem) getChildAt(index).findViewWithTag(ExpandableLayoutItem.class.getName());
//                    currentExpandableLayout.setFirstListview(expandList);
//                    if (currentExpandableLayout.isOpened() && index != (position - getFirstVisiblePosition())) {
//                        currentExpandableLayout.hideNow();
//                        descendViewHeight(currentExpandableLayout);
//                    } else if (!currentExpandableLayout.getCloseByUser() && !currentExpandableLayout.isOpened() && index == (position - getFirstVisiblePosition())) {
//                        currentExpandableLayout.showNow();
//                        increaseViewHeight(currentExpandableLayout);
//                    }
//                }
            }
        }
    }

    public void notifyExpand() {
        if (hasListenerConfigured()) {
            listener.onExpand();
        }
    }

    public void notifyCollapse() {
        if (hasListenerConfigured()) {
            listener.onCollapse();
        }
    }

    public void notifyExpanded() {
        if (hasListenerConfigured()) {
            listener.onExpanded();
        }
    }

    public void notifyCollapsed() {
        if (hasListenerConfigured()) {
            listener.onCollapsed();
        }
    }

    private boolean hasListenerConfigured() {
        return listener != null;
    }


//    /**
//     * 点击外层listview后将任务名称传递
//     */
//    public void passTheDirAndInitItem(int i) {
//
//        currentExpandableLayout = (ExpandableLayoutItem) getChildAt(i).findViewWithTag(ExpandableLayoutItem.class.getName());
//        currentExpandableLayout.setSampleFileDir(getTaskDir().listFiles()[i]);
//        currentExpandableLayout.initChildListView(expandList);
//    }

//    /**
//     * 传递文件的路径并且为childlistview在、初始化和刷新listview的Item
//     */
//    public void RefeshItem() {
//
//        if(currentExpandableLayout!=null&&expandList.currentExpandableLayout.isOpened()) {
//            currentExpandableLayout.refreshChildListView(expandList);
//            expandList.increaseViewHeight(currentExpandableLayout);//刷新外层可扩展listview的高度
//        }
//    }

    /**
     * 更新外层listview的高度
     */
    public void refreshExpandableListViewHeight() {
//        ViewGroup.LayoutParams params = expandList.getLayoutParams();
//
//        params.height = getTotalheightOfListView() + expandList.currentExpandableLayout.getTotalHeightofItem();
//
//        expandList.setLayoutParams(params);
    }

    /**
     * 计算外层Item的高度
     *
     * @return
     */
    public int getTotalheightOfListView() {
        ArrayAdapter arrayAdapter = (ArrayAdapter) expandList.getAdapter();
        //调整listview的高度
        int totalHeight = 0;
        for (int i = 0; i < arrayAdapter.getCount(); i++) {
            View listItem = arrayAdapter.getView(i, null, expandList);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        totalHeight = totalHeight + (expandList.getDividerHeight() * (arrayAdapter.getCount() - 1));
        return totalHeight;
    }


}
