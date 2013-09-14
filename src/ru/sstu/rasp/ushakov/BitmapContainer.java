package ru.sstu.rasp.ushakov;

import android.graphics.*;

public class BitmapContainer {
	private boolean secondSwitch;
	
	private boolean firstUse;
	private int firstX;
	private boolean firstEven;
	private static Bitmap firstBmp;
	private boolean firstToRedraw;
	
	private boolean secondUse;
	private int secondX;
	private boolean secondEven;
	private static Bitmap secondBmp;
	private boolean secondToRedraw;
	
	public void setSize(int width,int height){
		if(firstBmp==null||width!=firstBmp.getWidth()||height!=firstBmp.getHeight()){
			if(firstBmp!=null)firstBmp.recycle();
			if(secondBmp!=null)secondBmp.recycle();
			firstBmp=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
			secondBmp=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
			secondUse=false;
			firstUse=false;
			firstToRedraw=true;
			secondToRedraw=true;
		}
		//System.gc();
	}
	public void clean(){
		firstUse=false;
		secondUse=false;
	}
	public Bitmap getBitmap(int daynum,boolean even){
		if(firstUse&&firstX==daynum&&firstEven==even){
			return firstBmp;
		}
		if(secondUse&&secondX==daynum&&secondEven==even){
			return secondBmp;
		}
		secondSwitch=!secondSwitch;
		secondToRedraw=true;
		firstToRedraw=true;
		if(secondSwitch){
			secondUse=true;
			secondX=daynum;
			secondEven=even;
			return secondBmp;
		}else{
			firstUse=true;
			firstX=daynum;
			firstEven=even;
			return firstBmp;
		}
	}
	
	public boolean needRedraw(int daynum,boolean even){
		if(firstUse&&firstX==daynum&&firstEven==even){
			boolean res=firstToRedraw;
			firstToRedraw=false;
			return res;
		}
		if(secondUse&&secondX==daynum&&secondEven==even){
			boolean res=secondToRedraw;
			secondToRedraw=false;
			return res;
		}
		return true;
	}
	
	public BitmapContainer(){
		secondSwitch=false;
		firstUse=false;
		firstX=-1;
		firstEven=false;
		if(firstBmp!=null)firstBmp.recycle();
		firstBmp=null;
		secondUse=false;
		secondX=-1;
		secondEven=false;
		if(secondBmp!=null)secondBmp.recycle();
		secondBmp=null;
		firstToRedraw=true;
		secondToRedraw=true;
	}
}
