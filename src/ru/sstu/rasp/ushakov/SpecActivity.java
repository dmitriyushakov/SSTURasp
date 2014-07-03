package ru.sstu.rasp.ushakov;

import java.io.IOException;
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
	private boolean toLecturers;
	private boolean toAuditory;
	private String num;
	private Handler handler;
	@SuppressLint("HandlerLeak")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle extras=getIntent().getExtras();
		num=extras.getString("facnum");
		toLecturers=extras.getBoolean("toLecturers",false);
		toAuditory=extras.getBoolean("toAuditory",false);
		if(toLecturers)this.setTitle(R.string.lecturers);
		if(toAuditory)this.setTitle(R.string.auds);
		
		if(savedInstanceState!=null){
			fac=Faculty.restoreFromBundle(savedInstanceState);
			if(fac==null)showInternetFail();
			else onGetData(fac);
		}else{
			handler=new Handler(){
				public void handleMessage(Message msg){
					if(msg.what==0)onGetData(fac);
					else if(msg.what==1){
						showInternetFail();
					}
				}
			};
			
			Runnable runnable=new Runnable(){
				public void run(){
					try{
						if(toLecturers){
							fac=Faculty.getLecturers();
						}else if(toAuditory){
							fac=Faculty.getAuditories();
						}else{
							fac=Faculty.getFaculty(num);
						}
						handler.sendEmptyMessage(0);
					}catch(IOException e){
						e.printStackTrace();
						handler.sendEmptyMessage(1);
					}
				}
			};
			
			Thread thread=new Thread(runnable);
			thread.start();
		}
	}
	private void onGetData(Faculty fac){
		String specList[]=new String[fac.size()];
		
		for(int i=0;i<fac.size();i++){
			specList[i]=fac.at(i).getName();
		}
		
		adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,specList);
		setListAdapter(adapter);
	}
	private void showInternetFail(){
		DialogInterface.OnClickListener listener=new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		};
		ErrorDialog.internetFailDialog(this,listener).show();
	}
	protected void onListItemClick(ListView listv,View view,int pos,long id){
		String url=fac.at(pos).getUrl();
		Intent intent=new Intent(this,RaspActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("url",url);
		intent.putExtra("lecturer",toLecturers);
		intent.putExtra("auditory",toAuditory);
		startActivity(intent);
	}
	protected void onSaveInstanceState(Bundle state){
		if(fac!=null)fac.putToBundle(state);
	}
}
