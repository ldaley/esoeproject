/* 
 * Copyright 2006, Queensland University of Technology
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy of 
 * the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 * 
 * Author: Shaun Mangelsdorf
 * Creation Date: 25/10/2006
 * 
 * Purpose: Maintains a background thread that collates all metadata from the database and collates to new metadata document.
 */
package com.qut.middleware.esoemanager.metadata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.MonitorThread;
import com.qut.middleware.saml2.exception.UnmarshallerException;

public class MetadataUpdateMonitor extends Thread implements MonitorThread
{
	private volatile boolean running;
	
	private MetadataCache metadataCache;
	private MetadataGenerator metadataGenerator;
	
	private int interval;
	private String historyDirectory;
	private String historicalFileName;
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(MetadataUpdateMonitor.class.getName());

	/**
	 * @param metadataCache
	 *            The metadata cache to manipulate with this thread
	 * @param historyDirectory Directory to store historical version of all metadata document changes
	 * @param historicalFileName Name to use when writing historical metadata archives to disk
	 * @param interval
	 *            The interval at which to refresh the metadata and update if it has changed, in seconds
	 */
	public MetadataUpdateMonitor(MetadataCache metadataCache, MetadataGenerator metadataGenerator, String historyDirectory, String historicalFileName, int interval) throws UnmarshallerException
	{
		super("ESOEManager Metadata update monitor"); //$NON-NLS-1$
		
		if(metadataCache == null)
		{
			this.logger.error("Supplied metadataCache was NULL for MetadataUpdateMonitor");
			throw new IllegalArgumentException("Supplied metadataCache was NULL for MetadataUpdateMonitor");
		}
		if(metadataGenerator == null)
		{
			this.logger.error("Supplied metadataGenerator was NULL for MetadataUpdateMonitor");
			throw new IllegalArgumentException("Supplied metadataGenerator was NULL for MetadataUpdateMonitor");
		}
		if(interval <= 0 || (interval > Integer.MAX_VALUE / 1000) )
		{
			throw new IllegalArgumentException("Supplied value for interval was invalid"); //$NON-NLS-1$
		}
				
		this.metadataCache = metadataCache;
		this.metadataGenerator = metadataGenerator;
		this.historyDirectory = historyDirectory;
		this.historicalFileName = historicalFileName;
		this.interval = interval * 1000;
				
		this.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{
		this.setRunning(true);		
		
		try
		{
			doGetMetadata();	
		}
		catch (Exception e)
		{
			this.logger.error("Creating metadata caused exception network will not be initialized \n" + e.getLocalizedMessage());
			this.logger.debug(e);
		}
			
		while (this.isRunning())
		{
			try
			{
				Thread.sleep(this.interval);
				
				this.logger.debug("Woke up about to do metadata update");
			
				// retrieve metadata and update if required
				doGetMetadata();
			}
			catch(InterruptedException e)
			{
				if(!this.isRunning())
					break;
			}
			catch (Exception e)
			{
				this.logger.error("Creating metadata caused exception metadata will not be updated \n" + e.getLocalizedMessage());
				this.logger.debug(e);
			}
		}
		
		this.logger.info("Terminating thread for class " + this.getName());
		
		return;
	}

	private void doGetMetadata() throws Exception
	{
		String cachedata = null;
			
		/* Generate new metadata instance */
		cachedata = this.metadataGenerator.generateMetadata();
		
		if(cachedata != null)
		{
			this.logger.debug("Successfully updated metadata");
			this.metadataCache.setCacheData(cachedata);
			writeHistory(cachedata);
			
			this.logger.debug("Metadata updated successfully");
			return;
		}
		this.logger.debug("Metadata update failed");
	}
	
	/**
	 * Attempts to write metadata that has been generated by this thread to cache file, if not successful does not throw fault and allows processing to continue
	 * archival, processing, cleanup is responsible of the operating system, these files are never read by this application.
	 * @param cachedata the data to write to disk
	 */
	private void writeHistory(String cachedata)
	{
		String filename = this.historyDirectory + File.separator + this.historicalFileName + System.currentTimeMillis() + Constants.METADATA_EXTENSION;
		try
		{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-16"));
			out.write(cachedata);
			out.flush();
			out.close();
		}
		catch (UnsupportedEncodingException e)
		{
			this.logger.error("UnsupportedEncodingException when attempting to write metadata history, not fatal continuing");
			this.logger.debug(e);
		}
		catch (FileNotFoundException e)
		{
			this.logger.error("FileNotFoundException when attempting to write metadata history, not fatal continuing");
			this.logger.debug(e);
		}
		catch (IOException e)
		{
			this.logger.error("IOException when attempting to write metadata history, not fatal continuing");
			this.logger.debug(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.MonitorThread#shutdown()
	 */
	public void shutdown()
	{
		this.setRunning(false);
		
		this.interrupt();
	}
	
	/**
	 * @return
	 */
	protected synchronized boolean isRunning()
	{
		return this.running;
	}
	
	/**
	 * @param running
	 */
	protected synchronized void setRunning(boolean running)
	{
		this.running = running;
	}		
}