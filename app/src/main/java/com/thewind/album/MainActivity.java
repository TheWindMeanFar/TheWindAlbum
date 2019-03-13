package com.thewind.album;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.provider.MediaStore;
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
    private Button btnVideoRecord;
    private TextView tvPath;

//    private String savePathRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCamera = findViewById(R.id.btn_camera);
        btnAlbum = findViewById(R.id.btn_album);
        btnVideo = findViewById(R.id.btn_video);
        btnVideoRecord = findViewById(R.id.btn_video_record);
        tvPath = findViewById(R.id.tv_path);

//        savePathRoot = Environment.getExternalStorageDirectory().getAbsolutePath()
//                + File.separator + "/aaaaa/";
//        File file = new File(savePathRoot);
//        if (!file.exists()) file.mkdirs();

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TheWind.get().with(MainActivity.this)
                        .initValue()
                        .setCircle(true)  // 可选设置，裁剪时是否为圆形，默认为 false 矩形，如果拍照后不需要裁剪，可以不用设置
//                        .setCameraFileSavePath(savePathRoot + "camera_" + new Date().getTime() + ".jpg") // 可选设置，拍照后的保存路径，不设置的话会有默认路径
//                        .setCropFileSavePath(savePathRoot + "crop_" + new Date().getTime() + ".jpg") // 可选设置，裁剪后的保存路径，不设置的话会有默认路径，，如果拍照后不需要裁剪，可以不用设置
                        .setMaxHeight(1000) // 可选设置，设置截图后的图片最大高度，如果拍照后不需要裁剪，可以不用设置
                        .setMaxWidth(1000) // 可选设置，设置截图后的图片最大宽度，如果拍照后不需要裁剪，可以不用设置
                        .openCamera(); // 打开相机拍照
            }
        });

        btnAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TheWind.get().with(MainActivity.this)
                        .initValue()
                        .setCircle(true) // 可选设置，裁剪时是否为圆形，默认为 false 矩形，如果拍照后不需要裁剪，可以不用设置
                        .setMaxSize(2) // 可选设置，设置最多能选几张图，默认 9 张
                        .setMaxHeight(1000) // 可选设置，设置截图后的图片最大高度，如果拍照后不需要裁剪，可以不用设置
                        .setMaxWidth(1000) // 可选设置，设置截图后的图片最大宽度，如果拍照后不需要裁剪，可以不用设置
//                        .setCompressFileSavePath(savePathRoot) // 可选设置，大于 300K 的图片会进行压缩，不设置的话会有默认路径
//                        .setCropFileSavePath(savePathRoot + "crop_" + new Date().getTime() + ".jpg") // 可选设置，裁剪后的保存路径，不设置的话会有默认路径，，如果拍照后不需要裁剪，可以不用设置
                        .openAlbum(); // 打开相册
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

        btnVideoRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TheWind.get().with(MainActivity.this)
                        .openVideoRecord();
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
                        // tvPath.setText("拍照成功：" + TheWind.get().getCameraFileSavePath());
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

            // 拍摄视频完成
            case TheWind.VIDEO_RECORD_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null && cursor.moveToNext()) {
                        String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
                        tvPath.setText("视频拍摄成功：" + filePath);
                    }
                    cursor.close();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TheWind.close();
    }
}
