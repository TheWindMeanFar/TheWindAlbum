package com.thewind.album.lib.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.thewind.album.lib.R;
import com.thewind.album.lib.base.BaseActivity;
import com.thewind.album.lib.bean.Photo;
import com.thewind.album.lib.fragment.PicShowFragment;
import com.thewind.album.lib.utils.CropUtils;
import com.yalantis.ucrop.UCrop;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

import static com.thewind.album.lib.activity.AlbumActivity.RESULT_FINISH;

public class PicShowActivity extends BaseActivity {
    private ViewPager viewPager;
    private Button btnEdit;
    private CheckBox cbCheck;
    private ImageView ivBack;
    private TextView tvTitle;
    private Button btnCustom;
    private RecyclerView rvPreview;

    /**
     * 要显示的图片在数据源中的位置
     */
    private int currentIndex;
    /**
     * 0：数据源为全部 | 1：数据源为预览
     */
    private int type;
    /**
     * 截图的保存路径，带文件名
     */
    private String cropFileSavePath;

    private CommonAdapter<Photo> previewAdapter;
    /**
     * 最多可选择数量
     */
    private int maxSize = 9;
    /**
     * 裁剪后的最大宽度
     */
    private int maxWidth = 0;
    /**
     * 裁剪后的最大高度
     */
    private int maxHeight = 0;
    /**
     * 压缩后的保存路径
     */
    private String compressFileSavePath;
    /**
     * 是否为圆形裁剪
     */
    private boolean isCircle = false;
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
        setContentView(R.layout.wind_activity_pic_show);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void instance() {
        super.instance();

        compressFileSavePath = getIntent().getStringExtra("compressFileSavePath");
        maxSize = getIntent().getIntExtra("maxSize", 9);
        maxWidth = getIntent().getIntExtra("maxWidth", 0);
        maxHeight = getIntent().getIntExtra("maxHeight", 0);
        currentIndex = getIntent().getIntExtra("index", 0);
        type = getIntent().getIntExtra("type", 0);
        cropFileSavePath = getIntent().getStringExtra("cropFileSavePath");
        isCircle = getIntent().getBooleanExtra("isCircle", false);

        previewAdapter = new CommonAdapter<Photo>(this, R.layout.wind_item_image_preview, AlbumActivity.listCheckPhoto) {
            @Override
            protected void convert(ViewHolder holder, Photo photo, final int position) {
                ImageView imageView = holder.getView(R.id.imageView);
                // 如果图片取消选择则显示多一层半透明白色作为区分
                holder.setVisible(R.id.ivHalf, !photo.isSelect());
                // 图片的边框，区分当前显示与非当前显示
                holder.setBackgroundColor(R.id.imageView, ContextCompat.getColor(PicShowActivity.this, photo.isShow() ? R.color.colorBorderWind : R.color.colorTransparentWind));
                Glide.with(PicShowActivity.this).load(new File(photo.getFilePath())).override(150, 150).into(imageView);

                // 点击预览图的时候跳转到对应的位置，设为当前显示
                holder.setOnClickListener(R.id.layoutItem, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 从预览图中获取点击的图片的对象，获取其在数据源中的位置，将viewPager跳转到该位置
                        int index = (type == 0 ? AlbumActivity.listPhoto : AlbumActivity.listCheckPhoto).indexOf(AlbumActivity.listCheckPhoto.get(position));
                        if (index >= 0) viewPager.setCurrentItem(index);
                        else toast("该图片不在此相册中");
                    }
                });
            }
        };
    }

    @Override
    protected void findViewAndSetListener() {
        super.findViewAndSetListener();

        viewPager = findViewById(R.id.viewPager);
        btnEdit = findViewById(R.id.btnEdit);
        cbCheck = findViewById(R.id.cbCheck);
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        btnCustom = findViewById(R.id.btnCustom);
        rvPreview = findViewById(R.id.rvPreview);

        ivBack.setOnClickListener(clickListener);
        btnCustom.setOnClickListener(clickListener);
        btnEdit.setOnClickListener(clickListener);

        initRecyclerView();

        cbCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cbCheck.isChecked()) {
                    // 选中图片要先判断数量是否超过设定的数量，超过的CheckBox重新设为非选中状态
                    if (getCheckCount() >= maxSize) {
                        toast("最多只能选择" + maxSize + "张图片");
                        cbCheck.setChecked(false);
                        return;
                    }
                    // 数据源是相册全部内容
                    if (type == 0) {
                        // 从数据源中获取到当前显示的图片对象，设为选中，返回上个界面的时候才会正确显示
                        AlbumActivity.listPhoto.get(viewPager.getCurrentItem()).setSelect(true);
                        /*
                         * 此种情况是用户取消之后重新选中，为了不更改listCheckPhoto中的顺序，也为了不重复添加
                         * 先判断要添加的对象是否已经存在，没有的话才是添加到listCheckPhoto中
                         */
                        if (!AlbumActivity.listCheckPhoto.contains(AlbumActivity.listPhoto.get(viewPager.getCurrentItem())))
                            AlbumActivity.listCheckPhoto.add(AlbumActivity.listPhoto.get(viewPager.getCurrentItem()));
                    }
                    // 数据源是选中（预览）部分
                    if (type == 1) {
                        // 从数据源中获取当前显示的图片对象，获取其在listPhoto中的位置，将其设置为选中，返回上个界面的时候才会正确显示
                        AlbumActivity.listPhoto.get(AlbumActivity.listPhoto.indexOf(AlbumActivity.listCheckPhoto.get(viewPager.getCurrentItem()))).setSelect(true);
                    }
                    previewAdapter.notifyDataSetChanged();
                } else {
                    if (type == 0) {
                        // 这里为什么不会出现下面的情况，因为这里无法取消选中
                        AlbumActivity.listPhoto.get(viewPager.getCurrentItem()).setSelect(false);
                    }
                    if (type == 1) {
                        /*
                        有一种情况，先在“所有图片”中选中一张图片，切换到“相册A”，选中的图片不属于相册A，此时按“预览”，
                        取消选中，如果还和之前一样操作listPhoto的话，就会出问题，因为此时的listPhoto中没有这张图，
                        所有要操作所有图片的fileList
                         */
                        AlbumActivity.fileList.get(
                                AlbumActivity.fileList.indexOf(
                                        AlbumActivity.listCheckPhoto.get(
                                                viewPager.getCurrentItem()))).setSelect(false);
                    }
                    previewAdapter.notifyDataSetChanged();
                }
                btnCustom.setVisibility(getCheckCount() > 0 ? View.VISIBLE : View.INVISIBLE);
                btnCustom.setText("完成(" + getCheckCount() + "/" + maxSize + ")");
                selectPreview();
            }
        });
    }

    @Override
    protected void showView() {
        super.showView();
        btnCustom.setVisibility(getCheckCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        btnCustom.setText("完成(" + getCheckCount() + "/" + maxSize + ")");

        setupViewPager(viewPager);
        selectPreview();
    }

    private void initRecyclerView() {
        rvPreview.setNestedScrollingEnabled(false);
        // 设置Item增加、移除动画
        rvPreview.setItemAnimator(new DefaultItemAnimator());
        // 添加分割线
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.wind_divider));
        rvPreview.addItemDecoration(dividerItemDecoration);
        // 设置布局方式
        rvPreview.setLayoutManager(new LinearLayoutManager(this, LinearLayout.HORIZONTAL, false));
        rvPreview.setAdapter(previewAdapter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ivBack.performClick();
        }
        return super.onKeyDown(keyCode, event);
    }

    private ViewPagerAdapter adapter;

    private void setupViewPager(final ViewPager viewPager) {
        /*
        要很注意很注意Fragment的嵌套关系，此处需要用getChildFragmentManager，不能用getFragmentManager
        否则会出现返回后，再进来时，tab对应的fragment不会重新加载
        */
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        for (Photo photo : type == 0 ? AlbumActivity.listPhoto : AlbumActivity.listCheckPhoto) {
            Bundle bundle = new Bundle();
            bundle.putString("title", (type == 0 ? AlbumActivity.listPhoto : AlbumActivity.listCheckPhoto).indexOf(photo) + "/" + (type == 0 ? AlbumActivity.listPhoto : AlbumActivity.listCheckPhoto).size());
            bundle.putString("filePath", photo.getFilePath());

            Fragment fragment = new PicShowFragment();
            fragment.setArguments(bundle);
            adapter.addFrag(fragment, photo.getFilePath());
        }

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentIndex);
        viewPager.setOffscreenPageLimit(2);

        // 设置标题
        setTitle((currentIndex + 1) + " / " + adapter.getCount());
        cbCheck.setChecked((type == 0 ? AlbumActivity.listPhoto : AlbumActivity.listCheckPhoto).get(currentIndex).isSelect());
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setTitle((position + 1) + " / " + adapter.getCount());
                cbCheck.setChecked((type == 0 ? AlbumActivity.listPhoto : AlbumActivity.listCheckPhoto).get(position).isSelect());
                selectPreview();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 下方有一行图为预览图，设置选中的图片
     */
    private void selectPreview() {
        if (AlbumActivity.listCheckPhoto.size() <= 0) {
            rvPreview.setVisibility(View.GONE);
            return;
        }
        rvPreview.setVisibility(View.VISIBLE);
        // 从数据源中获取当前显示的图片的对象在listCheckPhoto中的位置
        int index = AlbumActivity.listCheckPhoto.indexOf((type == 0 ? AlbumActivity.listPhoto : AlbumActivity.listCheckPhoto).get(viewPager.getCurrentItem()));
        // 通过迭代listCheckPhoto，将index位置的图片设置为当前显示的，其他为非显示
        for (int i = 0; i < AlbumActivity.listCheckPhoto.size(); i++) {
            AlbumActivity.listCheckPhoto.get(i).setShow(i == index);
        }
        previewAdapter.notifyDataSetChanged();
        rvPreview.scrollToPosition(index);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.ivBack){
                setResult(RESULT_OK);
                finish();
                return;
            }

            if (view.getId() == R.id.btnEdit) {
                // 这里说明为什么 cropFileSavePath 已存在的话，要重命名文件名
                // 因为有 cropFileSavePath 是用户传进来的，所以第一次裁剪的时候没问题
                // 第二次裁剪的时候，数据源跟要保存的文件名一样，导致裁剪出问题
                // 所以数据源的名字跟要保存的文件名不能一样
                if (new File(cropFileSavePath).exists() && cropFileSavePath.lastIndexOf(".") > 0){
                    cropFileSavePath = cropFileSavePath.substring(0, cropFileSavePath.lastIndexOf(".")) + "_1"
                            + cropFileSavePath.substring(cropFileSavePath.lastIndexOf("."));
                }

                CropUtils.crop(PicShowActivity.this, null, isCircle, maxWidth, maxHeight,
                        (type == 0 ? AlbumActivity.listPhoto : AlbumActivity.listCheckPhoto).get(viewPager.getCurrentItem()).getFilePath(),
                        cropFileSavePath);
                return;
            }

            if (view.getId() == R.id.btnCustom) {
                AlbumActivity.getCheck();
                final ArrayList<String> pathList = new ArrayList<>();
                for (Photo photo : AlbumActivity.listCheckPhoto)
                    pathList.add(photo.getFilePath());

                progressDialog.show();
                Luban.with(PicShowActivity.this)
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

                return;
            }
        }
    };

    private void setFinish(int listSize){
        if (count == listSize) {
            progressDialog.dismiss();
            System.out.println("压缩结束");
            Intent intent = getIntent();
            intent.putExtra("pathList", compressPathList);
            setResult(RESULT_FINISH, intent);
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 截图返回
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            if (type == 0) {
                // 更新数据源中的图片
                AlbumActivity.listPhoto.get(viewPager.getCurrentItem()).setFilePath(resultUri.getPath());
            }
            if (type == 1) {
                // 更新数据源中的图片
                AlbumActivity.listCheckPhoto.get(viewPager.getCurrentItem()).setFilePath(resultUri.getPath());
            }
            previewAdapter.notifyDataSetChanged();
            ((PicShowFragment) adapter.getItem(viewPager.getCurrentItem())).refresh(resultUri.getPath());
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
    }

    /**
     * 获取选中的图片数量，因为有一些没选中的，其isSelect==false
     *
     * @return
     */
    private int getCheckCount() {
        int count = 0;
        for (Photo photo : AlbumActivity.listCheckPhoto) {
            if (photo.isSelect()) ++count;
        }
        return count;
    }

    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            super.finishUpdate(container);
        }
    }
}
