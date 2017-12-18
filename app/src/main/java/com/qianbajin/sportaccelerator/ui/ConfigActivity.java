package com.qianbajin.sportaccelerator.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.qianbajin.sportaccelerator.Constant;
import com.qianbajin.sportaccelerator.R;
import com.qianbajin.sportaccelerator.fragment.AliFragment;
import com.qianbajin.sportaccelerator.fragment.QQFragment;

/**
 * @author Administrator
 * @Created at 2017/12/4 0004  23:37
 * @des
 */
public class ConfigActivity extends AppCompatActivity {

    public static final String ARG = "arg";

    public static void show(Context context, String packageName) {
        Intent intent = new Intent(context, ConfigActivity.class);
        intent.putExtra(ConfigActivity.ARG, packageName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        String pkg = getIntent().getStringExtra(ARG);
        Fragment fragment = null;
        String title = "";
        if (pkg.equals(Constant.PKG_ALIPAY)) {
            fragment = new AliFragment();
            title = getString(R.string.alipay);
        } else {
            fragment = new QQFragment();
            title = getString(R.string.qq);
        }
        setTitle(title);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragment).commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
