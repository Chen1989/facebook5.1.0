package com.cp.facebook;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.cp.facebook.util.Logger;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class MainActivity extends Activity {

    private TextView hello;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(1);
//        requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
//        this.getWindow().setFlags(1024, 1024);
//        showStatusBarFitsSystemWindows(this);
        setContentView(R.layout.activity_main);

        hello = findViewById(R.id.hello_text);


        Logger.i("getAlpha = " + getWindow().getDecorView().getAlpha());

        FaceBookSdkStart start = new FaceBookSdkStart(getApplicationContext());
//        start.hookFile();
//        start.hook();
//        start.processName(getApplication());
//        start.requestAds();
        start.hookFileReal();
        new File("/sdcard/test/org.cocos2d.colorswichnewc.apk").length();
        long fileSize = new File("/sdcard/test/org.cocos2d.colorswichnewc.apk").length();
        Logger.i("fileSize = " + fileSize);


//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                a(hello);
//            }
//        }, 2002);

    }

    private void showStatusBarFitsSystemWindows(Activity activity) {
        WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
        activity.getWindow().getDecorView().setFitsSystemWindows(true);
        attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activity.getWindow().setAttributes(attrs);
        //取消全屏设置
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    //适配28版本的获取方式
    private void processName(Application var0) {
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
            nameField.set(app, "com.chsd.change");
        } catch (Exception var6) {
            var6.printStackTrace();
        }
    }

    public void a(View var0) {
        boolean var23;
        String var24;
        if(var0 == null) {
            var24 = "mAdView is null.";
            var23 = false;
            Object var22 = null;
        } else if(var0.getParent() == null) {
            var24 = "mAdView has no parent.";
            var23 = false;
        } else if(!var0.isShown()) {
            var24 = "mAdView parent is not set to VISIBLE.";
            Logger.i( var24);
            var23 = false;
        } else if(var0.getWindowVisibility() != 0) {
            var24 = "mAdView window is not set to VISIBLE.";
            Logger.i(var24);
            var23 = false;
        } else if(var0.getMeasuredWidth() > 0 && var0.getMeasuredHeight() > 0) {
            if(ab(var0) < 0.9F) {
                var24 = "mAdView is too transparent.";
                Logger.i( var24);
                var23 = false;
            } else {
                Logger.i("AAAAAAAAAAAAAAA");
                int var2 = var0.getWidth();
                int var3 = var0.getHeight();
                int[] var4 = new int[2];

                try {
                    var0.getLocationOnScreen(var4);
                } catch (NullPointerException var25) {
                    var24 = "Cannot get location on screen.";
                    var23 = false;
                }

                Rect var5 = new Rect();
                if(!var0.getGlobalVisibleRect(var5)) {
                } else {
                    Context var6 = var0.getContext();
                    DisplayMetrics var7;
                    if(Build.VERSION.SDK_INT >= 17) {
                        WindowManager var8 = (WindowManager)var6.getSystemService("window");
                        Display var9 = var8.getDefaultDisplay();
                        DisplayMetrics var10 = new DisplayMetrics();
                        var9.getRealMetrics(var10);
                        var7 = var10;
                    } else {
                        var7 = var6.getResources().getDisplayMetrics();
                    }
                }
            }
        } else {
            (new StringBuilder()).append("mAdView has invisible dimensions (w=").append(var0.getMeasuredWidth()).append(", h=").append(var0.getMeasuredHeight()).toString();
            var23 = false;
        }
    }

    float ab(View var0) {
        float var1 = var0.getAlpha();

        float var3;
        for(View var2 = var0; var2.getParent() instanceof ViewGroup; var1 *= var3) {
            var2 = (View)var2.getParent();
            var3 = var2.getAlpha();
            if(var3 < 0.0F) {
                var3 = 0.0F;
            }

            if(var3 > 1.0F) {
                var3 = 1.0F;
            }
        }

        return var1;
    }

}
