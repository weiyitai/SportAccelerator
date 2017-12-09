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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static int MAX_QQ = 60000;
    private static int RATE_QQ = 15, RATE_ALI = 15, M_SHEALTH = 15, M_DYLAN = 15, SENSOR_STEP, ALI_TODAY_STEP;
    private static float sStep, mPreStep;
    private static String sPackageName, sProcessName;
    private static XSharedPreferences sXsp;
    private static boolean sConfigLog = true, sSensorLog;
    private static Object sObject;
    private static SensorHook sSensorHook;
    private static int sAliUpperLimit;
    private static Calendar sCalendar;

    private static void printLog(Object... msg) {
        if (sConfigLog) {
            StringBuilder sb = new StringBuilder(64);
            for (Object o : msg) {
                sb.append(o.toString());
            }
            XposedBridge.log(sb.toString());
        }
    }

    private static void loadConfig() {
        sXsp.reload();
        Map<String, ?> all = sXsp.getAll();
        if (all.isEmpty()) {
            XposedBridge.log(BuildConfig.APPLICATION_ID + "加载资源失败");
        }
        sConfigLog = sXsp.getBoolean(Constant.SP_KEY_CONFIG_LOG, true);
        sSensorLog = sXsp.getBoolean(Constant.SP_KEY_SENSOR_LOG, false);
        printLog(sXsp.getAll());
    }

    private static void direHook(XC_LoadPackage.LoadPackageParam lpparam, int rate) {
        Class<?> aClass = XposedHelpers.findClass("android.hardware.SystemSensorManager$SensorEventQueue", lpparam != null ? lpparam.classLoader : ClassLoader.getSystemClassLoader());
        Method dispatchSensorEvent = XposedHelpers.findMethodExact(aClass, "dispatchSensorEvent", int.class, float[].class, int.class, long.class);
        DireSportHook sportHook = new DireSportHook(rate);
        printLog("sportHook:", sportHook);
        XposedBridge.hookMethod(dispatchSensorEvent, sportHook);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        sPackageName = lpparam.packageName;
        sProcessName = lpparam.processName;
        if (sPackageName.equals(Constant.PK_ALIPAY)) {
            printLog("加载:", sPackageName, "     进程:", lpparam.processName, "     pid:", Process.myPid());
            if (sProcessName.equals(Constant.PK_ALIPAY)) {
                loadConfig();
                Class<?> application = XposedHelpers.findClass("android.app.Application", lpparam.classLoader);
                Method onCreate = XposedHelpers.findMethodExact(application, "onCreate", new Class[0]);
                XposedBridge.hookMethod(onCreate, new AliApplicationHook());
            }
            sAliUpperLimit = Integer.parseInt(sXsp.getString(Constant.SP_KEY_ALI_UPPER_LIMIT, "30000"));
            printLog("在EXT进程启动之前判断,今日步数是否已达标:ALI_TODAY_STEP:", ALI_TODAY_STEP, "    sAliUpperLimit:", sAliUpperLimit);
            if (sProcessName.equals(Constant.ALI_EXT)) {
                if (Constant.SP_KEY_ALI_SENSOR.equals(sXsp.getString(Constant.PK_ALIPAY, ""))) {
                    if (ALI_TODAY_STEP < sAliUpperLimit) {
                        printLog("ALI_TODAY_STEP < sAliUpperLimit:ALI_TODAY_STEP:", ALI_TODAY_STEP, "    sAliUpperLimit:", sAliUpperLimit);
                        RATE_ALI = Integer.parseInt(sXsp.getString(Constant.SP_KEY_ALI_RATE, "15"));
                        direHook(lpparam, RATE_ALI);
                    }
                }
            }
        }

        if (sPackageName.equals(Constant.PK_QQ)) {
            printLog("加载:", sPackageName, "     进程:", lpparam.processName, "     pid:", Process.myPid());
            if (sProcessName.equals(Constant.QQ_MSF)) {
                RATE_QQ = Integer.parseInt(sXsp.getString(Constant.SP_KEY_QQ_RATE, "15"));
                direHook(lpparam, RATE_QQ);
            }
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        XposedBridge.log("initZygote初始化加载,pid:" + Process.myPid());
        sXsp = new XSharedPreferences(BuildConfig.APPLICATION_ID);
        sCalendar = Calendar.getInstance();
    }

    private static class AliApplicationHook extends XC_MethodHook {

        private long mToday0Mills;

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            boolean edit = Constant.SP_KEY_ALI_EDIT.equals(sXsp.getString(Constant.PK_ALIPAY, ""));
            Application application = (Application) param.thisObject;
            printLog("onCreate", "    application:", application, "    edit:", edit);
            SharedPreferences recordSp = application.getSharedPreferences("NewPedoMeter_private", Context.MODE_PRIVATE);
            AliStepRecord recordStep = getRecordStep(recordSp);
            AliStepRecord todayStep = getTodayStep(application);
            if (edit) {
                int upperLimit = Integer.parseInt(sXsp.getString(Constant.SP_KEY_ALI_UPPER_LIMIT, "26000"));
                int gainStep = Integer.parseInt(sXsp.getString(Constant.SP_KEY_ALI_GAIN_STEP, "19000"));
                if (ALI_TODAY_STEP < upperLimit) {
                    long time = todayStep.getTime();
                    long today0Mills = getToday0Mills();
                    // 是否是今天记录
                    String tip;
                    long haftMin = 30000;
                    if (time < today0Mills) {
                        recordStep.setTime(today0Mills + 10);
                        tip = "新的一天增加步数";
                    } else if (time + haftMin < recordStep.getTime()) {
                        // 把记录尽量靠前,这样可以跨步数大点
                        recordStep.setTime(time + haftMin);
                        tip = "在baseStep的时间上增加步数";
                    } else {
                        tip = "在stepRecord的时间上增加步数";
                    }
                    int sensorStep = recordStep.getSteps();
                    recordStep.setSteps(sensorStep - gainStep);
                    String json = JSON.toJSON(new AliStepRecord[]{recordStep}).toString();
                    recordSp.edit().putString(Constant.ALI_SP_KEY_STEPRECORD, json).apply();
                    printLog("修改完成>>:", tip, ">>baseStep:", ALI_TODAY_STEP, "    本次增加步数:", gainStep, "    修改之后步数:", ALI_TODAY_STEP + gainStep);
                }
            }
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            printLog("afterHookedMethod>>>android.app.Application>>baseStep:", ALI_TODAY_STEP, "    SENSOR_STEP:", SENSOR_STEP);
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
            }
            return stepRecord != null ? stepRecord : new AliStepRecord();
        }

        private AliStepRecord getTodayStep(Application application) {
            SharedPreferences baseSp = application.getSharedPreferences("NewPedoMeter", Context.MODE_PRIVATE);
            String step = baseSp.getString(Constant.ALI_SP_KEY_BASESTEP, "");
            AliStepRecord baseStep = null;
            if (!TextUtils.isEmpty(step)) {
                baseStep = JSON.parseObject(step, AliStepRecord.class);
                ALI_TODAY_STEP = baseStep.getTime() > getToday0Mills() ? baseStep.getSteps() : 0;
            }
            return baseStep != null ? baseStep : new AliStepRecord();
        }

        private long getToday0Mills() {
            if (mToday0Mills == 0) {
                sCalendar.setTimeInMillis(System.currentTimeMillis());
                sCalendar.set(Calendar.HOUR, 0);
                sCalendar.set(Calendar.MINUTE, 0);
                sCalendar.set(Calendar.SECOND, 0);
                sCalendar.set(Calendar.MILLISECOND, 0);
                mToday0Mills = sCalendar.getTimeInMillis();
            }
            return mToday0Mills;
        }

    }


    private static class DireSportHook extends XC_MethodHook {

        private final int mRate;
        private int mCount;

        public DireSportHook(int rate) {
            mRate = rate;
            printLog("DireSportHook rate:", rate, "    SENSOR_STEP:", SENSOR_STEP, "   ALI_TODAY_STEP:", ALI_TODAY_STEP);
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            float value = ((float[]) param.args[1])[0];
            if (SENSOR_STEP > value) {
                value = SENSOR_STEP;
            }
            ((float[]) param.args[1])[0] = value + mRate * mCount;
            mCount++;
            if (sSensorLog) {
                printLog(sProcessName, "  传感器值:", value, "    ", RATE_ALI + " * " + mCount, "   步数:", ((float[]) param.args[1])[0]);
            }
        }
    }


    private static class SensorHook extends XC_MethodHook {

        public static int COUNT;
        private HashMap<Integer, Sensor> mHandleToSensor;

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            sObject = param.thisObject;
            int handle = (int) param.args[0];
            if (mHandleToSensor == null) {
                Class<?> SystemSensorManager = XposedHelpers.findClass("android.hardware.SystemSensorManager", XposedBridge.BOOTCLASSLOADER);
                Field handleToSensor = XposedHelpers.findField(SystemSensorManager, "mHandleToSensor");
                ;
//                    handleToSensor = XposedHelpers.findField(SystemSensorManager, "sHandleToSensor");
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
