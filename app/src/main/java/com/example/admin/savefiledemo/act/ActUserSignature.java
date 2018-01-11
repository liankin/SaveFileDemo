package com.example.admin.savefiledemo.act;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.admin.savefiledemo.R;
import com.example.admin.savefiledemo.views.SignaturePngView;

import java.io.File;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_usersignature);
        ButterKnife.bind(this);

        File sdDir = Environment.getExternalStorageDirectory();
        File fileDir = new File(sdDir.getPath() + "/SAVEFILEDEMO/TUYA");
        if (!fileDir.exists()) {
            // 必须要先有父文件夹才能在父文件夹下建立想要的子文件夹
            // 即LIMS文件必须存在，才能建立IMG文件夹
            fileDir.mkdir();
        }
        String fileUrl = fileDir.getAbsolutePath()+"/个人签名.png";
        File file = new File(fileUrl);
        signaturePngView.setResultImageFile(file);
    }

    @OnClick({R.id.btn_clean_all, R.id.btn_save_image})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_clean_all:
                signaturePngView.cleanAll();
                break;
            case R.id.btn_save_image:
                signaturePngView.saveResultImage();
                break;
        }
    }
}

