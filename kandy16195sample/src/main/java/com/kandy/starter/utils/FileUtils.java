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
package com.kandy.starter.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

public class FileUtils
{
	private static String[] mSupportedFiles = new String[]{".png", ".pdf" };
	
	public static void copyAssets(Context context, File targetDir) {
	    AssetManager assetManager = context.getAssets();
	    String[] files = null;
	    try {
	        files = assetManager.list("");
	    } catch (IOException e) {
	        Log.e("tag", "Failed to get asset file list.", e);
	    }
	    for(String filename : files) {
	       
			if (isSupported(filename))
			{
				InputStream in = null;
				OutputStream out = null;
				try
				{
					in = assetManager.open(filename);

					File outFile = new File(targetDir, filename);

					out = new FileOutputStream(outFile);
					copyFile(in, out);
				}
				catch (IOException e)
				{
					Log.e("tag", "Failed to copy asset file: " + filename, e);
				}
				finally
				{
					if (in != null)
					{
						try
						{
							in.close();
						}
						catch (IOException e)
						{
							Log.e("tag", "Failed to close inputstream: " + filename, e);
						}
					}
					if (out != null)
					{
						try
						{
							out.close();
						}
						catch (IOException e)
						{
							Log.e("tag", "Failed to close outputstream: " + filename, e);
						}
					}
				}
			}
		}
	}

	public static File getFilesDirectory(String name)
	{
		File file = new File(Environment.getExternalStorageDirectory(), name);
		if(!file.exists())
		{
			file.mkdirs();
		}
		return file;
	}
	
	private static void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}
	
	private static boolean isSupported(String fileName)
	{
		for (int i = 0; i < mSupportedFiles.length; i++)
		{
			if(fileName.endsWith(mSupportedFiles[i]))
			{
				return true;
			}
		}
		return false;
	}
	
	public static void clearDirectory(File dir)
	{
		String[] list = dir.list();
		if(list != null)
		{
			for (int i = 0; i < list.length; i++)
			{
				File file = new File(dir, list[i]);
				file.delete();
			}
		}
	}

}
