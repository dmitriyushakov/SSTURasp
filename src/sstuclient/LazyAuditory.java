package sstuclient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.*;
import java.util.*;

import android.os.Bundle;

public class LazyAuditory extends LazyFaculty{
	public LazyAuditory(String name){
		this.name=name;
	}
	@Override
	public void syncInit() throws MalformedURLException, IOException {
		String content=HttpGetter.get("http://rasp.sstu.ru/aud");
		
		Pattern linkPattern=Pattern.compile("<a href=\"([^\"]*)\">([^<]*)</a>");
		Pattern titlePattern=Pattern.compile("<h3 class=\"panel-title\">([^<]*)</h3>");
		
		String buildStrs[]=content.split("<div class=\"panel-heading\">");
		List<SpecialityTag> tags=new ArrayList<SpecialityTag>();
		for(int i=1;i<buildStrs.length;i++){
			String str=buildStrs[i];
			Matcher titleMatcher=titlePattern.matcher(str);
			titleMatcher.find();
			String title=titleMatcher.group(1);
			
			Matcher linkMatcher=linkPattern.matcher(str);
			
			while(linkMatcher.find()){
				String url="http://rasp.sstu.ru"+linkMatcher.group(1);
				String name=(linkMatcher.group(2)+" "+title).replace("&nbsp;"," ");
				
				tags.add(new SpecialityTag(name,url));
			}
		}
		
		SpecialityTag arr[]=new SpecialityTag[tags.size()];
		for(int i=0;i<arr.length;i++)arr[i]=tags.get(i);
		
		realFaculty=new UsualFaculty(name,arr);
	}
	@Override
	public void putToBundle(Bundle bundle) {
		if(realFaculty==null){
			bundle.putBoolean("isAuditory",true);
			bundle.putString("facName",name);
		}else{
			realFaculty.putToBundle(bundle);
		}
	}
}
