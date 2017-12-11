package com.qianbajin.sportaccelerator.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.qianbajin.sportaccelerator.R;
import com.qianbajin.sportaccelerator.fragment.AppListFragment;
import com.qianbajin.sportaccelerator.fragment.SettingFragment;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String SP_PATH = "chmod 654 /data/data/com.qianbajin.sportaccelerator/shared_prefs/com.qianbajin.sportaccelerator_preferences.xml";
    private static final String SP_PATH_P = "chmod 755 /data/data/com.qianbajin.sportaccelerator/shared_prefs";
    private static final String SP_PATH_pp = "chmod 755 /data/data/com.qianbajin.sportaccelerator";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab);
        ViewPager viewPager = (ViewPager) findViewById(R.id.vp_content);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.app)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.setting)));

        String[] strings = getResources().getStringArray(R.array.adapt_title);

        viewPager.setAdapter(new PaperAdapter(getSupportFragmentManager(), strings));

    }

    private boolean chmod() {
        try {
            Runtime.getRuntime().exec(SP_PATH_pp);
            Runtime.getRuntime().exec(SP_PATH_P);
            Runtime.getRuntime().exec(SP_PATH);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        chmod();
        super.onBackPressed();
    }

    static class PaperAdapter extends FragmentStatePagerAdapter {

        private final String[] mTitle;

        public PaperAdapter(FragmentManager fm, String[] strings) {
            super(fm);
            mTitle = strings;
        }

        @Override
        public Fragment getItem(int position) {
            Log.d("PaperAdapter", "getItem:" + position);
            Fragment fragment;
            switch (position) {
                case 0:
                    fragment = new AppListFragment();
                    break;
                case 1:
                default:
                    fragment = new SettingFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return mTitle.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitle[position];
        }
    }

}
