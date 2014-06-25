package sstuclient;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Time {
	private int hour;
	private int minute;
	@SuppressWarnings("unused")
	private Time(){};
	
	public Time(int h,int m){
		hour=h;
		minute=m;
	}
	public static Time parse(String str){
		String vals[]=null;
		if(str.indexOf(".")!=-1)vals=str.split("\\.");
		else vals=str.split(":");
		int hour=Integer.parseInt(vals[0]);
		int minute=Integer.parseInt(vals[1]);
		return new Time(hour,minute);
	}
	public boolean isLater(Time other){
		return other.hour<hour||other.hour==hour&&other.minute<minute;
	}
	public String toString(){
		return hour+":"+(minute<10?"0":"")+minute;
	}
	public static Time now(){
		Calendar calendar=new GregorianCalendar();
		int hour=calendar.get(Calendar.HOUR_OF_DAY);
		int minute=calendar.get(Calendar.MINUTE);
		return new Time(hour,minute);
	}
}
