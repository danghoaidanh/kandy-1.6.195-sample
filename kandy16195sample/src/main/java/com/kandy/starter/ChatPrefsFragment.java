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

import java.io.File;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.chats.IKandyChatDownloadPathBuilder;
import com.genband.kandy.api.services.chats.IKandyFileItem;
import com.genband.kandy.api.services.chats.KandyChatSettings;
import com.genband.kandy.api.services.chats.KandyThumbnailSize;
import com.genband.kandy.api.services.common.ConnectionType;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.kandy.starter.utils.MediaSizePicker;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

public class ChatPrefsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	private static final String TAG = ChatPrefsFragment.class.getSimpleName();
	
	private ListPreference mDownloadPolicyPrefs;
	private ListPreference mAutoDownloadThumbnailSizePrefs;
	private EditTextPreference mDownloadPathPrefs;
	private MediaSizePicker mMediaSizePrefs;
	private KandyChatSettings mSettings;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preference_chat_settings);
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
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		if (ChatSettingsActivity.PREF_KEY_PATH.equals(key)) {
			
			String path = sharedPreferences.getString(key, "");
			mDownloadPathPrefs.setSummary(path);
			applyMediaDownloadPath(path);

		} else if (ChatSettingsActivity.PREF_KEY_CUSTOM_PATH.equals(key)) {

			boolean usePath = sharedPreferences.getBoolean(key, false);

			if (usePath)
			{
				applyCustomMediaDownloadPath(new IKandyChatDownloadPathBuilder() {
					
					@Override
					public File uploadAbsolutePath() {
						
						File dir = new File(Environment.getExternalStorageDirectory(), "custom");
						dir.mkdirs();
						
						return dir;
					}
					
					@Override
					public File downloadAbsolutPath(KandyRecord sender, KandyRecord recipient, IKandyFileItem fileItem,
							boolean isThumbnail) {
						
						File dir = new File(Environment.getExternalStorageDirectory(), sender.getUserName());
						dir.mkdirs();
						
						File file = new File(dir, fileItem.getDisplayName());
						
						return file;
					}
				});
			} 

		} else if(ChatSettingsActivity.PREF_KEY_MAX_SIZE.equals(key)) {

			int maxSize = sharedPreferences.getInt(key, 0);
			mMediaSizePrefs.setSummary(createMaxSizeMessage(maxSize));
			applyMaxMediaSize(maxSize);

		} else if(ChatSettingsActivity.PREF_KEY_POLICY.equals(key)) {
			
			Preference preference = findPreference(key);
			ListPreference listPref = (ListPreference) preference;
			listPref.getValue();
			mDownloadPolicyPrefs.setSummary(listPref.getEntry());
			ConnectionType policy = ConnectionType.valueOf(listPref.getValue());
			applyDownloadPolicy(policy);
			
		} else if(ChatSettingsActivity.PREF_KEY_THUMB_SIZE.equals(key)) {
			
			Preference preference = findPreference(key);
			ListPreference listPref = (ListPreference)preference;
			listPref.getValue();
			mAutoDownloadThumbnailSizePrefs.setSummary(listPref.getEntry());
			KandyThumbnailSize size = KandyThumbnailSize.valueOf(listPref.getValue());
			applyAutoDownloadThumbnailSize(size);
		}
	}
	
	/**
	 * Binding the preferences and setting the default values on first run
	 */
	private void bindDefaultSettings() {
		mSettings = Kandy.getServices().getChatService().getSettings();

		mDownloadPolicyPrefs = (ListPreference)findPreference(ChatSettingsActivity.PREF_KEY_POLICY);
		mDownloadPathPrefs = (EditTextPreference)findPreference(ChatSettingsActivity.PREF_KEY_PATH);
		mMediaSizePrefs = (MediaSizePicker)findPreference(ChatSettingsActivity.PREF_KEY_MAX_SIZE);
		mAutoDownloadThumbnailSizePrefs = (ListPreference)findPreference(ChatSettingsActivity.PREF_KEY_THUMB_SIZE);
		
		if (mDownloadPathPrefs.getText() == null) {
			mDownloadPathPrefs.setText(mSettings.getDownloadMediaPath().getPath());
		}

		mDownloadPathPrefs.setSummary(mDownloadPathPrefs.getText().toString());


		if(mDownloadPolicyPrefs.getValue() == null) {
			int index = getValueIndex(mSettings.getAudoDownloadMediaConnectionType().name());
			mDownloadPolicyPrefs.setValueIndex(index);
		}

		mDownloadPolicyPrefs.setSummary(mDownloadPolicyPrefs.getEntry());

		if(mAutoDownloadThumbnailSizePrefs.getValue() == null) {
			int index = getValueIndex(mSettings.getAutoDownloadThumbnailSize().name());
			mAutoDownloadThumbnailSizePrefs.setValueIndex(index);
		}
		
		mAutoDownloadThumbnailSizePrefs.setSummary(mAutoDownloadThumbnailSizePrefs.getEntry());
		
		int  maxSize = mMediaSizePrefs.getValue();
		if(maxSize == -1) {
			maxSize = mSettings.getMediaMaxSize();
		}
		mMediaSizePrefs.setDefaultValue((Integer)maxSize);
		mMediaSizePrefs.setSummary(createMaxSizeMessage(maxSize));
	}

	/**
	 * Create the label with maximal size value
	 * @param size maximal size set
	 * @return label with message
	 */
	private String createMaxSizeMessage(int size) {
		String formatingString = getString(R.string.chat_settings_max_size_msg);
		String str = String.format(formatingString, size);
		return str;
	}
	
	/**
	 * get Value of Preference List by index of the value
	 * @param value
	 * @return
	 */
	private int getValueIndex(String value) {
		
		CharSequence values[] = mDownloadPolicyPrefs.getEntryValues();
		for(int i=0; i<values.length; i++) {

			if(values[i].toString().equals(value)) {
				return i;
			}
		}
		
		return 0;
	}
	
	private void applyMaxMediaSize(int size) {
		try {
			mSettings.setMediaMaxSize(size);
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			Log.w(TAG, "applyMaxMediaSize: " + e.getLocalizedMessage(), e);
		}
	}
	
	private void applyDownloadPolicy(ConnectionType policy) {
		mSettings.setAutoDownloadMediaConnectionType(policy);
	}
	
	private void applyMediaDownloadPath(String path) {
		mSettings.setDownloadMediaPath(new File(path));
	}
	
	private void applyCustomMediaDownloadPath(IKandyChatDownloadPathBuilder builder) {
		mSettings.setDownloadMediaPath(builder);
	}
	
	private void applyAutoDownloadThumbnailSize(KandyThumbnailSize thumbnailSize) {
		mSettings.setAutoDownloadThumbnailSize(thumbnailSize);
	}
}
