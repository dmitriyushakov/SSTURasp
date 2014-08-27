package sstuclient;

import android.os.Bundle;

public class UsualFaculty extends Faculty{
	private SpecialityTag tags[];
	private UsualFaculty(){};

	public static Faculty restoreFromBundle(Bundle bundle){
		UsualFaculty fac=new UsualFaculty();
		
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
	@Override
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
	UsualFaculty(String name,SpecialityTag tags[]){
		this.name=name;
		this.tags=tags;
	}
	@Override
	public void syncInit(){};
	@Override
	public String getName(){
		return name;
	}
	@Override
	public int size(){
		return tags.length;
	}
	@Override
	public SpecialityTag at(int num){
		return tags[num];
	}
}
