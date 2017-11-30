package com.qianbajin.sportaccelerator;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.SparseArray;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
    public static final String QQ_STEP = "com.tencent.mobileqq:MSF";
    public static final String ALIPAY = "com.eg.android.AlipayGphone";
    public static final String ALIPAY_EXT = "com.eg.android.AlipayGphone:ext";
    public static final String SAMSUNG_HEALTH = "com.sec.android.app.shealth";
    private static int MAX_QQ = 60000;
    private static int M_QQ = 15, M_ALIPAY = 15, M_SHEALTH = 15, M_DYLAN = 15;
    private static float sStep, mPreStep;
    private static String sPackageName, sProcessName;
    private static XSharedPreferences sXsp;
    private static boolean sLog = true;
    private static Object sObject;
    private static SensorHook sSensorHook;
    private static int sAliStep;

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

    private static void hook(XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException, NoSuchMethodException {
        Class<?> aClass = XposedHelpers.findClass("android.hardware.SystemSensorManager$SensorEventQueue", lpparam.classLoader);
        if (sSensorHook == null) {
            Field sensorsField = XposedHelpers.findField(aClass, "mSensorsEvents");
            sSensorHook = new SensorHook(sensorsField);
            printLog("sensorHook:", sSensorHook);
        }
        XposedHelpers.findAndHookMethod(aClass, "dispatchSensorEvent", int.class, float[].class, int.class, long.class, sSensorHook);
    }

    private static void loadConfig() {
        if (!sXsp.getFile().canRead()) {
            sXsp.getFile().setReadable(true, false);
        }
        sXsp.reload();
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        sPackageName = lpparam.packageName;
        sProcessName = lpparam.processName;
        printLog("加载:", sPackageName, "     进程:", lpparam.processName, "     进程id:", Process.myPid());
//        loadConfig();
        if (sPackageName.equals(ALIPAY)) {
            sAliStep = sXsp.getInt("alipay_count", 24581);
            hook(lpparam);
            boolean plog = sXsp.getBoolean("plog", true);
            printLog("alipay_count:", sAliStep, "plog:", plog);
        }

        if (sPackageName.equals(QQ) && (sProcessName.equals(QQ_STEP) || sProcessName.equals(QQ))) {
            hook(lpparam);
        }

        if (sPackageName.equals(DYLAN)) {
            hook(lpparam);
        }

        if (sPackageName.equals(SAMSUNG_HEALTH)) {
            Class<?> pedometerService = XposedHelpers.findClass("com.samsung.android.app.shealth.tracker.pedometer.service.PedometerService", lpparam.classLoader);
            Method[] pedometerServiceDeclaredMethods = pedometerService.getDeclaredMethods();
            for (Method pedometerServiceDeclaredMethod : pedometerServiceDeclaredMethods) {
                String name = pedometerServiceDeclaredMethod.getName();
                printLog("pedometerService>DeclaredMethod:", name);
            }
            XposedBridge.hookAllMethods(pedometerService, "onSensorAvailabilityChanged", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    printLog("hook 住了 onSensorAvailabilityChanged");
                    Object[] args = param.args;
                    if (args != null) {
                        for (Object arg : args) {
                            printLog("onSensorAvailabilityChanged arg:", arg);
                        }
                    }
                }
            });
            XposedBridge.hookAllMethods(pedometerService, "onSensorAvailable", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    printLog("hook 住了 onSensorAvailable");
                    Object[] args = param.args;
                    if (args != null) {
                        for (Object arg : args) {
                            printLog("onSensorAvailable arg:", arg);
                        }
                    }
                }
            });

            Class<?> mainActivity = XposedHelpers.findClass("com.samsung.android.app.shealth.tracker.pedometer.activity.TrackerPedometerMainActivity", lpparam.classLoader);
            Method[] declaredMethods = mainActivity.getDeclaredMethods();
            for (Method method : declaredMethods) {
                printLog("mainActivity>declaredMethods:", method.getName());
            }
            XposedBridge.hookAllMethods(mainActivity, "getDataStore", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    printLog("hook 住了 getDataStore");
                    Object[] args = param.args;
                    if (args != null) {
                        for (Object arg : args) {
                            printLog("getDataStore arg:", arg);
                        }
                    }
                    Object result = param.getResult();
                    printLog("getDataStore result:", result);
                }
            });

            Class<?> informationActivity = XposedHelpers.findClass("com.samsung.android.app.shealth.tracker.pedometer.activity.TrackerPedometerInformationActivity", lpparam.classLoader);
            Method[] declaredMethods1 = informationActivity.getDeclaredMethods();
            for (Method method : declaredMethods1) {
                printLog("informationActivity>declaredMethods:", method.getName());
            }

        }

        if (sPackageName.equals(DYLAN)) {
            if (sObject != null) {
                Handler handler = new Handler(Looper.getMainLooper());
//            XposedHelpers.callMethod(sObject, "dispatchSensorEvent")
            }
        }

//        if (sPackageName.equals("com.huawei.android.ros4kbar")) {
//            Class<?> aClass = XposedHelpers.findClass("com.huawei.android.ros4kbar.activity.StartPageActivity", lpparam.classLoader);
//            XposedHelpers.findAndHookMethod(aClass, "onClick", View.class, new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    param.setResult(null);
//
//                }
//            });
//        }

        if (sPackageName.equals("com.qianbajin.sportaccelerator")) {
            Class<?> sportApp = Class.forName("com.qianbajin.sportaccelerator.SportApp", false, lpparam.classLoader);
            Field field = sportApp.getDeclaredField("sInstance");
            field.setAccessible(true);
            Object instance = field.get(sportApp);
            printLog("instance:", instance);
            Method getInstance = sportApp.getDeclaredMethod("getInstance", new Class[0]);
            getInstance.setAccessible(true);
            Object invoke = getInstance.invoke(sportApp);
            printLog("invoke:", invoke);
            Object instance1 = XposedHelpers.callStaticMethod(sportApp, "getInstance", new Object[]{});
            printLog("instance1 :", instance1);
            XposedHelpers.callMethod(sportApp, "getInstance", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object result = param.getResult();
                    printLog("result:", result);

                }
            });
//            Class<?> activityManagerService = Class.forName("com.android.server.am.ActivityManagerService", false, ClassLoader.getSystemClassLoader());
//            printLog("MainActivity", "activityManagerService:", activityManagerService);
//            Class<?> aClass = Class.forName("com.qianbajin.sportaccelerator.MainActivity", false, lpparam.classLoader);
//            XposedBridge.hookAllMethods(aClass, "printHa", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    int i = (int) param.args[0];
//                    if (i % 2 == 0) {
//                        return;
//                    }
//                    param.args[0] = 3;
//
//                }
//            });

        }
//
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        XposedBridge.log("initZygote初始化加载" + Process.myPid());
//        PathClassLoader loader = new PathClassLoader(startupParam.modulePath, ClassLoader.getSystemClassLoader());
//        Class<?> mainClass = loader.loadClass("com.qianbajin.sportaccelerator.MainActivity");
//        Method chmod = mainClass.getDeclaredMethod("chmod", new Class[]{});
//        chmod.setAccessible(true);
//        Object invoke = chmod.invoke(mainClass.newInstance());
//        XposedBridge.log("chmod permission:" + invoke);
        sXsp = new XSharedPreferences(BuildConfig.APPLICATION_ID);
        int alipay_count = sXsp.getInt("alipay_count", 10);
        printLog("init xsp ","alipay_count:", alipay_count);
    }

    private static class SensorHook extends XC_MethodHook {

        public static int COUNT;
        private Field mSensorsField;
        private SparseArray<SensorEvent> mSensorsEvents;

        public SensorHook(Field mSensorsField) {
            this.mSensorsField = mSensorsField;
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            sObject = param.thisObject;
            int handle = (int) param.args[0];
            if (mSensorsEvents == null) {
                printString("mSensorsEvents == null");
                mSensorsEvents = (SparseArray<SensorEvent>) mSensorsField.get(param.thisObject);
            }
            SensorEvent sensorEvent = mSensorsEvents.get(handle);
            if (sensorEvent == null||sensorEvent.sensor == null) {
                return;
            }
            int type = sensorEvent.sensor.getType();
            if (type == Sensor.TYPE_STEP_COUNTER || type == Sensor.TYPE_STEP_DETECTOR) {
                float value = ((float[]) param.args[1])[0];
                if (sPackageName.equals(ALIPAY)) {
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
