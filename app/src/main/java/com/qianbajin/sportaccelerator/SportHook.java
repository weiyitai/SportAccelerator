package com.qianbajin.sportaccelerator;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.os.Process;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

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
    public static final String QQ_MSF = "com.tencent.mobileqq:MSF";
    public static final String ALIPAY = "com.eg.android.AlipayGphone";
    public static final String ALI_EXT = "com.eg.android.AlipayGphone:ext";
    public static final String SAMSUNG_HEALTH = "com.sec.android.app.shealth";
    private static int MAX_QQ = 60000;
    private static int M_QQ = 15, M_ALIPAY = 15, M_SHEALTH = 15, M_DYLAN = 15, ALI_NOW_STEP;
    private static float sStep, mPreStep;
    private static String sPackageName, sProcessName;
    private static XSharedPreferences sXsp;
    private static boolean sLog = true;
    private static Object sObject;
    private static SensorHook sSensorHook;
    private static int sAliUpperLimit;

    static {
        XposedBridge.log("SportHook  static myPid:" + Process.myPid());
    }

    private static void printString(String... msg) {
        if (!sLog) {
            return;
        }
        StringBuilder sb = new StringBuilder(32);
        for (String s : msg) {
            sb.append(s);
        }
        XposedBridge.log(sb.toString());
    }

    private static void printLog(Object... msg) {
        if (!sLog) {
            return;
        }
        StringBuilder sb = new StringBuilder(64);
        for (Object o : msg) {
            sb.append(o.toString());
        }
        XposedBridge.log(sb.toString());
    }

    private static void loadConfig() {
        sXsp.reload();
        printLog(sXsp.getAll());
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        sPackageName = lpparam.packageName;
        sProcessName = lpparam.processName;
        printLog("加载:", sPackageName, "     进程:", lpparam.processName, "     pid:", Process.myPid());

        if (sProcessName.equals(ALI_EXT)) {
            loadConfig();
            sAliUpperLimit = sXsp.getInt(Constant.SP_KEY_ALI_UPPER_LIMIT, 50000);
            Class<?> application = XposedHelpers.findClass("android.app.Application", lpparam.classLoader);
            Method onCreate = XposedHelpers.findMethodExact(application, "onCreate", new Class[0]);
            XposedBridge.hookMethod(onCreate, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Application application = (Application) param.thisObject;
                    printLog("onCreate", "application:", application);
                    SharedPreferences newPedoMeter = application.getSharedPreferences("NewPedoMeter", Context.MODE_PRIVATE);
                    String step = newPedoMeter.getString("baseStep", "0");
                    JSONObject object = new JSONObject(step);
                    int baseStep = object.getInt("steps");
                    SharedPreferences recordSp = ((Application) param.thisObject).getSharedPreferences("NewPedoMeter_private", Context.MODE_PRIVATE);
                    String stepRecord = recordSp.getString("stepRecord", "");
                    int recordStep = 0;
                    if (!TextUtils.isEmpty(stepRecord)) {
                        JSONArray jsonArray = new JSONArray(stepRecord);
                        JSONObject jsonObject = jsonArray.getJSONObject(jsonArray.length() - 1);
                        recordStep = jsonObject.getInt("steps");
                    }
                    ALI_NOW_STEP = Math.max(baseStep, recordStep);
                    printLog("baseStep:", baseStep, "recordStep:", recordStep, "ALI_NOW_STEP:", ALI_NOW_STEP);
                }
            });
            direHook(lpparam, M_ALIPAY);
        }

        if (sProcessName.equals(QQ_MSF)) {
            direHook(lpparam, M_QQ);
        }
    }

    private void direHook(XC_LoadPackage.LoadPackageParam lpparam, int rate) {
        Class<?> aClass = XposedHelpers.findClass("android.hardware.SystemSensorManager$SensorEventQueue", lpparam.classLoader);
        Method dispatchSensorEvent = XposedHelpers.findMethodExact(aClass, "dispatchSensorEvent", int.class, float[].class, int.class, long.class);
        XposedBridge.hookMethod(dispatchSensorEvent, new DireSportHook(rate));
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        XposedBridge.log("initZygote初始化加载" + Process.myPid());
        sXsp = new XSharedPreferences(BuildConfig.APPLICATION_ID);
    }

    private static class DireSportHook extends XC_MethodHook {

        private final int mRate;
        private int mCount;

        public DireSportHook(int rate) {
            mRate = rate;
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            float value = ((float[]) param.args[1])[0];
            ((float[]) param.args[1])[0] = value + mRate * mCount;
            mCount++;
            printLog(sProcessName, "  传感器值:", value, "   ", M_ALIPAY + " * " + mCount, "   步数:", ((float[]) param.args[1])[0]);
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
                    int max = Math.max(((int) value), ALI_NOW_STEP);
                    ((float[]) param.args[1])[0] += M_ALIPAY * COUNT;
                    COUNT++;
                    printLog(sProcessName, "  传感器值:", value, "   ", M_ALIPAY + " * " + COUNT, "   步数:", ((float[]) param.args[1])[0]);
                }
                if (sPackageName.equals(QQ)) {
                    ((float[]) param.args[1])[0] += M_QQ * COUNT;
                    COUNT++;
                    printLog(sProcessName, "  传感器值:", value, "   ", M_QQ + " * " + COUNT, "   步数:", ((float[]) param.args[1])[0]);
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
