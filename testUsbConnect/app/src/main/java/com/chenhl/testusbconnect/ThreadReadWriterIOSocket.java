package com.chenhl.testusbconnect;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.chenhl.testusbconnect.utils.FileHelper;
import com.chenhl.testusbconnect.utils.MyUtil;

public class ThreadReadWriterIOSocket implements Runnable
{
	private Socket client;
	private Context context;

	public ThreadReadWriterIOSocket(Context context, Socket client)
	{
		this.client = client;
		this.context = context;
		run();
	}

    Process process = null;
    DataOutputStream os = null;
    BufferedOutputStream out;
	@Override
	public void run()
	{
		Log.d("chl", "a client has connected to server!");
		BufferedInputStream in;
		//command
		final String command = "screencap -p /sdcard/screen.jpg";

		try
		{
			/* PC端发来的数据msg */
			String currCMD = "";
			out = new BufferedOutputStream(client.getOutputStream());
			in = new BufferedInputStream(client.getInputStream());
			androidService.ioThreadFlag = true;
			//command

			while (androidService.ioThreadFlag)
			{
				try
				{
					if (!client.isConnected())
					{
						break;
					}
					/* 接收PC发来的数据 */
					Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "will read......");
					/* 读操作命令 */
					currCMD = readCMDFromSocket(in);
					Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "**currCMD ==== " + currCMD);
					if(currCMD.equals("start")){
                        Thread thread=new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                while(true) {
                                    try {
                                        process = Runtime.getRuntime().exec(new String[]{"su", "-c", command});// the phone must be root,it can exctue the adb command
                                        process.waitFor();
                                        byte[] filebytes = FileHelper.readFile("screen.jpg");
                                        String encodedString = Base64.encodeToString(filebytes, Base64.DEFAULT);
                                        byte[] b = encodedString.getBytes();
                                        out.write(("length:" + b.length + "").getBytes());
                                        out.flush();
                                        out.write(b);
                                        out.flush();
                                    }
                                    catch (Exception e){
										break;
									}
                                }
                            }
                        });
                        thread.start();
					}else if(currCMD.indexOf("tyt")!=-1){
						String cmd = "input swipe 100 100 100 100 "+currCMD.replace("tyt","");
						process = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});// the phone must be root,it can exctue the adb command
						process.waitFor();
					}


				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			out.close();
			in.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if (client != null)
				{
					Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "client.close()");
					client.close();
				}
			} catch (IOException e)
			{
				Log.e(androidService.TAG, Thread.currentThread().getName() + "---->" + "read write error333333");
				e.printStackTrace();
			}
		}
	}

	/* 读取命令 */
	public String readCMDFromSocket(InputStream in)
	{
		int MAX_BUFFER_BYTES = 2048;
		String msg = "";
		byte[] tempbuffer = new byte[MAX_BUFFER_BYTES];
		try
		{
			int numReadedBytes = in.read(tempbuffer, 0, tempbuffer.length);
			msg = new String(tempbuffer, 0, numReadedBytes, "utf-8");
			tempbuffer = null;
		} catch (Exception e)
		{
			Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "readFromSocket error");
			androidService.ioThreadFlag = false;
			e.printStackTrace();
		}
		// Log.v(Service139.TAG, "msg=" + msg);
		return msg;
	}

	public void saveMyBitmap(Bitmap mBitmap)  {
		File f = new File( "/sdcard/screensmall.jpg");
		FileOutputStream fOut = null;
		if(f.exists()){
			f.delete();
		}
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		mBitmap.compress(Bitmap.CompressFormat.JPEG, 60, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//计算图片的缩放值
	public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}
	// 根据路径获得图片并压缩，返回bitmap用于显示
	public static Bitmap getSmallBitmap(String filePath) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, 480, 800);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(filePath, options);
	}
}