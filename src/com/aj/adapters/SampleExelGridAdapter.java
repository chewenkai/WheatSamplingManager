package com.aj.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.aj.activity.CollectionApplication;
import com.aj.bean.ImageInfo;
import com.aj.collection.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * 采样单中gridview图片的适配器
 * Created by kevin on 15-9-26.
 */

public class SampleExelGridAdapter extends BaseAdapter {
    Context mContext;
    ArrayList<ImageInfo> mImages;
    ImageLoader imageLoader=ImageLoader.getInstance();

    public ArrayList<ImageInfo> getmImages() {
        return mImages;
    }

    public void setmImages(ArrayList<ImageInfo> mImages) {
        this.mImages = mImages;
        this.notifyDataSetChanged();
    }

    public SampleExelGridAdapter(Context context, ArrayList<ImageInfo> mImages) {
        mContext = context;
        this.mImages =mImages;
    }


    @Override
    public int getCount() {
        return mImages.size();
    }


    public ArrayList<ImageInfo> getImages(){
        return this.mImages;
    }

    @Override
    public Object getItem(int position) {

        return position;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder h = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.imageview_one, null);

            h = new Holder();
            ImageView img = (ImageView) convertView.findViewById(R.id.icon);
            h.v = img;
            convertView.setTag(h);
        } else
            h = (Holder) convertView.getTag();

        if(!imageLoader.isInited()){
            CollectionApplication.initImageLoader(mContext);
        }

        if (mImages.get(position).getMediaType()==ImageInfo.DEFAULT) {
            imageLoader.displayImage("file://" +
                            mImages.get(position).getLocalPath(), h.v,
                    CollectionApplication.defaultOptions);
        }else if(mImages.get(position).getMediaType()==ImageInfo.PICTURE){
            h.v.setImageResource(R.drawable.addsamplepicturebtn);

        }else if(mImages.get(position).getMediaType()==ImageInfo.VIDEO){
            h.v.setImageResource(R.drawable.addsamplepicturebtn);
        }else if(mImages.get(position).getMediaType()==ImageInfo.VIDEOBMP){
            h.v.setImageBitmap(mImages.get(position).getBitmap());
        }
//        h.v.setImageBitmap(ImageTools.getBitmapFromPath(mImages.get(position).getLocalPath()));
        return convertView;
    }

    class Holder {
        ImageView v;
    }
}