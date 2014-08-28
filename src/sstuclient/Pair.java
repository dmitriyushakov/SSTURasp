package sstuclient;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.*;

public class Pair implements Parcelable,JSONConvertable{
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
	
	public Pair(Parcel parcel){
		auditorium=parcel.readString();
		subject=parcel.readString();
		lecturer=parcel.readString();
		start=new Time(parcel);
		end=new Time(parcel);
	}
	public boolean isBefore(Time time){
		return start.isLater(time);
	}
	public boolean isAfter(Time time){
		return time.isLater(end);
	}
	public boolean isInside(Time time){
		return !isBefore(time)&&!isAfter(time);
	}
	public float partOfTime(Time time){
		if(isInside(time)){
			int end=this.end.getHour()*60+this.end.getMinute();
			int start=this.start.getHour()*60+this.start.getMinute();
			int now=time.getHour()*60+time.getMinute();
			return ((float)(now-start))/(end-start);
		}else return 0;
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
	public boolean isLater(Pair other){
		return other.end.isLater(end);
	}

	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(auditorium);
		dest.writeString(subject);
		dest.writeString(lecturer);
		start.writeToParcel(dest,flags);
		end.writeToParcel(dest,flags);
	}
	
	public static final Parcelable.Creator<Pair> CREATOR=new Parcelable.Creator<Pair>(){
		@Override
		public Pair createFromParcel(Parcel source) {
			return new Pair(source);
		}
		@Override
		public Pair[] newArray(int size) {
			return new Pair[size];
		}
	};
	public JSONObject toJSON(){
		JSONObject obj=new JSONObject();
		
		try {
			obj.put("auditorium",auditorium);
			obj.put("subject",subject);
			obj.put("lecturer",lecturer);
			obj.put("start",start.toJSON());
			obj.put("end",end.toJSON());
			
			return obj;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	public static Pair fromJSON(JSONObject obj){
		try {
			return new Pair(
					obj.getString("auditorium"),
					obj.getString("subject"),
					obj.getString("lecturer"),
					Time.fromJSON(obj.getJSONObject("start")),
					Time.fromJSON(obj.getJSONObject("end")));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
