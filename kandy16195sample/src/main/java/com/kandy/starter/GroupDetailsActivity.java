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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.chats.IKandyImageItem;
import com.genband.kandy.api.services.chats.IKandyTransferProgress;
import com.genband.kandy.api.services.chats.KandyThumbnailSize;
import com.genband.kandy.api.services.common.KandyGroupUploadProgressListener;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.common.KandyResponseProgressListener;
import com.genband.kandy.api.services.groups.IKandyGroupDestroyed;
import com.genband.kandy.api.services.groups.IKandyGroupParticipantJoined;
import com.genband.kandy.api.services.groups.IKandyGroupParticipantKicked;
import com.genband.kandy.api.services.groups.IKandyGroupParticipantLeft;
import com.genband.kandy.api.services.groups.IKandyGroupUpdated;
import com.genband.kandy.api.services.groups.KandyGroup;
import com.genband.kandy.api.services.groups.KandyGroupParams;
import com.genband.kandy.api.services.groups.KandyGroupParticipant;
import com.genband.kandy.api.services.groups.KandyGroupResponseListener;
import com.genband.kandy.api.services.groups.KandyGroupServiceNotificationListener;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.kandy.starter.adapters.GroupParticipantAdapter;
import com.kandy.starter.utils.UIUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class GroupDetailsActivity extends Activity implements KandyGroupServiceNotificationListener{

	public static final String GROUP_ID = "group_id";
	private static final int PICK_IMAGE = 10000;
	private List<KandyGroupParticipant> mParticipants;
	private GroupParticipantAdapter mAdapter;

	private KandyGroup mKandyGroup;

	private String mGroupId = null;
	private SimpleDateFormat mDateFormat;

	private EditText uiGroupNameEdit;
	private EditText uiGroupParticipantEdit;
	private TextView uiGroupCreatedAt;
	private TextView uiGroupMuted;
	private ImageView uiThumbnailImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_details);

		initViews();

		Intent intent = getIntent();

		if(intent != null) {
			mGroupId = intent.getStringExtra(GROUP_ID);

			if(mGroupId != null) {
				getSelectedGroup();
			}
		}
		
		mDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.group_settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings_leave) {
			leaveGroup();
			return true;
		} else if(id == R.id.action_settings_destroy) {
			destroyGroup();
			return true;
		} else if(id == R.id.action_settings_mute) {
			muteGroup(getKandyGroupInstance().isGroupMuted());
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		Kandy.getServices().getGroupService().registerNotificationListener(GroupDetailsActivity.this);
	}

	@Override
	protected void onPause()
	{
		Kandy.getServices().getGroupService().unregisterNotificationListener(GroupDetailsActivity.this);
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case PICK_IMAGE:
			if(resultCode == RESULT_OK) {
				Uri uri = data.getData();

				if(uri != null) {
					uploadSelectedImage(uri);
				}
			}
			break;
		}
	}

	private void initViews() {

		uiGroupCreatedAt = (TextView) findViewById(R.id.ui_activity_group_settings_group_created_at);
		uiGroupMuted = (TextView) findViewById(R.id.ui_activity_group_settings_group_muted);
		uiGroupNameEdit = (EditText)findViewById(R.id.ui_activity_group_settings_group_name_edit);
		uiGroupParticipantEdit = (EditText)findViewById(R.id.ui_activity_group_settings_add_participant_edit);
		uiThumbnailImage = (ImageView)findViewById(R.id.ui_group_thumbnail_img);
		
		uiGroupNameEdit.setOnEditorActionListener(new OnEditorActionListener()
		{
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				boolean handled = false;
		        if (actionId == EditorInfo.IME_ACTION_SEND) {
		        	updateGroupName(uiGroupNameEdit.getText().toString());
		            handled = true;
		        }
		        return handled;
			}
		});
		
		uiThumbnailImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				createImageActionsDialog().show();
			}
		});

		if(mParticipants == null) {
			mParticipants = new ArrayList<KandyGroupParticipant>();
			mAdapter = new GroupParticipantAdapter(GroupDetailsActivity.this, R.layout.participant_list_item, mParticipants);
		}

		((Button)findViewById(R.id.ui_activity_group_settings_chat_button)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GroupDetailsActivity.this, ChatServiceActivity.class);
				intent.putExtra(GROUP_ID, getKandyGroupInstance().getGroupId().getUri());
				startActivity(intent);			}
		});

		((Button)findViewById(R.id.ui_activity_group_settings_add_participant_button)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String participant = uiGroupParticipantEdit.getText().toString();
				addParticipant(participant);
			}
		});

		ListView list = (ListView)findViewById(R.id.ui_activity_group_settings_participants_list);
		list.setAdapter(mAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final KandyGroupParticipant participant = mAdapter.getItem(position);
				createParticipantActionsDialog(participant).show();
			}
		});
	}

	private void pickImage() {

		Intent pickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		pickerIntent.setType("image/*");

		startActivityForResult(pickerIntent, PICK_IMAGE);
	}

	private void updateGroupName(String string) {
		UIUtils.showProgressDialogWithMessage(GroupDetailsActivity.this, getString(R.string.groups_settings_activity_action_updating_group_name_));
		Kandy.getServices().getGroupService().updateGroupName(getKandyGroupInstance().getGroupId(), string, new KandyGroupResponseListener() {
			
			@Override
			public void onRequestFailed(int responseCode, String err) {
				UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, true, err);
			}
			
			@Override
			public void onRequestSucceded(KandyGroup kandyGroup) {
				setKandyGroupInstance(kandyGroup);
				updateUI();
				UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, false, getString(R.string.groups_settings_activity_action_group_name_changed));
			}
		});
	}
	
	private void uploadSelectedImage(Uri uri) {
		UIUtils.showProgressDialogWithMessage(GroupDetailsActivity.this, getString(R.string.groups_settings_activity_action_updating_group_image_));
		Kandy.getServices().getGroupService().updateGroupImage(getKandyGroupInstance().getGroupId(), uri, new KandyGroupUploadProgressListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, true, err);

			}

			@Override
			public void onRequestSucceded(KandyGroup kandyGroup) {
				setKandyGroupInstance(kandyGroup);
				updateImageUI();
			}

			@Override
			public void onProgressUpdate(IKandyTransferProgress progress) {
				Log.d("UPLOAD_IMAGE", "uploading image, progress: " + progress.getProgress());
			}
		});
	}

	private AlertDialog createImageActionsDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(GroupDetailsActivity.this);
		builder.setTitle(getString(R.string.groups_settings_activity_action_image_action_msg));

		builder.setItems(new String[]{"Remove Image", "Update Image"}, new android.content.DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				switch(which) {
				case 0:
					removeImage();
					break;

				case 1:
					pickImage();
					break;
				}

				dialog.dismiss();
			}
		});

		return builder.create();
	}

	private AlertDialog createParticipantActionsDialog(final KandyGroupParticipant participant) {

		final List<KandyRecord>participants = new ArrayList<KandyRecord>();
		participants.add(participant.getParticipant());

		AlertDialog.Builder builder = new AlertDialog.Builder(GroupDetailsActivity.this);
		builder.setTitle( getString(R.string.groups_settings_activity_action_participant_action_msg));
		String muteState = (participant.isMuted())?"Unmute Participant" : "Mute Participant";

		builder.setItems(new String[]{"Remove participant", muteState}, new android.content.DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				switch(which) {
				case 0:
					removeParticipants(participants);
					break;

				case 1:
					muteParticipants(participant.isMuted(), participants);
					break;
				}

				dialog.dismiss();
				UIUtils.showProgressDialogWithMessage(GroupDetailsActivity.this,  getString(R.string.groups_settings_activity_action_settings_applying_msg));
			}
		});

		return builder.create();
	}

	private void removeImage() {
		Kandy.getServices().getGroupService().removeGroupImage(getKandyGroupInstance().getGroupId(), new KandyGroupResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, true, err);
			}

			@Override
			public void onRequestSucceded(KandyGroup kandyGroup) {
				setKandyGroupInstance(kandyGroup);
				UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, false, getString(R.string.groups_settings_activity_action_image_has_been_removed));
				updateImageUI();
			}
		});
	}

	private void downloadImageThumbnail() {
		Kandy.getServices().getGroupService().downloadGroupImageThumbnail(getKandyGroupInstance().getGroupId(), KandyThumbnailSize.MEDIUM, new KandyResponseProgressListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, true, err);
			}

			@Override
			public void onRequestSucceded(final Uri fileUri) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						uiThumbnailImage.setImageURI(fileUri);
						UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, false, getString(R.string.groups_settings_activity_action_image_has_been_changed));
					}
				});
			}

			@Override
			public void onProgressUpdate(IKandyTransferProgress progress) {
				Log.d("DOWNLOAD_IMAGE", "download Image thumbnail, progress: " + progress.getProgress());
			}
		});
	}


	private void muteGroup(boolean groupMuted) {
		UIUtils.showProgressDialogWithMessage(GroupDetailsActivity.this, getString(R.string.groups_settings_activity_action_settings_applying_msg));
		if(groupMuted) {
			Kandy.getServices().getGroupService().unmuteGroup(getKandyGroupInstance().getGroupId(), new KandyGroupResponseListener() {

				@Override
				public void onRequestFailed(int responseCode, String err) {
					UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, true, err);
				}

				@Override
				public void onRequestSucceded(KandyGroup kandyGroup) {
					setKandyGroupInstance(kandyGroup);
					updateUI();
					UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, false, getString(R.string.groups_settings_activity_action_group_unmuted));
				}
			});
		} else {
			Kandy.getServices().getGroupService().muteGroup(getKandyGroupInstance().getGroupId(), new KandyGroupResponseListener() {

				@Override
				public void onRequestFailed(int responseCode, String err) {
					UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, true, err);
				}

				@Override
				public void onRequestSucceded(KandyGroup kandyGroup) {
					setKandyGroupInstance(kandyGroup);
					updateUI();
					UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, false, getString(R.string.groups_settings_activity_action_group_muted));
				}
			});
		}
	}

	private void destroyGroup() {
		UIUtils.showProgressDialogWithMessage(GroupDetailsActivity.this, getString(R.string.groups_settings_activity_action_settings_applying_msg));
		Kandy.getServices().getGroupService().destroyGroup(getKandyGroupInstance().getGroupId(), new KandyResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, true, err);
			}

			@Override
			public void onRequestSucceded() {
				UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, false, getString(R.string.groups_settings_activity_action_group_destroyed));
				finish();
			}
		});
	}

	private void leaveGroup() {
		UIUtils.showProgressDialogWithMessage(GroupDetailsActivity.this, getString(R.string.groups_settings_activity_action_settings_applying_msg));
		Kandy.getServices().getGroupService().leaveGroup(getKandyGroupInstance().getGroupId(), new KandyResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, true, err);
			}

			@Override
			public void onRequestSucceded() {
				UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, false, getString(R.string.groups_settings_activity_action_group_left));
			}
		});
	}

	private void removeParticipants(List<KandyRecord> participants) {
		Kandy.getServices().getGroupService().removeParticipants(getKandyGroupInstance().getGroupId(), participants, new KandyGroupResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {			
				UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, true, err);
			}

			@Override
			public void onRequestSucceded(KandyGroup kandyGroup) {
				setKandyGroupInstance(kandyGroup);
				updateUI();
				UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, false, getString(R.string.groups_settings_activity_action_participant_removed));
			}
		});
	}

	private void muteParticipants(boolean mute, List<KandyRecord> participants) {
		if(mute) {

			Kandy.getServices().getGroupService().unmuteParticipants(getKandyGroupInstance().getGroupId(), participants, new KandyGroupResponseListener() {

				@Override
				public void onRequestFailed(int responseCode, String err) {			
					UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, true, err);
				}

				@Override
				public void onRequestSucceded(KandyGroup kandyGroup) {
					setKandyGroupInstance(kandyGroup);
					updateUI();
					UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, false, getString(R.string.groups_settings_activity_action_participant_unmuted));
				}
			});


		} else {
			Kandy.getServices().getGroupService().muteParticipants(getKandyGroupInstance().getGroupId(), participants, new KandyGroupResponseListener() {

				@Override
				public void onRequestFailed(int responseCode, String err) {			
					UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, true, err);
				}

				@Override
				public void onRequestSucceded(KandyGroup kandyGroup) {
					setKandyGroupInstance(kandyGroup);
					updateUI();
					UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, false, getString(R.string.groups_settings_activity_action_participant_muted));
				}
			});
		}
	}

	private void addParticipant(String participant) {

		List<KandyRecord> newParticipants = new ArrayList<KandyRecord>();

		try {
			KandyRecord  newParticipant = new KandyRecord(participant);
			newParticipants.add(newParticipant);
		} catch (KandyIllegalArgumentException e) {
			UIUtils.showDialogWithErrorMessage(this, e.getMessage());
			return;
		}

		Kandy.getServices().getGroupService().addParticipants(getKandyGroupInstance().getGroupId(), newParticipants, new KandyGroupResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, true, err);
			}

			@Override
			public void onRequestSucceded(KandyGroup kandyGroup) {
				setKandyGroupInstance(kandyGroup);
				updateUI();
				UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, false, getString(R.string.groups_list_activity_participant_added_msg));
			}
		});

	}

	private synchronized void setKandyGroupInstance(KandyGroup kandyGroup) {
		mKandyGroup = kandyGroup;
	}

	private synchronized KandyGroup getKandyGroupInstance() {
		return mKandyGroup;
	}

	private void getSelectedGroup() {
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				UIUtils.showProgressDialogWithMessage(GroupDetailsActivity.this, getString(R.string.groups_settings_activity_action_getting_group_data));
				try
				{
					Kandy.getServices().getGroupService().getGroupById(new KandyRecord(mGroupId), new KandyGroupResponseListener() {
						
						@Override
						public void onRequestFailed(int responseCode, String error) {
							UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, true, error);
						}
						
						@Override
						public void onRequestSucceded(KandyGroup kandyGroup) {
							setKandyGroupInstance(kandyGroup);
							updateUI();
							updateImageUI();
						}
					});
				}
				catch (KandyIllegalArgumentException e)
				{
					UIUtils.showDialogWithErrorMessage(GroupDetailsActivity.this, e.getMessage());
				}
				
			}
		});
	}

	private void updateUI() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				String dateString = mDateFormat.format(getKandyGroupInstance().getCreationDate());
				uiGroupCreatedAt.setText("created at: " + dateString);
				uiGroupMuted.setText("is muted: " + Boolean.toString(getKandyGroupInstance().isGroupMuted()));
				uiGroupNameEdit.setText(getKandyGroupInstance().getGroupName());

				updateImageUI();
				mParticipants.clear();
				mParticipants.addAll(getKandyGroupInstance().getGroupParticipants());
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	private void updateImageUI() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				UIUtils.dismissProgressDialog();
				IKandyImageItem image = getKandyGroupInstance().getImage();
				if(image == null) {
					uiThumbnailImage.setImageResource(R.drawable.icon_photo);
				} else {
					if(image.getLocalThumbnailUri() == null) {
						downloadImageThumbnail();
					} else {
						uiThumbnailImage.setImageURI(image.getLocalThumbnailUri());
					}
				}
			}
		});
	}

	@Override
	public void onGroupDestroyed(IKandyGroupDestroyed message)
	{
		if (message.getGroupId().getUri().equals(getKandyGroupInstance().getGroupId().getUri()))
		{
			UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, false, getString(R.string.groups_settings_activity_action_group_destroyed));
			finish();
		}
	}

	@Override
	public void onGroupUpdated(IKandyGroupUpdated message)
	{
		if (message.getGroupId().getUri().equals(getKandyGroupInstance().getGroupId().getUri()))
		{
			boolean isNameUpdated = message.getGroupParams().getGroupName() != null; 
			if (isNameUpdated)
			{
				getKandyGroupInstance().setGroupName(message.getGroupParams().getGroupName());
				updateUI();
				UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, false, getString(R.string.groups_settings_activity_action_group_name_changed));
			}
			
			boolean isImageUpdated = message.getGroupParams().getGroupImage() != null; 
			if (isImageUpdated)
			{
				// is image updated?
				getKandyGroupInstance().setGroupImage(message.getGroupParams().getGroupImage());
				updateImageUI();
			}
			else 
			{
				getKandyGroupInstance().setGroupImage(message.getGroupParams().getGroupImage());
				boolean isImageRemoved = KandyGroupParams.REMOVE_IMAGE.equals(message.getGroupParams().getImageUri());
				// is image removed?
				if (isImageRemoved)
				{
					runOnUiThread(new Runnable()
					{
						
						@Override
						public void run()
						{
							uiThumbnailImage.setImageResource(R.drawable.icon_photo);
						}
					});
				}
			}
		}
	}

	@Override
	public void onParticipantJoined(IKandyGroupParticipantJoined message)
	{
		if (message.getGroupId().getUri().equals(getKandyGroupInstance().getGroupId().getUri()))
		{
			getSelectedGroup();
		}
	}

	@Override
	public void onParticipantKicked(IKandyGroupParticipantKicked message)
	{
		if (message.getGroupId().getUri().equals(getKandyGroupInstance().getGroupId().getUri()))
		{
			List<KandyRecord> booted = message.getBooted();
			boolean isMeKicked = false;
			for (KandyRecord record : booted)
			{
				String me = Kandy.getSession().getKandyUser().getUserId();
				if (record.getUri().equals(me))
				{
					UIUtils.handleResultOnUiThread(GroupDetailsActivity.this, false, getString(R.string.groups_settings_activity_action_group_kicked));
					finish();
					isMeKicked = true;
				}
			}
			
			if (!isMeKicked)
			{
				getSelectedGroup();
			}
		}
		
	}

	@Override
	public void onParticipantLeft(IKandyGroupParticipantLeft message)
	{
		if (message.getGroupId().getUri().equals(getKandyGroupInstance().getGroupId().getUri()))
		{
			getSelectedGroup();
		}
		
	}
}
