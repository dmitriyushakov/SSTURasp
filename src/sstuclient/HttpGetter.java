package sstuclient;

import java.io.*;
import java.net.*;

public class HttpGetter {
	public static String get(String addres) throws IOException,MalformedURLException{
		URL url=null;
		
		url=new URL(addres);
		
		String content=null;
		HttpURLConnection connection=(HttpURLConnection)url.openConnection();
		connection.connect();
			
		BufferedInputStream stream=new BufferedInputStream(connection.getInputStream());
		ByteBuffer buf=new ByteBuffer(10000);
		int rval=0;
		while((rval=stream.read())!=-1){
			byte symbol=(byte) rval;
			buf.put(symbol);
		}
		
		content=new String(buf.array());
		
		return content;
	}
}
