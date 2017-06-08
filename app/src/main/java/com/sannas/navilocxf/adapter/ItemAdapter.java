package com.sannas.navilocxf.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.PoiItem;
import com.bumptech.glide.Glide;
import com.sannas.navilocxf.R;
import com.sannas.navilocxf.activity.GPSNaviActivity;
import com.sannas.navilocxf.util.SpeechUtils;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by JY on 2016/11/9.
 */

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    ArrayList<PoiItem> poiItems;
    MyViewHolder item;

    LatLng mLatlng;

    public ItemAdapter(Context context, ArrayList<PoiItem> poiItems, LatLng mLatlng) {
        this.context = context;
        this.mLatlng = mLatlng;
        if (poiItems == null) {
            this.poiItems = new ArrayList<>();
        } else {
            this.poiItems = poiItems;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View holder = LayoutInflater.from(context).inflate(R.layout.item_rv, parent, false);
        item = new MyViewHolder(holder);
        return item;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (poiItems.get(position).getPhotos().size() != 0) {
            String pic = poiItems.get(position).getPhotos().get(0).getUrl();
            Glide.with(context).load(pic).into(((MyViewHolder) holder).ivPic);
        }
        ((MyViewHolder) holder).tvTitle.setText(position + 1 + "." + poiItems.get(position).getTitle());
        ((MyViewHolder) holder).tvAddress.setText(poiItems.get(position).getSnippet());
        ((MyViewHolder) holder).tvTel.setText(poiItems.get(position).getTel());
        //将距离转换为float并保留两位小数
        float distance = (float) poiItems.get(position).getDistance() / 1000;
        BigDecimal b = new BigDecimal(distance);
        distance = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        ((MyViewHolder) holder).tvDistance.setText("距您" + distance + "公里");

        ((MyViewHolder) holder).ibNav.setImageResource(R.drawable.icon_nav);
        ((MyViewHolder) holder).ibNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
//                    StringBuffer stringBuffer = new StringBuffer("androidamap://navi?sourceApplication=amap");
//                    stringBuffer.append("&lat=").append(poiItems.get(position).getLatLonPoint().getLatitude())
//                            .append("&lon=").append(poiItems.get(position).getLatLonPoint().getLongitude());
//                    Intent intent = new Intent("android.intent.action.VIEW",
//                            android.net.Uri.parse(stringBuffer.toString()));
//                    intent.setPackage("com.autonavi.minimap");
//                    context.startActivity(intent);
                    Intent intent = new Intent(context, GPSNaviActivity.class);
                    intent.putExtra("mStartLatitude",poiItems.get(position).getLatLonPoint().getLatitude());
                    intent.putExtra("mStartLongitude",poiItems.get(position).getLatLonPoint().getLongitude());
                    intent.putExtra("mStartLatitude", mLatlng.latitude);
                    intent.putExtra("mStartLongitude", mLatlng.longitude);
                    context.startActivity(intent);
                    SpeechUtils speechUtils = new SpeechUtils(context);
                    if (speechUtils.isSpeaking()) {
                        speechUtils.stopSpeakding();
                    }
                    speechUtils.startSpeaking("正在为您导航至:" + poiItems.get(position).getTitle());
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, "尚未安装高德", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    @Override
    public int getItemCount() {
        return poiItems.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPic;
        TextView tvTitle, tvAddress, tvTel, tvDistance;
        ImageButton ibNav;

        public MyViewHolder(View itemView) {
            super(itemView);
            ivPic = (ImageView) itemView.findViewById(R.id.ivPic);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvAddress = (TextView) itemView.findViewById(R.id.tvAddress);
            tvTel = (TextView) itemView.findViewById(R.id.tvDistance);
            tvDistance = (TextView) itemView.findViewById(R.id.tvDistance);
            ibNav = (ImageButton) itemView.findViewById(R.id.ibNav);


        }
    }


}


