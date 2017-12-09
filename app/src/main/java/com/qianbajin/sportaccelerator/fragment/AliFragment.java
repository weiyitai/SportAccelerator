package com.qianbajin.sportaccelerator.fragment;

import android.preference.ListPreference;
import android.text.TextUtils;
import android.util.Log;

import com.qianbajin.sportaccelerator.Constant;
import com.qianbajin.sportaccelerator.R;
/**
 * @author Administrator
 * @Created at 2017/12/4 0004  23:31
 * @des
 */

public class AliFragment extends BasePreferenceFragment {

    @Override
    protected int getXmlId() {
        return R.xml.config_alipay;
    }

    @Override
    protected void init() {
        super.init();
        ListPreference preference = (ListPreference) findPreference(Constant.PK_ALIPAY);
        CharSequence entry = preference.getEntry();
        Log.d(TAG, "entry:" + entry);
        preference.setSummary(TextUtils.isEmpty(entry) ? getString(R.string.edit) : entry);
    }
}
