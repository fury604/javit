/**
 * UUDecoder.java
 *
 * a class for decoding uuencoded data via
 * DataInput and DataOutput streams
 *
 * original code Copyright Henrik Bj�rkman 1998
 * modified for the needs of the world
 */

package net.nexxus.decode;

import java.io.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UUDecoder {

	private static Logger log = LogManager.getLogger(UUDecoder.class.getName());
	private BufferedReader in;
	private File infile;
	private File outputPath;
	private DataOutputStream out;

	// default constructor
	public UUDecoder() {}

	// full constructor
	public UUDecoder(File infile, File outputPath) {
		this.infile = infile;
		this.outputPath = outputPath;
	}

	/*
	 *  decode()
	 * 
	 * decode bytes from InputStream
	 * and write them to OutputStream
	 */
	public void decode() {
		String str;
		boolean more=true;
		int n=0;
		try {
			// set up our input stream
			in = new BufferedReader(new FileReader(infile));
			// suck it in
			while(more) {
				str = in.readLine();
				if (str == null) {
					more = false;
					break;
				}
				if ( str.startsWith("begin ") ) {  
					// pos 9 to end is file name, should remove leading and trailing whitespace
					String filename = str.substring(9).trim();
					File outputFile = new File(new String(outputPath.toString() + "/" + filename));
					if ( !outputFile.exists() ) {
						try { 
							outputFile.createNewFile(); 
						} 
						catch (SecurityException se) {
							log.error("cannot create output file " + outputFile.getName());
							break;
						}
					}
					if (filename.length()==0) {
						break;
					}
					out = new DataOutputStream(new FileOutputStream(outputFile));

					// loop       
					int v = 0;
					for(;;) {
						str = in.readLine();
						if (str == null) {
							more = false;
							break;
						}
						if (str.equals("end")) {
							break;
						}

						int pos=1;
						int d=0;
						ByteArrayOutputStream baos = new ByteArrayOutputStream();

						int len=((str.charAt(0)&0x3f)^0x20);
						while ((d+3<=len) && (pos+4<=str.length())) {
							decodeString3(str.substring(pos,pos+4), baos );
							pos+=4;
							d+=3;
						}            
						if ((d+2<=len) && (pos+3<=str.length())) {
							decodeString2(str.substring(pos,pos+3), baos);
							pos+=3;
							d+=2;
						}            
						if ((d+1<=len) && (pos+2<=str.length())) {
							decodeString1(str.substring(pos,pos+2), baos);
							pos+=2;
							d+=1;
						}            
						if (d!=len) {
							log.error("did not get all parts for decoding");
						}

						baos.writeTo(out);
						baos.reset();
					} // end of for(;;)
					out.close();
					n++;
				}
			}
		} 
		catch (IOException e) {
			log.error("exception caught uudecoding "+e);
		} 
		finally { 
			try {
				if (in!=null) {in.close();}
				if (out!=null) {out.close();}
			} 
			catch (IOException e) {
				log.error("exception caught closing file resources in uudecode: "+e);
			}
		}
	} 

	/*
	 * decodeMultipart()
	 * 
	 * decode from InputStream
	 * and write to OutputStream
	 * 
	 * caller invokes multiple times, each part is assigned to InputStream
	 * OutputStream stays constant
	 */
	public void decodeMultipart() {
		for (;;) {
			try {
				String line = in.readLine();
				if (line == null || line.length() < 1) {
				    break;
				}
				if (line.equals("end")) {
				    break;
				}
				if (!line.startsWith("begin ")) {
					int pos=1;
					int d=0;
					int len=((line.charAt(0)&0x3f)^0x20);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();

					while ((d+3<=len) && (pos+4<=line.length())) {
						decodeString3(line.substring(pos,pos+4), baos );
						pos+=4;
						d+=3;
					}            
					if ((d+2<=len) && (pos+3<=line.length())) {
						decodeString2(line.substring(pos,pos+3), baos);
						pos+=3;
						d+=2;
					}            
					if ((d+1<=len) && (pos+2<=line.length())) {
						decodeString1(line.substring(pos,pos+2), baos);
						pos+=2;
						d+=1;
					}            
					if (d!=len) {
						log.error("did not get all parts for decoding");
					}
					// we've converted a line to bytes, write it out
					baos.writeTo(out); 
					baos.reset();
				}
			} 
			catch (IOException ioe) {
				log.debug("failed decoding part: " + ioe.toString());
				try { 
				    out.close(); 
				} 
				catch (IOException ie) {}
			}
		} // end of for(;;)
	}

	public boolean checkUUEncoded(File cache) {
		try {
			BufferedReader breader = new BufferedReader(new FileReader(cache));
			String line = breader.readLine();
			breader.close();
			if ( line == null || line.length() < 0 ) return false;
			if ( line.startsWith("begin ") ) return true;
		} 
		catch (IOException ioe) {
			log.error("error checking UUEncoding: " + ioe.toString());
			return false;
		}
		return false;
	}

	public String getFilename(File cache) {
		try {
			BufferedReader breader = new BufferedReader(new FileReader(cache));
			String line = breader.readLine();
			breader.close();
			if ( line == null || line.length() < 0 ) {
			    return new String();
			}
			if ( line.startsWith("begin ") ) {
			    return line.substring(9).trim();
			}
		} 
		catch (IOException ioe) {
			log.error("error checking UUEncoding: " + ioe.toString());
			return new String();
		}
		return new String();
	}
	
	// To get a string without the 
	// first n words in string str.
	public static String skipWords(String str, int n) {
		int i=0;
		while (i<str.length() && Character.isSpaceChar(str.charAt(i))) {
		    i++;
		}
		while (n>0) {
			while (i<str.length() && !Character.isSpaceChar(str.charAt(i))) {
			    i++;
			}
			while (i<str.length() && Character.isSpaceChar(str.charAt(i))) {
			    i++;
			}
			n--;
		}
		return(str.substring(i));
	}

	// To get the first word in a string. Returns a string with all characters 
	// found before the first space character.
	public static String getFirstWord(String str) {
		int i=0;
		while (i<str.length() && !Character.isSpaceChar(str.charAt(i))) {
		    i++;
		}
		return(str.substring(0,i));
	}

	////
	public static String getWord(String str, int n) {
		return(getFirstWord(skipWords(str,n)));
	}  

	////
	private void printBin8(int d) throws IOException {
		for (int i=0;i<8;i++) {
			out.write((((d<<i)&0x80)==0)?'0':'1');
		}
		out.write(' ');
	}

	////
	private void decodeString3(String str, ByteArrayOutputStream baos) {
		int c0=str.charAt(0)^0x20;
		int c1=str.charAt(1)^0x20;
		int c2=str.charAt(2)^0x20;
		int c3=str.charAt(3)^0x20;
		baos.write( ((c0<<2) & 0xfc) | ((c1>>4) & 0x3) );
		baos.write( ((c1<<4) & 0xf0) | ((c2>>2) & 0xf) );
		baos.write( ((c2<<6) & 0xc0) | ((c3) & 0x3f) );
	}

	////
	private void decodeString2(String str, ByteArrayOutputStream baos) {
		int c0=str.charAt(0)^0x20;
		int c1=str.charAt(1)^0x20;
		int c2=str.charAt(2)^0x20;
		baos.write( ((c0<<2) & 0xfc) | ((c1>>4) & 0x3) );
		baos.write( ((c1<<4) & 0xf0) | ((c2>>2) & 0xf) );
	}

	////
	private void decodeString1(String str, ByteArrayOutputStream baos) {
		int c0=str.charAt(0)^0x20;
		int c1=str.charAt(1)^0x20;
		baos.write( ((c0<<2) & 0xfc) | ((c1>>4) & 0x3) );
	}

	// set the output stream 
	public void setOutputStream(DataOutputStream ostream) {
		out = ostream;
	}

	// set the input stream
	public void setInputStream(BufferedReader istream) {
		in = istream;
	}
}
