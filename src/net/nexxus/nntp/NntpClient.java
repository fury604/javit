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
 *  Simple NNTP Client Interface
 */

package net.nexxus.nntp;

import java.io.File;
import java.util.ArrayList;

public interface NntpClient {

    // NNTP commands
    public static final String NNTP_CMD_MODE_READER       = "MODE READER";
    public static final String NNTP_CMD_LIST_OVERVIEW_FMT = "LIST OVERVIEW.FMT";
    public static final String NNTP_CMD_LIST              = "LIST";
    public static final String NNTP_CMD_LISTGROUP         = "LISTGROUP";
    public static final String NNTP_CMD_GROUP             = "GROUP";
    public static final String NNTP_CMD_XOVER             = "XOVER";
    public static final String NNTP_CMD_XHDR              = "XHDR";
    public static final String NNTP_CMD_HEAD              = "HEAD";
    public static final String NNTP_CMD_STAT              = "STAT";
    public static final String NNTP_CMD_BODY              = "BODY";
    public static final String NNTP_CMD_ARTICLE           = "ARTICLE";
    public static final String NNTP_CMD_AUTHUSER          = "AUTHINFO USER";
    public static final String NNTP_CMD_AUTHPASS          = "AUTHINFO PASS";
    
    // NNTP RESPONSES
    public static final int NNTP_RESP_POSTING_OK                = 200;
    public static final int NNTP_RESP_POSTING_NOT_OK            = 201;
    public static final int NNTP_RESP_GROUP_SELECTED            = 211;
    public static final int NNTP_RESP_INFO_FOLLOWS              = 215;
    public static final int NNTP_RESP_ARTICLE_HEAD_BODY_FOLLOWS = 220;
    public static final int NNTP_RESP_HEADER_FOLLOWS            = 221;
    public static final int NNTP_RESP_ARTICLE_BODY_FOLLOWS      = 222;
    public static final int NNTP_RESP_ARTICLE_TEXT_SEPARATE     = 223;
    public static final int NNTP_RESP_XOVER_INFO_FOLLOWS        = 224;
    public static final int NNTP_RESP_TOO_MANY_CONNECTIONS      = 400;
    public static final int NNTP_RESP_NO_SUCH_GROUP             = 411;
    public static final int NNTP_RESP_NO_GROUP_SELECTED         = 412;
    public static final int NNTP_RESP_NO_ARTICLE_SELECTED       = 420;
    public static final int NNTP_RESP_NO_SUCH_ARTICLE_NUMBER    = 423;
    public static final int NNTP_RESP_NO_SUCH_ARTICLE           = 430;
    public static final int NNTP_RESP_GROUP_NOT_AVAILABLE	    = 480;
    public static final int NNTP_RESP_SYNTAX_ERROR              = 500;
    public static final int NNTP_RESP_NO_PERMISSION             = 502;
    public static final int NNTP_RESP_PROGRAM_ERROR             = 503;
    
    public static String[] XOVER_HDRS = {
        "id",
        "Subject",
        "From",
        "Date",
        "Message ID",
        "Bytes",
        "Lines",
        "Xref"
    };

    public void connect() throws NntpException;

    public void connect(NntpServer nServer) throws NntpException;

    public void disconnect() throws NntpException;

    public boolean isConnected();

    public ArrayList<NntpGroup> getGroupList(NntpServer nServer) throws NntpException;

    public NntpGroup setGroup(NntpGroup grp) throws NntpException;

    public void getHeaders(NntpGroup grp) throws Exception;

    public ArrayList<Integer> getGroupIDs(NntpGroup group);

    public void getArticleBody(NntpArticlePartID part, File cache) throws NntpException;
    
    // for NZB support 
    public byte[] getArticleBody(long msgID) throws NntpException;

    // for NZB support
   	public void verifyHeader(String msgId) throws Exception;
    
}