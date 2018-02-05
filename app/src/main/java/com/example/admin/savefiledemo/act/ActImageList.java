package com.example.admin.savefiledemo.act;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.admin.savefiledemo.Constant;
import com.example.admin.savefiledemo.R;
import com.example.admin.savefiledemo.util.CrcheckUtil;
import com.example.admin.savefiledemo.util.ToastUtil;
import com.example.admin.savefiledemo.adapter.ImageListAdapter;
import com.example.admin.savefiledemo.mode.ChooseFileMode;
import com.example.admin.savefiledemo.views.MessageDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
 * 对于图片的操作代码全在此类中
 * Created by admin on 2017/11/15.
 */

public class ActImageList extends AppCompatActivity {

    @BindView(R.id.list_view)
    ListView listView;
    @BindView(R.id.image_view)
    ImageView imageView;

    private List<File> filesList = new ArrayList<>();//文件夹里的所有文件
    private ImageListAdapter imageListAdapter;

    private static final File FILES_DIR = Constant.getFolderDir(Constant.IMAGE_FILE_PATH);//文件保存路径

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,};//SD卡读写的权限
    private static final int REQUEST_EXTERNAL_STORAGE = 21;
    public static final int REQUEST_GRAFFITI = 22;
    private final static int RESULT_PERMISSION = 1001;
    private final static int RESULT_SUCCESS = 3;

    private File chooseFile;//列表中选择的文件

    private MessageDialog messageDialog;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_readfiles);
        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        imageListAdapter = new ImageListAdapter(ActImageList.this);
        listView.setAdapter(imageListAdapter);

        verifyStoragePermissions();
    }

    /**
     * 请求SD卡读写的权限
     */
    public void verifyStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int i = ContextCompat.checkSelfPermission(ActImageList.this, PERMISSIONS_STORAGE[0]);
            int j = ContextCompat.checkSelfPermission(ActImageList.this, PERMISSIONS_STORAGE[1]);
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
        if (FILES_DIR.isDirectory()) {
            File[] filesArray = FILES_DIR.listFiles();
            if (filesArray == null || filesArray.length == 0) {
                ToastUtil.showMessage("暂无任何文件");
            } else {
                filesList.clear();
                for (int i = filesArray.length - 1; i >= 0; i--) {
                    File file = filesArray[i];
                    if(file.isFile()){
                        filesList.add(file);
                    }
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
    }

    /**
     * 开始涂鸦
     */
    public void openGraffity() {
        if(chooseFile == null || TextUtils.isEmpty(chooseFile.getAbsolutePath())){
            return;
        }

//        Intent intent = new Intent();
//        intent.setClass(ActImageList.this, ActBigImage.class);
//        startActivity(intent);

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
        GraffitiActivity.startActivityForResult(ActImageList.this, params,pathList, REQUEST_GRAFFITI);
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
     * 获得图片的crc16校验码
     */
    public void initCrc(){
        try {
//            byte[] imageByte = BitmapToBytes();
//            Log.d("ActImageList", "获得图片的byte[].toString()：" + imageByte.toString());
//
//            String str16 = CrcheckUtil.bytesToHexFun2(imageByte);
//            Log.d("ActImageList", "byte[] 转 16进制字符：" + str16);
//
//            int result = CrcheckUtil.getCrc16(str16);
//            Log.d("ActImageList", "计算得到十进制数crc校验码：" + result);
//
//            String result10to16 = String.format("0x%04x", result);
//            Log.d("ActImageList", "十进制数crc校验码 转16进制：" + result10to16);

//            Log.d("ActImageList", "======================================");
//
//            int crc = CrcheckUtil.calcCrc16(imageByte);
//            Log.d("ActImageList", "byte[] 转 十进制数 crc16：" + crc);
//
//            String str10to16 = String.format("0x%04x", crc);//10进制转换16正确
//            Log.d("ActImageList", "十进制数 crc16 转16进制：" + str10to16);
//
//            String str10to2 = Integer.toBinaryString(crc);//10进制转换2正确
//            Log.d("ActImageList", "十进制数 crc16 转2进制：" + str10to2);

//            Log.d("ActImageList", "======================================");
//            String crc16str = Make_CRC(imageByte);
//            Log.d("ActImageList", "任意byte[] 计算得 crc16校验码：" + crc16str);//两个低位在前，两个高位在后

            //CRC16_MODBUS：多项式x16+x15+x5+1（0x8005），初始值0xFFFF，低位在前，高位在后，结果与0x0000异或
            Log.d("ActImageList", "===============CRC-16 (Modbus)=======================");
            //"7E000560313233" (hex) ---> CRC-16 (Modbus) 0xBD53
            String str16jinzhi = "7E000560313233";
            byte[] str16jinzhiTobyte = CrcheckUtil.hexStringToBytes(str16jinzhi);
            String crc16str = Make_CRC(str16jinzhiTobyte);
            Log.d("ActImageList", "任意byte[] 计算得 crc16校验码：" + crc16str);//两个低位在前，两个高位在后

            Log.d("ActImageList", "=================CRC-16 (Modbus)=====================");
            int crc = CrcheckUtil.calcCrc16(str16jinzhiTobyte);
            String str10to16 = String.format("0x%04x", crc);//10进制转换16正确
            Log.d("ActImageList", "十进制数 crc16 转16进制：" + str10to16);

            Log.d("ActImageList", "===================CRC-16 (Modbus)===================");
            int result = CrcheckUtil.getCrc16(str16jinzhi);
            Log.d("ActImageList", "计算得到十进制数crc校验码：" + result);
            String result10to16 = String.format("0x%04x", result);
            Log.d("ActImageList", "十进制数crc校验码 转16进制：" + result10to16);


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public byte[] BitmapToBytes() {
        String chooseFilePath = chooseFile.getAbsolutePath();
        Bitmap bm = BitmapFactory.decodeFile(chooseFilePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * CRC-16 (Modbus) ：
     * 计算产生校验码
     * @param data 需要校验的数据
     * @return 校验码
     */
    public static String Make_CRC(byte[] data) {
        byte[] buf = new byte[data.length];// 存储需要产生校验码的数据
        for (int i = 0; i < data.length; i++) {
            buf[i] = data[i];
        }
        int len = buf.length;
        int crc = 0xFFFF;//16位
        for (int pos = 0; pos < len; pos++) {
            if (buf[pos] < 0) {
                crc ^= (int) buf[pos] + 256; // XOR byte into least sig. byte of
                // crc
            } else {
                crc ^= (int) buf[pos]; // XOR byte into least sig. byte of crc
            }
            for (int i = 8; i != 0; i--) { // Loop over each bit
                if ((crc & 0x0001) != 0) { // If the LSB is set
                    crc >>= 1; // Shift right and XOR 0xA001
                    crc ^= 0xA001;
                } else
                    // Else LSB is not set
                    crc >>= 1; // Just shift right
            }
        }
        String c = Integer.toHexString(crc);
        if (c.length() == 4) {
            c = c.substring(2, 4) + c.substring(0, 2);
        } else if (c.length() == 3) {
            c = "0" + c;
            c = c.substring(2, 4) + c.substring(0, 2);
        } else if (c.length() == 2) {
            c = "0" + c.substring(1, 2) + "0" + c.substring(0, 1);
        }
        return c;
    }


    /**
     * 删除选中的文件
     */
    public void deleteFile(){
        //删除此文件
        if( messageDialog == null){
            messageDialog = new MessageDialog(ActImageList.this);
        }
        messageDialog.show();
        messageDialog.setContent("确定删除此文件？");
        messageDialog.setBtnSure("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageDialog.dismiss();
                if(chooseFile != null){
                    chooseFile.delete();
                    imageView.setImageDrawable(null);
                    chooseFile = null;
                    getFilesList();
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
     * 保存文件到指定路径
     */
    public void saveFile(){
        mBitmap = BitmapFactory.decodeFile(chooseFile.getAbsolutePath());
        SimpleDateFormat time = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = time.format(System.currentTimeMillis());
        File currentFile = new File(FILES_DIR,fileName + ".jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(currentFile);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ToastUtil.showMessage("保存失败：" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            ToastUtil.showMessage("保存失败：" + fileName);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            ToastUtil.showMessage("添加成功：" + fileName);
            getFilesList();
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
        boolean isSuccess = chooseFile.renameTo(newFile);
        if(isSuccess){
            chooseFile = newFile;
            ToastUtil.showMessage("文件已经成功地被命名了" + chooseFile.getName());
            getFilesList();
        }else {
            ToastUtil.showMessage("命名失败");
        }
    }

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
        ActivityCompat.requestPermissions(ActImageList.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
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

        AlertDialog dialog = new AlertDialog.Builder(ActImageList.this)
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

        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }
    }

}
