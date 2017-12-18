package com.qianbajin.sportaccelerator;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Process;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.qianbajin.sportaccelerator.bean.AliStepRecord;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

    public static final String PATTERN = "yyyyMMdd-HH:mm:ss";
    private static int RATE_QQ = 15, RATE_ALI = 15, SENSOR_STEP, ALI_TODAY_STEP, sAliUpperLimit;
    private static String sPackageName, sProcessName;
    private static XSharedPreferences sXsp;
    private static boolean sConfigLog = true, sSensorLog;

    private static void printString(String msg) {
        if (sConfigLog) {
            XposedBridge.log(msg);
        }
    }

    private static void printLog(Object... msg) {
        if (sConfigLog) {
            StringBuilder sb = new StringBuilder(32);
            for (Object o : msg) {
                sb.append(o.toString());
            }
            XposedBridge.log(sb.toString());
        }
    }

    private static void loadConfig() {
        sXsp.reload();
        Map<String, ?> all = sXsp.getAll();
        if (!all.isEmpty()) {
            StringBuilder sb = new StringBuilder(64);
            sb.append('{');
            Set<? extends Map.Entry<String, ?>> entries = all.entrySet();
            for (Map.Entry<String, ?> entry : entries) {
                sb.append(entry.getKey()).append('=').append(entry.getValue()).append(',');
            }
            sb.append('}');
            XposedBridge.log(sb.toString());
        } else {
            XposedBridge.log(BuildConfig.APPLICATION_ID + "加载资源失败,没有权限?尝试启动一下本App,然后正常退出吧!");
        }
    }

    private static void direHook(XC_LoadPackage.LoadPackageParam lpparam, int rate) {
        Class<?> aClass = XposedHelpers.findClass("android.hardware.SystemSensorManager$SensorEventQueue", lpparam.classLoader);
        Method dispatchSensorEvent = XposedHelpers.findMethodExact(aClass, "dispatchSensorEvent", int.class, float[].class, int.class, long.class);
        SensorHook sensorHook = new SensorHook(rate);
        printString("SensorHook:" + sensorHook);
        XposedBridge.hookMethod(dispatchSensorEvent, sensorHook);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        sPackageName = lpparam.packageName;
        sProcessName = lpparam.processName;
        if (sPackageName.equals(Constant.PKG_ALIPAY)) {
            sConfigLog = sXsp.getBoolean(Constant.SP_KEY_CONFIG_LOG, true);
            printLog("加载:", sPackageName, "     进程:", lpparam.processName, "     pid:", Process.myPid());
            if (sProcessName.equals(Constant.PKG_ALIPAY)) {
                loadConfig();
                boolean edit = sXsp.getBoolean(Constant.SP_KEY_ALI_EDIT, false);
                XposedBridge.log("直接修改已:" + (edit ? "打开" : "关闭"));
                if (edit) {
                    Class<?> application = XposedHelpers.findClass("android.app.Application", lpparam.classLoader);
                    Method onCreate = XposedHelpers.findMethodExact(application, "onCreate", new Class[0]);
                    XposedBridge.hookMethod(onCreate, new AliApplicationHook());
                }
            } else if (sProcessName.equals(Constant.ALI_EXT)) {
                sAliUpperLimit = Integer.parseInt(sXsp.getString(Constant.SP_KEY_ALI_UPPER_LIMIT, "30000"));
                boolean sensorHook = sXsp.getBoolean(Constant.SP_KEY_ALI_SENSOR_HOOK, false);
                printLog(":sensorHook:", sensorHook, "    ALI_TODAY_STEP:", ALI_TODAY_STEP, "    sAliUpperLimit:", sAliUpperLimit);
                if (sensorHook && ALI_TODAY_STEP < sAliUpperLimit) {
                    RATE_ALI = Integer.parseInt(sXsp.getString(Constant.SP_KEY_ALI_RATE, "15"));
                    direHook(lpparam, RATE_ALI);
                    sSensorLog = sXsp.getBoolean(Constant.SP_KEY_SENSOR_LOG, false);
                }
            }
        }

        if (sPackageName.equals(Constant.PKG_QQ)) {
            sConfigLog = sXsp.getBoolean(Constant.SP_KEY_CONFIG_LOG, false);
            printLog("加载:", sPackageName, "     进程:", lpparam.processName, "     pid:", Process.myPid());
            if (sProcessName.equals(Constant.PKG_QQ)) {
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
    }

    private static class AliApplicationHook extends XC_MethodHook {

        private long mToday0Mills;
        private SharedPreferences mRecordSp;

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            Application application = (Application) param.thisObject;
            XposedBridge.log("onCreate.application");
            mRecordSp = application.getSharedPreferences("NewPedoMeter_private", Context.MODE_PRIVATE);
            AliStepRecord recordStep = getRecordStep(mRecordSp);
            AliStepRecord todayStep = getTodayStep(application);
            if (todayStep != null && recordStep != null) {
                int upperLimit = Integer.parseInt(sXsp.getString(Constant.SP_KEY_ALI_UPPER_LIMIT, "26000"));
                int gainStep = Integer.parseInt(sXsp.getString(Constant.SP_KEY_ALI_GAIN_STEP, "19000"));
                if (ALI_TODAY_STEP < upperLimit) {
                    long baseMills = todayStep.getTime();
                    long today0Mills = getToday0Mills();
                    int sensorStep = recordStep.getSteps();
                    String tip;
                    // 是否是今天记录
                    if (baseMills < today0Mills) {
                        recordStep.setTime(today0Mills + 2000);
                        tip = "新的一天增加步数";
                    } else if (recordStep.getTime() < today0Mills) {
                        // 0点过后今天步数时间已经更新,但传感器记录不一定更新
                        recordStep.setTime(baseMills + 2000);
                        tip = "在baseStep的时间上增加步数";
                    } else {
                        tip = "在stepRecord的时间上增加步数";
                    }
                    if (ALI_TODAY_STEP + gainStep > upperLimit) {
                        tip += ",本次再增加就超过步数上限了,减小修改量";
                        gainStep = upperLimit - todayStep.getSteps();
                    }
                    recordStep.setSteps(sensorStep - gainStep);
                    String json = JSON.toJSON(new AliStepRecord[]{recordStep}).toString();
                    mRecordSp.edit().putString(Constant.ALI_SP_KEY_STEPRECORD, json).apply();
                    SimpleDateFormat format = new SimpleDateFormat(PATTERN, Locale.getDefault());
                    StringBuilder sb = new StringBuilder(128).append("修改完成>>:").append(tip)
                            .append("  stepRecord的时间为:").append(format.format(new Date(recordStep.getTime())))
                            .append("  baseStep:").append(ALI_TODAY_STEP)
                            .append("  baseStepTime:").append(format.format(new Date(todayStep.getTime())))
                            .append("  本次增加步数:").append(gainStep)
                            .append("  修改之后步数:").append(ALI_TODAY_STEP + gainStep);
                    XposedBridge.log(sb.toString());
                } else {
                    XposedBridge.log("今天步数已经达标,明天再改吧!嘿嘿,今天步数为:" + ALI_TODAY_STEP + "    SENSOR_STEP:" + SENSOR_STEP);
                }
            }
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            Application application = (Application) param.thisObject;
            AliStepRecord firstStep = getFirstStep(mRecordSp);
            AliStepRecord lastStepToday = getLastStepToday(application);
            SimpleDateFormat format = new SimpleDateFormat(PATTERN, Locale.getDefault());
            StringBuilder sb = new StringBuilder(128);
            sb.append("afterHookedMethod>>>SENSOR_STEP:").append(SENSOR_STEP)
                    .append("  firstStep:").append(firstStep.getSteps())
                    .append("  firstStepTime:").append(format.format(new Date(firstStep.getTime())))
                    .append("  lastStep:").append(lastStepToday.getSteps())
                    .append("  lastStepTime:").append(format.format(new Date(lastStepToday.getTime())))
                    .append("  today0Mills:")
                    .append(format.format(new Date(getToday0Mills())));
            XposedBridge.log(sb.toString());
        }

        private AliStepRecord getFirstStep(SharedPreferences recordSp) {
            String firstStep = recordSp.getString(Constant.ALI_SP_KEY_FIRST_STEP, "");
            if (!TextUtils.isEmpty(firstStep)) {
                AliStepRecord stepRecord = JSON.parseObject(firstStep, AliStepRecord.class);
                return stepRecord;
            } else {
                XposedBridge.log("firstStep 数据为空");
            }
            return new AliStepRecord();
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

        private AliStepRecord getLastStepToday(Application application) {
            SharedPreferences baseSp = application.getSharedPreferences("NewPedoMeter", Context.MODE_PRIVATE);
            String lastStep = baseSp.getString(Constant.ALI_SP_KEY_LAST_STEP_TODAY, "");
            if (!TextUtils.isEmpty(lastStep)) {
                AliStepRecord lastSteps = JSON.parseObject(lastStep, AliStepRecord.class);
                return lastSteps;
            } else {
                XposedBridge.log("上次步数记录为空");
            }
            return new AliStepRecord();
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
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                mToday0Mills = calendar.getTimeInMillis();
            }
            return mToday0Mills;
        }
    }


    private static class SensorHook extends XC_MethodHook {

        private final int mRate;
        private int mCount;
        private StringBuilder mSb;
        private int mAfter;

        public SensorHook(int rate) {
            mRate = rate;
            mSb = new StringBuilder();
            printLog("SensorHook rate:", rate, "    SENSOR_STEP:", SENSOR_STEP, "   ALI_TODAY_STEP:", ALI_TODAY_STEP);
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            int value = (int) ((float[]) param.args[1])[0];
            mAfter = value + mRate * mCount;
            ((float[]) param.args[1])[0] = mAfter;
            mCount++;
            if (sSensorLog) {
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
                mSb.setLength(0);
            }
        }
    }
}
