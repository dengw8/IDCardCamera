package com.example.idcardcamera.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {
    // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权限
    private static List<String> rejectedPermissions = new ArrayList<>();

    /**
     * 检查权限，检查应用所需要的某个权限
     * @param context
     * @param requestCode  请求码
     * @param permission  权限
     * @return
     */
    public static boolean checkSinglePermission(Context context,int requestCode, String permission) {
        int permissionCode = ActivityCompat.checkSelfPermission(context, permission);
        if(permissionCode != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, requestCode);
            return false;
        }
        return true;
    }
    /**
     * 检查权限，检查应用所需要的所有权限
     * @param context
     * @param requestCode  请求码
     * @param permissions  权限数组
     * @return
     */
    public static boolean checkMultiPermissions(Context context, int requestCode, String []permissions) {
        rejectedPermissions.clear();
        for(String per : permissions) {
            int permissionCode = ActivityCompat.checkSelfPermission(context, per);
            if(permissionCode != PackageManager.PERMISSION_GRANTED) {
                rejectedPermissions.add(per);
            }
        }
        if(rejectedPermissions.isEmpty()) {
            return true;
        } else {
            String[] permissionList = rejectedPermissions.toArray(new String[rejectedPermissions.size()]);
            ActivityCompat.requestPermissions((Activity)context, permissionList, requestCode);
            return false;
        }
    }
}
