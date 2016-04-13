package com.kandy.starter;

import java.util.ArrayList;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceCreateAndInviteListener;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceFailedInvitees;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceInvitees;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceRoom;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceSuccessfullInvitees;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.kandy.starter.utils.UIUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

public class MultiPartyConferenceActivity extends Activity{

	protected static final String TAG = MultiPartyConferenceActivity.class.getSimpleName();
	private EditText addInviteesEditText;
	private Button addInviteesButton;
	private RadioGroup inviteByRadioGroup;
	private RadioButton inviteByChatRadioBtn;
	private EditText nicknameEdit;
	private ToggleButton startCallWithVideoToogle;
	private ToggleButton startCallWithSpeakerToogle;
	private Button startCallButton;
	private ListView inviteesList;

	private ArrayAdapter<String> inviteesAdapter;
	
	private ArrayList<KandyRecord> inviteesByChat;
	private ArrayList<String> inviteesBySMS;
	private ArrayList<String> inviteesByEmail;

	//list for list view
	private ArrayList<String> invitees;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multi_party_conference);
		initViews();
	}

	private void initViews() {
		addInviteesEditText = (EditText)findViewById( R.id.ui_activity_multi_party_conference_add_invitees_edit );
		addInviteesButton = (Button)findViewById( R.id.ui_activity_multi_party_conference_add_invitees_button );
		inviteByRadioGroup = (RadioGroup)findViewById( R.id.ui_activity_multi_party_conference_invite_by_radio_group );
		inviteByChatRadioBtn = (RadioButton)findViewById( R.id.ui_activity_multi_party_conference_invite_by_chat_radio_btn );
		nicknameEdit = (EditText)findViewById( R.id.ui_activity_multi_party_conference_nickname_edit );
		startCallWithVideoToogle = (ToggleButton)findViewById( R.id.ui_activity_multi_party_conference_start_call_with_video_toogle );
		startCallWithSpeakerToogle = (ToggleButton)findViewById( R.id.ui_activity_multi_party_conference_start_call_with_speaker_toogle );
		startCallButton = (Button)findViewById( R.id.ui_activity_multi_party_conference_start_call_button );
		inviteesList = (ListView)findViewById( R.id.ui_activity_group_settings_invitees_list );

		inviteesByChat = new ArrayList<KandyRecord>();
		inviteesBySMS = new ArrayList<String>();
		inviteesByEmail = new ArrayList<String>();

		invitees = new ArrayList<String>();
		inviteesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, invitees);
		inviteesList.setAdapter(inviteesAdapter); 
		
		
		addInviteesButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String inviteeUser = addInviteesEditText.getText().toString();

				addInvitee(inviteeUser);
			}
		});

		startCallButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startConferenceCall();
			}
		});
		//Default value
		inviteByChatRadioBtn.setChecked(true);
		startCallWithVideoToogle.setChecked(true);
		startCallWithSpeakerToogle.setChecked(true);
	}

	private void addInvitee(String inviteeUser) 
	{
		if (TextUtils.isEmpty(inviteeUser)) {
			UIUtils.showDialogWithErrorMessage(MultiPartyConferenceActivity.this, getString(R.string.multi_party_conference_invalid_invitee));
			return;
		}
		
		//get invite by method
		int id = findViewById(inviteByRadioGroup.getCheckedRadioButtonId()).getId();

		switch (id) {
		case  R.id.ui_activity_multi_party_conference_invite_by_chat_radio_btn:
			try {
				KandyRecord inviteKandyRecord = new KandyRecord(inviteeUser);
				inviteesByChat.add(inviteKandyRecord);
			} 
			catch (KandyIllegalArgumentException e) 
			{
				Log.e(TAG, "addInviteesButton: onClick: " + e.getLocalizedMessage());
				UIUtils.showDialogWithErrorMessage(MultiPartyConferenceActivity.this, getString(R.string.activity_chat_phone_number_verification_text));
				return;
			}
			break;
		case  R.id.ui_activity_multi_party_conference_invite_by_sms_radio_btn:
			inviteesBySMS.add(inviteeUser);
			break;
		case  R.id.ui_activity_multi_party_conference_invite_by_email_radio_btn:
			inviteesByEmail.add(inviteeUser);
			break;
		default:
			break;
		}
		invitees.add(inviteeUser);
		updateUI();
	}
	
	private void startConferenceCall() 
	{
		final String nickName = nicknameEdit.getText().toString();
		final boolean startCallWithVideo = startCallWithVideoToogle.isChecked();
		final boolean startCallWithSpeaker = startCallWithSpeakerToogle.isChecked();
		
		KandyMultiPartyConferenceInvitees kandyInvitees = new KandyMultiPartyConferenceInvitees();
		kandyInvitees.setInviteByChat(inviteesByChat);
		kandyInvitees.setInviteBySMS(inviteesBySMS);
		kandyInvitees.setInviteByMail(inviteesByEmail);

		UIUtils.showProgressDialogOnUiThread(MultiPartyConferenceActivity.this, getString(R.string.multi_party_conference_create_conference_message_dialog));
		Kandy.getServices().getMultiPartyConferenceService().createRoomAndInvite(kandyInvitees, new KandyMultiPartyConferenceCreateAndInviteListener() {
			
			@Override
			public void onRequestFailed(int responseCode, String err) 
			{
				Log.e(TAG, "createRoomAndInvite:onRequestFailed: " + " responseCode = "+responseCode+" err = "+err);
				UIUtils.dismissProgressDialog();
			}
			
			@Override
			public void onRequestSuceeded(KandyMultiPartyConferenceRoom createdRoom,
					KandyMultiPartyConferenceSuccessfullInvitees inviteesSeccess,
					KandyMultiPartyConferenceFailedInvitees failedInvite) 
			{
				Log.d(TAG, "createRoomAndInvite:onRequestSuceeded: " 
						+ " createdRoom = "+createdRoom
						+" inviteesSeccess = "+inviteesSeccess
						+" failedInvite = "+failedInvite);
				UIUtils.dismissProgressDialog();

				if(createdRoom != null)
				{
					Intent intent = new Intent(MultiPartyConferenceActivity.this, MultiPartyConferenceCallScreenActivity.class);
					intent.putExtra(MultiPartyConferenceCallScreenActivity.CONFERENCE_ID, createdRoom.getConferenceId());
					intent.putExtra(MultiPartyConferenceCallScreenActivity.ROOM_NUMBER, createdRoom.getRoomNumber());
					intent.putExtra(MultiPartyConferenceCallScreenActivity.ROOM_PIN_CODE, createdRoom.getRoomPinCode());
					intent.putExtra(MultiPartyConferenceCallScreenActivity.ROOM_PSTN_NUMBER, createdRoom.getRoomPSTNNumber());
					intent.putExtra(MultiPartyConferenceCallScreenActivity.PARTICIPANT_NICK_NAME, nickName);
					intent.putExtra(MultiPartyConferenceCallScreenActivity.START_CONFERENCE_CALL_WITH_SPEAKER, startCallWithSpeaker);
					intent.putExtra(MultiPartyConferenceCallScreenActivity.START_CONFERENCE_CALL_WITH_VIDEO, startCallWithVideo);

					startActivity(intent);
				}

			}
		});
	}
	
	private void updateUI() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				addInviteesEditText.setText("");
				startCallWithVideoToogle.setChecked(true);
				startCallWithSpeakerToogle.setChecked(true);
				inviteByChatRadioBtn.setChecked(true);
				inviteesAdapter.notifyDataSetChanged();
			}
		});
	}

}
