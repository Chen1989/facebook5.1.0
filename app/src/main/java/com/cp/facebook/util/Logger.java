package com.cp.facebook.util;

import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Created by anye6488 on 2016/9/9.
 */
public class Logger
{
    private static final String Tag = "ChenSdkAds";

    private static boolean isOpen;

    public static boolean isOpen()
    {
        isOpen= true;
        return isOpen;
    }

    public static void i(String msg)
    {
        if (isOpen())
            Log.i(Tag, ""+msg);
    }

    public static void d(String msg)
    {
        if (isOpen())
            Log.d(Tag, ""+msg);
    }

    public static void w(String msg)
    {
        if (isOpen())
            Log.w(Tag, ""+msg);
    }

    public static void w(String msg, Throwable e)
    {
        if (isOpen())
            Log.w(Tag, ""+msg + "\n" + getAllMessage(e));
    }

    public static void e(String msg)
    {
        if (isOpen())
            Log.e(Tag, ""+msg);
    }

    public static void e(String msg, Throwable e)
    {
        if (isOpen())
            Log.e(Tag, ""+msg + "\n" + getAllMessage(e));
    }

    public static String getAllMessage(Throwable e)
    {
        final StringBuilder buffer = new StringBuilder();
        PrintWriter writer = new PrintWriter(new Writer()
        {
            @Override
            public void close() throws IOException
            {

            }

            @Override
            public void flush() throws IOException
            {

            }

            @Override
            public void write(char[] chars, int i, int i1) throws IOException
            {
                buffer.append(chars, i, i1);
            }
        });

        e.printStackTrace(writer);

        return buffer.toString();
    }
}
