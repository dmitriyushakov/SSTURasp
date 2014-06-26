package sstuclient;

import java.util.*;
import java.util.regex.*;
import java.io.*;

public class Speciality {
	private static String SAVE_FILE="sstu_save";
	private static String stdWeekPattern="<div align=\"center\"><A href=\"[^\"]*\"><b>([^<]*)</b></A><br><font style=\"FONT-FAMILY: Arial\"size=\"2\">([^<]*)<br></font><font size=\"2\"><A href=\"[^\"]*\">([^<]*)</A></font><br></div>";
	private static String lectWeekPattern="<div align=\"center\"><A href=\"[^\"]*\"> <b>([^<]*)</b></A><br><font style=\"FONT-FAMILY: Arial\"size=\"2\">([^<]*)<br>([^<]*)</font><br>.*</div>";
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
		
		try{
			Writer writer=new FileWriter(file);
			
			writer.append(url+"\n"+name+"\n");
			writer.append((changeEven?"1":"0")+" "+(lecturer?"1":"0")+"\n\n");
			
			
			for(int i=0;i<6;i++){
				writer.append("0 "+i+"\n");
				
				Day day=nevenDays[i];
				for(int j=0;j<day.size();j++){
					Pair pair=day.at(j);
					writer.append(pair.getStart().toString()+"\n");
					writer.append(pair.getEnd().toString()+"\n");
					writer.append(pair.getAuditorium()+"\n");
					writer.append(pair.getSubject()+"\n");
					writer.append(pair.getLecturer()+"\n");
				}
				writer.append("\n");
			}
			
			for(int i=0;i<6;i++){
				writer.append("1 "+i+"\n");
				
				Day day=evenDays[i];
				for(int j=0;j<day.size();j++){
					Pair pair=day.at(j);
					writer.append(pair.getStart().toString()+"\n");
					writer.append(pair.getEnd().toString()+"\n");
					writer.append(pair.getAuditorium()+"\n");
					writer.append(pair.getSubject()+"\n");
					writer.append(pair.getLecturer()+"\n");
				}
				writer.append("\n");
			}
			
			writer.close();
		}catch(IOException e){
		}
	}
	public String getUrl(){
		return url;
	}
	public static Speciality restore(String folder){
		Speciality spec=new Speciality();
		spec.evenDays=new Day[6];
		spec.nevenDays=new Day[6];

		FileGetter getter=new FileGetter(folder+SAVE_FILE);
		
		spec.url=getter.getString();
		spec.name=getter.getString();
		String boolInfo[]=getter.getString().split(" ");
		spec.changeEven=!boolInfo[0].equals("0");
		spec.lecturer=!boolInfo[1].equals("0");
		getter.getString();
		
		while(getter.haveStrings()){
			String daystring=getter.getString();
			String vals[]=daystring.split(" ");
			boolean even=(!vals[0].equals("0"));
			int num=Integer.parseInt(vals[1]);
			
			Day day=new Day(num,even);
			
			while(true){
				String startTimeStr=getter.getString();
				if(startTimeStr.length()==0)break;
				Time startTime=Time.parse(startTimeStr);
				Time endTime=Time.parse(getter.getString());
				String auditorium=getter.getString();
				String subject=getter.getString();
				String lecturer=getter.getString();
				
				Pair pair=new Pair(auditorium,subject,lecturer,startTime,endTime);
				day.add(pair);
			}
			
			if(even)spec.evenDays[num]=day;
			else spec.nevenDays[num]=day;
		}
		
		return spec;
	}
}
