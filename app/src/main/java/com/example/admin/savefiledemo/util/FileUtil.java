package com.example.admin.savefiledemo.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileUtil {

    /**
     * 获得外部存储绝对路径
     * @return
     */
    public static File getSdDirFile(){
        File sdDir = Environment.getExternalStorageDirectory();//外部存储绝对路径
        return sdDir;
    }

    /**
     * 创建文件夹
     * @param fileName 文件夹名
     * @return
     */
    public static File createFolder(String fileName){
        File fileDir = new File(getSdDirFile().getPath() + "/" + fileName);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        return fileDir;
    }

    /**
     * 创建文件
     *
     * @param filePath 文件夹绝对路径
     * @param fileName 文件名
     * @return
     */
    public static boolean createFile(String filePath, String fileName) {

        File file = new File(filePath);
        if (!file.exists()) {
            /**  注意这里是 mkdirs()方法  可以创建多个文件夹 */
            file.mkdirs();
        }
        File subfile = new File(filePath, fileName);
        if (!subfile.exists()) {
            try {
                boolean b = subfile.createNewFile();
                return b;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return true;
        }
        return false;
    }

    /**
     * 遍历文件夹下的所有文件
     *
     * @param fileDir 文件夹
     */
    public static List<File> getFiles(File fileDir) {
        List<File> list = new ArrayList<>();
        File[] fileArray = fileDir.listFiles();
        if (fileArray == null) {
            return null;
        } else {
            for (File f : fileArray) {
                if (f.isFile()) {
                    list.add(0, f);
                } else {
                    getFiles(f);
                }
            }
        }
        return list;
    }

    /**
     * 获得指定文件
     * @param nameList 文件名列表
     * @param folderPath 文件夹绝对路径
     * @return
     */
    public  static List<File> getFiles(List<String> nameList, String folderPath){
        List<File> list = new ArrayList<>();
        if (nameList == null || nameList.size() == 0) {
            return null;
        } else {
            for( int i = 0; i < nameList.size(); i++){
                File file = new File(folderPath + "/" + nameList.get(i));
                if(file.exists()){
                    list.add(file);
                }
            }
        }
        return list;
    }

    /**
     * 删除文件夹里的所有文件
     *
     * @param fileDirPath 文件夹绝对路径
     * @return
     */
    public static boolean deleteFolder(String fileDirPath) {
        List<File> files = getFiles(new File(fileDirPath));
        if (files.size() != 0) {
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                /**  如果是文件则删除  如果都删除可不必判断  */
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
        return true;
    }

    /**
     * 删除文件
     *
     * @param filePath 文件绝对路径
     * @return
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return file.delete();
    }

    /**
     * 删除文件
     *
     * @param file 需要删除的文件
     * @return
     */
    public static boolean deleteFile(File file) {
        return file.delete();
    }



    /**
     * 向文件中的指定位置添加String内容：
     * RandomAccessFile类的主要功能是完成随机读取功能，可以读取指定位置的内容。
     * @param strContent 内容
     * @param fileDirPath   文件夹绝对路径
     * @param fileName   文件名
     */
    public static boolean writeTextFileRandom(String strContent, String fileDirPath, String fileName) {
        File file = new File(fileDirPath);
        if(!file.exists()){
            file.mkdir();
        }
        File subfile = new File(fileDirPath, fileName);
        RandomAccessFile raf = null;
        try {
            // 构造函数 第二个是读写模式，如果文件不存在，会自动创建
            raf = new RandomAccessFile(subfile, "rw");
            // 将记录指针移动到该文件的最后
            raf.seek(subfile.length());
            // 向文件末尾追加内容
            raf.write(strContent.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }finally {
            try{
                if(raf != null){
                    raf.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return true;
    }


    /**
     * 修改文件内容（覆盖或者添加String）
     *
     * @param fileDirPath    文件夹绝对路径
     * @param content 内容
     * @param isAppend  指定是覆盖写还是追加写(true=追加)(false=覆盖)
     */
    public static boolean writeTextFile(String fileDirPath, String fileName, String content, boolean isAppend) {
        File file = new File(fileDirPath);
        if(!file.exists()){
            file.mkdir();
        }
        FileOutputStream fileOutputStream = null;
        BufferedWriter writer = null;
        try {
            fileOutputStream = new FileOutputStream(fileDirPath + "/" + fileName, isAppend);
            writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * 读取文件内容String
     *
     * @param fileDirPath 文件夹绝对路径
     * @param fileName 文件名
     * @return 返回内容
     */
    public static String readTextFile(String fileDirPath, String fileName) {
        StringBuffer sb = new StringBuffer("");
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(fileDirPath + "/" + fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return e.toString();
        }
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return e1.toString();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.toString();
        }finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * 重命名文件
     *
     * @param oldPath 原来的文件绝对路径
     * @param newPath 新的文件绝对路径
     */
    public static boolean renameFile(String oldPath, String newPath) {
        File oleFile = new File(oldPath);
        File newFile = new File(newPath);
        //执行重命名
        return oleFile.renameTo(newFile);
    }

    /**
     * 重命名文件
     * @param oldFile 原来的文件
     * @param newPath 新的文件绝对路径
     * @return
     */
    public static boolean renameFile(File oldFile, String newPath) {
        File newFile = new File(newPath);
        //执行重命名
        return oldFile.renameTo(newFile);
    }

    /**
     * 重命名文件
     * @param oldFile 原来的文件
     * @param newFile 新的文件
     * @return
     */
    public static boolean renameFile(File oldFile, File newFile) {
        //执行重命名
        return oldFile.renameTo(newFile);
    }


    /**
     * 复制文件夹
     *
     * @param srcFile 要复制的文件夹绝对路径
     * @param subFile   要粘贴的文件夹绝对路径
     * @return 是否复制成功
     */
    public static boolean copyFolder(String srcFile, String subFile) {
        //要复制的文件夹目录
        File[] currentFiles;
        File root = new File(srcFile);
        //如同判断SD卡是否存在或者文件夹是否存在
        //如果不存在则 return出去
        if (!root.exists()) {
            return false;
        }
        //如果存在则获取当前目录下的全部文件 填充数组
        currentFiles = root.listFiles();

        //目标目录
        File targetDir = new File(subFile);
        //创建目录
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        //遍历要复制该目录下的全部文件
        for (int i = 0; i < currentFiles.length; i++) {
            if (currentFiles[i].isDirectory())//如果当前项为子目录 进行递归
            {
                copyFolder(currentFiles[i].getAbsolutePath(), subFile + "/" + currentFiles[i].getName());

            } else//如果当前项为文件则进行文件拷贝
            {
                copySdcardFile(currentFiles[i].getAbsolutePath(), subFile + "/" + currentFiles[i].getName());
            }
        }
        return true;
    }


    /**
     * 复制文件:
     * @param srcFile 要复制的文件绝对路径
     * @param subFile  要粘贴的文件绝对路径
     * @return
     */
    public static boolean copySdcardFile(String srcFile, String subFile) {
        try {
            InputStream fosfrom = new FileInputStream(srcFile);
            OutputStream fosto = new FileOutputStream(subFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            return true;

        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 拷贝图片文件
     * @param srcImage 原图片
     * @param subImagePath 新图片保存的绝对路径
     * @param subImageName 新图片的名字
     * @return
     */
    public static boolean saveOrCopyImage(File srcImage,String subImagePath, String subImageName){
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeFile(srcImage.getAbsolutePath());
        File newImage = new File(subImagePath,subImageName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(newImage);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
           return true;
        }
    }

    /**
     * 修改文件内容String（相对路径）
     * 使用BufferedWriter对象
     * Context.MODE_PRIVATE：为默认操作模式，代表该文件是私有数据，
     * 只能被应用本身访问，在该模式下，写入的内容会覆盖原文件的内容。
     * Context.MODE_APPEND：模式会检查文件是否存在，存在就往文件追加内容，否则就创建新文件。
     * @param context
     * @param inputText 内容
     * @param fileName 文件名
     * @param saveMode 写入模式
     * @return
     */
    public static boolean writeTextFile(Context context, String inputText, String fileName, int saveMode) {
        FileOutputStream fileOutputStream = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileOutputStream = context.openFileOutput(fileName, saveMode);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            bufferedWriter.write(inputText);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 修改文件内容String（是否追加）（相对路径）
     * @param context
     * @param inputText 内容
     * @param fileName 文件名
     * @param isAppend 是否追加：true在文本末尾追加内容，false覆盖文本内容
     * @return
     */
    public static boolean writeTextFile(Context context, String inputText, String fileName, boolean isAppend) {
        FileOutputStream fileOutputStream = null;
        BufferedWriter bufferedWriter = null;
        try {
            if(isAppend){
                fileOutputStream = context.openFileOutput(fileName, Context.MODE_APPEND);
            }else {
                fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            }
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            bufferedWriter.write(inputText);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 读取文件内容String（相对路径）
     * @param context
     * @param fileName 文件名
     * @return
     */
    public static String readTextFile(Context context, String fileName) {
        FileInputStream fileInputStream = null;
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            fileInputStream = context.openFileInput(fileName);
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.toString();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder.toString();
    }

}
