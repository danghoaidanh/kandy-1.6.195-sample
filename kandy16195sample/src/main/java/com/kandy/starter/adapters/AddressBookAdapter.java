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
package com.kandy.starter.adapters;

import java.util.List;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.addressbook.IKandyContact;
import com.genband.kandy.api.services.addressbook.KandyEmailContactRecord;
import com.genband.kandy.api.services.addressbook.KandyPhoneContactRecord;
import com.kandy.starter.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AddressBookAdapter extends ArrayAdapter<IKandyContact>
{
	int layoutResId;
	
	public AddressBookAdapter(Context context, int resource, List<IKandyContact> items)
	{
		super(context, resource, items);
		this.layoutResId = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		if (convertView == null)
		{
			Context context = Kandy.getApplicationContext();
			convertView = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(layoutResId, null);//parent, false);

			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.ab_device_contact_name);
			holder.numbersTitle = (TextView) convertView.findViewById(R.id.ab_device_contact_numbers_title);
			holder.emailsTitle = (TextView) convertView.findViewById(R.id.ab_device_contact_emails_title);
			holder.numbers =  (TextView) convertView.findViewById(R.id.ab_device_contact_numbers);
			holder.emails =  (TextView) convertView.findViewById(R.id.ab_device_contact_emails);
			
			convertView.setTag(holder);
			
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		IKandyContact contact = getItem(position);
		holder.name.setText(contact.getDisplayName());
		
		holder.numbersTitle.setVisibility(View.GONE);
		holder.numbers.setVisibility(View.GONE);
		holder.emailsTitle.setVisibility(View.GONE);
		holder.emails.setVisibility(View.GONE);
		
		List<KandyPhoneContactRecord> numbers = contact.getNumbers();
		if (numbers != null && !numbers.isEmpty())
		{
			holder.numbersTitle.setVisibility(View.VISIBLE);
			String text = "";
			for (KandyPhoneContactRecord number : numbers)
			{
				text += number + "\n";
			}
			holder.numbers.setText(text);
			holder.numbers.setVisibility(View.VISIBLE);
		}
		
		List<KandyEmailContactRecord> emails = contact.getEmails();
		if (emails != null && !emails.isEmpty())
		{
			holder.emailsTitle.setVisibility(View.VISIBLE);
			String text = "";
			for (KandyEmailContactRecord email : emails)
			{
				text += email + "\n";
			}
			holder.emails.setText(text);
			holder.emails.setVisibility(View.VISIBLE);
		}
		
		return convertView;
	}
	
	private static class ViewHolder {
		TextView name;
		TextView numbersTitle;
		TextView emailsTitle;
		TextView numbers;
		TextView emails;
	}
	
	public void updateList(List<IKandyContact> contacts)
	{
		clear();
		if (contacts != null)
		{
			addAll(contacts);
		}
	}


}
