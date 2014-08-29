package ru.sstu.rasp.ushakov;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import sstuclient.*;

public class TimerView extends View{
	private Paint paint;
	private RectF rect;
	private Rect box;
	private int max;
	private int time;
	private boolean sizeSelected=false;
	private boolean showTime;
	private Day day;
	private View decorView;
	private final static int WHITE=0xFFFFFFFF;
	private final static int BLACK=0xFF000000;
	private Thread hideThread;
	private Context cont;
	private Handler hideHandler=new Handler(){
		@Override
		public void handleMessage(Message msg){
			if(msg.what==0){
				System.out.println("Would set");
				decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			}
		}
	};
	private Runnable hideRunnable=new Runnable(){
		@Override
		public void run(){
			System.out.println("Thread started");
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				System.out.println("Interrupted");
				return;
			}
			System.out.println("Would send");
			hideHandler.sendEmptyMessage(0);
		}
	};
	
	TimerView(Context context,Day day,View decorView){
		super(context);
		cont=context;
		paint=new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		rect=new RectF();
		this.day=day;
		this.decorView=decorView;
		box=new Rect();
	}
	private void setRectPadding(int padding,int w,int h){
		if(h>w){
			rect.set(padding,h/2-(w/2-padding),w-padding,h/2+(w/2-padding));
		}else{
			rect.set(w/2-(h/2-padding),padding,w/2+(h/2-padding),h-padding);
		}
	}
	private void updateValues(){
		if(day==null){
			showTime=false;
			return;
		}
		Time now=Time.now();
		showTime=true;
		time=0;
		for(int i=0;i<day.size();i++){
			Pair pair=day.at(i);
			if(pair.isBefore(now)){
				time=now.different(pair.getStart());
				if(i!=0){
					max=pair.getStart().different(day.at(i-1).getEnd());
				}else{
					max=0;
				}
				return;
			}else if(pair.isInside(now)){
				max=pair.getStart().different(pair.getEnd());
				time=now.different(pair.getEnd());
				return;
			}
		}
		showTime=false;
	}
	@Override
	public void onDraw(Canvas canv){
		updateValues();
		int w=getWidth();
		int h=getHeight();
		
		paint.setColor(BLACK);
		canv.drawRect(0,0,w,h,paint);
		
		if(!showTime){
			paint.setColor(WHITE);
			String nopStr=cont.getString(R.string.no_pairs);
			float size=paint.getTextSize();
			paint.getTextBounds(nopStr,0,nopStr.length(),box);
			float realWidth=box.width();
			float factor=(w-40)/realWidth;
			paint.setTextSize(factor*size);
			paint.getTextBounds(nopStr,0,nopStr.length(),box);
			int xpos=(w-box.width())/2;
			int ypos=(h+box.height())/2;
			canv.drawText(nopStr,xpos,ypos,paint);
			return;
		}
		
		paint.setColor(WHITE);
		setRectPadding(20,w,h);
		canv.drawOval(rect,paint);
		paint.setColor(BLACK);
		setRectPadding(50,w,h);
		canv.drawOval(rect,paint);
		setRectPadding(60,w,h);
		
		paint.setColor(WHITE);
		if(max!=0){
			setRectPadding(70,w,h);
			float startAngle=360*(max-time)/max-90;
			float sweepAngle=360*time/max;
			canv.drawArc(rect,startAngle,sweepAngle,true,paint);
		}
		
		if(!sizeSelected){
			sizeSelected=true;
			if(h>w)paint.setTextSize((h-w)/3);
			else{
				paint.setTextSize(h);
				paint.getTextBounds("00:00",0,5,box);
				int realWidth=box.width();
				int needWidth=(w-h)/2-20;
				float factor=((float)needWidth)/realWidth;
				paint.setTextSize(factor*h);
			}
		}
		
		if(h>w){
			String timeStr=getTimeString();
			paint.getTextBounds(timeStr,0,timeStr.length(),box);
			int timeXPos=(w-box.width())/2;
			int timeYPos=((h-w)/2+box.height())/2;
			canv.drawText(timeStr,timeXPos,timeYPos,paint);
			
			timeStr=Integer.toString(time);
			paint.getTextBounds(timeStr,0,timeStr.length(),box);
			timeXPos=(w-box.width())/2;
			timeYPos=((int)(h*1.5)+w/2+box.height())/2;
			canv.drawText(timeStr,timeXPos,timeYPos,paint);
		}else{
			String timeStr=getTimeString();
			paint.getTextBounds(timeStr,0,timeStr.length(),box);
			int timeXPos=(w-h)/4-box.width()/2;
			int timeYPos=(h+box.height())/2;
			canv.drawText(timeStr,timeXPos,timeYPos,paint);
			
			timeStr=Integer.toString(time);
			paint.getTextBounds(timeStr,0,timeStr.length(),box);
			timeXPos=w*3/4+h/4-box.width()/2;
			timeYPos=(h+box.height())/2;
			canv.drawText(timeStr,timeXPos,timeYPos,paint);
		}
	}
	private String getTimeString(){
		Calendar c=Calendar.getInstance();
		int min=c.get(Calendar.MINUTE);
		return c.get(Calendar.HOUR_OF_DAY)+(min<10?":0":":")+min;
	}
	public void stopWait(){
		if(hideThread!=null&&!hideThread.isInterrupted()){
			hideThread.interrupt();
		}
	}
	@Override
	public void onSizeChanged(int w,int h,int ow,int oh){
		if(w<ow&&h==oh||h<oh&&w==ow){
			if(hideThread!=null&&!hideThread.isInterrupted()){
				hideThread.interrupt();
			}
			hideThread=new Thread(hideRunnable);
			hideThread.start();
		}
	}
}