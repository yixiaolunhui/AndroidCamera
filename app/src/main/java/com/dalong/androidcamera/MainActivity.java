package com.dalong.androidcamera;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public  static final  int  RESUEST_CODE=100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    /**
     * 拍照
     * @param view
     */
    public void cameraBtn(View view){
        String path=Environment.getExternalStorageDirectory()+"/dalong/";
        CameraManager cameraManager=new CameraManager(this)
                .defaultToFrontFacing(false)
                .saveDir(path)
                .start(RESUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case  RESUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this,data.getData().getPath(), Toast.LENGTH_LONG).show();
                } else if (data != null) {
                    Exception e = (Exception) data.getSerializableExtra(CameraManager.ERROR_EXTRA);
                    if (e != null) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
}
