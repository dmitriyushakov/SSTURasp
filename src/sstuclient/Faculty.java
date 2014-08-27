package sstuclient;

import java.io.IOException;
import java.net.MalformedURLException;

import android.os.Bundle;

abstract public class Faculty {
	protected String name;

	public static Faculty restoreFromBundle(Bundle bundle){
		if(bundle.getBoolean("isTeacher",false)){
			return new LazyTeacher(bundle.getString("facName"));
		}else if(bundle.getBoolean("isAuditory",false)){
			return new LazyAuditory(bundle.getString("facName"));
		}else{
			return UsualFaculty.restoreFromBundle(bundle);
		}
	}
	public static Faculty getFaculty(String name,SpecialityTag tags[]){
		return new UsualFaculty(name,tags);
	}
	public abstract void syncInit() throws MalformedURLException, IOException;
	public abstract void putToBundle(Bundle bundle);
	public abstract String getName();
	public abstract int size();
	public abstract SpecialityTag at(int num);
}
