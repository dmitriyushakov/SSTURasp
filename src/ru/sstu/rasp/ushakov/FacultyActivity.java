package ru.sstu.rasp.ushakov;

import android.os.*;
import android.view.*;
import android.widget.*;
import android.app.*;
import android.content.*;
import sstuclient.*;

public class FacultyActivity extends ListActivity {
	private ArrayAdapter<String> adapter;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String lines[]=new String[FacultyList.size()+1];
		for(int i=0;i<FacultyList.size();i++)
			lines[i]=FacultyList.getTag(i).getName();
		
		lines[lines.length-1]=getResources().getString(R.string.lecturers);
		
		adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,lines);
		
		setListAdapter(adapter);
	}
	
	protected void onListItemClick(ListView listv,View view,int pos,long id){
		Intent intent=new Intent(this,SpecActivity.class);
		if(pos==FacultyList.size()){
			intent.putExtra("toLecturers",true);
		}else{
			intent.putExtra("facnum",FacultyList.getTag(pos).getNum());
		}
		startActivity(intent);
	}
}
