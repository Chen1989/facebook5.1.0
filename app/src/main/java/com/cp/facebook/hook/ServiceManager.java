package com.cp.facebook.hook;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by PengChen on 2017/12/25.
 */

public class ServiceManager {

    static final class DataService {
        public IBinder token;
        public Intent intent;
        public boolean rebind;
        public boolean binded;
    }

    private static Map<Object, Service> mServiceMap = new HashMap<Object, Service>();
    private static Map<Object, DataService> mDataService = new HashMap<Object, DataService>();

    private static Service createService(Context context, Intent intent, ServiceInfo info) {
        Service service = null;
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);

            // 获取默认的compatibility配置
            Class<?> compatibilityClass = Class.forName("android.content.res.CompatibilityInfo");
            Field defaultCompatibilityField = compatibilityClass.getDeclaredField("DEFAULT_COMPATIBILITY_INFO");
            Object defaultCompatibility = defaultCompatibilityField.get(null);

            Method packageInfoMethod = activityThreadClass.getDeclaredMethod("getPackageInfoNoCheck", ApplicationInfo.class,
                    compatibilityClass);
            packageInfoMethod.setAccessible(true);
            Object loadedApk = packageInfoMethod.invoke(currentActivityThread, info.applicationInfo, defaultCompatibility);

            service = (Service) Class.forName(info.name).newInstance();


            Class impContext = Class.forName("android.app.ContextImpl");

            Method contextInitMethod = impContext.getDeclaredMethod("getImpl", Context.class);
            contextInitMethod.setAccessible(true);
            Object contextImp = contextInitMethod.invoke(null, context);

            Method setMethod = contextImp.getClass().getDeclaredMethod("setOuterContext", Context.class);
            setMethod.setAccessible(true);
            setMethod.invoke(contextImp, service);

            Method serviceAttach = Service.class.getDeclaredMethod("attach", Context.class, activityThreadClass,
                    String.class, IBinder.class, Application.class, Object.class);
            serviceAttach.setAccessible(true);
            IBinder token = new Binder();

            Class nativeManager = Class.forName("android.app.ActivityManagerNative");
            Method getD = nativeManager.getDeclaredMethod("getDefault");
            getD.setAccessible(true);
            Object defaultManager = getD.invoke(null);

            serviceAttach.invoke(service, contextImp, currentActivityThread, info.name,
                    token, context.getApplicationContext().getClassLoader().getParent().getParent(),
                    defaultManager);

            service.onCreate();

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return service;
    }

    public static void startService(Context context, Intent intent, ServiceInfo info) {
        Service service = mServiceMap.get(info.name);
        if (service == null) {
            service = createService(context, intent, info);
            mServiceMap.put(info.name, service);
        }
        service.onStartCommand(intent, 0, 0);
    }

    public static void stopService(Context context, Intent intent, ServiceInfo info) {
        Service service = mServiceMap.remove(info.name);
        if (service != null) {
            service.onDestroy();
        }
    }

    public static void bindService(Context context, Intent intent, ServiceInfo info, Object connArg) {
        Service service = mServiceMap.get(connArg);
        DataService dataService = mDataService.get(connArg);
        if (service != null) {
            if (dataService.rebind) {
                service.onRebind(intent);
                dataService.rebind = false;
            } else if (!dataService.binded){
                service.onBind(intent);
            }
        } else {
            service = createService(context, intent, info);
            DataService dataService1 = new DataService();
            mServiceMap.put(connArg, service);
            dataService1.binded = true;
            dataService1.intent = intent;
            mDataService.put(connArg, dataService1);
            service.onBind(intent);
        }
    }

    public static boolean unbindService(Context context, Object connArg) {
        Service service = mServiceMap.remove(connArg);
        DataService dataService = mDataService.remove(connArg);
        if (service != null) {
            if (dataService == null) {
                service.onUnbind(null);
            } else {
                service.onUnbind(dataService.intent);
            }
            service.onDestroy();
            return true;
        }
        return false;
    }
}
