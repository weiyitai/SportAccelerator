package com.qianbajin.sportaccelerator;

/**
 * @author Administrator
 * @Created at 2017/12/1 0001  0:59
 * @des
 */

//public class SamsungHook {
//
//            if(sPackageName.equals(SAMSUNG_HEALTH))
//
//    {
//        Class<?> pedometerService = XposedHelpers.findClass("com.samsung.android.app.shealth.tracker.pedometer.service.PedometerService", lpparam.classLoader);
//        Method[] pedometerServiceDeclaredMethods = pedometerService.getDeclaredMethods();
//        for (Method pedometerServiceDeclaredMethod : pedometerServiceDeclaredMethods) {
//            String name = pedometerServiceDeclaredMethod.getName();
//            printLog("pedometerService>DeclaredMethod:", name);
//        }
//        XposedBridge.hookAllMethods(pedometerService, "onSensorAvailabilityChanged", new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                printLog("hook 住了 onSensorAvailabilityChanged");
//                Object[] args = param.args;
//                if (args != null) {
//                    for (Object arg : args) {
//                        printLog("onSensorAvailabilityChanged arg:", arg);
//                    }
//                }
//            }
//        });
//        XposedBridge.hookAllMethods(pedometerService, "onSensorAvailable", new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                printLog("hook 住了 onSensorAvailable");
//                Object[] args = param.args;
//                if (args != null) {
//                    for (Object arg : args) {
//                        printLog("onSensorAvailable arg:", arg);
//                    }
//                }
//            }
//        });
//
//        Class<?> mainActivity = XposedHelpers.findClass("com.samsung.android.app.shealth.tracker.pedometer.activity.TrackerPedometerMainActivity", lpparam.classLoader);
//        Method[] declaredMethods = mainActivity.getDeclaredMethods();
//        for (Method method : declaredMethods) {
//            printLog("mainActivity>declaredMethods:", method.getName());
//        }
//        XposedBridge.hookAllMethods(mainActivity, "getDataStore", new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                printLog("hook 住了 getDataStore");
//                Object[] args = param.args;
//                if (args != null) {
//                    for (Object arg : args) {
//                        printLog("getDataStore arg:", arg);
//                    }
//                }
//                Object result = param.getResult();
//                printLog("getDataStore result:", result);
//            }
//        });
//
//        Class<?> informationActivity = XposedHelpers.findClass("com.samsung.android.app.shealth.tracker.pedometer.activity.TrackerPedometerInformationActivity", lpparam.classLoader);
//        Method[] declaredMethods1 = informationActivity.getDeclaredMethods();
//        for (Method method : declaredMethods1) {
//            printLog("informationActivity>declaredMethods:", method.getName());
//        }
//
//    }
//
//}
