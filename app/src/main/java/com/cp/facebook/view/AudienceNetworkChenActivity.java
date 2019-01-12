package com.cp.facebook.view;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cp.facebook.util.Logger;
import com.facebook.ads.AudienceNetworkActivity;

/**
 * Created by PengChen on 2018/12/25.
 */


public class AudienceNetworkChenActivity extends AudienceNetworkActivity {

    @Override
    public void onCreate(Bundle bundle) {
        this.requestWindowFeature(1);
        this.getWindow().setFlags(1024, 1024);
        super.onCreate(bundle);
    }

    @Override
    public void setContentView(View view) {
        FaceBookLinearLayout layout = new FaceBookLinearLayout(this);
        layout.addView(view);
        layout.setAlpha(1);
        Logger.i("setContentView");
        super.setContentView(layout);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        FaceBookLinearLayout layout = new FaceBookLinearLayout(this);
        layout.addView(view);
        layout.setAlpha(1);
        Logger.i("setContentView(,)");
        super.setContentView(view, params);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    public static class FaceBookLinearLayout extends LinearLayout {

        public FaceBookLinearLayout(Context context) {
            super(context);
            setAlpha(0.0f);
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
}
