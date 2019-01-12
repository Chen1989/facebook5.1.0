package com.cp.facebook.hook;

import android.os.Build;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by PengChen on 2019/1/9.
 */

public class FileHook {
    private static Object sfProxy;
    public static void hookFile() {
        try {
            if (Build.VERSION.SDK_INT > 23) {
                Field fsField = File.class.getDeclaredField("fs");
                fsField.setAccessible(true);
                final Object fsObject = fsField.get(null);
                if (fsObject != sfProxy) {
                    Class fileSys = Class.forName("java.io.DefaultFileSystem");
                    Method fileSysMethod = fileSys.getDeclaredMethod("getFileSystem");
                    sfProxy = fileSysMethod.invoke(null);
                    fsField.set(null, sfProxy);
                }
            } else {

            }


        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
