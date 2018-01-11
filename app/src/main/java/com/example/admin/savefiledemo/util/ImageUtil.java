package com.example.admin.savefiledemo.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.forward.androids.Image.ImageCache;
import cn.forward.androids.utils.ImageUtils;
import cn.forward.androids.utils.ReflectUtil;
import cn.forward.androids.utils.Util;

/**
 * Created by Administrator on 2016/7/7 0007.
 */
public class ImageUtil {

    /**
     * Base64转字符串为bitmap
     * @param base64Data
     * @return
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * 根据图片绝对路径将其转换成Base64字符串
     * @param filePath 图片绝对路径
     * @return
     */
    public static String bitmapToBase64(String filePath) {
        String result = null;
        //获得原始图片
        Bitmap bm = BitmapFactory.decodeFile(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        //1.5M的压缩后在100Kb以内，测试得值,压缩后的大小=94486字节,压缩后的大小=74473字节
        //这里的JPEG 如果换成PNG，那么压缩的就有600kB这样
        //quality(0~100)为40
        bm.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] b = baos.toByteArray();
        result = Base64.encodeToString(b, Base64.DEFAULT);
        try{
            baos.flush();
            baos.close();
        }catch (IOException e){
            e.printStackTrace();
        }try {
            if (baos != null) {
                baos.flush();
                baos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 把bitmap转换成Base64字符串
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
                baos.flush();
                baos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 把bitmap压缩后转换成Base64字符串
     * @param filePath 图片绝对路径
     * @return
     */
    public static String bitmapDecodeToString(String filePath) {
        //压缩图片
        Bitmap bm = getSmallBitmap(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        //1.5M的压缩后在100Kb以内，测试得值,压缩后的大小=94486字节,压缩后的大小=74473字节
        //这里的JPEG 如果换成PNG，那么压缩的就有600kB这样
        bm.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    /**
     * 按照图片尺寸压缩：
     * 根据路径获得图片并按照图片尺寸压缩，返回bitmap用于显示
     * @param filePath
     * @return
     */
    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();

        // 第一次解析时，inJustDecodeBounds设置为true，
        // 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        //设置图片的缩放值
        int reqWidth = 480;
        int reqHeight = 800;
        //原始图片的宽高
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;

        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(filePath, options);
        int desWidth = (int) (width / inSampleSize);
        int desHeight = (int) (height / inSampleSize);
        bitmap = Bitmap.createScaledBitmap(bitmap, desWidth, desHeight, true);
//        try {
//            FileOutputStream fos = null;
//            fos = new FileOutputStream(filePath);
//            if (bitmap != null) {
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        return bitmap;
    }


    /**
     * 计算图片的缩放值（比例压缩）
     * @param options
     * @param reqWidth 期望压缩的宽度
     * @param reqHeight 期望压缩的高度
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
        //原始图片的宽高
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * 保存图片到本地指定路径
     * @param fileDirPath 保存的文件夹绝对路径
     * @param fileName 图片名
     * @return
     */
    public static boolean saveImageFile(Bitmap mBitmap,String fileDirPath, String fileName){
//        Bitmap mBitmap = BitmapFactory.decodeFile(fileDirPath + "/" + fileName);
        File currentFile = new File(fileDirPath,fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(currentFile);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 比例压缩图片
     *
     * @param image  需要进行压缩的Bitmap对象
     * @return
     */
    public static Bitmap proportionCompressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        //判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
        if (baos.toByteArray().length / 1024 > 1024) {
            //重置baos即清空baos
            baos.reset();
            //这里压缩50%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0){
            be = 1;
        }
        newOpts.inSampleSize = be;//设置缩放比例
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;//降低图片从ARGB888到RGB565
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return bitmap;//压缩好比例大小后再进行质量压缩
    }

    /**
     * 质量压缩图片
     *
     * @param image 需要进行压缩的Bitmap对象
     * @return
     */
    public static Bitmap qualityCompressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        //循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > 100) {
            //重置baos即清空baos
            baos.reset();
            //每次都减少10
            options -= 10;
            //这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
        //把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        //把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }


    //*******************************************************************************************

    /**
     * 获取一定尺寸范围内的的图片，防止oom。参考系统自带相机的图片获取方法
     *
     * @param path      路径
     * @param maxWidth  图片的最大宽
     * @param maxHeight 图片的最大高
     * @return 经过按比例缩放的图片
     * @throws IOException
     */
    public static final Bitmap createBitmapFromPath(String path, int maxWidth, int maxHeight) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = null;
        if (path.endsWith(".3gp")) {
            return ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
        } else {
            try {
                options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, options);
                int width = options.outWidth;
                int height = options.outHeight;
                options.inSampleSize = computeBitmapSimple(width * height, maxWidth * maxHeight * 2);
                options.inPurgeable = true;
                options.inPreferredConfig = Bitmap.Config.RGB_565;//有A的才能加载透明图片时，透明部分不变黑
                options.inDither = false;
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeFile(path, options);
                return ImageUtils.rotateBitmapByExif(bitmap, path, true);
            } catch (OutOfMemoryError error) {//内容溢出，则再次缩小图片
                options.inSampleSize *= 2;
                bitmap = BitmapFactory.decodeFile(path, options);
                return ImageUtils.rotateBitmapByExif(bitmap, path, true);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * 计算bitmap的simple值
     *
     * @param realPixels,图片的实际像素，
     * @param maxPixels,压缩后最大像素
     * @return simple值
     */
    public static int computeBitmapSimple(int realPixels, int maxPixels) {
        if (maxPixels <= 0) {
            return 1;
        }
        try {
            if (realPixels <= maxPixels) {//如果图片尺寸小于最大尺寸，则直接读取
                return 1;
            } else {
                int scale = 2;
                while (realPixels / (scale * scale) > maxPixels) {
                    scale *= 2;
                }
                return scale;
            }
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * 保存图片到本地指定路径
     *
     * @return
     */
    public static boolean saveImageFile(Bitmap mBitmap, File file) {
//        Bitmap mBitmap = BitmapFactory.decodeFile(fileDirPath + "/" + fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    //*******************************************************************************************

}

