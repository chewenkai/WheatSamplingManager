package com.aj.collection.Navigation;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aj.Constant;
import com.aj.collection.R;
import com.aj.collection.tools.Util;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManager;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import at.markushi.ui.CircleButton;

/**
 * 地图跑步主界面
 */
public class MapActivity extends Activity {

    /**
     * MapView 是百度地图主控件
     */
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private UiSettings mUiSettings;
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;

    //百度地图定位
    LocationClientOption.LocationMode tempMode = LocationClientOption.LocationMode.Hight_Accuracy;//定位模式
    private String tempcoor = Constant.COORDINATION_STADARD;//定位坐标标准   bd09ll 百度经纬度标准    "gcj02"国家测绘局标准    "bd09"百度墨卡托标准
    private LocationClient mLocationClient;//定位客户端
    public MyLocationListenner myListener;//定位监听器，会每隔span时间返回一次数据，后面还需要LocationClient注册定位监听器
    boolean isFirstLoc = true;// 是否首次定位
    public Vibrator mVibrator;

    //activity控件
    Dialog dialog;

    TextView naviButton;

    EditText startN, endN;

    RelativeLayout map_view;

    ImageView img_map_overlay;
    CircleButton img_toMyLocation,img_map_offline;

    //数据类型
    LatLng startPoint, endPoint;
    LatLng myLocation;

    //统计标志变量

    private boolean isGPSRunning = false;//GPS是否连接
    String startpoi="",endpoi="";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog = new Dialog(MapActivity.this);

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);


        setContentView(R.layout.activity_map_run);

        //初始化导航
        if (initDirs()) {
            initNavi();
        }

        //初始化View
        initView();

        //设置定位相关
        initLocation();

        //设置地图相关
        initMapView();

        //监听地图点击
        clickListen();




    }

    private void clickListen() {

        BaiduMap.OnMapClickListener listener = new BaiduMap.OnMapClickListener() {
            /**
             * 地图单击事件回调函数
             * @param point 点击的地理坐标
             */
            public void onMapClick(LatLng point) {
                showMapClickDialog(point, "");
            }

            /**
             * 地图内 Poi 单击事件回调函数
             * @param poi 点击的 poi 信息
             */
            public boolean onMapPoiClick(MapPoi poi) {
                showMapClickDialog(poi.getPosition(), poi.getName());
                //Toast.makeText(MapActivity.this,poi.getName(),Toast.LENGTH_SHORT).show();
                return true;
            }
        };
        mBaiduMap.setOnMapClickListener(listener);



    }


    private void initView() {
        map_view = (RelativeLayout) findViewById(R.id.RelativeL_mapview);

        startN = (EditText) findViewById(R.id.map_nav_start);
        endN = (EditText) findViewById(R.id.map_nav_end);

        img_toMyLocation=(CircleButton)findViewById(R.id.img_tomylocation);
        img_map_offline=(CircleButton)findViewById(R.id.img_map_offline);

        naviButton = (TextView) findViewById(R.id.map_nav_button);
        naviButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(startPoint==null||endPoint==null||startN.getText().toString().equals("")||
                        endN.getText().toString().equals("")){
                    Toast.makeText(MapActivity.this,"请点击屏幕选取起点和终点",Toast.LENGTH_LONG).show();
                    return;
                }
                routeplanToNavi(BNRoutePlanNode.CoordinateType.GCJ02);
            }
        });

        img_toMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(myLocation);
                mBaiduMap.animateMapStatus(u);
            }
        });
        img_map_offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            startActivity(new Intent(MapActivity.this,MapOffline.class));
            }
        });
    }


    private void initLocation() {
        // mLocationClient = ((LocationApplication) getApplication()).mLocationClient;
        mLocationClient = new LocationClient(this.getApplicationContext());
        myListener = new MyLocationListenner();
        mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        mLocationClient.registerLocationListener(myListener);//注册定位监听器

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(tempMode);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType(tempcoor);//可选，默认gcj02，设置返回的定位结果坐标系，
        int span = 1000;//设置刷新间隔

        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        mLocationClient.setLocOption(option);

        //((LocationApplication) getApplication()).mLocationResult = mapscreen;

        mLocationClient.start();//定位SDK start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request


    }

    private void initMapView() {
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        mUiSettings = mBaiduMap.getUiSettings();
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setOverlookingGesturesEnabled(true);

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

//		 设置地图显示模式为跟随（罗盘和普通）   并修改为自定义marker
        mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.map_marker_focus);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                mCurrentMode, true, mCurrentMarker));

        MapStatus ms = new MapStatus.Builder().overlook(0).zoom(17).build();
        MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(ms);
        mBaiduMap.animateMapStatus(u, 1000);

        // add ground overlay
//        BitmapDescriptor bdGround = BitmapDescriptorFactory
//                .fromResource(R.drawable.ground_overlay);
//
//
//        OverlayOptions ooGround = new GroundOverlayOptions()..image(bdGround);
//        mBaiduMap.addOverlay(ooGround);
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(final BDLocation location) {




            if (location.getLocType() == BDLocation.TypeGpsLocation) {//搜索到卫星 GPS定位成功 按钮显示开始

                isGPSRunning = true;//设置gps标志

                //在地图上更新我的位置
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                                // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100).latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                mBaiduMap.setMyLocationData(locData);

                myLocation= new LatLng(location.getLatitude(),
                        location.getLongitude());

                if (isFirstLoc) {
                    isFirstLoc = false;
                    LatLng ll = new LatLng(location.getLatitude(),
                            location.getLongitude());
                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                    mBaiduMap.animateMapStatus(u);
                }

                mBaiduMap.setOnMyLocationClickListener(new BaiduMap.OnMyLocationClickListener() {
                    @Override
                    public boolean onMyLocationClick() {

                        showMapClickDialog(new LatLng(location.getLatitude(),
                                location.getLongitude()), location.getAddrStr());
                        return false;
                    }
                });

            }else{
                isGPSRunning = false;//设置gps标志
            }

            if ( isGPSRunning==false&&location.getLocType() == BDLocation.TypeNetWorkLocation ||
                    location.getLocType()==BDLocation.TypeOffLineLocation) {//GPS定位失败

                if (mMapView == null) {
                    return;
                }

                // 显示网络定位结果
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                                // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100).latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                mBaiduMap.setMyLocationData(locData);

                myLocation= new LatLng(location.getLatitude(),
                        location.getLongitude());

                if (isFirstLoc) {
                    isFirstLoc = false;
                    LatLng ll = new LatLng(location.getLatitude(),
                            location.getLongitude());
                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                    mBaiduMap.animateMapStatus(u);
                }

                mBaiduMap.setOnMyLocationClickListener(new BaiduMap.OnMyLocationClickListener() {
                    @Override
                    public boolean onMyLocationClick() {

                        showMapClickDialog(new LatLng(location.getLatitude(),
                                location.getLongitude()), location.getAddrStr());
                        return false;
                    }
                });
            }

        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }

        public void onReceivePoi(BDLocation poiLocation) {
        }

    }


    @Override
    protected void onPause() {
        // MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
        if (mMapView != null)
            mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        boolean gps = Util.isOpen(this);
        if (!gps) {

            openGPSDialog();//显示打开gps的对话框

        }
        // MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {


        //销毁定位客户端
        mLocationClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        // MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
        mMapView.onDestroy();
        mMapView = null;
//        BaiduNaviManager.getInstance().uninit();  //更新sdk后要取消注释
        super.onDestroy();
    }


    /**
     * 再按一次退出程序
     */
    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出地图模式", Toast.LENGTH_LONG).show();
                exitTime = System.currentTimeMillis();
            } else {


                //销毁定位客户端
                mLocationClient.stop();
                // 关闭定位图层
                mBaiduMap.setMyLocationEnabled(false);
                // MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
                finish();

                //System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 打开GPS提示对话框
     */
    void openGPSDialog() {
        if (dialog.isShowing()) {
            return;
        }

        LayoutInflater inflater = (LayoutInflater) MapActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(
                R.layout.dialogview_two_button, null);
        TextView Title = (TextView) layout.findViewById(R.id.Alert_Dialog_Title_SlideMenu);
        TextView Message = (TextView) layout.findViewById(R.id.Alert_Dialog_Message_SlideMenu);

        TextView positivebutton = (TextView) layout.findViewById(R.id.textview_positive_button);
        TextView negativebutton = (TextView) layout.findViewById(R.id.textview_negative_button);

        Title.setText("本软件需开启GPS定位开关");
        Message.setText("是否开启？");

        positivebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }
        });
        negativebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                MapActivity.this.finish();
            }
        });


        dialog.setContentView(layout);
        dialog.setCancelable(false);
        dialog.show();
    }


    void showMapClickDialog(final LatLng point, final String poi) {
        final Dialog dialog = new Dialog(MapActivity.this);

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = (LayoutInflater) MapActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(
                R.layout.dialogview_twocheckbox_onebutton, null);
        TextView Title = (TextView) layout.findViewById(R.id.Alert_Dialog_Title_SlideMenu);
        TextView Message = (TextView) layout.findViewById(R.id.Alert_Dialog_Message_SlideMenu);

        final TextView tv1 = (TextView) layout.findViewById(R.id.tv_checkbox1);
        TextView tv2 = (TextView) layout.findViewById(R.id.tv_checkbox2);

        tv1.setText("起点");
        tv2.setText("终点");

        final CheckBox start = (CheckBox) layout.findViewById(R.id.checkbox_task);
        final CheckBox end = (CheckBox) layout.findViewById(R.id.checkbox_templet);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (start.isChecked()) {
                    startPoint = point;
                    if (poi.equals("")) {
                        startN.setText(String.valueOf(point));
                        startpoi="";
                    }
                    else {
                        startN.setText(poi);
                        startpoi=poi;
                    }
                } else
                    startN.setText("");
            }
        });

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (end.isChecked()) {
                    endPoint = point;

                    if (poi.equals("")){
                        endN.setText(String.valueOf(point));
                        endpoi="";
                    }
                    else {
                        endN.setText(poi);
                        endpoi=poi;
                    }
                } else
                    endN.setText("");
            }
        });

        start.setChecked(false);
        end.setChecked(false);

        LinearLayout LL_task = (LinearLayout) layout.findViewById(R.id.LL_task);
        LinearLayout LL_template = (LinearLayout) layout.findViewById(R.id.LL_templet);

        LL_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (start.isChecked()) {
                    startPoint = point;
                    if (poi.equals("")) {
                        startN.setText(String.valueOf(point));
                        startpoi="";
                    }
                    else {
                        startN.setText(poi);
                        startpoi=poi;
                    }
                } else
                    startN.setText("");
            }
        });

        LL_template.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (end.isChecked()) {
                    endPoint = point;

                    if (poi.equals("")) {
                        endN.setText(String.valueOf(point));
                        endpoi = "";
                    } else {
                        endN.setText(poi);
                        endpoi = poi;
                    }
                } else
                    endN.setText("");
            }
        });

        TextView positivebutton = (TextView) layout.findViewById(R.id.textview_save_button);



        if (!poi.equals("")){
            Title.setText("将" + poi + "设置为");
        }
        else {
            Title.setText("将此点设置为");
        }
        Message.setVisibility(View.GONE);

        positivebutton.setText(" 确定 ");


        positivebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (start.isChecked() && end.isChecked()) {
                    Toast.makeText(MapActivity.this, "只能选择一个", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!start.isChecked() && !end.isChecked()) {
                    Toast.makeText(MapActivity.this, "至少选择一个", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();

            }
        });


        dialog.setContentView(layout);
        dialog.setCancelable(true);
        dialog.show();
    }

    /**
     * 导航相关函数
     */
    private String mSDCardPath = null;
    private static final String APP_FOLDER_NAME = "BNSDKDemo";
    public static final String ROUTE_PLAN_NODE = "routePlanNode";

    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == authBaseRequestCode) {
            for (int ret : grantResults) {
                if (ret == 0) {
                    continue;
                } else {
                    Toast.makeText(this, "缺少导航基本的权限!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            initNavi();
        }

    }

    private static final String[] authBaseArr = { Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION };
    private boolean hasBasePhoneAuth() {
        // TODO Auto-generated method stub

        PackageManager pm = this.getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    String authinfo = null;
    private static final int authBaseRequestCode = 1;
    private void initNavi() {
        // 申请权限
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            if (!hasBasePhoneAuth()) {

                this.requestPermissions(authBaseArr, authBaseRequestCode);
                return;

            }
        }
//        BaiduNaviManager.getInstance().setNativeLibraryPath(mSDCardPath + "/BaiduNaviSDK_SO");
        BaiduNaviManager.getInstance().init(this, mSDCardPath, APP_FOLDER_NAME,
                new BaiduNaviManager.NaviInitListener() {
                    @Override
                    public void onAuthResult(int status, String msg) {
                        if (0 == status) {
                            authinfo = "key校验成功!";
                        } else {
                            authinfo = "key校验失败, " + msg;
                        }
                        MapActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(MapActivity.this, authinfo, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    public void initSuccess() {
                        Toast.makeText(MapActivity.this, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                    }

                    public void initStart() {
                        Toast.makeText(MapActivity.this, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
                    }

                    public void initFailed() {
                        Toast.makeText(MapActivity.this, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
                    }
                }, null /*mTTSCallback*/);
    }

    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(
                Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    private void routeplanToNavi(BNRoutePlanNode.CoordinateType coType) {
        BNRoutePlanNode sNode = null;
        BNRoutePlanNode eNode = null;
        switch(coType) {
            case GCJ02: {
                sNode = new BNRoutePlanNode(PositionUtil.bd09_To_Gcj02(startPoint.latitude,startPoint.longitude).getWgLon(),
                        PositionUtil.bd09_To_Gcj02(startPoint.latitude,startPoint.longitude).getWgLat(),
                        startpoi, null, BNRoutePlanNode.CoordinateType.GCJ02);
                eNode = new BNRoutePlanNode(PositionUtil.bd09_To_Gcj02( endPoint.latitude,endPoint.longitude).getWgLon(),
                        PositionUtil.bd09_To_Gcj02( endPoint.latitude,endPoint.longitude).getWgLat(),
                       endpoi, null, BNRoutePlanNode.CoordinateType.GCJ02);
                //latitude: 45.713397, longitude: 126.619916
                //latitude: 45.691246, longitude: 126.625477
//                sNode = new BNRoutePlanNode(116.30142, 40.05087,
//                        startpoi, null, BNRoutePlanNode.CoordinateType.GCJ02);
//                eNode = new BNRoutePlanNode(116.39750, 39.90882,
//                        endpoi, null, BNRoutePlanNode.CoordinateType.GCJ02);
                break;
            }
            case WGS84: {
                sNode = new BNRoutePlanNode(startPoint.longitude,startPoint.latitude,
                        startpoi, null, coType);
                eNode = new BNRoutePlanNode(endPoint.longitude, endPoint.latitude,
                        endpoi, null, coType);
                break;
            }
            case BD09_MC: {
                sNode = new BNRoutePlanNode(startPoint.longitude,startPoint.latitude,
                        startpoi, null, coType);
                eNode = new BNRoutePlanNode(endPoint.longitude, endPoint.latitude,
                        endpoi, null, coType);
                break;
            }
            default : break;

        }
        if (sNode != null && eNode != null) {
            List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
            list.add(sNode);
            list.add(eNode);
            BaiduNaviManager.getInstance().launchNavigator(MapActivity.this, list, 1, true, new DemoRoutePlanListener(sNode));
        }
    }

    class DemoRoutePlanListener implements BaiduNaviManager.RoutePlanListener {

        private BNRoutePlanNode mBNRoutePlanNode = null;
        DemoRoutePlanListener(BNRoutePlanNode node){
            mBNRoutePlanNode = node;
        }

        @Override
        public void onJumpToNavigator() {
            Intent intent = new Intent(MapActivity.this, BNGuideActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        @Override
        public void onRoutePlanFailed() {
            Toast.makeText(MapActivity.this,"导航失败",Toast.LENGTH_SHORT).show();

        }
    }

}
