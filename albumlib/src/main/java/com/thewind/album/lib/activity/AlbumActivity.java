package com.thewind.album.lib.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.KeyEvent;
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
import com.thewind.album.lib.bean.Album;
import com.thewind.album.lib.bean.Photo;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class AlbumActivity extends BaseActivity {
    public static final int RESULT_FINISH = 1003;

    private ImageView ivBack;
    private TextView tvTitle;
    private Button btnCustom;
    private RecyclerView rvPhoto;
    private RecyclerView rvAlbum;
    private LinearLayout layoutAlbum;
    private Button tvAlbumName;
    private Button btnPreview;
    private RelativeLayout layoutBottomBar;

    /**
     * 所有图片的数据源
     * fileList > listPhoto > listCheckPhoto
     * 修改任何一个记录中的对象，都会全部更新
     * 说到底只需要fileList一个就够，为什么还要其他两个数据源
     * 考虑到可能fileList有很多张图片，那操作起来可能就会慢许多
     * 所以分出了后面两个
     */
    public static ArrayList<Photo> fileList = new ArrayList<>();
    /**
     * 左下角切换相册的
     */
    private CommonAdapter<Album> adapterAlbum;
    private ArrayList<Album> listAlbum = new ArrayList<>();
    /**
     * 某个相册的内容
     */
    private CommonAdapter<Photo> adapterPhoto;
    public static ArrayList<Photo> listPhoto = new ArrayList<>();
    /**
     * 已选中的图片，在当前界面，选中和取消会add和remove
     * 在PicShowActivity中选中会add，取消不会remove
     * 因为PicShowActivity需要预览，不能remove，否则会造成麻烦
     */
    public static ArrayList<Photo> listCheckPhoto = new ArrayList<>();
    /**
     * 相册图片Item的长宽
     */
    private int itemImageWidth;
    /**
     * 最多可选择数量
     */
    private int maxSize = 9;
    /**
     * 裁剪后的最大宽度
     */
    private int maxWidth = 1080;
    /**
     * 裁剪后的最大高度
     */
    private int maxHeight = 1920;
    /**
     * 压缩后的保存路径
     */
    private String compressFileSavePath;
    /**
     * 压缩到第几张，用来判断是否全部压缩完成
     */
    private int count = 0;
    /**
     * 压缩后的图片路径
     */
    private ArrayList<String> compressPathList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.wind_activity_album);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void instance() {
        super.instance();

        compressFileSavePath = getIntent().getStringExtra("compressFileSavePath");
        itemImageWidth = (int) (getResources().getDisplayMetrics().widthPixels - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics())) / 4;
        maxSize = getIntent().getIntExtra("maxSize", 9);
        maxWidth = getIntent().getIntExtra("maxWidth", 1080);
        maxHeight = getIntent().getIntExtra("maxHeight", 1920);

        adapterPhoto = new CommonAdapter<Photo>(this, R.layout.wind_item_image, listPhoto) {
            @Override
            protected void convert(ViewHolder holder, final Photo photo, int position) {
                ImageView imageView = holder.getView(R.id.imageView);
                imageView.getLayoutParams().width = imageView.getLayoutParams().height = itemImageWidth;
                RequestOptions options = new RequestOptions().override(300, 300);
                Glide.with(AlbumActivity.this)
                        .load(new File(photo.getFilePath()))
                        .apply(options)
                        .into(imageView);

                holder.setChecked(R.id.cbCheck, photo.isSelect());
                holder.setOnClickListener(R.id.cbCheck, new PhotoClick(photo));
                holder.setOnClickListener(R.id.imageView, new PhotoClick(photo));
            }
        };

        adapterAlbum = new CommonAdapter<Album>(this, R.layout.wind_item_album, listAlbum) {
            @Override
            protected void convert(ViewHolder holder, final Album album, final int position) {
                if (album == null || album.getPhotos() == null || album.getPhotos().size() == 0)
                    return;
                RequestOptions options = new RequestOptions().override(300, 300);
                Glide.with(AlbumActivity.this)
                        .load(new File(album.getPhotos().get(0).getFilePath()))
                        .apply(options)
                        .into((ImageView) holder.getView(R.id.ivCover));
                if (position == 0) {
                    holder.setText(R.id.tvName, "所有图片");
                    holder.setTag(R.id.tvName, "所有图片");
                } else {
                    // 获取相册下的第一张图片的文件路径（含文件名）
                    String coverName = album.getPhotos().get(0).getFilePath();
                    // 这里去掉了文件名，剩余一个路径，即为相册的路径
                    coverName = coverName.substring(0, coverName.lastIndexOf("/"));
                    holder.setTag(R.id.tvName, coverName);
                    // 相册的路径，取最后一层的文件夹作为相册名来显示
                    coverName = coverName.substring(coverName.lastIndexOf("/") + 1);
                    holder.setText(R.id.tvName, coverName);
                }
                holder.setText(R.id.tvCount, album.getPhotos().size() + "张");
                holder.setVisible(R.id.ivSelect, album.isSelect());
                holder.setOnClickListener(R.id.layoutAlbum, new PhotoClick(album, position));
            }
        };
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

        tvAlbumName.setOnClickListener(onClickListener);
        btnPreview.setOnClickListener(onClickListener);
        ivBack.setOnClickListener(onClickListener);
        btnCustom.setOnClickListener(onClickListener);

        initRecyclerView();
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchCallback(rvAlbum, listAlbum));
//        itemTouchHelper.attachToRecyclerView(rvAlbum);
    }

    @Override
    protected void showView() {
        super.showView();
        tvAlbumName.setText("所有图片");
        tvAlbumName.setTag("所有图片");
        tvTitle.setText("相册");
        getPhoto();
    }

    private void initRecyclerView() {
        rvPhoto.setNestedScrollingEnabled(false);
        // 设置Item增加、移除动画
        rvPhoto.setItemAnimator(new DefaultItemAnimator());
        // 添加分割线
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.wind_divider_small));
        rvPhoto.addItemDecoration(dividerItemDecoration);
        // 设置布局方式
        rvPhoto.setLayoutManager(new GridLayoutManager(this, 4));
        rvPhoto.setAdapter(adapterPhoto);

        rvAlbum.setNestedScrollingEnabled(false);
        // 设置Item增加、移除动画
        rvAlbum.setItemAnimator(new DefaultItemAnimator());
        // 添加分割线
        rvAlbum.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        // 设置布局方式
        rvAlbum.setLayoutManager(new LinearLayoutManager(this, LinearLayout.VERTICAL, false));
        rvAlbum.setAdapter(adapterAlbum);
    }

    /**
     * 相册的详细内容，就是那一堆展示的图片
     */
    private class PhotoClick implements View.OnClickListener {
        /**
         * 一张图片
         */
        private Photo photo;
        /**
         * 选中的相册
         */
        private Album album;
        /**
         * 选中的相册对应的位置
         */
        private int position;

        public PhotoClick(Photo photo) {
            this.photo = photo;
        }

        public PhotoClick(Album album, int position) {
            this.album = album;
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.cbCheck) {
                // 图片右上角的checkbox
                if (((CheckBox) view).isChecked()) {
                    if (listCheckPhoto.size() >= maxSize) {
                        toast("最多只能选择" + maxSize + "张图片");
                        ((CheckBox) view).setChecked(false);
                        return;
                    }
                    listCheckPhoto.add(photo);
                } else listCheckPhoto.remove(photo);
                photo.setSelect(((CheckBox) view).isChecked());

                btnCustom.setVisibility(listCheckPhoto.size() > 0 ? View.VISIBLE : View.INVISIBLE);
                btnCustom.setText("完成(" + listCheckPhoto.size() + "/" + maxSize + ")");

                btnPreview.setText(listCheckPhoto.size() > 0 ? "预览(" + listCheckPhoto.size() + ")" : "预览");
                btnPreview.setVisibility(listCheckPhoto.size() > 0 ? View.VISIBLE : View.GONE);
            }
            if (view.getId() == R.id.imageView) {
                // 图片的点击
                startPicShowActivity(listPhoto.indexOf(photo), 0);
            }
            if (view.getId() == R.id.layoutAlbum) {
                // 相册Item点击事件

                // 切换相册时设置左下角显示的相册名
                if (position == 0) {
                    tvAlbumName.setText("所有图片");
                    tvAlbumName.setTag("所有图片");
                } else {
                    String coverName = album.getPhotos().get(0).getFilePath();
                    coverName = coverName.substring(0, coverName.lastIndexOf("/"));
                    tvAlbumName.setTag(coverName);
                    coverName = coverName.substring(coverName.lastIndexOf("/") + 1);
                    tvAlbumName.setText(coverName);
                }
                // 显示属于该相册的所有图片
                listPhoto.clear();
                listPhoto.addAll(album.getPhotos());
                adapterPhoto.notifyDataSetChanged();

                // 循环查找，更改select的状态，设置选中的相册，显示右边的标记，不要用listAlbum.get(position)，会错位
                for (Album a : listAlbum) a.setSelect(a == album);
                adapterAlbum.notifyDataSetChanged();
                layoutAlbum.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 获取所有图片，封装好相册
     */
    private void getPhoto() {
        // 记录相册名称，用map可以去重
        LinkedHashMap<String, Object> albumMap = new LinkedHashMap<>();
        // 查询所有相册的图片
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Images.Media._ID + " desc");
        while (cursor.moveToNext()) {
            byte[] data = cursor.getBlob(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            // 图片的路径
            String filePath = new String(data, 0, data.length - 1);
            // 将所有图片的路径添加fileList
            fileList.add(new Photo(filePath));
            // 保存相册名（相册名是一个路径，没有文件名的），如/storage/emulated/0/DCIM/Camera
            albumMap.put(filePath.substring(0, filePath.lastIndexOf("/")), null);
        }
        // 设置图片数据源并刷新
        listPhoto.addAll(fileList);
        adapterPhoto.notifyDataSetChanged();

        // 第一个相册为“所有图片”，不能直接把listPhoto给listAlbum，因为它是作为一个数据源，后续会更新数据源
        Album album = new Album();
        album.setName("所有图片");
        album.setSelect(true);
        album.setPhotos(fileList);
        listAlbum.add(album);
        for (LinkedHashMap.Entry<String, Object> map : albumMap.entrySet()) {
            album = new Album();
            album.setName(map.getKey());
            // 相册名已经得到，存在map中，现在就从所有图片中来区分，哪些照片属于哪个相册
            for (Photo photo : fileList) {
                // 如果文件路径从0到最后一个“/”跟map中存放的相册名相同（即文件的完整路径去掉了文件名），就是属于这个相册的
                if (photo.getFilePath().substring(0, photo.getFilePath().lastIndexOf("/")).equals(map.getKey())) {
                    album.getPhotos().add(photo);
                }
            }
            // 走完for循环，添加到数据源中，这就得到一个相册的内容
            listAlbum.add(album);
        }
        adapterAlbum.notifyDataSetChanged();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view.getId() == R.id.tvAlbumName) {
                if (listAlbum.size() == 1) {
                    toast("没有更多相册了");
                    return;
                }
                layoutAlbum.setVisibility(layoutAlbum.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }

            if (view.getId() == R.id.btnPreview) {
                startPicShowActivity(0, 1);
            }

            if (view.getId() == R.id.ivBack) {
                finish();
            }

            if (view.getId() == R.id.btnCustom) {
                final ArrayList<String> pathList = new ArrayList<>();
                for (Photo photo : listCheckPhoto)
                    pathList.add(photo.getFilePath());

                count = 0;
                compressPathList.clear();

                progressDialog.show();
                Luban.with(AlbumActivity.this)
                        .load(pathList) // 传入要压缩的图片（列表）
                        .ignoreBy(300) // 忽略不压缩图片的大小，超过就压缩
                        .setTargetDir(compressFileSavePath) // 设置压缩后文件存储位置
                        .setCompressListener(new OnCompressListener() {
                            @Override
                            public void onStart() {
                                System.out.println("压缩开始");
                                ++count;
                            }

                            @Override
                            public void onSuccess(File file) {
                                System.out.println("压缩成功" + file.getAbsolutePath());
                                compressPathList.add(file.getAbsolutePath());
                                setFinish(pathList.size());
                            }

                            @Override
                            public void onError(Throwable e) {
                                System.out.println("压缩失败");
                                setFinish(pathList.size());
                                e.printStackTrace();
                            }
                        }).launch();
            }
        }
    };

    private void setFinish(int listSize) {
        if (count == listSize) {
            progressDialog.dismiss();
            System.out.println("压缩结束");
            Intent intent = getIntent();
            intent.putExtra("pathList", compressPathList);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void startPicShowActivity(int index, int type) {
        Intent intent = new Intent(AlbumActivity.this, PicShowActivity.class);
        intent.putExtra("maxSize", maxSize);
        intent.putExtra("maxWidth", maxWidth);
        intent.putExtra("maxHeight", maxHeight);
        intent.putExtra("index", index);
        intent.putExtra("type", type);
        intent.putExtra("cropFileSavePath", getIntent().getStringExtra("cropFileSavePath"));
        intent.putExtra("compressFileSavePath", getIntent().getStringExtra("compressFileSavePath"));
        intent.putExtra("isCircle", getIntent().getBooleanExtra("isCircle", false));
        startActivityForResult(intent, REQUEST_SHOW);
    }

    /**
     * 跳转到PicShowActivity之后返回
     */
    public final int REQUEST_SHOW = 100;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // 跳转到PicShowActivity之后返回
            case REQUEST_SHOW:
                // 在PicShowActivity按了手机的返回键
                if (resultCode == RESULT_OK) {
                    getCheck();
                    btnCustom.setVisibility(listCheckPhoto.size() > 0 ? View.VISIBLE : View.INVISIBLE);
                    btnCustom.setText("完成(" + listCheckPhoto.size() + "/" + maxSize + ")");

                    btnPreview.setText(listCheckPhoto.size() > 0 ? "预览(" + listCheckPhoto.size() + ")" : "预览");
                    btnPreview.setVisibility(listCheckPhoto.size() > 0 ? View.VISIBLE : View.GONE);
                    adapterPhoto.notifyDataSetChanged();
                }
                // 在PicShowActivity按了右上角的完成键
                if (resultCode == RESULT_FINISH) {
                    setResult(RESULT_OK, data);
                    finish();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 在PicShowActivity中，图片选中会add，取消选中不会remove
     * 所以要去掉那些没被选中的
     */
    public static void getCheck() {
        ArrayList<Photo> removeList = new ArrayList<>();
        for (Photo photo : listCheckPhoto) {
            photo.setShow(false);
            if (!photo.isSelect())
                removeList.add(photo);
        }
        for (Photo photo : removeList)
            listCheckPhoto.remove(photo);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (layoutAlbum.getVisibility() == View.VISIBLE) {
                layoutAlbum.setVisibility(View.GONE);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        listPhoto.clear();
        listCheckPhoto.clear();
        fileList.clear();
    }
}
