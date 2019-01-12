package com.cp.facebook.hook;

import java.lang.reflect.Method;

/**
 * Created by PengChen on 2017/12/24.
 * 检测manifest文件的回调
 */

public class PackageManagerHooker {
    private static IPackageManagerInfoHooker _hooker;

    public static boolean needHook() {
        return _hooker != null;
    }

    public static void addPackageManagerHooker(IPackageManagerInfoHooker hooker) {
        _hooker = hooker;
    }

    public static Object hook(Object host, Method method, Object[] args) {
        if (_hooker != null) {
            return _hooker.packageManagerHooker(host, method, args);
        }
        return null;
    }

    public interface IPackageManagerInfoHooker {
        Object packageManagerHooker(Object host, Method method, Object[] args);
    }
}
