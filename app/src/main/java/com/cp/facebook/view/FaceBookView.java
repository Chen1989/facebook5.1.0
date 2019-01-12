package com.cp.facebook.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by PengChen on 2018/12/25.
 */

public class FaceBookView extends View {
    private View mView;
    public FaceBookView(Context context) {
        super(context);

    }

    public FaceBookView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FaceBookView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setView(View view) {
        mView = view;
    }

}
