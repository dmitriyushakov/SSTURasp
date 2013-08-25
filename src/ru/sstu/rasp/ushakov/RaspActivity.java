package ru.sstu.rasp.ushakov;

import java.io.IOException;
import sstuclient.*;
import android.os.*;
import android.view.*;
import android.view.ContextMenu.*;
import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;

public class RaspActivity extends Activity {
	private Speciality spec;
	private String url;
	private Handler handler;
	private Handler updhandler;
	private boolean menusave;
	private Thread t;
	private boolean _showfail;
	private boolean lecturer;
	RaspView view;
	private void gotoFacultySelector(){
		Intent i=new Intent(this,FacultyActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}
	@SuppressLint("HandlerLeak")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		_showfail=false;
		Bundle extras=getIntent().getExtras();
		if(extras!=null){
			lecturer=extras.getBoolean("lecturer");
		}else{
			lecturer=false;
		}

		handler=new Handler(){
			public void handleMessage(Message msg){
				if(msg.what==0)onSpecGet();
				else if(msg.what==1){
					showInternetFail();
				}
			}
		};
		
		if(savedInstanceState!=null){
			spec=Speciality.restoreFromBundle(savedInstanceState);
			if(spec==null)showInternetFail();
			else{
				onSpecGet();
				menusave=savedInstanceState.getBoolean("menusave",false);
				view.restoreInstance(savedInstanceState);
				_showfail=savedInstanceState.getBoolean("showfail",false);
				if(_showfail)showInternetFail();
			}
		}else if(extras!=null&&extras.containsKey("url")){
			url=extras.getString("url");
			
			Runnable runnable=new Runnable(){
				public void run(){
					try{
						if(lecturer){
							spec=Speciality.getLecturer(url);
						}else{
							spec=Speciality.getSpeciality(url);
						}
						if(Thread.interrupted())return;
						boolean saved=Speciality.isSaved(getFilesDir().getAbsolutePath());
						menusave=saved;
						if(!saved)spec.save(getFilesDir().getAbsolutePath());
						if(Thread.interrupted())return;
						handler.sendEmptyMessage(0);
					}catch(IOException e){
						handler.sendEmptyMessage(1);
					}
				}
			};
			
			t=new Thread(runnable);
			t.start();
		}else if(Speciality.isSaved(getFilesDir().getAbsolutePath())){
			spec=Speciality.restore(getFilesDir().getAbsolutePath());
			onSpecGet();
			menusave=false;
		}else{
			gotoFacultySelector();
		}
	}
	public void onCreateContextMenu(ContextMenu menu,View v,ContextMenuInfo inf){
		super.onCreateContextMenu(menu,v,inf);
		MenuInflater inflanter=getMenuInflater();
		inflanter.inflate(menusave?R.menu.rasp_save:R.menu.rasp,menu);
	}
	private void showInternetFail(){
		_showfail=true;
		DialogInterface.OnClickListener listener=new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				_showfail=false;
				if(view==null){
					gotoFacultySelector();
				}
			}
		};
		ErrorDialog.internetFailDialog(this,listener).show();
	}
	
	public boolean showOthers(MenuItem item){
		Intent i=new Intent(this,FacultyActivity.class);
		startActivity(i);
		return true;
	}
	public boolean saveRasp(MenuItem item){
		spec.save(getFilesDir().getAbsolutePath());
		menusave=false;
		return true;
	}
	@SuppressLint("HandlerLeak")
	public boolean updateRasp(MenuItem item){
		if(updhandler==null)updhandler=new Handler(){
			public void handleMessage(Message msg){
				onSpecGet();
				spec.save(getFilesDir().getAbsolutePath());
			}
		};
		Runnable runnable=new Runnable(){
			public void run(){
				try{
					if(spec.isLecturer()){
						spec=Speciality.getLecturer(spec.getUrl());
					}else{
						spec=Speciality.getSpeciality(spec.getUrl());
					}
					if(Thread.interrupted())return;
					spec.save(getFilesDir().getAbsolutePath());
					if(Thread.interrupted())return;
					updhandler.sendEmptyMessage(0);
				}catch(IOException e){
					handler.sendEmptyMessage(1);
				}
			}
		};
		t=new Thread(runnable);
		t.start();
		
		return true;
	}
	public boolean showAbout(MenuItem item){
		Intent intent=new Intent(this,AboutActivity.class);
		startActivity(intent);
		return true;
	}
	public boolean gotoHome(MenuItem item){
		spec=Speciality.restore(getFilesDir().getAbsolutePath());
		view.init(spec);
		view.postInit();
		menusave=false;
		return true;
	}
	
	protected void onSaveInstanceState(Bundle state){
		if(spec!=null)spec.putToBundle(state);
		if(spec!=null)view.saveInstance(state);
		state.putBoolean("showfail",_showfail);
		state.putBoolean("menusave",menusave);
	}
	private void onSpecGet(){
		if(view==null){
			view=new RaspView(this);
			view.init(spec);
			registerForContextMenu(view);
			setContentView(view);
		}else{
			view.drawBitmaps(spec);
			view.invalidate();
		}
	}
	protected void onStop(){
		super.onStop();
	}
	protected void onDestroy(){
		super.onDestroy();
		if(t!=null&&t.isAlive())t.interrupt();
	}
}
