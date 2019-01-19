package com.cp.facebook.hook;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.cp.facebook.util.Constant;
import com.cp.facebook.util.Logger;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

/**
 * Created by PengChen on 2018/7/12.
 */

public class SdkSharedPreference implements SharedPreferences {
    private SharedPreferences mSharedPreferences;
    private String mAFP;
    public SdkSharedPreference(SharedPreferences sharedPreferences, String afp) {
        mSharedPreferences = sharedPreferences;
        mAFP = afp;
    }
    @Override
    public Map<String, ?> getAll() {
        return mSharedPreferences.getAll();
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return mSharedPreferences.getString(key, defValue);
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return mSharedPreferences.getStringSet(key, defValues);
    }

    @Override
    public int getInt(String key, int defValue) {
        int result = mSharedPreferences.getInt(key, defValue);
        Logger.i("result = " + result);
        if (key.equals("AppMinSdkVersion") && result == defValue) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Class cl = Class.forName("com.facebook.ads.internal.w.b.d");
                        Field version = cl.getDeclaredField("b");
                        version.setAccessible(true);
                        version.set(null, Constant._fakeMinAppVersion);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        return result;
    }

    @Override
    public long getLong(String key, long defValue) {
        return mSharedPreferences.getLong(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return mSharedPreferences.getFloat(key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return mSharedPreferences.getBoolean(key, defValue);
    }

    @Override
    public boolean contains(String key) {
        return mSharedPreferences.contains(key);
    }

    @Override
    public Editor edit() {
        StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : traceElements) {
            if (element.toString().contains("com.facebook.ads.internal.n.d")) {
                try {
                    Class faceUtil = Class.forName("com.facebook.ads.internal.n.d");
                    Field fieldB = faceUtil.getDeclaredField("c");
                    fieldB.setAccessible(true);
                    if (fieldB.get(null) != null) {
                        fieldB.set(null, mAFP);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return mSharedPreferences.edit();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mSharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
