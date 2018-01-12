package com.example.admin.savefiledemo.act;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.admin.savefiledemo.R;

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
    @BindView(R.id.btn_my_signature)
    Button btnMySignature;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

    }

    @OnClick({R.id.btn_user_signature,
            R.id.btn_my_signature, R.id.btn_download_image, R.id.btn_image_list, R.id.btn_image_operation,
            R.id.btn_file_operation, R.id.btn_move_image, R.id.btn_tu_ya_more_image, R.id.btn_graffti_more_image})
    public void onViewClicked(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.btn_user_signature:
                intent = new Intent(HomeActivity.this, ActUserSignature.class);
                startActivity(intent);
                break;
            case R.id.btn_my_signature:
                intent = new Intent(HomeActivity.this, ActMySign.class);
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
