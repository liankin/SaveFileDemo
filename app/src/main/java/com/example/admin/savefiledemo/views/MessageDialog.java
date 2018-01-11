package com.example.admin.savefiledemo.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;


import com.example.admin.savefiledemo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**消息提示对话框
 * Created by Administrator on 2017/4/21 0021.
 */
public class MessageDialog extends Dialog {

    @BindView(R.id.tv_content)
    TextView tvContent;
    @BindView(R.id.btn_cancel)
    TextView btnCancel;
    @BindView(R.id.btn_sure)
    TextView btnSure;

    public MessageDialog(Context context) {
        super(context, R.style.alert_dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_message);
        setCanceledOnTouchOutside(false);
        ButterKnife.bind(this);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setContent(String content){
        tvContent.setText(content);
    }
    public void setBtnSure(String str, View.OnClickListener listener){
        btnSure.setText(str);
        btnSure.setOnClickListener(listener);
    }
    public void setBtnCancel(String str, View.OnClickListener listener){
        btnCancel.setText(str);
        btnCancel.setOnClickListener(listener);
    }

}
