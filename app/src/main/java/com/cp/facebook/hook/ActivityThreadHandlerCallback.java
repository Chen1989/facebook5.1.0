package com.cp.facebook.hook;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.cp.facebook.util.Logger;

import java.lang.reflect.Field;

/**
 * Created by PengChen on 2017/11/29.
 */

//启动真实activity回调，使用真实的activity代替伪装的activity
public class ActivityThreadHandlerCallback implements Handler.Callback {
    private Handler handler;

    public ActivityThreadHandlerCallback(Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean handleMessage(Message msg) {
        //替换之前的Intent
        Logger.i("msg.what = " + msg.what + ", msg.obj = " + msg.obj);
        if (msg.what ==100) {
            handleLaunchActivity(msg);
        }

        handler.handleMessage(msg);
        return true;
    }

    private void handleLaunchActivity(Message msg) {
        Object obj = msg.obj;//ActivityClientRecord
        try{
            Field intentField = obj.getClass().getDeclaredField("intent");
            intentField.setAccessible(true);
            Intent proxyIntent = (Intent) intentField.get(obj);
            Intent realIntent = proxyIntent.getParcelableExtra("oldIntent");
            if (realIntent != null) {
                intentField.set(obj, realIntent);
            }
        }catch (Exception e){
            Logger.i("launch Activity failed + " + e.getMessage());
        }

    }
}
