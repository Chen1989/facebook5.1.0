package com.cp.facebook.hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.cp.facebook.util.Logger;
import com.cp.facebook.util.ReflectAccess;

public class ActivityHooker {
    private static final String TAG = "Sdk ActivityHooker";
    private static OnActivityHooker hooker;
    private static OnActivityLifeHooker lifeHooker;
    private static Object lifeHookInstrumentation;
    private static Object HookInstrumentation;

    public static void addLifeHooker(OnActivityLifeHooker activityLifeHooker) {
        lifeHooker = activityLifeHooker;
    }

    public static void lifeHock(final Context context) {
        try {
            Object o = ReflectAccess.staticInvoke("android.app.ActivityThread", "currentActivityThread", new Class[0]);
            if (o != lifeHookInstrumentation) {
                lifeHookInstrumentation = new Instrumentation() {
                    @Override
                    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
                        Logger.d("newActivity: "+className);
                        return super.newActivity(cl, className, intent);
                    }

                    @Override
                    public void callActivityOnResume(final Activity activity) {
                        Logger.d("callActivityOnResume: "+activity.getClass().getName());
                        super.callActivityOnResume(activity);
                        if (lifeHooker != null) {
                            lifeHooker.onResume(context, activity);
                        }
                    }

                    @Override
                    public void callActivityOnStop(Activity activity) {
                        Logger.d("callActivityOnStop: "+activity.getClass().getName());
                        super.callActivityOnStop(activity);
                        if (lifeHooker != null) {
                            lifeHooker.onStop(context, activity);
                        }
                    }

                    @Override
                    public void callActivityOnPause(Activity activity) {
                        Logger.d("callActivityOnPause: "+activity.getClass().getName());
                        if ("com.google.android.gms.ads.AdActivity".equals(activity.getClass().getName())
                                ) {
                            super.callActivityOnPause(activity);
                            super.callActivityOnResume(activity);
                        } else {
                            super.callActivityOnPause(activity);
                        }

                        if (lifeHooker != null) {
                            lifeHooker.onPause(context, activity);
                        }
                    }

                    @Override
                    public void callActivityOnCreate(Activity activity, Bundle icicle) {
                        Logger.d("callActivityOnCreate: "+activity.getClass().getName());
                        if (lifeHooker != null) {
                            lifeHooker.onCreateBefore(context, activity);
                        }
                        super.callActivityOnCreate(activity, icicle);
                        if (lifeHooker != null) {
                            lifeHooker.onCreateAfter(context, activity);
                        }
                    }

                    @Override
                    public void callActivityOnDestroy(Activity activity) {
                        Logger.d("callActivityOnDestroy: "+activity.getClass().getName());
//                        if (!first && activity.getClass().getName().contains("AudienceNetwork")) {
//                            first = true;
//                            callActivityOnStart(activity);
//                            return;
//                        }
                        super.callActivityOnDestroy(activity);
                        if (lifeHooker != null) {
                            lifeHooker.onDestroy(context, activity);
                        }
                    }

                    @Override
                    public void callActivityOnStart(Activity activity) {
                        Logger.d("callActivityOnStart: "+activity.getClass().getName());
                        super.callActivityOnStart(activity);
                        if (lifeHooker != null) {
                            lifeHooker.onStart(context, activity);
                        }
                    }

                    @Override
                    public boolean onException(Object obj, Throwable e) {
                        Logger.e("inmobi ==onException=====: "+e.toString());
                        if (e.toString().contains("com.core.PackageAssist$VPackageContext cannot be cast to android.app.ContextImpl")) {
                            return true;
                        }
                        return super.onException(obj, e);
                    }
                };
                ReflectAccess.setValue(o, "mInstrumentation", lifeHookInstrumentation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void onCreateBefore(Context context, Activity activity) {

        if (hooker != null) {
            hooker.onCreateBefore(context, activity);
        }
    }

    private static void onCreateAfter(Context context, Activity activity) {
        if (hooker != null) {
            hooker.onCreateAfter(context, activity);
        }
    }

    public interface OnActivityHooker {
        void onCreateBefore(Context context, Activity activity);

        void onCreateAfter(Context context, Activity activity);
    }

    public interface OnActivityLifeHooker {
        void onCreateBefore(Context context, Activity activity);

        void onCreateAfter(Context context, Activity activity);
        void onStart(Context context, Activity activity);
        void onResume(Context context, Activity activity);
        void onPause(Context context, Activity activity);
        void onStop(Context context, Activity activity);
        void onDestroy(Context context, Activity activity);
    }
}
