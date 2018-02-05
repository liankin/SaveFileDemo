package com.example.admin.savefiledemo.act;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.admin.savefiledemo.Constant;
import com.example.admin.savefiledemo.R;
import com.example.admin.savefiledemo.util.ImageUtil;
import com.example.admin.savefiledemo.util.ToastUtil;
import com.example.admin.savefiledemo.views.LinePathView;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActMySign extends AppCompatActivity {

    @BindView(R.id.clear1)
    Button clear1;
    @BindView(R.id.save1)
    Button save1;
    @BindView(R.id.view)
    LinePathView mPathView;
    @BindView(R.id.iv_mysign)
    ImageView ivMysign;

    private File fileDir = Constant.getFolderDir(Constant.SIGNAYURE_FILE_PATH);
    private String path = fileDir.getAbsolutePath() +"/" + Constant.SIGNATURE_FILE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_my_sign);
        ButterKnife.bind(this);

        File file = new File(path);
        if (file.exists()){
            ivMysign.setImageBitmap(BitmapFactory.decodeFile(path));
        }
    }

    @OnClick({R.id.clear1, R.id.save1})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.clear1:
                mPathView.clear();
                break;
            case R.id.save1:
                try {
                    mPathView.save(path, true, 10);
                    ToastUtil.showMessage("保存成功");
                    ivMysign.setImageBitmap(mPathView.getBitMap());
                } catch (IOException e) {
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
