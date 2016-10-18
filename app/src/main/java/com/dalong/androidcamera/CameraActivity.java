package com.dalong.androidcamera;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

/**
 * 拍照activity
 */
public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_camera);
        initWindows();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, createFragment(), CameraFragment.TAG)
                .commit();
    }

    public final CameraFragment createFragment() {
        CameraFragment frag = CameraFragment.newInstance();
        frag.setArguments(getIntent().getExtras());
        return frag;
    }

    private void initWindows() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Window window = getWindow();
            window.setStatusBarColor(darkenColor(0));
            window.setNavigationBarColor(0);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }



    @ColorInt
    public  int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        color = Color.HSVToColor(hsv);
        return color;
    }
}
