package com.example.admin.savefiledemo.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.example.admin.savefiledemo.R;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.hzw.imageselector.ImageLoader;

/**
 * Created by admin on 2018/1/2.
 */

public class GraffityImageAdapter extends BaseAdapter<File> {

    public GraffityImageAdapter(Context context) {
        super(context);
    }

    @Override
    protected View getItemView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_graffity_image, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final File item = getItem(position);
        //根据图片绝对路径获取图片并显示在界面上
        String path = item.getAbsolutePath();
        if (!TextUtils.isEmpty(path)) {
            Glide.with(mContext)
                    .load(path)
                    .signature(new StringSignature(System.currentTimeMillis()+""))//添加修改时间
                    .into(holder.itemImage);
            holder.itemName.setText(item.getName());
        }
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.item_image)
        ImageView itemImage;
        @BindView(R.id.item_name)
        TextView itemName;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
