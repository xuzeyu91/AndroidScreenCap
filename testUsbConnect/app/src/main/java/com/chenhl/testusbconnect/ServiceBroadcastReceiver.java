package com.chenhl.testusbconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceBroadcastReceiver extends BroadcastReceiver {

	private static String START_ACTION = "NotifyServiceStart";
	private static String STOP_ACTION = "NotifyServiceStop";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(androidService.TAG, Thread.currentThread().getName() + "---->"  
                + "ServiceBroadcastReceiver onReceive");  
  
        String action = intent.getAction();  
        if (START_ACTION.equalsIgnoreCase(action)) {  
            context.startService(new Intent(context, androidService.class));  
  
            Log.d(androidService.TAG, Thread.currentThread().getName() + "---->"  
                    + "ServiceBroadcastReceiver onReceive start end");  
        } else if (STOP_ACTION.equalsIgnoreCase(action)) {  
            context.stopService(new Intent(context, androidService.class));  
            Log.d(androidService.TAG, Thread.currentThread().getName() + "---->"  
                    + "ServiceBroadcastReceiver onReceive stop end");  
        }  
	}

}
