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
import java.util.List;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.groups.IKandyGroupDestroyed;
import com.genband.kandy.api.services.groups.IKandyGroupParticipantJoined;
import com.genband.kandy.api.services.groups.IKandyGroupParticipantKicked;
import com.genband.kandy.api.services.groups.IKandyGroupParticipantLeft;
import com.genband.kandy.api.services.groups.IKandyGroupUpdated;
import com.genband.kandy.api.services.groups.KandyGroup;
import com.genband.kandy.api.services.groups.KandyGroupParams;
import com.genband.kandy.api.services.groups.KandyGroupResponseListener;
import com.genband.kandy.api.services.groups.KandyGroupServiceNotificationListener;
import com.genband.kandy.api.services.groups.KandyGroupsResponseListener;
import com.kandy.starter.adapters.GroupAdapter;
import com.kandy.starter.utils.UIUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class KandyGroupsActivity extends Activity implements OnItemClickListener, KandyGroupServiceNotificationListener{

	private List<KandyGroup> mGroups;

	private GroupAdapter  mGroupAdapter;

	private EditText uiGroupNameEdit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kandy_groups);

		initViews();
		
		getListOfGroups();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Kandy.getServices().getGroupService().registerNotificationListener(KandyGroupsActivity.this);
	}

	@Override
	protected void onPause()
	{
		Kandy.getServices().getGroupService().unregisterNotificationListener(KandyGroupsActivity.this);
		super.onPause();
	}

	private void initViews() {
		mGroups = new ArrayList<KandyGroup>();
		mGroupAdapter = new GroupAdapter(KandyGroupsActivity.this, R.layout.group_list_item, mGroups);
		uiGroupNameEdit = (EditText)findViewById(R.id.ui_activity_groups_list_group_name_edit);
		
		((ListView)findViewById(R.id.activity_groups_list)).setAdapter(mGroupAdapter);
		((ListView)findViewById(R.id.activity_groups_list)).setOnItemClickListener(KandyGroupsActivity.this);

		((Button)findViewById(R.id.ui_activity_groups_list_group_create_btn)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				createGroup();
			}

		});
	}

	private void createGroup() {
		UIUtils.showProgressDialogWithMessage(KandyGroupsActivity.this, getString(R.string.groups_list_activity_create_group_msg));
		KandyGroupParams params = new KandyGroupParams();
		String name = uiGroupNameEdit.getText().toString();
		params.setGroupName(name);
		
		Kandy.getServices().getGroupService().createGroup(params, new KandyGroupResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String error) {
				UIUtils.handleResultOnUiThread(KandyGroupsActivity.this, true, error);
			}

			@Override
			public void onRequestSucceded(KandyGroup kandyGroup) {
				mGroups.add(kandyGroup);
				updateUI();
				UIUtils.handleResultOnUiThread(KandyGroupsActivity.this, false, getString(R.string.groups_list_activity_group_created));
			}
		});
	}
	
	private void getListOfGroups() {

		UIUtils.showProgressDialogWithMessage(KandyGroupsActivity.this, getString(R.string.groups_list_activity_get_groups_msg));
		Kandy.getServices().getGroupService().getMyGroups(new KandyGroupsResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String error) {
				UIUtils.handleResultOnUiThread(KandyGroupsActivity.this, true, error);
			}

			@Override
			public void onRequestSucceded(List<KandyGroup> groupList) {
				mGroups.clear();
				mGroups.addAll(groupList);
				updateUI();
				UIUtils.dismissProgressDialog();
			}
		});
	}

	private void updateUI() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mGroupAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		KandyGroup group = (KandyGroup)mGroupAdapter.getItem(position);
		String groupId = group.getGroupId().getUri();
		Intent intent = new Intent(KandyGroupsActivity.this , GroupDetailsActivity.class);
		intent.putExtra(GroupDetailsActivity.GROUP_ID, groupId);
		startActivity(intent);

	}

	@Override
	public void onGroupDestroyed(IKandyGroupDestroyed message)
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				getListOfGroups();
			}
		});
		
	}

	@Override
	public void onGroupUpdated(IKandyGroupUpdated message)
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				getListOfGroups();
			}
		});
	}

	@Override
	public void onParticipantJoined(IKandyGroupParticipantJoined message)
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				getListOfGroups();
			}
		});
	}

	@Override
	public void onParticipantKicked(IKandyGroupParticipantKicked message)
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				getListOfGroups();
			}
		});
	}

	@Override
	public void onParticipantLeft(IKandyGroupParticipantLeft message)
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				getListOfGroups();
			}
		});
	}
}
