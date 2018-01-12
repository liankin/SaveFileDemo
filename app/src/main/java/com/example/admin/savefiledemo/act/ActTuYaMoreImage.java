package com.example.admin.savefiledemo.act;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.admin.savefiledemo.Constant;
import com.example.admin.savefiledemo.R;
import com.example.admin.savefiledemo.util.ToastUtil;
import com.example.admin.savefiledemo.views.PaintStyleDialog;
import com.example.admin.savefiledemo.views.TuYaMoreView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 涂鸦多张图片：
 * 移动缩放与涂鸦不兼容；图片切换、撤销、清屏、保存、画笔样式、贴图
 * Created by admin on 2017/12/14.
 */
public class ActTuYaMoreImage extends AppCompatActivity {


    @BindView(R.id.tv_image_index)
    TextView tvImageIndex;
    @BindView(R.id.btn_save)
    TextView btnSave;
    @BindView(R.id.btn_revocation)
    TextView btnRevocation;
    @BindView(R.id.btn_clear_all)
    TextView btnClearAll;
    @BindView(R.id.btn_add_image)
    TextView btnAddImage;
    @BindView(R.id.btn_paint_style)
    TextView btnPaintStyle;
    @BindView(R.id.btn_paint)
    TextView btnPaint;
    @BindView(R.id.btn_previous_image)
    ImageView btnPreviousImage;
    @BindView(R.id.btn_next_image)
    ImageView btnNextImage;
    @BindView(R.id.layout_move_image)
    RelativeLayout layoutMoveImage;

    private TuYaMoreView tuyaView = null;
    private File readFileDir = Constant.getFileDir(Constant.GRAFFITY_SRC_FILE_PATH);
    private String saveFileDirPath = Constant.getFileDir(Constant.GRAFFITY_DES_FILE_PATH).getAbsolutePath()
            +"/第一个模板涂鸦结果";
    private PaintStyleDialog paintStyleDialog;
    private Paint paint = new Paint();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_tuyamoreimage);
        ButterKnife.bind(this);
        btnPaint.setVisibility(View.GONE);

        String folderPath = readFileDir.getAbsolutePath() + "/第一个模板";
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        tuyaView = new TuYaMoreView(ActTuYaMoreImage.this, folderPath);
        layoutMoveImage.addView(tuyaView, 0);
        if (tuyaView.getSrcFileList() != null && tuyaView.getSrcFileList().size() > 0) {
            tvImageIndex.setText("当前:" + (tuyaView.getCurrentFileIndex() + 1) + "/" + tuyaView.getSrcFileList().size());
        } else {
            tvImageIndex.setText("当前:0/0");
        }
    }

    /**
     * 设置画笔样式
     */
    public void setPaintStyle() {
        if (paintStyleDialog == null) {
            paintStyleDialog = new PaintStyleDialog(ActTuYaMoreImage.this);
        }
        paintStyleDialog.show();
        paint = tuyaView.getmPaint();
        paintStyleDialog.setBtnSure(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String paintSize = paintStyleDialog.getEdPaintSize().getText().toString().trim();
                if (!TextUtils.isEmpty(paintSize)) {
                    paint.setStrokeWidth(Integer.valueOf(paintSize));// 画笔宽度
                }
                tuyaView.setmPaint(paint);
                paintStyleDialog.dismiss();
            }
        });

        paintStyleDialog.getTvRed().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paint.setColor(Color.parseColor("#D90E0E"));
                ToastUtil.showMessage("红色");
            }
        });
        paintStyleDialog.getTvBlack().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paint.setColor(Color.parseColor("#000000"));
                ToastUtil.showMessage("黑色");
            }
        });
        paintStyleDialog.getTvGreen().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paint.setColor(Color.parseColor("#218868"));
                ToastUtil.showMessage("绿色");
            }
        });
    }

    @OnClick({R.id.btn_revocation, R.id.btn_previous_image, R.id.btn_next_image, R.id.btn_save,
            R.id.btn_clear_all, R.id.btn_add_image, R.id.btn_paint_style})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_revocation://撤销
                tuyaView.revocationGraffity();
                break;
            case R.id.btn_previous_image://上一张
                tuyaView.previousImage();
                tvImageIndex.setText("当前:" + (tuyaView.getCurrentFileIndex() + 1) + "/" + tuyaView.getSrcFileList().size());
                break;
            case R.id.btn_next_image://下一张
                tuyaView.nextImage();
                tvImageIndex.setText("当前:" + (tuyaView.getCurrentFileIndex() + 1) + "/" + tuyaView.getSrcFileList().size());
                break;
            case R.id.btn_save://保存
                tuyaView.saveResultImage(saveFileDirPath);
                break;
            case R.id.btn_clear_all://清屏
                tuyaView.cleanGraffity();
                break;
            case R.id.btn_add_image://贴图
                File file = new File(readFileDir.getAbsolutePath() + "/image1.jpg");
                if(file.exists()){
                    tuyaView.openAddImageMode(readFileDir.getAbsolutePath() + "/image1.jpg");
                }
                break;
            case R.id.btn_paint_style://画笔样式
                setPaintStyle();
                break;
        }
    }
}
