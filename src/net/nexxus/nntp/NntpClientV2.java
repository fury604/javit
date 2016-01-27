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
 */

/**
 *  NntpClient.java
 *
 *  simple news client implementation
 *  to interact with an NntpServer
 *
 */

package net.nexxus.nntp;

import java.net.Socket;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.File;
import javax.swing.event.EventListenerList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.nexxus.db.DBManager;
import net.nexxus.db.DBManagerImpl;
//import net.nexxus.cache.CacheManager;
//import net.nexxus.cache.CacheManagerV2;
//import net.nexxus.cache.CacheManagerV3;
//import net.nexxus.util.ApplicationProperties;
//import net.nexxus.util.ComponentManager;
//import net.nexxus.search.SearchManager;
//import net.nexxus.task.TaskManager;

import net.nexxus.event.*;

public class NntpClientV2 extends EventListenerImpl implements NntpClient {

	private static Logger log = LogManager.getLogger(NntpClientV2.class.getName());

	private String server;
	private int port;
	private String group;
	private String username;
	private String password;

	private Socket clientSocket;
	private DataInputStream input;
	private DataOutputStream output;
	private BufferedReader inputControl;

	private NntpGroup nGroup;
	private NntpServer nServer;
	//private CacheManager cm = ComponentManager.getCacheManager();
	DBManager dbManager; 
	private EventListenerList listenerList = new EventListenerList();
	private int responseCode;
	private int articleCount;
	private int firstArticleId;
	private int lastArticleId;
	private int bytesread = 0;

	// will match *[00/10]* or *(00/10)*
	private static Pattern pattern = Pattern
			.compile("^.*[\\(\\[](\\d+)\\/(\\d+)[\\)\\]].*");
	private Matcher regex;

	private static final int TIMEOUT = 60000;
	private static final String NEWLINE = "\r\n";
	private static final String LATIN = "ISO-8859-1";
	private static final int SLEEP = 3000;
	

	// c'tor taking NntpServer as argument
	public NntpClientV2(NntpServer srv, DBManager dbManager) {
		this.server = srv.getServer();
		this.port = srv.getPort();
		this.username = srv.getUsername();
		this.password = srv.getPassword();
		this.dbManager = dbManager;
	}

	/**
	 * connect to server
	 */
	public void connect() throws NntpException {
	    try {
	        clientSocket = new Socket(server, port);
	        clientSocket.setSoTimeout(TIMEOUT);

	        // create socket stream handlers
	        output = new DataOutputStream(clientSocket.getOutputStream());
	        input = new DataInputStream(clientSocket.getInputStream());
	        inputControl = new BufferedReader(new InputStreamReader(input));

	        // if not connected for some reason, panic
	        if (!clientSocket.isConnected())
	            throw new NntpException(
	                    "error: failed socket connection attempt");

	        while (true) {
	            if (inputControl.ready())
	                break;
	        }

	        String response = inputControl.readLine();
	        responseCodeHandler(response);
	        log.debug("connection response was: " + response);

	        // authenticate first
	        if (password != null && password.length() != 0) {
	            log.debug("authenticating");
	            String authCmd = new String(NNTP_CMD_AUTHUSER + " " + username
	                    + "\r\n");
	            output.write(authCmd.getBytes(), 0, authCmd.length());
	            response = inputControl.readLine();
	            authCmd = new String(NNTP_CMD_AUTHPASS + " " + password
	                    + "\r\n");
	            output.write(authCmd.getBytes(), 0, authCmd.length());
	            response = inputControl.readLine();
	            log.debug("authenticated");
	        }

	        // set mode to reader & wait until we can read
	        String modeReader = new String(NNTP_CMD_MODE_READER + "\r\n");
	        output.write(modeReader.getBytes(), 0, modeReader.length());
	        while (true) {
	            if (inputControl.ready())
	                break;
	        }
	        response = inputControl.readLine();
	        responseCodeHandler(response);

	    } 
	    catch (UnknownHostException e) {
	        throw new NntpException(e.toString());
	    } 
	    catch (IOException ioe) {
	        log.error("io error connecting to " + server);
	        throw new NntpException(ioe.toString());
	    } 
	    catch (Exception ne) {
	        throw new NntpException("error: " + ne.toString());
	    }
	}

	/**
	 * connect(NntpServer nServer)
	 * 
	 * connect to given NntpServer
	 * 
	 * @param nServer
	 * @throws NntpException
	 */
	public void connect(NntpServer nServer) throws NntpException {
		this.nServer = nServer;
		this.server = nServer.getServer();
		this.port = nServer.getPort();
		this.username = nServer.getUsername();
		this.password = nServer.getPassword();
		connect();
	}

	/**
	 * disconnect from server
	 */
	public void disconnect() throws NntpException {
		try {
			String disconnectCommand = new String("quit\r\n");
			output.write(disconnectCommand.getBytes(), 0,
					disconnectCommand.length());
			clientSocket.close();
		} 
		catch (Exception ioe) {
			throw new NntpException("failed closing socket " + ioe.toString());
		}
	}

	public boolean isConnected() {
		return clientSocket.isConnected();
	}

	public boolean isDisconnected() {
		return clientSocket.isClosed();
	}

	/**
	 * getGroupList()
	 * 
	 * return an ArrayList of group Strings
	 */
	public ArrayList<NntpGroup> getGroupList(NntpServer nServer) throws NntpException {
		String listCommand = new String(NNTP_CMD_LIST + NEWLINE);
		try {
			output.write(listCommand.getBytes(), 0, listCommand.length());
		} 
		catch (IOException ioe) {
			throw new NntpException("error: " + ioe.toString());
		}

		ArrayList<NntpGroup> groups = new ArrayList<NntpGroup>();
		try {
			while (true) {
				if (inputControl.ready())
					break;
			}
			String response = inputControl.readLine();
			responseCodeHandler(response); // throws Exception on fail

			// now collect grouplist
			while (true) {
				String line = inputControl.readLine();
				if (line.equals(".")) {
					break;
				}
				// parse line into an NntpGroup
				String[] tokens = line.split(" ");
				if (tokens != null && tokens.length >=3) {
				    String name = tokens[0];
				    long hi = 0L;
				    long low = 0L;
				    try {
				        hi = Long.parseLong(tokens[1]);
				        low = Long.parseLong(tokens[2]);
				    }
				    catch (Exception nfe) {
				        log.error("failed parsing long for group hi - low: " + nfe.getMessage());
				    }
				    
				    long count = hi - low;
				    if (count > 1) {
				        NntpGroup group = new NntpGroup();
				        group.setName(name);
				        group.setHighID(hi);
				        group.setLowID(low);
				        //group.setServer(nServer.getServer());
				        // add to ArrayList
				        groups.add(group);
				    }
				}
			}
		} 
		catch (IOException ioe) {
			log.error("got exception collecting groups " + ioe.toString());
			throw new NntpException(ioe.toString());
		}
		
		return groups;
	}

	/**
	 * setGroup(NntpGroup)
	 * 
	 * set current group to be NntpGroup returns an NntpGroup object with the
	 * servers current group information
	 * 
	 * @param grp
	 * @throws Exception
	 */
	public NntpGroup setGroup(NntpGroup grp) throws NntpException {
		String groupCommand = new String(NNTP_CMD_GROUP + " " + grp.getName()
				+ NEWLINE);
		try {
			output.write(groupCommand.getBytes(), 0, groupCommand.length());
		} 
		catch (IOException ioe) {
			throw new NntpException("error: " + ioe.toString());
		}

		try {
			while (true) {
				if (inputControl.ready())
					break;
			}
			String response = inputControl.readLine();
			log.debug("xover response: " + response);
			StringTokenizer tokenizer = new StringTokenizer(response);
			this.responseCode = Integer.parseInt(tokenizer.nextToken());
			// confirm response code
			if (this.responseCode == NNTP_RESP_NO_SUCH_GROUP ||
				this.responseCode == NNTP_RESP_GROUP_NOT_AVAILABLE) {

			    throw new NntpException("error: group not available: "
						+ grp.getName());
			}

			// should populate NntpGroup object and return
			grp.setCount(Long.parseLong(tokenizer.nextToken()));
			grp.setLowID(Long.parseLong(tokenizer.nextToken()));
			grp.setHighID(Long.parseLong(tokenizer.nextToken()));
		} 
		catch (Exception e) {
			throw new NntpException(e.toString());
		}
		
		this.nGroup = grp;
		return grp;
	}

	/**
	 * getHeaders
	 * 
	 * method to get article headers using XOVER command
	 * 
	 */
	public void getHeaders(NntpGroup grp) throws Exception {
	    
		if (!clientSocket.isConnected()) {
			throw new Exception("not connected to server");
		}
		
		long lastID = grp.getHighID();
		long grpCount = grp.getCount();
		setGroup(grp);

		log.debug("count is " + grp.getCount());
		log.debug("lowID is  " + grp.getLowID());
		log.debug("highID is  " + grp.getHighID());

		long grpTotal = grp.getCount();
		int num = 0;
		boolean isIncremental = false;
		long count = 0;

		// compose command, should use highID from supplied NntpGroup
		if (lastID > 0 && lastID <= grp.getHighID()) {
			String xoverCommand = new String(NNTP_CMD_XOVER + " " + lastID
					+ "- " + NEWLINE);
			output.write(xoverCommand.getBytes(), 0, xoverCommand.length());
			log.debug("sent XOVER command: " + xoverCommand);
			grpTotal = grp.getHighID() - lastID;
			isIncremental = true;
		} 
		else {
			String xoverCommand = new String(NNTP_CMD_XOVER + " "
					+ grp.getLowID() + "-" + NEWLINE); // + grp.getHighID()
			output.write(xoverCommand.getBytes(), 0, xoverCommand.length());
			log.debug("sent XOVER command: " + xoverCommand);
		}

		HashMap multipartMap = new HashMap();
		// wait for response
		while (true) {
			if (inputControl.ready())
				break;
		}
		String response = inputControl.readLine();
		responseCodeHandler(response); // throws Exception on fail

		while (true) {
			String testLine = inputControl.readLine();
			if (testLine == null || testLine.equals(".")) {
				log.debug("got . and now returning");
				break;
			}
			
			// dealing with XOVER output, tab delimited
			String[] header = testLine.split("\t+");

			if (header.length > 5) {
				// set up our initial regex matcher
				regex = pattern.matcher(header[1]);
				try {
					NntpArticleHeader currentHeader = new NntpArticleHeader();
                    currentHeader.setSource(grp.getServer(), grp.getPort(), grp.getName());
					regex.reset(header[1]); // set up our regex test on subject

					// look for multipart article
					if (regex.matches() && (regex.groupCount() > 1)
							&& (Integer.parseInt(regex.group(2)) > 1)) {

						// if more than 0 parts
						if (Integer.parseInt(regex.group(2)) > 0) {
							// mangle the header by removing the multipart info
							// header[1] is now a key for all parts
							header[1] = header[1].substring(0,
									(regex.start(1)) - 1);

							// inner loop consts
							int placeholder = Integer.parseInt(regex.group(1)) - 1;

							// part of an existing multipart?
							if (!multipartMap.containsKey(header[1])) {
							    addNewHeaderToMap(header, currentHeader, multipartMap, regex, placeholder);
							}
							// else we have this multipart already
							else {
                                addHeaderToMap(currentHeader, multipartMap, header, placeholder, grpCount, count, testLine);
							}

						}
					}
					// singlepart article
					else {
						currentHeader.setSource(grp.getServer(), grp.getPort(), grp.getName());
						currentHeader = buildArticleHeader(currentHeader, header);
						try {
						    dbManager.addHeader(currentHeader);
						}
						catch (Exception e) {
						    log.error("could not add header to DB: " + e.getMessage());
						}
						grpCount++;
						count++;
					}

					// fire event here to show we've download a header
					fireEvent(new HeaderDownloadedEvent(grp.getName(), num++, grpTotal));

				} 
				catch (ArrayIndexOutOfBoundsException aob) {
					//log.warn("caught exception composing NntpArticleHeader: "
					//		+ testLine + aob.toString());
				}
			}
		}

		log.debug("had " + multipartMap.size()
				+ " incomplete headers left over");
		multipartMap.clear();
	}

	public void verifyHeader(String msgId) throws Exception {
		try {
			BufferedReader buffered = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream(), LATIN));

			// compose command
			String bodyCommand = new String(NNTP_CMD_STAT + " " + msgId
					+ NEWLINE);
			output.write(bodyCommand.getBytes(), 0, bodyCommand.length());
			String response = buffered.readLine();
			responseCodeHandler(response);

		} 
		catch (Exception e) {
			throw e;
		}
	}

	public ArrayList<Integer> getGroupIDs(NntpGroup group) {
		ArrayList<Integer> ids = new ArrayList();
		try {
			BufferedReader buffered = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream(), LATIN));
			// compose command
			String bodyCommand = new String(NNTP_CMD_LISTGROUP + " "
					+ group.getName() + NEWLINE);
			output.write(bodyCommand.getBytes(), 0, bodyCommand.length());

			// wait for response
			while (true) {
				if (inputControl.ready())
					break;
			}
			String response = inputControl.readLine();
			responseCodeHandler(response); // throws Exception on fail

			// StringBuffer buf = new StringBuffer();
			// now collect grouplist
			while (true) {
				String line = inputControl.readLine();
				if (line.equals("."))
					break;
				ids.add(new Integer(line));
			}

		} 
		catch (Exception e) {
		}

		return ids;
	}


	
	
	
	// //////////////////
	// helper methods
	// //////////////////

	/**
	 * setGroup(String)
	 * private method using only String as arg
	 * @param group
	 * @throws NntpException
	 */
	private void setGroup(String group) throws NntpException {
		NntpGroup nGroup = new NntpGroup(server, port, group, 0);
		setGroup(nGroup);
	}

	/**
	 * responseCodeHandler
	 * method to handle NNTP response codes these should clean up before
	 * throwing
	 * N.B should add a method to handle download quota met situation
	 */
	private void responseCodeHandler(String responseCode) throws NntpException {
		if (responseCode.length() < 4) {
			log.debug("got malformed response code: " + responseCode);
			throw new NntpException("got malformed response code " +
					responseCode);
		}
		String code = responseCode.substring(0, 3);
		
		if (code == null) {
			throw new NntpException("nntp response code was null");
		}

		try {

			// case NNTP_RESP_NO_SUCH_GROUP
			if (Integer.parseInt(code) == NNTP_RESP_NO_SUCH_GROUP) {
				throw new NntpException("no such group " + group);
			}
			// case NNTP_RESP_NO_GROUP_SELECTED
			if (Integer.parseInt(code) == NNTP_RESP_NO_GROUP_SELECTED) {
				throw new NntpException("no group selected");
			}
			// case NNTP_RESP_NO_ARTICLE_SELECTED
			if (Integer.parseInt(code) == NNTP_RESP_NO_ARTICLE_SELECTED) {
				throw new NntpException("no article selected");
			}
			// case NNTP_RESP_NO_SUCH_ARTICLE
			if (Integer.parseInt(code) == NNTP_RESP_NO_SUCH_ARTICLE) {
			    NntpException nntpException = new NntpException("no such article");
			    nntpException.setResponseCode(NNTP_RESP_NO_SUCH_ARTICLE);
				throw nntpException;
			}
			// case NNTP_RESP_NO_SUCH_ARTICLE_NUMBER
			if (Integer.parseInt(code) == NNTP_RESP_NO_SUCH_ARTICLE_NUMBER) {
				NntpException nntpException = new NntpException("no such article id");
			    nntpException.setResponseCode(NNTP_RESP_NO_SUCH_ARTICLE_NUMBER);
				throw nntpException;
			}
			// case NNTP_RESP_NO_PERMISSION
			if (Integer.parseInt(code) == NNTP_RESP_NO_PERMISSION) {
				throw new NntpException("no permission");
			}
			// case NNTP_RESP_SYNTAX_ERROR
			if (Integer.parseInt(code) == NNTP_RESP_SYNTAX_ERROR) {
				throw new NntpException("syntax error in nntp command");
			}
			if (Integer.parseInt(code) == NNTP_RESP_TOO_MANY_CONNECTIONS) {
				try {
					Thread.sleep(TIMEOUT); // sleep to relax server a bit
				} catch (Exception e) {
				    // do nothing
				}
				throw new NntpException("too many connections");
			}
			
			//if (Integer.parseInt(code) == NNTP_RESP_NO_SUCH_ARTICLE_NUMBER) {
			//	throw new NntpException("no such article number");
			//}
			if (Integer.parseInt(code) == NNTP_RESP_PROGRAM_ERROR) {
				throw new NntpException("503 error - feature not supported?");
			}
		} 
		catch (NumberFormatException nfe) {
			log.error("got garbled response code: " + responseCode);
			throw new NntpException("response code error: " + responseCode);
		}
	}

	/**
	 * this method will build up an NntpArticleHeader from 
	 * an array of Strings that was returned as part of an 
	 * XOVER command
	 * 
	 * @param myHeader
	 * @param header
	 * @return
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public NntpArticleHeader buildArticleHeader(NntpArticleHeader myHeader,
			String[] header) throws ArrayIndexOutOfBoundsException {
		int x = 0;
		// normally 9 headers
		myHeader.setID(Long.valueOf(header[x++])); // 0
		myHeader.setSubject(header[x++]); // 1
		myHeader.setFrom(header[x++]); // 2
		myHeader.setDate(header[x++]); // 3
		myHeader.setMsgID(header[x++]); // 4
		if (header[x].indexOf("<") > -1) { // 5
			x++;
		}
		myHeader.setBytes(header[x++]); // 6
		myHeader.setLines(header[x++]); // 7

		return myHeader;
	}

    /**
     * Add a new Header into a Multipart Map
     * @param header
     * @param currentHeader
     * @param multipartMap
     * @param regex
     */
    public void addNewHeaderToMap(String[] header, NntpArticleHeader currentHeader, 
            HashMap multipartMap, Matcher regex, int placeholder ) {
        try {
            // order parts by regex.group(1) on header
            currentHeader.setAsMultipart(true);
            currentHeader.setTotalParts(Integer.parseInt(regex.group(2)));
            // add this header ID to the array for this multipart
            if (placeholder < 0) {
                placeholder = 0;
            }
            currentHeader.addMultiPartID(placeholder, Long.parseLong(header[0]), header[4]);
            currentHeader = buildArticleHeader(currentHeader, header);
            // add to multipart Map
            multipartMap.put(header[1], currentHeader);
        } 
        catch (ArrayIndexOutOfBoundsException obe) {
            log.warn("addNewHeaderToMap error: " + obe.toString());
            multipartMap.remove(header[1]);
        }
    }

    public void addHeaderToMap(NntpArticleHeader currentHeader, HashMap multipartMap,
            String[] header, int placeholder, long grpCount, long count, String line)
    throws Exception {
        
        try {
            // place id in array in proper order based
            // on posting part
            currentHeader = (NntpArticleHeader) multipartMap.get(header[1]);

            if (placeholder < 0) {
                placeholder = 0;
            }
            currentHeader.addMultiPartID(placeholder, Long.parseLong(header[0]), header[4]);

            // update bytes for this multipart header[5]
            try {
                int bytes = Integer.parseInt(currentHeader.getBytes());
                if (header[5].indexOf("<") > -1) {
                    bytes += Integer.parseInt(header[6]);
                } 
                else {
                    bytes += Integer.parseInt(header[5]);
                }
                currentHeader.setBytes(Integer.toString(bytes));
            } 
            catch (NumberFormatException nfe) {
                log.error("malformed header?");
            }

            // place into persistance store, removing from multipartMap
            if (currentHeader.isComplete()) {
                multipartMap.remove(header[1]);
                try {
                    dbManager.addHeader(currentHeader);
                }
                catch (Exception e) {
                    log.error("failed adding header to DB: " + e.getMessage());
                }
                grpCount++;
                count++;
            } 
            else { // else put it back in the map
                multipartMap.put(header[1], currentHeader);
            }

        } 
        catch (ArrayIndexOutOfBoundsException obe) {
            log.warn("addHeaderToMap error: " + obe.toString());
            log.debug("xover line was: " + line);
            log.debug("article subject: " + currentHeader.getSubject());
            log.debug("additional info parts length: " + currentHeader.getTotalParts() + 
                    " placeholder: " + placeholder + " with regex value " + regex.group(2));
            //obe.printStackTrace();
            multipartMap.remove(header[1]);
        }
    }
	
    /**
     * getArticleBody()
     * 
     * download article BODY only storing to File cache
     * we assume we are already connected to an NNTP server
     * and have the group set
     * 
     * @param long msgID
     * @param File
     *            cache
     * @throws NntpException
     */
    public void getArticleBody(NntpArticlePartID part, File cache) throws NntpException {
        // sanity checks
        if (this.isDisconnected()) {
            throw new NntpException("not connected");
        }
        if (this.nGroup == null) {
            throw new NntpException("group is not set");
        }

        // we are ready
        log.debug("downloading article id: " + part.getId());
        try {
            BufferedReader buffered = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream(), LATIN));

            // compose command
            String bodyCommand = new String(NNTP_CMD_BODY + " " + part.getId()
                    + NEWLINE);
            output.write(bodyCommand.getBytes(), 0, bodyCommand.length());
            String response = buffered.readLine();
            
            try { 
                responseCodeHandler(response);
            }
            catch (NntpException nntpException) {
                if (nntpException.getResponseCode() == NNTP_RESP_NO_SUCH_ARTICLE ||
                        nntpException.getResponseCode() == NNTP_RESP_NO_SUCH_ARTICLE_NUMBER) {
                    log.debug("failed retrieving article using ID");
                    log.debug("attempting to retrieve article using MSGID");
                    
                    bodyCommand = new String(NNTP_CMD_BODY + " " + part.getMsgID()
                            + NEWLINE);
                    output.write(bodyCommand.getBytes(), 0, bodyCommand.length());
                    response = buffered.readLine();
                    log.debug("msgid response was: " + response);
                    responseCodeHandler(response);
                }
                else {
                    throw nntpException;
                }
            }

            FileOutputStream fos = new FileOutputStream(cache);
            int lines = 1;

            while (true) {
                StringBuffer line = new StringBuffer(buffered.readLine());

                if (line.length() == 1 && line.charAt(0) == '.') {
                    fos.close();
                    return;
                }
                if (line.length() > 0 && line.charAt(0) == '.') {
                    line.deleteCharAt(0);
                    line.append(NEWLINE);
                    fos.write(line.toString().getBytes(LATIN));
                } 
                else if (line.length() > 0 && lines != 0) {
                    line.append(NEWLINE);
                    fos.write(line.toString().getBytes(LATIN));
                }
                
                lines++;
            }
        } 
        catch (IOException ioe) {
            log.error("IOException : " + ioe.getMessage());
            try {
                Thread.sleep(SLEEP);
            } catch (Exception e) {
            }
            throw new NntpException("IO error");
        } 
        catch (Exception ee) {
            log.error("exception : " + ee.toString());
            //ee.printStackTrace();
            try {
                Thread.sleep(SLEEP);
            } catch (Exception e) {
            }
            throw new NntpException(ee.getMessage());
        }
    }
    

	/** 
	 * get the Article Body of an NNTP article with the
	 * given msgID
	 */
	public byte[] getArticleBody(long msgID) throws NntpException {
        // sanity checks
        if (this.isDisconnected()) {
            throw new NntpException("not connected");
        }
        if (this.nGroup == null) {
            throw new NntpException("group is not set");
        }

        // we are ready
        log.debug("downloading article id: " + msgID);
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		try {
			BufferedReader buffered = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream(), LATIN));
			// compose command
			String bodyCommand = new String(NNTP_CMD_BODY + " " + msgID
					+ NEWLINE);
			output.write(bodyCommand.getBytes(), 0, bodyCommand.length());
			String response = buffered.readLine();
			responseCodeHandler(response);

			int lines = 1;
			while (true) {
				StringBuffer line = new StringBuffer(buffered.readLine());
				if (line.length() == 1 && line.charAt(0) == '.') {
					fos.close();
					return fos.toByteArray();
				}
				if (line.length() > 0 && line.charAt(0) == '.') {
					line.deleteCharAt(0);
					line.append(NEWLINE);
					fos.write(line.toString().getBytes(LATIN));
				} 
				else if (line.length() > 0 && lines != 0) {
					line.append(NEWLINE);
					fos.write(line.toString().getBytes(LATIN));
				}
				lines++;
			}
		} 
		catch (IOException ioe) {
			log.error("IOException : " + ioe.getMessage());
			try {
				Thread.sleep(SLEEP);
			} 
			catch (Exception e) {
			}
			throw new NntpException("IO error");
		} 
		catch (Exception ee) {
			log.error("unknown exception : " + ee.toString());
			try {
				Thread.sleep(SLEEP);
			} 
			catch (Exception e) {
			}
			throw new NntpException(ee.getMessage());
		}
	}

}
