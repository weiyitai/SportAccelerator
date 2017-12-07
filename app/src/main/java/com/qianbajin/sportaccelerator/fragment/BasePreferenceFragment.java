package com.qianbajin.sportaccelerator.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.EditText;

import com.qianbajin.sportaccelerator.v4.PreferenceFragment;
/**
 * @author Administrator
 * @Created at 2017/12/7 0007  23:46
 * @des
 */

public abstract class BasePreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "BasePreferenceFragment";
    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(getResId());
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        PreferenceScreen screen = getPreferenceScreen();
        int preferenceCount = screen.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = screen.getPreference(i);
            if (preference instanceof EditTextPreference) {
                onSharedPreferenceChanged(sp, preference.getKey());
            }
        }
    }

    /**
     * 获取布局ID
     * @return
     */
    protected abstract int getResId();

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference instanceof EditTextPreference) {
            EditText editText = ((EditTextPreference) preference).getEditText();
            editText.setSelection(editText.getText().length());
        } else if (preference instanceof Preference) {
            onPreferenceClick(preference);
        } else {

        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    protected void onPreferenceClick(Preference preference) {
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        Log.d(TAG, "onSharedPreferenceChanged:" + key);
        Preference preference = findPreference(key);
        if (preference != null && preference instanceof EditTextPreference) {
            preference.setSummary(sp.getString(key, "30000"));
        }
    }
}
