/**
 * This file is part of Javit.
 *
 * Javit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Javit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Javit.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2005-2016 Richard Stride <fury@nexxus.net>
 *
 * DecodeManager.java
 *
 * a Class to handle the sorting and decoding
 * of single and multipart binaries
 *
 */

package net.nexxus.decode;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.nntp.NntpArticlePartID;
import net.nexxus.task.Task;
import net.nexxus.task.TaskManager;
import net.nexxus.util.ApplicationConstants;
//import net.nexxus.task.TaskManager;
//import net.nexxus.util.ComponentManager;

public class DecodeManager implements Runnable {

	private static Logger log = LogManager.getLogger(DecodeManager.class.getName());
	private static DecodeManager ref;
	private List decodeQueue = Collections.synchronizedList(new ArrayList());
	private static Task currentTask;
	private static int SLEEP_TIME = 2000;
	private String cacheDir = ApplicationConstants.CACHE_DIR;
	private String downloadDir = ApplicationConstants.DOWNLOAD_DIR;

	// default c'tor, ensure singleton
	private DecodeManager() {}

	// provide method for an instance of this class
	public synchronized static DecodeManager getInstance() {
		if ( ref == null ) {
		    ref = new DecodeManager();
		}
		return ref;
	}

	//// add item to queue
	public synchronized void add(Task t) {
		log.debug("adding task");
		decodeQueue.add(t);
	}

    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
    }

    public void setDownloadDir(String downloadDir) {
        this.downloadDir = downloadDir;
    }
	
	/**
	 * decode singlepart article
	 */
	public void decodeSinglepart(NntpArticleHeader header) {
		header.setStatus(NntpArticleHeader.STATUS_DECODING);
		YDecoder yDec = new YDecoder();
		File cachefile = new File(cacheDir + 
				File.separator + header.getServer() + "." +
				header.getGroup() + "." + header.getID());
		try {
			FileInputStream fis = new FileInputStream(cachefile);
			if ( ! yDec.checkYenc(cachefile) ) { 
				throw new NotYencException();
			}
			File path = new File(downloadDir + File.separator + header.getGroup()); 
			checkPath(path); 
			File output = new File(path + File.separator + yDec.getFilename(cachefile));
			FileOutputStream fos = new FileOutputStream(output);
			yDec.setInputStream(fis);
			yDec.setOutputStream(fos);
			
			// now call decode
			try {
				yDec.decode(); 
				fos.flush(); 
				fos.close();
			} 
			catch (IOException ioe) {
				log.warn("io exception: " + ioe.getMessage());
				output.delete(); 
				fos.close(); 
				fis.close();
				cleanupCache(header);
				return;
			}
		} 
		catch (NotYencException nye) {
			File path = new File(downloadDir + File.separator + header.getGroup()); 
			checkPath(path); 
			uudecodeSinglepart(header, cachefile);
		} 
		catch (IOException ioe) {
			log.warn("IO Exception: " + ioe.getMessage());
			ioe.printStackTrace();
			return;
		}    

		cleanupCache(header);

		//header.setStatus(NntpArticleHeader.STATUS_READ);
		// should generate HeaderChangeEvent here
		log.debug("total memory after decoding " + Runtime.getRuntime().totalMemory());
	}
	
	/**
	 * decode multipart article
	 */
	public void decodeMultipart(NntpArticleHeader header) {
		header.setStatus(NntpArticleHeader.STATUS_DECODING);
		YDecoder yDec = new YDecoder();
		try { 									
			// order our parts
			NntpArticlePartID[] ids = header.getParts();
			int[] orderparts = new int[ids.length];
			for (int x=0; x < ids.length; x++) {
			    NntpArticlePartID partID = ids[x];
			    File myfile = this.getCacheFile(header, partID);
				if (! yDec.checkYenc(myfile) || yDec.getPartNumber(myfile) == -1) {
					throw new NotYencException();
				}
				orderparts[yDec.getPartNumber(myfile)-1] = x;
			}

			// first part ?
			NntpArticlePartID firstPart = ids[orderparts[0]];
			File firstFile = this.getCacheFile(header, firstPart);
			FileInputStream fis = new FileInputStream(firstFile);
			
			//test if yEnc 
			if ( yDec.getFilename(firstFile) == null ) { 					
				log.error("couldn't get yEnc filename");
				throw new NotYencException();
			}
			String filename = yDec.getFilename(firstFile);
			fis.close();

			// create the path to our target
			File path = new File(downloadDir + File.separator + header.getGroup());
			checkPath(path);

			// create the target file in the path
			File target = new File(path + File.separator + filename);
			FileOutputStream fos = new FileOutputStream(target);
			yDec.setOutputStream(fos);

			// now decode in order
			//log.debug("decoding:  " + header.getSubject());
			long timer = System.currentTimeMillis();
			for (int n=0; n < orderparts.length; n++) {
	            NntpArticlePartID currentPart = ids[orderparts[n]];
	            File myfile = this.getCacheFile(header, currentPart);
				fis = new FileInputStream(myfile);
				yDec.setInputStream(fis);
				try {
					yDec.decode(); 
				} 
				catch (IOException ioe) {
					log.error("io exception: " + ioe.getMessage());
					cleanupCache(header); target.delete();
					return;
				} 
				catch (Exception wth) {
					log.error("what the hell happened downloading article?" + wth.getMessage());
					cleanupCache(header); target.delete();
					return;
				} 
				finally { 
					fis.close(); 
				}
			} // end of for

			log.debug("decoded file in : " + ((System.currentTimeMillis() - timer)/1000) );
			//log.info("finished downloading multipart article");
			// close our target file resource
			cleanupCache(header); 
			fos.flush(); 
			fos.close();
			header.setStatus(NntpArticleHeader.STATUS_READ);
		} 
		catch (NotYencException nye) {				
			//log.debug("not a yEnc encoding, possibly uuencoded " + nye.toString());
			long timer = System.currentTimeMillis();
			uudecodeMultipart(header);
			log.debug("decoded file in : " + ((System.currentTimeMillis() - timer)/1000) );
			cleanupCache(header);
			header.setStatus(NntpArticleHeader.STATUS_READ);
			return;
		}
		catch (IOException ioe) {
			log.error("ioe exception caught " + ioe.getMessage());
			cleanupCache(header);
			return;
		} 
		catch (ArrayIndexOutOfBoundsException oobe) {
			log.error("array oobe while sorting yenc parts: " + oobe.getMessage());
			cleanupCache(header);
			return;
		} 
	}

	/**
	 * run()
	 * 
	 * main method used as Thread payload
	 */
	public void run() {
		log.info("decoder started");
		while (true) {
			// if something in the queue
			if ( decodeQueue.size() > 0 ) {
				currentTask = (Task)decodeQueue.remove(0);
				NntpArticleHeader header = currentTask.getHeader();
				//log.debug("got header from queue");
				currentTask.setStatus(Task.DECODING);
				TaskManager.getInstance().updateTask(currentTask);

				if ( header.isMultipart() ) {
					log.debug("decoding multipart");
					decodeMultipart(header);
				}
				else {
					log.debug("decoding singlepart");
					decodeSinglepart(header);
				}
				TaskManager.getInstance().removeTask(currentTask);
			}
			else {
				try {
					Thread.sleep(SLEEP_TIME);
				}
				catch (InterruptedException ie) { 
					log.warn("decoder interrupted!"); 
				}
			}
		}
	}

	private File getCacheFile(NntpArticleHeader header, NntpArticlePartID part) {
        File file = new File(cacheDir + File.separator + 
                header.getServer() + "." +
                header.getGroup() + "." + part.getIdAsString());
        return file;

	}
    /**
     * uudecodeSinglePart
     */
    private void uudecodeSinglepart(NntpArticleHeader header, File cachefile) {
        File outputPath = new File(downloadDir + File.separator + header.getGroup());
        UUDecoder uu = new UUDecoder(cachefile, outputPath);
        uu.decode();
    }

    /**
     * uudecodeMultipart(NntpArticleHeader)
     */
    private void uudecodeMultipart(NntpArticleHeader header) {
        UUDecoder uu = new UUDecoder();
        NntpArticlePartID[] ids = header.getParts();  // assume they are ordered
        // setup out output file
        NntpArticlePartID partID = ids[0];
        File cache = this.getCacheFile(header, partID);
        String filename = uu.getFilename(cache);
        // check for cases where we actually don't get a filename
        if ( filename.equals("") ) { 
            String subject = header.getSubject();
            try {
                int a = subject.indexOf("\"");
                int b = subject.indexOf("\"", a+1);
                filename = subject.substring(a+1,b); // StringOutOfBoundsException
            }
            catch (Exception e) {
                log.error("could not determine filename: " + e.getMessage());
                filename = subject;
            }
        }
        log.debug("got a filename of " + filename);

        File outputPath = new File(downloadDir + File.separator + header.getGroup());
        // check for folder existence
        checkPath(outputPath);
        File output = new File(downloadDir + File.separator + 
                header.getGroup() + File.separator + filename);
        // set up output stream
        try {
            DataOutputStream ostream = new DataOutputStream(new FileOutputStream(output)); 
            uu.setOutputStream(ostream);
            // iterate over parts decoding each
            for (int x=0; x < ids.length; x++) {
                log.debug("I GET : " + ids.length);
                NntpArticlePartID currentPart = ids[x];
                File myfile = this.getCacheFile(header, currentPart);
                BufferedReader istream = new BufferedReader(new FileReader(myfile));
                uu.setInputStream(istream); 
                uu.decodeMultipart();
                istream.close();
            }
            ostream.close();
        } 
        catch (FileNotFoundException fnfe) {
            log.error("no such file: " + fnfe.getMessage());
        } 
        catch (IOException ioe) {
            log.error("got io exception in uudecoding " + ioe.getMessage());
        }
    }

    //////////////////////////////////////
    // utility methods
    //////////////////////////////////////

    //// checkPath
    private void checkPath(File path) {
        try {
            if (!path.exists()) {
                if (!path.mkdir()) {
                    log.warn("failed creating group path " + path.toString());
                }
            }
        } 
        catch (SecurityException se) {
            log.warn("exception creating path " + path.toString());
        }
    }

    //// cleanupCache
    private void cleanupCache(NntpArticleHeader header) {
        log.debug("cleaning up cache files");
        try {
            NntpArticlePartID[] ids = header.getParts();
            if ( (ids != null)  && (ids.length > 0) ) {
                for (int x=0; x < ids.length; x++) {
                    try {
                        NntpArticlePartID currentPart = ids[x];
                        File cachefile = new File(cacheDir + File.separator + header.getServer() + "." +
                                header.getGroup() + "." + currentPart.getIdAsString());
                        if ( cachefile.exists() ) {
                            log.debug("is writable: " + cachefile.canWrite());
                            if (!cachefile.delete()) {
                                log.warn("failed removing cachefile: " + cachefile.toString());
                            }
                        }
                    } 
                    catch (SecurityException se) {
                        log.warn("caught exception removing cachefile: " + se.getMessage());
                    }
                }
            } 
            else {
                try {
                    File cachefile = new File(cacheDir + File.separator + header.getServer() + "." +
                            header.getGroup() + "." + header.getID());
                    if ( cachefile.exists() ) {
                        log.debug("is writable: " + cachefile.canWrite());
                        if (!cachefile.delete()) { 
                            log.warn("failed removing cachefile: " + cachefile.toString());
                        }
                    }
                } 
                catch (SecurityException se) {
                    log.warn("caught exception removing cachefile: " + se.getMessage());
                }
            }
        } 
        catch (NullPointerException npe) {
            log.error("what happened, null pointer exception: " + npe.getMessage());
        }
    }
	
}
