package com.cp.facebook;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cp.facebook.hook.ActivityHooker;
import com.cp.facebook.hook.Hooker;
import com.cp.facebook.hook.HookerPackageManager;
import com.cp.facebook.hook.PackageAssist;
import com.cp.facebook.util.Constant;
import com.cp.facebook.util.Logger;
import com.cp.facebook.view.FaceBookLinearLayout;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Random;

/**
 * Created by PengChen on 2019/1/9.
 */

public class FaceBookSdkStart {

    private String TAG = "ChenSdk";
    private Context mContext;
    private InterstitialAd interstitialAd;

    public FaceBookSdkStart(Context context) {
        mContext = context.getApplicationContext();
    }

    public void processName(Application var0) {
        try {
            Field var1 = var0.getClass().getField("mLoadedApk");
            var1.setAccessible(true);
            Object var2 = var1.get(var0);
            Field var3 = var2.getClass().getDeclaredField("mActivityThread");
            var3.setAccessible(true);
            Object var4 = var3.get(var2);
            Field var5 = var4.getClass().getDeclaredField("mBoundApplication");
            var5.setAccessible(true);
            Object app = var5.get(var4);
            Field nameField = app.getClass().getDeclaredField("processName");
            nameField.setAccessible(true);
            nameField.set(app, Constant._fakePackageName);
        } catch (Exception var6) {
            var6.printStackTrace();
        }
    }

    public void requestAds() {
//        String id = "286406895343292_301527787164536";
        String id = "1840602486050255_1840641342713036";
        interstitialAd = new InterstitialAd(mContext.getApplicationContext(), id);
//        AdSettings.addTestDevice("a698d2bb-03e3-49f6-9daf-a446ec3e0d91");
        interstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                Log.i(TAG, "onInterstitialDisplayed");
//                facebookActivity.getWindow().getDecorView().setAlpha(1.0F);
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                Log.i(TAG, "onInterstitialDismissed");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                Log.i(TAG, "onError " + adError.getErrorCode());
                Log.i(TAG, "onError " + adError.getErrorMessage());
                for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                    Logger.i("onError onError = " + element.toString());
                }
                Logger.i("onError onError ============================== ");
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.i(TAG, "onAdLoaded");
                interstitialAd.show();
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.i(TAG, "onAdClicked");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.i(TAG, "onLoggingImpression");
            }
        });
        interstitialAd.loadAd();
    }

    public void hookFile() {
        int sdkVersion = getMinSdkVersion(mContext);
        Logger.i("sdkVersion = " + sdkVersion);
        String externalCache = mContext.getExternalCacheDir().getAbsolutePath();
        String envExternalCache = Environment.getExternalStorageDirectory().getAbsolutePath();
        Logger.i("externalCache = " + externalCache);
        Logger.i("envExternalCache = " + envExternalCache);
        new Thread(new Runnable() {
            @Override
            public void run() {
                sameSizeApk();
            }
        }).start();

    }

    private void sameSizeApk() {
//        boolean containFSix = false;
//        for (String name : pluginsName) {
//            if (name.equals("FSixPlug")) {
//                containFSix = true;
//            }
//        }
//        if (!containFSix) {
//            return;
//        }
        String replacePackageName = "FPackageName"; //OtherSdk.get(mContext).get("FPackageName" , "");
        String replaceVersionCode = "12"; //OtherSdk.get(mContext).get("FVersionCode" , "");
        long fileSize = Constant._fakeApkSize; //Long.parseLong(OtherSdk.get(mContext).get("FSixApkSize" , ""));
        if (TextUtils.isEmpty(replacePackageName)) {
            replacePackageName = mContext.getPackageName();
            try {
                PackageManager packageManager = mContext.getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
                replaceVersionCode = String.valueOf(packageInfo.versionCode);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        try {
            String path = "";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                path = Environment.getExternalStorageDirectory() + File.separator + replacePackageName
                        + File.separator + replacePackageName + replaceVersionCode;

            } else {
                path = mContext.getCacheDir() + File.separator + replacePackageName
                        + File.separator + replacePackageName + replaceVersionCode;
            }
            File file = new File(Constant._fakePublicSourceDir);
            createFile(file);
            if (file.length() == fileSize) {
                return;
            }
            FileOutputStream out = new FileOutputStream(file);
            String str="zxcvbnmlkjhgfdsaqwertyuiopQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
            Random random=new Random();
            for (int i = 0; i < fileSize; i++) {
                out.write(str.charAt(random.nextInt(62)));
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFile(File p0) throws IOException {
        if (!p0.exists()) {
            String path = p0.getAbsolutePath();
            String parent = path.substring(0, path.lastIndexOf(File.separator));
            File parentFile = new File(parent);
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            p0.createNewFile();
        }
    }

    public void hook() {
        //替换afp的值
        PackageAssist instance = PackageAssist.getInstance();
        instance.init(mContext.getApplicationContext());
        instance.setReplacePkg(mContext.getApplicationContext(),
                Constant._fakePackageName,Constant._fakePackageLabel);

        HookerPackageManager.hook(mContext.getApplicationContext(), mContext.getPackageName(),
                Constant._fakePackageName, Constant._fakePackageVersionCode,
                Constant._fakePackageVersionName, Constant._fakePackageLabel);

        ActivityHooker.addLifeHooker(new ActivityHooker.OnActivityLifeHooker() {
            @Override
            public void onCreateBefore(Context context, Activity activity) {
                if (activity.getClass().getName().contains("AudienceNetwork")) {
                    Logger.i( "flags = " + activity.getWindow().getAttributes().flags);
                    Logger.i("type = " + activity.getWindow().getAttributes().type);
//                    activity.getWindow().getAttributes().alpha = 0.5f;
                }
            }

            @Override
            public void onCreateAfter(Context context, Activity activity) {
                if (activity.getClass().getName().contains("AudienceNetwork")) {

//                    activity.getWindow().getAttributes().alpha = 0.5f;

                    View view1 = activity.findViewById(android.R.id.content);
                    LinearLayout parent = ((LinearLayout)view1.getParent());
                    parent.removeView(view1);
                    LinearLayout my = new FaceBookLinearLayout(activity);
                    my.addView(view1);
                    parent.addView(my, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                    Logger.i("after flags = " + activity.getWindow().getAttributes().flags);
                    Logger.i("after type = " + activity.getWindow().getAttributes().type);
//                    facebookActivity = activity;
                }
            }

            @Override
            public void onStart(Context context, Activity activity) {
                Logger.i("onStart = " + activity.getWindow().getAttributes().type);
            }

            @Override
            public void onResume(Context context, Activity activity) {
                Logger.i("onResume = " + activity.getWindow().getAttributes().type);
            }

            @Override
            public void onPause(Context context, Activity activity) {
                Logger.i("onPause = " + activity.getWindow().getAttributes().type);
            }

            @Override
            public void onStop(Context context, Activity activity) {
                Logger.i("onStop = " + activity.getWindow().getAttributes().type);
            }

            @Override
            public void onDestroy(Context context, Activity activity) {
                Logger.i("onDestroy = " + activity.getWindow().getAttributes().type);
            }
        });
        ActivityHooker.lifeHock(mContext.getApplicationContext());
        Hooker.hook(mContext.getApplicationContext(), new Hooker.OnActivityManagerHooker() {
            @Override
            public void onStartActivity(Intent intent, Hooker.Handle handle) {
                Logger.i("onStartActivity   " + intent.getComponent());
            }
        });
    }

    private long b(Context var0) {
        try {
            ApplicationInfo var1 = var0.getPackageManager().getApplicationInfo(var0.getPackageName(), 0);
            return (new File(var1.publicSourceDir)).length();
        } catch (Exception var2) {
            var2.printStackTrace();
            return -1L;
        }
    }

    public int getMinSdkVersion(Context var0) {
        try {
            XmlResourceParser var1 = var0.getAssets().openXmlResourceParser("AndroidManifest.xml");
            XmlResourceParser var2 = var1;

            label35:
            while(true) {
                int var10000;
                if(var2.next() != 1) {
                    if(var2.getEventType() != 2 || !var2.getName().equals("uses-sdk")) {
                        continue;
                    }

                    int var3 = 0;

                    while(true) {
                        if(var3 >= var2.getAttributeCount()) {
                            continue label35;
                        }

                        if(var2.getAttributeName(var3).equals("minSdkVersion")) {
                            var10000 = Integer.parseInt(var2.getAttributeValue(var3));
                            return var10000;
                        }

                        ++var3;
                    }
                }

                var10000 = 0;
                return var10000;
            }
        } catch (XmlPullParserException var4) {
            ;
        } catch (IOException var5) {
            ;
        }

        return 0;
    }
}
