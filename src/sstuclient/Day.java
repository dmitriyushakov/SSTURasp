package sstuclient;

import java.util.*;

public class Day {
	public static int MONDAY=0;
	public static int TUESDAY=1;
	public static int WEDNESDAY=2;
	public static int THURSDAY=3;
	public static int FRIDAY=4;
	public static int SATURDAY=5;
	private int day;
	private List<Pair> pairs;
	private boolean even;
	
	public Day(int d,boolean e){
		day=d;
		even=e;
		pairs=new ArrayList<Pair>();
	}
	public boolean isToday(boolean iseven){
		Calendar calendar=new GregorianCalendar();
		int cday=calendar.get(Calendar.DAY_OF_WEEK);
		return (
				(day==MONDAY&&cday==Calendar.MONDAY||
				day==TUESDAY&&cday==Calendar.TUESDAY||
				day==WEDNESDAY&&cday==Calendar.WEDNESDAY||
				day==THURSDAY&&cday==Calendar.THURSDAY||
				day==FRIDAY&&cday==Calendar.FRIDAY||
				day==SATURDAY&&cday==Calendar.SATURDAY)&&
				even==iseven);
		
	}
	public boolean isTomorrow(boolean iseven){
		Calendar calendar=new GregorianCalendar();
		int cday=calendar.get(Calendar.DAY_OF_WEEK);
		return (
				(day==MONDAY&&cday==Calendar.SUNDAY||
				day==TUESDAY&&cday==Calendar.MONDAY||
				day==WEDNESDAY&&cday==Calendar.TUESDAY||
				day==THURSDAY&&cday==Calendar.WEDNESDAY||
				day==FRIDAY&&cday==Calendar.THURSDAY||
				day==SATURDAY&&cday==Calendar.FRIDAY)&&
				even==iseven);
		
	}
	public void add(Pair pair){
		pairs.add(pair);
	}
	public int dayOfWeek(){
		return day;
	}
	public int size(){
		return pairs.size();
	}
	public Pair at(int i){
		return pairs.get(i);
	}
	public boolean isEven(){
		return even;
	}
}
