package com.rfid.hwsoftscan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2018/4/19.
 */

public class StartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent toServeive = new Intent(context, SotfScanService.class);
//        context.startService(toServeive);
    }
}
