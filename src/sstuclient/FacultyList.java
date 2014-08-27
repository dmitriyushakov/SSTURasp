package sstuclient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.*;

import ru.sstu.rasp.ushakov.R;
import android.content.Context;

public class FacultyList {
	private List<Faculty> list;
	private boolean hasLazy=false;
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
			
			Faculty faculty=Faculty.getFaculty(name,arr);
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
	private void initLazyFaculties(Context cont){
		if(!hasLazy&&cont!=null){
			hasLazy=true;
			list.add(new LazyTeacher(cont.getString(R.string.lecturers)));
			list.add(new LazyAuditory(cont.getString(R.string.auds)));
		}
	}
	public static synchronized void syncInit(Context cont) throws MalformedURLException, IOException{
		if(instance==null)instance=new FacultyList();
		instance.initLazyFaculties(cont);
	}
	public static int size(){
		return getInstance().list.size();
	}
	public static Faculty getFaculty(int num){
		return getInstance().list.get(num);
	}
}