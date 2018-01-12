package com.example.admin.savefiledemo;

import android.os.Environment;

import java.io.File;

/**
 * Created by admin on 2017/11/21.
 */

public class Constant {

    public static final String DATA = "_data";

    private static final File SD_DIR = Environment.getExternalStorageDirectory();

    public static final String DIR_FILE_PATH = "/SaveFileDemo";//所有文件保存的根目录文件夹的绝对路径

    public static final String IMAGE_FILE_PATH = "/images";//图片保存的文件夹名字

    public static final String TXT_FILE_PATH = "/txtFiles";//txt文本保存的文件夹名字
    public static final String TXT_FILE_NAME = "mytextdata";//txt文本文件的名字

    public static final String SIGNAYURE_FILE_PATH = "/mySignature";//签名结果图保存的文件夹名字
    public static final String SIGNATURE_FILE_NAME = "个人签名.png";//txt文本文件的名字

    public static final String GRAFFITY_SRC_FILE_PATH = "/graffity/graffitySrcImage";//涂鸦源图保存的文件夹名字
    public static final String GRAFFITY_DES_FILE_PATH = "/graffity/graffityDesImage";//涂鸦结果图保存的文件夹名字

    public static final String IMG_URL = "http://pic1.win4000.com/wallpaper/8/575e50b24e386.jpg";//下载图片的url

    /**
     * 获得文件夹对象
     * @param folderName
     * @return
     */
    public static File getFileDir(String folderName){
        File fileDir = new File(SD_DIR.getAbsoluteFile() + DIR_FILE_PATH + folderName);
        if (!fileDir.exists()) {
            // 必须要先有父文件夹才能在父文件夹下建立想要的子文件夹
            // 即LIMS文件必须存在，才能建立IMG文件夹
            fileDir.mkdir();
        }
        return fileDir;
    }

}
