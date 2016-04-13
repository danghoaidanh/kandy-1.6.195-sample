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


import org.json.JSONObject;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.access.KandyConnectServiceNotificationListener;
import com.genband.kandy.api.access.KandyConnectionState;
import com.genband.kandy.api.access.KandyLoginResponseListener;
import com.genband.kandy.api.access.KandyLogoutResponseListener;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.kandy.starter.utils.UIUtils;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AccessActivity extends Activity implements KandyConnectServiceNotificationListener {

	private static final String TAG = AccessActivity.class.getSimpleName();

	private String mUserAccessToken = "UAT60d2ad7fd839413f8c6201c44e0f5067"; // put your USER_ACCESS_TOKEN here to force login with it instead of user & password
	
	private EditText uiUsernameEditText;
	private EditText uiPasswordEditText;

	private Button uiLoginButton;
	private Button uiLogoutButton;

	private TextView uiRegistrationStateTextView;

	private OnClickListener onLoginButtonCLicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			startLoginProcess();
		}
	};

	private OnClickListener onLogoutButtonClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			startLogoutProcess();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		initViews();	
	}

	@Override
	protected void onResume() {
		super.onResume();
		initRegistrationStateListeners();
		setRegistrationState(Kandy.getAccess().getConnectionState());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterRegistrationStateListeners();
	}

	/**
	 * Set the registration state from the NON UI thread on a UI thread
	 * 
	 * @param pState
	 *            registration state {@link KandyRegistrationState}
	 */
	private void setRegistrationStateOnUIThread(final KandyConnectionState pState) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				setRegistrationState(pState);
			}
		});
	}

	/**
	 * Set the registration state
	 * 
	 * @param pState
	 *            registration state {@link KandyRegistrationState}
	 */
	private void setRegistrationState(KandyConnectionState pState) {
		uiRegistrationStateTextView.setText(pState.name());
	}

	private void initViews() {
		uiLoginButton = (Button) findViewById(R.id.activity_login_login_button);
		uiLoginButton.setOnClickListener(onLoginButtonCLicked);
		uiLogoutButton = (Button) findViewById(R.id.activity_login_logout_button);
		uiLogoutButton.setOnClickListener(onLogoutButtonClicked);
		uiRegistrationStateTextView = (TextView) findViewById(R.id.activity_login_registration_state);

		uiUsernameEditText = (EditText)findViewById(R.id.activity_login_username_edit);
		uiPasswordEditText = (EditText)findViewById(R.id.activity_login_password_edit);
	}

	private String getUsername() {
		return uiUsernameEditText.getText().toString();
	}

	private String getPassword() {
		return uiPasswordEditText.getText().toString();
	}

	private void startLoginProcess() {
		UIUtils.showProgressDialogWithMessage(AccessActivity.this, getString(R.string.activity_login_login_process));
		
		// if user access token is define we login without user and password
		if (!TextUtils.isEmpty(mUserAccessToken)) {
			login(mUserAccessToken);
		} else {
			login(getUsername(), getPassword());
		}
	}
	
	/**
	 * Register/login the user on the server with credentials received from admin
	 * @param pUsername username
	 * @param pDomain user's domain
	 * @param pPassword password
	 */
	private void login(final String pUsername, final String pPassword) {

		KandyRecord kandyUser ;

		try {
			kandyUser = new KandyRecord(pUsername);

		} catch (KandyIllegalArgumentException ex) {
			UIUtils.showDialogWithErrorMessage(this, getString(R.string.activity_login_empty_username_text));
			return;
		}

		if(pPassword == null || pPassword.isEmpty()) {
			UIUtils.showDialogWithErrorMessage(this, getString(R.string.activity_login_empty_password_text));
			return ;
		}
		
		Kandy.getAccess().login(kandyUser, pPassword, new KandyLoginResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.i(TAG, "Kandy.getAccess().login:onRequestFailed error: " + err + ". Response code: " + responseCode);
				UIUtils.handleResultOnUiThread(AccessActivity.this, true, err);
			}

			@Override
			public void onLoginSucceeded() {
				Log.i(TAG, "Kandy.getAccess().login:onLoginSucceeded");
				UIUtils.handleResultOnUiThread(AccessActivity.this, false, getString(R.string.activity_login_login_success));
				finish();
			}
		});
	}
	
	/**
	 * Register/login the user on the server with userAccessToken 
	 * @param userAccessToken The use access token string
	 */
	private void login(String userAccessToken) {
		Kandy.getAccess().login(userAccessToken, new KandyLoginResponseListener()
		{

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.i(TAG, "Kandy.getAccess().login:onRequestFailed error: " + err + ". Response code: " + responseCode);
				UIUtils.handleResultOnUiThread(AccessActivity.this, true, err);
			}

			@Override
			public void onLoginSucceeded() {
				Log.i(TAG, "Kandy.getAccess().login:onLoginSucceeded");
				UIUtils.handleResultOnUiThread(AccessActivity.this, false, getString(R.string.activity_login_login_success));
				finish();
			}
		});
	}

	private void startLogoutProcess() {
		UIUtils.showProgressDialogWithMessage(AccessActivity.this, getString(R.string.activity_login_logout_process));
		logout();
	}

	/**
	 * This method unregisters user from the server
	 */
	private void logout() {
		
		Kandy.getAccess().logout(new KandyLogoutResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.i(TAG, "Kandy.getAccess().logout:onRequestFailed error: " + err + ". Response code: " + responseCode);
				UIUtils.handleResultOnUiThread(AccessActivity.this, true, err);
			}

			@Override
			public void onLogoutSucceeded() {
				UIUtils.handleResultOnUiThread(AccessActivity.this, false, getString(R.string.activity_login_logout_success));
			}
		});
	}

	/**
	 * Check if device already logged in or logged out
	 * @return logged or not
	 */
	public static boolean isLoggedIn() {
		return (KandyConnectionState.CONNECTED.equals(Kandy.getAccess().getConnectionState()));
	}

	/**
	 * Register the {@link KandyRegistrationNotificationListener} to reeceive
	 * calls related events like onRegistrationStateChenged
	 */
	private void initRegistrationStateListeners() {
		Log.d(TAG, "initRegistrationStateListeners()");
		Kandy.getAccess().registerNotificationListener(this);
	}
	
	private void unregisterRegistrationStateListeners() {
		Log.d(TAG, "unregisterRegistrationStateListeners()");
		Kandy.getAccess().unregisterNotificationListener(this);
	}
	
	@Override
	public void onSocketFailedWithError(String error) {
		Log.w(TAG, "onSocketFailedWithError(): " + error);
	}

	@Override
	public void onSocketDisconnected() {
		Log.i(TAG, "onSocketDisconnected() fired");
	}

	@Override
	public void onSocketConnecting() {
		Log.i(TAG, "onSocketConnecting() fired");
	}

	@Override
	public void onSocketConnected() {
		Log.i(TAG, "onSocketConnected() fired");
	}

	@Override
	public void onConnectionStateChanged(KandyConnectionState state) {
		Log.i(TAG,"onRegistrationStateChanged() fired with state: " + state.name());
		setRegistrationStateOnUIThread(state);
	}
	
	@Override
	public void onInvalidUser(String error) {
		Log.i(TAG,"onInvalidUser() fired with error: " + error);
		UIUtils.handleResultOnUiThread(AccessActivity.this, true, error);
	}

	@Override
	public void onSessionExpired(String error) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSDKNotSupported(String error) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCertificateError(String error)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onServerConfigurationReceived(JSONObject serverConfiguration)
	{
		// TODO Auto-generated method stub
		
	}
}