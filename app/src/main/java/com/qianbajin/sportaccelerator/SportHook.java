package com.qianbajin.sportaccelerator;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.os.Process;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.qianbajin.sportaccelerator.bean.AliStepRecord;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
/**
 * @author Administrator
 * @Created at 2017/11/5 0005  22:11
 * @des
 */

public class SportHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    public static final String DYLAN = "cn.bluemobi.dylan.step";
    public static final String QQ = "com.tencent.mobileqq";
    public static final String ALIPAY = "com.eg.android.AlipayGphone";
    public static final String SAMSUNG_HEALTH = "com.sec.android.app.shealth";
    public static final String PATTERN = "yyyyMMdd-HH:mm:ss";
    private static int RATE_QQ = 15, RATE_ALI = 15, M_SHEALTH = 15, M_DYLAN = 15, SENSOR_STEP, ALI_TODAY_STEP, sAliUpperLimit;
    private static float sStep, mPreStep;
    private static String sPackageName, sProcessName;
    private static XSharedPreferences sXsp;
    private static boolean sConfigLog = true, sSensorLog;
    private static Calendar sCalendar;
    private static StringBuilder sSb;

    private static void printString(String msg) {
        if (sConfigLog) {
            XposedBridge.log(msg);
        }
    }

    private static void printLog(Object... msg) {
        if (sConfigLog) {
            sSb.setLength(0);
            for (Object o : msg) {
                sSb.append(o.toString());
            }
            XposedBridge.log(sSb.toString());
        }
    }

    private static void loadConfig() {
        sXsp.reload();
        Map<String, ?> all = sXsp.getAll();
        if (all.isEmpty()) {
            XposedBridge.log(BuildConfig.APPLICATION_ID + "加载资源失败,没有权限?尝试启动一下本App,然后正常退出吧!");
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append('{');
        Set<? extends Map.Entry<String, ?>> entries = all.entrySet();
        for (Map.Entry<String, ?> entry : entries) {
            sb.append(entry.getKey()).append('=').append(entry.getValue()).append(',');
        }
        sb.append('}');
        XposedBridge.log(sb.toString());
    }

    private static void direHook(XC_LoadPackage.LoadPackageParam lpparam, int rate) {
        Class<?> aClass = XposedHelpers.findClass("android.hardware.SystemSensorManager$SensorEventQueue", lpparam != null ? lpparam.classLoader : ClassLoader.getSystemClassLoader());
        Method dispatchSensorEvent = XposedHelpers.findMethodExact(aClass, "dispatchSensorEvent", int.class, float[].class, int.class, long.class);
        DireSportHook sportHook = new DireSportHook(rate);
        printString("DireSportHook:" + sportHook);
        XposedBridge.hookMethod(dispatchSensorEvent, sportHook);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        sPackageName = lpparam.packageName;
        sProcessName = lpparam.processName;
        if (sPackageName.equals(Constant.PK_ALIPAY)) {
            sConfigLog = sXsp.getBoolean(Constant.SP_KEY_CONFIG_LOG, true);
            printLog("加载:", sPackageName, "     进程:", lpparam.processName, "     pid:", Process.myPid());
            if (sProcessName.equals(Constant.PK_ALIPAY)) {
                loadConfig();
                Class<?> application = XposedHelpers.findClass("android.app.Application", lpparam.classLoader);
                Method onCreate = XposedHelpers.findMethodExact(application, "onCreate", new Class[0]);
                XposedBridge.hookMethod(onCreate, new AliApplicationHook());
            }
            sAliUpperLimit = Integer.parseInt(sXsp.getString(Constant.SP_KEY_ALI_UPPER_LIMIT, "30000"));
            if (sProcessName.equals(Constant.ALI_EXT)) {
                boolean sensor = sXsp.getBoolean(Constant.SP_KEY_ALI_SENSOR, false);
                printLog(":sensor:", sensor, "    ALI_TODAY_STEP:", ALI_TODAY_STEP, "    sAliUpperLimit:", sAliUpperLimit);
                if (sensor && ALI_TODAY_STEP < sAliUpperLimit) {
                    RATE_ALI = Integer.parseInt(sXsp.getString(Constant.SP_KEY_ALI_RATE, "15"));
                    direHook(lpparam, RATE_ALI);
                    sSensorLog = sXsp.getBoolean(Constant.SP_KEY_SENSOR_LOG, false);
                }
            }
        }

        if (sPackageName.equals(Constant.PK_QQ)) {
            sConfigLog = sXsp.getBoolean(Constant.SP_KEY_CONFIG_LOG, false);
            printLog("加载:", sPackageName, "     进程:", lpparam.processName, "     pid:", Process.myPid());
            if (sProcessName.equals(Constant.PK_QQ)) {
                loadConfig();
            }
            if (sProcessName.equals(Constant.QQ_MSF)) {
                RATE_QQ = Integer.parseInt(sXsp.getString(Constant.SP_KEY_QQ_RATE, "15"));
                direHook(lpparam, RATE_QQ);
                sSensorLog = sXsp.getBoolean(Constant.SP_KEY_SENSOR_LOG, false);
            }
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        XposedBridge.log("initZygote初始化加载,pid:" + Process.myPid());
        sXsp = new XSharedPreferences(BuildConfig.APPLICATION_ID);
        sCalendar = Calendar.getInstance();
        sSb = new StringBuilder(64);
    }

    private static class AliApplicationHook extends XC_MethodHook {

        private long mToday0Mills;

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            Application application = (Application) param.thisObject;
            boolean edit = sXsp.getBoolean(Constant.SP_KEY_ALI_EDIT, false);
            XposedBridge.log("onCreate,application:" + application + "    edit:" + Boolean.toString(edit));
            SharedPreferences recordSp = application.getSharedPreferences("NewPedoMeter_private", Context.MODE_PRIVATE);
            AliStepRecord todayStep = getTodayStep(application);
            AliStepRecord recordStep = getRecordStep(recordSp);
            if (todayStep != null && recordStep != null) {
                if (edit) {
                    int upperLimit = Integer.parseInt(sXsp.getString(Constant.SP_KEY_ALI_UPPER_LIMIT, "26000"));
                    int gainStep = Integer.parseInt(sXsp.getString(Constant.SP_KEY_ALI_GAIN_STEP, "19000"));
                    if (ALI_TODAY_STEP < upperLimit) {
                        long baseMills = todayStep.getTime();
                        long today0Mills = getToday0Mills();
                        int sensorStep = recordStep.getSteps();
                        if (sensorStep < 0) {
                            // 上次写入数据后
                            XposedBridge.log("sensorStep < 0,本次放弃修改");
                            return;
                        }
                        String tip;
                        // 是否是今天记录
                        if (baseMills < today0Mills) {
                            recordStep.setTime(today0Mills + 10);
                            tip = "新的一天增加步数";
                        } else if (recordStep.getTime() < today0Mills) {
                            // 0点过后今天步数时间已经更新,但传感器记录不一定更新
                            recordStep.setTime(baseMills + 10);
                            tip = "在baseStep的时间上增加步数";
                        } else {
                            tip = "在stepRecord的时间上增加步数";
                        }
                        recordStep.setSteps(sensorStep - gainStep);
                        String json = JSON.toJSON(new AliStepRecord[]{recordStep}).toString();
                        recordSp.edit().putString(Constant.ALI_SP_KEY_STEPRECORD, json).apply();
                        StringBuilder sb = new StringBuilder(64).append("修改完成>>:").append(tip).append("  修改步数的时间为:").append(new SimpleDateFormat(PATTERN, Locale.getDefault()).format(new Date(recordStep.getTime()))).append("    SENSOR_STEP:")
                                .append(SENSOR_STEP).append(">>baseStep:").append(ALI_TODAY_STEP).append("    本次增加步数:").append(gainStep).append("    修改之后步数:").append(ALI_TODAY_STEP + gainStep).append(">>mToday0Mills:").append(mToday0Mills);
                        XposedBridge.log(sb.toString());

                    } else {
                        XposedBridge.log("今天步数已经达标,明天再改吧!嘿嘿,今天步数为:" + ALI_TODAY_STEP + "    SENSOR_STEP:" + SENSOR_STEP);
                    }
                }
            }
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            XposedBridge.log("afterHookedMethod>>>android.app.Application>>baseStep:" + ALI_TODAY_STEP + "    SENSOR_STEP:" + SENSOR_STEP + ">>today0Mills:" + new SimpleDateFormat(PATTERN, Locale.getDefault()).format(new Date(getToday0Mills())));
        }

        private AliStepRecord getRecordStep(SharedPreferences recordSp) {
            String stepRecords = recordSp.getString(Constant.ALI_SP_KEY_STEPRECORD, "");
            AliStepRecord stepRecord = null;
            if (!TextUtils.isEmpty(stepRecords)) {
                List<AliStepRecord> stepRecordList = JSON.parseArray(stepRecords, AliStepRecord.class);
                if (stepRecordList != null && !stepRecordList.isEmpty()) {
                    stepRecord = stepRecordList.get(stepRecordList.size() - 1);
                    SENSOR_STEP = stepRecord.getSteps();
                }
            } else {
                XposedBridge.log("糟糕,步数记录数据为空哎!主人切换了账号?");
            }
            return stepRecord;
        }

        private AliStepRecord getTodayStep(Application application) {
            SharedPreferences baseSp = application.getSharedPreferences("NewPedoMeter", Context.MODE_PRIVATE);
            String step = baseSp.getString(Constant.ALI_SP_KEY_BASESTEP, "");
            AliStepRecord baseStep = null;
            if (!TextUtils.isEmpty(step)) {
                baseStep = JSON.parseObject(step, AliStepRecord.class);
                ALI_TODAY_STEP = baseStep.getTime() > getToday0Mills() ? baseStep.getSteps() : 0;
            } else {
                XposedBridge.log("奇怪!今天的步数数据为空哎!主人切换了账号?重新登录了?");
            }
            return baseStep;
        }

        private long getToday0Mills() {
            if (mToday0Mills == 0L) {
                sCalendar.setTimeInMillis(System.currentTimeMillis());
                sCalendar.set(Calendar.HOUR_OF_DAY, 0);
                sCalendar.set(Calendar.MINUTE, 0);
                sCalendar.set(Calendar.SECOND, 0);
                sCalendar.set(Calendar.MILLISECOND, 0);
                mToday0Mills = sCalendar.getTimeInMillis();
                XposedBridge.log("设置了时间,今天0点毫秒值:" + mToday0Mills);
            }
            return mToday0Mills;
        }

    }


    private static class DireSportHook extends XC_MethodHook {

        private final int mRate;
        private int mCount;
        private StringBuilder mSb;
        private int mAfter;

        public DireSportHook(int rate) {
            mRate = rate;
            mSb = new StringBuilder(128);
            printLog("DireSportHook rate:", rate, "    SENSOR_STEP:", SENSOR_STEP, "   ALI_TODAY_STEP:", ALI_TODAY_STEP);
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            int value = (int) ((float[]) param.args[1])[0];
//            if (SENSOR_STEP > value) {
//                value = SENSOR_STEP;
//            }
            mAfter = value + mRate * mCount;
            ((float[]) param.args[1])[0] = mAfter;
            mCount++;
            if (sSensorLog) {
                mSb.setLength(0);
                mSb.append(sProcessName)
                        .append("  传感器值:")
                        .append(value)
                        .append("    ")
                        .append(mRate)
                        .append(" * ")
                        .append(mCount)
                        .append("   步数:")
                        .append(mAfter);
                XposedBridge.log(mSb.toString());
            }
        }
    }


    private static class SensorHook extends XC_MethodHook {

        public static int COUNT;
        private HashMap<Integer, Sensor> mHandleToSensor;

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            int handle = (int) param.args[0];
            if (mHandleToSensor == null) {
                Class<?> SystemSensorManager = XposedHelpers.findClass("android.hardware.SystemSensorManager", XposedBridge.BOOTCLASSLOADER);
                Field handleToSensor = XposedHelpers.findField(SystemSensorManager, "mHandleToSensor");
                Field mManager = XposedHelpers.findField(param.thisObject.getClass().getSuperclass(), "mManager");
                Object o = mManager.get(param.thisObject);
                mHandleToSensor = (HashMap) handleToSensor.get(o);
            }
            Sensor sensor = mHandleToSensor.get(handle);
            if (sensor == null) {
                printLog("sensor == null");
                mHandleToSensor = null;
                // sensor disconnected
                return;
            }
            int type = sensor.getType();
            if (type == Sensor.TYPE_STEP_COUNTER || type == Sensor.TYPE_STEP_DETECTOR) {
                float value = ((float[]) param.args[1])[0];
                if (sPackageName.equals(ALIPAY)) {
                    int max = Math.max(((int) value), SENSOR_STEP);
                    ((float[]) param.args[1])[0] += RATE_ALI * COUNT;
                    COUNT++;
                    printLog(sProcessName, "  传感器值:", value, "   ", RATE_ALI + " * " + COUNT, "   步数:", ((float[]) param.args[1])[0]);
                }
                if (sPackageName.equals(QQ)) {
                    ((float[]) param.args[1])[0] += RATE_QQ * COUNT;
                    COUNT++;
                    printLog(sProcessName, "  传感器值:", value, "   ", RATE_QQ + " * " + COUNT, "   步数:", ((float[]) param.args[1])[0]);
                }
                if (sPackageName.equals(DYLAN)) {
                    ((float[]) param.args[1])[0] += M_DYLAN * COUNT;
                    sStep = value;
                    COUNT++;
                    printLog(sProcessName, "  传感器值:", value, "   ", M_DYLAN + " * " + COUNT, "   步数:", ((float[]) param.args[1])[0]);
                }
                if (sPackageName.equals(SAMSUNG_HEALTH)) {
                    ((float[]) param.args[1])[0] += M_SHEALTH * COUNT;
                    COUNT++;
                    printLog(sProcessName, "  传感器值:", value, "   ", M_SHEALTH + " * " + COUNT, "   步数:", ((float[]) param.args[1])[0]);
                }
            }
        }
    }

}
