package com.kandy.starter;

import java.util.ArrayList;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.audio.IKandyAudioDevice;
import com.genband.kandy.api.services.audio.KandyAudioDeviceType;
import com.genband.kandy.api.services.audio.KandyAudioServiceNotificationListener;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.KandyCallState;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.calls.KandyView;
import com.genband.kandy.api.services.common.IKandyDomain;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.mpv.IKandyMultiPartyConferenceNotificationListener;
import com.genband.kandy.api.services.mpv.IKandyMultiPartyConferenceRoomDetails;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceInvite;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantHold;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantMute;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantUnHold;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantUnMute;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantJoined;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantLeft;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantNameChanged;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantRemoved;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantVideoDisabled;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceParticipantVideoEnabled;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceRoomDetailsListener;
import com.genband.kandy.api.services.mpv.KandyMultiPartyConferenceRoomRemoved;
import com.genband.kandy.api.utils.KandyError;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.kandy.starter.utils.UIUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.util.Log;

public class MultiPartyConferenceCallScreenActivity extends CallServiceActivity implements KandyAudioServiceNotificationListener, 
IKandyMultiPartyConferenceNotificationListener, OnClickListener
{

	private static final String TAG = MultiPartyConferenceCallScreenActivity.class.getSimpleName();

	public static final String CONFERENCE_ID = "conference_id";
	public static final String ROOM_NUMBER = "room_number";
	public static final String ROOM_PSTN_NUMBER = "room_pstn_number";
	public static final String ROOM_PIN_CODE = "room_pin_code";
	public static final String PARTICIPANT_NICK_NAME = "participant_nick_name";
	public static final String START_CONFERENCE_CALL_WITH_VIDEO ="start_conference_call_with_video";
	public static final String START_CONFERENCE_CALL_WITH_SPEAKER ="start_conference_call_with_speaker";


	private Button muteBtn;
	private Button unmuteBtn;
	private Button holdBtn;
	private Button unholdBtn;
	private Button enableVideoBtn;
	private Button disableVideoBtn;
	private EditText changeNameEditText;
	private Button changeNameBtn;
	private EditText inviteParticipantEditText;
	private Button inviteParticipantBtn;
	private Button removeParticipentBtn;
	private Button infoConferenceBtn;


	private String conferenceId;
	private String roomNumber;
	private String pstnNumber;
	private String pinCode;

	private String nickName;
	private Boolean startConferenceCallWithSpeaker;


	private KandyCallState mPrevState;

	protected OnCheckedChangeListener onSpeakerTButtonClicked = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked){
				Kandy.getServices().getAudioService().setAudioDevice(KandyAudioDeviceType.SPEAKER);
			}
			else{
				Kandy.getServices().getAudioService().setAudioDevice(KandyAudioDeviceType.EARPIECE);
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multi_party_converrence_calls);
		initViews();

		Intent intent = getIntent();
		conferenceId = intent.getStringExtra(CONFERENCE_ID);
		roomNumber = intent.getStringExtra(ROOM_NUMBER);
		pstnNumber = intent.getStringExtra(ROOM_PSTN_NUMBER);
		pinCode = intent.getStringExtra(ROOM_PIN_CODE);
		nickName = intent.getStringExtra(PARTICIPANT_NICK_NAME);
		mIsCreateVideoCall = intent.getBooleanExtra(START_CONFERENCE_CALL_WITH_VIDEO, false);
		startConferenceCallWithSpeaker = intent.getBooleanExtra(START_CONFERENCE_CALL_WITH_SPEAKER, false);

		//start conference call
		callRoom(roomNumber);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerMultiPartyConferenceListener();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterMultiPartyConferenceListener();
	}

	private void initViews()
	{
		uiLocalVideoView = (KandyView)findViewById( R.id.activity_calls_local_video_view );
		uiRemoteVideoView = (KandyView)findViewById( R.id.activity_calls_video_view );
		muteBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_mute_btn );
		unmuteBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_unmute_btn );
		holdBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_hold_btn );
		unholdBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_unhold_btn );
		enableVideoBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_enable_video_btn );
		disableVideoBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_disable_video_btn );
		changeNameEditText = (EditText)findViewById( R.id.activity_multi_party_conference_calls_action_change_name_edit );
		changeNameBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_change_name_btn );
		inviteParticipantEditText = (EditText)findViewById( R.id.activity_multi_party_conference_calls_action_invite_participant_edit );
		inviteParticipantBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_invite_participant_btn );
		removeParticipentBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_remove_participent_btn );
		infoConferenceBtn = (Button)findViewById( R.id.activity_multi_party_conference_calls_action_info_conference_btn );
		uiAudioStateTextView = (TextView)findViewById( R.id.activity_calls_state_audio_text );
		uiVideoStateTextView = (TextView)findViewById( R.id.activity_calls_state_video_text );
		uiCallsStateTextView = (TextView)findViewById( R.id.activity_calls_state_call_text );
		uiHoldTButton = (ToggleButton)findViewById( R.id.activity_calls_hold_tbutton );
		uiMuteTButton = (ToggleButton)findViewById( R.id.activity_calls_mute_tbutton );
		uiVideoTButton = (ToggleButton)findViewById( R.id.activity_calls_video_tbutton );
		uiSwitchCameraTButton = (ToggleButton)findViewById( R.id.activity_calls_switch_camera_tbutton );

		uiHangupButton = (ImageView)findViewById(R.id.activity_calls_hangup_button);
		uiHangupButton.setOnClickListener(onHangupButtonClicked);

		uiHoldTButton = (ToggleButton)findViewById(R.id.activity_calls_hold_tbutton);
		uiHoldTButton.setOnCheckedChangeListener(onHoldTButtonClicked);

		uiMuteTButton = (ToggleButton)findViewById(R.id.activity_calls_mute_tbutton);
		uiMuteTButton.setOnCheckedChangeListener(onMuteTButtonClicked);

		uiVideoTButton = (ToggleButton)findViewById(R.id.activity_calls_video_tbutton);
		uiVideoTButton.setOnCheckedChangeListener(onVideoTButtonClicked);

		uiSwitchCameraTButton = (ToggleButton )findViewById(R.id.activity_calls_switch_camera_tbutton);
		uiSwitchCameraTButton.setOnCheckedChangeListener(onSwitchCameraTButtonClicked);

		uiSpeakerButton = (ToggleButton)findViewById(R.id.activity_calls_call_route_speaker);
		uiSpeakerButton.setOnCheckedChangeListener(onSpeakerTButtonClicked);


		muteBtn.setOnClickListener( this );
		unmuteBtn.setOnClickListener( this );
		holdBtn.setOnClickListener( this );
		unholdBtn.setOnClickListener( this );
		enableVideoBtn.setOnClickListener( this );
		disableVideoBtn.setOnClickListener( this );
		changeNameBtn.setOnClickListener( this );
		inviteParticipantBtn.setOnClickListener( this );
		removeParticipentBtn.setOnClickListener( this );
		infoConferenceBtn.setOnClickListener( this );

		setAudioState(mIsMute);
		setVideoState(false, false);
		setCallState(KandyCallState.INITIAL.name());


		KandyCallState callState = mCurrentCall != null ? mCurrentCall.getCallState() : KandyCallState.INITIAL;
		setCallSettingsOnUIThread(callState);
	}

	@Override
	public void onClick(View v) {
		if ( v == muteBtn ) {
			// Handle clicks for activityMultiPartyConferenceCallsActionMuteBtn
		} else if ( v == unmuteBtn ) {
			// Handle clicks for activityMultiPartyConferenceCallsActionUnmuteBtn
		} else if ( v == holdBtn ) {
			// Handle clicks for activityMultiPartyConferenceCallsActionHoldBtn
		} else if ( v == unholdBtn ) {
			// Handle clicks for activityMultiPartyConferenceCallsActionUnholdBtn
		} else if ( v == enableVideoBtn ) {
			// Handle clicks for activityMultiPartyConferenceCallsActionEnableVideoBtn
		} else if ( v == disableVideoBtn ) {
			// Handle clicks for activityMultiPartyConferenceCallsActionDisableVideoBtn
		} else if ( v == changeNameBtn ) {
			// Handle clicks for activityMultiPartyConferenceCallsActionChangeNameBtn
		} else if ( v == inviteParticipantBtn ) {
			// Handle clicks for activityMultiPartyConferenceCallsActionInviteParticipantBtn
		} else if ( v == removeParticipentBtn ) {
			// Handle clicks for activityMultiPartyConferenceCallsActionRemoveParticipentBtn
		} else if ( v == infoConferenceBtn ) {
			getRoomDeatiles();
		}
	}

	private void registerMultiPartyConferenceListener() {
		Log.d(TAG, "registerMultiPartyConferenceListener()");
		Kandy.getServices().getMultiPartyConferenceService().registerNotificationListener(MultiPartyConferenceCallScreenActivity.this);
	}

	private void unregisterMultiPartyConferenceListener() {
		Log.d(TAG, "unregisterMultiPartyConferenceListener()");
		Kandy.getServices().getMultiPartyConferenceService().unregisterNotificationListener(MultiPartyConferenceCallScreenActivity.this);
	}

	public void callRoom(String roomNumber)
	{
		KandyRecord roomRecord = createKandyRecordWithDomain(roomNumber);
		if(roomRecord != null)
		{
			doCall(roomRecord.getUri());
		}
	}


	private KandyRecord createKandyRecordWithDomain(String number) {
		IKandyDomain kandyDomain = Kandy.getSession().getKandyDomain();
		if(kandyDomain == null)
		{
			Log.e(TAG,"createKandyRecordWithDomain:  missing domain" );
			return null;
		}
		String kandyDomainName = kandyDomain.getName();

		KandyRecord record = null;
		try
		{
			record = new KandyRecord(number,kandyDomainName);
		}
		catch (KandyIllegalArgumentException e)
		{
			Log.e(TAG,"createKandyRecordWithDomain: "  + e.getLocalizedMessage());
		}
		return record;
	}

	



	@Override
	public void onAudioRouteChanged(IKandyAudioDevice activeAudioDevice, KandyError error) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAvailableAudioRoutesChanged(ArrayList<IKandyAudioDevice> availableAudioDevices) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVideoStateChanged(IKandyCall call, boolean isReceivingVideo, boolean isSendingVideo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCallStateChanged(KandyCallState state, IKandyCall call) {
		// TODO Auto-generated method stub
		super.onCallStateChanged(state, call);

		switch (state)
		{
		case INITIAL:

			break;
		case TALKING:
			if(mPrevState != KandyCallState.TALKING)
			{
				if(startConferenceCallWithSpeaker && uiSpeakerButton.isChecked() == false)
				{
					//turn on speaker 
					uiSpeakerButton.performClick();
				}
				joinRoom();
			}
			break;
		case TERMINATED:
			if(mPrevState != KandyCallState.TERMINATED)
			{
				removeRoomBeforeLeavingRoom();
			}
		default:
			break;
		}
		mPrevState = state;
	}



	
	private void leaveRoom() 
	{
		UIUtils.showProgressDialogOnUiThread(MultiPartyConferenceCallScreenActivity.this, "Leaving conference room");

		Kandy.getServices().getMultiPartyConferenceService().leave(conferenceId, new KandyResponseListener()
		{

			@Override
			public void onRequestFailed(int responseCode, String err)
			{
				Log.e(TAG, "leaveRoom: KandyResponseListener() {...}:onRequestFailed:" + responseCode + " - " + err);
				UIUtils.dismissProgressDialog();
			}

			@Override
			public void onRequestSucceded()
			{
				Log.d(TAG, "leaveRoom:  KandyResponseListener() {...}:onRequestSucceded: " + " ");
				UIUtils.dismissProgressDialog();
				closeMultiPartyConferenceScreen();
			}
		});
	}


	private void joinRoom() 
	{
		Kandy.getServices().getMultiPartyConferenceService().join(conferenceId, nickName, new KandyResponseListener()
		{

			@Override
			public void onRequestFailed(int responseCode, String err)
			{
				Log.e(TAG, "joinRoom: KandyResponseListener() {...}:onRequestFailed:" + responseCode + " - " + err);
			}

			@Override
			public void onRequestSucceded()
			{
				Log.d(TAG, "joinRoom: KandyResponseListener() {...}:onRequestSucceded: " + " ");						
			}
		});
	}
	
	private void removeRoomBeforeLeavingRoom() 
	{
		UIUtils.showProgressDialogOnUiThread(MultiPartyConferenceCallScreenActivity.this, "closing conference call");
		
		Kandy.getServices().getMultiPartyConferenceService().getRoomDetails(conferenceId, new KandyMultiPartyConferenceRoomDetailsListener() {
			
			@Override
			public void onRequestFailed(int responseCode, String err) 
			{
				Log.e(TAG, "getRoomDetails:onRequestFailed:" + responseCode + " - " + err);
				UIUtils.dismissProgressDialog();
				leaveRoom();
			}
			
			@Override
			public void onRequestSuceeded( IKandyMultiPartyConferenceRoomDetails roomDetails) 
			{
				Log.d(TAG, "getRoomDetails:onRequestSuceeded:" + roomDetails);
				UIUtils.dismissProgressDialog();
				if(roomDetails.getParticipants().size() == 0)
				{
					destroyRoom();
				}
				else
				{
					leaveRoom();
				}
			}
		});	
	}

	public void destroyRoom()
	{
		UIUtils.showProgressDialogOnUiThread(MultiPartyConferenceCallScreenActivity.this, "Destroing conference room");
		Kandy.getServices().getMultiPartyConferenceService().destroyRoom(conferenceId, new KandyResponseListener() {
			
			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.e(TAG, "destroyRoom:onRequestFailed:" + responseCode + " - " + err);
				UIUtils.dismissProgressDialog();
				leaveRoom();
			}
			
			@Override
			public void onRequestSucceded() {
				Log.d(TAG, "destroyRoom:onRequestSuceeded:");
				UIUtils.dismissProgressDialog();
				closeMultiPartyConferenceScreen();
			}
		});	
	}
	
	
	public void getRoomDeatiles(){

		Kandy.getServices().getMultiPartyConferenceService().getRoomDetails(conferenceId, new KandyMultiPartyConferenceRoomDetailsListener()
		{

			@Override
			public void onRequestFailed(int responseCode, String err)
			{
				Log.e(TAG, "getRoomDeatiles:onRequestFailed: "+ " responseCode = "+responseCode+" err = "+err);
				final String message = "getRoomDeatiles "+" Failed "+ "responseCode = "+responseCode+" err = "+err;
				UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, true, message);

			}

			@Override
			public void onRequestSuceeded(IKandyMultiPartyConferenceRoomDetails roomDetails)
			{
				Log.d(TAG, "getRoomDeatiles:onRequestSuceeded: " + " roomDetails = "+roomDetails);
				final String message = "getRoomDeatiles "+" Succeded";

				UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);

			}
		});
	}


	private void closeMultiPartyConferenceScreen()
	{
		Log.d(TAG, "closeMultiPartyConferenceScreen: " + " ");
		finish();
	}
	/**
	 * IKandyMultiPartyConferenceNotificationListener
	 */

	@Override
	public void onInviteRecieved(KandyMultiPartyConferenceInvite multiPartyConferenceInvite) 
	{
		String message = "Invite to conference "+multiPartyConferenceInvite.getRoom().getConferenceId()+" Recieved";
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantMute(KandyMultiPartyConferenceParticipantMute participantMute) 
	{
		String message = "Participant "+participantMute.getParticipantId()+" Mute";
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantUnMute(KandyMultiPartyConferenceParticipantUnMute participantUnMute) 
	{
		String message = "Participant "+participantUnMute.getParticipantId()+" UnMute";
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantJoinedRoom(KandyMultiPartyConferenceParticipantJoined participantJoinedRoom) 
	{
		String message = "Participant "+participantJoinedRoom.getParticipantId()+" Joined Room";
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantLeftRoom(KandyMultiPartyConferenceParticipantLeft participantLeftRoom)
	{
		String message = "Participant "+participantLeftRoom.getParticipantId()+" Left Room";
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantNameChanged(KandyMultiPartyConferenceParticipantNameChanged participantNameChanged) 
	{
		String message = "Participant "+participantNameChanged.getParticipantId()+" Name Changed to "+participantNameChanged.getNewName();
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantVideoEnabled(KandyMultiPartyConferenceParticipantVideoEnabled participantVideoEnable) 
	{
		String message = "Participant "+participantVideoEnable.getParticipantId()+" Video Enable";
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantVideoDisabled(KandyMultiPartyConferenceParticipantVideoDisabled participantVideoDisabled) 
	{
		String message = "Participant "+participantVideoDisabled.getParticipantId()+" Video Disabled";
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantHold(KandyMultiPartyConferenceParticipantHold participantHold) 
	{
		String message = "Participant "+participantHold.getParticipantId()+" Hold";
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantUnHold(KandyMultiPartyConferenceParticipantUnHold participantUnHold) 
	{
		String message = "Participant "+participantUnHold.getParticipantId()+" UnHold";
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onParticipantRemoved(KandyMultiPartyConferenceParticipantRemoved participantRemoved) 
	{
		String message = "Participant "+participantRemoved.getParticipantId()+" Removed";
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}

	@Override
	public void onConferenceRoomRemoved(KandyMultiPartyConferenceRoomRemoved conferenceRoomRemoved) 
	{
		String message = "Room "+conferenceRoomRemoved.getConferenceId()+" Removed";
		UIUtils.handleResultOnUiThread(MultiPartyConferenceCallScreenActivity.this, false, message);
	}


}
