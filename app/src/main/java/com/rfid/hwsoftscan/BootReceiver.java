package com.rfid.hwsoftscan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Administrator on 2018/3/31.
 * 用于开机启动
 */

public class BootReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isOpen = prefs.getBoolean("switch_scan_service", false);
//        if (isOpen) {
//            Intent toServeive = new Intent(context, SotfScanService.class);
//            context.startService(toServeive);
            Intent toServeive = new Intent(context, ConfigActivity.class);
        toServeive.putExtra("isboot", true);
            context.startActivity(toServeive);
//        }

    }
}
