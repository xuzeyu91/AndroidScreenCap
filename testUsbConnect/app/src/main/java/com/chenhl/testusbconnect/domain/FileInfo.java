package com.chenhl.testusbconnect.domain;

import java.util.Arrays;

/*************
 * 通过adb
 * ------------->socket-------------->读取文件信息---------->到文件中;;文件最终的还是Bytes字节流;;;
 * 
 * @author Administrator
 * 
 */

public class FileInfo
{

	/****
	 * 从socket中读到的文件信息如下: 1. 文件名;; 2.此文件的长度;;; 3.此文件的字节信息;;;
	 */
	private String fileName;
	private int fileLength;
	private byte[] fileBytes;

	public FileInfo()
	{
		super();
	}

	public FileInfo(String fileName, int fileLength, byte[] fileBytes)
	{
		super();
		this.fileName = fileName;
		this.fileLength = fileLength;
		this.fileBytes = fileBytes;
	}

	@Override
	public String toString()
	{
		return "FileInfo [fileName=" + fileName + ", fileLength=" + fileLength + ", fileBytes="
				+ Arrays.toString(fileBytes) + "]";
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public int getFileLength()
	{
		return fileLength;
	}

	public void setFileLength(int fileLength)
	{
		this.fileLength = fileLength;
	}

	public byte[] getFileBytes()
	{
		return fileBytes;
	}

	public void setFileBytes(byte[] fileBytes)
	{
		this.fileBytes = fileBytes;
	}
}
