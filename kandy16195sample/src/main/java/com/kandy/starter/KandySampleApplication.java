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

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.genband.kandy.api.IKandyGlobalSettings;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.chats.KandyChatSettings;
import com.genband.kandy.api.services.chats.KandyThumbnailSize;
import com.genband.kandy.api.services.common.ConnectionType;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.genband.kandy.api.utils.KandyLog.Level;
import com.kandy.starter.utils.FileUtils;
import com.kandy.starter.utils.KandyLogger;

import java.io.File;

public class KandySampleApplication extends Application {


	public static final String TAG = KandySampleApplication.class.getSimpleName();

	public static final String API_KEY = "";
	public static final String API_SECRET = "";
	public static final String GCM_PROJECT_ID = "876416103603";

	
	@Override
	public void onCreate() {
		super.onCreate();

		// set log level
		Kandy.getKandyLog().setLogLevel(Level.VERBOSE);

		//Init Kandy SDK
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Kandy.initialize(this, prefs.getString(DashboardActivity.API_KEY_PREFS_KEY, KandySampleApplication.API_KEY),
				prefs.getString(DashboardActivity.API_SECRET_PREFS_KEY, KandySampleApplication.API_SECRET));
		
		// set custom logger instead of using default one
		Kandy.getKandyLog().setLogger(new KandyLogger());
		
		// set chat settings
		applyKandyChatSettings();
		
		// set host
		IKandyGlobalSettings settings = Kandy.getGlobalSettings();
		settings.setKandyHostURL(prefs.getString(DashboardActivity.KANDY_HOST_PREFS_KEY, settings.getKandyHostURL()));
		
		// load tutorials
		prepareLocalStorage();
		
	}


	private void prepareLocalStorage()
	{
		File localStorageDirectory = FileUtils.getFilesDirectory(KandyCloudStorageServiceActivity.LOCAL_STORAGE);
		FileUtils.clearDirectory(localStorageDirectory);
		FileUtils.copyAssets(getApplicationContext(), localStorageDirectory);
	}


	/**
	 * Applies the {@link KandyChatSettings} with user defined settings or default if not set by developer
	 */
	public void applyKandyChatSettings() {

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		KandyChatSettings settings = Kandy.getServices().getChatService().getSettings();

		String value = sp.getString(ChatSettingsActivity.PREF_KEY_POLICY, null);
		if(value != null) { //otherwise will be used default setting from SDK
			ConnectionType downloadPolicy = ConnectionType.valueOf(value);
			settings.setAutoDownloadMediaConnectionType(downloadPolicy);
		}

		int uploadSize = sp.getInt(ChatSettingsActivity.PREF_KEY_MAX_SIZE, -1);
		
		if(uploadSize != -1) { //otherwise will be used default setting from SDK
			try {
				settings.setMediaMaxSize(uploadSize);
			} catch (KandyIllegalArgumentException e) {
				Log.d(TAG, "applyKandyChatSettings: " + e.getMessage());
			}
		}

		value = sp.getString(ChatSettingsActivity.PREF_KEY_PATH, null);
		if(value != null) { //otherwise will be used default setting from SDK
			File downloadPath = new File(value);
			settings.setDownloadMediaPath(downloadPath);
		}
		
		value = sp.getString(ChatSettingsActivity.PREF_KEY_THUMB_SIZE, null);
		if(value != null) { //otherwise will be used default setting from SDK
			KandyThumbnailSize thumbnailSize = KandyThumbnailSize.valueOf(value);
			settings.setAutoDownloadThumbnailSize(thumbnailSize);
		}
	}


}