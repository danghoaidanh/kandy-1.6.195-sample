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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.addressbook.IKandyContact;
import com.genband.kandy.api.services.addressbook.KandyAddressBookServiceNotificationListener;
import com.genband.kandy.api.services.addressbook.KandyContactsListener;
import com.genband.kandy.api.services.addressbook.KandyDeviceContactsFilter;
import com.kandy.starter.adapters.AddressBookAdapter;
import com.kandy.starter.utils.UIUtils;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;

public class DeviceAddressBookActivity extends ListActivity implements KandyAddressBookServiceNotificationListener
{
	private static final String TAG = DeviceAddressBookActivity.class.getSimpleName();

	/**
	 * contacts list adapter
	 */
	AddressBookAdapter mAdapter;

	/**
	 * contacts list filter
	 */
	Set<KandyDeviceContactsFilter> mFilter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_addressbook);

		init();
		bindViews();
	}

	private void init()
	{
		mAdapter = new AddressBookAdapter(this, R.layout.list_item_device_addressbook, new ArrayList<IKandyContact>());
		setListAdapter(mAdapter);

		mFilter = new HashSet<KandyDeviceContactsFilter>();
		getDeviceContacts(mFilter);
		
	}

	private void bindViews()
	{
		CheckBox emails = (CheckBox) findViewById(R.id.radioButton_emails);
		emails.setOnCheckedChangeListener(mFilterListener);

		CheckBox favorite = (CheckBox) findViewById(R.id.radioButton_favorite);
		favorite.setOnCheckedChangeListener(mFilterListener);

		CheckBox phones = (CheckBox) findViewById(R.id.radioButton_phones);
		phones.setOnCheckedChangeListener(mFilterListener);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		registerNotificationListener();
	}

	@Override
	protected void onPause()
	{
		unregisterNotificationListener();
		super.onPause();
	}

	
	private void registerNotificationListener() {
		Kandy.getServices().getAddressBookService().registerNotificationListener(this);
	}
	
	private void unregisterNotificationListener() {
		Kandy.getServices().getAddressBookService().unregisterNotificationListener(this);
	}
	
	@Override
	protected void onListItemClick(ListView list, View view, int position, long id)
	{
		Log.d(TAG, "onListItemClick: position: " + position + " id: " + id);
	}

	private void getDeviceContacts(Set<KandyDeviceContactsFilter> filter)
	{
		UIUtils.showProgressDialogWithMessage(DeviceAddressBookActivity.this, getString(R.string.loading));
		KandyDeviceContactsFilter[] items = filter.toArray(new KandyDeviceContactsFilter[filter.size()]);
		Kandy.getServices().getAddressBookService().getDeviceContacts(items, new KandyContactsListener()
		{

			@Override
			public void onRequestFailed(int responseCode, String err)
			{
				UIUtils.handleResultOnUiThread(DeviceAddressBookActivity.this, true, err);
			}

			@Override
			public void onRequestSucceded(final List<IKandyContact> contacts)
			{
				runOnUiThread(new Runnable()
				{

					@Override
					public void run()
					{
						mAdapter.updateList(contacts);
						UIUtils.dismissProgressDialog();
					}
				});
			}
		});
	}
	
	private OnCheckedChangeListener mFilterListener = new OnCheckedChangeListener()
	{

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			Log.d(TAG, "onCheckedChanged: id: " + buttonView.getId() + " isChecked: " + isChecked);

			buttonView.setChecked(isChecked);

			switch (buttonView.getId())
			{
			case R.id.radioButton_emails:
				if (isChecked)
				{
					mFilter.add(KandyDeviceContactsFilter.HAS_EMAIL_ADDRESS);
				}
				else
				{
					mFilter.remove(KandyDeviceContactsFilter.HAS_EMAIL_ADDRESS);
				}
				break;
			case R.id.radioButton_phones:
				if (isChecked)
				{
					mFilter.add(KandyDeviceContactsFilter.HAS_PHONE_NUMBER);
				}
				else
				{
					mFilter.remove(KandyDeviceContactsFilter.HAS_PHONE_NUMBER);
				}
				break;
			case R.id.radioButton_favorite:
				if (isChecked)
				{
					mFilter.add(KandyDeviceContactsFilter.IS_FAVORITE);
				}
				else
				{
					mFilter.remove(KandyDeviceContactsFilter.IS_FAVORITE);
				}
				break;
			}

			getDeviceContacts(mFilter);
		}
	};
	
	@Override
	public void onDeviceAddressBookChanged()
	{
		Log.d(TAG, "onDeviceAddressBookChanged: ");
		getDeviceContacts(mFilter);
	}
}
