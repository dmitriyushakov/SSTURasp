package ru.sstu.rasp.ushakov;

import java.io.IOException;

import android.os.*;
import android.view.*;
import android.widget.*;
import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import sstuclient.*;

public class FacultyActivity extends ListActivity {
	private ArrayAdapter<String> adapter;
	private Handler handler;
	private Thread thread;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		handler=new Handler(){
			@Override
			public void handleMessage(Message msg){
				if(msg.what==0)onInit();
				else{
					OnClickListener listener=new OnClickListener(){
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							finish();
						}
					};
					ErrorDialog.internetFailDialog(getBaseContext(),listener);
				}
			}
		};
		Runnable runnable=new Runnable(){
			@Override
			public void run(){
				boolean error=false;
				try {
					FacultyList.syncInit(getApplicationContext());
				} catch (IOException e) {
					error=true;
					e.printStackTrace();
				}
				if(Thread.interrupted())return;
				if(!error){
					handler.sendEmptyMessage(0);
				}else handler.sendEmptyMessage(1);
			}
		};
		thread=new Thread(runnable);
		thread.start();
	}
	
	private void onInit(){
		String lines[]=new String[FacultyList.size()];
		for(int i=0;i<FacultyList.size();i++)
			lines[i]=FacultyList.getFaculty(i).getName();
		
		adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,lines);
		
		setListAdapter(adapter);
	}
	
	protected void onListItemClick(ListView listv,View view,int pos,long id){
		Intent intent=new Intent(this,SpecActivity.class);
		Bundle faculty=new Bundle();
		FacultyList.getFaculty(pos).putToBundle(faculty);
		intent.putExtra("faculty",faculty);
		startActivity(intent);
	}
	@Override
	protected void onStop(){
		super.onStop();
		thread.interrupt();
	}
}
