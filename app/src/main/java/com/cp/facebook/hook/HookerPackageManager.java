package com.cp.facebook.hook;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.text.TextUtils;

import com.cp.facebook.util.Constant;
import com.cp.facebook.util.Logger;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


public class HookerPackageManager
{
    public static void hook(Context context, String realpkg, final String newpkg, int versionCode, String versionName, String appLabel)
    {
        try
        {
            // 获取全局的ActivityThread对象
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);

            // 获取ActivityThread里面原始的 sPackageManager
            Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
            sPackageManagerField.setAccessible(true);
            Object sPackageManager = sPackageManagerField.get(currentActivityThread);

            // 准备好代理对象, 用来替换原始的对象
            Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");

            // 1. 替换掉ActivityThread里面的 sPackageManager 字段
            sPackageManagerField.set(currentActivityThread, getActivityManagerProxy(sPackageManager,iPackageManagerInterface,context, realpkg, newpkg,versionCode,versionName,appLabel));

            // 2. 替换 ApplicationPackageManager 里面的 mPM对象
            PackageManager pm = context.getPackageManager();
            PackageManager appPm = context.getApplicationContext().getPackageManager();
            Field mPmField = pm.getClass().getDeclaredField("mPM");
            mPmField.setAccessible(true);
            mPmField.set(pm, getActivityManagerProxy(sPackageManager,iPackageManagerInterface,context, realpkg, newpkg,versionCode,versionName,appLabel));

            mPmField.set(appPm, getActivityManagerProxy(sPackageManager,iPackageManagerInterface,context, realpkg, newpkg,versionCode,versionName,appLabel));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static Object getActivityManagerProxy(final Object host, Class<?> ia, final Context context, final String realpkg, final String newpkg, final int versionCode, final String versionName, final String appLabel)
    {
        return Proxy.newProxyInstance(ia.getClassLoader(), new Class<?>[]{ia}, new InvocationHandler()
        {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
            {
                //单独针对facebook的获取安装来源
                if ("getInstallerPackageName".equals(method.getName())) {
                    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                        if (element.toString().contains("com.facebook.ads.internal")){
                            return "com.android.vending";
                        }
                    }
                }
                if ("getPackageInfo".equals(method.getName())) {
//                    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
//                        Logger.i("getPackageInfo = " + element.toString());
//                    }
//                    Logger.i("getPackageInfo ============================== " + Arrays.asList(args).toString());

                    String name = "AZeroPlug";
                    if (("FSixPlug".equals(name) || ("AZeroPlug".equals(name))) && args[0] instanceof String)
                    {
                        if (newpkg.equals(args[0]) || realpkg.equals(args[0])) {
                            args[0] = realpkg;
                            PackageInfo packageInfo = (PackageInfo)method.invoke(host, args);
                            if (packageInfo != null) {
                                packageInfo.versionName = versionName;
                                packageInfo.versionCode = versionCode;
                                packageInfo.packageName = newpkg;
                                String configSign = "FBSign";
                                if("AZeroPlug".equals(name)) {
                                    configSign = name + "Sign";
                                }
                                String signatureStr = Constant._fakeSign;
                                if (!TextUtils.isEmpty(signatureStr)) {
                                    Signature signature = new Signature(signatureStr);
                                    packageInfo.signatures = new Signature[]{signature};
                                }
                                return packageInfo;
                            }
                        }
                    }
                }
                if (!TextUtils.isEmpty(newpkg) && !newpkg.equals(realpkg)) {
                    if ("checkPermission".equals(method.getName())) {
                        if (args.length >= 2) {
                            Object arg1 = args[1];
                            if (arg1 instanceof String)
                            {
                                String str = (String) args[1];
                                if (newpkg.equals(str)) {
                                    args[1] = realpkg;
                                    return method.invoke(host, args);
                                }
                            }
                        }
                    }
                    if ("getApplicationLabel".equals(method.getName())) {
                        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                            if (element.getClassName().contains("com.facebook.ads.internal.l")) {
                                return appLabel;
                            }
                        }
                    }
                    if ("getApplicationInfo".equals(method.getName())) {
//                        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
//                            Logger.i("PackageManager1 getApplicationInfo = " + element.toString());
//                        }
//                        Logger.i("PackageManager1 getApplicationInfo ============================== ");
//                        Logger.i("PackageManager1 args " + Arrays.asList(args).toString() + realpkg + newpkg);
                        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                            if (element.toString().contains("com.facebook.ads.internal.l.b")
                                    || element.toString().contains("org.chromium")) {
                                for (int i = 0; i < args.length; i++) {
                                    if (args[i].equals(newpkg)) {
                                        args[i] = realpkg;
                                        ApplicationInfo invoke = (ApplicationInfo) method.invoke(host, args);
                                        if (invoke != null) {
                                            invoke.packageName = newpkg;
                                            invoke.name = appLabel;
                                            invoke.labelRes = 0;
                                            invoke.nonLocalizedLabel = null;
                                            return invoke;
                                        }
                                    }
                                }
                            }
                            if (element.toString().contains("com.facebook.ads.internal.n.b")
                                    || element.toString().contains("com.facebook.ads.internal.w.b.d")) {
                                for (int i = 0; i < args.length; i++) {
                                    if (args[i].equals(newpkg) || args[i].equals(realpkg)) {
                                        args[i] = realpkg;
                                        ApplicationInfo invoke = (ApplicationInfo) method.invoke(host, args);
                                        if (invoke != null) {
                                            invoke.packageName = newpkg;
                                            invoke.name = appLabel;
                                            invoke.labelRes = 0;
                                            invoke.nonLocalizedLabel = null;
                                            if (Build.VERSION.SDK_INT >= 24) {
                                                invoke.minSdkVersion = Constant._fakeMinAppVersion;
                                            }
                                            invoke.publicSourceDir = Constant._fakePublicSourceDir;
                                            Logger.i("PackageManager1 publicSourceDir = " + invoke.publicSourceDir);
//                                            writeFBFile(filePath);
                                            return invoke;
                                        }
                                    }
                                }
                            }
                        }
                        ApplicationInfo invoke = (ApplicationInfo) method.invoke(host, args);
                        if (invoke != null) {
                            invoke.name = appLabel;
                            invoke.labelRes = 0;
                            invoke.nonLocalizedLabel = null;
                            return invoke;
                        }
                    }
                }
                Object result = PackageManagerHooker.hook(host, method, args);
                if (result != null) {
                    return result;
                }

                return method.invoke(host, args);
            }

            private void writeFBFile(String filePath) {
                String FPackageName = Constant._fakePackageName;
                if (!TextUtils.isEmpty(FPackageName)) {
                    try {
                        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath));
                        outputStream.write(600);
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
