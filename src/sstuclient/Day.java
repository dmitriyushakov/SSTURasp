package sstuclient;

import java.util.*;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.*;

public class Day implements Parcelable,JSONConvertable{
	public static final int MONDAY=0;
	public static final int TUESDAY=1;
	public static final int WEDNESDAY=2;
	public static final int THURSDAY=3;
	public static final int FRIDAY=4;
	public static final int SATURDAY=5;
	private int day;
	private List<Pair> pairs;
	private boolean even;
	
	public Day(int d,boolean e){
		day=d;
		even=e;
		pairs=new ArrayList<Pair>();
	}
	
	public Day(Parcel parcel){
		day=parcel.readInt();
		even=parcel.readByte()!=0;
		pairs=new ArrayList<Pair>();
		parcel.readTypedList(pairs,Pair.CREATOR);
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
	public Pair last(){
		Pair pair=null;
		for(Pair cpair:pairs){
			if(pair==null||pair.isLater(cpair))pair=cpair;
		}
		return pair;
	}
	public boolean isEven(){
		return even;
	}
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(day);
		dest.writeByte((byte)(even?1:0));
		dest.writeTypedList(pairs);
	}
	
	public static final Parcelable.Creator<Day> CREATOR=new Parcelable.Creator<Day>(){
		@Override
		public Day createFromParcel(Parcel source) {
			return new Day(source);
		}
		@Override
		public Day[] newArray(int size) {
			return new Day[size];
		}
	};
	public JSONObject toJSON(){
		JSONObject obj=new JSONObject();
		
		try {
			obj.put("day",day);
			obj.put("even",even);
			
			JSONArray array=new JSONArray();
			for(Pair pair:pairs){
				array.put(pair.toJSON());
			}
			
			obj.put("list",array);
			
			return obj;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	public static Day fromJSON(JSONObject obj){
		try {
			Day day=new Day(obj.getInt("day"),obj.getBoolean("even"));
			JSONArray array=obj.getJSONArray("list");
			for(int i=0;i<array.length();i++){
				JSONObject pairobj=array.getJSONObject(i);
				day.add(Pair.fromJSON(pairobj));
			}
			return day;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
		return null;
	}
}
