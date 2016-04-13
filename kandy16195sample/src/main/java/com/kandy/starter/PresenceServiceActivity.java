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

import java.util.ArrayList;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.presence.IKandyPresence;
import com.genband.kandy.api.services.presence.KandyPresenceResponseListener;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.kandy.starter.utils.UIUtils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PresenceServiceActivity extends Activity {

	private static final String TAG = PresenceServiceActivity.class.getSimpleName();

	private TextView uiWatchForText;
	private TextView uiPresenceText;

	private Button uiWatchButton;

	private EditText uiPhoneNumberEdit;
	
	private String mPhone;


	private OnClickListener onWatchButtonClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			getLastSeen();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_presence);
		
		initViews();
	}

	private void initViews() {
		uiWatchForText = (TextView)findViewById(R.id.activity_presence_watch_for_text);
		uiPresenceText = (TextView)findViewById(R.id.activity_presence_presence_text);
		
		uiPhoneNumberEdit = (EditText)findViewById(R.id.activity_presence_number_edit);

		uiWatchButton = (Button)findViewById(R.id.activity_presence_watch_button);
		uiWatchButton.setOnClickListener(onWatchButtonClicked);
	}

	private void showLastSeenOnUIThread(final String pWatchFor) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				showLastSeenFor(pWatchFor);
			}
		});
	}

	private void showLastSeenFor(String pWatchFor) {
		uiWatchForText.setText(pWatchFor);
	}

	private void setLastSeenOnUIThread(final String pState) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				setLastSeen(pState);
			}
		});
	}

	private void setLastSeen(String pState) {
		uiPresenceText.setText(pState);
	}

	private String getPhone() {
		return uiPhoneNumberEdit.getText().toString();
	}

	private void getLastSeen() {
		mPhone = getPhone();

		if(mPhone == null || mPhone.isEmpty()) {
			UIUtils.showDialogWithErrorMessage(this, "No Phone number for watch");
			return;
		}
		
		KandyRecord record = null;
		try {
			record = new KandyRecord(mPhone);
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			UIUtils.showDialogWithErrorMessage(this, getString(R.string.activity_login_empty_username_text));
			return;
		}
	
		ArrayList<KandyRecord> list = new ArrayList<KandyRecord>();
		list.add(record);
		
		Kandy.getServices().getPresenceService().retrievePresence(list, new KandyPresenceResponseListener() {
			
			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.e(TAG, "onRequestFailed: " + "responseCode: " + responseCode + " err:" + err);
				UIUtils.handleResultOnUiThread(PresenceServiceActivity.this, true, err);
			}
			
			@Override
			public void onRequestSucceed(ArrayList<IKandyPresence> presences, ArrayList<KandyRecord> missingRecords) {
				
				if(presences.size() > 0) {
					Log.d(TAG, "onRequestSucceed: " + " PRESENCE: " + presences.get(0).getLastSeenDate());
					setLastSeenOnUIThread(presences.get(0).getLastSeenDate().toString());
					showLastSeenOnUIThread(presences.get(0).getUser().getUri());
				}
				
				Log.i(TAG, "onRequestSucceed: " + "Presence: " + presences.size() + " users");
				for(IKandyPresence presence : presences) {
					Log.i(TAG, "onRequestSucceed: " + " Last Seen: " + presence.getLastSeenDate() +  " for: " + presence.getUser());
				}
				
				Log.i(TAG, "onRequestSucceed: " + "Missing Records: " + missingRecords.size() + " users");
				for(KandyRecord missingRecord : missingRecords) {
					Log.i(TAG, "onRequestSucceed: " + "Username: " + missingRecord.getUri());
				}
			}
		});
	}
}