/*******************************************************************************
 * Copyright 2015 � GENBAND US LLC, All Rights Reserved              
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
 * AGREEMENT ARE FUNDAMENTAL PARTS OF THE BASIS OF GENBAND�S BARGAIN 
 * HEREUNDER, AND YOU ACKNOWLEDGE THAT GENBAND WOULD NOT BE ABLE TO  
 * PROVIDE THE PRODUCT TO YOU ABSENT SUCH LIMITATIONS.  IN THOSE     
 * STATES AND JURISDICTIONS THAT DO NOT ALLOW CERTAIN LIMITATIONS OF 
 * LIABILITY, GENBAND�S LIABILITY SHALL BE LIMITED TO THE GREATEST   
 * EXTENT PERMITTED UNDER APPLICABLE LAW.                            
 *                                                                   
 * Restricted Rights legend:                                         
 * Use, duplication, or disclosure by the U.S. Government is         
 * subject to restrictions set forth in subdivision (c)(1) of        
 * FAR 52.227-19 or in subdivision (c)(1)(ii) of DFAR 252.227-7013.  
 *******************************************************************************/
package com.kandy.starter;

import com.genband.kandy.api.Kandy;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class APISettingsActivity extends Activity {

	private static final String TAG = APISettingsActivity.class.getSimpleName();

	private static final String POWER_SAVING_MODE = "power_saving_mode";

	private Button uiSetApiKeyButton;
	private Button uiSetApiSecretButton;
	private Button uiSetHostUrlButton;

	private EditText uiAPIKeyEdit;
	private EditText uiAPISecretEdit;
	private EditText uiHostUrlEdit;
	private TextView uiAPIKeyTextView;
	private TextView uiAPISecretTextView;
	private TextView uiConfigReportTextView;

	private SharedPreferences mDefaultSharedPrefs;

	private CheckedTextView mPowerSavingModeCheckBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_apisettings);

		mDefaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		initViews();
	}

	private void initViews() {
		
		uiSetApiKeyButton = (Button)findViewById(R.id.activity_settings_set_api_key_button);
		uiSetApiKeyButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				setNewApiKey();
			}
		});
		
		uiSetApiSecretButton = (Button)findViewById(R.id.activity_settings_set_api_secret_button);
		uiSetApiSecretButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setNewApiSecret();
			}
		});
		
		uiAPIKeyEdit = (EditText)findViewById(R.id.activity_settings_current_api_key_edit);
		uiAPISecretEdit = (EditText)findViewById(R.id.activity_settings_current_api_secret_edit);

		uiAPIKeyTextView = (TextView)findViewById(R.id.activity_settings_current_api_key_label);
		uiAPISecretTextView = (TextView)findViewById(R.id.activity_settings_current_api_secret_label);
		
		uiHostUrlEdit = (EditText)findViewById(R.id.activity_settings_host_edit);
		uiHostUrlEdit.setText(Kandy.getGlobalSettings().getKandyHostURL());
		
		uiSetHostUrlButton = (Button)findViewById(R.id.activity_settings_set_host_button);
		uiSetHostUrlButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String newHost = uiHostUrlEdit.getText().toString();
				
				Kandy.getGlobalSettings().setKandyHostURL(newHost);
				mDefaultSharedPrefs.edit().putString(DashboardActivity.KANDY_HOST_PREFS_KEY, newHost).commit();
			}
		});
		
		uiConfigReportTextView = (TextView)findViewById(R.id.activity_settings_config_report_tv);

		setCurrentApiKey();
		setCurrentApiSecret();
		
		uiConfigReportTextView.setText(Kandy.getGlobalSettings().getReport());
		
		mPowerSavingModeCheckBox = (CheckedTextView)findViewById(R.id.activity_settings_enable_power_saving);
		final boolean powerSavingEnabled = Kandy.getGlobalSettings().isPowerSaverEnable();
		mPowerSavingModeCheckBox.setChecked(powerSavingEnabled);
		mPowerSavingModeCheckBox.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View mV)
			{
				mPowerSavingModeCheckBox.toggle();
				Kandy.getGlobalSettings().setPowerSaverEnable(mPowerSavingModeCheckBox.isChecked());
				uiConfigReportTextView.setText(Kandy.getGlobalSettings().getReport());
			}
		});
	}

	private void setNewApiKey() {
		String key = uiAPIKeyEdit.getText().toString();
		if(key == null || key.isEmpty()) {
			Toast.makeText(APISettingsActivity.this, "API key field is empty",Toast.LENGTH_SHORT).show();
			return;
		}
		
		SharedPreferences.Editor edit = mDefaultSharedPrefs.edit();
		edit.putString(DashboardActivity.API_KEY_PREFS_KEY, key).commit();
		edit.putBoolean(DashboardActivity.API_KEY_CHANGED_PREFS_KEY, true).commit();
		
		setCurrentApiKey();
		Kandy.initialize(this, key, mDefaultSharedPrefs.getString(DashboardActivity.API_SECRET_PREFS_KEY, ""));
	}
	
	private void setHost(String hostUrl) {
		
	}
	
	
	
	
	
	/**
	 * Set new API secret and save to {@link SharedPreferences} to be initialized on next application launch
	 */
	private void setNewApiSecret() {
		String secret = uiAPISecretEdit.getText().toString();

		if(secret == null || secret.isEmpty()) {
			Toast.makeText(APISettingsActivity.this, "API secret field is empty",Toast.LENGTH_SHORT).show();
			return;
		}
		
		Kandy.getGlobalSettings().setKandyDomainSecret(secret);

		SharedPreferences.Editor edit = mDefaultSharedPrefs.edit();
		edit.putString(DashboardActivity.API_SECRET_PREFS_KEY, secret).commit();
		edit.putBoolean(DashboardActivity.API_SECRET_CHANGED_PREFS_KEY, true).commit();

		uiAPISecretTextView.setText(secret);
		Kandy.initialize(this, mDefaultSharedPrefs.getString(DashboardActivity.API_KEY_PREFS_KEY, ""), secret);
	}
	
	private void setCurrentApiKey() {
		
		boolean isChanged = mDefaultSharedPrefs.getBoolean(DashboardActivity.API_KEY_CHANGED_PREFS_KEY, false);
		
		if(!isChanged) {
			uiAPIKeyTextView.setText(KandySampleApplication.API_KEY);
		} else {
			uiAPIKeyTextView.setText(mDefaultSharedPrefs.getString(DashboardActivity.API_KEY_PREFS_KEY, ""));
		}
	}

	private void setCurrentApiSecret() {
		
		boolean isChanged = mDefaultSharedPrefs.getBoolean(DashboardActivity.API_SECRET_CHANGED_PREFS_KEY, false);
		
		if(!isChanged) {
			uiAPISecretTextView.setText(KandySampleApplication.API_SECRET);
		} else {
			uiAPISecretTextView.setText(mDefaultSharedPrefs.getString(DashboardActivity.API_SECRET_PREFS_KEY, ""));
		}
	}
}
