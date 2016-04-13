/*******************************************************************************
 * Copyright 2015 © GENBAND US LLC, All Rights Reserved              
 *                                                                   
 * This software embodies materials and concepts which are           
 * proprietary to GENBAND and/or its licensors and is made           
 * available to you for use solely in association with GENBAND       
 * products or services which must be obtained under a separate      
 * agreement between you and GENBAND or an authorized GENBAND        
 * distributor or reseller.                                          
 *                                                                   
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED      
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED            
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR        
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER    
 * AND/OR ITS LICENSORS BE LIABLE FOR ANY DIRECT, INDIRECT,          
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES          
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS   
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,      
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING         
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF     
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 * THE WARRANTY AND LIMITATION OF LIABILITY CONTAINED IN THIS        
 * AGREEMENT ARE FUNDAMENTAL PARTS OF THE BASIS OF GENBAND’S BARGAIN 
 * HEREUNDER, AND YOU ACKNOWLEDGE THAT GENBAND WOULD NOT BE ABLE TO  
 * PROVIDE THE PRODUCT TO YOU ABSENT SUCH LIMITATIONS.  IN THOSE     
 * STATES AND JURISDICTIONS THAT DO NOT ALLOW CERTAIN LIMITATIONS OF 
 * LIABILITY, GENBAND’S LIABILITY SHALL BE LIMITED TO THE GREATEST   
 * EXTENT PERMITTED UNDER APPLICABLE LAW.                            
 *                                                                   
 * Restricted Rights legend:                                         
 * Use, duplication, or disclosure by the U.S. Government is         
 * subject to restrictions set forth in subdivision (c)(1) of        
 * FAR 52.227-19 or in subdivision (c)(1)(ii) of DFAR 252.227-7013.  
 *******************************************************************************/
package com.kandy.starter;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.KandyCallSettings;
import com.genband.kandy.api.services.common.KandyCameraInfo;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.genband.kandy.api.utils.KandyLog;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class CallSettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	private static final String TAG = CallSettingsFragment.class.getSimpleName();
	
	private KandyCallSettings mSettings;

	private ListPreference mCameraFacingPrefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preference_call_settings);
		bindDefaultSettings();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	public void onPause() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		if (CallSettingsActivity.PREF_KEY_CAMERA_FACING.equals(key)) {

			Preference preference = findPreference(key);
			ListPreference listPref = (ListPreference) preference;
			listPref.getValue();
			mCameraFacingPrefs.setSummary(listPref.getEntry());
			KandyCameraInfo cameraInfo = KandyCameraInfo.valueOf(listPref.getValue());
			applyCameraFacingMode(cameraInfo);
		}
	}

	private void applyCameraFacingMode(KandyCameraInfo cameraInfo) {
		try
		{
			mSettings.setCameraMode(cameraInfo);
		}
		catch (KandyIllegalArgumentException e)
		{
			KandyLog.e(TAG, "applyCameraFacingMode", e);
		}
	}

	/**
	 * Binding the preferences and setting the default values on first run
	 */
	private void bindDefaultSettings() {
		mSettings = Kandy.getServices().getCallService().getSettings();

		mCameraFacingPrefs = (ListPreference) findPreference(CallSettingsActivity.PREF_KEY_CAMERA_FACING);
		int index = getValueIndex(mSettings.getCameraMode().name());
		mCameraFacingPrefs.setValueIndex(index);
		mCameraFacingPrefs.setSummary(mCameraFacingPrefs.getEntry());
	}

	/**
	 * get Value of Preference List by index of the value
	 * 
	 * @param value
	 * @return
	 */
	private int getValueIndex(String value) {

		CharSequence values[] = mCameraFacingPrefs.getEntryValues();
		for (int i = 0; i < values.length; i++) {

			if (values[i].toString().equals(value)) {
				return i;
			}
		}

		return 0;
	}
}
