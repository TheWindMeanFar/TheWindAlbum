package com.thewind.album.lib.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.Toast;

public class BaseActivity extends AppCompatActivity {

    public ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance();
        findViewAndSetListener();
        showView();
    }

    /**
     * 初始化对象、数据
     */
    protected void instance() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("请稍后...");
        progressDialog.setCancelable(false);
    }

    /**
     * 获取控件、设置监听器
     */
    protected void findViewAndSetListener() {
    }

    /**
     * 展示
     */
    protected void showView() {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
