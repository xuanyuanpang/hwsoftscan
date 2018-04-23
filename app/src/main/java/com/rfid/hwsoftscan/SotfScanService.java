package com.rfid.hwsoftscan;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import com.hsm.barcode.DecodeResult;
import com.hsm.barcode.Decoder;
import com.hsm.barcode.DecoderConfigValues;
import com.hsm.barcode.DecoderException;
import com.hsm.barcode.SymbologyConfig;

/**
 * 此服务需要开机后一直运行，外部调用时可以传入各种指令
 */
public class SotfScanService extends Service {

    public final String TAG = "SotfScanService";

    public static final String ACTION_SCAN = "com.rfid.SCAN_CMD"; //ACTION_SCAN_CMD

    public static final String ACTION_SCAN_INIT = "com.rfid.SCAN_INIT"; //ACTION_SCAN_CMD

    public static final String ACTION_SCAN_KEY = "com.rfid.SCAN_KEY"; //ACTION_SCAN_CMD

    public static final String ACTION_SCAN_CONFIG = "com.rfid.SCAN_CONFIG"; //ACTION_SCAN_CMD

    public static final String ACTION_SCAN_SET_TIMEOUT = "com.rfid.SCAN_SET_TIMEOUT"; //ACTION_SCAN_CMD

    public static final String ACTION_KILL_SCAN = "com.rfid.KILL_SCAN"; //ACTION_SCAN_CMD

    public static final String ACTION_CLOSE_SCAN = "com.rfid.CLOSE_SCAN"; //ACTION_SCAN_CMD

    public static final String ACTION_SET_SCAN_MODE = "com.rfid.SET_SCAN_MODE"; //ACTION_SCAN_CMD

    public static final String SCAN_RESULT = "com.rfid.SCAN";//扫描结果广播返回


    private static final int  AUS_POST = 1;
    private static final int  JAPAN_POST = 3;
    private static final int  KIX = 4;
    private static final int  PLANETCODE = 5;
    private static final int  POSTNET = 6;
    private static final int  ROYAL_MAIL = 7;
    private static final int  UPU_4_STATE = 9;
    private static final int  USPS_4_STATE = 10;
    private static final int  US_POSTALS = 29;
    private static final int  CANADIAN = 30;

    public ScanBroadcast scanBroadcast ;//接收外部广播

    private Decoder mDecoder ;  //扫描解码
    private DecodeResult mDecodeResult ; //扫描结果

    private Handler mhandler = new Handler();

    private int timeOut = 3000 ;//默认设置解码超时3s

    public SotfScanService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        //注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SCAN);
        filter.addAction(ACTION_SCAN_INIT);
        filter.addAction(ACTION_KILL_SCAN);
        filter.addAction(ACTION_SCAN_KEY);
        filter.addAction(ACTION_SCAN_CONFIG);
        filter.addAction(ACTION_SCAN_SET_TIMEOUT);
        filter.addAction(ACTION_SET_SCAN_MODE);
        filter.addAction(ACTION_CLOSE_SCAN);
        scanBroadcast = new ScanBroadcast() ;
        registerReceiver(scanBroadcast, filter);
        //listner screen on/off
        IntentFilter screenFilter = new IntentFilter() ;
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF) ;
        screenFilter.addAction(Intent.ACTION_SCREEN_ON) ;
        registerReceiver(powerModeReceiver, screenFilter) ;
        Util.initSoundPool(this);
        mDecodeResult = new DecodeResult();
//        mDecoder = new Decoder() ;
//        try {
//            mDecoder.connectDecoderLibrary();
//            Log.e(TAG, "decoder init success");
////            setSymbologyPreferences(false); //设置码制
//            SetSymbologySettings();
//        } catch (Exception e) {
//            Log.e(TAG, "decoder init fail");
//            mDecoder = null ;//连接扫描头失败
//        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDecoder != null) {
            try {
//                mDecoder.stopScanning();
                mDecoder.disconnectDecoderLibrary();
                mDecoder = null ;
            } catch (DecoderException e) {
                e.printStackTrace();
            }
        }
        unregisterReceiver(scanBroadcast);
        unregisterReceiver(powerModeReceiver) ;
    }

    boolean isUp = true ;//是否松开

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.e(TAG, "onStartCommand");
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        boolean keyDown = intent.getBooleanExtra("keyDown", false);//按键是否按下
        if (keyDown && !isScanning && isUp) {
            isUp = false ;
            new Thread(new ScanRunnable()).start();//创建扫描线程
        }else if(!keyDown){//松开按键停止扫描
            isUp = true ;
            if (mDecoder != null) {
                new Thread(new StopScanThread()).start();//停止扫描线程
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    //用于停止扫描
    private class StopScanThread implements Runnable{

        @Override
        public void run() {
            try {
                if (isScanning) {
                    mDecoder.stopScanning();
                }

            } catch (DecoderException e) {
                Log.e("DecoderException", "e.getErrorCode() = " + e.getErrorCode());
//                mDecoder = null ;
                e.printStackTrace();

            }
        }
    }

    /**
     * 用于接收扫描各种指令和关闭服务的指令
     */
    class ScanBroadcast extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String action = intent.getAction() ;
//            int mode = intent.getIntExtra("mode", 1);//默认为输入框模式
            boolean keyDown = intent.getBooleanExtra("keyDown", false);//按键是否按下
            Log.e(TAG, "action = " + action);
            //根据不同的action执行不同的操作
            if (ACTION_SCAN.equals(action)) {
                new Thread(new ScanRunnable()).start();//创建扫描线程
            } else if (ACTION_KILL_SCAN.equals(action)) {
                if (mDecoder != null) {
                    try {
//                        mDecoder.stopScanning();
                        mDecoder.disconnectDecoderLibrary();
                        mDecoder = null ;
                    } catch (DecoderException e) {
                        e.printStackTrace();
                    }
                }
                SotfScanService.this.stopSelf();//关闭服务

            } else if (ACTION_CLOSE_SCAN.equals(action)) {
                if (mDecoder != null) {
                    try {
//                        mDecoder.stopScanning();
                        mDecoder.disconnectDecoderLibrary();
                        mDecoder = null ;
                    } catch (DecoderException e) {
                        e.printStackTrace();
                    }
                }
                //设置扫描服务开头为不可用
                SharedPreferences.Editor edit = prefs.edit() ;
                edit.putBoolean("switch_scan_service", false);
                edit.commit() ;

            } else if (ACTION_SCAN_CONFIG.equals(action)) {//设置参数
                if (mDecoder != null) {
                    SetSymbologySettings();
                    try {
                        SetScanningSettings();//设置扫描灯光
                    } catch (DecoderException e) {
                        e.printStackTrace();
                    }
                }

            } else if (ACTION_SCAN_INIT.equals(action)) {//扫描初始化
//                timeOut = intent.getIntExtra("timeout", 3000);
                    mDecoder = new Decoder() ;
                    mDecodeResult = new DecodeResult();
                     try {
                     mDecoder.connectDecoderLibrary();
                    Log.e(TAG, "decoder init success");
                    SetSymbologySettings();
                    } catch (Exception e) {
                    Log.e(TAG, "decoder init fail");
                    mDecoder = null ;//连接扫描头失败
                    }



            }else if (ACTION_SET_SCAN_MODE.equals(action)) {//设置输入模式
//                timeOut = intent.getIntExtra("timeout", 3000);
                int mode = intent.getIntExtra("mode", 0);//0为广播模式
                Log.e("mode", "set scan mode = " + mode) ;
                SharedPreferences.Editor edit = prefs.edit() ;
                edit.putString("inputConfig", "0");
                edit.commit() ;

            }else if (ACTION_SCAN_KEY.equals(action)) {//设置扫码超时
//                if (keyDown && !isScanning) {
//                    new Thread(new ScanRunnable()).start();//创建扫描线程
//                }else if(!keyDown){//松开按键停止扫描
//                    if (mDecoder != null) {
//                        try {
//                            mDecoder.stopScanning();
//                        } catch (DecoderException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }

            }
        }
    }

    private boolean isScanning = false ;
    //触发扫描线程
    class ScanRunnable implements Runnable{

        @Override
        public void run() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String time = prefs.getString("decode_time_limit", "3000");//扫码超时
            String mode = prefs.getString("inputConfig", "0");//扫码模式
            boolean isOpen = prefs.getBoolean("switch_scan_service", false);
            timeOut = Integer.valueOf(time);
            if (mDecoder != null && !isScanning ) {
                Log.e("mode", "mode = " + mode) ;
                    try {
                        isScanning = true ;
                        mDecoder.waitForDecodeTwo(timeOut, mDecodeResult);
                        isScanning = false ;
                        if (mDecodeResult.length > 0 && mDecoder!= null) {
                            byte[] tt = mDecoder.getBarcodeByteData();
                            Log.e(TAG, "barcode = " + new String(tt));
                            mhandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Util.play(1, 0);
                                }
                            }) ;
                            if ("0".equals(mode)) {
                                sendScanResult(tt) ;//广播模式
                            } else if ("1".equals(mode)) {
                                sendToInput(new String(tt), false) ;//后台输入
                            }

                        }

                    } catch (DecoderException e) {
                        isScanning = false ;
                    }
                isScanning = false ;
            } else if (mDecoder == null ) {
                //需要重启扫描头
                mDecodeResult = new DecodeResult();
                mDecoder = new Decoder() ;
                try {
                    mDecoder.connectDecoderLibrary();
                    Log.e(TAG, "decoder init success");
//            setSymbologyPreferences(false); //设置码制
                    SetSymbologySettings();
                } catch (Exception e) {
                    Log.e(TAG, "decoder init fail");
                    mDecoder = null ;//连接扫描头失败
                }
                this.run();//重新调用
            }
        }
    }


    /**
     * 将扫描结果以广播的形式发回
     * @param data
     */
    private void sendScanResult(byte[] data){
        Intent intent = new Intent();
        intent.putExtra("data", data);
        intent.setAction(SCAN_RESULT);
        sendBroadcast(intent);
    }

    /**
     * 将结果直接输入光标处
     * @param data
     * @param enterFlag
     */
    private void sendToInput(String data , boolean enterFlag){
        String result = getfixChar(data);


        String append = getAppendChar();
        switch (append) {
            case "1":
                enterFlag = true ;
                break ;
            case "2":
                result += "\n";
                break ;
            case "3":
                result += "\t";
                break ;
            case "4":

                break ;
        }

        Intent toBack = new Intent() ;
        toBack.setAction("android.rfid.INPUT") ;
        toBack.putExtra("data", result ) ;//发送添加前缀后缀的数据
        toBack.putExtra("enter", enterFlag) ;
        sendBroadcast(toBack) ;
    }

    private String getAppendChar() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String append = prefs.getString("append_ending_char", "4");
        return append ;
    }

    /**
     * 对扫描结果添加前缀后缀
     * @param data
     * @return
     */
    private String getfixChar(String data) {
        String result = "";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String prefix = prefs.getString("prefix_config", "");
        String suffix = prefs.getString("suffix_config", "");
//        String append = prefs.getString("append_ending_char", "");
//        Log.e("append_ending_char", "append = " + append) ;
        result = prefix + data + suffix ;
        return result;
    }


    //屏灭屏亮广播
    private BroadcastReceiver powerModeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction() ;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            boolean isOpen = prefs.getBoolean("switch_scan_service", false);
//            if (isOpen) {
//                if(Intent.ACTION_SCREEN_ON.equals(action)){//连接扫描头
//                    if (mDecoder == null) {
//                        mDecodeResult = new DecodeResult();
//                        mDecoder = new Decoder() ;
//                        try {
//                            mDecoder.connectDecoderLibrary();
//                            Log.e(TAG, "decoder init success");
//                            SetSymbologySettings();
//                        } catch (Exception e) {
//                            Log.e(TAG, "decoder init fail");
//                            mDecoder = null ;//连接扫描头失败
//                        }
//                    }
//                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {//关闭扫描头
//                    if (mDecoder != null) {
//                        try {
////                            mDecoder.stopScanning();
//                            mDecoder.disconnectDecoderLibrary();
//                            mDecoder = null ;
//                        } catch (DecoderException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//            if(scanConfig.isPowerScreen()){
//                //SCREEN ON ACTION
//                if(action.equals(Intent.ACTION_SCREEN_ON)){
////					Log.e("powerModeReceiver", "screent on +++ ") ;
//                    new Thread(initTask).start() ;
//                }
//                //SCREEN OFF ACTION
//                if(action.equals(Intent.ACTION_SCREEN_OFF)){
////					Log.e("powerModeReceiver", "screent off +++") ;
//                    if(scanDev != null){
//                        scanDev.close() ;
//                    }
//                }
//            }

        }
    };


    /**
     * Sets default preferences based on "HSMDecoderAPI" settings
     * @throws DecoderException
     *
     */
    @SuppressWarnings("deprecation")
    private void setSymbologyPreferences(boolean bDefault)// throws DecoderException
    {
        Log. d(TAG, "SetSymbologyPreferences++");

        SymbologyConfig symConfig = new SymbologyConfig(0);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int time = prefs.getInt("decode_time_limit", 3000);
        Toast.makeText(getApplicationContext(), time + "", Toast.LENGTH_LONG).show();
        SharedPreferences.Editor editor = prefs.edit();

        for(int i = 0; i < DecoderConfigValues.SymbologyID.SYM_ALL; i++)
        {

            symConfig.symID = i; // TODO: move me?

            try
            {
                if(bDefault)
                    mDecoder.getSymbologyConfigDefaults(symConfig);
                else
                    mDecoder.getSymbologyConfig(symConfig);
            }
            catch (DecoderException e) {
                // Exceptions are OK here since we are only "getting"
                Log.d(TAG, "SymId " + i + " " + e.getMessage());
            }

            switch(i)
            {
                case DecoderConfigValues.SymbologyID.SYM_AZTEC:
                    // enable, min, max
                    editor.putBoolean("sym_aztec_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putString("sym_aztec_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_aztec_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_CODABAR:
                    // enable, check enable, start/stop transmit, codabar concatenate, min, max
                    editor.putBoolean("sym_codabar_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putBoolean("sym_codabar_check_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_ENABLE) > 0 ? true : false);
                    editor.putBoolean("sym_codabar_start_stop_transmit_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT) > 0 ? true : false);
                    editor.putBoolean("sym_codabar_concatenate_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CODABAR_CONCATENATE) > 0 ? true : false);
                    editor.putString("sym_codabar_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_codabar_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_CODE11:
                    // enable, check enable, min, max
                    editor.putBoolean("sym_code11_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putBoolean("sym_code11_check_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_ENABLE) > 0 ? true : false);
                    editor.putString("sym_code11_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_code11_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_CODE128:
                    // enable, min, max
                    editor.putBoolean("sym_code128_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putString("sym_code128_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_code128_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_GS1_128:
                    // enable, min, max
                    editor.putBoolean("sym_gs1_128_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putString("sym_gs1_128_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_gs1_128_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_CODE39:
                    // enable, check enable, start/stop transmit, append, fullascii
                    editor.putBoolean("sym_code39_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putBoolean("sym_code39_check_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_ENABLE) > 0 ? true : false);
                    editor.putBoolean("sym_code39_start_stop_transmit_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT) > 0 ? true : false);
                    editor.putBoolean("sym_code39_append_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE_APPEND_MODE) > 0 ? true : false);
                    editor.putBoolean("sym_code39_fullascii_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE_FULLASCII) > 0 ? true : false);
                    editor.putString("sym_code39_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_code39_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_CODE49:
                case DecoderConfigValues.SymbologyID.SYM_GRIDMATRIX:
                case DecoderConfigValues.SymbologyID.SYM_PLESSEY:
                case DecoderConfigValues.SymbologyID.SYM_CODE16K:
                case DecoderConfigValues.SymbologyID.SYM_POSICODE:
                case DecoderConfigValues.SymbologyID.SYM_LABEL:
                    // not supported
                    break;
                case DecoderConfigValues.SymbologyID.SYM_CODE93:
                    // enable, min, max
                    editor.putBoolean("sym_code93_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putString("sym_code93_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_code93_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_COMPOSITE:
                    // enable, composite upc, min, max
                    editor.putBoolean("sym_composite_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putBoolean("sym_composite_upc_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_COMPOSITE_UPC) > 0 ? true : false);
                    editor.putString("sym_composite_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_composite_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_DATAMATRIX:
                    // enable, min, max
                    editor.putBoolean("sym_datamatrix_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putString("sym_datamatrix_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_datamatrix_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_EAN8:
                    // enable, check transmit, addenda separator, 2 digit addenda, 5 digit addenda, addenda required
                    editor.putBoolean("sym_ean8_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putBoolean("sym_ean8_check_transmit_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT) > 0 ? true : false);
                    editor.putBoolean("sym_ean8_addenda_separator_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ADDENDA_SEPARATOR) > 0 ? true : false);
                    editor.putBoolean("sym_ean8_2_digit_addenda_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA) > 0 ? true : false);
                    editor.putBoolean("sym_ean8_5_digit_addenda_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA) > 0 ? true : false);
                    editor.putBoolean("sym_ean8_addenda_required_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ADDENDA_REQUIRED) > 0 ? true : false);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_EAN13:
                    // enable, check transmit, addenda separator, 2 digit addenda, 5 digit addenda, addenda required
                    editor.putBoolean("sym_ean13_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putBoolean("sym_ean13_check_transmit_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT) > 0 ? true : false);
                    editor.putBoolean("sym_ean13_addenda_separator_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ADDENDA_SEPARATOR) > 0 ? true : false);
                    editor.putBoolean("sym_ean13_2_digit_addenda_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA) > 0 ? true : false);
                    editor.putBoolean("sym_ean13_5_digit_addenda_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA) > 0 ? true : false);
                    editor.putBoolean("sym_ean13_addenda_required_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ADDENDA_REQUIRED) > 0 ? true : false);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_INT25:
                    // enable, check enable, check transmit enable, min, max
                    editor.putBoolean("sym_int25_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putBoolean("sym_int25_check_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_ENABLE) > 0 ? true : false);
                    editor.putBoolean("sym_int25_check_transmit_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT) > 0 ? true : false);
                    editor.putString("sym_int25_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_int25_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_MAXICODE:
                    // enable, min, max
                    editor.putBoolean("sym_maxicode_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putString("sym_maxicode_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_maxicode_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_MICROPDF:
                    // enable, min, max
                    editor.putBoolean("sym_micropdf_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putString("sym_micropdf_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_micropdf_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_PDF417:
                    // enable, min, max
                    editor.putBoolean("sym_pdf417_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putString("sym_pdf417_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_pdf417_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_QR:
                    // enable, min, max
                    editor.putBoolean("sym_qr_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putString("sym_qr_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_qr_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_HANXIN:
                    // enable, min, max
                    editor.putBoolean("sym_hanxin_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putString("sym_hanxin_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_hanxin_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_RSS:
                    // rss enable, rsl enable, rse enable, min, max
                    editor.putBoolean("sym_rss_rss_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_RSS_ENABLE) > 0 ? true : false);
                    editor.putBoolean("sym_rss_rsl_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_RSL_ENABLE) > 0 ? true : false);
                    editor.putBoolean("sym_rss_rse_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_RSE_ENABLE) > 0 ? true : false);
                    editor.putString("sym_rss_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_rss_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_UPCA:
                    // enable, check transmit, sys num transmit, addenda separator, 2 digit addenda, 5 digit addenda, addenda required
                    editor.putBoolean("sym_upca_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putBoolean("sym_upca_check_transmit_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT) > 0 ? true : false);
                    editor.putBoolean("sym_upca_sys_num_transmit_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_NUM_SYS_TRANSMIT) > 0 ? true : false);
                    editor.putBoolean("sym_upca_addenda_separator_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ADDENDA_SEPARATOR) > 0 ? true : false);
                    editor.putBoolean("sym_upca_2_digit_addenda_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA) > 0 ? true : false);
                    editor.putBoolean("sym_upca_5_digit_addenda_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA) > 0 ? true : false);
                    editor.putBoolean("sym_upca_addenda_required_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ADDENDA_REQUIRED) > 0 ? true : false);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_UPCE1:
                    // upce1 enable
                    editor.putBoolean("sym_upce1_upce1_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_UPCE1_ENABLE) > 0 ? true : false);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_UPCE0:
                    // enable, upce expanded, char char transmit, num sys transmit, addenda separator, 2 digit addenda, 5 digit addenda, addenda required
                    editor.putBoolean("sym_upce0_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putBoolean("sym_upce0_upce_expanded_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_EXPANDED_UPCE) > 0 ? true : false);
                    editor.putBoolean("sym_upce0_check_transmit_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT) > 0 ? true : false);
                    editor.putBoolean("sym_upce0_sys_num_transmit_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_NUM_SYS_TRANSMIT) > 0 ? true : false);
                    editor.putBoolean("sym_upce0_addenda_separator_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ADDENDA_SEPARATOR) > 0 ? true : false);
                    editor.putBoolean("sym_upce0_2_digit_addenda_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA) > 0 ? true : false);
                    editor.putBoolean("sym_upce0_5_digit_addenda_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA) > 0 ? true : false);
                    editor.putBoolean("sym_upce0_addenda_required_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ADDENDA_REQUIRED) > 0 ? true : false);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_ISBT:
                    // enable
                    editor.putBoolean("sym_isbt_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_IATA25:
                    // enable, min, max
                    editor.putBoolean("sym_iata25_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putString("sym_iata25_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_iata25_max", Integer.toString(symConfig.MaxLength));
                case DecoderConfigValues.SymbologyID.SYM_CODABLOCK:
                    // enable, min, max
                    editor.putBoolean("sym_codablock_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putString("sym_codablock_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_codablock_max", Integer.toString(symConfig.MaxLength));
                    break;

				/* Post Symbology Config */
                case DecoderConfigValues.SymbologyID.SYM_POSTNET:
                    // check transmit
                    editor.putBoolean("sym_postnet_check_transmit_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT) > 0 ? true : false);
                case DecoderConfigValues.SymbologyID.SYM_JAPOST:
                case DecoderConfigValues.SymbologyID.SYM_PLANET:
                case DecoderConfigValues.SymbologyID.SYM_DUTCHPOST:
                case DecoderConfigValues.SymbologyID.SYM_US_POSTALS1:
                case DecoderConfigValues.SymbologyID.SYM_USPS4CB:
                case DecoderConfigValues.SymbologyID.SYM_IDTAG:
                case DecoderConfigValues.SymbologyID.SYM_BPO:
                case DecoderConfigValues.SymbologyID.SYM_CANPOST:
                case DecoderConfigValues.SymbologyID.SYM_AUSPOST:
                    // enable (config)
                    editor.putString("sym_post_config", "0"); // i know this is disabled (no_postals) by default - another way?

                    if(i == DecoderConfigValues.SymbologyID.SYM_AUSPOST)
                    {
                        // Default Bar Width & Interpret Mode (both off)
                        editor.putBoolean("sym_auspost_bar_output_enable", false);
                        editor.putString("sym_aus_interpret_mode", "0");
                    }

                    break;
				/* ===================== */

                case DecoderConfigValues.SymbologyID.SYM_MSI:
                    // enable, check enable, min, max
                    editor.putBoolean("sym_msi_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putBoolean("sym_msi_check_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_ENABLE) > 0 ? true : false);
                    editor.putString("sym_msi_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_msi_max", Integer.toString(symConfig.MaxLength));
                case DecoderConfigValues.SymbologyID.SYM_TLCODE39:
                    // enable
                    editor.putBoolean("sym_tlcode39_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_MATRIX25:
                    // enable, min, max
                    editor.putBoolean("sym_matrix25_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putString("sym_matrix25_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_matrix25_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_KOREAPOST:
                    // enable, min, max
                    editor.putBoolean("sym_koreapost_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putString("sym_koreapost_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_koreapost_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_TRIOPTIC:
                    // enable
                    editor.putBoolean("sym_trioptic_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_CODE32:
                    // enable
                    editor.putBoolean("sym_code32_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_STRT25:
                    // enable, min, max
                    editor.putBoolean("sym_strt25_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putString("sym_strt25_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_strt25_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_CHINAPOST:
                    // enable, min, max
                    editor.putBoolean("sym_chinapost_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putString("sym_chinapost_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_chinapost_max", Integer.toString(symConfig.MaxLength));
                case DecoderConfigValues.SymbologyID.SYM_TELEPEN:
                    // enable, telepen old style, min, max
                    editor.putBoolean("sym_telepen_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    editor.putBoolean("sym_telepen_telepen_old_style_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_TELEPEN_OLD_STYLE) > 0 ? true : false);
                    editor.putString("sym_telepen_min", Integer.toString(symConfig.MinLength));
                    editor.putString("sym_telepen_max", Integer.toString(symConfig.MaxLength));
                    break;
                case DecoderConfigValues.SymbologyID.SYM_COUPONCODE:
                    // enable
                    editor.putBoolean("sym_couponcode_enable", (symConfig.Flags & DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE) > 0 ? true : false);
                    break;
                default:
                    break;
            }

            editor.commit();
        }

        // OCR Config (disabled, user, 13777777770)
		/*
		editor.putBoolean("sym_ocr_enable", false);
		editor.putString("sym_ocr_mode_config", Integer.toString(OCRMode.OCR_OFF));
		editor.putString("sym_ocr_template_config", Integer.toString(OCRTemplate.USER));
		editor.putString("sym_ocr_user_template", "13777777770");
		editor.commit();
		*/
        Log. d(TAG, "SetSymbologyPreferences--");
    }


    /**
     * Sets the symbology settings based on user preferences
     * @throws DecoderException
     *
     */
    @SuppressWarnings("deprecation")
    void SetSymbologySettings() //throws DecoderException
    {
        Log. d(TAG, "SetSymbologySettings++");

        int flags = 0;											// flags config
        int min = 0;											// minimum length config
        int max = 0;											// maximum length config
        int postal_config = 0;									// postal config
        String temp;											// temp string for converting string to int
        SymbologyConfig symConfig = new SymbologyConfig(0);		// symbology config
        int min_default, max_default;
        String strMinDefault = null;
        String strMaxDefault = null;
        boolean bNotSupported = false;

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        for(int i = 0; i < DecoderConfigValues.SymbologyID.SYM_ALL; i++)
        {

            symConfig.symID = i;								// symID
            //if( i != SymbologyID.SYM_OCR &&
            //	i != SymbologyID.SYM_POSTALS )
            //m_Decoder.getSymbologyConfig(symConfig,false); // gets the current symConfig
            flags = 0;											// reset the flags

            // Set appropriate sym config mask...
            switch(i)
            {
                // Flag & Range:
                case DecoderConfigValues.SymbologyID.SYM_AZTEC:
                case DecoderConfigValues.SymbologyID.SYM_CODABAR:
                case DecoderConfigValues.SymbologyID.SYM_CODE11:
                case DecoderConfigValues.SymbologyID.SYM_CODE128:
                case DecoderConfigValues.SymbologyID.SYM_GS1_128:
                case DecoderConfigValues.SymbologyID.SYM_CODE39:
                    //case SymbologyID.SYM_CODE49: 		// not supported
                case DecoderConfigValues.SymbologyID.SYM_CODE93:
                case DecoderConfigValues.SymbologyID.SYM_COMPOSITE:
                case DecoderConfigValues.SymbologyID.SYM_DATAMATRIX:
                case DecoderConfigValues.SymbologyID.SYM_INT25:
                case DecoderConfigValues.SymbologyID.SYM_MAXICODE:
                case DecoderConfigValues.SymbologyID.SYM_MICROPDF:
                case DecoderConfigValues.SymbologyID.SYM_PDF417:
                case DecoderConfigValues.SymbologyID.SYM_QR:
                case DecoderConfigValues.SymbologyID.SYM_RSS:
                case DecoderConfigValues.SymbologyID.SYM_IATA25:
                case DecoderConfigValues.SymbologyID.SYM_CODABLOCK:
                case DecoderConfigValues.SymbologyID.SYM_MSI:
                case DecoderConfigValues.SymbologyID.SYM_MATRIX25:
                case DecoderConfigValues.SymbologyID.SYM_KOREAPOST:
                case DecoderConfigValues.SymbologyID.SYM_STRT25:
                    //case SymbologyID.SYM_PLESSEY: 	// not supported
                case DecoderConfigValues.SymbologyID.SYM_CHINAPOST:
                case DecoderConfigValues.SymbologyID.SYM_TELEPEN:
                    //case SymbologyID.SYM_CODE16K: 	// not supported
                    //case SymbologyID.SYM_POSICODE:	// not supported
                case DecoderConfigValues.SymbologyID.SYM_HANXIN:
                    //case SymbologyID.SYM_GRIDMATRIX:	// not supported
                    try
                    {
                        mDecoder.getSymbologyConfig(symConfig); // gets the current symConfig
                        min_default = mDecoder.getSymbologyMinRange(i); strMinDefault = Integer.toString(min_default);
                        max_default = mDecoder.getSymbologyMaxRange(i); strMaxDefault = Integer.toString(max_default);
                    }
                    catch(DecoderException e)
                    {
//                        HandleDecoderException(e);
                    }
                    symConfig.Mask = DecoderConfigValues.SymbologyFlags.SYM_MASK_FLAGS | DecoderConfigValues.SymbologyFlags.SYM_MASK_MIN_LEN | DecoderConfigValues.SymbologyFlags.SYM_MASK_MAX_LEN;
                    break;
                // Flags Only:
                case DecoderConfigValues.SymbologyID.SYM_EAN8:
                case DecoderConfigValues.SymbologyID.SYM_EAN13:
                case DecoderConfigValues.SymbologyID.SYM_POSTNET:
                case DecoderConfigValues.SymbologyID.SYM_UPCA:
                case DecoderConfigValues.SymbologyID.SYM_UPCE0:
                case DecoderConfigValues.SymbologyID.SYM_UPCE1:
                case DecoderConfigValues.SymbologyID.SYM_ISBT:
                case DecoderConfigValues.SymbologyID.SYM_BPO:
                case DecoderConfigValues.SymbologyID.SYM_CANPOST:
                case DecoderConfigValues.SymbologyID.SYM_AUSPOST:
                case DecoderConfigValues.SymbologyID.SYM_JAPOST:
                case DecoderConfigValues.SymbologyID.SYM_PLANET:
                case DecoderConfigValues.SymbologyID.SYM_DUTCHPOST:
                case DecoderConfigValues.SymbologyID.SYM_TLCODE39:
                case DecoderConfigValues.SymbologyID.SYM_TRIOPTIC:
                case DecoderConfigValues.SymbologyID.SYM_CODE32:
                case DecoderConfigValues.SymbologyID.SYM_COUPONCODE:
                case DecoderConfigValues.SymbologyID.SYM_USPS4CB:
                case DecoderConfigValues.SymbologyID.SYM_IDTAG:
                    //case SymbologyID.SYM_LABEL:		// not supported
                case DecoderConfigValues.SymbologyID.SYM_US_POSTALS1:
                    try
                    {
//                        Log.e("tt", i + ", flags = " + symConfig.Flags +",mask =  " + symConfig.Mask ) ;
                        mDecoder.getSymbologyConfig(symConfig); // gets the current symConfig
                    }
                    catch(DecoderException e)
                    {
//                        HandleDecoderException(e);
                    }
                    symConfig.Mask = DecoderConfigValues.SymbologyFlags.SYM_MASK_FLAGS;
                    break;
                // default:
                default:
                    // invalid / not supported
                    bNotSupported = true;
                    break;
            }

            // Set symbology config...
            switch(i)
            {
                case DecoderConfigValues.SymbologyID.SYM_AZTEC:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_aztec_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_aztec_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_aztec_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_CODABAR:
                    // enable, check char, start/stop transmit, codabar concatenate
                    flags |= sharedPrefs.getBoolean("sym_codabar_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    flags |= sharedPrefs.getBoolean("sym_codabar_check_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0;
                    flags |= sharedPrefs.getBoolean("sym_codabar_start_stop_transmit_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_START_STOP_XMIT : 0;
                    flags |= sharedPrefs.getBoolean("sym_codabar_concatenate_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CODABAR_CONCATENATE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_codabar_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_codabar_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_CODE11:
                    // enable, check char
                    flags |= sharedPrefs.getBoolean("sym_code11_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    flags |= sharedPrefs.getBoolean("sym_code11_check_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_code11_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_code11_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_CODE128:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_code128_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_code128_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_code128_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_GS1_128:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_gs1_128_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_gs1_128_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_gs1_128_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_CODE39:
                    // enable, check char, start/stop transmit, append, full ascii
                    flags |= sharedPrefs.getBoolean("sym_code39_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    flags |= sharedPrefs.getBoolean("sym_code39_check_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0;
                    flags |= sharedPrefs.getBoolean("sym_code39_start_stop_transmit_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_START_STOP_XMIT : 0;
                    flags |= sharedPrefs.getBoolean("sym_code39_append_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE_APPEND_MODE : 0;
                    flags |= sharedPrefs.getBoolean("sym_code39_fullascii_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE_FULLASCII : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_code39_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_code39_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_CODE49:
                case DecoderConfigValues.SymbologyID.SYM_GRIDMATRIX:
                case DecoderConfigValues.SymbologyID.SYM_PLESSEY:
                case DecoderConfigValues.SymbologyID.SYM_CODE16K:
                case DecoderConfigValues.SymbologyID.SYM_POSICODE:
                case DecoderConfigValues.SymbologyID.SYM_LABEL:
                    // not supported
                    break;
                case DecoderConfigValues.SymbologyID.SYM_CODE93:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_code93_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_code93_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_code93_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_COMPOSITE:
                    // enable, composit upc
                    flags |= sharedPrefs.getBoolean("sym_composite_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    flags |= sharedPrefs.getBoolean("sym_composite_upc_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_COMPOSITE_UPC : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_composite_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_composite_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_DATAMATRIX:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_datamatrix_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_datamatrix_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_datamatrix_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_EAN8:
                    // enable, check char transmit, addenda separator, 2 digit addena, 5 digit addena, addena required
                    flags |= sharedPrefs.getBoolean("sym_ean8_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    flags |= sharedPrefs.getBoolean("sym_ean8_check_transmit_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT : 0;
                    flags |= sharedPrefs.getBoolean("sym_ean8_addenda_separator_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ADDENDA_SEPARATOR : 0;
                    flags |= sharedPrefs.getBoolean("sym_ean8_2_digit_addenda_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA: 0;
                    flags |= sharedPrefs.getBoolean("sym_ean8_5_digit_addenda_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA : 0;
                    flags |= sharedPrefs.getBoolean("sym_ean8_addenda_required_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ADDENDA_REQUIRED : 0;
                    break;
                case DecoderConfigValues.SymbologyID.SYM_EAN13:
                    // enable, check char transmit, addenda separator, 2 digit addena, 5 digit addena, addena required
                    flags |= sharedPrefs.getBoolean("sym_ean13_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    flags |= sharedPrefs.getBoolean("sym_ean13_check_transmit_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT : 0;
                    flags |= sharedPrefs.getBoolean("sym_ean13_addenda_separator_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ADDENDA_SEPARATOR : 0;
                    flags |= sharedPrefs.getBoolean("sym_ean13_2_digit_addenda_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA: 0;
                    flags |= sharedPrefs.getBoolean("sym_ean13_5_digit_addenda_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA : 0;
                    flags |= sharedPrefs.getBoolean("sym_ean13_addenda_required_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ADDENDA_REQUIRED : 0;
                    break;
                case DecoderConfigValues.SymbologyID.SYM_INT25:
                    // enable, check enable, check transmit
                    flags |= sharedPrefs.getBoolean("sym_int25_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    flags |= sharedPrefs.getBoolean("sym_int25_check_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0;
                    flags |= sharedPrefs.getBoolean("sym_int25_check_transmit_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_int25_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_int25_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_MAXICODE:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_maxicode_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_maxicode_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_maxicode_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_MICROPDF:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_micropdf_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_micropdf_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_micropdf_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_PDF417:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_pdf417_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_pdf417_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_pdf417_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_QR:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_qr_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_qr_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_qr_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_HANXIN:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_hanxin_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_hanxin_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_hanxin_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_RSS:
                    // rss enable, rsl enable, rse enable
                    flags |= sharedPrefs.getBoolean("sym_rss_rss_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_RSS_ENABLE : 0;
                    flags |= sharedPrefs.getBoolean("sym_rss_rsl_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_RSL_ENABLE : 0;
                    flags |= sharedPrefs.getBoolean("sym_rss_rse_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_RSE_ENABLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_rss_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_rss_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_UPCA:
                    // enable, check transmit, sys num transmit, addenda separator, 2 digit addenda, 5 digit addenda, addenda required
                    flags |= sharedPrefs.getBoolean("sym_upca_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    flags |= sharedPrefs.getBoolean("sym_upca_check_transmit_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0;
                    flags |= sharedPrefs.getBoolean("sym_upca_sys_num_transmit_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_NUM_SYS_TRANSMIT : 0;
                    flags |= sharedPrefs.getBoolean("sym_upca_addenda_separator_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ADDENDA_SEPARATOR : 0;
                    flags |= sharedPrefs.getBoolean("sym_upca_2_digit_addenda_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA : 0;
                    flags |= sharedPrefs.getBoolean("sym_upca_5_digit_addenda_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA : 0;
                    flags |= sharedPrefs.getBoolean("sym_upca_addenda_required_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ADDENDA_REQUIRED : 0;
                    break;
                case DecoderConfigValues.SymbologyID.SYM_UPCE1:
                    // upce1 enable
                    flags |= sharedPrefs.getBoolean("sym_upce1_upce1_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_UPCE1_ENABLE : 0;
                    break;
                case DecoderConfigValues.SymbologyID.SYM_UPCE0:
                    // enable, upce expanded, char char transmit, num sys transmit, addenda separator, 2 digit addenda, 5 digit addenda, addenda required
                    flags |= sharedPrefs.getBoolean("sym_upce0_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    flags |= sharedPrefs.getBoolean("sym_upce0_upce_expanded_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_EXPANDED_UPCE : 0;
                    flags |= sharedPrefs.getBoolean("sym_upce0_check_transmit_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT : 0;
                    flags |= sharedPrefs.getBoolean("sym_upce0_sys_num_transmit_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_NUM_SYS_TRANSMIT : 0;
                    flags |= sharedPrefs.getBoolean("sym_upce0_addenda_separator_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ADDENDA_SEPARATOR : 0;
                    flags |= sharedPrefs.getBoolean("sym_upce0_2_digit_addenda_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA : 0;
                    flags |= sharedPrefs.getBoolean("sym_upce0_5_digit_addenda_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA : 0;
                    flags |= sharedPrefs.getBoolean("sym_upce0_addenda_required_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ADDENDA_REQUIRED : 0;
                    break;
                case DecoderConfigValues.SymbologyID.SYM_ISBT:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_isbt_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    break;
                case DecoderConfigValues.SymbologyID.SYM_IATA25:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_iata25_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_iata25_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_iata25_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_CODABLOCK:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_codablock_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_codablock_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_codablock_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;

				/* Post Symbology Config */
                case DecoderConfigValues.SymbologyID.SYM_POSTNET:
                    Log. d(TAG, "Configure SYM_POSTNET");
                    // enable
                    temp = sharedPrefs.getString("sym_post_config", "0"); postal_config = Integer.parseInt(temp);
                    flags |= (postal_config == POSTNET) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    Log. d(TAG, "SYM_POSTNET postal_config = " + postal_config);
                    Log. d(TAG, "SYM_POSTNET flags = " + flags);
                    // check transmit
                    flags |= sharedPrefs.getBoolean("sym_postnet_check_transmit_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT : 0;
                    break;
                case DecoderConfigValues.SymbologyID.SYM_JAPOST:
                    // enable
                    temp = sharedPrefs.getString("sym_post_config", "0"); postal_config = Integer.parseInt(temp);
                    flags |= (postal_config == JAPAN_POST) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    break;
                case DecoderConfigValues.SymbologyID.SYM_PLANET:
                    // enable
                    temp = sharedPrefs.getString("sym_post_config", "0"); postal_config = Integer.parseInt(temp);
                    flags |= (postal_config == PLANETCODE) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    break;
                case DecoderConfigValues.SymbologyID.SYM_DUTCHPOST:
                    // enable
                    temp = sharedPrefs.getString("sym_post_config", "0"); postal_config = Integer.parseInt(temp);
                    flags |= (postal_config == KIX) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    break;
                case DecoderConfigValues.SymbologyID.SYM_US_POSTALS1:
                    // enable
                    temp = sharedPrefs.getString("sym_post_config", "0"); postal_config = Integer.parseInt(temp);
                    flags |= (postal_config == US_POSTALS) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    break;
                case DecoderConfigValues.SymbologyID.SYM_USPS4CB:
                    // enable
                    temp = sharedPrefs.getString("sym_post_config", "0"); postal_config = Integer.parseInt(temp);
                    flags |= (postal_config == USPS_4_STATE) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    break;
                case DecoderConfigValues.SymbologyID.SYM_IDTAG:
                    // enable
                    temp = sharedPrefs.getString("sym_post_config", "0"); postal_config = Integer.parseInt(temp);
                    flags |= (postal_config == UPU_4_STATE) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    break;
                case DecoderConfigValues.SymbologyID.SYM_BPO:
                    // enable
                    temp = sharedPrefs.getString("sym_post_config", "0"); postal_config = Integer.parseInt(temp);
                    flags |= (postal_config == ROYAL_MAIL) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    break;
                case DecoderConfigValues.SymbologyID.SYM_CANPOST:
                    // enable
                    temp = sharedPrefs.getString("sym_post_config", "0"); postal_config = Integer.parseInt(temp);
                    flags |= (postal_config == CANADIAN) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    break;
                case DecoderConfigValues.SymbologyID.SYM_AUSPOST:
                    // enable
                    temp = sharedPrefs.getString("sym_post_config", "0"); postal_config = Integer.parseInt(temp);
                    flags |= (postal_config == AUS_POST) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    // Bar output
                    sharedPrefs.getBoolean("sym_auspost_bar_output_enable", false);
                    // Interpret Mode
                    temp = sharedPrefs.getString("sym_aus_interpret_mode","0"); postal_config = Integer.parseInt(temp);
                    switch(postal_config)
                    {
                        // Numeric N Table:
                        case 1:
                            flags |= DecoderConfigValues.SymbologyFlags.SYMBOLOGY_AUS_POST_NUMERIC_N_TABLE;
                            break;
                        // Alphanumeric C Table:
                        case 2:
                            flags |= DecoderConfigValues.SymbologyFlags.SYMBOLOGY_AUS_POST_ALPHANUMERIC_C_TABLE;
                            break;
                        // Combination N & C Tables:
                        case 3:
                            flags |= DecoderConfigValues.SymbologyFlags.SYMBOLOGY_AUS_POST_COMBINATION_N_AND_C_TABLES;
                            break;
                        default:
                            break;
                    }
                    break;
				/* ===================== */

                case DecoderConfigValues.SymbologyID.SYM_MSI:
                    // enable, check transmit
                    flags |= sharedPrefs.getBoolean("sym_msi_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    flags |= sharedPrefs.getBoolean("sym_msi_check_transmit_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT : 0;
                    Log.d(TAG, "sym msi flags = " + flags);
                    // min, max
                    temp = sharedPrefs.getString("sym_msi_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_msi_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_TLCODE39:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_tlcode39_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    break;
                case DecoderConfigValues.SymbologyID.SYM_MATRIX25:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_matrix25_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_matrix25_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_matrix25_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_KOREAPOST:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_koreapost_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_koreapost_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_koreapost_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_TRIOPTIC:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_trioptic_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    break;
                case DecoderConfigValues.SymbologyID.SYM_CODE32:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_code32_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    break;
                case DecoderConfigValues.SymbologyID.SYM_STRT25:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_strt25_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_strt25_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_strt25_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_CHINAPOST:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_chinapost_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_chinapost_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_chinapost_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_TELEPEN:
                    // enable, telepen old style
                    flags |= sharedPrefs.getBoolean("sym_telepen_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    flags |= sharedPrefs.getBoolean("sym_telepen_telepen_old_style_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_TELEPEN_OLD_STYLE : 0;
                    // min, max
                    temp = sharedPrefs.getString("sym_telepen_min", strMinDefault); min = Integer.parseInt(temp);
                    temp = sharedPrefs.getString("sym_telepen_max", strMaxDefault); max = Integer.parseInt(temp);
                    break;
                case DecoderConfigValues.SymbologyID.SYM_COUPONCODE:
                    // enable
                    flags |= sharedPrefs.getBoolean("sym_couponcode_enable", false) ? DecoderConfigValues.SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                    break;

                default:
                    symConfig.Mask = 0; // will not setSymbologyConfig
                    break;

            }


            if(bNotSupported)
            {
                bNotSupported = false; // // do nothing, but reset flag
            }
            if(symConfig.Mask == (DecoderConfigValues.SymbologyFlags.SYM_MASK_FLAGS | DecoderConfigValues.SymbologyFlags.SYM_MASK_MIN_LEN | DecoderConfigValues.SymbologyFlags.SYM_MASK_MAX_LEN) ) // Flags & Range
            {
                symConfig.Flags = flags;
                symConfig.MinLength = min;
                symConfig.MaxLength = max;
                try
                {
//                    Log.e("tt--", i + ", flags = " + symConfig.Flags +",mask =  " + symConfig.Mask ) ;
                    mDecoder.setSymbologyConfig(symConfig);
//					symConfig.
                }
                catch (DecoderException e)
                {
                    Log. d(TAG, "1 EXCEPTION SYMID = " + i);
//                    HandleDecoderException(e);
                }
            }
            else if(symConfig.Mask == (DecoderConfigValues.SymbologyFlags.SYM_MASK_FLAGS)) // Flag Only
            {
                symConfig.Flags = flags;
                try
                {
                    //if(symConfig.symID == 16)
//                    Log.e("tt==", i + ", flags = " + symConfig.Flags +",mask =  " + symConfig.Mask + ",symID = " + symConfig.symID) ;
                    mDecoder.setSymbologyConfig(symConfig);
                }
                catch (DecoderException e)
                {
                    Log. d(TAG, "2 EXCEPTION SYMID = " + i);
//                    HandleDecoderException(e);
                }
            }
            else
            {
                // invalid
            }
        }

        Log. d(TAG, "SetSymbologySettings--");
    }



    /**
     * Sets the Scanning settings based on user preferences
     * @throws DecoderException
     * @throws NumberFormatException
     *
     */
    void SetScanningSettings() throws NumberFormatException, DecoderException
    {
        Log. d(TAG, "SetScanningSettings++");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int myLightsMode = DecoderConfigValues.LightsMode.ILLUM_AIM_ON;

        Log. d(TAG, "myLightsMode = " + myLightsMode);

		/* Lights Mode */
        String lightsModeString = prefs.getString("lightsConfig", "3");
        myLightsMode = Integer.parseInt(lightsModeString);
        Log.e("SetScanningSettings", "setLightsMode-" + myLightsMode);
        mDecoder.setLightsMode(myLightsMode);

//        g_bContinuousScanEnabled = prefs.getBoolean("continous_scan_enable", false);

        Log. d(TAG, "SetScanningSettings--");
    }

}
