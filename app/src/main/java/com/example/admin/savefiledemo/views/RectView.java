package com.example.admin.savefiledemo.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.MutableBoolean;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class RectView extends View implements Runnable {

    // 声明Paint对象
    private Paint mPaint = null;

    private float mLeft;
    private float mTop;
    private float mRight;
    private float mBottom;

    public RectView(Context context,float left, float top, float right, float bottom) {
        super(context);
        // 构建对象
        mPaint = new Paint();

        this.mLeft = left;
        this.mRight = right;
        this.mTop = top;
        this.mBottom = bottom;
        // 开启线程
        new Thread(this).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 设置Paint为无锯齿
        mPaint.setAntiAlias(true);

        // 设置Paint的颜色
        mPaint.setColor(Color.RED);
        mPaint.setColor(Color.BLUE);
        mPaint.setColor(Color.YELLOW);
        mPaint.setColor(Color.GREEN);
        // 同样是设置颜色
        mPaint.setColor(Color.rgb(255, 0, 0));

        // 提取颜色
        Color.red(0xcccccc);
        Color.green(0xcccccc);

        // 设置paint的颜色和Alpha值(a,r,g,b)
        mPaint.setAlpha(220);

        // 这里可以设置为另外一个paint对象
        // mPaint.set(new Paint());
        // 设置字体的尺寸
        mPaint.setTextSize(14);

        // 设置paint的风格为“空心”
        // 当然也可以设置为"实心"(Paint.Style.FILL)
        mPaint.setStyle(Paint.Style.STROKE);

        // 设置“空心”的外框的宽度
        mPaint.setStrokeWidth(5);

        // 绘制一空心个矩形
        canvas.drawRect(mLeft,mTop,mRight, mBottom, mPaint);
        // 设置风格为实心
        //mPaint.setStyle(Paint.Style.FILL);
        //mPaint.setColor(Color.GREEN);
        // 绘制绿色实心矩形
        //canvas.drawRect(0, 20, 40, 20 + 40, mPaint);
    }

    // 触笔事件
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    // 按键按下事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }

    // 按键弹起事件
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return true;
    }

    public boolean onKeyMultiple(int KeyCode, int repeatCount, KeyEvent event) {
        return true;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
            // 更新界面
            postInvalidate();
        }
    }
}
