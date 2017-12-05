package com.qianbajin.sportaccelerator;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * @author Administrator
 * @Created at 2017/12/4 0004  23:37
 * @des
 */
public class ConfigActivity extends AppCompatActivity {

    public static final String ARG = "arg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        String pk = getIntent().getStringExtra(ARG);
        Fragment fragment;
        String title;
        if (pk.equals(Constant.PK_ALIPAY)) {
            fragment = new AliFragment();
            title = getString(R.string.alipay);
        } else {
            fragment = new QQFragment();
            title = getString(R.string.qq);
        }
        setTitle(title);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragment).commit();

    }
}
