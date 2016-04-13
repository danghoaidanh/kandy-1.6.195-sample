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
import com.genband.kandy.api.services.location.IKandyAreaCode;
import com.genband.kandy.api.services.location.KandyCountryInfoResponseListener;
import com.kandy.starter.utils.UIUtils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class LocationServiceActivity extends Activity {

	private static final String TAG = LocationServiceActivity.class.getSimpleName();
	
	private Button uiGetLocationCodeButton;
	
	private TextView uiLocationCodeTextView;
	private TextView uiLocationLongNameTextView;
	private TextView uiLocationNumCodeTextView;
	
	private OnClickListener onGetLocationCodeButtonClicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			getLocationCode();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		
		initViews();
	}
	
	private void initViews() {
		uiLocationCodeTextView = (TextView)findViewById(R.id.activity_location_two_lewtter_code_text);
		uiLocationLongNameTextView = (TextView)findViewById(R.id.activity_location_long_name_label);
		uiLocationNumCodeTextView = (TextView)findViewById(R.id.activity_location_code_text);
		uiGetLocationCodeButton = (Button)findViewById(R.id.activity_location_get_location_button);
		uiGetLocationCodeButton.setOnClickListener(onGetLocationCodeButtonClicked);
	}
	
	private void setTextInTextViewOnUIThread(final TextView pTxtView, final String pText) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				pTxtView.setText(pText);	
			}
		});
	}
	
	/**
	 * Get Two letter country code
	 */
	private void getLocationCode() {
		
		Kandy.getServices().getLocationService().getCountryInfo(new KandyCountryInfoResponseListener() {
			
			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.i(TAG, "Kandy.getServices().getLocationService().getCountryInfo: " + err + ".Response code: " + responseCode);
				UIUtils.handleResultOnUiThread(LocationServiceActivity.this, true, err);
			}
			
			@Override
			public void onRequestSuccess(IKandyAreaCode response) {
				UIUtils.handleResultOnUiThread(LocationServiceActivity.this, false, getString(R.string.activity_loaction_got_location_toast_text));
				setTextInTextViewOnUIThread(uiLocationCodeTextView, response.getCountryNameShort());
				setTextInTextViewOnUIThread(uiLocationLongNameTextView, response.getCountryNameLong());
				setTextInTextViewOnUIThread(uiLocationNumCodeTextView, response.getCountryCode());
			}
		});
	}
}