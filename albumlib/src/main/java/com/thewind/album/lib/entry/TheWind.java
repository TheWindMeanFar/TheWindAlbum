package com.thewind.album.lib.entry;

import android.app.Activity;
import android.support.v4.app.Fragment;

public class TheWind {
    /**
     * 拍照返回
     */
    public static final int CAMERA_REQUEST_CODE = 1001;
    /**
     * 相册返回
     */
    public static final int ALBUM_REQUEST_CODE = 1002;
    /**
     * 视频返回
     */
    public static final int VIDEO_REQUEST_CODE = 1004;
    /**
     * 裁剪完成
     */
    public static final int CROP_REQUEST_CODE = com.yalantis.ucrop.UCrop.REQUEST_CROP;
    /**
     * 相册/视频选择完成后的结果带在 intent 中的参数名
     */
    public static final String SELECT_RESULT_PATH_LIST = "pathList";

    private static TheWind theWind;
    private static TheWindAlbum theWindAlbum;

    public static TheWind get() {
        if (theWind == null)
            theWind = new TheWind();
        return theWind;
    }

    public TheWindAlbum with(Activity activity) {
        if (theWindAlbum == null)
            theWindAlbum = new TheWindAlbum(activity);
        return theWindAlbum;
    }

    public TheWindAlbum with(Fragment fragment) {
        if (theWindAlbum == null)
            theWindAlbum = new TheWindAlbum(fragment);
        return theWindAlbum;
    }

    public String getCameraFileSavePath() {
        return theWindAlbum.cameraFileSavePath;
    }

    public String getCropFileSavePath() {
        return theWindAlbum.cropFileSavePath;
    }
}
