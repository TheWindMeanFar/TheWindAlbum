package com.thewind.album.lib.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.thewind.album.lib.R;
import com.thewind.album.lib.base.BaseFragment;

import java.io.File;

/**
 * Created by robot on 2018/4/11.
 */

public class PicShowFragment extends BaseFragment {
    private ImageView imageView;

    /**
     * 要显示的图片的本地路径
     */
    private String filePath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentLayout = R.layout.wind_public_image_view;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void instance(View fragmentView) {
        super.instance(fragmentView);
        filePath = getArguments().getString("filePath");
    }

    @Override
    protected void findViewAndSetListener(View fragmentView) {
        super.findViewAndSetListener(fragmentView);
        imageView = fragmentView.findViewById(R.id.imageView);
    }

    @Override
    protected void showView() {
        super.showView();
    }

    @Override
    public void onPause() {
        Glide.clear(imageView);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(filePath))
            Glide.with(this)
                    .load(new File(filePath))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imageView);
    }

    public void refresh(String filePath){
        this.filePath = filePath;
    }
}
