package com.qianbajin.sportaccelerator.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;

import com.qianbajin.sportaccelerator.R;
import com.qianbajin.sportaccelerator.v4.PreferenceFragment;
/**
 * @author Administrator
 * @Created at 2017/11/26 0026  11:38
 * @des
 */

public class SettingFragment extends PreferenceFragment {

    private SharedPreferences mSp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_preference);
        mSp = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
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
}
