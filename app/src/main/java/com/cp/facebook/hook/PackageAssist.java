package com.cp.facebook.hook;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.cp.facebook.util.Constant;
import com.cp.facebook.util.Logger;
import com.cp.facebook.util.ReflectAccess;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class PackageAssist {
    /**
     * 原来的包名
     */
    private String realPkg;
    /**
     * 替换后的包名
     */
    private String replacePkg;

    private static PackageAssist instance;

    private Object mApplication;
    private static String mAppLabel;
    private SharedPreferences mSharedPreferences;

    public static PackageAssist getInstance() {
        if (instance == null) {
            synchronized (PackageAssist.class) {
                instance = new PackageAssist();
            }
        }
        return instance;
    }

    public static boolean isReplacePkg(String pkg) {
        if (getInstance().replacePkg == null || pkg == null)
            return false;
        return pkg.equals(getInstance().replacePkg);
    }

    public void init(Context mApplication) {
        realPkg = mApplication.getPackageName();
        this.mApplication = mApplication;
        mSharedPreferences = mApplication.getSharedPreferences("FBAdPrefs", 0);
    }

    /**
     * 替换某一参数的intent包名
     *
     * @param args
     * @param index
     */
    public static void replaceIntentReal(Object[] args, int index) {
        if (args[index] != null && args[index] instanceof Intent) {
            Intent intent = (Intent) args[index];
            String arg = null;
            if (intent.getComponent() != null) {
                arg = intent.getComponent().getPackageName();
                if (isReplacePkg(arg)) {
//                    ReflacUtlis.set(intent.getComponent(), "mPackage", getRealPkg());
                    ReflectAccess.setValue(intent.getComponent(), "mPackage", getRealPkg());
                }
            }

            if (!TextUtils.isEmpty(intent.getPackage()) && isReplacePkg(intent.getPackage())) {
                intent.setPackage(getRealPkg());
            }
        }
    }

    /**
     * 设置虚拟的包名替换真实包名
     *
     * @param replacePkg
     */
    public void setReplacePkg(Context context, String replacePkg, String appLabel) {
        if (!TextUtils.isEmpty(getInstance().replacePkg))
            return;

//        if (isReplacePkg(replacePkg))
//            return;
        mAppLabel = appLabel;
        getInstance().replacePkg = replacePkg;

        if (getInstance().mApplication == null) {
            Logger.i("替换包名失败，获取application为null");
            return;
        }
        Object mBase = ReflectAccess.getValue(getInstance().mApplication, "mBase");
//        Object mBase = ReflacUtlis.get(getInstance().mApplication, "mBase");
        if (mBase instanceof VPackageContext) {
            VPackageContext packageContext = (VPackageContext) mBase;
            packageContext.setProviderPName(replacePkg);
            return;
        }
        VPackageContext packageContext = new VPackageContext((Context) mBase);
        packageContext.setProviderPName(replacePkg);
//        ReflacUtlis.set(getInstance().mApplication, "mBase", packageContext);
        ReflectAccess.setValue(getInstance().mApplication, "mBase", packageContext);
        //这里上传替换之后的applicationContext,用于后面的验证
//        if (CommonUtil.getBPoint(context).equals("on")) {
//            DataEvent.get(context)
//                    .versionArg(CommonUtil.getPluginVersion(context, CommonUtil.MIDDLE_PLUGIN_NAME))
//                    .arg(1, "ServiceTime = " + Utils.getServiceTime(context))
//                    .arg(2, "context.getApplicationContext()" + context.getApplicationContext())
//                    .arg(3, "context = " + packageContext)
//                    .arg(4, "")
//                    .type("UploadContextChenTest").send();
//        }
    }

    public static void replaceReal(Object[] args) {
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof String) {
                    String arg = String.valueOf(args[i]);
                    if (PackageAssist.isReplacePkg(arg)) {
                        args[i] = PackageAssist.getRealPkg();
                    }
                } else {
                    if (args[i] instanceof Intent)
                        replaceIntentReal(args, i);
                }
            }
        }
    }

    public static String getRealPkg() {
        return getInstance().realPkg;
    }

    public Object getApplication() {
        return mApplication;
    }

    public class VPackageContext extends ContextWrapper {

        private String providerPName;
        private PackageManager _mPackageManager = null;

        public void setProviderPName(String providerPName) {
            this.providerPName = providerPName;
        }

        public VPackageContext(Context base) {
            super(base);
        }

        @Override
        public Context getApplicationContext() {
            return (Context) PackageAssist.getInstance().getApplication();
        }

        //admob更换包名请求失败的问题
        @Override
        public String getPackageResourcePath() {
            if (realPkg.equals(providerPName) || TextUtils.isEmpty(providerPName)) {
                return super.getPackageResourcePath();
            }
            String FPackageName = Constant._fakePackageName;
            if (!TextUtils.isEmpty(FPackageName)) {
                String fileName = Constant._fakePackageResourcePath + ".txt";
                String filePath = getFilesDir() + File.separator + fileName;
                try {
                    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath));
                    outputStream.write(FPackageName.getBytes());
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return filePath;
            }
            return super.getPackageResourcePath();
        }

        @Override
        public SharedPreferences getSharedPreferences(String name, int mode) {
            String afp = Constant._fakeAFP;
            if (name.contains("FBAdPrefs") && !TextUtils.isEmpty(afp)) {
                return new SdkSharedPreference(mSharedPreferences, afp);
            }
            return super.getSharedPreferences(name, mode);
        }

        @Override
        public String getPackageName() {
//            if (realPkg.equals(providerPName) || TextUtils.isEmpty(providerPName)) {
//                return realPkg;
//            }
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                Logger.i("getPackageName = " + element.toString());
            }
            Logger.i("getPackageName ============================== ");

            StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();
            for (int i = 0; i <  traceElements.length; i++) {
                if (traceElements[i].toString().contains("content.ContextWrapper.getPackageName")
                        && (traceElements[i + 1].toString().contains("com.facebook.ads.internal.u.c")
                            || traceElements[i + 1].toString().contains("com.facebook.ads.internal.w.f.a.a"))) {
                    return providerPName;
                }
            }

            boolean isLovinPlug = false;
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                // 广告的包名替换
                if (element.getClassName().contains("com.google.android.gms")
                        || element.getClassName().contains("com.applovin.impl.sdk.cr")  //
//                        || element.getClassName().contains("com.inmobi")
                        || element.getClassName().contains("com.appnext")
                        || element.getClassName().contains("com.chartboost")
                        || element.getClassName().contains("chromium")
                        || element.getClassName().equals("JniUtil")
                        || element.getClassName().contains("webkit")
                        || element.toString().contains("com.mobi.sdk.ADSDK.realInit")
                        || element.toString().contains("com.mobi.sdk.break")
                        || element.toString().contains("com.mobi.sdk.float")
                        || element.toString().contains("com.mobi.sdk.invoke")
                        || element.toString().contains("com.mobi.sdk.j")
                        || element.toString().contains("com.mobi.sdk.au")
                        ) {

                    if (element.toString().contains("com.google.android.gms.ads.internal.util.al.a")
                            || element.toString().contains("org.chromium.policy.CombinedPolicyProvider.linkNative")//DL android 7.0 换包名崩溃问题
                            || element.toString().contains("com.google.android.gms.common.internal.zzz")
                            || element.toString().contains("com.google.android.gms.ads.internal.overlay")
                            || element.toString().contains("com.google.android.gms.ads.identifier.AdvertisingIdClient")
                            || element.toString().contains("org.chromium.base.PackageUtils.getOwnPackageInfo")
                            || element.toString().contains("org.chromium.policy.AbstractAppRestrictionsProvider")
                            || element.toString().contains("org.chromium.policy.AbstractAppRestrictionsProvider")
                            || element.toString().contains("com.chartboost.sdk.b.c")  // 检查 CBImpressionActivity
                            || element.toString().contains("com.appnext.ads.interstitial.Interstitial.b")  // com.appnext.ads.interstitial.InterstitialActivity
                            || element.toString().contains("com.appnext.sdk.service.b.j.e")  // cmp=com.haxor/com.appnext.sdk.service.services.alarms.Default
                            || element.toString().contains("com.appnext.sdk.service.b.j.a")  // ComponentInfo{com.zq.qweasd/com.appnext.sdk.service.services.CameraNotificationJobService} xiaomi
                            || element.toString().contains("com.google.android.gms.common.internal.zzd.zza")  // Exception:java.lang.SecurityException: Unknown calling package name 'com.zq.qwe'.
                            || (element.toString().contains("com.google.android.gms.common.internal.zzf.zza") && !isLovinPlug)    //facebook使用 Exception:java.lang.SecurityException: Unknown calling package name '...'.
                            || (element.toString().contains("com.google.android.gms.common.internal") && !isLovinPlug)    //com.google.android.gms.common.internal.W.u BLU Studio Mega 7.0
                            || (element.toString().contains("com.google.android.gms.common.api.internal.bt") && !isLovinPlug)//解决根据包名找不到应用的错误(不会崩,仅仅提示错误)
                            || (element.toString().contains("org.chromium.android_webview.AwSafeBrowsingConfigHelper.getAppOptInPreference") && !isLovinPlug)//App could not find itself by package name!
                            || (element.toString().contains("com.google.android.gms.ads.internal.d.x") && !isLovinPlug)
                            || (element.toString().contains("com.google.android.gms.common.zze") && !isLovinPlug)
                            || (element.toString().contains("com.google.android.gms.internal.zzeb") && !isLovinPlug)
//                            || (element.toString().contains("com.google.android.gms.ads.internal.csi") && !isLovinPlug)
                            || (element.toString().contains("com.google.android.gms.ads.internal.util") && !isLovinPlug)//7.0 util.af,5.1
                            || element.toString().contains("com.google.android.gms.internal.zzpi.zzy")
                            || element.toString().contains("com.google.android.gms.ads.internal.util.aj.a")
                            || element.toString().contains("com.mobi.sdk.invoke.for")
                            ) {
                        return getRealPkg();
                    }
                    return providerPName;
                }
            }
            return getRealPkg();
        }

        @Override
        public ApplicationInfo getApplicationInfo() {
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                Logger.i("getApplicationInfo = " + element.toString());
            }
            Logger.i("getApplicationInfo ============================== ");

            if (realPkg.equals(providerPName) || TextUtils.isEmpty(providerPName)) {
                return super.getApplicationInfo();
            }

            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                // 广告的包名替换
                if (element.getClassName().contains("com.google.android.gms")
                        || element.getClassName().contains("com.appnext")
//                        || element.getClassName().contains("com.inmobi")
//                        || element.getClassName().contains("com.chartboost")
                        || element.getClassName().contains("com.applovin")  //
                        || element.getClassName().contains("chromium")
                        || element.getClassName().equals("JniUtil")
                        || element.getClassName().contains("webkit")
                        ) {
                    if (element.getClassName().contains("com.google.android.gms.ads.internal.d")) {
                        ApplicationInfo applicationInfo = super.getApplicationInfo();
                        if (applicationInfo != null) {
                            applicationInfo.name = mAppLabel;
                            applicationInfo.labelRes = 0;
                            applicationInfo.nonLocalizedLabel = null;
                            return applicationInfo;
                        }
                    }
                    ApplicationInfo applicationInfo = super.getApplicationInfo();
                    if (applicationInfo != null) {
                        applicationInfo.packageName = providerPName;
                        applicationInfo.name = mAppLabel;
                        applicationInfo.labelRes = 0;
                        applicationInfo.nonLocalizedLabel = null;
                        return applicationInfo;
                    }
                }
            }
            ApplicationInfo applicationInfo = super.getApplicationInfo();
            if (applicationInfo != null) {
                applicationInfo.name = mAppLabel;
                applicationInfo.labelRes = 0;
                applicationInfo.nonLocalizedLabel = null;
                return applicationInfo;
            }
            return super.getApplicationInfo();
        }


    }
}
