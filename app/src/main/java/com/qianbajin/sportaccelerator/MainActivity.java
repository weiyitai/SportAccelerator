package com.qianbajin.sportaccelerator;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  
    private static final String SP_PATH = "chmod 755 /data/data/com.qianbajin.sportaccelerator/shared_prefs/com.qianbajin.sportaccelerator_preferences.xml";
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

        SparseArrayCompat<Fragment> arrayCompat = new SparseArrayCompat<>(2);
        arrayCompat.put(0, new AppListFragment());
        arrayCompat.put(1, new SettingFragment());
        String[] strings = getResources().getStringArray(R.array.adapt_title);

        viewPager.setAdapter(new PaperAdapter(getSupportFragmentManager(), strings, arrayCompat));

    }

    static class PaperAdapter extends FragmentStatePagerAdapter {

        private final SparseArrayCompat<Fragment> mArrayCompat;
        private final String[] mTitle;

        public PaperAdapter(FragmentManager fm, String[] strings, SparseArrayCompat<Fragment> arrayCompat) {
            super(fm);
            mTitle = strings;
            mArrayCompat = arrayCompat;
        }

        @Override
        public Fragment getItem(int position) {
            return mArrayCompat.get(position);
        }

        @Override
        public int getCount() {
            return mArrayCompat.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitle[position];
        }
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

}
