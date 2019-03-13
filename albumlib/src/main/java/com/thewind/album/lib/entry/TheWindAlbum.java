package com.thewind.album.lib.entry;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.thewind.album.lib.activity.AlbumActivity;
import com.thewind.album.lib.activity.VideoActivity;
import com.thewind.album.lib.utils.CropUtils;

import java.io.File;
import java.util.Date;

import static com.thewind.album.lib.utils.UriUtils.getUriForFile;

public class TheWindAlbum {
    /**
     * 图片参数：裁剪的时候是否用圆形
     * 默认矩形
     */
    private boolean isCircle = false;
    /**
     * 图片参数：开启裁剪后，最大宽度
     * 默认 0，表示无限制
     */
    private int maxWidth = 0;
    /**
     * 图片参数：开启裁剪后，最大高度
     * 默认 0，表示无限制
     */
    private int maxHeight = 0;

    /**
     * 图片参数：默认保存根目录
     */
    private String savePathRoot = "";
    /**
     * 图片参数：拍照后的保存路径，需带文件名
     */
    public String cameraFileSavePath = "";
    /**
     * 裁剪后保存位置，要带文件名
     */
    public String cropFileSavePath = "";
    /**
     * 压缩后的保存路径，不要带文件名
     */
    public String compressFileSavePath = "";
    /**
     * 设置录制视频的最大时长，默认 10s
     */
    public int recordMaxTimeLength = 10;

    /**
     * 共用参数：最大选择多少张图/多少个视频
     * 默认 9
     */
    private int maxSize = 9;

    private Activity activity;
    private Fragment fragment;

    public TheWindAlbum(Activity activity) {
        this.activity = activity;
        createDefaultPath();
    }

    public TheWindAlbum(Fragment fragment) {
        this.fragment = fragment;
        createDefaultPath();
    }

    /**
     * 图片方法：裁剪时设置最大的宽度
     *
     * @param maxWidth
     * @return
     */
    public TheWindAlbum setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    /**
     * 图片方法：裁剪时设置最大的高度
     *
     * @param maxHeight
     * @return
     */
    public TheWindAlbum setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    /**
     * 共用方法：设置最多选择多少个
     *
     * @param maxSize
     * @return
     */
    public TheWindAlbum setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    /**
     * 裁剪的时候是否为圆形
     *
     * @param circle
     * @return
     */
    public TheWindAlbum setCircle(boolean circle) {
        isCircle = circle;
        return this;
    }

    /**
     * 图片方法：裁剪后的保存路径，需带文件名
     *
     * @param cropFileSavePath
     * @return
     */
    public TheWindAlbum setCropFileSavePath(String cropFileSavePath) {
        this.cropFileSavePath = cropFileSavePath;
        return this;
    }

    /**
     * 图片方法：拍照后的保存路径，需带文件名
     *
     * @param cameraFileSavePath
     * @return
     */
    public TheWindAlbum setCameraFileSavePath(String cameraFileSavePath) {
        this.cameraFileSavePath = cameraFileSavePath;
        return this;
    }

    /**
     * 图片方法：压缩后的保存路径
     * 不带文件名
     *
     * @param compressFileSavePath
     * @return
     */
    public TheWindAlbum setCompressFileSavePath(String compressFileSavePath) {
        this.compressFileSavePath = compressFileSavePath;
        File file = new File(compressFileSavePath);
        if (!file.exists()) file.mkdirs();
        return this;
    }

    /**
     * 设置视频录制的最大时长
     * @param timeLength
     * @return
     */
    public TheWindAlbum setRecordMaxTimeLengh(int timeLength){
        recordMaxTimeLength = timeLength;
        return this;
    }

    /**
     * 选择视频
     */
    public void openVideo() {
        Intent intent = new Intent();
        intent.putExtra("maxSize", maxSize);
        if (fragment != null) {
            intent.setClass(fragment.getContext(), VideoActivity.class);
            fragment.startActivityForResult(intent, TheWind.VIDEO_REQUEST_CODE);
        } else if (activity != null) {
            intent.setClass(activity, VideoActivity.class);
            activity.startActivityForResult(intent, TheWind.VIDEO_REQUEST_CODE);
        }
    }

    /**
     * 拍摄视频
     */
    public void openVideoRecord() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // 设置录制质量，0低质量，1高质量，没有中间值，0很模糊，1体积很大，30秒150M左右
//        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        // 限制文件大小
//        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 20 * 1024 * 1024L);//限制录制大小(10M=10 * 1024 * 1024L)
        // 设置录制视频最大时长
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, recordMaxTimeLength);
        activity.startActivityForResult(intent, TheWind.VIDEO_RECORD_REQUEST_CODE);
    }

    /**
     * 打开相册
     */
    public void openAlbum() {
        if (TextUtils.isEmpty(cropFileSavePath))
            cropFileSavePath = savePathRoot + "crop_" + new Date().getTime() + ".jpg";
        Intent intent = new Intent();
        intent.putExtra("cropFileSavePath", cropFileSavePath);
        intent.putExtra("compressFileSavePath", TextUtils.isEmpty(compressFileSavePath) ? savePathRoot : compressFileSavePath);
        intent.putExtra("isCircle", isCircle);
        intent.putExtra("maxSize", maxSize);
        intent.putExtra("maxWidth", maxWidth);
        intent.putExtra("maxHeight", maxHeight);
        if (fragment != null) {
            intent.setClass(fragment.getContext(), AlbumActivity.class);
            fragment.startActivityForResult(intent, TheWind.ALBUM_REQUEST_CODE);
        } else if (activity != null) {
            intent.setClass(activity, AlbumActivity.class);
            activity.startActivityForResult(intent, TheWind.ALBUM_REQUEST_CODE);
        }
    }

    /**
     * 打开相机拍照
     */
    public void openCamera() {
        if (TextUtils.isEmpty(cameraFileSavePath))
            cameraFileSavePath = savePathRoot + "camera_" + new Date().getTime() + ".jpg";
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (fragment != null) {
            intent.putExtra("output", getUriForFile(fragment.getContext(), new File(cameraFileSavePath)));
            fragment.startActivityForResult(intent, TheWind.CAMERA_REQUEST_CODE);
        } else if (activity != null) {
            intent.putExtra("output", getUriForFile(activity, new File(cameraFileSavePath)));
            activity.startActivityForResult(intent, TheWind.CAMERA_REQUEST_CODE);
        }
    }

    /**
     * 启动裁剪
     */
    public void openCrop() {
        if (TextUtils.isEmpty(cropFileSavePath))
            cropFileSavePath = savePathRoot + "crop_" + new Date().getTime() + ".jpg";
        CropUtils.crop(activity, fragment, isCircle, maxWidth, maxHeight, cameraFileSavePath, cropFileSavePath);
    }


    /**
     * 创建默认根目录
     */
    private void createDefaultPath() {
        savePathRoot = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "/TheWindAlbum/";
        File file = new File(savePathRoot);
        if (!file.exists()) file.mkdirs();
    }

    /**
     * 因为要保存用户设置的各种值，所以并不是一直 new 新对象。
     * 所以会出现如，第一次调用设置了 maxSize = 5
     * 第二次如果不设置，也会等于 5
     * 所以每次调用的时候要把各种值恢复
     */
    public TheWindAlbum initValue() {
        isCircle = false;
        maxWidth = 0;
        maxHeight = 0;
        cameraFileSavePath = "";
        cropFileSavePath = "";
        compressFileSavePath = "";
        recordMaxTimeLength = 10;
        maxSize = 9;
        return this;
    }
}
