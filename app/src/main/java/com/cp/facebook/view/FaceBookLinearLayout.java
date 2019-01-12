package com.cp.facebook.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

/**
 * Created by PengChen on 2018/12/26.
 */

public class FaceBookLinearLayout extends LinearLayout {
    public FaceBookLinearLayout(Context context) {
        super(context);
        setAlpha(0.5f);
    }

    public FaceBookLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setAlpha(0.0f);
    }

    public FaceBookLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAlpha(0.0f);
    }

    @Override
    public float getAlpha() {
        Log.i("ChenSdk", "getAlpha");
        super.getAlpha();
        return 1.0f;
    }



    @Override
    public int getVisibility() {
        return VISIBLE;
    }
}
