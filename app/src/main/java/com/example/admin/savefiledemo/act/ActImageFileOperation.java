package com.example.admin.savefiledemo.act;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.admin.savefiledemo.Constant;
import com.example.admin.savefiledemo.util.FileUtil;
import com.example.admin.savefiledemo.R;
import com.example.admin.savefiledemo.util.ToastUtil;
import com.example.admin.savefiledemo.adapter.ImageListAdapter;
import com.example.admin.savefiledemo.mode.ChooseFileMode;
import com.example.admin.savefiledemo.views.MessageDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.hzw.graffiti.GraffitiActivity;
import cn.hzw.graffiti.GraffitiParams;
import cn.hzw.imageselector.ImageLoader;

/**
 * 图片列表：
 * 使用FileUtils，把对图片文件的操作进行了封装
 * Created by admin on 2017/11/21.
 */

public class ActImageFileOperation extends AppCompatActivity {

    @BindView(R.id.list_view)
    ListView listView;
    @BindView(R.id.image_view)
    ImageView imageView;

    private List<File> filesList = new ArrayList<>();//文件夹里的所有文件
    private ImageListAdapter imageListAdapter;

    private static final File FILES_DIR = Constant.getFileDir(Constant.IMAGE_FILE_PATH);//文件保存路径

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,};//SD卡读写的权限
    private static final int REQUEST_EXTERNAL_STORAGE = 21;
    public static final int REQUEST_GRAFFITI = 22;
    private final static int RESULT_PERMISSION = 1001;
    private final static int RESULT_SUCCESS = 3;//数值小于16

    private File chooseFile;//列表中选择的文件

    private MessageDialog messageDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_readfiles);
        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        imageListAdapter = new ImageListAdapter(ActImageFileOperation.this);
        listView.setAdapter(imageListAdapter);

        verifyStoragePermissions();
    }

    /**
     * 请求SD卡读写的权限
     */
    public void verifyStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int i = ContextCompat.checkSelfPermission(ActImageFileOperation.this, PERMISSIONS_STORAGE[0]);
            int j = ContextCompat.checkSelfPermission(ActImageFileOperation.this, PERMISSIONS_STORAGE[1]);
            if (i != PackageManager.PERMISSION_GRANTED || j != PackageManager.PERMISSION_GRANTED) {
                //权限还没有授予，进行申请权限
                startRequestPermission();
            } else {
                getFilesList();
            }
        } else {
            getFilesList();
        }
    }

    /**
     * 读取指定文件夹里的所有文件
     */
    public void getFilesList() {
        //如果是一个目录则返回true
        if(FILES_DIR.isDirectory()){
            filesList.clear();
            filesList.addAll(FileUtil.getFiles(FILES_DIR));
            if(filesList == null || filesList.size() == 0){
                ToastUtil.showMessage("暂无任何文件");
                return;
            }
            String path ="";
            if(chooseFile == null){
                chooseFile = filesList.get(0);
                path = filesList.get(0).getAbsolutePath();
            }else {
                path = chooseFile.getAbsolutePath();
            }
            imageListAdapter.setChooseFile(chooseFile);
            ImageLoader.getInstance(this).display(findViewById(R.id.image_view), path);
            imageListAdapter.initData(0, filesList);
        }
    }

    /**
     * 开始涂鸦
     */
    public void openGraffity() {
        if(chooseFile == null || TextUtils.isEmpty(chooseFile.getAbsolutePath())){
            return;
        }
        // 涂鸦参数
        GraffitiParams params = new GraffitiParams();
        // 图片路径
        params.mImagePath = chooseFile.getAbsolutePath();
        params.mSavePath = FILES_DIR.getAbsolutePath(); //设置涂鸦后的图片保存的路径
        params.mAmplifierScale =0;
        params.mSavePathIsDir = true;
        params.mPaintSize = 2;//设置初始笔的大小
        params.mIsFullScreen = true; //图片充满全屏
        params.mIsDrawableOutside = false; //不允许涂鸦到图片以外的位置
                /*
                // 橡皮擦底图，如果为null，则底图为当前图片路径
                params.mEraserPath = "/storage/emulated/0/tencent/MicroMsg/WeiXin/mmexport1485172092678.jpg";
                //  橡皮擦底图是否调整大小，如果为true则调整到跟当前涂鸦图片一样的大小．
                params.mEraserImageIsResizeable = true;
                // 设置放大镜的倍数，当小于等于0时表示不使用放大器功能.放大器只有在设置面板被隐藏的时候才会出现
                params.mAmplifierScale = 2.5f;
                */
        ArrayList<String> pathList = new ArrayList<>();
        pathList.add(chooseFile.getAbsolutePath());
        GraffitiActivity.startActivityForResult(ActImageFileOperation.this, params,pathList, REQUEST_GRAFFITI);
    }

    /**
     * 获取到在文件列表点击的文件：ChooseFileMode
     * @param chooseFileMode
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getChooseFile(ChooseFileMode chooseFileMode) {
        if(chooseFileMode == null){
            ToastUtil.showMessage("选择的文件为空");
            return;
        }
        File file = chooseFileMode.getFile();
        if (file == null) {
            ToastUtil.showMessage("选择的文件为空");
            return;
        }
        chooseFile = file;
        //0为涂鸦，1为删除此文件，2为重名命， 3为复制一份
        switch (chooseFileMode.getOperation()){
            case 0:
                imageListAdapter.setChooseFile(file);
                openGraffity();
                break;
            case 1:
                //删除此文件
                deleteFile();
                break;
            case 2:
                renameFile();
                break;
            case 3:
                saveFile();
                break;
        }
    }

    /**
     * 删除选中的文件
     */
    public void deleteFile(){
        //删除此文件
        if( messageDialog == null){
            messageDialog = new MessageDialog(ActImageFileOperation.this);
        }
        messageDialog.show();
        messageDialog.setContent("确定删除此文件？");
        messageDialog.setBtnSure("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageDialog.dismiss();
                if(chooseFile != null){
                    boolean isSuccess = FileUtil.deleteFile(chooseFile);
                    if(isSuccess){
                        ToastUtil.showMessage("删除成功");
                        chooseFile = null;
                        getFilesList();
                    }else {
                        ToastUtil.showMessage("删除失败");
                    }
                }
            }
        });
        messageDialog.setBtnCancel("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageDialog.dismiss();
            }
        });
    }

    /**
     * 复制文件
     */
    public void saveFile(){
        SimpleDateFormat time = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = time.format(System.currentTimeMillis()) + ".jpg";
        String savePath = FILES_DIR.getAbsolutePath();
        boolean isSuccess = FileUtil.saveOrCopyImage(chooseFile,savePath, fileName);
        if(isSuccess){
            ToastUtil.showMessage("复制成功");
            getFilesList();
        }else {
            ToastUtil.showMessage("复制失败");
        }
    }

    /**
     * 重命名文件
     */
    public void renameFile(){
        SimpleDateFormat time = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = time.format(System.currentTimeMillis());
        String savePath = FILES_DIR.getAbsolutePath() + "/" + fileName + ".jpg";
        File newFile = new File(savePath);
        boolean isSuccess = FileUtil.renameFile(chooseFile,newFile);
        if(isSuccess){
            ToastUtil.showMessage("重命名成功");
            chooseFile = newFile;
            getFilesList();
        }else {
            ToastUtil.showMessage("重命名失败");
        }
    }

    /**
     * 编辑图片结果回调
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GRAFFITI) {
            if (data == null) {
                return;
            }
            if (resultCode == GraffitiActivity.RESULT_OK) {
                String path = data.getStringExtra(GraffitiActivity.KEY_IMAGE_PATH);
                if (TextUtils.isEmpty(path)) {
                    return;
                }
                chooseFile = new File(path);
                getFilesList();//刷新列表数据
//                ImageLoader.getInstance(this).display(findViewById(R.id.image_view), path);
            } else if (resultCode == GraffitiActivity.RESULT_ERROR) {
                ToastUtil.showMessage("onActivityResult发生错误");
            }
        }
    }

    /**
     * 开始提交请求权限
     */
    private void startRequestPermission() {
        ActivityCompat.requestPermissions(ActImageFileOperation.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
    }

    /**
     * 用户权限 申请 的回调方法
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RESULT_SUCCESS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults.length != 0) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                        boolean b = shouldShowRequestPermissionRationale(permissions[0]);
                        if (!b) {
                            // 用户还是想用我的 APP 的
                            // 提示用户去应用设置界面手动开启权限
                            showDialogTipUserGoToAppSettting();
                        } else {
                            //    finish();
                        }
                    } else {
                        getFilesList();
                    }
                }
            }
        }
    }

    /**
     * 提示用户去应用设置界面手动开启权限
     */
    private void showDialogTipUserGoToAppSettting() {

        AlertDialog dialog = new AlertDialog.Builder(ActImageFileOperation.this)
                .setTitle("权限不可用")
                .setMessage("请在-设置-应用管理中，允许使用存储权限")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到应用设置界面
                        goToAppSetting();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(false).show();
    }

    /**
     * 跳转到当前应用的设置界面
     */
    private void goToAppSetting() {
        Intent intent = new Intent();

        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);

        startActivityForResult(intent, RESULT_PERMISSION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}

