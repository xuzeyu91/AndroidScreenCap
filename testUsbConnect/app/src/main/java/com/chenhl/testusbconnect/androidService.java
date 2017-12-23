package com.chenhl.testusbconnect;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class androidService extends Service
{

	public static final String TAG = "chl";
	public static Boolean mainThreadFlag = true;
	public static Boolean ioThreadFlag = true;
	ServerSocket serverSocket = null;
	final int SERVER_PORT = 10086;

	File testFile;
	private sysBroadcastReceiver sysBR;

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.d(TAG, "androidService--->onCreate()");
		/* 创建内部类sysBroadcastReceiver 并注册registerReceiver */
		sysRegisterReceiver();
		/*
		 * new Thread() { public void run() { doListen(); }; }.start();
		 */
	}

	private void doListen()
	{
		serverSocket = null;
		try
		{
			Log.e("chl", "doListen()");
			serverSocket = new ServerSocket(SERVER_PORT);
			Log.e("chl", "doListen() 2");
			Socket socket = serverSocket.accept();
			Log.e("chl", "doListen() 3");
			new Thread(new ThreadReadWriterIOSocket(this, socket)).start();

		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/* 创建内部类sysBroadcastReceiver 并注册registerReceiver */
	private void sysRegisterReceiver()
	{
		Log.v("chl", Thread.currentThread().getName() + "---->" + "sysRegisterReceiver");
		sysBR = new sysBroadcastReceiver();
		/* 注册BroadcastReceiver */
		IntentFilter filter1 = new IntentFilter();
		/* 新的应用程序被安装到了设备上的广播 */
		filter1.addAction("android.intent.action.PACKAGE_ADDED");
		filter1.addDataScheme("package");
		filter1.addAction("android.intent.action.PACKAGE_REMOVED");
		filter1.addDataScheme("package");
		registerReceiver(sysBR, filter1);
	}

	/* 内部类：BroadcastReceiver 用于接收系统事件 */
	private class sysBroadcastReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (action.equalsIgnoreCase("android.intent.action.PACKAGE_ADDED"))
			{
				// ReadInstalledAPP();
			} else if (action.equalsIgnoreCase("android.intent.action.PACKAGE_REMOVED"))
			{
				// ReadInstalledAPP();
			}
			Log.v(TAG, Thread.currentThread().getName() + "---->" + "sysBroadcastReceiver onReceive");
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.d("chl", "androidService----->onStartCommand()");
		mainThreadFlag = true;
		new Thread()
		{
			public void run()
			{
				doListen();
			};
		}.start();
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		// 关闭线程
		mainThreadFlag = false;
		ioThreadFlag = false;
		// 关闭服务器
		try
		{
			Log.v(TAG, Thread.currentThread().getName() + "---->" + "serverSocket.close()");
			if (serverSocket != null) serverSocket.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		Log.v(TAG, Thread.currentThread().getName() + "---->" + "**************** onDestroy****************");
	}

}
