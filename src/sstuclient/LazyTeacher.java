package sstuclient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.*;
import java.util.*;

import android.os.Bundle;

public class LazyTeacher extends LazyFaculty{
	public LazyTeacher(String name){
		this.name=name;
	}
	@Override
	public void syncInit() throws MalformedURLException, IOException {
		String content=HttpGetter.get("http://rasp.sstu.ru/teacher");
		
		Pattern linkPattern=Pattern.compile("<a href=\"([^\"]*)\">([^<]*)</a>");
		Matcher linkMatcher=linkPattern.matcher(content);
		
		List<SpecialityTag> tags=new ArrayList<SpecialityTag>();
		while(linkMatcher.find()){
			String name=linkMatcher.group(2).replace("&nbsp;"," ");
			String url="http://rasp.sstu.ru"+linkMatcher.group(1);
			tags.add(new SpecialityTag(name,url));
		}
		
		SpecialityTag arr[]=new SpecialityTag[tags.size()];
		for(int i=0;i<arr.length;i++)arr[i]=tags.get(i);
		
		realFaculty=new UsualFaculty(name,arr);
	}
	@Override
	public void putToBundle(Bundle bundle) {
		if(realFaculty==null){
			bundle.putBoolean("isTeacher",true);
			bundle.putString("facName",name);
		}else{
			realFaculty.putToBundle(bundle);
		}
	}
}
