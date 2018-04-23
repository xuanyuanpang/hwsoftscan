package com.rfid.hwsoftscan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * 软解配置界面
 */

public class ConfigActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    private SwitchPreference swichScanService ;
    private final String SWITCH_SCAN_SERVICE = "switch_scan_service" ;
    private final String CATEGORY_SYM_CONFIG = "category_sym_config" ;
    private final String SYM_AZTEC_ENABLE = "sym_aztec_enable" ;
    private final String SYM_CODABAR_ENABLE = "sym_codabar_enable" ;
    private final String SYM_CODABLOCK_ENABLE = "sym_codablock_enable" ;
    private final String SYM_CODE11_ENABLE = "sym_code11_enable" ;
    private final String SYM_CODE128_ENABLE = "sym_code128_enable" ;
    private final String SYM_CODE32_ENABLE = "sym_code32_enable" ;
    private final String SYM_CODE39_ENABLE = "sym_code39_enable" ;
    private final String SYM_CODE49_ENABLE = "sym_code49_enable" ;
    private final String SYM_CODE93_ENABLE = "sym_code93_enable" ;
    private final String SYM_COMPOSITE_ENABLE = "sym_composite_enable" ;
    private final String SYM_COUPONCODE_ENABLE = "sym_couponcode_enable" ;
    private final String SYM_DATAMATRIX_ENABLE = "sym_datamatrix_enable" ;
    private final String SYM_EAN8_ENABLE = "sym_ean8_enable" ;
    private final String SYM_EAN13_ENABLE = "sym_ean13_enable" ;
    private final String SYM_GS1_128_ENABLE = "sym_gs1_128_enable" ;
    private final String SYM_HANXIN_ENABLE = "sym_hanxin_enable" ;
    private final String SYM_IATA25_ENABLE = "sym_iata25_enable" ;
    private final String SYM_INT25_ENABLE = "sym_int25_enable" ;
    private final String SYM_ISBT_ENABLE = "sym_isbt_enable" ;
    private final String SYM_MATRIX25_ENABLE = "sym_matrix25_enable" ;
    private final String SYM_MAXICODE_ENABLE = "sym_maxicode_enable" ;
    private final String SYM_MICROPDF_ENABLE = "sym_micropdf_enable" ;
    private final String SYM_MSI_ENABLE = "sym_msi_enable" ;
    private final String SYM_PDF417_ENABLE = "sym_pdf417_enable" ;
    private final String SYM_QR_ENABLE = "sym_qr_enable" ;
    private final String SYM_RSS_RSS_ENABLE = "sym_rss_rss_enable" ;
    private final String SYM_STRT25_ENABLE = "sym_strt25_enable" ;
    private final String SYM_TELEPEN_ENABLE = "sym_telepen_enable" ;
    private final String SYM_TLCODE39_ENABLE = "sym_tlcode39_enable" ;
    private final String SYM_TRIOPTIC_ENABLE = "sym_trioptic_enable" ;
    private final String SYM_UPCA_ENABLE = "sym_upca_enable" ;
    private final String SYM_UPCE0_ENABLE = "sym_upce0_enable" ;
    private final String SYM_UPCE1_UPCE1_ENABLE = "sym_upce1_upce1_enable" ;
    private final String CATEGORY_SYM_ADVANCED_SYM_SETUP = "category_sym_advanced_sym_setup" ;
    private final String SYM_CHINAPOST_ENABLE = "sym_chinapost_enable" ;
    private final String SYM_KOREAPOST_ENABLE = "sym_koreapost_enable" ;
    private final String SYM_POST_CONFIG = "sym_post_config" ;
    private final String SYM_OCR_ENABLE = "sym_ocr_enable" ;
    private final String SYM_OCR_MODE_CONFIG = "sym_ocr_mode_config" ;
    private final String SYM_OCR_TEMPLATE_CONFIG = "sym_ocr_template_config" ;
    private final String SYM_OCR_USER_TEMPLATE = "sym_ocr_user_template" ;


//    private final String SYM_QR_ENABLE = "sym_qr_enable" ;
//    private final String SYM_QR_ENABLE = "sym_qr_enable" ;
//    private final String SYM_QR_ENABLE = "sym_qr_enable" ;
//    private final String SYM_QR_ENABLE = "sym_qr_enable" ;
//    private final String SYM_QR_ENABLE = "sym_qr_enable" ;
//    private final String SYM_QR_ENABLE = "sym_qr_enable" ;
//    private final String SYM_QR_ENABLE = "sym_qr_enable" ;
//    private final String SYM_QR_ENABLE = "sym_qr_enable" ;
//    private final String SYM_QR_ENABLE = "sym_qr_enable" ;

    //time out settings

    private final String DECODE_TIME_LIMIT = "decode_time_limit" ;
    private ListPreference listPreferenceTimeout  ;

    private final String LIGHTS_CONFIG = "lightsConfig" ;
    private ListPreference listPreferenceLights  ;

    private final String INPUT_CONFIG = "inputConfig" ;
    private ListPreference listPreferenceInput  ;

    private final String CATEGORY_SCANNING = "category_scanning";

    private final String CATEGORY_SCAN_TIME = "category_scan_time";

    private final String APPEND_ENDING_CHAR = "append_ending_char" ;//追加结束符
    private ListPreference listPreferenceAppend  ;

    private PreferenceScreen mPreference ;
    private boolean isboot ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Material_Light_DarkActionBar);
        addPreferencesFromResource(R.xml.configuration_settings);
        isboot = getIntent().getBooleanExtra("isboot", false);
        mPreference = getPreferenceScreen() ;
        swichScanService = (SwitchPreference) findPreference(SWITCH_SCAN_SERVICE);
        swichScanService.setOnPreferenceChangeListener(this);
        listPreferenceTimeout = (ListPreference) findPreference(DECODE_TIME_LIMIT);
        listPreferenceLights = (ListPreference) findPreference(LIGHTS_CONFIG);
        listPreferenceInput = (ListPreference) findPreference(INPUT_CONFIG);
        listPreferenceAppend = (ListPreference) findPreference(APPEND_ENDING_CHAR);
        //初始超时设置
        String valueTimeout = listPreferenceTimeout.getValue();
        CharSequence[] entriesTimeout = listPreferenceTimeout.getEntries() ;
        listPreferenceTimeout.setSummary(entriesTimeout[listPreferenceTimeout.findIndexOfValue(valueTimeout)]);
        listPreferenceTimeout.setOnPreferenceChangeListener(this) ;
        //灯光设置
        String valueLight = listPreferenceLights.getValue();
        CharSequence[] entriesLight = listPreferenceLights.getEntries() ;
        listPreferenceLights.setSummary(entriesLight[listPreferenceLights.findIndexOfValue(valueLight)]);
        listPreferenceLights.setOnPreferenceChangeListener(this) ;
        //输入模式设置
        String valueInput = listPreferenceInput.getValue();
        CharSequence[] entriesInput = listPreferenceInput.getEntries() ;
        listPreferenceInput.setSummary(entriesInput[listPreferenceInput.findIndexOfValue(valueInput)]);
        listPreferenceInput.setOnPreferenceChangeListener(this) ;
        //追加结束符
        String valueAppend = listPreferenceAppend.getValue();
        CharSequence[] entriesAppend = listPreferenceAppend.getEntries() ;
        listPreferenceAppend.setSummary(entriesAppend[listPreferenceAppend.findIndexOfValue(valueAppend)]);
        listPreferenceAppend.setOnPreferenceChangeListener(this) ;

        findPreference(SYM_AZTEC_ENABLE).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_CODABAR_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_CODABLOCK_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_CODE11_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_CODE128_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_CODE32_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_CODE39_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_CODE49_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_CODE93_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_COMPOSITE_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_COUPONCODE_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_DATAMATRIX_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_EAN8_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_EAN13_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_GS1_128_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_HANXIN_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_IATA25_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_INT25_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_ISBT_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_MATRIX25_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_MAXICODE_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_MICROPDF_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_MSI_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_PDF417_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_QR_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_RSS_RSS_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_STRT25_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_TELEPEN_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_TLCODE39_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_TRIOPTIC_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_UPCA_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_UPCE0_ENABLE ).setOnPreferenceChangeListener(this) ;
        findPreference(SYM_UPCE1_UPCE1_ENABLE ).setOnPreferenceChangeListener(this) ;

        if (!swichScanService.isChecked()) {
            findPreference(CATEGORY_SYM_CONFIG).setEnabled(false);
            findPreference(CATEGORY_SCANNING).setEnabled(false);
            findPreference(CATEGORY_SCAN_TIME).setEnabled(false);
        }

        if (isboot) {
            Intent toServeive = new Intent(this, SotfScanService.class);
            startService(toServeive);
            finish();
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey() ;

        switch (key) {
            case SWITCH_SCAN_SERVICE:
                boolean checked = (Boolean)newValue ;
                if (checked) {
                    Intent intent = new Intent(ConfigActivity.this, SotfScanService.class);
                    startService(intent);
                    findPreference(CATEGORY_SYM_CONFIG).setEnabled(true);
                    findPreference(CATEGORY_SCANNING).setEnabled(true);
                    //CATEGORY_SCAN_TIME
                    findPreference(CATEGORY_SCAN_TIME).setEnabled(true);
                }else{
                    Intent toKillService = new Intent();
                    toKillService.setAction(SotfScanService.ACTION_CLOSE_SCAN);
                    sendBroadcast(toKillService);
//                    mPreference.SET;
                    findPreference(CATEGORY_SYM_CONFIG).setEnabled(false);
                    findPreference(CATEGORY_SCANNING).setEnabled(false);
                    findPreference(CATEGORY_SCAN_TIME).setEnabled(false);
                }

                break ;
            case SYM_AZTEC_ENABLE://码制开启
            case SYM_CODABAR_ENABLE :
            case SYM_CODABLOCK_ENABLE :
            case SYM_CODE11_ENABLE :
            case SYM_CODE128_ENABLE :
            case SYM_CODE32_ENABLE :
            case SYM_CODE39_ENABLE :
            case SYM_CODE49_ENABLE :
            case SYM_CODE93_ENABLE :
            case SYM_COMPOSITE_ENABLE :
            case SYM_COUPONCODE_ENABLE :
            case SYM_DATAMATRIX_ENABLE :
            case SYM_EAN8_ENABLE :
            case SYM_EAN13_ENABLE :
            case SYM_GS1_128_ENABLE :
            case SYM_HANXIN_ENABLE :
            case SYM_IATA25_ENABLE :
            case SYM_INT25_ENABLE :
            case SYM_ISBT_ENABLE :
            case SYM_MATRIX25_ENABLE :
            case SYM_MAXICODE_ENABLE :
            case SYM_MICROPDF_ENABLE :
            case SYM_MSI_ENABLE :
            case SYM_PDF417_ENABLE :
            case SYM_QR_ENABLE :
            case SYM_RSS_RSS_ENABLE :
            case SYM_STRT25_ENABLE :
            case SYM_TELEPEN_ENABLE  :
            case SYM_TLCODE39_ENABLE  :
            case SYM_TRIOPTIC_ENABLE  :
            case SYM_UPCA_ENABLE  :
            case SYM_UPCE0_ENABLE  :
            case SYM_UPCE1_UPCE1_ENABLE  :
                Intent configIntent = new Intent();
                configIntent.setAction(SotfScanService.ACTION_SCAN_CONFIG);
                sendBroadcast(configIntent);
                break ;
            case DECODE_TIME_LIMIT://设置超时时间
                ListPreference listPreferenceTime = (ListPreference)preference;
                //获取ListPreference中的实体内容
                CharSequence[] entriesTime = listPreferenceTime.getEntries();
                int indexTime = listPreferenceTime.findIndexOfValue((String)newValue);
              //  Toast.makeText(getApplicationContext(), index+ "", Toast.LENGTH_SHORT).show();
                //把listPreference中的摘要显示为当前ListPreference的实体内容中选择的那个项目
                listPreferenceTime.setSummary(entriesTime[indexTime]);
//                int value = Integer.valueOf((String) newValue);
//                Intent timeoutIntent = new Intent();
//                timeoutIntent.setAction(SotfScanService.ACTION_SCAN_SET_TIMEOUT);
//                timeoutIntent.putExtra("timeout", value);
//                sendBroadcast(timeoutIntent);

//                break;
            case INPUT_CONFIG://输入模式
                ListPreference listPreference = (ListPreference)preference;
                CharSequence[] entries=listPreference.getEntries();
                int index=listPreference.findIndexOfValue((String)newValue);
                listPreference.setSummary(entries[index]);
                break;
            case LIGHTS_CONFIG://灯光设置

                ListPreference listPreferenceLight = (ListPreference)preference;
                CharSequence[] entriesLight = listPreferenceLight.getEntries();
                int indexLight = listPreferenceLight.findIndexOfValue((String)newValue);
                listPreferenceLight.setSummary(entriesLight[indexLight]);
                Intent configLightIntent = new Intent();
                configLightIntent.setAction(SotfScanService.ACTION_SCAN_CONFIG);
                sendBroadcast(configLightIntent);
                break ;
            case APPEND_ENDING_CHAR:
                ListPreference listPreferenceAppend = (ListPreference)preference;
                CharSequence[] entriesAppend = listPreferenceAppend.getEntries();
                int indexAppend = listPreferenceAppend.findIndexOfValue((String)newValue);
                listPreferenceAppend.setSummary(entriesAppend[indexAppend]);
                break;

        }
        return true;
    }
}
