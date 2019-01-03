package com.thewind.album.lib.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BaseFragment extends Fragment{

    public int fragmentLayout;
    public View fragmentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 这里是为了避免使用FragmentTabHost切换的时候重新加载UI
        if (fragmentView == null) {
            fragmentView = inflater.inflate(fragmentLayout, container, false);
            instance(fragmentView);
            findViewAndSetListener(fragmentView);
            showView();
        } else {
            // 缓存的viewiew需要判断是否已经被加过parent，
            // 如果有parent需要从parent删除，要不然会发生这个view已经有parent的错误。
            ViewGroup parent = (ViewGroup) fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
            showViewCache();
        }
        return fragmentView;
    }

    /**
     * 初始化对象、数据
     */
    protected void instance(View fragmentView) {
    }

    /**
     * 获取控件、设置监听器
     */
    protected void findViewAndSetListener(View fragmentView) {
    }

    /**
     * 展示
     */
    protected void showView() {

    }

    protected void showViewCache() {

    }

    private FragmentTransaction ft;
    public void showFragment(Fragment fragment, int frameLayoutId) {
        if (fragment != null) {
            ft = getFragmentManager().beginTransaction();
            ft.add(fragment, fragment.getClass().getSimpleName());
            ft.replace(frameLayoutId, fragment);
            ft.addToBackStack(fragment.getClass().getSimpleName());
            ft.commit();
        }
    }

    public void showFragmentNoBack(Fragment fragment, int frameLayoutId) {
        if (fragment != null) {
            ft = getFragmentManager().beginTransaction();
            ft.add(fragment, fragment.getClass().getSimpleName());
            ft.replace(frameLayoutId, fragment);
            ft.commit();
        }
    }
}
