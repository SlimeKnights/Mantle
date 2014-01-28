package mantle.util.network;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * @author fuj1n
 */
public class StringEncoder {

	public static final byte terminatorStart = "\002".getBytes(Charset.forName("UTF-8"))[0];
	public static final byte terminatorEnd = "\003".getBytes(Charset.forName("UTF-8"))[0];
	
	public static void encodeString(ByteBuf buffer, String s){
		byte[] bytes = new byte[s.getBytes().length];
		bytes[0] = terminatorStart;
		byte[] stringBytes = s.getBytes(Charset.forName("UTF-8"));
		for(int i = 0; i < stringBytes.length; i++){
			bytes[i + 1] = stringBytes[i];
			bytes[i + 2] = terminatorEnd;
		}
		buffer.writeBytes(bytes);
	}
	
	public static String decodeString(ByteBuf buffer){
		byte startByte = buffer.readByte();
		if(startByte == terminatorStart){
			ArrayList<Byte> bytesList = new ArrayList<Byte>();
			byte curChar = startByte;
			while(curChar != terminatorEnd){
				curChar = buffer.readByte();
				if(curChar != terminatorEnd){
					bytesList.add(curChar);
				}
			}
			byte[] bytes = new byte[bytesList.size()];
			for(int i = 0; i < bytes.length; i++){
				bytes[i] = bytesList.get(i);
			}
			
			try {
				return new String(bytes, "US-ASCII");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		return "Decoding Failed";
	}
	
}
