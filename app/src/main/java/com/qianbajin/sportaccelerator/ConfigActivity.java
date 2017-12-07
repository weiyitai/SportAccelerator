package com.qianbajin.sportaccelerator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.qianbajin.sportaccelerator.fragment.AliEditFragment;
import com.qianbajin.sportaccelerator.fragment.AliFragment;
import com.qianbajin.sportaccelerator.fragment.QQFragment;

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
        String title = "";
        if (pk.equals(Constant.PK_ALIPAY)) {
            fragment = new AliFragment();
            title = getString(R.string.alipay);
        } else if (pk.equals(Constant.PK_QQ)) {
            fragment = new QQFragment();
            title = getString(R.string.qq);
        } else {
            fragment = new AliEditFragment();
            title = getString(R.string.edit_ali_step);
        }
        setTitle(title);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragment).commit();

    }

    public static void show(Context context,String packageName) {
        Intent intent = new Intent(context, ConfigActivity.class);
        intent.putExtra(ConfigActivity.ARG, packageName);
        context.startActivity(intent);
    }
}
