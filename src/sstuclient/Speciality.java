package sstuclient;

import java.util.*;
import java.util.regex.*;
import java.io.*;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.*;

public class Speciality implements Parcelable,JSONConvertable{
	private static final String SAVE_FILE="sstu_save.json";
	private static final String stdWeekPattern="<div align=\"center\"><A href=\"[^\"]*\"><b>([^<]*)</b></A><br><font style=\"FONT-FAMILY: Arial\"size=\"2\">([^<]*)<br></font><font size=\"2\"><A href=\"[^\"]*\">([^<]*)</A></font><br></div>";
	private static final String lectWeekPattern="<div align=\"center\"><A href=\"[^\"]*\"> <b>([^<]*)</b></A><br><font style=\"FONT-FAMILY: Arial\"size=\"2\">([^<]*)<br>([^<]*)</font><br>.*</div>";
	private String url;
	private boolean lecturer;
	private boolean changeEven;
	private Day nevenDays[];
	private Day evenDays[];
	private String name;
	
	private Speciality(){};
	
	private static Day[] getWeek(String content,boolean even,String pattern){
		Day[] days=new Day[6];
		for(int i=0;i<6;i++){
			days[i]=new Day(i,even);
		}
		
		content=content.substring(0,content.indexOf("</tbody>"));
		
		String strings[]=content.split("<tr>");
		
		for(int i=2;i<strings.length;i++){
			String row=strings[i];
			String cells[]=row.split("</td>");
			
			Pattern timepattern=Pattern.compile("<b>([0-9\\.]*)<br>([0-9\\.]*)");
			Matcher timematcher=timepattern.matcher(cells[0]);
			timematcher.find();
			
			Time starttime=Time.parse(timematcher.group(1));
			Time endtime=Time.parse(timematcher.group(2));
			
			for(int j=1;j<cells.length-1;j++){
				
				Pattern cellpattern=Pattern.compile(pattern);
				Matcher cellmatcher=cellpattern.matcher(cells[j]);
				
				if(cellmatcher.find()){
					String aud=cellmatcher.group(1).replace("//","/");
					String subj=cellmatcher.group(2);
					String lect=cellmatcher.group(3);
					
					int prevlen=-1;
					while(prevlen!=subj.length()){
						prevlen=subj.length();
						subj=subj.replace("  ", " ");
					}
					
					Pair pair=new Pair(aud,subj,lect,starttime,endtime);
					days[j-1].add(pair);
				}
			}
		}
		
		return days;
	}
	
	public Speciality(Parcel parcel){
		name=parcel.readString();
		url=parcel.readString();
		byte fl=parcel.readByte();
		lecturer=(fl&1)==1;
		changeEven=(fl&2)==2;
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
	
	private static Speciality getAnything(String urlval,String pattern) throws IOException{
		Speciality spec=new Speciality();
		
		String content=HttpGetter.get(urlval);
		
		String name=content.substring(content.indexOf("<title>")+7, content.indexOf("</title>"));
		name=name.trim();
		
		boolean siteEven=content.indexOf("IMAGE/chet.jpg")!=-1;
		spec.changeEven=(siteEven!=evenWeek());
		if((new GregorianCalendar()).get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY)
			spec.changeEven=!spec.changeEven;
		spec.name=name;
		spec.url=urlval;
		
		String strs[]=content.split("<table align=\"center\" border=\"0\" cellpadding=\"2\" cellspacing=\"1\"  bordercolor=\"#ff3300\" bgcolor=\"#000000\">");
		
		spec.nevenDays=getWeek(strs[1],false,pattern);
		spec.evenDays=getWeek(strs[2],true,pattern);
		
		return spec;
	}
	
	public static Speciality getSpeciality(String url) throws IOException{
		Speciality spec=getAnything(url,stdWeekPattern);;
		spec.lecturer=false;
		return spec;
	}
	
	public static Speciality getLecturer(String url) throws IOException{
		Speciality lect=getAnything(url.replace(" ",""),lectWeekPattern);
		lect.lecturer=true;
		return lect;
	}
	
	public boolean isEven(){
		boolean even=evenWeek();
		if(changeEven)even=!even;
		
		return even;
	}
	public boolean isLecturer(){
		return lecturer;
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
			obj.put("isLecturer",lecturer);
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
			spec.lecturer=obj.getBoolean("isLecturer");
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
		byte fl=(byte)((lecturer?1:0)+(changeEven?2:0));
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
