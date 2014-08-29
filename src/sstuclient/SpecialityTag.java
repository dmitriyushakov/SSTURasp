package sstuclient;

public class SpecialityTag implements Comparable{
	private String name;
	private String url;
	public SpecialityTag(String n,String u){
		name=n;
		url=u;
	}
	public String getName(){
		return name;
	}
	public String getUrl(){
		return url;
	}
	@Override
	public int compareTo(Object other) {
		SpecialityTag othert=(SpecialityTag)other;
		return name.compareTo(othert.name);
	}
}
