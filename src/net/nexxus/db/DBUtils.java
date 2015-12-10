package net.nexxus.db;

import java.util.HashMap;
import java.util.Map;

import net.nexxus.nntp.NntpArticleHeader;
import net.nexxus.util.DateHelper;

public class DBUtils {
    
    public static String convertGroup(String group) {
        return group.replaceAll("\\.|\\-", "_");
    }

    public static HashMap mapHeader(NntpArticleHeader myHeader)
    throws Exception {
        HashMap header = new HashMap();
        
        header.put("table", DBUtils.convertGroup(myHeader.getGroup()));
        header.put("id", myHeader.getID());
        header.put("subject", myHeader.getSubject());
        header.put("frm", myHeader.getFrom());
        header.put("post_date", DateHelper.parse(myHeader.getDate()));
        header.put("status", myHeader.getStatus());
        header.put("msgid", myHeader.getMsgID());
        header.put("bytes", myHeader.getBytes());
        header.put("parts", myHeader.getPartsAsJSON());
        header.put("total_parts", myHeader.getTotalParts());
        header.put("server", myHeader.getServer());
        header.put("port", myHeader.getPort());
        header.put("group", myHeader.getGroup());
        
        return header;
    }
    
    public static HashMap mapHeaderForUpdate(NntpArticleHeader myHeader)
    throws Exception {
        HashMap header = new HashMap();
        
        header.put("table", DBUtils.convertGroup(myHeader.getGroup()));
        header.put("id", myHeader.getID());
        header.put("status", myHeader.getStatus());
        
        return header;
    }

    public static NntpArticleHeader mapHeader(Map map) 
    throws Exception {
        
        NntpArticleHeader header = new NntpArticleHeader();
        header.setID((long)map.get("id"));
        header.setSubject((String)map.get("subject"));
        header.setFrom((String)map.get("frm"));
        header.setDate((String)map.get("post_date"));
        header.setStatus((String)map.get("status"));
        header.setMsgID((String)map.get("msgid"));
        header.setBytes((String)map.get("bytes"));
        header.setTotalParts((int)map.get("total_parts"));
        header.setPartsFromJSON((String)map.get("parts"));
        header.setServer((String)map.get("server"));
        header.setPort((int)map.get("port"));
        header.setGroup((String)map.get("server_group"));
        
        return header;
        
    }
}
