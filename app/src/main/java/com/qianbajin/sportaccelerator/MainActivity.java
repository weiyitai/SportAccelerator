package com.qianbajin.sportaccelerator;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getFragmentManager().beginTransaction().replace(R.id.fl_content, new SettingFragment()).commit();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int a = 0x2;
                Log.d("MainActivity", "a:" + a);

                PackageManager pm = getPackageManager();

                long millis = System.currentTimeMillis();
//                SimpleDateFormat format6 = new SimpleDateFormat("yyyyMMdd_HH:mm:ss");
                DateFormat format6 = DateFormat.getDateTimeInstance();

                String format = format6.format(new Date(1511671883040L));
                Log.d("MainActivity", "format:" + format);
                String format0 = format6.format(new Date(1511653789541L));

                Log.d("MainActivity", "format0:" + format0);
                String format1 = format6.format(new Date(1511691064473L));
                Log.d("MainActivity", "format1:" + format1);
                String format2 = format6.format(new Date(1511653789541L));
                Log.d("MainActivity", "format2:" + format2);

                String format3 = format6.format(new Date(1511701214563L));
                Log.d("MainActivity", "format3:" + format3);

                String format4 = format6.format(new Date(1511701224352L));
                Log.d("MainActivity", "format4:" + format4);

                String format5 = format6.format(new Date(1511671883040L));
                Log.d("MainActivity", "format5:" + format5);

                long l = System.currentTimeMillis() - millis;
                Log.d("MainActivity", "l:" + l);

//                try {
//                    ApplicationInfo applicationInfo = pm.getApplicationInfo(SportHook.ALIPAY, PackageManager.GET_META_DATA);
//                    int i = applicationInfo.describeContents();
//                    Drawable drawable = applicationInfo.loadIcon(pm);
//                    Log.d("MainActivity", "drawable:" + drawable);
//                    int descriptionRes = applicationInfo.descriptionRes;
//                    Log.d("MainActivity", "i:" + i);
//                    Log.d("MainActivity", "descriptionRes:" + descriptionRes);
//                } catch (PackageManager.NameNotFoundException e) {
//                    e.printStackTrace();
//                }
//                List<PackageInfo> infoList = pm.getInstalledPackages(PackageManager.GET_META_DATA);
//                for (PackageInfo packageInfo : infoList) {
//                    ApplicationInfo applicationInfo = packageInfo.applicationInfo;
//                    int flags = applicationInfo.flags;
//                    Drawable drawable = applicationInfo.loadIcon(pm);
//                    boolean enabled = applicationInfo.enabled;
//                    Log.d("MainActivity", "enabled:" + enabled);
//                    Log.d("MainActivity", "applicationInfo:" + applicationInfo.packageName);
//
//                }

                Snackbar.make(view, /*"Replace with your own action" +*/ getTopActivityName(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_RESTARTED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        registerReceiver(new PackageAction(), intentFilter);

    }

    public String getTopActivityName() {
        String processName = null;
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
//        if (runningAppProcesses != null && !runningAppProcesses.isEmpty()) {
//            ActivityManager.RunningAppProcessInfo runningAppProcessInfo = runningAppProcesses.get(0);
//            runningAppProcessInfo.
//                    processName = runningAppProcesses.get(0).processName;
//        }
        List<ActivityManager.RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
        String className = runningTasks.get(0).topActivity.getClassName();

        return className;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private static class PackageAction extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("PackageAction", "action:" + action);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        getSharedPreferences("", MODE_MULTI_PROCESS);

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
