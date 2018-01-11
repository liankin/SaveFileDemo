package com.example.admin.savefiledemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.example.admin.savefiledemo.act.ActBigImage;
import com.example.admin.savefiledemo.Constant;
import com.example.admin.savefiledemo.R;
import com.example.admin.savefiledemo.mode.ChooseFileMode;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.hzw.imageselector.ImageLoader;

/**
 * Created by Administrator on 2017/10/18 0018.
 */

public class ImageListAdapter extends BaseAdapter<File> {

    private File chooseFile;

    /**
     * 选择的是哪个文件
     *
     * @param chooseFile
     */
    public void setChooseFile(File chooseFile) {
        this.chooseFile = chooseFile;
        notifyDataSetChanged();
        // 只能刷新样式，不能刷新内部数据
        // 因为封装好的Adapter内部有自带的DataList，只能通过initData方法刷新数据
    }

    public ImageListAdapter(Context context) {
        super(context);
    }

    @Override
    protected View getItemView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_file, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final File item = getItem(position);
        //根据图片绝对路径获取图片并显示在界面上
        String path = item.getAbsolutePath();
        if (!TextUtils.isEmpty(path)) {
//            ImageLoader.getInstance(mContext).display(holder.itemImage, path);
            Glide.with(mContext)
                    .load(path)
                    .signature(new StringSignature(System.currentTimeMillis()+""))//添加修改时间
                    .into(holder.itemImage);
            holder.itemName.setText(item.getName());
        }
        if (chooseFile != null) {
            if (item.getName().equals(chooseFile.getName())) {
                convertView.setBackgroundColor(mContext.getResources().getColor(R.color.colorGray));
                holder.itemName.setTextColor(mContext.getResources().getColor(R.color.colorWhite));
            } else {
                convertView.setBackgroundColor(mContext.getResources().getColor(R.color.colorWhite));
                holder.itemName.setTextColor(mContext.getResources().getColor(R.color.colorBlack));
            }
        }
        //0为涂鸦，1为删除此文件，2为重民命， 3为复制一份, 4查看大图
        holder.itemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseFileMode chooseFileMode = new ChooseFileMode();
                chooseFileMode.setFile(item);
                chooseFileMode.setOperation(0);
                EventBus.getDefault().post(chooseFileMode);
            }
        });
        holder.itemDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseFileMode chooseFileMode = new ChooseFileMode();
                chooseFileMode.setFile(item);
                chooseFileMode.setOperation(1);
                EventBus.getDefault().post(chooseFileMode);
            }
        });
        holder.itemRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseFileMode chooseFileMode = new ChooseFileMode();
                chooseFileMode.setFile(item);
                chooseFileMode.setOperation(2);
                EventBus.getDefault().post(chooseFileMode);
            }
        });
        holder.itemCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseFileMode chooseFileMode = new ChooseFileMode();
                chooseFileMode.setFile(item);
                chooseFileMode.setOperation(3);
                EventBus.getDefault().post(chooseFileMode);
            }
        });
        holder.itemLook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseFileMode chooseFileMode = new ChooseFileMode();
                chooseFileMode.setFile(item);
                chooseFileMode.setOperation(4);

                Intent intent = new Intent();
                intent.setClass(mContext, ActBigImage.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constant.DATA, chooseFileMode);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.item_image)
        ImageView itemImage;
        @BindView(R.id.item_name)
        TextView itemName;
        @BindView(R.id.item_look)
        TextView itemLook;
        @BindView(R.id.item_rename)
        TextView itemRename;
        @BindView(R.id.item_copy)
        TextView itemCopy;
        @BindView(R.id.item_delete)
        TextView itemDelete;
        @BindView(R.id.item_layout)
        LinearLayout itemLayout;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
