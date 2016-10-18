package com.dalong.androidcamera;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.cameraview.CameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static android.app.Activity.RESULT_CANCELED;


/**
 * camera 拍照fragment
 * Created by zhouweilong on 16/10/18.
 */
public class CameraFragment extends Fragment implements View.OnClickListener , ActivityCompat.OnRequestPermissionsResultCallback{

    public final static String TAG="CameraFragment";

    private static final String FRAGMENT_DIALOG = "dialog";

    private CameraView mCameraView;

    private Handler mBackgroundHandler;

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_SD_PERMISSION = 2;

    private ImageView takePicture;
    private ImageView switchFlash;
    private ImageView switchCamera;
    private int mCurrentFlash;
    private static final int[] FLASH_OPTIONS = {
            CameraView.FLASH_AUTO,
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
    };

    private static final int[] FLASH_ICONS = {
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on,
    };


    public CameraFragment() {
    }

    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }
    private void initView(View view) {
        mCameraView = (CameraView) view.findViewById(R.id.camera);
        takePicture = (ImageView)  view.findViewById(R.id.take_picture);
        switchFlash = (ImageView)  view.findViewById(R.id.switch_flash);
        switchCamera = (ImageView)  view.findViewById(R.id.switch_camera);
        if (mCameraView != null) {
            mCameraView.addCallback(mCallback);
        }
        takePicture.setOnClickListener(this);
        switchFlash.setOnClickListener(this);
        switchCamera.setOnClickListener(this);
        boolean facing=getArguments().getBoolean(CameraIntentKey.DEFAULT_TO_FRONT_FACING, false);
        mCameraView.setFacing(facing? CameraView.FACING_FRONT : CameraView.FACING_BACK);
        switchCamera.setBackgroundResource(facing ? R.drawable.ic_camera_front : R.drawable.ic_camera_rear);
    }
    private CameraView.Callback mCallback = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            Log.d(TAG, "onPictureTaken " + data.length);
            try{
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    savePhotoFile(data);
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    CameraFragment.ConfirmationDialogFragment
                            .newInstance(R.string.sd_permission_not_granted,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_SD_PERMISSION,
                                    R.string.sd_permission_not_granted)
                            .show(getActivity().getSupportFragmentManager(), FRAGMENT_DIALOG);
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_SD_PERMISSION);
                }
            }catch (Exception e){
                e.printStackTrace();
                CameraFragment.ConfirmationDialogFragment
                        .newInstance(R.string.sd_permission_not_granted,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_SD_PERMISSION,
                                R.string.sd_permission_not_granted)
                        .show(getActivity().getSupportFragmentManager(), FRAGMENT_DIALOG);
            }


        }

    };


    public  void savePhotoFile(final byte[] data){
        getBackgroundHandler().post(new Runnable() {
            @Override
            public void run() {
                File cameraFir = new File(Environment.getExternalStorageDirectory()+ "/DCIM/Camera/");
                if(!cameraFir.exists())cameraFir.mkdirs();

                String  saveDir = getArguments().getString(CameraIntentKey.SAVE_DIR,"");
                File file;
                if(TextUtils.isEmpty(saveDir)){
                    file = new File(cameraFir, System.currentTimeMillis()+".jpg");
                }else{
                    File saveFir=new File(saveDir);
                    if(!saveFir.exists())saveFir.mkdirs();
                    file = new File(saveFir, System.currentTimeMillis()+".jpg");
                }
                OutputStream os = null;
                Intent intent=new Intent();
                try {
                    os = new FileOutputStream(file);
                    os.write(data);
                    os.close();
                    intent.putExtra(CameraManager.STATUS_EXTRA, CameraManager.STATUS_RECORDED)
                            .setDataAndType(Uri.parse(file.getPath()), "image/jpeg");
                    //发广播更新图库
                    getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.parse("file://"+file.getPath())));
                    getActivity().setResult(Activity.RESULT_OK,intent);
                    getActivity().finish();
                } catch (IOException e) {
                    Log.w(TAG, "Cannot write to " + file, e);
                    getActivity().setResult(RESULT_CANCELED, new Intent().putExtra(CameraManager.ERROR_EXTRA, e));
                    getActivity().finish();
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            // Ignore
                            getActivity().setResult(RESULT_CANCELED, new Intent().putExtra(CameraManager.ERROR_EXTRA, e));
                            getActivity().finish();
                        }
                    }
                }
            }
        });
    }


    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    @Override
    public void onResume() {
        super.onResume();
        try{
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                mCameraView.start();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.CAMERA)) {
                CameraFragment.ConfirmationDialogFragment
                        .newInstance(R.string.camera_permission_confirmation,
                                new String[]{Manifest.permission.CAMERA},
                                REQUEST_CAMERA_PERMISSION,
                                R.string.camera_permission_not_granted)
                        .show(getActivity().getSupportFragmentManager(), FRAGMENT_DIALOG);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
            }
        }catch (Exception e){
            e.printStackTrace();
            CameraFragment.ConfirmationDialogFragment
                    .newInstance(R.string.camera_permission_confirmation,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION,
                            R.string.camera_permission_not_granted)
                    .show(getActivity().getSupportFragmentManager(), FRAGMENT_DIALOG);
        }

    }


    @Override
    public void onPause() {
        mCameraView.stop();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (permissions.length != 1 || grantResults.length != 1) {
                    throw new RuntimeException("Error on requesting camera permission.");
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), R.string.camera_permission_not_granted,
                            Toast.LENGTH_SHORT).show();
                }
                // No need to start camera here; it is handled by onResume
                break;
            case REQUEST_SD_PERMISSION:

                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.take_picture:
                if (mCameraView != null) {
                    mCameraView.takePicture();
                }
                break;
            case R.id.switch_flash:
                if (mCameraView != null) {
                    mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                    switchFlash.setBackgroundResource(FLASH_ICONS[mCurrentFlash]);
                    mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
                }
                break;
            case R.id.switch_camera:
                if (mCameraView != null) {
                    int facing = mCameraView.getFacing();
                    switchCamera.setBackgroundResource(facing == CameraView.FACING_FRONT ?
                            R.drawable.ic_camera_rear : R.drawable.ic_camera_front);
                    mCameraView.setFacing(facing == CameraView.FACING_FRONT ?
                            CameraView.FACING_BACK : CameraView.FACING_FRONT);
                }
                break;
        }
    }

    public static class ConfirmationDialogFragment extends DialogFragment {

        private static final String ARG_MESSAGE = "message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

        public static CameraFragment.ConfirmationDialogFragment newInstance(@StringRes int message,
                                                                            String[] permissions, int requestCode,
                                                                            @StringRes int notGrantedMessage) {
            CameraFragment.ConfirmationDialogFragment fragment = new CameraFragment.ConfirmationDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_MESSAGE, message);
            args.putStringArray(ARG_PERMISSIONS, permissions);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle args = getArguments();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(args.getInt(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String[] permissions = args.getStringArray(ARG_PERMISSIONS);
                                    if (permissions == null) {
                                        throw new IllegalArgumentException();
                                    }
                                    ActivityCompat.requestPermissions(getActivity(),
                                            permissions, args.getInt(ARG_REQUEST_CODE));
//                                    startActivity(getAppDetailSettingIntent(getActivity()));
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity(),
                                            args.getInt(ARG_NOT_GRANTED_MESSAGE),
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                    .create();
        }

    }

    /**
     * 获取对应的app的设置界面
     * @param context
     * @return
     */
    private static Intent getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(),null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings","com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        return localIntent;
    }
}
