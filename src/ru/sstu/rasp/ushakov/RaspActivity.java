package ru.sstu.rasp.ushakov;

import java.io.IOException;
import java.util.*;

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
	private Thread redrawThread;
	private boolean _showfail;
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

		handler=new Handler(){
			public void handleMessage(Message msg){
				if(msg.what==0)onSpecGet();
				else if(msg.what==1){
					showInternetFail();
				}
			}
		};
		
		if(savedInstanceState!=null){
			spec=savedInstanceState.getParcelable("spec");
			if(spec==null)finish();
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
						spec=Speciality.getSpeciality(url);
						if(Thread.interrupted())return;
						boolean saved=Speciality.isSaved(getFilesDir().getAbsolutePath());
						menusave=saved;
						if(!saved)spec.save(getFilesDir().getAbsolutePath());
						if(Thread.interrupted())return;
						handler.sendEmptyMessage(0);
					}catch(IOException e){
						e.printStackTrace();
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
	@Override
	protected void onStart(){
		super.onStart();
		
		Runnable redrawRunnable=new Runnable(){
			public void run(){
				Calendar cal;
				while(true){
					cal=Calendar.getInstance();
					int timeSleep=60000-cal.get(Calendar.SECOND)*1000-cal.get(Calendar.MILLISECOND);
					try {
						Thread.sleep(timeSleep);
					} catch (InterruptedException e) {
						break;
					}
					if(view!=null)view.postRedraw();
				}
			}
		};
		redrawThread=new Thread(redrawRunnable);
		redrawThread.start();
		if(view!=null)view.postRedraw();
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu,View v,ContextMenuInfo inf){
		super.onCreateContextMenu(menu,v,inf);
		MenuInflater inflanter=getMenuInflater();
		inflanter.inflate(menusave?R.menu.rasp_save:R.menu.rasp,menu);
	}
	@Override
	public boolean onContextItemSelected(MenuItem item){
		return onOptionsItemSelected(item);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflanter=getMenuInflater();
		inflanter.inflate(menusave?R.menu.rasp_save:R.menu.rasp,menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.other:
			showOthers();
			return true;
		case R.id.save:
			saveRasp();
			return true;
		case R.id.update:
			updateRasp();
			return true;
		case R.id.about:
			showAbout();
			return true;
		case R.id.gotohome:
			gotoHome();
			return true;
		default:
			return false;
		}
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
	
	public void showOthers(){
		Intent i=new Intent(this,FacultyActivity.class);
		startActivity(i);
	}
	public void saveRasp(){
		spec.save(getFilesDir().getAbsolutePath());
		menusave=false;
	}
	@SuppressLint("HandlerLeak")
	public void updateRasp(){
		if(updhandler==null)updhandler=new Handler(){
			public void handleMessage(Message msg){
				onSpecGet();
				spec.save(getFilesDir().getAbsolutePath());
			}
		};
		Runnable runnable=new Runnable(){
			public void run(){
				try{
					spec=Speciality.getSpeciality(spec.getUrl());
					spec.save(getFilesDir().getAbsolutePath());
					updhandler.sendEmptyMessage(0);
				}catch(IOException e){
					handler.sendEmptyMessage(1);
				}
			}
		};
		t=new Thread(runnable);
		t.start();
	}
	public void showAbout(){
		Intent intent=new Intent(this,AboutActivity.class);
		startActivity(intent);
	}
	public void gotoHome(){
		spec=Speciality.restore(getFilesDir().getAbsolutePath());
		view.init(spec);
		view.postInit();
		menusave=false;
	}
	@Override
	protected void onSaveInstanceState(Bundle state){
		if(spec!=null)state.putParcelable("spec",spec);
		if(view!=null)view.saveInstance(state);
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
			view.setSpeciality(spec);
			view.invalidate();
		}
	}
	@Override
	protected void onStop(){
		super.onStop();
		if(redrawThread!=null){
			redrawThread.interrupt();
			redrawThread=null;
		}
	}
	@Override
	protected void onDestroy(){
		super.onDestroy();
		if(t!=null&&t.isAlive())t.interrupt();
	}
}
