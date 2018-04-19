package com.example.oemscandemo;


import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
 
public class ImageCaptureSettingActivity extends PreferenceActivity {
	
	private static final String TAG = "ImageCaptureSettingActivity";
	
	//  TODO: add to JNI
	final static int DEC_MIN_ES_EXPOSURE_METHOD = 0;
	final static int DEC_MAX_ES_EXPOSURE_METHOD = 2;
	final static int DEC_MIN_ES_TARGET_VALUE = 1;
	final static int DEC_MAX_ES_TARGET_VALUE = 255;
	final static int DEC_MIN_ES_TARGET_PERCENTILE =  1;
	final static int DEC_MAX_ES_TARGET_PERCENTILE = 99;
	final static int DEC_MIN_ES_TARGET_ACCEPT_GAP = 1;
	final static int DEC_MAX_ES_TARGET_ACCEPT_GAP = 50;
	final static int DEC_MIN_ES_MAX_EXP = 1;
	final static int DEC_MAX_ES_MAX_EXP = 7874;
	final static int DEC_MIN_ES_MAX_GAIN = 1;
	final static int DEC_MAX_ES_MAX_GAIN = 4;
	final static int DEC_MIN_ES_FRAME_RATE = 1;
	final static int DEC_MAX_ES_FRAME_RATE = 30;
	final static int DEC_MIN_ES_CONFORM_IMAGE = 0;
	final static int DEC_MAX_ES_CONFORM_IMAGE = 1;
	final static int DEC_MIN_ES_CONFORM_TRIES = 1;
	final static int DEC_MAX_ES_CONFORM_TRIES = 8;
	final static int DEC_MIN_ES_SPECULAR_EXCLUSION = 0;
	final static int DEC_MAX_ES_SPECULAR_EXCLUSION = 4;
	final static int DEC_MIN_ES_SPECULAR_SAT = 200;
	final static int DEC_MAX_ES_SPECULAR_SAT = 255;
	final static int DEC_MIN_ES_SPECULAR_LIMIT = 1;
	final static int DEC_MAX_ES_SPECULAR_LIMIT =  5;
	final static int DEC_MIN_ES_FIXED_EXP = 1;
	final static int DEC_MAX_ES_FIXED_EXP = 7874;
	final static int DEC_MIN_ES_FIXED_GAIN = 1;
	final static int DEC_MAX_ES_FIXED_GAIN = 4;
	final static int DEC_MIN_ES_FIXED_FRAME_RATE = 1;
	final static int DEC_MAX_ES_FIXED_FRAME_RATE = 30;
 
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
       // addPreferencesFromResource(R.xml.settings);
        addPreferencesFromResource(R.xml.image_capture_settings);
        
        findPreference("exposure_method").setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {	
                	Log.d(TAG, "Setting exp method to " + newValue.toString()); 
                	
                	//final static int DEC_MIN_ES_EXPOSURE_METHOD = 0;
                	//final static int DEC_MAX_ES_EXPOSURE_METHOD = 2;
                	if(
                			DEC_MIN_ES_EXPOSURE_METHOD <= Integer.parseInt(newValue.toString()) && 
                			DEC_MAX_ES_EXPOSURE_METHOD >= Integer.parseInt(newValue.toString()))
                		return true;
                	
                	Log.d(TAG, "Error: Value ranges from " + DEC_MIN_ES_EXPOSURE_METHOD + " to " + DEC_MAX_ES_EXPOSURE_METHOD); 
                	return false;
                }

            });
        
        findPreference("exposure_target_value").setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {	
                	Log.d(TAG, "Setting white target to to " + newValue.toString()); 
                	
                	//final static int DEC_MIN_ES_TARGET_VALUE = 1;
                	//final static int DEC_MAX_ES_TARGET_VALUE = 255;
                	if(
                			DEC_MIN_ES_TARGET_VALUE <= Integer.parseInt(newValue.toString()) && 
                			DEC_MAX_ES_TARGET_VALUE >= Integer.parseInt(newValue.toString()))
                		return true;
                	
                	Log.d(TAG, "Error: Value ranges from " + DEC_MIN_ES_TARGET_VALUE + " to " + DEC_MAX_ES_TARGET_VALUE); 
                	return false;
                }

            });
        
        findPreference("exposure_target_percentile").setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {	
                	Log.d(TAG, "Setting target percentile to " + newValue.toString()); 
                	
                	//final static int DEC_MIN_ES_TARGET_PERCENTILE =  1;
                	//final static int DEC_MAX_ES_TARGET_PERCENTILE = 99;
                	if(
                			DEC_MIN_ES_TARGET_PERCENTILE <= Integer.parseInt(newValue.toString()) && 
                			DEC_MAX_ES_TARGET_PERCENTILE >= Integer.parseInt(newValue.toString()))
                		return true;
                	
                	Log.d(TAG, "Error: Value ranges from " + DEC_MIN_ES_TARGET_PERCENTILE + " to " + DEC_MAX_ES_TARGET_PERCENTILE); 
                	return false;
                }

            });
        
        findPreference("exposure_target_acceptance_gap").setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {	
                	Log.d(TAG, "Setting target acceptance gap to " + newValue.toString()); 
                	
                	//final static int DEC_MIN_ES_TARGET_ACCEPT_GAP = 1;
                	//final static int DEC_MAX_ES_TARGET_ACCEPT_GAP = 50;
                	if(
                			DEC_MIN_ES_TARGET_ACCEPT_GAP <= Integer.parseInt(newValue.toString()) && 
                			DEC_MAX_ES_TARGET_ACCEPT_GAP >= Integer.parseInt(newValue.toString()))
                		return true;
                	
                	Log.d(TAG, "Error: Value ranges from " + DEC_MIN_ES_TARGET_ACCEPT_GAP + " to " + DEC_MAX_ES_TARGET_ACCEPT_GAP); 
                	return false;
                }

            });
 
    }  
    
    /* Flawless work around for submenu issue with 2.3 */
    @SuppressWarnings("deprecation")
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
    {
    	super.onPreferenceTreeClick(preferenceScreen, preference);
    	if (preference!=null)
	    	if (preference instanceof PreferenceScreen)
	        	if (((PreferenceScreen)preference).getDialog()!=null)
	        		((PreferenceScreen)preference).getDialog().getWindow().getDecorView().setBackgroundDrawable(this.getWindow().getDecorView().getBackground().getConstantState().newDrawable());
    	return false;
    }   
    
}