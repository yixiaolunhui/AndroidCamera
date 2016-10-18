package com.dalong.androidcamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

/**
 * Created by zhouweilong on 2016/10/18.
 */

public class CameraManager {

    private Context mContext;
    private Activity mActivityContext;
    private android.app.Fragment mAppFragment;
    private android.support.v4.app.Fragment mSupportFragment;
    //保存目录
    private String mSaveDir;

    //默认是否是前置相机  false为不是
    private boolean mDefaultToFrontFacing = false;

    private boolean mIsFragment = false;

    public static final String ERROR_EXTRA = "camera_error";
    public static final String STATUS_EXTRA = "camera_status";
    public static final int STATUS_RECORDED = 1;


    public CameraManager(Activity context){
        mContext=context;
        mActivityContext = context;
    }

    public CameraManager(@NonNull android.app.Fragment context) {
        mIsFragment = true;
        mContext = context.getActivity();
        mAppFragment = context;
        mSupportFragment = null;
    }

    public CameraManager(@NonNull android.support.v4.app.Fragment context) {
        mIsFragment = true;
        mContext = context.getContext();
        mSupportFragment = context;
        mAppFragment = null;
    }


    public CameraManager saveDir(@Nullable File dir) {
        if (dir == null) return saveDir((String) null);
        return saveDir(dir.getAbsolutePath());
    }

    public CameraManager saveDir(@Nullable String dir) {
        mSaveDir = dir;
        return this;
    }


    public CameraManager defaultToFrontFacing(boolean frontFacing) {
        mDefaultToFrontFacing = frontFacing;
        return this;
    }

    public Intent getIntent() {
        Intent intent = new Intent(mContext, CameraActivity.class)
                .putExtra(CameraIntentKey.SAVE_DIR, mSaveDir)
                .putExtra(CameraIntentKey.DEFAULT_TO_FRONT_FACING, mDefaultToFrontFacing);
        return intent;
    }

    public CameraManager start(int requestCode) {
        if (mIsFragment && mSupportFragment != null)
            mSupportFragment.startActivityForResult(getIntent(), requestCode);
        else if (mIsFragment && mAppFragment != null)
            mAppFragment.startActivityForResult(getIntent(), requestCode);
        else
            mActivityContext.startActivityForResult(getIntent(), requestCode);
        return this;
    }
}
