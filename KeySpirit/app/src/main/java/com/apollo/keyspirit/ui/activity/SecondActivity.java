package com.apollo.keyspirit.ui.activity;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.apollo.keyspirit.R;
import com.apollo.keyspirit.ui.fragment.HolderFragment;
import com.apollo.keyspirit.ui.fragment.VideoFragment;
import com.apollo.keyspirit.widget.MyVPAdapter;
import com.apollo.keyspirit.widget.MyVideoView;
import com.apollo.keyspirit.widget.MyViewPager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class SecondActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    @BindView(R.id.vp)
    MyViewPager mVp;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_second;
    }

    @Override
    protected void initView() {
        List<Fragment> fragments = new ArrayList<>();
        VideoFragment videoFragment = new VideoFragment();
        HolderFragment holderLeft = new HolderFragment();
        HolderFragment holderRight = new HolderFragment();
        fragments.add(holderLeft);
        fragments.add(videoFragment);
        fragments.add(holderRight);
        MyVPAdapter adapter = new MyVPAdapter(getSupportFragmentManager(), fragments);
        mVp.setAdapter(adapter);
        mVp.setCurrentItem(1);
        mVp.addOnPageChangeListener(this);
        mVp.setCanScroll(true);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            mVp.setCurrentItem(1, false);
        }
    }
}
