package com.sannas.navilocxf.view;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.sannas.navilocxf.R;
import com.sannas.navilocxf.adapter.ItemAdapter;
import com.sannas.navilocxf.util.CheckPermissionsActivity;
import com.sannas.navilocxf.util.SpeechUtils;

import java.util.ArrayList;

public class LbsActivity extends CheckPermissionsActivity implements LocationSource {
    MapView mMapView;
    AMapLocationClient mAMapLocationClient;
    UiSettings mUiSettings;
    private static final String TAG = "LbsActivity";
    private LatLng mLatlng;
    private AMap aMap;
    private Marker mMarker;
    private PoiSearch.Query query;
    private PoiSearch poi;
    RecyclerView rv;
    private ArrayList<PoiItem> pois;
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    OnLocationChangedListener mListener;
    ArrayList<Marker> markers;
    Animation mAnimation;
    boolean isFirstStart = true;

    ItemAdapter adapter;
    SpeechUtils utils;
    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    poiSearch();
                    break;
                case 1:
                    adapter = new ItemAdapter(LbsActivity.this, pois, mLatlng);
                    rv.setLayoutManager(new LinearLayoutManager(LbsActivity.this));
                    rv.setAdapter(adapter);
                    break;
            }
            return false;
        }
    });
    private StringBuffer sb;
    int page;
    int totalpage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remark);
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy != 0) {
                    LinearLayoutManager im = (LinearLayoutManager) rv.getLayoutManager();
                    int position = im.findFirstCompletelyVisibleItemPosition();
                    Log.d(TAG, "onScrolled: position:" + position + "  dy:" + dy);
                    LatLng l = new LatLng(pois.get(position).getLatLonPoint().getLatitude(),
                            pois.get(position).getLatLonPoint().getLongitude());
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(l, 15));
                    //给Maker添加动画
                    if (markers != null) {
                        mAnimation = new ScaleAnimation(1, 1.3f, 1, 1.3f);
//                        mAnimation.setDuration(500);
//                        mAnimation.setInterpolator(new LinearInterpolator());
                        markers.get(position).setAnimation(mAnimation);
                        markers.get(position).startAnimation();
                        markers.get(position).showInfoWindow();
                    }
                }
            }
        });
        utils = new SpeechUtils(this);
        initMap(savedInstanceState);
        setLocationButton();
        getLocation();

    }

    private void poiSearch() {
        if (markers == null) {
            markers = new ArrayList<>();
        } else {
            markers.clear();
        }

        //清除map中的marker
        aMap.clear(true);
        if (pois != null) {
            pois.clear();
            adapter.notifyDataSetChanged();
        }
        if (sb != null) {
            sb.delete(0, sb.length());
        }

        //POI搜索
        //     query = new PoiSearch.Query("", "110202|110203", "");
        query = new PoiSearch.Query("", "停车场", "");
        query.setPageSize(30);
        query.setPageNum(page);
        poi = new PoiSearch(LbsActivity.this, query);
        poi.setOnPoiSearchListener(mOnPoiSearchListener);
        poi.setBound(new PoiSearch.SearchBound(new LatLonPoint(mLatlng.latitude, mLatlng.longitude), 100000));
        poi.searchPOIAsyn();
    }

    //POI搜索的监听
    PoiSearch.OnPoiSearchListener mOnPoiSearchListener = new PoiSearch.OnPoiSearchListener() {
        @Override
        public void onPoiSearched(PoiResult poiResult, int i) {
            Log.d(TAG, "PageCount: " + poiResult.getPageCount());
            totalpage = poiResult.getPageCount() - 1;
            int currentPage = poiResult.getQuery().getPageNum();

            if (pois == null) {
                pois = new ArrayList<>();
            }

            for (int a = 0; a < poiResult.getPois().size(); a++) {
                LatLng latLng = new LatLng(poiResult.getPois().get(a).getLatLonPoint().getLatitude(), poiResult.getPois().get(a).getLatLonPoint().getLongitude());
                Marker marker = aMap.addMarker(new MarkerOptions().
                        position(latLng).title(poiResult.getPois().get(a).getTitle())
                        .snippet(poiResult.getPois().get(a).getSnippet()));
                Log.d(TAG, "onPoiSearched: " + poiResult.getPois().get(a).getTitle() + " " + poiResult.getPois().get(a).getSnippet() + " " + poiResult.getPois().get(a).getTypeDes() +
                        " " + poiResult.getPois().get(a).getEnter() + " " + poiResult.getPois().get(a).getDirection());
                markers.add(marker);
                pois.ensureCapacity(poiResult.getPageCount());
                pois.add(poiResult.getPois().get(a));
//                sb.append(a + 1 + "。" + pois.get(a).getTitle() + "。");
            }
            Log.d(TAG, "page:" + page + " totalpage:" + totalpage);
            page = page + 1;
            while (page <= totalpage) {
                query.setPageNum(page);
                poi.searchPOIAsyn();
                break;
            }
            if (currentPage == totalpage) {
                dealWithPOIResult();
            }
        }

        @Override
        public void onPoiItemSearched(PoiItem poiItem, int i) {

        }
    };

    //处理搜索到的POI结果
    public void dealWithPOIResult() {
        if (sb == null) {
            sb = new StringBuffer();
        }
        //   sb.append("为您找到了" + pois.size() + "个景点。");
        sb.append("为您找到了" + pois.size() + "停车场。");
        for (int i = 0; i < pois.size(); i++) {
            sb.append(i + 1 + "。" + pois.get(i).getTitle() + "。");
        }
        Log.d(TAG, "dealWithPOIResult: " + sb);
        mHandler.sendEmptyMessage(1);
        //  utils.startSpeaking(sb.toString());
        page = 0;
        totalpage = 0;
    }

    private void setLocationButton() {
        aMap.setLocationSource(this);// 设置定位监听
        mUiSettings = aMap.getUiSettings();//实例化UiSettings类
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true); // 显示默认的定位按钮
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        aMap.setMyLocationEnabled(true);// 可触发定位并显示定位层
        setUpLocationStyle();
    }

    private void setUpLocationStyle() {
        // 自定义系统定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
                fromResource(R.drawable.gps_point));
        // 自定义精度范围的圆形边框颜色
        myLocationStyle.strokeColor(STROKE_COLOR);
        //自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(5);
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(FILL_COLOR);
        // 将自定义的 myLocationStyle 对象添加到地图上
        aMap.setMyLocationStyle(myLocationStyle);
    }

    private void getLocation() {
        mAMapLocationClient = new AMapLocationClient(this);
        AMapLocationClientOption aMapLocationClientOption = new AMapLocationClientOption();
        aMapLocationClientOption.setOnceLocation(true);
        aMapLocationClientOption.setSensorEnable(true);
        aMapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        mAMapLocationClient.setLocationOption(aMapLocationClientOption);
        mAMapLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation == null || aMapLocation.getErrorCode() != 0) {
                    return;
                }
                if (mListener != null) {
                    mListener.onLocationChanged(aMapLocation);
                }
                mLatlng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatlng, 19));
                if (mLatlng != null) {
                    mHandler.sendEmptyMessage(0);
                }
            }
        });
        mAMapLocationClient.startLocation();
    }

    private void initMap(Bundle savedInstanceState) {
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);// 此方法必须重写
        aMap = mMapView.getMap();
        aMap.setTrafficEnabled(true);// 显示实时交通状况
        //地图模式可选类型：MAP_TYPE_NORMAL,MAP_TYPE_SATELLITE,MAP_TYPE_NIGHT
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 卫星地图模式
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，实现地图生命周期管理
        mMapView.onSaveInstanceState(outState);
    }


    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        Log.d(TAG, "activate: ");
        if (!isFirstStart) {
//            if (markers != null | pois != null) {
//                markers.clear();
//                pois.clear();
//            }
            getLocation();
            isFirstStart = false;
        } else {
            isFirstStart = false;
        }
//        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatlng, 19));
        mListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        Log.d(TAG, "deactivate: ");
        mListener = null;
    }

}