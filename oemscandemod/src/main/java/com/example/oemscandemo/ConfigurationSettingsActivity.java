package com.example.oemscandemo;

import com.hsm.barcode.DecoderException;
import com.hsm.barcode.Decoder;
import com.hsm.barcode.DecoderConfigValues.SymbologyID;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
 
public class ConfigurationSettingsActivity extends PreferenceActivity {
	private  final String TAG = "BarcodeConfigSettingsActivity";
	private Decoder m_decDecoder = null;
 
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        m_decDecoder = new Decoder();	// FIXME: Can we use instance from main activity?
        
        setTheme(android.R.style.Theme_Light);

        addPreferencesFromResource(R.xml.configuration_settings);
       
       /**
        * Aztec min check example
        * 
        */
       findPreference("sym_aztec_min").setOnPreferenceChangeListener(
               new Preference.OnPreferenceChangeListener() {
               @Override
               public boolean onPreferenceChange(Preference preference, Object newValue) {	
               	Log.d(TAG, "Aztec min setting to " + newValue.toString()); 
               	
               	int min_range = 0;
               	try {
					min_range = m_decDecoder.getSymbologyMinRange(SymbologyID.SYM_AZTEC);
				} catch (DecoderException e) {
					HandleDecoderException(e);
					return false;
				}
               	
               	Log.d(TAG, "Aztec min = " + min_range); 
               	
               	   if(Integer.parseInt(newValue.toString()) < min_range ) 
               	   {
               		   Log.d(TAG, "Min setting out of range"); 
               		   return false;
               	   }
               	   
                   return true;
               }

           });
       
       /**
        * Aztec max check example
        * 
        */
       findPreference("sym_aztec_max").setOnPreferenceChangeListener(
               new Preference.OnPreferenceChangeListener() {
               @Override
               public boolean onPreferenceChange(Preference preference, Object newValue) {	
               	Log.d(TAG, "Aztec max setting to " + newValue.toString()); 
               	
               	int max_range = 0;
               	
               	try {
					Log.d(TAG, "Aztec max = " + m_decDecoder.getSymbologyMaxRange(SymbologyID.SYM_AZTEC));
				} catch (DecoderException e) {
					// TODO Auto-generated catch block
					HandleDecoderException(e);
				} 
               	
               	
               		if(Integer.parseInt(newValue.toString()) > max_range ) 
				   {
               		 	Log.d(TAG, "Max setting out of range");
					   return false;
				   }
               	
                   return true;
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
    
    void HandleDecoderException(DecoderException e)
    {
    	Log.d(TAG, "Config Error: " + e.getMessage()); 
    }
 
}