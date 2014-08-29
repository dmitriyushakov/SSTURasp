package ru.sstu.rasp.ushakov;

import java.util.*;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import sstuclient.Day;

public class TimerActivity extends Activity{
	private TimerView view;
	private Runnable updateRunnable=new Runnable(){
		@Override
		public void run(){
			Calendar cal;
			while(true){
				cal=Calendar.getInstance();
				int time=60000-cal.get(Calendar.SECOND)*1000-cal.get(Calendar.MILLISECOND);
				try {
					Thread.sleep(time);
				} catch (InterruptedException e) {
					return;
				}
				view.postInvalidate();
			}
		}
	};
	private Thread updateThread;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		View decorView=getWindow().getDecorView();
		try {
			View.class.getMethod("setSystemUiVisibility",int.class);
			decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		Day day=(Day)getIntent().getExtras().getParcelable("day");
		view=new TimerView(this,day,decorView);
		setContentView(view);
		
		Window win=getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		WindowManager.LayoutParams lay=win.getAttributes();
		try {
			WindowManager.LayoutParams.class.getField("buttonBrightness");
			lay.buttonBrightness=WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		lay.screenBrightness=0.03f;
		win.setAttributes(lay);
	}
	@Override
	protected void onStart(){
		super.onStart();
		if(updateThread!=null&&!updateThread.isInterrupted())updateThread.interrupt();
		updateThread=new Thread(updateRunnable);
		updateThread.start();
	}
	@Override
	protected void onStop(){
		super.onStop();
		view.stopWait();
		updateThread.interrupt();
	}
}
