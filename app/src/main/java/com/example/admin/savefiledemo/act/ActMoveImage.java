package com.example.admin.savefiledemo.act;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.admin.savefiledemo.Constant;
import com.example.admin.savefiledemo.R;
import com.example.admin.savefiledemo.views.TuyaView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 移动缩放单张图片：
 * 移动缩放与涂鸦不兼容；撤销、清屏、保存、贴图
 * Created by admin on 2017/12/11.
 */

public class ActMoveImage extends AppCompatActivity {

    @BindView(R.id.layout_move_image)
    FrameLayout layoutMoveImage;
    @BindView(R.id.btn_revocation)
    TextView btnRevocation;
    @BindView(R.id.btn_save)
    TextView btnSave;
    @BindView(R.id.btn_clear_all)
    TextView btnClearAll;
    @BindView(R.id.btn_add_image)
    TextView btnAddImage;

    private TuyaView tuyaView = null;
    private File readFileDir = Constant.getFileDir(Constant.GRAFFITY_SRC_FILE_PATH);
    private File saveFileDir = Constant.getFileDir(Constant.GRAFFITY_DES_FILE_PATH);
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_moveimage);
        ButterKnife.bind(this);

        String imagePath = readFileDir.getAbsolutePath() + "/image1.jpg";
        File file = new File(imagePath);
        if(file.exists()){
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            tuyaView = new TuyaView(ActMoveImage.this, imagePath);
            layoutMoveImage.addView(tuyaView, 0);
        }
    }

    @OnClick({R.id.btn_revocation, R.id.btn_save, R.id.btn_clear_all, R.id.btn_add_image})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_revocation:
                tuyaView.revocationGraffity();
                break;
            case R.id.btn_save:
                tuyaView.saveResultImage(saveFileDir.getAbsolutePath()+"/image1_1.png");
                break;
            case R.id.btn_clear_all:
                tuyaView.cleanGraffity();
                break;
            case R.id.btn_add_image:
                File file = new File(readFileDir.getAbsolutePath() + "/image2.jpg");
                if(file.exists()){
                    tuyaView.openAddImage(readFileDir.getAbsolutePath() + "/image2.jpg");
                }
                break;
        }
    }
}
