package com.example.admin.savefiledemo.act;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.admin.savefiledemo.Constant;
import com.example.admin.savefiledemo.R;
import com.example.admin.savefiledemo.util.ToastUtil;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 阅读pdf文件
 */

public class ActReadPdf extends AppCompatActivity {

    @BindView(R.id.pdf_view)
    PDFView pdfView;
    @BindView(R.id.tv_current_index)
    TextView tvCurrentIndex;
    @BindView(R.id.btn_previous_page)
    TextView btnPreviousPage;
    @BindView(R.id.btn_next_page)
    TextView btnNextPage;

    private int currentPageIndex;
    private int sumPageIndex;

    private File folderDir = Constant.getFolderDir(Constant.PDF_FILE_PATH);
    private String fileUrl = folderDir.getAbsolutePath() + "/故乡的年味.pdf";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_readpdf);
        ButterKnife.bind(this);

        File pdfFile = new File(fileUrl);
        if (pdfFile.exists()) {
            //pdfView.fromFile(pdfFile).pages(0).load();//默认显示第一张图片

            pdfView.fromFile(pdfFile)//设置pdf文件地址
                    .onLoad(new OnLoadCompleteListener() {
                        @Override
                        public void loadComplete(int nbPages) {
                            sumPageIndex = pdfView.getPageCount();
                            currentPageIndex = pdfView.getCurrentPage();
                        }
                    })
                    .onPageChange(new OnPageChangeListener() {
                        @Override
                        public void onPageChanged(int page, int pageCount) {
                            tvCurrentIndex.setText((page+1) + "/" + pageCount);
                            currentPageIndex = page;
                        }
                    })//设置翻页监听
                    .onRender(new OnRenderListener() {
                        @Override
                        public void onInitiallyRendered(int nbPages, float pageWidth, float pageHeight) {
                            pdfView.fitToWidth();
                        }
                    })//设置每一页适应屏幕宽，默认适应屏幕高
                    .swipeHorizontal(false)//设置不可水平滑动
                    .load();
            currentPageIndex = 0;
        }
    }

    @OnClick({R.id.btn_previous_page, R.id.btn_next_page})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_previous_page:
                currentPageIndex = currentPageIndex - 1;
                if(currentPageIndex < 0){
                    currentPageIndex = sumPageIndex -1;
                }
                pdfView.jumpTo(currentPageIndex,true);
                break;
            case R.id.btn_next_page:
                currentPageIndex = currentPageIndex + 1;
                if(currentPageIndex >= sumPageIndex){
                    currentPageIndex = 0;
                }
                pdfView.jumpTo(currentPageIndex,true);
                break;
        }
    }
}
