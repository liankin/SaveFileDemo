package com.example.admin.savefiledemo.act;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.admin.savefiledemo.Constant;
import com.example.admin.savefiledemo.R;
import com.example.admin.savefiledemo.util.ToastUtil;
import com.example.admin.savefiledemo.views.SignaturePngView;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 涂鸦写字、保存Png透明图片、清屏；
 */

public class ActUserSignature extends AppCompatActivity {

    @BindView(R.id.btn_clean_all)
    Button btnCleanAll;
    @BindView(R.id.btn_save_image)
    Button btnSaveImage;
    @BindView(R.id.signature_png_view)
    SignaturePngView signaturePngView;
    @BindView(R.id.iv_mysign)
    ImageView ivMysign;

    private File fileDir = Constant.getFolderDir(Constant.SIGNAYURE_FILE_PATH);
    private String fileUrl = fileDir.getAbsolutePath() +"/" + Constant.SIGNATURE_FILE_NAME;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_usersignature);
        ButterKnife.bind(this);

        File file = new File(fileUrl);
        if (file.exists()){
            ivMysign.setImageBitmap(BitmapFactory.decodeFile(fileUrl));
        }
    }

    @OnClick({R.id.btn_clean_all, R.id.btn_save_image})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_clean_all:
                signaturePngView.cleanAll();
                break;
            case R.id.btn_save_image:
                try {
                    signaturePngView.saveResultImage(fileUrl, true, 10);
                    ToastUtil.showMessage("保存结束");
                    ivMysign.setImageBitmap(signaturePngView.getmBitmap());
                }catch (IOException e){
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

