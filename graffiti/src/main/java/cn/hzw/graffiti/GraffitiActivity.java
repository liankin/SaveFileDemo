package cn.hzw.graffiti;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.forward.androids.utils.ImageUtils;
import cn.forward.androids.utils.LogUtil;
import cn.forward.androids.utils.StatusBarUtil;
import cn.forward.androids.utils.ThreadUtil;
import cn.hzw.graffiti.imagepicker.ImageSelectorView;

/**
 * 涂鸦界面，根据GraffitiView的接口，提供页面交互
 * （这边代码和ui比较粗糙，主要目的是告诉大家GraffitiView的接口具体能实现什么功能，实际需求中的ui和交互需另提别论）
 * Created by huangziwei(154330138@qq.com) on 2016/9/3.
 */
public class GraffitiActivity extends Activity {

    public static final String TAG = "Graffiti";

    public static final int RESULT_ERROR = -111; // 出现错误
    private ArrayList<String> pathList;
    private View mNextPage;
    private View mLostPage;
    private String mTitleName;

    /**
     * 启动涂鸦界面
     *
     * @param activity
     * @param params      涂鸦参数
     * @param requestCode startActivityForResult的请求码
     * @see GraffitiParams
     */
    public static void startActivityForResult(Activity activity, GraffitiParams params, ArrayList<String> paths, int requestCode) {
        Intent intent = new Intent(activity, GraffitiActivity.class);
        intent.putExtra(GraffitiActivity.KEY_PARAMS, params);
        if (paths!=null) {
            intent.putStringArrayListExtra(GraffitiActivity.KEY_PATH_LIST, paths);
        }
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 启动涂鸦界面
     *
     * @param activity
     * @param imagePath   　图片路径
     * @param savePath    　保存路径
     * @param isDir       　保存路径是否为目录
     * @param requestCode 　startActivityForResult的请求码
     */
    @Deprecated
    public static void startActivityForResult(Activity activity, String imagePath, String savePath, boolean isDir, int requestCode) {
        GraffitiParams params = new GraffitiParams();
        params.mImagePath = imagePath;
        params.mSavePath = savePath;
        params.mSavePathIsDir = isDir;
        startActivityForResult(activity, params,null, requestCode);
    }

    /**
     * {@link GraffitiActivity#startActivityForResult(Activity, String, String, boolean, int)}
     */
    @Deprecated
    public static void startActivityForResult(Activity activity, String imagePath, int requestCode) {
        GraffitiParams params = new GraffitiParams();
        params.mImagePath = imagePath;
        startActivityForResult(activity, params,null, requestCode);
    }

    public static final String KEY_PARAMS = "key_graffiti_params";
    public static final String KEY_IMAGE_PATH = "key_image_path";
    public static final String KEY_PATH_LIST = "key_path_list";
    private String mImagePath;
    private Bitmap mBitmap;

    private FrameLayout mFrameLayout;
    private GraffitiView mGraffitiView;

    private View.OnClickListener mOnClickListener;

    private SeekBar mPaintSizeBar;
    private TextView mPaintSizeView;

    private View mBtnColor;
    private Runnable mUpdateScale;

    private int mTouchMode;
    private boolean mIsMovingPic = false;

    // 手势操作相关
    private float mOldScale, mOldDist, mNewDist, mToucheCentreXOnGraffiti,
            mToucheCentreYOnGraffiti, mTouchCentreX, mTouchCentreY;// 双指距离

    private float mTouchLastX, mTouchLastY;

    private boolean mIsScaling = false;
    private float mScale = 1;
    private final float mMaxScale = 30f; // 最大缩放倍数
    private final float mMinScale = 0.25f; // 最小缩放倍数
    private final int TIME_SPAN = 40;
    private View mBtnMovePic, mBtnHidePanel, mSettingsPanel;
    private View mShapeModeContainer;
    private View mSelectedTextEditContainer;
    private View mEditContainer;

    private int mTouchSlop;

    private AlphaAnimation mViewShowAnimation, mViewHideAnimation; // view隐藏和显示时用到的渐变动画

    // 当前屏幕中心点对应在GraffitiView中的点的坐标
    float mCenterXOnGraffiti;
    float mCenterYOnGraffiti;

    private GraffitiParams mGraffitiParams;

    // 触摸屏幕超过一定时间才判断为需要隐藏设置面板
    private Runnable mHideDelayRunnable;
    //触摸屏幕超过一定时间才判断为需要隐藏设置面板
    private Runnable mShowDelayRunnable;

//    private float mSelectableItemSize;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_PARAMS, mGraffitiParams);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        mGraffitiParams = savedInstanceState.getParcelable(KEY_PARAMS);
    }
    private int index=0;//当前页码
    private List<Bitmap> bitmaps = new ArrayList<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setStatusBarTranslucent(this, true, false);
        if (mGraffitiParams == null) {
            mGraffitiParams = getIntent().getExtras().getParcelable(KEY_PARAMS);
        }
        if (mGraffitiParams == null) {
            LogUtil.e("TAG", "mGraffitiParams is null!");
            this.finish();
            return;
        }
        mTitleName = mGraffitiParams.mTitleName;
        mImagePath = mGraffitiParams.mImagePath;
        if (mImagePath == null) {
            LogUtil.e("TAG", "mImagePath is null!");
            this.finish();
            return;
        }
        LogUtil.d("TAG", mImagePath);
        if (mGraffitiParams.mIsFullScreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }/*else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
      // mBitmap = ImageUtils.createBitmapFromPath(mImagePath, this);
        Intent intent = getIntent();
        if (intent!=null&&intent.hasExtra(GraffitiActivity.KEY_PATH_LIST)){
            pathList = intent.getStringArrayListExtra(GraffitiActivity.KEY_PATH_LIST);
        }
        setContentView(R.layout.layout_graffiti);
        mFrameLayout = (FrameLayout) findViewById(R.id.graffiti_container);
        // /storage/emulated/0/DCIM/Graffiti/1479369280029.jpg
        initGraffitiView(null);
        mOnClickListener = new GraffitiOnClickListener();
        mTouchSlop = ViewConfiguration.get(getApplicationContext()).getScaledTouchSlop();
        initView();
    }
   private void initGraffitiView(GraffitiColor color){
       if (bitmaps.size()>index){
           mBitmap = bitmaps.get(index);
       }else {
           mBitmap = BitmapFactory.decodeFile(pathList.get(index));
       }
       if (mBitmap == null) {
           this.finish();
           return;
       }
       mGraffitiView = new GraffitiView(this, mBitmap, mGraffitiParams.mEraserPath, mGraffitiParams.mEraserImageIsResizeable,
               new GraffitiListener() {
                   @Override
                   public void onSaved(Bitmap bitmap, Bitmap bitmapEraser) { // 保存图片
                       if (bitmaps.size()==index){
                           bitmaps.add(bitmap);
                       }else {
                           bitmaps.set(index,bitmap);
                       }
                       if (bitmapEraser != null) {
                           bitmapEraser.recycle(); // 回收图片，不再涂鸦，避免内存溢出
                       }
                       String savePath = mGraffitiParams.mSavePath;
                      // boolean isDir = mGraffitiParams.mSavePathIsDir;
                       try {
                           File dir = new File(savePath);
                           if (!dir.exists()) {
                               dir.mkdirs();
                           }
                           ArrayList<String> savePaths = new ArrayList<>();
                           for (int i = 0; i < bitmaps.size(); i++) {
                               File file = new File(dir, i + "");
                               saveImageFile(bitmaps.get(i),file);
                               savePaths.add(file.getAbsolutePath());
                           }
                           Intent intent = new Intent();
                           intent.putStringArrayListExtra(KEY_IMAGE_PATH, savePaths);
                           setResult(Activity.RESULT_OK, intent);
                           finish();
                       } catch (Exception e) {
                           e.printStackTrace();
                           onError(GraffitiView.ERROR_SAVE, e.getMessage());
                           Toast.makeText(GraffitiActivity.this,"保存出错",Toast.LENGTH_SHORT).show();
                       }
                   }

                   @Override
                   public void onError(int i, String msg) {
                       setResult(RESULT_ERROR);
                       finish();
                   }

                   @Override
                   public void onReady() {
                       mGraffitiView.setPaintSize(mGraffitiParams.mPaintSize > 0 ? mGraffitiParams.mPaintSize
                               : mGraffitiView.getPaintSize());
                       mPaintSizeBar.setProgress((int) (mGraffitiView.getPaintSize() + 0.5f));
                       mPaintSizeBar.setMax(10);
                       // mPaintSizeBar.setMax((int) (Math.min(mGraffitiView.getBitmapWidthOnView(), mGraffitiView.getBitmapHeightOnView()) / 3 * DrawUtil.GRAFFITI_PIXEL_UNIT));
                       mPaintSizeView.setText("" + mPaintSizeBar.getProgress());
                       findViewById(R.id.btn_pen_hand).performClick();
                       findViewById(R.id.btn_hand_write).performClick();
                   }

                   @Override
                   public void onSelectedItem(GraffitiSelectableItem selectableItem, boolean selected) {
                       if (selected) {
                           mSelectedTextEditContainer.setVisibility(View.VISIBLE);
                           if (mGraffitiView.getSelectedItemColor().getType() == GraffitiColor.Type.BITMAP) {
                               mBtnColor.setBackgroundDrawable(new BitmapDrawable(mGraffitiView.getSelectedItemColor().getBitmap()));
                           } else {
                               mBtnColor.setBackgroundColor(mGraffitiView.getSelectedItemColor().getColor());
                           }
                           mPaintSizeBar.setProgress((int) (mGraffitiView.getSelectedItemSize() + 0.5f));
                       } else {
                           mSelectedTextEditContainer.setVisibility(View.GONE);
                           mEditContainer.setVisibility(View.VISIBLE);
                           if (mGraffitiView.getColor().getType() == GraffitiColor.Type.BITMAP) {
                               mBtnColor.setBackgroundDrawable(new BitmapDrawable(mGraffitiView.getColor().getBitmap()));
                           } else {
                               mBtnColor.setBackgroundColor(mGraffitiView.getColor().getColor());
                           }
                           mPaintSizeBar.setProgress((int) (mGraffitiView.getPaintSize() + 0.5f));
                       }
                   }

                   @Override
                   public void onCreateSelectableItem(GraffitiView.Pen pen, float x, float y) {
                       if (pen == GraffitiView.Pen.TEXT) {
                           createGraffitiText(null, x, y);
                       } else if (pen == GraffitiView.Pen.BITMAP) {
                           createGraffitiBitmap(null, x, y);
                       }
                   }
               });
       mGraffitiView.setIsDrawableOutside(mGraffitiParams.mIsDrawableOutside);
       mFrameLayout.addView(mGraffitiView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        // 添加涂鸦的触摸监听器，移动图片位置
        mGraffitiView.setOnTouchListener(new GraffitiOnTouchListener());
       if (color!=null) {
           mGraffitiView.setColor(color.getColor());
       }
    }

    private void pageChange(boolean isNext){
        Bitmap mChangeBitmap = mGraffitiView.pagechange();
        if (isNext) {
            if (bitmaps.size()==index){
                bitmaps.add(mChangeBitmap);
            }else {
                bitmaps.set(index,mChangeBitmap);
            }
            index += 1;
        }else {
            if (bitmaps.size()==index){
                bitmaps.add(mChangeBitmap);
            }else {
                bitmaps.set(index,mChangeBitmap);
            }
            index -= 1;
        }
        mLostPage.setVisibility(View.VISIBLE);
        mNextPage.setVisibility(View.VISIBLE);
        if (index==0){
            mLostPage.setVisibility(View.GONE);
        }
        if (index==pathList.size()-1){
            mNextPage.setVisibility(View.GONE);
        }
        mFrameLayout.removeView(mGraffitiView);
        mGraffitiParams.mPaintSize = mPaintSizeBar.getProgress();
        GraffitiColor color = mGraffitiView.getColor();
        initGraffitiView(color);
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
    private void createGraffitiText(final GraffitiText graffitiText, final float x, final float y) {
        Activity activity = this;

        boolean fullScreen = (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
        Dialog dialog = null;
        if (fullScreen) {
            dialog = new Dialog(activity,
                    android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        } else {
            dialog = new Dialog(activity,
                    android.R.style.Theme_Translucent_NoTitleBar);
        }
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();

        ViewGroup container = (ViewGroup) View.inflate(getApplicationContext(), R.layout.graffiti_create_text, null);
        final Dialog finalDialog = dialog;
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalDialog.dismiss();
            }
        });
        dialog.setContentView(container);

        final EditText textView = (EditText) container.findViewById(R.id.graffiti_selectable_edit);
        final View cancelBtn = container.findViewById(R.id.graffiti_text_cancel_btn);
        final TextView enterBtn = (TextView) container.findViewById(R.id.graffiti_text_enter_btn);

        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = (textView.getText() + "").trim();
                if (TextUtils.isEmpty(text)) {
                    enterBtn.setEnabled(false);
                    enterBtn.setTextColor(0xffb3b3b3);
                } else {
                    enterBtn.setEnabled(true);
                    enterBtn.setTextColor(0xff232323);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        textView.setText(graffitiText == null ? "" : graffitiText.getText());

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelBtn.setSelected(true);
                finalDialog.dismiss();
            }
        });

        enterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalDialog.dismiss();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (cancelBtn.isSelected()) {
                    mSettingsPanel.removeCallbacks(mHideDelayRunnable);
                    return;
                }
                String text = (textView.getText() + "").trim();
                if (TextUtils.isEmpty(text)) {
                    return;
                }
                if (graffitiText == null) {
                    mGraffitiView.addSelectableItem(new GraffitiText(mGraffitiView.getPen(), text, mGraffitiView.getPaintSize(), mGraffitiView.getColor().copy(),
                            0, mGraffitiView.getGraffitiRotateDegree(), x, y, mGraffitiView.getOriginalPivotX(), mGraffitiView.getOriginalPivotY()));
                } else {
                    graffitiText.setText(text);
                }
                mGraffitiView.invalidate();
            }
        });

        if (graffitiText == null) {
            mSettingsPanel.removeCallbacks(mHideDelayRunnable);
        }

    }

    private void createGraffitiBitmap(final GraffitiBitmap graffitiBitmap, final float x, final float y) {
        Activity activity = this;

        boolean fullScreen = (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
        Dialog dialog = null;
        if (fullScreen) {
            dialog = new Dialog(activity,
                    android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        } else {
            dialog = new Dialog(activity,
                    android.R.style.Theme_Translucent_NoTitleBar);
        }
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
        ViewGroup container = (ViewGroup) View.inflate(getApplicationContext(), R.layout.graffiti_create_bitmap, null);
        final Dialog finalDialog = dialog;
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalDialog.dismiss();
            }
        });
        dialog.setContentView(container);

        ViewGroup selectorContainer = (ViewGroup) finalDialog.findViewById(R.id.graffiti_image_selector_container);
        ImageSelectorView selectorView = new ImageSelectorView(this, false, 1, null, new ImageSelectorView.ImageSelectorListener() {
            @Override
            public void onCancel() {
                finalDialog.dismiss();
            }

            @Override
            public void onEnter(List<String> pathList) {
                finalDialog.dismiss();
                Bitmap bitmap = ImageUtils.createBitmapFromPath(pathList.get(0), mGraffitiView.getWidth() / 4, mGraffitiView.getHeight() / 4);

                if (graffitiBitmap == null) {
                    mGraffitiView.addSelectableItem(new GraffitiBitmap(mGraffitiView.getPen(), bitmap, mGraffitiView.getPaintSize(), mGraffitiView.getColor().copy(),
                            0, mGraffitiView.getGraffitiRotateDegree(), x, y, mGraffitiView.getOriginalPivotX(), mGraffitiView.getOriginalPivotY()));
                } else {
                    graffitiBitmap.setBitmap(bitmap);
                }
                mGraffitiView.invalidate();
            }
        });
        selectorContainer.addView(selectorView);
    }

    private void initView() {
        findViewById(R.id.btn_pen_hand).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_pen_copy).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_pen_eraser).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_pen_text).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_pen_bitmap).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_hand_write).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_arrow).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_line).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_holl_circle).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_fill_circle).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_holl_rect).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_fill_rect).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_clear).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_undo).setOnClickListener(mOnClickListener);
        findViewById(R.id.graffiti_selectable_edit).setOnClickListener(mOnClickListener);
        findViewById(R.id.graffiti_selectable_remove).setOnClickListener(mOnClickListener);
        findViewById(R.id.graffiti_selectable_top).setOnClickListener(mOnClickListener);
        mShapeModeContainer = findViewById(R.id.bar_shape_mode);
        mSelectedTextEditContainer = findViewById(R.id.graffiti_selectable_edit_container);
        mEditContainer = findViewById(R.id.graffiti_edit_container);
        mNextPage = findViewById(R.id.btn_next_page);
        mLostPage = findViewById(R.id.btn_lost_page);
        mLostPage.setVisibility(View.VISIBLE);
        mNextPage.setVisibility(View.VISIBLE);
        if (index==0){
            mLostPage.setVisibility(View.GONE);
        }
        if (index==pathList.size()-1){
            mNextPage.setVisibility(View.GONE);
        }
        mNextPage.setOnClickListener(mOnClickListener);
        mLostPage.setOnClickListener(mOnClickListener);
        mBtnHidePanel = findViewById(R.id.graffiti_btn_hide_panel);
        mBtnHidePanel.setOnClickListener(mOnClickListener);
        findViewById(R.id.graffiti_btn_finish).setOnClickListener(mOnClickListener);
        findViewById(R.id.graffiti_btn_back).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_centre_pic).setOnClickListener(mOnClickListener);
        mBtnMovePic = findViewById(R.id.btn_move_pic);
        mBtnMovePic.setOnClickListener(mOnClickListener);
        mBtnColor = findViewById(R.id.btn_set_color);
        mBtnColor.setOnClickListener(mOnClickListener);
        mSettingsPanel = findViewById(R.id.graffiti_panel);
        if (mGraffitiView.getGraffitiColor().getType() == GraffitiColor.Type.COLOR) {
            mBtnColor.setBackgroundColor(mGraffitiView.getGraffitiColor().getColor());
        } else if (mGraffitiView.getGraffitiColor().getType() == GraffitiColor.Type.BITMAP) {
            mBtnColor.setBackgroundDrawable(new BitmapDrawable(mGraffitiView.getGraffitiColor().getBitmap()));
        }

        mPaintSizeBar = (SeekBar) findViewById(R.id.paint_size);
        mPaintSizeView = (TextView) findViewById(R.id.paint_size_text);

        mPaintSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    mPaintSizeBar.setProgress(1);
                    return;
                }
                mPaintSizeView.setText("" + progress);
                if (mGraffitiView.isSelectedItem()) {
                    mGraffitiView.setSelectedItemSize(progress);
                } else {
                    mGraffitiView.setPaintSize(progress);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        ScaleOnTouchListener onTouchListener = new ScaleOnTouchListener();
        findViewById(R.id.btn_amplifier).setOnTouchListener(onTouchListener);
        findViewById(R.id.btn_reduce).setOnTouchListener(onTouchListener);

        /*// 添加涂鸦的触摸监听器，移动图片位置
        mGraffitiView.setOnTouchListener(new GraffitiOnTouchListener());*/

        TextView tv_title = (TextView) findViewById(R.id.graffiti_txt_title);
        if (!TextUtils.isEmpty(mTitleName)){
            tv_title.setText(mTitleName);
        }
        tv_title.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) { // 长按标题栏显示原图
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        mGraffitiView.setJustDrawOriginal(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mGraffitiView.setJustDrawOriginal(false);
                        break;
                }
                return true;
            }
        });

        mViewShowAnimation = new AlphaAnimation(0, 1);
        mViewShowAnimation.setDuration(500);
        mViewHideAnimation = new AlphaAnimation(1, 0);
        mViewHideAnimation.setDuration(500);
        mHideDelayRunnable = new Runnable() {
            public void run() {
                hideView(mSettingsPanel);
            }

        };
        mShowDelayRunnable = new Runnable() {
            public void run() {
                showView(mSettingsPanel);
            }
        };

        findViewById(R.id.graffiti_btn_rotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGraffitiView.rotate(mGraffitiView.getGraffitiRotateDegree() + 90);
            }
        });
    }
    private class GraffitiOnTouchListener implements  View.OnTouchListener{

        boolean mIsBusy = false; // 避免双指滑动，手指抬起时处理单指事件。

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // 隐藏设置面板
            if (!mBtnHidePanel.isSelected()  // 设置面板没有被隐藏
                    && mGraffitiParams.mChangePanelVisibilityDelay > 0) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        mSettingsPanel.removeCallbacks(mHideDelayRunnable);
                        mSettingsPanel.removeCallbacks(mShowDelayRunnable);
                        mSettingsPanel.postDelayed(mHideDelayRunnable, mGraffitiParams.mChangePanelVisibilityDelay); //触摸屏幕超过一定时间才判断为需要隐藏设置面板
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        mSettingsPanel.removeCallbacks(mHideDelayRunnable);
                        mSettingsPanel.removeCallbacks(mShowDelayRunnable);
                        mSettingsPanel.postDelayed(mShowDelayRunnable, mGraffitiParams.mChangePanelVisibilityDelay); //离开屏幕超过一定时间才判断为需要显示设置面板
                        break;
                }
            } else if (mBtnHidePanel.isSelected() && mGraffitiView.getAmplifierScale() > 0) {
                mGraffitiView.setAmplifierScale(-1);
            }

            if (!mIsMovingPic) {
                return false;  // 交给下一层的涂鸦处理
            }
            mScale = mGraffitiView.getScale();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mTouchMode = 1;
                    mTouchLastX = event.getX();
                    mTouchLastY = event.getY();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mTouchMode = 0;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (mTouchMode < 2) { // 单点滑动
                        if (mIsBusy) { // 从多点触摸变为单点触摸，忽略该次事件，避免从双指缩放变为单指移动时图片瞬间移动
                            mIsBusy = false;
                            mTouchLastX = event.getX();
                            mTouchLastY = event.getY();
                            return true;
                        }
                        float tranX = event.getX() - mTouchLastX;
                        float tranY = event.getY() - mTouchLastY;
                        mGraffitiView.setTrans(mGraffitiView.getTransX() + tranX, mGraffitiView.getTransY() + tranY);
                        mTouchLastX = event.getX();
                        mTouchLastY = event.getY();
                    } else { // 多点
                        mNewDist = spacing(event);// 两点滑动时的距离
                        if (Math.abs(mNewDist - mOldDist) >= mTouchSlop) {
                            float scale = mNewDist / mOldDist;
                            mScale = mOldScale * scale;

                            if (mScale > mMaxScale) {
                                mScale = mMaxScale;
                            }
                            if (mScale < mMinScale) { // 最小倍数
                                mScale = mMinScale;
                            }
                            // 围绕坐标(0,0)缩放图片
                            mGraffitiView.setScale(mScale);
                            // 缩放后，偏移图片，以产生围绕某个点缩放的效果
                            float transX = mGraffitiView.toTransX(mTouchCentreX, mToucheCentreXOnGraffiti);
                            float transY = mGraffitiView.toTransY(mTouchCentreY, mToucheCentreYOnGraffiti);
                            mGraffitiView.setTrans(transX, transY);
                        }
                    }
                    return true;
                case MotionEvent.ACTION_POINTER_UP:
                    mTouchMode -= 1;
                    return true;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mTouchMode += 1;
                    mOldScale = mGraffitiView.getScale();
                    mOldDist = spacing(event);// 两点按下时的距离
                    mTouchCentreX = (event.getX(0) + event.getX(1)) / 2;// 不用减trans
                    mTouchCentreY = (event.getY(0) + event.getY(1)) / 2;
                    mToucheCentreXOnGraffiti = mGraffitiView.toX(mTouchCentreX);
                    mToucheCentreYOnGraffiti = mGraffitiView.toY(mTouchCentreY);
                    mIsBusy = true; // 标志位多点触摸
                    return true;
            }
            return true;
        }
    }
    /**
     * 计算两指间的距离
     *
     * @param event
     * @return
     */

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private class GraffitiOnClickListener implements View.OnClickListener {

        private View mLastPenView, mLastShapeView;
        private boolean mDone = false;

        @Override
        public void onClick(View v) {
            mDone = false;
            if (v.getId() == R.id.btn_pen_hand) {
                mPaintSizeBar.setProgress((int) (mGraffitiView.getPaintSize() + 0.5f));
                mShapeModeContainer.setVisibility(View.GONE);
                mGraffitiView.setPen(GraffitiView.Pen.HAND);
                mIsMovingPic =false;
                mDone = true;
            } else if (v.getId() == R.id.btn_pen_copy) {
                mPaintSizeBar.setProgress((int) (mGraffitiView.getPaintSize() + 0.5f));
                mShapeModeContainer.setVisibility(View.GONE);
                mGraffitiView.setPen(GraffitiView.Pen.COPY);
                mIsMovingPic =false;
                mDone = true;
            } else if (v.getId() == R.id.btn_pen_eraser) {
                mPaintSizeBar.setProgress((int) (mGraffitiView.getPaintSize() + 0.5f));
                mShapeModeContainer.setVisibility(View.GONE);
                mGraffitiView.setPen(GraffitiView.Pen.ERASER);
                mIsMovingPic =false;
                mDone = true;
            } else if (v.getId() == R.id.btn_pen_text) {
                mShapeModeContainer.setVisibility(View.GONE);
                mGraffitiView.setPen(GraffitiView.Pen.TEXT);
                mIsMovingPic =false;
                mDone = true;
            } else if (v.getId() == R.id.btn_pen_bitmap) {
                mShapeModeContainer.setVisibility(View.GONE);
                mGraffitiView.setPen(GraffitiView.Pen.BITMAP);
                mIsMovingPic =false;
                mDone = true;
            }else if (v.getId() == R.id.btn_next_page){//下一页
                pageChange(true);
            }else if (v.getId() == R.id.btn_lost_page){//上一页
                pageChange(false);
            }else if (v.getId() == R.id.btn_move_pic) {
               // v.setSelected(!v.isSelected());
                mIsMovingPic = !v.isSelected();
                mDone = true;
            }
            if (mDone) {
                if (mLastPenView != null) {
                    mLastPenView.setSelected(false);
                }
                v.setSelected(true);
                mLastPenView = v;
                return;
            }

            if (v.getId() == R.id.btn_clear) {
                if (!(GraffitiParams.getDialogInterceptor() != null
                        && GraffitiParams.getDialogInterceptor().onShow(GraffitiActivity.this, mGraffitiView, GraffitiParams.DialogType.CLEAR_ALL))) {
                    DialogController.showEnterCancelDialog(GraffitiActivity.this,
                            getString(R.string.graffiti_clear_screen), getString(R.string.graffiti_cant_undo_after_clearing),
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mGraffitiView.clear();
                                }
                            }, null
                    );
                }
                mDone = true;
            } else if (v.getId() == R.id.btn_undo) {
                mGraffitiView.undo();
                mDone = true;
            } else if (v.getId() == R.id.btn_set_color) {
                if (!(GraffitiParams.getDialogInterceptor() != null
                        && GraffitiParams.getDialogInterceptor().onShow(GraffitiActivity.this, mGraffitiView, GraffitiParams.DialogType.COLOR_PICKER))) {
                    new ColorPickerDialog(GraffitiActivity.this, mGraffitiView.getGraffitiColor().getColor(), "画笔颜色",
                            new ColorPickerDialog.OnColorChangedListener() {
                                public void colorChanged(int color) {
                                    mBtnColor.setBackgroundColor(color);
                                    if (mGraffitiView.isSelectedItem()) {
                                        mGraffitiView.setSelectedItemColor(color);
                                    } else {
                                        mGraffitiView.setColor(color);
                                    }
                                }

                                @Override
                                public void colorChanged(Drawable color) {
                                    mBtnColor.setBackgroundDrawable(color);
                                    if (mGraffitiView.isSelectedItem()) {
                                        mGraffitiView.setSelectedItemColor(ImageUtils.getBitmapFromDrawable(color));
                                    } else {
                                        mGraffitiView.setColor(ImageUtils.getBitmapFromDrawable(color));
                                    }
                                }
                            }).show();
                }
                mDone = true;
            }
            if (mDone) {
                return;
            }

            if (v.getId() == R.id.graffiti_btn_hide_panel) {
                mSettingsPanel.removeCallbacks(mHideDelayRunnable);
                mSettingsPanel.removeCallbacks(mShowDelayRunnable);
                v.setSelected(!v.isSelected());
                if (!mBtnHidePanel.isSelected()) {
                    showView(mSettingsPanel);
                } else {
                    hideView(mSettingsPanel);
                }
                mDone = true;
            } else if (v.getId() == R.id.graffiti_btn_finish) {
                mGraffitiView.save();
                mDone = true;
            } else if (v.getId() == R.id.graffiti_btn_back) {
                if (!mGraffitiView.isModified()) {
                    finish();
                    return;
                }
                if (!(GraffitiParams.getDialogInterceptor() != null
                        && GraffitiParams.getDialogInterceptor().onShow(GraffitiActivity.this, mGraffitiView, GraffitiParams.DialogType.SAVE))) {
                    DialogController.showEnterCancelDialog(GraffitiActivity.this, getString(R.string.graffiti_saving_picture), null, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mGraffitiView.save();
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                }
                mDone = true;
            } else if (v.getId() == R.id.btn_centre_pic) {
                mGraffitiView.centrePic();
                mDone = true;
            }
            if (mDone) {
                return;
            }


            if (v.getId() == R.id.graffiti_selectable_edit) {
                if (mGraffitiView.getSelectedItem() instanceof GraffitiText) {
                    createGraffitiText((GraffitiText) mGraffitiView.getSelectedItem(), -1, -1);
                } else if (mGraffitiView.getSelectedItem() instanceof GraffitiBitmap) {
                    createGraffitiBitmap((GraffitiBitmap) mGraffitiView.getSelectedItem(), -1, -1);
                }
                mDone = true;
            } else if (v.getId() == R.id.graffiti_selectable_remove) {
                mGraffitiView.removeSelectedItem();
                mDone = true;
            } else if (v.getId() == R.id.graffiti_selectable_top) {
                mGraffitiView.topSelectedItem();
                mDone = true;
            }
            if (mDone) {
                return;
            }

            if (v.getId() == R.id.btn_hand_write) {
                mGraffitiView.setShape(GraffitiView.Shape.HAND_WRITE);
            } else if (v.getId() == R.id.btn_arrow) {
                mGraffitiView.setShape(GraffitiView.Shape.ARROW);
            } else if (v.getId() == R.id.btn_line) {
                mGraffitiView.setShape(GraffitiView.Shape.LINE);
            } else if (v.getId() == R.id.btn_holl_circle) {
                mGraffitiView.setShape(GraffitiView.Shape.HOLLOW_CIRCLE);
            } else if (v.getId() == R.id.btn_fill_circle) {
                mGraffitiView.setShape(GraffitiView.Shape.FILL_CIRCLE);
            } else if (v.getId() == R.id.btn_holl_rect) {
                mGraffitiView.setShape(GraffitiView.Shape.HOLLOW_RECT);
            } else if (v.getId() == R.id.btn_fill_rect) {
                mGraffitiView.setShape(GraffitiView.Shape.FILL_RECT);
            }

            if (mLastShapeView != null) {
                mLastShapeView.setSelected(false);
            }
            v.setSelected(true);
            mLastShapeView = v;
        }
    }

    @Override
    public void onBackPressed() {

        if (mBtnMovePic.isSelected()) {
            mBtnMovePic.performClick();
            return;
        } else {
            findViewById(R.id.graffiti_btn_back).performClick();
        }

    }

    /**
     * 放大缩小
     */
    private class ScaleOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    scalePic(v);
                    v.setSelected(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mIsScaling = false;
                    v.setSelected(false);
                    break;
            }
            return true;
        }
    }

    /**
     * 缩放
     *
     * @param v
     */
    public void scalePic(View v) {
        if (mIsScaling)
            return;
        mIsScaling = true;
        mScale = mGraffitiView.getScale();

        // 确定当前屏幕中心点对应在GraffitiView中的点的坐标，之后将围绕这个点缩放
        mCenterXOnGraffiti = mGraffitiView.toX(mGraffitiView.getWidth() / 2);
        mCenterYOnGraffiti = mGraffitiView.toY(mGraffitiView.getHeight() / 2);

        if (v.getId() == R.id.btn_amplifier) { // 放大
            ThreadUtil.getInstance().runOnAsyncThread(new Runnable() {
                public void run() {
                    do {
                        mScale += 0.05f;
                        if (mScale > mMaxScale) {
                            mScale = mMaxScale;
                            mIsScaling = false;
                        }
                        updateScale();
                        try {
                            Thread.sleep(TIME_SPAN);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (mIsScaling);

                }
            });
        } else if (v.getId() == R.id.btn_reduce) { // 缩小
            ThreadUtil.getInstance().runOnAsyncThread(new Runnable() {
                public void run() {
                    do {
                        mScale -= 0.05f;
                        if (mScale < mMinScale) {
                            mScale = mMinScale;
                            mIsScaling = false;
                        }
                        updateScale();
                        try {
                            Thread.sleep(TIME_SPAN);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (mIsScaling);
                }
            });
        }
    }

    private void updateScale() {
        if (mUpdateScale == null) {

            mUpdateScale = new Runnable() {
                public void run() {
                    // 围绕坐标(0,0)缩放图片
                    mGraffitiView.setScale(mScale);
                    // 缩放后，偏移图片，以产生围绕某个点缩放的效果
                    float transX = mGraffitiView.toTransX(mGraffitiView.getWidth() / 2, mCenterXOnGraffiti);
                    float transY = mGraffitiView.toTransY(mGraffitiView.getHeight() / 2, mCenterYOnGraffiti);
                    mGraffitiView.setTrans(transX, transY);
                }
            };
        }
        ThreadUtil.getInstance().runOnMainThread(mUpdateScale);
    }

    private void showView(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            return;
        }

        view.clearAnimation();
        view.startAnimation(mViewShowAnimation);
        view.setVisibility(View.VISIBLE);
        if (view == mSettingsPanel || mBtnHidePanel.isSelected()) {
            mGraffitiView.setAmplifierScale(-1);
        }
    }

    private void hideView(View view) {
        if (view.getVisibility() != View.VISIBLE) {
            if (view == mSettingsPanel && mGraffitiView.getAmplifierScale() > 0) {
                mGraffitiView.setAmplifierScale(-1);
            }
            return;
        }
        view.clearAnimation();
        view.startAnimation(mViewHideAnimation);
        view.setVisibility(View.GONE);
        if (view == mSettingsPanel && !mBtnHidePanel.isSelected() && !mBtnMovePic.isSelected()) {
            // 当设置面板隐藏时才显示放大器
            mGraffitiView.setAmplifierScale(mGraffitiParams.mAmplifierScale);
        } else if ((view == mSettingsPanel && mGraffitiView.getAmplifierScale() > 0)) {
            mGraffitiView.setAmplifierScale(-1);
        }
    }

}
