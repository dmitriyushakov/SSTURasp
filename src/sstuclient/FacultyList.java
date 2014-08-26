package sstuclient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.*;

public class FacultyList {
	private List<Faculty> list;
	private FacultyList() throws MalformedURLException, IOException{
		list=new ArrayList<Faculty>();
		
		String content=HttpGetter.get("http://rasp.sstu.ru/");
		
		content=content.substring(0,content.indexOf("<div class=\"panel-body row\" id=\"middle-menu\">"));
		String facultyStrs[]=content.split("<div class=\"panel panel-default center-block\">");
		
		for(int i=1;i<facultyStrs.length;i++){
			List<SpecialityTag> specs=new ArrayList<SpecialityTag>();
			Pattern linkPattern=Pattern.compile("<a href=\"([^\"]*)\">([^<]*)</a>");
			Matcher linkMatcher=linkPattern.matcher(facultyStrs[i]);
			
			while(linkMatcher.find()){
				SpecialityTag tag=new SpecialityTag(linkMatcher.group(2),"http://rasp.sstu.ru"+linkMatcher.group(1));
				specs.add(tag);
			}
			
			Pattern titlePattern=Pattern.compile("<span class=\"pseudo\"[\\s]+style=\"word-wrap: break-word;\">([^<]*)</span>");
			Matcher titleMatcher=titlePattern.matcher(facultyStrs[i]);
			titleMatcher.find();
			String name=titleMatcher.group(1).trim();
			
			SpecialityTag arr[]=new SpecialityTag[specs.size()];
			for(int j=0;j<specs.size();j++)arr[j]=specs.get(j);
			
			Faculty faculty=new Faculty(name,arr);
			list.add(faculty);
		}
	}
	private static FacultyList instance;
	private static FacultyList getInstance(){
		try {
			if(instance==null)instance=new FacultyList();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return instance;
	}
	public static void syncInit() throws MalformedURLException, IOException{
		if(instance==null)instance=new FacultyList();
	}
	public static int size(){
		return getInstance().list.size();
	}
	public static Faculty getFaculty(int num){
		return getInstance().list.get(num);
	}
}