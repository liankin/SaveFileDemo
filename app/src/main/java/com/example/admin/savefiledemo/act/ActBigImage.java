package com.example.admin.savefiledemo.act;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.example.admin.savefiledemo.Constant;
import com.example.admin.savefiledemo.R;
import com.example.admin.savefiledemo.mode.ChooseFileMode;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 全屏查看大图
 * Created by admin on 2017/11/21.
 */

public class ActBigImage extends AppCompatActivity {

    @BindView(R.id.subsampling_scale_image_view)
    SubsamplingScaleImageView subsamplingScaleImageView;
    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.tv_current_index)
    TextView tvCurrentIndex;
    @BindView(R.id.btn_previous_page)
    ImageView btnPreviousPage;
    @BindView(R.id.btn_next_page)
    ImageView btnNextPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_bigimage);
        ButterKnife.bind(this);
        tvCurrentIndex.setVisibility(View.GONE);
        btnPreviousPage.setVisibility(View.GONE);
        btnNextPage.setVisibility(View.GONE);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Constant.DATA)) {
            ChooseFileMode chooseMode = (ChooseFileMode) intent.getSerializableExtra(Constant.DATA);
            String saveFilePath = chooseMode.getFile().getAbsolutePath();
            if (!TextUtils.isEmpty(saveFilePath)) {
//                ImageLoader.getInstance(this).display(imageView, path);
                File file = new File(saveFilePath);
                if (!file.exists()) {
                    return;
                }
                subsamplingScaleImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
                subsamplingScaleImageView.setMinScale(0.1F);//最小显示比例
                // 将图片文件给SubsamplingScaleImageView,这里注意设置ImageViewState设置初始显示比例
                float initImageScale = getInitImageScale(saveFilePath);
                subsamplingScaleImageView.setMaxScale(initImageScale + 3.0f);//最大显示比例
                // ImageViewState的三个参数为：scale,center,orientation
                subsamplingScaleImageView.setImage(ImageSource.uri(saveFilePath),
                        new ImageViewState(initImageScale, new PointF(0, 0), 0));
            }
        } else {
            finish();
        }
    }

    /**
     * 计算出图片初次显示需要放大倍数
     *
     * @param imagePath 图片的绝对路径
     */
    public float getInitImageScale(String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        WindowManager wm = this.getWindowManager();
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        // 拿到图片的宽和高
        int dw = bitmap.getWidth();
        int dh = bitmap.getHeight();
        float scale = 1.0f;
        //图片宽度大于屏幕，但高度小于屏幕，则缩小图片至填满屏幕宽
        if (dw > width && dh <= height) {
            scale = width * 1.0f / dw;
        }
        //图片宽度小于屏幕，但高度大于屏幕，则放大图片至填满屏幕宽
        if (dw <= width && dh > height) {
            scale = width * 1.0f / dw;
        }
        //图片高度和宽度都小于屏幕，则放大图片至填满屏幕宽
        if (dw < width && dh < height) {
            scale = width * 1.0f / dw;
        }
        //图片高度和宽度都大于屏幕，则缩小图片至填满屏幕宽
        if (dw > width && dh > height) {
            scale = width * 1.0f / dw;
        }
        return scale;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @OnClick({R.id.tv_back, R.id.btn_previous_page, R.id.btn_next_page})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                onBackPressed();
                break;
            case R.id.btn_previous_page:
                break;
            case R.id.btn_next_page:
                break;
        }
    }
}

