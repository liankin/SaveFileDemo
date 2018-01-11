package com.example.admin.savefiledemo.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Environment;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import com.example.admin.savefiledemo.util.FileUtil;
import com.example.admin.savefiledemo.util.ToastUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by admin on 2017/12/14.
 */

public class TuYaMoreView extends View {

    private int mode = 0;// 当前所处模式
    private static final int MODE_NO = 0;//无模式
    private static final int MODE_PAINT = 1;//画笔模式
    private static final int MODE_DRAG_ZOOM = 2;//拖动和放大缩小照片模式
    private static final int MODE_ADD_IMAGE = 3;//贴图模式

    private float startDis;//两个手指的开始距离
    private PointF midPoint;//两个手指的中间点

    private Bitmap mBitmap;//叠加的图层
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;// 画布的画笔
    private Paint mPaint;// 真实的画笔
    private float mX, mY;// 单击时，临时点坐标
    private static final float TOUCH_TOLERANCE = 4;
    private static List<DrawPath> savePath;//保存Path路径的集合,用List集合来模拟栈
    private DrawPath dp;//记录Path路径的对象
    private class DrawPath {
        public Path path;// 路径
        public Paint paint;// 画笔
    }

    private Matrix mMatrix = new Matrix();//用于记录拖拉图片移动的坐标位置
    private Matrix currentMatrix = new Matrix();//用于记录图片要进行拖拉时候的坐标位置
    private String addImageFilePath = null;//贴图的图片文件的绝对路径

    private String folderPath = null;//文件夹绝对路径

    public List<File> getSrcFileList() {
        return srcFileList;
    }
    private List<File> srcFileList = new ArrayList<>();
    public int getCurrentFileIndex() {
        return currentFileIndex;
    }
    private int currentFileIndex = 0;
    private List<Bitmap> bitmapList = new ArrayList<>();//图片对象数组

    public TuYaMoreView(Context context, String folderPath) {
        super(context);
        this.folderPath = folderPath;
        File folder = new File(folderPath);
        if(!folder.exists()){
            return;
        }
        srcFileList = FileUtil.getFiles(folder);
        if(srcFileList == null || srcFileList.size() == 0){
            return;
        }
        for( int i = 0; i < srcFileList.size(); i++){
            Bitmap bitmap = BitmapFactory.decodeFile(srcFileList.get(i).getAbsolutePath()).copy(Bitmap.Config.ARGB_8888, true);
            bitmapList.add(bitmap);
        }
        currentFileIndex = 0;
        mBitmap = bitmapList.get(currentFileIndex);
        //根据底图生成Canvas
        mCanvas = new Canvas(mBitmap);
        //设置画布的画笔
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        //设置真实的画笔样式
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(5);
        //定义存储画笔轨迹的数组
        savePath = new ArrayList<DrawPath>();
    }

    /**
     * 实时更新界面：
     * 调用invalidate();就会调用此方法；
     * 只是实时的让界面在视觉上发生改动，但mBitmap图片本身、mCanvas图层本身并没有任何的改动；
     * 界面上实时的显示画笔的痕迹；
     * @param canvas
     */
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmap != null) {
            // 将前面已经画过得显示出来；mMatrix只对图片起作用，所以画布并没有变化
            canvas.drawBitmap(mBitmap,mMatrix,mBitmapPaint);
        }
    }

    /**
     * 获得、设置画笔
     * @return
     */
    public Paint getmPaint() {
        return mPaint;
    }

    public void setmPaint(Paint mPaint) {
        this.mPaint = mPaint;
    }

    /**
     * 触摸开始：记录当前触摸的位置
     * @param x
     * @param y
     */
    private void touch_start(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        mCanvas.drawPath(mPath, mPaint);
    }

    /**
     * 触摸移动
     * @param x
     * @param y
     */
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(mY - y);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            // 从x1,y1到x2,y2画一条贝塞尔曲线，更平滑(直接用mPath.lineTo也是可以的)
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
            mCanvas.drawPath(mPath, mPaint);
        }
    }

    /**
     * 触摸结束：结束画笔后，把轨迹真正地画到mBitmap图片上
     */
    private void touch_up() {
        mPath.lineTo(mX, mY);
        mCanvas.drawPath(mPath, mPaint);
        //将一条完整的路径保存下来(相当于入栈操作)
        savePath.add(dp);
        mPath = null;
    }

    /**
     * 处理Touch事件
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(mode == MODE_ADD_IMAGE){
                    mX = x;
                    mY = y;
                }else {
                    if(mode == MODE_NO){
                        /** 单指模式 */
                        mode = MODE_PAINT;
                    }
                    //每次down下去重新new一个Path
                    mPath = new Path();
                    //每一次记录的路径对象是不一样的
                    dp = new DrawPath();
                    dp.path = mPath;
                    dp.paint = mPaint;
                    touch_start(x, y);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_2_DOWN:
                /** 双指模式 */
                mode = MODE_DRAG_ZOOM;
                mPath = null;
                //计算两个手指间的距离
                startDis = distance(event);
                //计算两个手指间的中间点
                if (startDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                    midPoint = mid(event);
                    //记录当前ImageView的缩放倍数
                    currentMatrix.set(mMatrix);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                /** 画笔 */
                if(mode == MODE_PAINT){
                    touch_move(x, y);
                }
                /** 缩放移动 */
                if( mode == MODE_DRAG_ZOOM && event.getPointerCount() >= 2){
                    float endDis = distance(event);// 结束距离
                    // 缩放：两个手指并拢在一起的时候像素大于10
                    float scale = endDis / startDis;// 得到缩放倍数
                    mMatrix.set(currentMatrix);
                    mMatrix.postScale(scale, scale,midPoint.x,midPoint.y);
                    //拖动
                    float tragX = event.getX() - mX; // 得到x轴的移动距离
                    float tragY = event.getY() - mY; // 得到x轴的移动距离
                    mMatrix.postTranslate(tragX, tragY);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if(mode == MODE_PAINT){
                    touch_up();
                }
                if(mode == MODE_DRAG_ZOOM){
                    mCanvas.setMatrix(mMatrix);
                    mode = MODE_NO;
                }
                if(mode == MODE_ADD_IMAGE){
                    addImage();
                    mode = MODE_NO;
                }
                invalidate();
                break;
        }
        return true;
    }

    /** 计算两个手指间的距离 */
    private float distance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        /** 使用勾股定理返回两点之间的距离 */
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /** 计算两个手指间的中间点 */
    private PointF mid(MotionEvent event) {
        float midX = (event.getX(1) + event.getX(0)) / 2;
        float midY = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(midX, midY);
    }

    /**
     * 上一张图片
     */
    public void previousImage(){
        bitmapList.set(currentFileIndex,mBitmap);//保存当前图片
        currentFileIndex--;
        if(srcFileList != null && srcFileList.size() > 0 && currentFileIndex < 0){
            currentFileIndex = srcFileList.size() - 1;
        }
        mBitmap = bitmapList.get(currentFileIndex);//加载新的一张图片
        mCanvas.setBitmap(mBitmap);
        // 清空画布
        if (savePath != null && savePath.size() > 0) {
            savePath.clear();
        }
        invalidate();// 刷新
    }

    /**
     * 下一张图片
     */
    public void nextImage(){
        bitmapList.set(currentFileIndex,mBitmap);//保存当前图片
        currentFileIndex++;
        if(srcFileList != null && currentFileIndex >= srcFileList.size()){
            currentFileIndex = 0;
        }
        mBitmap = bitmapList.get(currentFileIndex);//加载新的一张图片
        mCanvas.setBitmap(mBitmap);
        // 清空画布
        if (savePath != null && savePath.size() > 0) {
            savePath.clear();
        }
        invalidate();// 刷新
    }

    /**
     * 保存涂鸦结果图片
     */
    public void saveResultImage(){
        File sdDir = Environment.getExternalStorageDirectory();
        File fileDir = new File(sdDir.getPath() + "/SAVEFILEDEMO/TUYA/测试涂鸦");//创建文件夹
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        for( int i = 0; i < bitmapList.size(); i++){
            String fileUrl = fileDir.getAbsolutePath() + "/" + i + ".jpg";
            try {
                FileOutputStream fos = new FileOutputStream(new File(fileUrl));
                bitmapList.get(i).compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ToastUtil.showMessage("保存结束_" + i);
        }
    }

    /**
     * 撤销：
     * 撤销的核心思想就是将画布清空，
     * 将保存下来的Path路径最后一个移除掉，
     * 重新将路径画在画布上面。
     */
    public void revocationGraffity() {
        mBitmap = bitmapList.get(currentFileIndex);
        mCanvas.setBitmap(mBitmap);// 重新设置画布，相当于清空画布
        // 清空画布，但是如果图片有背景的话，则使用上面的重新初始化的方法，用该方法会将背景清空掉…
        if (savePath != null && savePath.size() > 0) {
            // 移除最后一个path,相当于出栈操作
            savePath.remove(savePath.size() - 1);
            Iterator<DrawPath> iter = savePath.iterator();
            while (iter.hasNext()) {
                DrawPath drawPath = iter.next();
                mCanvas.drawPath(drawPath.path, drawPath.paint);
            }
            invalidate();// 刷新
        }
        ToastUtil.showMessage("撤销结束");
    }

    /**
     * 清屏:
     * 清屏的核心思想就是将撤销的路径保存到另外一个集合里面(栈)，
     * 然后从redo的集合里面取出最顶端对象，
     * 画在画布上面即可。
     */
    public void cleanGraffity() {
        mBitmap = bitmapList.get(currentFileIndex);
        mCanvas.setBitmap(mBitmap);// 重新设置画布，相当于清空画布
        // 清空画布
        if (savePath != null && savePath.size() > 0) {
            savePath.clear();
            invalidate();// 刷新
        }
        ToastUtil.showMessage("清屏结束");
    }

    /**
     * 开启贴图功能
     * @param addImageFilePath
     */
    public void openAddImageMode(String addImageFilePath){
        this.addImageFilePath = addImageFilePath;
        mode = MODE_ADD_IMAGE;
    }

    /**
     * 执行贴图
     */
    public void addImage(){
        if( mBitmap == null || TextUtils.isEmpty(addImageFilePath)){
            return;
        }
        Bitmap markBitmap = BitmapFactory.decodeFile(addImageFilePath).copy(Bitmap.Config.ARGB_8888, true);
        markBitmap = Bitmap.createBitmap(markBitmap, 0, 0, markBitmap.getWidth()/3, markBitmap.getHeight()/3);
        mCanvas.drawBitmap( markBitmap, mX, mY, null );//在点击的位置贴图
        ToastUtil.showMessage("贴图结束");
    }

    /**
     * 开启画笔模式
     */
    public void openPaintMode(){
        mode = MODE_PAINT;
    }
}


