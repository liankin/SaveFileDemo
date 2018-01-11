package com.example.admin.savefiledemo.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.admin.savefiledemo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by admin on 2018/1/2.
 */

public class PaintStyleDialog extends Dialog {

    @BindView(R.id.btn_cancel)
    TextView btnCancel;
    @BindView(R.id.btn_sure)
    TextView btnSure;
    @BindView(R.id.ed_paint_size)
    EditText edPaintSize;
    @BindView(R.id.tv_red)
    TextView tvRed;
    @BindView(R.id.tv_black)
    TextView tvBlack;
    @BindView(R.id.tv_green)
    TextView tvGreen;


    public PaintStyleDialog(@NonNull Context context) {
        super(context, R.style.alert_dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_paintstyle);
        setCanceledOnTouchOutside(false);
        ButterKnife.bind(this);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setBtnSure(View.OnClickListener listener) {
        btnSure.setOnClickListener(listener);
    }

    public EditText getEdPaintSize() {
        return edPaintSize;
    }


    public TextView getTvRed() {
        return tvRed;
    }

    public TextView getTvBlack() {
        return tvBlack;
    }

    public TextView getTvGreen() {
        return tvGreen;
    }

}
