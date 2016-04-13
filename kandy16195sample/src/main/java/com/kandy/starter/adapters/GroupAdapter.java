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
import com.genband.kandy.api.services.groups.KandyGroup;
import com.genband.kandy.api.services.groups.KandyGroupParticipant;
import com.kandy.starter.GroupDetailsActivity;
import com.kandy.starter.R;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * {@link ArrayAdapter} for {@link KandyGroup}.
 * Used to handle and show the KandyGroup items in list and sets to each item 
 * the action onClick
 *
 */
public class GroupAdapter extends ArrayAdapter<KandyGroup> implements OnItemClickListener {

	public GroupAdapter(Context context, int resource, List<KandyGroup> items) {
		super(context, resource, items);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder;

		if(convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(R.layout.group_list_item, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.uiGroupNameText = (TextView)convertView.findViewById(R.id.ui_group_list_name);
			viewHolder.uiGroupParticipantsText = (TextView)convertView.findViewById(R.id.ui_group_list_participants_lbl);
			viewHolder.uiGroupParticipantsNumber = (TextView)convertView.findViewById(R.id.ui_group_list_participants_number);
			viewHolder.uiGroupCreatedAt = (TextView)convertView.findViewById(R.id.ui_group_list_created_at);
			viewHolder.uiGroupMuted = (TextView)convertView.findViewById(R.id.ui_group_list_muted);
			viewHolder.uiGroupPermissions = (TextView)convertView.findViewById(R.id.ui_group_list_permissions);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder)convertView.getTag();
		}

		KandyGroup kandyGroup = getItem(position);

		if(kandyGroup != null) {
			viewHolder.uiGroupNameText.setText(kandyGroup.getGroupName());

			int number = kandyGroup.getGroupParticipants().size();
			viewHolder.uiGroupParticipantsNumber.setText(String.valueOf(number));

			viewHolder.uiGroupCreatedAt.setText(kandyGroup.getCreationDate().toString());
			
			viewHolder.uiGroupMuted.setText(Boolean.toString(kandyGroup.isGroupMuted()));

			String permissions = "read only"; 
			String me = Kandy.getSession().getKandyUser().getUserId();
			List<KandyGroupParticipant> participants = kandyGroup.getGroupParticipants();
			for (KandyGroupParticipant participant : participants)
			{
				if (participant.getParticipant().getUri().equals(me) &&
						participant.isAdmin())
				{
					permissions = "can edit"; 
				}
			}
			viewHolder.uiGroupPermissions.setText(permissions);
		}

		return convertView;
	}

	static class ViewHolder {
		TextView uiGroupNameText;
		TextView uiGroupParticipantsText;
		TextView uiGroupParticipantsNumber;
		TextView uiGroupCreatedAt;
		TextView uiGroupMuted;
		TextView uiGroupPermissions;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		KandyGroup group = (KandyGroup)getItem(position);
		String groupId = group.getGroupId().getUri();
		Intent intent = new Intent(getContext(), GroupDetailsActivity.class);
		intent.putExtra(GroupDetailsActivity.GROUP_ID, groupId);
		getContext().startActivity(intent);
	}
}
