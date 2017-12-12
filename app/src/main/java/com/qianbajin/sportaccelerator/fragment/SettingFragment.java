package com.qianbajin.sportaccelerator.fragment;

import com.qianbajin.sportaccelerator.R;
/**
 * @author Administrator
 * @Created at 2017/11/26 0026  11:38
 * @des
 */

public class SettingFragment extends BasePreferenceFragment {

    @Override
    protected int getXmlId() {
        return R.xml.setting_preference;
    }

    @Override
    protected boolean addPreferenceChangeListener() {
        return false;
    }
}
