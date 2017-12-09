package com.qianbajin.sportaccelerator;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.qianbajin.sportaccelerator.v4.PreferenceFragment;
/**
 * @author Administrator
 * @Created at 2017/11/26 0026  17:26
 * @des
 */

public class AppListFragment extends PreferenceFragment {

    private String[] mAppPkList;

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        Activity activity = getActivity();
        addPreferencesFromResource(R.xml.app_list);
        PreferenceScreen screen = (PreferenceScreen) findPreference("ps_root");
        PackageManager pm = getActivity().getPackageManager();
        mAppPkList = Constant.SUPPORT_APP_PK_LIST;
        try {
            for (String pk : mAppPkList) {
                ApplicationInfo info = pm.getApplicationInfo(pk, PackageManager.GET_META_DATA);
                Preference preference = new Preference(activity);
                preference.setTitle(info.loadLabel(pm));
                preference.setIcon(info.loadIcon(pm));
                preference.setKey(pk);
                screen.addPreference(preference);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(activity, "请检查是否安装了支付宝和QQ", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ConfigActivity.show(getActivity(), preference.getKey());
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
}
