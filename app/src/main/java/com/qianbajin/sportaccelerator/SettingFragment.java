package com.qianbajin.sportaccelerator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.util.Log;

import com.qianbajin.sportaccelerator.v4.PreferenceFragment;
/**
 * @author Administrator
 * @Created at 2017/11/26 0026  11:38
 * @des
 */

public class SettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_preference);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        PreferenceScreen root = (PreferenceScreen) findPreference("ps_root");
//        SwitchPreference app = (SwitchPreference) findPreference("app");
//        app.setIcon(R.mipmap.ic_launcher);
//        app.setTitle("支付宝");
        SwitchPreference preference = new SwitchPreference(getActivity());
        preference.setKey("qq");
        preference.setIcon(R.mipmap.ic_launcher_round);
        preference.setTitle("QQ");
//        root.addPreference(preference);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String key = preference.getKey();
//        Log.d("SettingFragment", key);
//        if (key.equals("edit")) {
//            EditTextPreference preference1 = (EditTextPreference) findPreference(key);
//            EditText editText = preference1.getEditText();
//            String text = preference1.getText();
//            Log.d("SettingFragment", text);
//        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        EditTextPreference preference1 = (EditTextPreference) findPreference(key);
//        String string = sharedPreferences.getString(key, "20");
//
//        preference1.setSummary(string);
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        sp.edit().putInt("alipay_count", Integer.parseInt(string)).apply();

        Log.d("SettingFragment", key);

    }
}
