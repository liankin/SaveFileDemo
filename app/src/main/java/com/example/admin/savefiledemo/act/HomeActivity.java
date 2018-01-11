package com.example.admin.savefiledemo.act;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.admin.savefiledemo.R;
import com.example.admin.savefiledemo.util.ImageUtil;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by admin on 2017/11/15.
 */

public class HomeActivity extends AppCompatActivity {


    @BindView(R.id.btn_download_image)
    Button btnDownloadImage;
    @BindView(R.id.btn_image_list)
    Button btnImageList;
    @BindView(R.id.btn_image_operation)
    Button btnImageOperation;
    @BindView(R.id.btn_file_operation)
    Button btnFileOperation;
    @BindView(R.id.btn_move_image)
    Button btnMoveImage;
    @BindView(R.id.btn_tu_ya_more_image)
    Button btnTuYaMoreImage;
    @BindView(R.id.btn_graffti_more_image)
    Button btnGrafftiMoreImage;
    @BindView(R.id.btn_user_signature)
    Button btnUserSignature;
    @BindView(R.id.image_view)
    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
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
        if(file.exists()){
            Bitmap bitmap = ImageUtil.createBitmapFromPath(file.getAbsolutePath(), 1920, 1080);
            imageView.setImageBitmap(bitmap);
        }
    }


    @OnClick({R.id.btn_user_signature, R.id.btn_download_image, R.id.btn_image_list, R.id.btn_image_operation,
            R.id.btn_file_operation, R.id.btn_move_image, R.id.btn_tu_ya_more_image, R.id.btn_graffti_more_image})
    public void onViewClicked(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.btn_user_signature:
                intent = new Intent(HomeActivity.this, ActUserSignature.class);
                startActivity(intent);
                break;
            case R.id.btn_download_image:
                intent = new Intent(HomeActivity.this, ActDownloadImage.class);
                startActivity(intent);
                break;
            case R.id.btn_image_list:
                intent = new Intent(HomeActivity.this, ActImageList.class);
                startActivity(intent);
                break;
            case R.id.btn_image_operation:
                intent = new Intent(HomeActivity.this, ActImageFileOperation.class);
                startActivity(intent);
                break;
            case R.id.btn_file_operation:
                intent = new Intent(HomeActivity.this, ActTextFileOperation.class);
                startActivity(intent);
                break;
            case R.id.btn_move_image:
                intent = new Intent(HomeActivity.this, ActMoveImage.class);
                startActivity(intent);
                break;
            case R.id.btn_tu_ya_more_image:
                intent = new Intent(HomeActivity.this, ActTuYaMoreImage.class);
                startActivity(intent);
                break;
            case R.id.btn_graffti_more_image:
                intent = new Intent(HomeActivity.this, ActGraffityImage.class);
                startActivity(intent);
                break;
        }
    }
}
