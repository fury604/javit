/**
 * This is a Yenc capable decoder class.
 * 
 * Richard Stride 2003
 *
 */
package net.nexxus.decode;

import java.io.*;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class YDecoder {

    private InputStreamReader isr;
    private BufferedReader breader;
    private ByteArrayOutputStream output;
    private FileOutputStream fos;
    private static String ENCODING = "ISO-8859-1";
    private static Logger log = LogManager.getLogger(YDecoder.class.getName());

    public YDecoder() {
    };

    public YDecoder(InputStream is) throws Exception {
        isr = new InputStreamReader(is, ENCODING);
        breader = new BufferedReader(isr);
        output = new ByteArrayOutputStream();
    }

    /**
     * determine if the given file 
     * really is a Yenc file
     *
     * @return boolean
     */
    public boolean checkYenc(File cache) {
        try {
            FileInputStream fis = new FileInputStream(cache);
            isr = new InputStreamReader(fis, ENCODING);
            breader = new BufferedReader(isr);
            String line = breader.readLine();
            breader.close();
            isr.close();
            fis.close();
            if ( line.startsWith("=ybegin") ) {
                getFilename(line);
                return true;
            }
        } 
        catch (Exception e) {
        }
        return false;
    }

    /**
     * get the part number for a File
     * from the given line. On error return -1
     *
     * @param String line
     * @return int  part number
     */
    public int getPartNumber(String line) {
        if ( line == null ) {
            return -1;
        }
        StringTokenizer st = new StringTokenizer(line, " ");

        while (st.hasMoreElements()) {
            String token = st.nextToken();
            if (token.startsWith("part")) {
                String sub = token.substring(token.indexOf("=")+1);
                return Integer.parseInt(sub);
            }
        }
        return -1;
    } 

    /**
     * get the part number for the given File
     * for the Yenc document. Return -1 on error.
     *
     * @param File cache
     * @return int  part number
     */
    public int getPartNumber(File cache) {
        try {
            FileInputStream fis = new FileInputStream(cache);
            InputStreamReader myisr = new InputStreamReader(fis, ENCODING);
            BufferedReader br = new BufferedReader(myisr);
            String line = br.readLine();
            br.close();
            myisr.close();
            fis.close();
            return getPartNumber(line);
        } 
        catch (IOException ioe) {
            log.debug("failed getting yenc part number: " + ioe.getMessage());
        }

        return -1;
    }

    /**
     * return the filename of a Yenc document
     * from the given line.
     * 
     * @param String line
     * @return String file name
     */
    public String getFilename(String line) {
        return line.substring(line.indexOf("name")+4).trim().substring(1).trim();
    }

    /**
     * return the filename of a Yenc document
     * from the given File. Return an empty String on error
     *
     * @param File cache
     * @return String file name
     */
    public String getFilename(File cache) {
        try {
            FileInputStream fis = new FileInputStream(cache);
            InputStreamReader myisr = new InputStreamReader(fis, ENCODING);
            BufferedReader br = new BufferedReader(myisr);
            String line = br.readLine();
            br.close();
            myisr.close();
            fis.close();
            return getFilename(line);
        } 
        catch (IOException ioe) {
            log.debug("failed getting yenc filename: " + ioe.getMessage());
        }

        return new String();
    }

    /**
     * set the InputStream required by this decoder 
     * 
     * @param FileInputStream fis
     * @throws IOException
     */
    public void setInputStream(FileInputStream fis) throws IOException {
        isr = new InputStreamReader(fis, ENCODING);
        breader = new BufferedReader(isr);
    }

    /**
     * set the OutputStream for this decoder
     * generally this is the decoded file
     *
     * @param FileOutputStream
     * @throws IOException
     */
    public void setOutputStream(FileOutputStream fos) throws IOException {
        this.fos = fos;
    }

    /**
     * Once our InputStream and OutputStream have been
     * hooked up, we call this method to pump the data
     * between them to decode.
     *
     * @throws IOException
     */
    public void decode() throws IOException {
        // assume we start at beginning of file
        // read header lines then decode until =yend
        String header_line_one = breader.readLine();
        String header_line_two = breader.readLine();
        while (true) {
            String line = breader.readLine();
            if ( line == null ) {
                break;
            }
            if ( line.startsWith("=yend") ) {
                break;
            }

            byte[] myBytes = line.getBytes(ENCODING);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int i = 0; i < myBytes.length; i++) {
                byte b = myBytes[i];
                if (b == '=') {
                    b = (byte) (myBytes[++i] - 64);
                }
                baos.write((byte) (((int) b + 256 - 42) % 256));
            }
            fos.write(baos.toByteArray());
        }
    }

    public void inMemoryDecode() throws IOException {
        // assume we start at beginning of file
        // read header lines then decode until =yend
        String header_line_one = breader.readLine();
        String header_line_two = breader.readLine();

        while (true) {
            String line = breader.readLine();
            if ( line == null ) {
                break;
            }
            if ( line.startsWith("=yend") ) { 
                break;
            }
            byte[] myBytes = line.getBytes(ENCODING);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int i = 0; i < myBytes.length; i++) {
                byte b = myBytes[i];
                if (b == '=') {
                    b = (byte) (myBytes[++i] - 64);
                }
                baos.write((byte) (((int) b + 256 - 42) % 256));
            }
            output.write(baos.toByteArray());
        }
    }

    public byte[] getDecoded() throws Exception {
        this.inMemoryDecode();
        return output.toByteArray();
    }
}
