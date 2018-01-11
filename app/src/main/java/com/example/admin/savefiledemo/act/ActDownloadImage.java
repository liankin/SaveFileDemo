package com.example.admin.savefiledemo.act;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.admin.savefiledemo.R;
import com.example.admin.savefiledemo.util.ImageUtil;
import com.example.admin.savefiledemo.util.ToastUtil;
import com.example.admin.savefiledemo.views.LoadingDialog;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 在线预览图片、下载保存原图、下载并保存压缩后的图片、删除图片文件
 */
public class ActDownloadImage extends AppCompatActivity {

    @BindView(R.id.btn_preview)
    Button btnPreview;
    @BindView(R.id.btn_download)
    Button btnDownload;
    @BindView(R.id.btn_download_and_zip)
    Button btnDownloadAndZip;
    @BindView(R.id.img)
    ImageView img;
    @BindView(R.id.btn_delete)
    Button btnDelete;
    @BindView(R.id.btn_list_image)
    Button btnListImage;

    public static String imgUrl = "http://pic1.win4000.com/wallpaper/8/575e50b24e386.jpg";
    private File fileDir;
    private String saveFilePath;
    private Bitmap mBitmap;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,};//SD卡读写的权限
    private static final int REQUEST_EXTERNAL_STORAGE = 21;
    private final static int RESULT_PERMISSION = 1001;
    private final static int RESULT_SUCCESS = 3;//数值小于16

    private LoadingDialog loadingDialog;
    private boolean isDownLoadFile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_image);
        ButterKnife.bind(this);

        // Environment.getExternalStorageDirectory()获取外部存储的根路径，
        // 返回的路径中最后一个字符不是/，如果需要创建子目录，需要在子目录的前后都加上/
        File sdDir = Environment.getExternalStorageDirectory();
        fileDir = new File(sdDir.getPath() + "/SAVEFILEDEMO/IMG");
        if (!fileDir.exists()) {
            // 必须要先有父文件夹才能在父文件夹下建立想要的子文件夹
            // 即LIMS文件必须存在，才能建立IMG文件夹
            fileDir.mkdir();
        }
        // 获取外部存储状态
        String state = Environment.getExternalStorageState();
        // 如果状态不是mounted，无法读写
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
    }

    @OnClick({R.id.btn_preview, R.id.btn_download, R.id.btn_delete, R.id.btn_list_image, R.id.btn_download_and_zip})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_preview:
                useXGetInternetImage();
                break;
            case R.id.btn_download:
                isDownLoadFile = true;
//                verifyStoragePermissions();
                downloadFile();
                break;
            case R.id.btn_delete:
                deleteFile();
                break;
            case R.id.btn_list_image:
                isDownLoadFile = false;
                verifyStoragePermissions();
                break;
            case R.id.btn_download_and_zip:
                downloadAndZipImage();
                break;
        }
    }

    /**
     * 使用xUtils绑定网络图片：使用ImageOptions设置图片属性
     */
    public void useXGetInternetImage() {
//            x.image().bind(img, imgUrl);
        x.image().bind(img, imgUrl, new Callback.CommonCallback<Drawable>() {
            @Override
            public void onSuccess(Drawable result) {
                ToastUtil.showMessage("加载图片成功");
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ToastUtil.showMessage(ex.toString());
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });

//        //设置图片属性的options
//        ImageOptions imageOptions = new ImageOptions.Builder()
//                //设置图片的大小
//                .setSize(500, 500)
//                // 如果ImageView的大小不是定义为wrap_content, 不要crop.
//                .setCrop(true)
//                // 加载中或错误图片的ScaleType
//                //.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
//                .setImageScaleType(ImageView.ScaleType.CENTER_CROP)
//                //设置加载过程中的图片
//                .setLoadingDrawableId(R.mipmap.ic_launcher)
//                //设置加载失败后的图片
//                .setFailureDrawableId(R.mipmap.ic_launcher)
//                //设置使用缓存
//                .setUseMemCache(true)
//                //设置支持gif
//                .setIgnoreGif(false)
//                //设置显示圆形图片
//                .setCircular(true).build();
//        x.image().bind(img, imgUrl, imageOptions, new Callback.CommonCallback<Drawable>() {
//            @Override
//            public void onSuccess(Drawable result) {
//                ToastUtil.showMessage("加载图片成功");
//            }
//
//            @Override
//            public void onError(Throwable ex, boolean isOnCallback) {
//                ToastUtil.showMessage(ex.toString());
//            }
//
//            @Override
//            public void onCancelled(CancelledException cex) {
//
//            }
//
//            @Override
//            public void onFinished() {
//
//            }
//        });

    }

    /**
     * 下载并压缩图片：
     *  x.image().loadDrawable()方法并不会保存图片，只是可以显示出来而已，即在线预览功能；
     *  但通过此方法可以获取到图片对象，然后进行比例、质量压缩，
     *  然后再把压缩后的图片保存到本地保存到本地。
     */
    public void downloadAndZipImage() {
        ImageOptions imageOptions = new ImageOptions.Builder()
//                    .setSize(DensityUtil.dip2px(120), DensityUtil.dip2px(120))
//                    .setRadius(DensityUtil.dip2px(5))
                // 如果ImageView的大小不是定义为wrap_content, 不要crop.设置成fasle就好了
//                    .setCrop(false) // 很多时候设置了合适的scaleType也不需要它.
                // 加载中或错误图片的ScaleType
                //.setPlaceholderScaleType(ImageView.ScaleType.MATRIX)
//                    .setImageScaleType(ImageView.ScaleType.FIT_XY)
                .setLoadingDrawableId(R.mipmap.ic_launcher)//加载中的图片
                .setFailureDrawableId(R.mipmap.ic_launcher)//默认的图片
                .build();
        x.image().loadDrawable(imgUrl, imageOptions, new Callback.CommonCallback<Drawable>() {
            @Override
            public void onSuccess(Drawable result) {
                BitmapDrawable bd = (BitmapDrawable) result;
//                Bitmap bitmap = bd.getBitmap();
//                img.setImageBitmap(bitmap);//只是显示在界面上但并没有保存图片
                mBitmap = ImageUtil.proportionCompressImage(bd.getBitmap());//按比例压缩图片
                mBitmap = ImageUtil.qualityCompressImage(mBitmap);//质量压缩图片

                img.setImageBitmap(mBitmap);

//                //图片转base64字符串
//                String base64String = ImageUtil.bitmapToBase64(mBitmap);
//                //base64字符串转图片
//                mBitmap = ImageUtil.base64ToBitmap(base64String);
//                img.setImageBitmap(mBitmap);

                //保存图片到本地
//                SimpleDateFormat time = new SimpleDateFormat("yyyyMMddHHmmss");
//                String fileName = "compress_" + time.format(System.currentTimeMillis());
//                saveFilePath = fileDir.getAbsolutePath() + "/" + fileName + ".jpg";
//                if(ImageUtil.saveImageFile(mBitmap, fileDir.getAbsolutePath(), fileName + ".jpg")){
//                    ToastUtil.showMessage("保存成功");
//                }else {
//                    ToastUtil.showMessage("保存失败");
//                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });

    }

    /**
     * 下载文件
     */
    public void downloadFile() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(ActDownloadImage.this);
        }
        loadingDialog.show();

        SimpleDateFormat time = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = time.format(System.currentTimeMillis());
        saveFilePath = fileDir.getAbsolutePath() + "/" + fileName + ".jpg";
        //File.getAbsolutePath()获得文件绝对路径
        RequestParams requestParams = new RequestParams(imgUrl);
        requestParams.setSaveFilePath(saveFilePath);
        x.http().get(requestParams, new Callback.CommonCallback<File>() {
            @Override
            public void onSuccess(File file) {
                loadingDialog.dismiss();
                if (file != null) {
                    //根据图片绝对路径获取图片并显示在界面上
                    mBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
//                    img.setImageBitmap(mBitmap);

//                    //图片转base64字符串
                    String base64String = ImageUtil.bitmapToBase64(mBitmap);
//                    //base64字符串转图片
                    Bitmap bitmap = ImageUtil.base64ToBitmap(base64String);
                    img.setImageBitmap(bitmap);
                    ToastUtil.showMessage("下载图片成功");
//                    mBitmap = ImageUtil.getSmallBitmap(file.getAbsolutePath());
//                    img.setImageBitmap(mBitmap);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                loadingDialog.dismiss();
                ToastUtil.showMessage(ex.toString());
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 删除文件
     */
    public void deleteFile() {
        if (!TextUtils.isEmpty(saveFilePath)) {
            File file = new File(saveFilePath);
            file.delete();
            img.setImageDrawable(null);
            ToastUtil.showMessage("文件删除成功");
        } else {
            ToastUtil.showMessage("文件路径错误");
        }
    }

    /**
     * 读取指定文件夹里的所有文件（此处设定为图片）
     */
    public void readFiles() {
        Intent intent = new Intent(ActDownloadImage.this, ActImageList.class);
        startActivity(intent);
    }

    /**
     * 请求权限：下载图片、查看文件信息列表
     */
    public void verifyStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int i = ContextCompat.checkSelfPermission(ActDownloadImage.this, PERMISSIONS_STORAGE[0]);
            int j = ContextCompat.checkSelfPermission(ActDownloadImage.this, PERMISSIONS_STORAGE[1]);
            if (i != PackageManager.PERMISSION_GRANTED || j != PackageManager.PERMISSION_GRANTED) {
                //权限还没有授予，进行申请权限
                startRequestPermission();
            } else {
                if (isDownLoadFile) {
                    downloadFile();
                } else {
                    readFiles();
                }
            }
        } else {
            if (isDownLoadFile) {
                downloadFile();
            } else {
                readFiles();
            }
        }
    }

    /**
     * 开始提交请求权限
     */
    private void startRequestPermission() {
        ActivityCompat.requestPermissions(ActDownloadImage.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
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
                        // Picker.from(ActEditAgencyInfo.this).count(1).enableCamera(true).setEngine(new GlideEngine()).forResult(RESULT_LICENSE);
                        //      Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                        ToastUtil.showMessage("权限获取成功");
                    }
                }
            }
        }
    }

    /**
     * 提示用户去应用设置界面手动开启权限
     */
    private void showDialogTipUserGoToAppSettting() {

        AlertDialog dialog = new AlertDialog.Builder(ActDownloadImage.this)
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
        releaseImageViewResouce(img);
    }

    /**
     * 手动回收ImageView的图片资源
     *
     * @param imageView
     */
    public void releaseImageViewResouce(ImageView imageView) {
        if (imageView == null) {
            return;
        }
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }
    }
}
