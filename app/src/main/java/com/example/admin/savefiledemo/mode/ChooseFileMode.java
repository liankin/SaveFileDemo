package com.example.admin.savefiledemo.mode;

import java.io.File;
import java.io.Serializable;

/**
 * Created by admin on 2017/11/17.
 */

public class ChooseFileMode implements Serializable{

    private File file;
    private int operation;//0为查看大图，1为删除此文件，2为重民命， 3为复制一份

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

}
