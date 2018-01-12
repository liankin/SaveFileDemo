package com.example.admin.savefiledemo.act;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.example.admin.savefiledemo.Constant;
import com.example.admin.savefiledemo.R;
import com.example.admin.savefiledemo.util.FileUtil;
import com.example.admin.savefiledemo.util.ToastUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * txt文本文件：
 * 使用FileUtil，对文本文件的操作进行了封装
 * 相对路径保存、读取；
 */

public class ActTextFileOperation extends AppCompatActivity {

    @BindView(R.id.tv_file_name)
    TextView tvFileName;
    @BindView(R.id.ed_input_text)
    EditText edInputText;
    @BindView(R.id.check_box_append)
    CheckBox checkBoxAppend;
    @BindView(R.id.btn_write_cancel)
    Button btnWriteCancel;
    @BindView(R.id.btn_write_ok)
    Button btnWriteOk;
    @BindView(R.id.ed_file_name)
    EditText edFileName;
    @BindView(R.id.btn_read_cancel)
    Button btnReadCancel;
    @BindView(R.id.btn_read_ok)
    Button btnReadOk;
    @BindView(R.id.tv_read_text)
    TextView tvReadText;

    private static final String FILE_NAME = Constant.TXT_FILE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_save_read_file);
        ButterKnife.bind(this);

        tvFileName.setText("默认文件名：" + FILE_NAME);
        edFileName.setText(FILE_NAME);

//        String readData = readData(FILE_NAME);
//
//        if (!TextUtils.isEmpty(readData)) {
//            edInputText.setText(readData);
//            edInputText.setSelection(readData.length());
//        }
//        tvReadText.setText("读取出的数据为：" + readData);
    }

    /**
     * 保存String型数据到文件里:
     * 使用BufferedWriter对象。
     * Context.MODE_PRIVATE：为默认操作模式，代表该文件是私有数据，
     * 只能被应用本身访问，在该模式下，写入的内容会覆盖原文件的内容。
     * Context.MODE_APPEND：模式会检查文件是否存在，存在就往文件追加内容，否则就创建新文件。
     * @param inputText
     */
    public void writeData(String inputText, String fileName, int saveMode) {
        FileOutputStream fileOutputStream = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileOutputStream = openFileOutput(fileName, saveMode);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            bufferedWriter.write(inputText);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从指定文件中读出String字符串
     *
     * @param fileName
     * @return
     */
    public String readData(String fileName) {
        FileInputStream fileInputStream = null;
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            fileInputStream = openFileInput(fileName);
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        saveData();
    }

    /**
     * 保存输入的文本
     */
    public void saveData() {
        String inputText = edInputText.getText().toString().trim();
        boolean isAppend = checkBoxAppend.isChecked();

        if (FileUtil.writeTextFile(ActTextFileOperation.this,inputText,FILE_NAME,isAppend)) {
            ToastUtil.showMessage("修改成功");
        } else {
            ToastUtil.showMessage("修改失败");
        }

//        if (FileUtil.writeTextFile(FILES_DIR.getAbsolutePath(), FILE_NAME, inputText, isAppend)) {
//            ToastUtil.showMessage("修改成功");
//        } else {
//            ToastUtil.showMessage("修改失败");
//        }

//        if (FileUtil.copyFile(FILES_DIR.getAbsolutePath(),SD_DIR.getPath() + "/SAVEFILEDEMO/TEXTFILE22")) {
//            ToastUtil.showMessage("文件夹拷贝成功");
//        } else {
//            ToastUtil.showMessage("文件夹拷贝失败");
//        }

//        if (FileUtil.copySdcardFile(FILES_DIR.getAbsolutePath()+"/"+ FILE_NAME,FILES_DIR.getAbsolutePath()+"/999.txt")) {
//            ToastUtil.showMessage("文件拷贝成功");
//        } else {
//            ToastUtil.showMessage("文件拷贝失败");
//        }

//        if (checkBoxCoverOld.isChecked()) {
//            saveData(inputText, FILE_NAME, Context.MODE_PRIVATE);
//        } else {
//            saveData(inputText, FILE_NAME, Context.MODE_APPEND);
//        }
    }

    @OnClick({R.id.btn_write_cancel, R.id.btn_write_ok, R.id.btn_read_cancel, R.id.btn_read_ok})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_write_cancel:
                edInputText.setText("");
                break;
            case R.id.btn_write_ok:
                saveData();
                break;
            case R.id.btn_read_cancel:
                edFileName.setText("");
                break;
            case R.id.btn_read_ok:
                tvReadText.setText("读取出的数据为：" + FileUtil.readTextFile(ActTextFileOperation.this, FILE_NAME));
//                tvReadText.setText("读取出的数据为：" + FileUtil.readTextFile(FILES_DIR.getAbsolutePath(), FILE_NAME));

//                if (!TextUtils.isEmpty(readData(FILE_NAME))) {
//                    tvReadText.setText("读取出的数据为：" + readData(FILE_NAME));
//                } else {
//                    tvReadText.setText("暂无数据");
//                }
                break;
        }
    }
}

