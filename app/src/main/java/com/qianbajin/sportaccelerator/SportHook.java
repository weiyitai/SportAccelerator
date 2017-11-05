package com.qianbajin.sportaccelerator;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
/**
 * @author Administrator
 * @Created at 2017/11/5 0005  22:11
 * @des
 */

public class SportHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

//        Class<?> dClass = XposedHelpers.findClassIfExists("name.caiyao.sporteditor.SettingsActivity", lpparam.classLoader);
//        XposedBridge.log("dClass:" + dClass);
//        if (dClass != null) {
//            XposedBridge.log("dClass:" + dClass);
//            Method[] declaredMethods3 = dClass.getDeclaredMethods();    checkSecurity
//            XposedBridge.log("declaredMethods3:" + declaredMethods3.length);
//        }


        Class<?> aClass = XposedHelpers.findClassIfExists("huawei.w3.MainActivity", lpparam.classLoader);
        XposedBridge.log("aClass:" + aClass);
        if (aClass != null) {
            Method[] declaredMethods = aClass.getDeclaredMethods();
            for (Method method : declaredMethods) {
                XposedBridge.log("declaredMethods:" + method.getName());
            }
        }
        XposedBridge.hookAllMethods(aClass, "checkSecurity", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Object[] args = param.args;
                if (args != null) {
                    for (Object arg : args) {
                        XposedBridge.log("arg:" + arg);
                    }
                }

                Object result = param.getResult();
                XposedBridge.log("result:" + result);
            }
        });

        XposedBridge.hookAllMethods(aClass, "securityLock", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Object[] args = param.args;
                if (args != null) {
                    for (Object arg : args) {
                        XposedBridge.log("securityLock arg:" + arg);
                    }
                }

                Object result = param.getResult();
                XposedBridge.log("securityLock result:" + result);
            }
        });

        Class<?> eClass = XposedHelpers.findClassIfExists("huawei.w3.ui.welcome.W3SplashScreenActivity", lpparam.classLoader);
        XposedBridge.log("eClass:" + eClass);
        if (eClass != null) {
            Method[] declaredMethods4 = eClass.getDeclaredMethods();
            for (Method method : declaredMethods4) {
                XposedBridge.log("declaredMethods4:" + method.getName());
            }
        }

        Class<?> bClass = XposedHelpers.findClassIfExists("huawei.w3.auth.AuthLoginActivity", lpparam.classLoader);
        XposedBridge.log("bClass:" + bClass);
        if (bClass != null) {
            Method[] declaredMethods1 = bClass.getDeclaredMethods();
            for (Method method : declaredMethods1) {
                XposedBridge.log("declaredMethods1:" + method.getName());
            }
        }

        Class<?> cClass = XposedHelpers.findClassIfExists("uawei.w3.ui.login.W3LoginActivity", lpparam.classLoader);
        XposedBridge.log("cClass:" + cClass);
        if (cClass != null) {
            Method[] declaredMethods2 = cClass.getDeclaredMethods();
            for (Method method : declaredMethods2) {
                XposedBridge.log("declaredMethods2:" + method.getName());
            }
        }

    }

    private void hook(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable, XposedHelpers.ClassNotFoundError {

    }
}
