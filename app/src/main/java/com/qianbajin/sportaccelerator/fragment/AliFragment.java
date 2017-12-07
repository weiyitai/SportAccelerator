package com.qianbajin.sportaccelerator.fragment;

import android.preference.Preference;

import com.qianbajin.sportaccelerator.ConfigActivity;
import com.qianbajin.sportaccelerator.R;
/**
 * @author Administrator
 * @Created at 2017/12/4 0004  23:31
 * @des
 */

public class AliFragment extends BasePreferenceFragment {

    @Override
    protected int getResId() {
        return R.xml.config_alipay;
    }

    @Override
    protected void onPreferenceClick(Preference preference) {
        super.onPreferenceClick(preference);
        String key = preference.getKey();
        switch (key) {
            case "direct_edit":
                ConfigActivity.show(getActivity(), key);
                break;
            default:
                break;
        }
    }
}
