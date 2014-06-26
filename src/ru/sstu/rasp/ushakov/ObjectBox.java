package ru.sstu.rasp.ushakov;

import java.util.*;

public class ObjectBox {
	private static ObjectBox instance;
	private Random r;
	private Map<Integer,Object> map;
	private ObjectBox(){
		r=new Random();
		map=new HashMap<Integer,Object>();
	}
	private static ObjectBox getState(){
		if(instance==null)instance=new ObjectBox();
		return instance;
	}
	private Object getObj(int id){
		return map.remove(id);
	}
	private int setObj(Object obj){
		Integer i=null;
		do{
			i=r.nextInt();
		}while(map.containsKey(i));
		map.put(i,obj);
		return i;
	}
	public static Object get(int id){
		return getState().getObj(id);
	}
	public static int set(Object obj){
		return getState().setObj(obj);
	}
}
