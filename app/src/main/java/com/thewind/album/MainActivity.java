package com.thewind.album;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.thewind.album.lib.entry.TheWind;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends Activity {

    private Button btnCamera;
    private Button btnAlbum;
    private Button btnVideo;
    private TextView tvPath;

    private String savePathRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCamera = findViewById(R.id.btn_camera);
        btnAlbum = findViewById(R.id.btn_album);
        btnVideo = findViewById(R.id.btn_video);
        tvPath = findViewById(R.id.tv_path);

        savePathRoot = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "/aaaaa/";
        File file = new File(savePathRoot);
        if (!file.exists()) file.mkdirs();

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TheWind.get().with(MainActivity.this)
                        .initValue()
                        .setCircle(true)
                        .setCameraFileSavePath(savePathRoot + "camera_" + new Date().getTime() + ".jpg")
                        .setCropFileSavePath(savePathRoot + "crop_" + new Date().getTime() + ".jpg")
                        .setMaxHeight(1000)
                        .setMaxWidth(1000)
                        .openCamera();
            }
        });

        btnAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TheWind.get().with(MainActivity.this)
                        .initValue()
                        .setCircle(true)
                        .setMaxSize(2)
                        .setMaxHeight(1000)
                        .setMaxWidth(1000)
                        .setCompressFileSavePath(savePathRoot)
                        .setCropFileSavePath(savePathRoot + "crop_" + new Date().getTime() + ".jpg")
                        .openAlbum();
            }
        });

        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TheWind.get().with(MainActivity.this)
                        .initValue()
                        .setMaxSize(5)
                        .openVideo();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // 视频选择完成
            case TheWind.VIDEO_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> pathList = (ArrayList<String>) data.getSerializableExtra(TheWind.SELECT_RESULT_PATH_LIST);
                    tvPath.setText("视频选择完成：" + pathList.toString());
                }
                break;
            // 相册选择完成
            case TheWind.ALBUM_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> pathList = (ArrayList<String>) data.getSerializableExtra(TheWind.SELECT_RESULT_PATH_LIST);
                    tvPath.setText("相册选择完成：" + pathList.toString());
                }
                break;
            // 拍照完成
            case TheWind.CAMERA_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (new File(TheWind.get().getCameraFileSavePath()).exists()) {
                        // 如果不裁剪，这就是拍照后的路径
//                        tvPath.setText("拍照成功：" + TheWind.get().getCameraFileSavePath());
                        // 如果要裁剪
                        TheWind.get().with(MainActivity.this).openCrop();
                    } else tvPath.setText("拍照失败");
                }
                break;
            // 裁剪完成
            case TheWind.CROP_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (new File(TheWind.get().getCropFileSavePath()).exists()) {
                        tvPath.setText("裁剪成功：" + TheWind.get().getCropFileSavePath());
                    } else
                        tvPath.setText("裁剪失败");
                }
                break;
        }
    }
}
