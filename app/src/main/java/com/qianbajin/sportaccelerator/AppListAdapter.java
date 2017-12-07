package com.qianbajin.sportaccelerator;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;
/**
 * @author Administrator
 * @Created at 2017/11/26 0026  18:08
 * @des
 */

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppHolder> {

    private SharedPreferences mSp;
    private final PackageManager mPm;
    private List<ApplicationInfo> mList;
    private OnItemClickListener mListener;

    public AppListAdapter(PackageManager pm, List<ApplicationInfo> infoList, SharedPreferences sp) {
        mPm = pm;
        mList = infoList;
        mSp = sp;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public AppListAdapter.AppHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_list, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(v);
                }
            }
        });
        return new AppHolder(view);
    }

    @Override
    public void onBindViewHolder(AppListAdapter.AppHolder holder, int position) {
        final ApplicationInfo applicationInfo = mList.get(position);
        final String packageName = applicationInfo.packageName;
        holder.mTvName.setText(applicationInfo.loadLabel(mPm));
        holder.mIvIcon.setImageDrawable(applicationInfo.loadIcon(mPm));
        holder.mSwitch.setChecked(mSp.getBoolean(packageName, true));
        holder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSp.edit().putBoolean(packageName, isChecked).apply();
            }
        });

//        holder.mTvDes.setText(applicationInfo.deviceProtectedDataDir);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class AppHolder extends RecyclerView.ViewHolder {

        ImageView mIvIcon;
        TextView mTvName, mTvDes;
        Switch mSwitch;

        public AppHolder(View itemView) {
            super(itemView);
            mIvIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            mTvName = (TextView) itemView.findViewById(R.id.tv_name);
            mTvDes = (TextView) itemView.findViewById(R.id.tv_des);
            mSwitch = (Switch) itemView.findViewById(R.id.sw_status);

        }
    }


    public interface OnItemClickListener {

        /**
         * 条目点击事件
         *
         * @param view
         */
        void onItemClick(View view);

    }

}
