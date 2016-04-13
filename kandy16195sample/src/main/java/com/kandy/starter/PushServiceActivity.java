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
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PushServiceActivity extends Activity {

	private static final String TAG = PushServiceActivity.class.getSimpleName();
	
	private TextView uiPushServiceStateTextView;
	
	private Button uiPushEnableButton;
	private Button uiPushDisableButton;
	
	private OnClickListener onPushEnableButtonCLicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			Context context = PushServiceActivity.this;
			GCMRegistrar.checkDevice(context);
			GCMRegistrar.checkManifest(context);
			String registrationId = GCMRegistrar.getRegistrationId(context);

			if (TextUtils.isEmpty(registrationId))
			{
				Log.d(TAG, "onPushEnableButtonCLicked GCM Push registration sent");
				GCMRegistrar.register(context, KandySampleApplication.GCM_PROJECT_ID);
			}

			Log.d(TAG, "onPushEnableButtonCLicked GCM Push registered update Kandy servers with registrationId: " + registrationId);
			
			Kandy.getServices().getPushService().enablePushNotification(registrationId, new KandyResponseListener() {
				
				@Override
				public void onRequestFailed(int responseCode, String err) {
					Log.i(TAG, "Kandy.getPushService().enablePushNotification:onRequestFailed with error: " + err);
				}
				
				@Override
				public void onRequestSucceded() {
					Log.i(TAG, "Kandy.getPushService().enablePushNotification:onRequestSucceded");
					setPushServiceStateOnUIThread(getString(R.string.activity_push_state_enabled_label));
				}
			});
		}
	};
	
	private OnClickListener onPushDisableButtonCLicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Kandy.getServices().getPushService().disablePushNotification(new KandyResponseListener() {
				
				@Override
				public void onRequestFailed(int responseCode, String err) {
					Log.i(TAG, "Kandy.getPushService().disablePushNotification:onRequestFailed with error: " + err);
				}
				
				@Override
				public void onRequestSucceded() {
					Log.i(TAG, "Kandy.getPushService().disablePushNotification:onRequestSucceded");
					setPushServiceStateOnUIThread(getString(R.string.activity_push_state_disabled_label));
				}
			});
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_push);
		
		initViews();
	}
	
	private void initViews() {
		uiPushServiceStateTextView = (TextView)findViewById(R.id.activity_push_state_label);
		
		uiPushEnableButton = (Button)findViewById(R.id.activity_push_enable_button);
		uiPushEnableButton.setOnClickListener(onPushEnableButtonCLicked);
		
		uiPushDisableButton = (Button)findViewById(R.id.activity_push_disable_button);
		uiPushDisableButton.setOnClickListener(onPushDisableButtonCLicked);
	}
	
	public void setPushServiceState(String pState) {
		uiPushServiceStateTextView.setText(pState);
	}
	
	public void setPushServiceStateOnUIThread(final String pState) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				setPushServiceState(pState);
			}
		});
	}
}