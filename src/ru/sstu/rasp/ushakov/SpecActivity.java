package ru.sstu.rasp.ushakov;

import java.io.IOException;

import android.os.*;
import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.widget.*;
import sstuclient.*;

public class SpecActivity extends ListActivity {
	ArrayAdapter<String> adapter;
	private Faculty fac;
	private Handler handler;
	private Thread thread;
	private String title;
	@SuppressLint("HandlerLeak")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle state;
		if(savedInstanceState!=null)state=savedInstanceState;
		else state=getIntent().getExtras();
		title=state.getString("title");
		if(title!=null)setTitle(title);
		fac=Faculty.restoreFromBundle(state.getBundle("faculty"));
		
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
				try {
					fac.syncInit();
					if(!Thread.interrupted())handler.sendEmptyMessage(0);
				}catch (IOException e) {
					e.printStackTrace();
					if(!Thread.interrupted())handler.sendEmptyMessage(1);
				}
			}
		};
		
		thread=new Thread(runnable);
		thread.start();
	}
	
	private void onInit(){
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
		if(fac!=null){
			Bundle bundle=new Bundle();
			fac.putToBundle(bundle);
			state.putBundle("faculty",bundle);
		}
		if(title!=null)state.putString("title",title);
	}
	protected void onStop(){
		super.onStop();
		thread.interrupt();
	}
}
