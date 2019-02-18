package com.thewind.album.lib.utils;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.thewind.album.lib.R;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;

import static com.thewind.album.lib.utils.UriUtils.getUriForFile;

public class CropUtils {
    /**
     * 裁剪
     *
     * @param activity
     * @param width
     * @param height
     * @param sourceFilePath
     * @param saveFilePath
     */
    public static void crop(Activity activity, Fragment fragment, boolean isCircle, int width, int height, String sourceFilePath, String saveFilePath) {
        Context context = null;
        if (fragment != null) context = fragment.getContext();
        else if (activity != null) context = activity;
//        System.out.println("要裁剪的图片路径：" + sourceFilePath);
        Uri uri_crop = getUriForFile(context, new File(sourceFilePath));
        //裁剪后保存到文件中
        Uri destinationUri = Uri.fromFile(new File(saveFilePath));
        UCrop uCrop = UCrop.of(uri_crop, destinationUri);
        UCrop.Options options = new UCrop.Options();
        options.withMaxResultSize(width, height);
        //设置裁剪图片可操作的手势
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        //设置隐藏底部容器，默认显示
//        options.setHideBottomControls(true);
        //设置toolbar颜色
        options.setToolbarColor(ActivityCompat.getColor(context, R.color.colorTopBarWind));
        //设置状态栏颜色
        options.setStatusBarColor(ActivityCompat.getColor(context, R.color.colorTopBarWind));
        //是否能调整裁剪框
        options.setFreeStyleCropEnabled(true);
        if (isCircle) {
            options.setCircleDimmedLayer(true);//设置是否为圆形裁剪框
            options.setShowCropFrame(false); //设置是否显示裁剪边框(true为方形边框)
        }
        uCrop.withOptions(options);
        if (fragment != null) uCrop.start(context, fragment);
        else if (activity != null) uCrop.start(activity);
    }
}
