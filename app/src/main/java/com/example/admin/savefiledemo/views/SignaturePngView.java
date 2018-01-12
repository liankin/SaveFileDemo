package com.example.admin.savefiledemo.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.admin.savefiledemo.util.ToastUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * 涂鸦并保存带字的透明PNG图片的自定义View
 */
public class SignaturePngView extends View {

    public Bitmap getmBitmap() {
        return mBitmap;
    }

    private Bitmap mBitmap;//结果图片
    private Paint paint;
    private Path path;
    private float downX,downY;
    private float tempX,tempY;
    private  int paintWidth = 10;//画笔大小
    private List<DrawPath> drawPathList;//保存当前绘制的所有画笔路径
    private List<DrawPath> savePathList;//保存撤销的所有画笔路径

    /**
     * 前景色
     */
    private int mPenColor = Color.BLACK;

    private int mBackColor=Color.TRANSPARENT;

    //画笔路径对象
    private class DrawPath {
        Path path;
        Paint paint;
    }

    public SignaturePngView(Context context) {
        this(context,null);
    }
    public SignaturePngView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    public SignaturePngView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        drawPathList = new ArrayList<>();
        savePathList = new ArrayList<>();
        initPaint();
    }

    /**
     * 初始化画笔及其样式
     */
    private void initPaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(paintWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(mPenColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(drawPathList != null && !drawPathList.isEmpty() ){
            for(DrawPath drawPath:drawPathList){
                if(drawPath.path != null){
                    canvas.drawPath(drawPath.path,drawPath.paint);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                path = new Path();//每次手指下去都是一条新的路径
                path.moveTo(downX,downY);
                DrawPath drawPath = new DrawPath();
                drawPath.paint = paint;
                drawPath.path = path;
                drawPathList.add(drawPath);
                invalidate();
                tempX = downX;
                tempY = downY;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                path.quadTo(tempX,tempY,moveX,moveY);
                invalidate();
                tempX = moveX;
                tempY = moveY;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }
    /**
     * 撤销功能
     */
    public void undo() {
        if(drawPathList!=null&&drawPathList.size()>=1){
            savePathList.add(drawPathList.get(drawPathList.size()-1));
            drawPathList.remove(drawPathList.size()-1);
            invalidate();
        }
    }
    /**
     * 反撤销功能
     */
    public void reundo() {
        if (savePathList != null && !savePathList.isEmpty()) {
            drawPathList.add(savePathList.get(savePathList.size() - 1));
            savePathList.remove(savePathList.size() - 1);
            invalidate();
        }
    }
    /**
     * 改变画笔颜色
     * @param color
     */
    public void resetPaintColor(int color) {
        paint.setColor(color);
    }
    /**
     * 改变画笔的大小
     */
    public void resetPaintWidth() {
        paintWidth+=2;
        paint.setStrokeWidth(paintWidth);
    }
    /**
     * 橡皮擦功能 把画笔的颜色和view的背景颜色一样就ok,然后把画笔的宽度变大了,擦除的时候显得擦除范围大点
     */
    public void eraser() {
        paint.setColor(Color.WHITE);//这是view背景的颜色
        paint.setStrokeWidth(paintWidth+6);
    }

    /**
     * 清屏
     */
    public void cleanAll() {
        if(drawPathList != null){
            drawPathList.clear();
            if(savePathList!= null){
                savePathList.clear();
            }
            invalidate();
        }
    }

    /**
     * 保存画板
     *
     * @param path       保存到路劲
     * @param clearBlank 是否清楚空白区域
     * @param blank  边缘空白区域
     */
    public void saveResultImage(String path, boolean clearBlank, int blank) throws IOException {
        // 生成与此控件View一样宽高的Bitmap
        // 若使背景为透明，必须设置为Bitmap.Config.ARGB_4444
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(mBitmap);
        for(int i=0;i<drawPathList.size();i++){
            mCanvas.drawPath(drawPathList.get(i).path, drawPathList.get(i).paint);
        }
        if(clearBlank){
            mBitmap = clearBlank(mBitmap, blank);
        }
//        File sdDir = Environment.getExternalStorageDirectory();
//        File fileDir = new File(sdDir.getPath() + "/SAVEFILEDEMO/TUYA");
//        if (!fileDir.exists()) {
//            // 必须要先有父文件夹才能在父文件夹下建立想要的子文件夹
//            // 即LIMS文件必须存在，才能建立IMG文件夹
//            fileDir.mkdir();
//        }
//        String fileUrl = fileDir.getAbsolutePath()+"/个人签名.png";
        try {
            FileOutputStream fos = new FileOutputStream(new File(path));
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 逐行扫描 清楚边界空白。
     *
     * @param bp
     * @param blank 边距留多少个像素
     * @return
     */
    private Bitmap clearBlank(Bitmap bp, int blank) {
        int HEIGHT = bp.getHeight();
        int WIDTH = bp.getWidth();
        int top = 0, left = 0, right = 0, bottom = 0;
        int[] pixs = new int[WIDTH];
        boolean isStop;
        for (int y = 0; y < HEIGHT; y++) {
            bp.getPixels(pixs, 0, WIDTH, 0, y, WIDTH, 1);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    top = y;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        for (int y = HEIGHT - 1; y >= 0; y--) {
            bp.getPixels(pixs, 0, WIDTH, 0, y, WIDTH, 1);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    bottom = y;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        pixs = new int[HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            bp.getPixels(pixs, 0, 1, x, 0, 1, HEIGHT);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    left = x;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        for (int x = WIDTH - 1; x > 0; x--) {
            bp.getPixels(pixs, 0, 1, x, 0, 1, HEIGHT);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    right = x;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        if (blank < 0) {
            blank = 0;
        }
        left = left - blank > 0 ? left - blank : 0;
        top = top - blank > 0 ? top - blank : 0;
        right = right + blank > WIDTH - 1 ? WIDTH - 1 : right + blank;
        bottom = bottom + blank > HEIGHT - 1 ? HEIGHT - 1 : bottom + blank;
        return Bitmap.createBitmap(bp, left, top, right - left, bottom - top);
    }

}
