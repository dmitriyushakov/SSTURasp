package ru.sstu.rasp.ushakov;

import android.app.*;
import android.content.*;

public class ErrorDialog {
	public static AlertDialog internetFailDialog(Context context,DialogInterface.OnClickListener listener){
		AlertDialog.Builder builder=new AlertDialog.Builder(context);
		
		builder.setTitle(R.string.internet_fail_title);
		builder.setCancelable(false);
		builder.setMessage(R.string.internet_fail_text);
		builder.setPositiveButton(R.string.internet_fail_ok,listener);
		
		return builder.create();
	}
}
