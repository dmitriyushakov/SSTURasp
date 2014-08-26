package ru.sstu.rasp.ushakov;

import android.os.*;
import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.view.View;
import android.widget.*;
import sstuclient.*;

public class SpecActivity extends ListActivity {
	ArrayAdapter<String> adapter;
	private Faculty fac;
	@SuppressLint("HandlerLeak")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle state;
		if(savedInstanceState!=null)state=savedInstanceState;
		else state=getIntent().getExtras();
		fac=Faculty.restoreFromBundle(state.getBundle("faculty"));
		
		String specList[]=new String[fac.size()];
		
		for(int i=0;i<fac.size();i++){
			specList[i]=fac.at(i).getName();
		}
		
		adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,specList);
		setListAdapter(adapter);
	}

	protected void onListItemClick(ListView listv,View view,int pos,long id){
		String url=fac.at(pos).getUrl();
		Intent intent=new Intent(this,RaspActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("url",url);
		startActivity(intent);
	}
	protected void onSaveInstanceState(Bundle state){
		if(fac!=null)fac.putToBundle(state);
	}
}
