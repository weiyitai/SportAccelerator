package com.qianbajin.sportaccelerator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.EditText;

import com.qianbajin.sportaccelerator.v4.PreferenceFragment;
/**
 * @author Administrator
 * @Created at 2017/12/4 0004  23:32
 * @des
 */

public class QQFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences mSp;

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.config_qq);

        mSp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        Preference limit = findPreference(Constant.SP_KEY_QQ_UPPER_LIMIT);
        limit.setSummary(mSp.getString(Constant.SP_KEY_QQ_UPPER_LIMIT, "30000"));

        Preference rate = findPreference(Constant.SP_KEY_QQ_RATE);
        rate.setSummary(mSp.getString(Constant.SP_KEY_QQ_RATE, "30"));

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        switch (preference.getKey()) {
            case Constant.SP_KEY_QQ_UPPER_LIMIT:
            case Constant.SP_KEY_QQ_RATE:
                EditText editText = ((EditTextPreference) preference).getEditText();
                editText.setSelection(editText.getText().length());
                break;
            default:
                break;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        switch (key) {
            case Constant.SP_KEY_QQ_UPPER_LIMIT:
                preference.setSummary(mSp.getString(Constant.SP_KEY_QQ_UPPER_LIMIT, "30000"));
                break;
            case Constant.SP_KEY_QQ_RATE:
                preference.setSummary(mSp.getString(Constant.SP_KEY_QQ_RATE, "20"));
                break;
            default:
                break;
        }
    }
}
