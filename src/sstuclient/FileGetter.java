package sstuclient;

import java.io.*;

public class FileGetter {
	public String strings[];
	public int capacity;
	public int index;
	public int size;
	public FileGetter(String filename){
		File file=new File(filename);
		BufferedReader reader=null;
		try{
			reader=new BufferedReader(new FileReader(file));
		}catch(FileNotFoundException e){};
		
		strings=new String[500];
		capacity=500;
		index=0;
		
		try{
			while(true){
				String str=reader.readLine();
				if(str==null){
					break;
				}else{
					if(index==capacity){
						int newcapacity=capacity*3/2;
						String newstrings[]=new String[newcapacity];
						System.arraycopy(strings,0,newstrings,0,capacity);
						strings=newstrings;
						capacity=newcapacity;
					}
					strings[index++]=str;
				}
			}
			size=index;
			index=0;
		}catch(IOException e){};
	}
	public String getString(){
		return strings[index++];
	}
	public boolean haveStrings(){
		return index<size;
	}
}
