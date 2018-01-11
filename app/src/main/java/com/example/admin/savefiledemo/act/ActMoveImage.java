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
    private File fileDir;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_moveimage);
        ButterKnife.bind(this);
        File sdDir = Environment.getExternalStorageDirectory();
        fileDir = new File(sdDir.getPath() + "/SAVEFILEDEMO/IMG");
        if (!fileDir.exists()) {
            // 必须要先有父文件夹才能在父文件夹下建立想要的子文件夹
            // 即LIMS文件必须存在，才能建立IMG文件夹
            fileDir.mkdir();
        }
        String imagePath = fileDir.getAbsolutePath() + "/百变小樱.jpeg";
        File file = new File(imagePath);
        if(file.exists()){
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            tuyaView = new TuyaView(ActMoveImage.this, imagePath);
            layoutMoveImage.addView(tuyaView, 0);
        }
    }

    /**
     * 计算两个手指间的距离
     */
    private float distance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        /** 使用勾股定理返回两点之间的距离 */
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * 计算两个手指间的中间点
     */
    private PointF mid(MotionEvent event) {
        float midX = (event.getX(1) + event.getX(0)) / 2;
        float midY = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(midX, midY);
    }

    /**
     * 计算出图片初次显示需要放大倍数
     */
    public float getInitImageScale(Bitmap bitmap) {
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

    @OnClick({R.id.btn_revocation, R.id.btn_save, R.id.btn_clear_all, R.id.btn_add_image})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_revocation:
                tuyaView.revocationGraffity();
                break;
            case R.id.btn_save:
                tuyaView.saveResultImage();
                break;
            case R.id.btn_clear_all:
                tuyaView.cleanGraffity();
                break;
            case R.id.btn_add_image:
                tuyaView.openAddImage(fileDir.getAbsolutePath() + "/风景.jpg");
                break;
        }
    }
}
