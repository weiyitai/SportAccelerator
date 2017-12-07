package com.qianbajin.sportaccelerator;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
/**
 * @author Administrator
 * @Created at 2017/12/1 0001  23:19
 * @des
 */

public class AppFragment extends Fragment implements AppListAdapter.OnItemClickListener {

    private RecyclerView mRv;
    private List<ApplicationInfo> mInfoList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRv = (RecyclerView) view.findViewById(R.id.rv_app);
        PackageManager pm = getActivity().getPackageManager();
        mInfoList = new ArrayList<>();
        try {
            ApplicationInfo ali = pm.getApplicationInfo(SportHook.ALIPAY, PackageManager.GET_META_DATA);
            ApplicationInfo qq = pm.getApplicationInfo(SportHook.QQ, PackageManager.GET_META_DATA);
            mInfoList.add(ali);
            mInfoList.add(qq);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        AppListAdapter adapter = new AppListAdapter(pm, mInfoList, sp);
        adapter.setOnItemClickListener(this);
        mRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRv.setHasFixedSize(true);
        mRv.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mRv.setAdapter(adapter);

    }

    @Override
    public void onItemClick(View view) {
        int position = mRv.getChildLayoutPosition(view);
        String packageName = mInfoList.get(position).packageName;
        
    }
}
