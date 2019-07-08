package com.thewind.album.lib.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.thewind.album.lib.R;
import com.thewind.album.lib.base.BaseActivity;
import com.thewind.album.lib.bean.VideoInfo;
import com.thewind.album.lib.utils.UriUtils;
import com.thewind.album.lib.utils.VideoUtils;
import com.yalantis.ucrop.util.FileUtils;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoActivity extends BaseActivity {

    private ImageView ivBack;
    private TextView tvTitle;
    private Button btnCustom;
    private RecyclerView rvPhoto;
    private RecyclerView rvAlbum;
    private LinearLayout layoutAlbum;
    private Button tvAlbumName;
    private Button btnPreview;
    private RelativeLayout layoutBottomBar;

    private List<VideoInfo> videoInfoList = new ArrayList<>();
    private CommonAdapter<VideoInfo> videoInfoCommonAdapter;

    /**
     * item 宽度
     */
    private int itemImageWidth;
    /**
     * 最多可选择数量
     */
    private int maxSize = 9;
//    /**
//     * 是否压缩
//     */
//    private boolean isCompress;
//    /**
//     * 是否裁剪
//     */
//    private boolean isCrop;
//    /**
//     * 裁剪的开始时间
//     */
//    private long startTime;
//    /**
//     * 裁剪的结束时间
//     */
//    private long endTime;
    /**
     * 已选中的视频，在当前界面，选中和取消会 add 和 remove
     */
    public ArrayList<VideoInfo> listCheckVideo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.wind_activity_video);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void instance() {
        super.instance();
        videoInfoList.addAll(VideoUtils.getLoadVideo(this));
        itemImageWidth = (int) (getResources().getDisplayMetrics().widthPixels - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics())) / 4;

        maxSize = getIntent().getIntExtra("maxSize", 9);
//        isCompress = getIntent().getBooleanExtra("isCompress", false);
//        isCrop = getIntent().getBooleanExtra("isCrop", false);
//        startTime = getIntent().getLongExtra("startTime", 0);
//        endTime = getIntent().getLongExtra("endTime", 0);
    }

    @Override
    protected void findViewAndSetListener() {
        super.findViewAndSetListener();
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        btnCustom = findViewById(R.id.btnCustom);
        rvPhoto = findViewById(R.id.rvPhoto);
        rvAlbum = findViewById(R.id.rvAlbum);
        layoutAlbum = findViewById(R.id.layoutAlbum);
        tvAlbumName = findViewById(R.id.tvAlbumName);
        btnPreview = findViewById(R.id.btnPreview);
        layoutBottomBar = findViewById(R.id.layoutBottomBar);

        ivBack.setOnClickListener(onClickListener);
        btnCustom.setOnClickListener(onClickListener);
    }

    @Override
    protected void showView() {
        super.showView();
        tvTitle.setText("视频");
        videoInfoCommonAdapter = new CommonAdapter<VideoInfo>(this, R.layout.wind_item_image, videoInfoList) {
            @Override
            protected void convert(ViewHolder holder, final VideoInfo videoInfo, int position) {
                ImageView imageView = holder.getView(R.id.imageView);
                imageView.getLayoutParams().width = imageView.getLayoutParams().height = itemImageWidth;
                RequestOptions options = new RequestOptions().override(300, 300);
                Glide.with(VideoActivity.this).load(videoInfo.getPath()).apply(options).into((ImageView) holder.getView(R.id.imageView));
                holder.setChecked(R.id.cbCheck, videoInfo.isSelect());
                holder.setVisible(R.id.textView, true);
                holder.setText(R.id.textView, millisecondToHMS(videoInfo.getDuration()));
                holder.setOnClickListener(R.id.cbCheck, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (((CheckBox) view).isChecked()) {
                            if (listCheckVideo.size() >= maxSize) {
                                toast("最多只能选择" + maxSize + "个视频");
                                ((CheckBox) view).setChecked(false);
                                return;
                            }
                            listCheckVideo.add(videoInfo);
                        } else listCheckVideo.remove(videoInfo);
                        videoInfo.setSelect(((CheckBox) view).isChecked());

                        btnCustom.setVisibility(listCheckVideo.size() > 0 ? View.VISIBLE : View.INVISIBLE);
                        btnCustom.setText("完成(" + listCheckVideo.size() + "/" + maxSize + ")");
                    }
                });
                holder.setOnClickListener(R.id.imageView, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        File file = new File(videoInfo.getPath());
                        Uri uri = UriUtils.getUriForFile(VideoActivity.this, file);
                        intent.setDataAndType(uri, "video/*");
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);
                    }
                });
            }
        };
        rvPhoto.setAdapter(videoInfoCommonAdapter);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.ivBack) {
                finish();
            }

            if (view.getId() == R.id.btnCustom) {
                final ArrayList<String> pathList = new ArrayList<>();
                for (VideoInfo videoInfo : listCheckVideo)
                    pathList.add(videoInfo.getPath());

                Intent intent = getIntent();
                intent.putExtra("pathList", pathList);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    };

    public static String millisecondToHMS(long millisUntilFinished) {
        long day = millisUntilFinished / 1000 / (24 * 60 * 60);
        long hour = millisUntilFinished / 1000 % (24 * 60 * 60) / (60 * 60);
        hour = day * 24 + hour;
        long min = millisUntilFinished / 1000 % (24 * 60 * 60) % (60 * 60) / 60;
        long sec = millisUntilFinished / 1000 % (24 * 60 * 60) % (60 * 60) % 60;
        return (hour >= 10 ? hour : "0" + hour) + ":"
                + (min >= 10 ? min : "0" + min) + ":"
                + (sec >= 10 ? sec : "0" + sec);
    }
}
