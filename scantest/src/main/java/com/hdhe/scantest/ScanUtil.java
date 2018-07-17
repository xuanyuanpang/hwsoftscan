package com.hdhe.scantest;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2018/4/19.
 */

public class ScanUtil {

    private final String ACTION_SCAN_INIT = "com.rfid.SCAN_INIT"  ;
    private final String ACTION_SCAN_CMD = "com.rfid.SCAN_CMD"  ;
    public static final String ACTION_CLOSE_SCAN = "com.rfid.CLOSE_SCAN"; //ACTION_SCAN_CMD

    public static final String ACTION_SET_SCAN_MODE = "com.rfid.SET_SCAN_MODE"; //ACTION_SCAN_CMD

    public static final String ACTION_ENABLE_SYM = "com.rfid.ENABLE_SYM"; //ACTION_SCAN_CMD


    private Context context ;
    public ScanUtil(Context context) {
        this.context = context ;
        Intent intent = new Intent();
        intent.setAction(ACTION_SCAN_INIT);
        context.sendBroadcast(intent);
    }

    /**
     * start scan
     */
    public void scan() {
        Intent intent = new Intent();
        intent.setAction(ACTION_SCAN_CMD);
        context.sendBroadcast(intent);
    }

    public void setScanMode(int mode) {
        Intent intent = new Intent();
        intent.setAction(ACTION_SET_SCAN_MODE);
        intent.putExtra("mode", mode);
        context.sendBroadcast(intent);
    }

    public void setScanSym(String symValue, boolean isEnable) {
        Intent setScanSym = new Intent();
        setScanSym.setAction(ACTION_ENABLE_SYM);
        setScanSym.putExtra("symbology", symValue);
        setScanSym.putExtra("enable", isEnable);
        context.sendBroadcast(setScanSym);
    }


    public void close() {
        Intent toKillService = new Intent();
        toKillService.setAction(ACTION_CLOSE_SCAN);
        context.sendBroadcast(toKillService);
    }

    public final class SymbologyValues{
        public static final String SYM_AZTEC = "sym_aztec_enable";
        public static final String SYM_CODABAR = "sym_codabar_enable";
        public static final String SYM_CODABLOCK = "sym_codablock_enable";
        public static final String SYM_CODE11 = "sym_code11_enable";
        public static final String SYM_CODE128 = "sym_code128_enable";
        public static final String SYM_CODE32 = "sym_code32_enable";
        public static final String SYM_CODE39 = "sym_code39_enable";
        public static final String SYM_CODE49 = "sym_code49_enable";
        public static final String SYM_CODE93 = "sym_code93_enable";
        public static final String SYM_COMPOSITE = "sym_composite_enable";
        public static final String SYM_COUPONCODE = "sym_couponcode_enable";
        public static final String SYM_DATAMATRIX = "sym_datamatrix_enable";
        public static final String SYM_EAN8 = "sym_ean8_enable";
        public static final String SYM_EAN13 = "sym_ean13_enable";
        public static final String SYM_GS1_128 = "sym_gs1_128_enable";
        public static final String SYM_HANXIN = "sym_hanxin_enable";
        public static final String SYM_IATA25 = "sym_iata25_enable";
        public static final String SYM_INT25 = "sym_int25_enable";
        public static final String SYM_ISBT = "sym_isbt_enable";
        public static final String SYM_MATRIX25 = "sym_matrix25_enable";
        public static final String SYM_MAXICODE = "sym_maxicode_enable";
        public static final String SYM_MICROPDF = "sym_micropdf_enable";
        public static final String SYM_MSI = "sym_msi_enable";
        public static final String SYM_PDF417 = "sym_pdf417_enable";
        public static final String SYM_QR = "sym_qr_enable";
        public static final String SYM_RSS = "sym_rss_rss_enable";
        public static final String SYM_STRT25 = "sym_strt25_enable";
        public static final String SYM_TELEPEN = "sym_telepen_enable";
        public static final String SYM_TLCODE39 = "sym_tlcode39_enable";
        public static final String SYM_TRIOPTIC = "sym_trioptic_enable";
        public static final String SYM_UPCA = "sym_upca_enable";
        public static final String SYM_UPCE0 = "sym_upce0_enable";
        public static final String SYM_UPCE1 = "sym_upce1_upce1_enable";

    }
}
