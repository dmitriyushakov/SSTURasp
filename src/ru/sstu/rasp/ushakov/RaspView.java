package ru.sstu.rasp.ushakov;

import sstuclient.*;
import android.annotation.SuppressLint;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.util.Log;
import android.view.*;

import java.util.*;

public class RaspView extends View{
	private static int STAY_MODE=0;
	private static int MOVE_X_MODE=1;
	private static int MOVE_Y_MODE=2;
	private Paint paint;
	private int mode;
	private Speciality spec;
	private static Bitmap evenDaysBmp[];
	private static Bitmap nevenDaysBmp[];
	private static Thread redrawth;
	private int xpos;
	private int ypos;
	private boolean drawed;
	
	private float kdx;
	private float kdy;
	private float kspeed;
	private long kprev;
	private Handler khandler;
	private Thread kthread;
	private Runnable krunnable;
	
	private boolean isTouched;
	private int touchId;
	private int lockx;
	private int locky;
	private int startx;
	private int starty;
	
	private int restx;
	private int resty;
	private boolean restored;
	
	private Handler menuhandl;
	
	public void saveInstance(Bundle bundle){
		bundle.putInt("viewx",xpos/getWidth());
		bundle.putInt("viewy",ypos/getHeight());
	}
	public void restoreInstance(Bundle bundle){
		restored=true;
		restx=bundle.getInt("viewx");
		resty=bundle.getInt("viewy");
		xpos=restx*getWidth();
		ypos=resty*getHeight();
	}
	public void drawBitmaps(Speciality spec){
		reallocateBitmaps(getWidth(),getHeight());
		for(int i=0;i<6;i++){
			Day day=spec.getDay(i,false);
			drawBitmap(day,nevenDaysBmp[i]);
			
			day=spec.getDay(i,true);
			drawBitmap(day,evenDaysBmp[i]);
		}
	}
	private void allocateBitmaps(int width,int height){
		System.gc();
		if(evenDaysBmp==null){
			evenDaysBmp=new Bitmap[6];
			for(int i=0;i<6;i++)evenDaysBmp[i]=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
		}
		if(nevenDaysBmp==null){
			nevenDaysBmp=new Bitmap[6];
			for(int i=0;i<6;i++)nevenDaysBmp[i]=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
		}
	}
	private void reallocateBitmaps(int width,int height){
		if(evenDaysBmp==null||nevenDaysBmp==null){
			allocateBitmaps(width,height);
		}else{
			Bitmap bmp=evenDaysBmp[0];
			if(bmp.getHeight()!=height||bmp.getWidth()!=width){
				deleteBitmaps();
				allocateBitmaps(width,height);
			}
		}
	}
	private void drawBitmap(Day day,Bitmap bmp){
		drawBitmap(day,bmp,null);
	}
	private void drawBitmap(Day day,Canvas canv){
		drawBitmap(day,null,canv);
	}
	private void drawBitmap(Day day,Bitmap bmp,Canvas canv){
		if(bmp!=null){
			canv=new Canvas(bmp);
		}
		Paint paint=new Paint();
		
		paint.setAntiAlias(true);
		paint.setColor(0xFFFFFFFF);
		paint.setStyle(Paint.Style.FILL);
		paint.setTextSize(20);
		canv.drawRect(0,0,getWidth(),getHeight(),paint);
		
		String dayName=getDayName(day.dayOfWeek());
		if(day.isEven()){
			paint.setColor(0xFF6699FF);
		}else{
			paint.setColor(0xFFFF3333);
		}
		canv.drawRect(0,0,getWidth(),paint.getTextSize()+8,paint);
		
		paint.setColor(0xFFFFFFFF);
		paint.setStrokeWidth(2);
		canv.drawLine(0,paint.getTextSize()+8,getWidth(),paint.getTextSize()+8,paint);
		canv.drawText(dayName,4,24,paint);	
		
		Rect box=new Rect();
		
		String relativeDay=null;
		if(day.isToday(spec.isEven()))relativeDay=getResources().getString(R.string.today_day);
		else if(day.isTomorrow(spec.isEven()))relativeDay=getResources().getString(R.string.tomorrow_day);
		
		if(relativeDay!=null){
			paint.setColor(0xAAFFFFFF);
			paint.getTextBounds(relativeDay,0,relativeDay.length()-1,box);
			canv.drawText(relativeDay,getWidth()-40-box.width(),22,paint);
		}
		
		paint.setColor(0xFF000000);	
		
		Paint greyPaint=new Paint();
		greyPaint.setAntiAlias(true);
		greyPaint.setColor(0xFF888888);
		greyPaint.setStyle(Paint.Style.FILL);
		greyPaint.setStrokeWidth(2);
		int startItems=(int)paint.getTextSize()+8;
		int itemHeight=(getHeight()-startItems)/8;
		if(itemHeight>60)itemHeight=60;
		int timeSize=(itemHeight-12)/3;
		int topSize=(itemHeight-12)/2;
		int bottomSize=(itemHeight-12)/3;
		for(int i=0;i<day.size();i++){
			Pair pair=day.at(i);

			int itemStart=startItems+itemHeight*i;
			
			paint.setTextSize(timeSize);
			String startTime=pair.getStart().toString();
			String endTime=pair.getEnd().toString();
			String timeExample="00:00";
			paint.getTextBounds(timeExample,0,timeExample.length(),box);
			int timeWidth=box.width();
			canv.drawText(startTime,getWidth()-4-timeWidth,itemStart+4+timeSize,paint);
			canv.drawText(endTime,getWidth()-4-timeWidth,itemStart+8+timeSize*2,paint);
			
			greyPaint.setTextSize(topSize);
			String aud=pair.getAuditorium();
			canv.drawText(aud,4,itemStart+4+topSize,greyPaint);
			String audExample="0/0000";
			greyPaint.getTextBounds(audExample,0,audExample.length(),box);
			int audWidth=box.width();
			greyPaint.getTextBounds(aud,0,aud.length(),box);
			if(box.width()>audWidth)audWidth=box.width();
			
			canv.save();
			box.set(audWidth+40,0,getWidth()-8-audWidth,getHeight());
			canv.clipRect(box);
			canv.drawText(pair.getSubject(),audWidth+40,itemStart+4+topSize,paint);
			canv.restore();
			
			greyPaint.setTextSize(bottomSize);
			canv.save();
			box.left=4;
			canv.clipRect(box);
			canv.drawText(pair.getLecturer(),4,itemStart+8+topSize+bottomSize,greyPaint);
			canv.restore();
			
			int linePos=itemStart+12+topSize+bottomSize;
			canv.drawLine(0,linePos,getWidth(),linePos,greyPaint);
		}
	}
	public RaspView(Context context) {
		super(context);
	}
	private String getDayName(int day){
		int id=0;
		if(day==Day.MONDAY)id=R.string.monday;
		if(day==Day.TUESDAY)id=R.string.tuesday;
		if(day==Day.WEDNESDAY)id=R.string.wednesday;
		if(day==Day.THURSDAY)id=R.string.thursday;
		if(day==Day.FRIDAY)id=R.string.friday;
		if(day==Day.SATURDAY)id=R.string.saturday;
		return getResources().getString(id);
	}
	private int dayOfWeek(){
		Calendar calendar=new GregorianCalendar();
		int preResult=calendar.get(Calendar.DAY_OF_WEEK);
		if(preResult==Calendar.SUNDAY)return 0;
		else return preResult-2;
	}
	public void postInit(){
		kspeed=((float)getWidth())*3;
		paint=new Paint();
		mode=STAY_MODE;
		if(restored){
			xpos=restx*getWidth();
			ypos=resty*getHeight();
		}else{
			ypos=spec.isEven()?getHeight():0;
			xpos=dayOfWeek()*getWidth();
		}
		isTouched=false;
		if(redrawth!=null&&redrawth.isAlive()){
			try{
				redrawth.join();
			}catch(InterruptedException e){};
		}
		Runnable runnable=new Runnable(){
			public void run(){
				try{
					Thread.sleep(20);
				}catch(InterruptedException e){};
				drawBitmaps(spec);
				drawed=true;
				postInvalidate();
			}
		};
		redrawth=new Thread(runnable);
		redrawth.start();
		invalidate();
	}
	protected void onSizeChanged(int w,int h,int ow,int oh){
		postInit();
	}
	@SuppressLint("HandlerLeak")
	public void init(Speciality spec){
		this.spec=spec;
		drawed=false;
		
		menuhandl=new Handler(){
			public void handleMessage(Message msg){
				showContextMenu();
			}
		};
		restored=false;
		khandler=new Handler(){
			public void handleMessage(Message msg){
				long current=System.currentTimeMillis();
				float dt=((float)(current-kprev))/1000;
				if(Math.abs(dt)>0.1)dt=(float)0.1;
				kprev=current;
				tick(dt);
			}
		};
		krunnable=new Runnable(){
			public void run(){
				kprev=System.currentTimeMillis();
				while(true){
					try{
						Thread.sleep(15);
					}catch(InterruptedException e){
						break;
					};
					if(Thread.interrupted())break;
					khandler.sendEmptyMessage(0);
				}
			}
		};
	}
	public void onDraw(Canvas canv){
		if(!drawed){
			Day day=spec.getDay(xpos/getWidth(),ypos!=0);
			drawBitmap(day,canv);
			return;
		}
		if(ypos%getHeight()!=0){
			int xindex=xpos/getWidth();
			if(ypos<getHeight()){
				canv.drawBitmap(nevenDaysBmp[xindex],0,-ypos,paint);
				canv.drawBitmap(evenDaysBmp[xindex],0,getHeight()-ypos,paint);
			}else if(ypos>getHeight()){
				canv.drawBitmap(evenDaysBmp[xindex],0,getHeight()-ypos,paint);
				canv.drawBitmap(nevenDaysBmp[xindex],0,getHeight()*2-ypos,paint);
			}
		}else if(xpos%getWidth()!=0){
			Bitmap daysBmp[]=(ypos==0)?nevenDaysBmp:evenDaysBmp;
			Bitmap odaysBmp=(ypos==0)?evenDaysBmp[0]:nevenDaysBmp[0];
			int xindex=xpos/getWidth();
			int firstx=xindex*getWidth()-xpos;
			canv.drawBitmap(daysBmp[xindex],firstx,0,paint);
			if(xindex==5){
				canv.drawBitmap(odaysBmp,firstx+getWidth(),0,paint);
			}else{
				canv.drawBitmap(daysBmp[xindex+1],firstx+getWidth(),0,paint);
			}
		}else{
			int xindex=xpos/getWidth();
			canv.drawBitmap(ypos==0?nevenDaysBmp[xindex]:evenDaysBmp[xindex],0,0,paint);
		}
	}
	private void setPos(int x,int y){
		if(!drawed)return;
		while(x>=6*getWidth()){
			x-=getWidth()*6;
		}
		while(x<0){
			x+=getWidth()*6;
		}
		while(y>=2*getHeight())y-=2*getHeight();
		while(y<0)y+=2*getHeight();
		xpos=x;
		ypos=y;
		invalidate();
	}
	public boolean onTouchEvent(MotionEvent motion){
		if(!isTouched){
			if(motion.getAction()==MotionEvent.ACTION_DOWN){
				int x=(int)motion.getX();
				int y=(int)motion.getY();
				touchId=motion.getDeviceId();
				isTouched=true;
				onPointerPress(x,y);
			}
		}else{
			if(touchId==motion.getDeviceId()){
				if(motion.getAction()==MotionEvent.ACTION_MOVE){
					int x=(int)motion.getX();
					int y=(int)motion.getY();
					onPointerMove(x,y);
				}else if(motion.getAction()==MotionEvent.ACTION_UP){
					int x=(int)motion.getX();
					int y=(int)motion.getY();
					isTouched=false;
					onPointerUp(x,y);
				}
			}
		}
		return true;
	}
	private void onPointerPress(int x,int y){
		TimerTask ttask=new TimerTask(){
			public void run(){
				if(mode==STAY_MODE&&isTouched){
					menuhandl.sendEmptyMessage(0);
				}
			}
		};
		Timer timer=new Timer();
		timer.schedule(ttask,200);
		
		if(kthread!=null&&kthread.isAlive()){
			isTouched=false;
			return;
		}
		lockx=x;
		locky=y;
		startx=xpos;
		starty=ypos;
	}
	private void onPointerMove(int x,int y){
		int dx=x-lockx;
		int dy=y-locky;
		if(mode==STAY_MODE){
			if(Math.abs(dx)>15)mode=MOVE_X_MODE;
			if(Math.abs(dy)>15)mode=MOVE_Y_MODE;
		}
		if(mode==MOVE_X_MODE){
			int nxpos=startx-dx;
			if(nxpos<0||nxpos>getWidth()*6)dy=getHeight();
			else dy=0;
			setPos(startx-dx,starty-dy);
		}else if(mode==MOVE_Y_MODE){
			setPos(startx,starty-dy);
		}
	}
	private void onPointerUp(int x,int y){
		kdx=(float)(x-lockx);
		kdy=(float)(y-locky);
		if(mode!=STAY_MODE){
			kthread=new Thread(krunnable);
			kthread.start();
		}
	}
	private void kineticStop(){
		kdx=0;
		kdy=0;
		mode=STAY_MODE;
		kthread.interrupt();
	}
	private void tick(float dt){
		if(mode==MOVE_X_MODE){
			float dx=dt*kspeed;
			if(Math.abs(kdx)<getWidth()/5){
				if(kdx<0){
					kdx+=dx;
					if(kdx>=0)kineticStop();
				}else if(kdx>0){
					kdx-=dx;
					if(kdx<=0)kineticStop();
				}else kineticStop();
			}else{
				if(kdx<0){
					kdx-=dx;
					if(kdx<=-getWidth()){
						kineticStop();
						kdx=-getWidth();
					}
				}else if(kdx>0){
					kdx+=dx;
					if(kdx>=getWidth()){
						kineticStop();
						kdx=getWidth();
					}
				}
			}
			int nxpos=(int)(startx-kdx);
			int dy=0;
			if(nxpos<0||nxpos>=getWidth()*6)dy=getHeight();
			else dy=0;
			setPos(nxpos,starty-dy);
		}else if(mode==MOVE_Y_MODE){
			float dy=dt*kspeed;
			if(Math.abs(kdy)<getHeight()/5){
				if(kdy<0){
					kdy+=dy;
					if(kdy>=0)kineticStop();
				}else if(kdy>0){
					kdy-=dy;
					if(kdy<=0)kineticStop();
				}else kineticStop();
			}else{
				if(kdy<0){
					kdy-=dy;
					if(kdy<=-getHeight()){
						kineticStop();
						kdy=-getHeight();
					}
				}else if(kdy>0){
					kdy+=dy;
					if(kdy>=getHeight()){
						kineticStop();
						kdy=getHeight();
					}
				}
			}
			setPos(startx,(int)(starty-kdy));
		}
	}
	private void deleteBitmaps(){
		drawed=false;
		if(evenDaysBmp!=null&&nevenDaysBmp!=null){
			for(int i=0;i<6;i++){
				evenDaysBmp[i].recycle();
				nevenDaysBmp[i].recycle();
			}
			evenDaysBmp=null;
			nevenDaysBmp=null;
		}
	}
}
