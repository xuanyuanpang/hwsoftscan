package com.example.oemscandemo;


import com.hsm.barcode.DecoderException;
import com.hsm.barcode.Decoder;
import com.hsm.barcode.DecoderConfigValues.LightsMode;
import com.hsm.barcode.ExposureValues.ExposureMode;
import com.hsm.barcode.ExposureValues.ExposureSettings;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Build;
import android.preference.PreferenceManager;

public class ImageCaptureActivity extends Activity {
	
	private static final String TAG = "ImageCaptureActivity"; // Logging
	
	private final int CAPTURE_MODE_NORMAL = 0;		// Normal size image capture
	private final int CAPTURE_MODE_PREVIEW = 1;		// 1/4th size image capture

	private static final int SCAN_KEY = -1;	// TODO: Set to SCAN button			
	
	private static int g_ScanKey = SCAN_KEY;
	
	private static boolean bOkToCapture = true;
	
	private Decoder m_Decoder = null;				/** Decoder object */
	
	private int capture_mode = CAPTURE_MODE_NORMAL;
	
	private static boolean bRetainApplicationSettings = false;
	

	// Declare/Initialize an array of ints
	@SuppressWarnings("deprecation")
	int g_nExposureSettings[] = 
	{
		ExposureSettings.DEC_ES_EXPOSURE_METHOD, 0,
		ExposureSettings.DEC_ES_TARGET_VALUE, 0,
		ExposureSettings.DEC_ES_TARGET_PERCENTILE, 0,
		ExposureSettings.DEC_ES_TARGET_ACCEPT_GAP, 0,
		ExposureSettings.DEC_ES_MAX_EXP, 0,
		ExposureSettings.DEC_ES_MAX_GAIN, 0,
		ExposureSettings.DEC_ES_FRAME_RATE, 0,
		ExposureSettings.DEC_ES_CONFORM_IMAGE, 0,
		ExposureSettings.DEC_ES_CONFORM_TRIES, 0,
		ExposureSettings.DEC_ES_SPECULAR_EXCLUSION, 0,
		ExposureSettings.DEC_ES_SPECULAR_SAT, 0,
		ExposureSettings.DEC_ES_SPECULAR_LIMIT, 0,
		ExposureSettings.DEC_ES_FIXED_EXP, 0,
		ExposureSettings.DEC_ES_FIXED_GAIN, 0,
		ExposureSettings.DEC_ES_FIXED_FRAME_RATE, 0,
	};
	
	//blah b = new blah(this) ;
	//MainActivity b = new MainActivity();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_capture);
		
		// Show the Up button in the action bar.
		setupActionBar();
		
		// Default capture settings.
		//DefaultImageCaptureSettings();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_capture, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_settings:
			Intent i = new Intent(this, ImageCaptureSettingActivity.class);
            startActivityForResult(i, 1);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	 @Override
	public void onResume() {
	    super.onResume();  // Always call the superclass method first

		Log. d(TAG, "onResume");
		m_Decoder = new Decoder();
		
	    try {
			m_Decoder.connectDecoderLibrary();
		} catch (DecoderException e) {
			MainActivity.instance.HandleDecoderException(e);
			return;
		}
	    
	    // TODO: enable/disable settings if we can get here?

	    if(!bRetainApplicationSettings)
	    {
	    	bRetainApplicationSettings = true;
	    	DefaultImageCaptureSettings();//SetExposurePreferences(true);
	    }
	    
	    try {
			ConfigureImageCaptureSettings();
		} catch (DecoderException e) {
			// TODO Auto-generated catch block
			MainActivity.instance.HandleDecoderException(e);
			return;
		}
	    

	}
	 
	 
	 
	 /** 
	  * Called when application loses focus
	  * 
	  */
	@Override
	public void onPause() {
	    super.onPause();  // Always call the superclass method first

		Log. d(TAG, "onPause");
	    try {
			m_Decoder.disconnectDecoderLibrary();
		} catch (DecoderException e) {
			MainActivity.instance.HandleDecoderException(e);
		}
	    m_Decoder = null;
	}
	
	/** 
	  * Called when key is down
	  * 
	  */
   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) 
   { 
   	Log. d(TAG, "onKeyDown" + "keyCode=" + keyCode + "(g_ScanKey=" + g_ScanKey + ")");
   	
   	if (keyCode == g_ScanKey) 
   	{ 
   		ProcessScanButtonDown();
   	}
   	else
   	{
   		
   		return super.onKeyDown(keyCode, event);
   	}
   	
   	return false;
   }
   
   /** 
	  * Called when key is up
	  * 
	  */
   @Override
   public boolean onKeyUp(int keyCode, KeyEvent event) 
   { 
   	
   	Log. d(TAG, "onKeyDown" + "keyCode=" + keyCode + "(g_ScanKey=" + g_ScanKey + ")");
   	
   	if (keyCode == g_ScanKey) 
   	{
   		ProcessScanButtonUp();
   	}
   	else
   	{
   		return super.onKeyUp(keyCode, event);
   	}
   	
   	return false;
   }
   
   /** 
    * Called when the user clicks the Scan button 
    * 
    * */
	public void onClickTakePicture(View view) {
	    // Do something in response to button
		Log. d(TAG, "onClickScan");

		//MainActivity.instance.NewTest(); // calls MainActivity from here

		ProcessScanButtonDown();
		ProcessScanButtonUp();
	}	
   
   /** 
	  * Defaults image capture settings (and preferences)
	  * 
	  */
   void DefaultImageCaptureSettings()
   {
	   Log. d(TAG, "DefaultImageCaptureSettings++");
	   
	   SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	   Editor editor = prefs.edit();
	   
	   // Preview Mode:
	   editor.putBoolean("prefPreviewMode", false);		// Disabled
	   
	   // Lights:
	   editor.putBoolean("prefIlluminationOn", true);	// Illum On
	   editor.putBoolean("prefAimerOn", true);			// Aimer On
	   	   
	   // Exposure Settings: 
	   editor.putBoolean("exposure_settings_enable", false); 	// Disabled
	   editor.putString("prefExposureMode", Integer.toString(ExposureMode.HHP));	// Use Auto
	   SetExposurePreferences(true);							// Sets current (default) settings
	   
	   editor.commit();
	   
	   Log. d(TAG, "DefaultImageCaptureSettings--");
   }
   
   /** 
	  * Configures image capture settings based on preferences
 * @throws DecoderException 
	  * 
	  */
   void ConfigureImageCaptureSettings() throws DecoderException
   {
	   
	   Log. d(TAG, "ConfigureImageCaptureSettings++");
   	
       SharedPreferences sharedPrefs = PreferenceManager
               .getDefaultSharedPreferences(this);

       // Preview Mode:
       if(sharedPrefs.getBoolean("prefPreviewMode", false) == true )     	
    	   capture_mode = CAPTURE_MODE_PREVIEW;
       else
    	   capture_mode = CAPTURE_MODE_NORMAL;
       
       // Lights Mode:
       if(sharedPrefs.getBoolean("prefIlluminationOn", true) == false &&
       		sharedPrefs.getBoolean("prefAimerOn", true) == false )
       {       	
       	Log. d(TAG, "Neither aimer or illumination");
       	m_Decoder.setLightsMode(LightsMode.ILLUM_AIM_OFF);
       }
       else if(sharedPrefs.getBoolean("prefIlluminationOn", true) == true &&
       		sharedPrefs.getBoolean("prefAimerOn", true) == false )
       {       	
       	Log. d(TAG, "Illumination only");
       	m_Decoder.setLightsMode(LightsMode.ILLUM_ONLY);
       }
       else if(sharedPrefs.getBoolean("prefIlluminationOn", true) == false &&
       		sharedPrefs.getBoolean("prefAimerOn", true) == true )
       {       	
       	Log. d(TAG, "Aimer only");
       	m_Decoder.setLightsMode(LightsMode.AIMER_ONLY);
       }
       else
       {
       	Log. d(TAG, "Aimer and illumination alternating");
         	m_Decoder.setLightsMode(LightsMode.ILLUM_AIM_ON);
       }
       
       // Exposure Settings:
       if(sharedPrefs.getBoolean("exposure_settings_enable", false) == true )
       {
    	   // Mode:
    	   String temp = sharedPrefs.getString("prefExposureMode", Integer.toString(ExposureMode.HHP));
    	   m_Decoder.setExposureMode(Integer.parseInt(temp));
    	   // Settings:
    	   SetExposureSettings();	
       }
       
       Log. d(TAG, "ConfigureImageCaptureSettings--");
   }
   
   /** 
	  * Processes SCAN down
	  * 
	  */
   void ProcessScanButtonDown()
   {
	   Log. d(TAG, "ProcessScanDown");
	   if(bOkToCapture) // handshaking
	   {
		   bOkToCapture = false;
		   StartScanning();   
	   }
	   
	   // Get the image...
	   try {
		GetImage();
	   } catch (DecoderException e) {
			// TODO Auto-generated catch block
		   MainActivity.instance.HandleDecoderException(e);
	   }
   }
   
   /** 
	  * Processes SCAN up
	  * 
	  */
   void ProcessScanButtonUp()
   {
	   Log. d(TAG, "ProcessScanButtonUp");

	   StopScanning();
	   bOkToCapture = true;
   }
   
   /** 
	  * Starts scanning
	  * 
	  */
   void StartScanning()
   {
	   Log. d(TAG, "StartScanning");
	   try {
		m_Decoder.startScanning();
		} catch (DecoderException e) {
			MainActivity.instance.HandleDecoderException(e);
		}
   }
   
   /** 
	  * Stops scanning
	  * 
	  */
   void StopScanning()
   {
	   Log. d(TAG, "StopScanning");
	   try {
		m_Decoder.stopScanning();
		} catch (DecoderException e) {
			MainActivity.instance.HandleDecoderException(e);
	
		}
   }
   
   /** 
	  * Gets an image
 * @throws DecoderException 
	  * 
	  */
	private void GetImage() throws DecoderException
	{		
		Bitmap bmp = null;
		
		boolean bPreview = (capture_mode == CAPTURE_MODE_PREVIEW) ? true : false;
		
		// Set height width
		int height = MainActivity.instance.g_nImageHeight;
		int width = MainActivity.instance.g_nImageWidth;
		
		// Preview mode is image width/4 height/4
		if(bPreview)
		{
			height = height / 4;
			width = width / 4;
		}
			
		// Create bmp
		bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		
		// Get image
		if(bPreview)
			m_Decoder.getPreviewFrame(bmp);
		else
			m_Decoder.getSingleFrame(bmp);
		
		DisplayImage(bmp);
	}
	
	private void DisplayImage(Bitmap bmp)
	{
		ImageView imageView = (ImageView) findViewById(R.id.imageView1);
		
		// Display image
		imageView.setImageBitmap(bmp);
	}
	
	/**
	 * Sets exposure settings
	 * 
	 */
	private void SetExposureSettings()
	{
		Log. d(TAG, "SetExposureSettings");

		String debug = "g_nExposureSettings:\n";
		
		int tag = 0;
		
		SharedPreferences sharedPrefs = PreferenceManager
	               .getDefaultSharedPreferences(this);
		
		Log. d(TAG, "g_nExposureSettings length = " + g_nExposureSettings.length);
		for(int i = 0; i < g_nExposureSettings.length; i++)
		{
			tag = g_nExposureSettings[i];
			
			switch(tag)
			{
				case ExposureSettings.DEC_ES_EXPOSURE_METHOD:
					g_nExposureSettings[++i] = Integer.parseInt(sharedPrefs.getString("exposure_method", "0"));
					break;
				case ExposureSettings.DEC_ES_TARGET_VALUE:
					g_nExposureSettings[++i] = Integer.parseInt(sharedPrefs.getString("exposure_target_value", "0"));
					break;
				case ExposureSettings.DEC_ES_TARGET_PERCENTILE:
					g_nExposureSettings[++i] = Integer.parseInt(sharedPrefs.getString("exposure_target_percentile", "0"));
					break;
				case ExposureSettings.DEC_ES_TARGET_ACCEPT_GAP:
					g_nExposureSettings[++i] = Integer.parseInt(sharedPrefs.getString("exposure_target_acceptance_gap", "0"));
					break;
				case ExposureSettings.DEC_ES_MAX_EXP:
					g_nExposureSettings[++i] = Integer.parseInt(sharedPrefs.getString("exposure_max_exposure", "0"));
					break;
				case ExposureSettings.DEC_ES_MAX_GAIN:
					g_nExposureSettings[++i] = Integer.parseInt(sharedPrefs.getString("exposure_max_gain", "0"));
					break;
				case ExposureSettings.DEC_ES_FRAME_RATE:
					g_nExposureSettings[++i] = Integer.parseInt(sharedPrefs.getString("exposure_frame_rate", "0"));
					break;
				case ExposureSettings.DEC_ES_CONFORM_IMAGE:
					g_nExposureSettings[++i] = sharedPrefs.getBoolean("exposure_conform", false) == true ? 1 : 0;
					break;
				case ExposureSettings.DEC_ES_CONFORM_TRIES:
					g_nExposureSettings[++i] = Integer.parseInt(sharedPrefs.getString("exposure_conform_tries", "0"));
					break;
				case ExposureSettings.DEC_ES_SPECULAR_EXCLUSION:
					g_nExposureSettings[++i] = Integer.parseInt(sharedPrefs.getString("exposure_specular_exclusion", "0"));
					break;
				case ExposureSettings.DEC_ES_SPECULAR_SAT:
					g_nExposureSettings[++i] = Integer.parseInt(sharedPrefs.getString("exposure_specular_saturation", "0"));
					break;
				case ExposureSettings.DEC_ES_SPECULAR_LIMIT:
					g_nExposureSettings[++i] = Integer.parseInt(sharedPrefs.getString("exposure_specular_limit", "0"));
					break;
				case ExposureSettings.DEC_ES_FIXED_EXP:
					g_nExposureSettings[++i] = Integer.parseInt(sharedPrefs.getString("exposure_fixed_exposure", "0"));
					break;
				case ExposureSettings.DEC_ES_FIXED_GAIN:
					g_nExposureSettings[++i] = Integer.parseInt(sharedPrefs.getString("exposure_fixed_gain", "0"));
					break;
				case ExposureSettings.DEC_ES_FIXED_FRAME_RATE:
					g_nExposureSettings[++i] = Integer.parseInt(sharedPrefs.getString("exposure_fixed_frame_rate", "0"));
					break;
				default:
					Log. d(TAG, "Unrecognized tag!! tag = " + tag );
					break;			
			}	
			
			debug += "tag = " + tag + " value = " + g_nExposureSettings[i] + "\n";
		}
		
		Log. d(TAG, debug);
		
		try {
			m_Decoder.setExposureSettings(g_nExposureSettings);
		} catch (DecoderException e) {
			MainActivity.instance.HandleDecoderException(e);
		}
	}
	
	@SuppressWarnings("deprecation")
	private void SetExposurePreferences(boolean bDefault)
	{
		Log. d(TAG, "SetExposurePreferences++");

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		Editor editor = sharedPrefs.edit();
		
		GetExposureSettings(); // Get current settings

		String debug = "g_nExposureSettings:\n";
		
		int tag = 0;
		int value = 0;
		
		Log. d(TAG, "g_nExposureSettings length = " + g_nExposureSettings.length);
		for(int i = 0; i < g_nExposureSettings.length; i++)
		{
			tag = g_nExposureSettings[i];
			value = g_nExposureSettings[++i];
			
			switch(tag)
			{
				case ExposureSettings.DEC_ES_EXPOSURE_METHOD:
					editor.putString("exposure_method", Integer.toString(value));
					break;
				case ExposureSettings.DEC_ES_TARGET_VALUE:
					editor.putString("exposure_target_value", Integer.toString(value));
					break;
				case ExposureSettings.DEC_ES_TARGET_PERCENTILE:
					editor.putString("exposure_target_percentile", Integer.toString(value));
					break;
				case ExposureSettings.DEC_ES_TARGET_ACCEPT_GAP:
					editor.putString("exposure_target_acceptance_gap", Integer.toString(value));
					break;
				case ExposureSettings.DEC_ES_MAX_EXP:
					editor.putString("exposure_max_exposure", Integer.toString(value));
					break;
				case ExposureSettings.DEC_ES_MAX_GAIN:
					editor.putString("exposure_max_gain", Integer.toString(value));
					break;
				case ExposureSettings.DEC_ES_FRAME_RATE:
					editor.putString("exposure_frame_rate", Integer.toString(value));
					break;
				case ExposureSettings.DEC_ES_CONFORM_IMAGE:
					editor.putBoolean("exposure_conform", (value > 0 ? true : false));
					break;
				case ExposureSettings.DEC_ES_CONFORM_TRIES:
					editor.putString("exposure_conform_tries", Integer.toString(value));
					break;
				case ExposureSettings.DEC_ES_SPECULAR_EXCLUSION:
					editor.putString("exposure_specular_exclusion", Integer.toString(value));
					break;
				case ExposureSettings.DEC_ES_SPECULAR_SAT:
					editor.putString("exposure_specular_saturation", Integer.toString(value));
					break;
				case ExposureSettings.DEC_ES_SPECULAR_LIMIT:
					editor.putString("exposure_specular_limit", Integer.toString(value));
					break;
				case ExposureSettings.DEC_ES_FIXED_EXP:
					editor.putString("exposure_fixed_exposure", Integer.toString(value));
					break;
				case ExposureSettings.DEC_ES_FIXED_GAIN:
					editor.putString("exposure_fixed_gain", Integer.toString(value));
					break;
				case ExposureSettings.DEC_ES_FIXED_FRAME_RATE:
					editor.putString("exposure_fixed_frame_rate", Integer.toString(value));
					break;
				default:
					Log. d(TAG, "Unrecognized tag!! tag = " + tag );
					break;			
			}	
			
			debug += "tag = " + tag + " value = " + value + "\n";
		}
		
		Log. d(TAG, debug );
		
		// Commit to prefs
		editor.commit();
	}
	
	/**
	 * Gets exposure settings
	 * 
	 */
	private void GetExposureSettings()
	{	
		Log. d(TAG, "GetExposureSettings++");
		
		try {
			
			m_Decoder.getExposureSettings(g_nExposureSettings);
		} 
		catch (DecoderException e) {
			MainActivity.instance.HandleDecoderException(e);
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
		}
		
		String debug = "g_nExposureSettings:\n";
		
		int tag = 0;
		
		Log. d(TAG, "g_nExposureSettings length = " + g_nExposureSettings.length);
		for(int i = 0; i < g_nExposureSettings.length; i++)
		{
			tag = g_nExposureSettings[i++];			
			debug += "tag = " + tag + " value = " + g_nExposureSettings[i] + "\n";
		}
		
		Log. d(TAG, debug);
		
		Log. d(TAG, "GetExposureSettings--");
	}
}
   


