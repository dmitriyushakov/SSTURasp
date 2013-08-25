package sstuclient;

public class FacultyTag {
	private String name;
	private int num;
	public FacultyTag(int num,String name){
		this.num=num;
		this.name=name;
	}
	public String getNum(){
		return (num<10)?"0"+num:Integer.toString(num);
	}
	public String getName(){
		return name;
	}
}
