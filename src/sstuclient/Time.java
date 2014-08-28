package sstuclient;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.*;

public class Time implements Parcelable, JSONConvertable{
	private int hour;
	private int minute;
	@SuppressWarnings("unused")
	private Time(){};
	
	public Time(int h,int m){
		hour=h;
		minute=m;
	}
	public Time(Parcel parcel){
		hour=parcel.readInt();
		minute=parcel.readInt();
	}
	public static Time parse(String str){
		String vals[]=null;
		if(str.indexOf(".")!=-1)vals=str.split("\\.");
		else vals=str.split(":");
		int hour=Integer.parseInt(vals[0]);
		int minute=Integer.parseInt(vals[1]);
		return new Time(hour,minute);
	}
	public int getHour(){
		return hour;
	}
	public int getMinute(){
		return minute;
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(hour);
		dest.writeInt(minute);
	}
	
	public static final Parcelable.Creator<Time> CREATOR=new Parcelable.Creator<Time>(){
		@Override
		public Time createFromParcel(Parcel parcel) {
			return new Time(parcel);
		}
		@Override
		public Time[] newArray(int size) {
			return new Time[size];
		}
	};
	
	public JSONObject toJSON(){
		JSONObject obj=new JSONObject();
		
		try {
			obj.put("hour",hour);
			obj.put("minute",minute);
			
			return obj;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	public static Time fromJSON(JSONObject obj){
		try {
			return new Time(obj.getInt("hour"),obj.getInt("minute"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
