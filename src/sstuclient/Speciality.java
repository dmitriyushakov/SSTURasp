package sstuclient;

import java.util.*;
import java.util.regex.*;
import java.io.*;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.*;

public class Speciality implements Parcelable,JSONConvertable{
	private static final String SAVE_FILE="sstu_save.json";
	private static final String stdWeekPattern="<div class=\"small\">(<div class=\"aud\">([^<]*)</div>)?<div class=\"subject-?m?\">([^<]*)(</div><div| <span) class=\"type\">([^<]*)</(div|span)>(<div class=\"(teacher|group)\">([^<]*)</div></div>)?";
	private static final String audWeekPattern="<div class=\"small\"><div class=\"subject\">([^<]*)</div><div class=\"type\">([^<]*)</div><div class=\"teacher\">([^<]*)</div><div class=\"group\">([^<]*)</div></div>";
	private String url;
	private boolean changeEven;
	private Day nevenDays[];
	private Day evenDays[];
	private String name;
	
	private Speciality(){};
	
	private static Day[] getWeek(String content,boolean even,boolean isAuditory){
		Day[] days=new Day[6];
		for(int i=0;i<6;i++){
			days[i]=new Day(i,even);
		}
		
		String strings[]=content.split("<div class=\"rasp-table-col\">");
		
		for(int i=1;i<strings.length;i++){
			String col=strings[i];
			String cells[]=col.split("<div class=\"rasp-table-row ");
			
			for(int j=1;j<=cells.length-1;j++){
				String cell=cells[j];
				
				String timesStr=cell.substring(0,cell.indexOf("<div class=\"rasp-table-inner-cell\">"));

				Pattern timepattern=Pattern.compile("([0-9]{1,2}:[0-9]{2})");
				Matcher timematcher=timepattern.matcher(timesStr);
				
				timematcher.find();
				Time starttime=Time.parse(timematcher.group(1));

				timematcher.find();
				Time endtime=Time.parse(timematcher.group(1));
				
				Pattern cellpattern=Pattern.compile(isAuditory?audWeekPattern:stdWeekPattern);
				Matcher cellmatcher=cellpattern.matcher(cell);
				
				if(cellmatcher.find()){
					String aud=null;
					String subj=null;
					String lect=null;
					if(isAuditory){
						aud=cellmatcher.group(4);
						subj=cellmatcher.group(1)+" "+cellmatcher.group(2);
						lect=cellmatcher.group(3);
					}else{
						aud=cellmatcher.group(2);
						String type=cellmatcher.group(5);
						subj=cellmatcher.group(3)+(type==null?"":" "+type);
						lect=cellmatcher.group(9);
					}
					if(aud==null)aud="";
					if(lect==null)lect="";
					
					Pair pair=new Pair(aud,subj,lect,starttime,endtime);
					days[i-1].add(pair);
				}
			}
		}
		return days;
	}
	
	public Speciality(Parcel parcel){
		name=parcel.readString();
		url=parcel.readString();
		changeEven=parcel.readByte()!=0;
		nevenDays=new Day[6];
		evenDays=new Day[6];
		parcel.readTypedArray(nevenDays,Day.CREATOR);
		parcel.readTypedArray(evenDays,Day.CREATOR);
	}
	
	private static boolean evenWeek(){
		Calendar calendar=new GregorianCalendar();
		boolean even=((calendar.get(Calendar.WEEK_OF_YEAR)%2)==0);
		if(calendar.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY)even=!even;
		return even;
	}
	
	public static Speciality getSpeciality(String url) throws IOException{
		Speciality spec=new Speciality();
		
		String content=HttpGetter.get(url);
		
		String name=content.substring(content.indexOf("<title>")+7, content.indexOf("</title>"));
		name=name.trim();
		
		// Calendars different. Foreign calendar begining from sunday. Our from monday.
		if((new GregorianCalendar()).get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY)
			spec.changeEven=!spec.changeEven;
		
		spec.name=name;
		spec.url=url;
		
		String strs[]=content.split("<span class=\"week_number\">");

		boolean siteEven=strs[0].indexOf("nechet")==-1;
		spec.changeEven=(siteEven!=evenWeek());
		
		boolean isAud=url.indexOf("http://rasp.sstu.ru/aud/")==0;
		if(siteEven){
			spec.nevenDays=getWeek(strs[2],false,isAud);
			spec.evenDays=getWeek(strs[1],true,isAud);
		}else{
			spec.nevenDays=getWeek(strs[1],false,isAud);
			spec.evenDays=getWeek(strs[2],true,isAud);
		}
		
		return spec;
	}
	
	public boolean isEven(){
		boolean even=evenWeek();
		if(changeEven)even=!even;
		
		return even;
	}
	public String getName(){
		return name;
	}
	public Day getDay(int day,boolean isEven){
		return isEven?evenDays[day]:nevenDays[day];
	}

	public static boolean isSaved(String folder){
		File file=new File(folder+SAVE_FILE);
		return file.exists();
	}
	public void save(String folder){
		File file=new File(folder+SAVE_FILE);
		if(file.exists())file.delete();
		
		Writer writer=null;
		try{
			writer=new FileWriter(file);
			writer.append(toJSON().toString());
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(writer!=null)
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	public JSONObject toJSON(){
		JSONObject obj=new JSONObject();
		try {
			obj.put("name",name);
			obj.put("url",url);
			obj.put("changeEven",changeEven);
			
			JSONArray evenArr=new JSONArray();
			for(Day day:evenDays){
				evenArr.put(day.toJSON());
			}
			obj.put("evenDays",evenArr);

			JSONArray nevenArr=new JSONArray();
			for(Day day:nevenDays){
				nevenArr.put(day.toJSON());
			}
			obj.put("nevenDays",nevenArr);
			
			return obj;
		} catch (JSONException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return obj;
	}
	public static Speciality fromJSON(JSONObject obj){
		try {
			Speciality spec=new Speciality();
			spec.evenDays=new Day[6];
			spec.nevenDays=new Day[6];
			spec.name=obj.getString("name");
			spec.url=obj.getString("url");
			spec.changeEven=obj.getBoolean("changeEven");
			
			JSONArray evenArr=obj.getJSONArray("evenDays");
			JSONArray nevenArr=obj.getJSONArray("nevenDays");
			for(int i=0;i<6;i++){
				spec.evenDays[i]=Day.fromJSON(evenArr.getJSONObject(i));
				spec.nevenDays[i]=Day.fromJSON(nevenArr.getJSONObject(i));
			}
			
			return spec;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	public String getUrl(){
		return url;
	}
	public static Speciality restore(String folder){
		File file=new File(folder+SAVE_FILE);
		
		InputStream in=null;
		try {
			byte buf[]=new byte[(int)file.length()];
			
			in=new FileInputStream(file);
			in.read(buf);
			
			String str=new String(buf);
			JSONTokener tokener=new JSONTokener(str);
			JSONObject obj=(JSONObject)tokener.nextValue();
			
			return Speciality.fromJSON(obj);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(JSONException e) {
			e.printStackTrace();
		} finally {
			try{
				if(in!=null)in.close();
			}catch(IOException e){
				
			}
		}
		
		return null;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(url);
		byte fl=(byte)(changeEven?1:0);
		dest.writeByte(fl);
		dest.writeTypedArray(nevenDays,flags);
		dest.writeTypedArray(evenDays,flags);
	}
	
	public static final Parcelable.Creator<Speciality> CREATOR=new Parcelable.Creator<Speciality>(){
		@Override
		public Speciality createFromParcel(Parcel source) {
			return new Speciality(source);
		}

		@Override
		public Speciality[] newArray(int size) {
			return new Speciality[size];
		}
		
	};
}
