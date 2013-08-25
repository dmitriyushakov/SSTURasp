package sstuclient;

import java.util.*;

public class FacultyList {
	private List<FacultyTag> tags;
	private FacultyList(){
		tags=new ArrayList<FacultyTag>();
		tags.add(new FacultyTag(11,"МФПИТ"));
		tags.add(new FacultyTag(1,"АМФ"));
		tags.add(new FacultyTag(3,"МСФ"));
		tags.add(new FacultyTag(5,"ФЭТиП"));
		tags.add(new FacultyTag(6,"ЭФ"));
		tags.add(new FacultyTag(7,"ФЭС"));
		tags.add(new FacultyTag(8,"ФЭМ"));
		tags.add(new FacultyTag(13,"СГФ"));
		tags.add(new FacultyTag(16,"САДИ"));
		tags.add(new FacultyTag(17,"ФТФ"));
	}
	private static FacultyList list;
	private static FacultyList getInstance(){
		if(list==null)list=new FacultyList();
		return list;
	}
	public static int size(){
		return getInstance().tags.size();
	}
	public static FacultyTag getTag(int num){
		return getInstance().tags.get(num);
	}
}
