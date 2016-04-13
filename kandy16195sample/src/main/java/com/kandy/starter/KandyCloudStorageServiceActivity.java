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

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.chats.IKandyFileItem;
import com.genband.kandy.api.services.chats.IKandyTransferProgress;
import com.genband.kandy.api.services.chats.KandyMessageBuilder;
import com.genband.kandy.api.services.chats.KandyThumbnailSize;
import com.genband.kandy.api.services.common.KandyResponseCancelListener;
import com.genband.kandy.api.services.common.KandyResponseProgressListener;
import com.genband.kandy.api.services.common.KandyUploadProgressListener;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.kandy.starter.utils.FileUtils;
import com.kandy.starter.utils.UIUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class KandyCloudStorageServiceActivity extends Activity
{

	private static final String TAG = KandyCloudStorageServiceActivity.class.getSimpleName();
	public final static String LOCAL_STORAGE = "com.kandy.starter//Local storage";
	private ListView mTargetListView;
	private ListView mLocalFilesListView;
	private Button mDownloadButton;
	private Button mUploadButton;
	private File mPath = FileUtils.getFilesDirectory(LOCAL_STORAGE);
	private ClickableArrayAdapter mLocalFilesAdapter;
	private ClickableArrayAdapter mTargetFilesAdapter;
	private Map<String, String> mOnCloudFiles;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cloud_storage);
		mOnCloudFiles = new HashMap<String, String>();
		initViews();
		
	}

	private void initViews()
	{
		mTargetListView  = (ListView) findViewById(R.id.activity_cloud_storage_cloud_list_view);
		mLocalFilesListView  = (ListView) findViewById(R.id.activity_cloud_storage_local_files_list_view);
		mUploadButton  = (Button) findViewById(R.id.activity_cloud_storage_upload_button);
		mDownloadButton  = (Button) findViewById(R.id.activity_cloud_storage_download_button);
		mUploadButton.setEnabled(false);
		mDownloadButton.setEnabled(false);
		
		initLocalFilesList();
		initTargetDirectoryListView();
		initUploadButton();
		initDownloadButton();
	}
	
	private void initLocalFilesList()
	{
		mLocalFilesAdapter = new ClickableArrayAdapter(this, android.R.layout.simple_list_item_activated_1);
		mLocalFilesListView.setAdapter(mLocalFilesAdapter);
		mLocalFilesListView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				mUploadButton.setEnabled(true);
				mDownloadButton.setEnabled(false);
				mLocalFilesAdapter.setSelectedItem(position);
				mTargetFilesAdapter.setSelectedItem(-1);
			}
		});
	}
	
	
	private void initTargetDirectoryListView()
	{
		
		
		mTargetFilesAdapter = new ClickableArrayAdapter(this, android.R.layout.simple_list_item_activated_1);
		View emptyView = findViewById(android.R.id.empty);
		mTargetListView.setEmptyView(emptyView);
		mTargetListView.setAdapter(mTargetFilesAdapter);
		mTargetListView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				mUploadButton.setEnabled(false);
				mDownloadButton.setEnabled(true);
				mTargetFilesAdapter.setSelectedItem(position);
				mLocalFilesAdapter.setSelectedItem(-1);
			}
		});
	}


	private void initDownloadButton()
	{
		mDownloadButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View mV)
			{
				int position = mTargetFilesAdapter.getSelectedPosition();
				final String fileName = mTargetFilesAdapter.getItem(position);
//				downloadThumbnail(fileName);
				downloadFile(fileName);
				
			}

		});

	}
	
	
	private void initUploadButton()
	{
		mUploadButton.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View mV)
			{
				int position = mLocalFilesAdapter.getSelectedPosition();
				String item = mLocalFilesAdapter.getItem(position);
				uploadFile(item);
			}
		});
	}
	
	
	/**
	 * Download file
	 * @param fileName
	 */
	private void downloadFile(final String fileName)
	{
		final long timeStamp = Calendar.getInstance().getTimeInMillis();
		Uri fileUri = Uri.parse(mPath.getAbsolutePath() + "//" + timeStamp + fileName);
		final IKandyFileItem kandyFileItem;
		try
		{
			/*
			 * Kandy File Item has to be created in oder do be passed to KANDY download API
			 */
			kandyFileItem = KandyMessageBuilder.createFile("", fileUri);
			String uuidString = mOnCloudFiles.get(fileName);
			UUID serverDataUri = UUID.fromString(uuidString);
			kandyFileItem.setServerUUID(serverDataUri);
			
			CancelTransferClickListener listener = new CancelTransferClickListener(kandyFileItem);
			final ProgressDialog progressDialog = UIUtils.showCancelableProgressDialogOnUiThreadWithProgressAndButton(KandyCloudStorageServiceActivity.this,getString(R.string.cloud_storage_download_file), listener, listener);
			
			Kandy.getServices().geCloudStorageService().downloadMedia(kandyFileItem, new KandyResponseProgressListener()
			{
				
				@Override
				public void onRequestFailed(int mResponseCode, String err)
				{
					/*
					 * Download failed 
					 */
					UIUtils.handleResultOnUiThread(KandyCloudStorageServiceActivity.this, true, err);
				}
				
				@Override
				public void onRequestSucceded(Uri mFileUri)
				{
					/*
					 * Download succeeded, handle result 
					 */
					UIUtils.handleResultOnUiThread(KandyCloudStorageServiceActivity.this, false,
							getString(R.string.cloud_storage_download_suceeded, mFileUri));
					reloadLocalFiles();
				}
				
				@Override
				public void onProgressUpdate(IKandyTransferProgress progress)
				{
					/*
					 * Track progress and display on UI
					 */
					final int progressValue = progress.getProgress();
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							progressDialog.setProgress(progressValue);
						}
					});
					Log.d(TAG, "downloading , progress: " + progress.getProgress());
				}
			});
		}
		catch (KandyIllegalArgumentException e)
		{
			Log.d(TAG, "download failed: " + e.getLocalizedMessage());
		}
	}
	
	/**
	 * Download file's thumbnail
	 * @param fileName name of the file which thumbnaik to download
	 */
	private void downloadThumbnail(final String fileName)
	{
		final long timeStamp = Calendar.getInstance().getTimeInMillis();
		Uri fileUri = Uri.parse(mPath.getAbsolutePath() + "//" + timeStamp + fileName);
		final IKandyFileItem kandyFileItem;
		try
		{
			/*
			 * Kandy File Item has to be created in oder do be passed to KANDY download API
			 */
			kandyFileItem = KandyMessageBuilder.createFile("", fileUri);
			String uuidString = mOnCloudFiles.get(fileName);
			UUID serverDataUri = UUID.fromString(uuidString);
			kandyFileItem.setServerUUID(serverDataUri);
			
			CancelTransferClickListener listener = new CancelTransferClickListener(kandyFileItem);
			final ProgressDialog progressDialog = UIUtils.showCancelableProgressDialogOnUiThreadWithProgressAndButton(KandyCloudStorageServiceActivity.this,getString(R.string.cloud_storage_download_file), listener, listener);
			Kandy.getServices().geCloudStorageService().downloadMediaThumbnail(kandyFileItem, KandyThumbnailSize.MEDIUM, new KandyResponseProgressListener()
			{
				
				@Override
				public void onRequestFailed(int mResponseCode, String err)
				{
					/*
					 * Download failed 
					 */
					UIUtils.handleResultOnUiThread(KandyCloudStorageServiceActivity.this, true, err);
				}
				
				@Override
				public void onRequestSucceded(Uri mFileUri)
				{
					/*
					 * Download succeeded, handle result 
					 */
					UIUtils.handleResultOnUiThread(KandyCloudStorageServiceActivity.this, false,
							getString(R.string.cloud_storage_download_suceeded, mFileUri));
					reloadLocalFiles();
				}
				
				@Override
				public void onProgressUpdate(IKandyTransferProgress progress)
				{
					/*
					 * Track progress and display on UI
					 */
					final int progressValue = progress.getProgress();
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							progressDialog.setProgress(progressValue);
						}
					});
					Log.d(TAG, "downloading thumbnail, progress: " + progress.getProgress());
				}
			});
		}
		catch (KandyIllegalArgumentException e)
		{
			Log.d(TAG, "download thumbnail failed: " + e.getLocalizedMessage());
		}
	}

	
	private void uploadFile(final String fileName)
	{
		
		
		Uri fileUri = Uri.parse(mPath.getAbsolutePath() +"//" + fileName);
		final IKandyFileItem kandyFileItem;
		try
		{
			/*
			 * Kandy File Item has to be created in oder do be passed to KANDY download API
			 */
			kandyFileItem = KandyMessageBuilder.createFile("", fileUri);

			CancelTransferClickListener listener = new CancelTransferClickListener(kandyFileItem);
		final ProgressDialog progressDialog = UIUtils.showCancelableProgressDialogOnUiThreadWithProgressAndButton(KandyCloudStorageServiceActivity.this, getString(R.string.cloud_storage_uploding_file), listener, listener );
		Kandy.getServices().geCloudStorageService().uploadMedia(kandyFileItem , new KandyUploadProgressListener()
		{
			
			@Override
			public void onRequestFailed(int responseCode, String err)
			{
				/*
				 * Upload failed
				 */
				UIUtils.handleResultOnUiThread(KandyCloudStorageServiceActivity.this, true, err);
			}
			
			@Override
			public void onRequestSucceded()
			{
				/*
				 * Upload succeeded handle result
				 */
				UIUtils.handleResultOnUiThread(KandyCloudStorageServiceActivity.this, false, getString(R.string.cloud_storage_upload_suceeded));
				mOnCloudFiles.put(fileName, kandyFileItem.getServerUUID().toString());
				getServerFiles();
			}
			
			@Override
			public void onProgressUpdate(IKandyTransferProgress progress)
			{
				/*
				 * Track uploading process
				 */
				final int progressValue = progress.getProgress();
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						progressDialog.setProgress(progressValue);
					}
				});
				Log.d(TAG, "uploading , progress: " + progress.getProgress());
			}
		});
		
		}
		catch (KandyIllegalArgumentException e)
		{
			Log.d(TAG, "upload failed: " + e.getLocalizedMessage());
		}
		
	}
	

	
	
	private class ClickableArrayAdapter extends ArrayAdapter<String>
	{
		private int mSelectedPosition = -1;
		
		public ClickableArrayAdapter(Context context, int resource)
		{
			super(context, resource);
		}
		
		
		
		/**
		 * @param mMSelectedPosition the mSelectedPosition to set
		 */
		public void setSelectedItem(int position)
		{
			mSelectedPosition = position;
			notifyDataSetChanged();
		}
		
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = super.getView(position, convertView, parent);
			if (position == getSelectedPosition())
			{
				view.setActivated(true);
			}
			else
			{
				view.setActivated(false);
			}
			return view;
		}


		/**
		 * @return the mSelectedPosition
		 */
		public int getSelectedPosition()
		{
			return mSelectedPosition;
		}

	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		getServerFiles();
		reloadLocalFiles();
	}

	private void getServerFiles()
	{
		runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				Map<String, String> onCloudFiles = mOnCloudFiles;
				Set<String> list = onCloudFiles.keySet();
				mTargetFilesAdapter.clear();
				mTargetFilesAdapter.addAll(list);

			}
		});
	}
	
	
	private void reloadLocalFiles()
	{
		
			String[] list = mPath.list();
			final ArrayList<String> localFilesList = new ArrayList<String>();
			for (int i = 0; i < list.length; i++)
			{
				String fileName = list[i];
				localFilesList.add(fileName);
				
			}
			runOnUiThread(new Runnable()
			{
				public void run()
				{
					mLocalFilesAdapter.clear();
					mLocalFilesAdapter.addAll(localFilesList);
				}
			});
	}
	
	
	private class CancelTransferClickListener implements DialogInterface.OnCancelListener, DialogInterface.OnClickListener
	{

		private IKandyFileItem mKandyFileItem;
		public CancelTransferClickListener(IKandyFileItem kandyFileItem)
		{
			mKandyFileItem = kandyFileItem;
		}
		
		@Override
		public void onCancel(DialogInterface mDialog)
		{
			/*
			 * Call cancel API on KANDY SDK
			 */
			Kandy.getServices().geCloudStorageService().cancelMediaTransfer(mKandyFileItem, new KandyResponseCancelListener()
			{
				
				@Override
				public void onRequestFailed(int mResponseCode, String mErr)
				{
					/*
					 * Cancel failed
					 */
					Log.d(TAG, "file transfer was canceled");
					
				}
				
				@Override
				public void onCancelSucceded()
				{
					/*
					 * Cancel succeeded, handle result
					 */
					UIUtils.handleResultOnUiThread(KandyCloudStorageServiceActivity.this, false, getString(R.string.cloud_storage_transfer_succeeded));
					Log.d(TAG, "file transfer was canceled");
				}
			});
		}

		@Override
		public void onClick(DialogInterface mDialog, int mWhich)
		{
			mDialog.cancel();
		}
		
	}
}
