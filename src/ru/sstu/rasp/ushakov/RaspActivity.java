package ru.sstu.rasp.ushakov;

import java.io.IOException;

import sstuclient.*;
import android.os.*;
import android.util.Log;
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
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Log.i("RaspActivity", "Try to start Faculty Activity");
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
	public void onCreateContextMenu(ContextMenu menu,View v,ContextMenuInfo inf){
		super.onCreateContextMenu(menu,v,inf);
		MenuInflater inflanter=getMenuInflater();
		inflanter.inflate(menusave?R.menu.rasp_save:R.menu.rasp,menu);
	}
	public boolean onContextItemSelected(MenuItem item){
		return onOptionsItemSelected(item);
	}
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflanter=getMenuInflater();
		inflanter.inflate(menusave?R.menu.rasp_save:R.menu.rasp,menu);
		return true;
	}
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
	protected void onStop(){
		super.onStop();
	}
	protected void onDestroy(){
		super.onDestroy();
		if(t!=null&&t.isAlive())t.interrupt();
	}
}
