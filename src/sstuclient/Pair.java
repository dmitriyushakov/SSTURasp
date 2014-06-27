package sstuclient;

import android.os.Parcel;
import android.os.Parcelable;

public class Pair implements Parcelable{
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
}
