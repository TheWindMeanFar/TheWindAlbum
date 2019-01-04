package com.thewind.album.lib.bean;

import java.io.Serializable;

public class Photo implements Serializable {
    private String filePath;
    private boolean isSelect = false;
    private boolean isShow = false;

    public Photo(){}
    public Photo(String filePath){
        this.filePath = filePath;
        this.isSelect = false;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }
}
