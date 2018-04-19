package com.rfid.hwsoftscan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;



public class KeyReceiver extends BroadcastReceiver {

    private String TAG = "KeyReceiver"  ;
    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int keyCode = intent.getIntExtra("keyCode", 0) ;
        if(keyCode == 0){//兼容H941
            keyCode = intent.getIntExtra("keycode", 0) ;
        }
        boolean keyDown = intent.getBooleanExtra("keydown", false) ;
//        Log.e(TAG, "KEYcODE = " + keyCode + ", Down = " + keyDown);
        boolean isOpen = prefs.getBoolean("switch_scan_service", false);
        boolean f1Enable = prefs.getBoolean("key_f1", true);
        boolean f2Enable = prefs.getBoolean("key_f2", true);
        boolean f3Enable = prefs.getBoolean("key_f3", true);
        boolean f4Enable = prefs.getBoolean("key_f4", true);
        boolean f5Enable = prefs.getBoolean("key_f5", true);
        boolean f6Enable = prefs.getBoolean("key_f6", true);
        boolean f7Enable = prefs.getBoolean("key_f7", true);
//        Log.e(TAG, "f1Enable = " + f1Enable  );
        if (isOpen) {//软件打开了服务才可以触发扫描
            Intent intentScan = new Intent(context, SotfScanService.class);
//            intentScan.setAction(SotfScanService.ACTION_SCAN_KEY);
            intentScan.putExtra("keyDown", keyDown) ;
            if (f1Enable && keyCode == 131) {
                context.startService(intentScan);
            }
            else if (f2Enable && keyCode == 132) {
                context.startService(intentScan);
            }else if (f3Enable && keyCode == 133) {
                context.startService(intentScan);
            }else if (f4Enable && keyCode == 134) {
                context.startService(intentScan);
            }else if (f5Enable && keyCode == 135) {
                context.startService(intentScan);
            }else if (f6Enable && keyCode == 136) {
                context.startService(intentScan);
            }else if (f7Enable && keyCode == 137) {
                context.startService(intentScan);
            }
        }

    }
}
