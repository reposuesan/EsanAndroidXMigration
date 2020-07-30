package com.example.vuforiamod.helpers;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by killerypa on 7/01/2019.
 * yahyrparedesarteaga@gmail.com
 */
public class PermissionHelper extends Application {


    public static final int MY_ACCESS_FINE_LOCATION = 1;
    public static final int MY_ACCESS_CAMERA = 2;
    public static final int MY_WRITE_EXTERNAL_STORAGE = 3;
    public static final int MY_READ_EXTERNAL_STORAGE = 4;

    public static boolean getLocation(Activity activity){
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_ACCESS_FINE_LOCATION);
            return false;
        }
        return  true;
    }

    public static boolean getCamera(Activity activity){
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, MY_ACCESS_CAMERA);
            return false;
        }
        return  true;
    }

    public static boolean getReadExternalStorage(Activity activity){
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_READ_EXTERNAL_STORAGE);
            return false;
        }
        return  true;
    }
    public static boolean getWriteExternalStorage(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_WRITE_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }
}
