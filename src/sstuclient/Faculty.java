package sstuclient;

import java.io.IOException;
import java.util.*;
import java.util.regex.*;
import java.net.URLEncoder;

import android.os.Bundle;

public class Faculty {
	private static String stdListPattern="<td><a href=\"([^\"]*)\">([^<]*)</a></td>";
	private static String lectListPattern="<a href=\"([^\"]*)\"><font size=\"2\" color=\"#000099\">([^<]*)</font></a>";
	private static String audListPattern="<a href=\"htmlViewRaspAud\\.aspx\\?AUD=([/\\w]*)&KORP=([0-9]{1,2})&OLD=0\">";
	private String name;
	private SpecialityTag tags[];
	private Faculty(){};

	static public Faculty getFaculty(String num) throws IOException{
		return getAnything("http://rasp.sstu.ru/SelSpec.aspx?FAK="+num+"&OLD=0",stdListPattern);
	}
	static public Faculty getLecturers() throws IOException{
		return getAnything("http://rasp.sstu.ru/SelPrep.aspx?OLD=0",lectListPattern);
	}
	static public Faculty getAuditories() throws IOException{
		Faculty faculty=new Faculty();
		
		String content=HttpGetter.get("http://rasp.sstu.ru/SelAud.aspx?OLD=0");
		
		Pattern pattern=Pattern.compile(audListPattern);
		Matcher matcher=pattern.matcher(content);
		List<SpecialityTag> taglist=new ArrayList<SpecialityTag>();
		
		while(matcher.find()){
			String num=matcher.group(1);
			String building=matcher.group(2);
			String turl="http://rasp.sstu.ru/htmlViewRaspAud.aspx?AUD="+URLEncoder.encode(num,"UTF-8")+"&KORP="+building+"&OLD=0";
			String tname=building+num;
			SpecialityTag tag=new SpecialityTag(tname,turl);
			taglist.add(tag);
		}
		
		SpecialityTag tagarray[]=new SpecialityTag[taglist.size()];
		for(int i=0;i<taglist.size();i++)tagarray[i]=taglist.get(i);
		faculty.tags=tagarray;
		
		return faculty;
	}
	
	static private Faculty getAnything(String url,String patt) throws IOException{
		Faculty faculty=new Faculty();
		
		String content=HttpGetter.get(url);
		
		String name=content.substring(content.indexOf("<title>")+7, content.indexOf("</title>"));
		name=name.trim();
		faculty.name=name;
		
		Pattern pattern=Pattern.compile(patt);
		Matcher matcher=pattern.matcher(content);
		List<SpecialityTag> taglist=new ArrayList<SpecialityTag>();
		
		while(matcher.find()){
			String turl="http://rasp.sstu.ru/"+matcher.group(1);
			String tname=matcher.group(2).replace("\t","");
			while(tname.substring(tname.length()).equals(" "))tname=tname.substring(0,tname.length()-2);
			SpecialityTag tag=new SpecialityTag(tname,turl);
			taglist.add(tag);
		}
		
		SpecialityTag tagarray[]=new SpecialityTag[taglist.size()];
		for(int i=0;i<taglist.size();i++)tagarray[i]=taglist.get(i);
		faculty.tags=tagarray;
		
		return faculty;
	}
	public static Faculty restoreFromBundle(Bundle bundle){
		Faculty fac=new Faculty();
		
		fac.name=bundle.getString("facName");
		if(fac.name==null)return null;
		String tagUrls[]=bundle.getStringArray("facTagUrls");
		String tagNames[]=bundle.getStringArray("facTagNames");
		fac.tags=new SpecialityTag[tagUrls.length];
		for(int i=0;i<fac.tags.length;i++){
			fac.tags[i]=new SpecialityTag(tagNames[i],tagUrls[i]);
		}
		
		return fac;
	}
	public void putToBundle(Bundle bundle){
		bundle.putString("facName",name);
		String tagUrls[]=new String[tags.length];
		String tagNames[]=new String[tags.length];
		
		for(int i=0;i<tags.length;i++){
			tagUrls[i]=tags[i].getUrl();
			tagNames[i]=tags[i].getName();
		}
		
		bundle.putStringArray("facTagUrls",tagUrls);
		bundle.putStringArray("facTagNames",tagNames);
	}
	public String getName(){
		return name;
	}
	public int size(){
		return tags.length;
	}
	public SpecialityTag at(int num){
		return tags[num];
	}
}
