package sstuclient;

public class Pair {
	private String auditorium;
	private String subject;
	private String lecturer;
	private Time start;
	private Time end;
	
	public Pair(String a,String s,String l,Time startTime,Time endTime){
		auditorium=a;
		subject=s;
		lecturer=l;
		start=startTime;
		end=endTime;
	}
	
	public String getAuditorium(){
		return auditorium;
	}
	public String getSubject(){
		return subject;
	}
	public String getLecturer(){
		return lecturer;
	}
	public Time getStart(){
		return start;
	}
	public Time getEnd(){
		return end;
	}
}
