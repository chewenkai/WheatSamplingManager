package com.aj.tools;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.util.EncodingUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

/**
 * File system.
 * @author Administrator
 *
 */
public class FileStream
{
	final String HISTORY="hisroty";
	private String res="";
	private String TASK_FILE,HISTORY_TASK_FILE;
	private String TEMPLET_FILE,HISTORY_TEMPLET_FILE;
	private File taskFile,historyTaskFile, templetFile,historyTempletFile;
	private Context mContext;

    public String getPath2() {
        String sdcard_path = null;
        String sd_default = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        L.d("text", sd_default);
        if (sd_default.endsWith("/")) {
            sd_default = sd_default.substring(0, sd_default.length() - 1);
        }
        // 得到路径
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                if (line.contains("secure"))
                    continue;
                if (line.contains("asec"))
                    continue;
                if (line.contains("fat") && line.contains("/mnt/")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        if (sd_default.trim().equals(columns[1].trim())) {
                            continue;
                        }
                        sdcard_path = columns[1];
                    }
                } else if (line.contains("fuse") && line.contains("/mnt/")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        if (sd_default.trim().equals(columns[1].trim())) {
                            continue;
                        }
                        sdcard_path = columns[1];
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        L.d("text", sdcard_path);
        return sdcard_path;
    }
	public FileStream(Context context)
	{
		mContext=context;
		TASK_FILE = "SampleCollection"+File.separator+
				SPUtils.get(mContext,SPUtils.LOGIN_NAME,"",SPUtils.LOGIN_VALIDATE)+File.separator+"Task";
		TEMPLET_FILE = "SampleCollection"+File.separator+
				SPUtils.get(mContext,SPUtils.LOGIN_NAME,"",SPUtils.LOGIN_VALIDATE)+File.separator+"Templet";

		HISTORY_TASK_FILE= "SampleCollection"+File.separator+
				SPUtils.get(mContext,SPUtils.LOGIN_NAME,"",SPUtils.LOGIN_VALIDATE)+File.separator+HISTORY+File.separator+"Task";
		HISTORY_TEMPLET_FILE="SampleCollection"+File.separator+
				SPUtils.get(mContext,SPUtils.LOGIN_NAME,"",SPUtils.LOGIN_VALIDATE)+File.separator+HISTORY+File.separator+"Templet";

		if(!createRootFile())
		{
			L.e("Create root file failed !");
		}
	}
	public File getTaskFile()
	{
		return taskFile;
	}
	public File getTempletFile()
	{
		return templetFile;
	}
	public File getHistoryTaskFile(){
		return historyTaskFile;
	}
	public File getHistoryTempletFile(){
		return historyTempletFile;
	}

	/**
	 * Get file list and return List<String> for arrayAdapter.
	 * @param file
	 * @return
	 */
	public List<Map<String,Object>> getFileSet(File file,int leftImgId)
	{
		List<Map<String,Object>> fileSet = new ArrayList<Map<String,Object>>();
		if(!file.isDirectory())
		{
			return null;
		}
		else
		{
			String[] fileList = file.list();
			for(int i =0; i<fileList.length; i++)
			{
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("title_img", leftImgId);
				map.put("title", fileList[i]);
				map.put("img", com.aj.collection.R.drawable.right_point);
				
				fileSet.add(map);
			}
		}
		return fileSet;
	}

	/**
	 * Get file list and return List<String> for do task adapter.
	 * @param file
	 * @return
	 */
	public List<Map<String,Object>> getDoTask(File file, Context context)
	{
		boolean auth = false;
		List<Map<String,Object>> fileSet = new ArrayList<Map<String,Object>>();
		if(!file.isDirectory())
		{
			return null;
		}
		else
		{
			String[] fileList = file.list(new MyFileFilter());
			for(int i =0; i<fileList.length; i++)
			{
				
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("title_img", com.aj.collection.R.drawable.form);
				map.put("title", fileList[i].substring(0,fileList[i].indexOf(".spms")));

				String name = file.getPath()+File.separator+fileList[i];    //文件的完整路径
				String jsonStr = readFileSdcard(name);
				String address = Util.getAddressFromJson(jsonStr);
				map.put("describe", address);
				auth = (Boolean) SPUtils.get(context, fileList[i], false, SPUtils.WHICHTASK);
				System.out.println(fileList[i]);
				System.out.println(auth);
				if(auth)
				{
					map.put("img", com.aj.collection.R.drawable.done);
				}
				else
				{
					map.put("img", null);
				}
				fileSet.add(map);
			}
		}
		return fileSet;
	}

	/**
	 * Get file list and return List<String> for see task adapter.
	 * @param file
	 * @return
	 */
	public List<Map<String,Object>> getSeeTask(File file, Context context)
	{
		List<Map<String,Object>> fileSet = new ArrayList<Map<String,Object>>();
		if(!file.isDirectory())
		{
			return null;
		}
		else
		{
			String[] fileList = file.list(new MyFileFilter());
			for(int i =0; i<fileList.length; i++)
			{
				
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("title_img", com.aj.collection.R.drawable.form);
				try {
					map.put("title", fileList[i].substring(0,fileList[i].indexOf("kevin")));
				} catch (Exception e) {
					map.put("title", fileList[i]);
				}
				map.put("img", null);
				
				String name = file.getPath()+File.separator+fileList[i];    //文件的完整路径
                String jsonStr = readFileSdcard(name);
                String address = Util.getAddressFromJson(jsonStr);
                map.put("describe", address);
				fileSet.add(map);
			}
		}
		return fileSet;
	}

	class MyFileFilter implements FilenameFilter
	{

		@Override
		public boolean accept(File dir, String filename)
		{
			return filename.endsWith(".spms");
		}
	}
	/**
	 * Create root file.
	 * @return
	 */
	private boolean createRootFile()
	{
//		Create task file	root/SampleCollection/Task
		if (!android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED))	//判断SD卡是否存在
		{
			L.e("SD card is null");
			return false;
		}
		taskFile=new File(Environment.getExternalStorageDirectory(), TASK_FILE);
//		taskFile=new File(getPath2(), TASK_FILE);
		if(!taskFile.exists())
		{
			if(!taskFile.mkdirs())
			{
				L.e("Failed to create directory taskFile, " +
						"check if you have the WRITE_EXTERNAL_STORAGE permission");
	            return false;
			}
		}
		// Create templet file root/SampleCollection/Templet
	    templetFile = new File(Environment.getExternalStorageDirectory() , TEMPLET_FILE);
	    if(!templetFile.exists())
	    {
	       if(!templetFile.mkdirs())
	       {
	    	  L.e("failed to create directory templetFile, " +
					"check if you have the WRITE_EXTERNAL_STORAGE permission");
	          return false;
	        }
	    }

		historyTaskFile=new File(Environment.getExternalStorageDirectory(), HISTORY_TASK_FILE);
//		taskFile=new File(getPath2(), TASK_FILE);
		if(!historyTaskFile.exists())
		{
			if(!historyTaskFile.mkdirs())
			{
				L.e("Failed to create directory taskFile, " +
						"check if you have the WRITE_EXTERNAL_STORAGE permission");
				return false;
			}
		}
		// Create templet file root/SampleCollection/Templet
		historyTempletFile = new File(Environment.getExternalStorageDirectory() , HISTORY_TEMPLET_FILE);
		if(!historyTempletFile.exists())
		{
			if(!historyTempletFile.mkdirs())
			{
				L.e("failed to create directory templetFile, " +
						"check if you have the WRITE_EXTERNAL_STORAGE permission");
				return false;
			}
		}

	    return true;
	}
	/**
	 * 生成文件夹
	 * @param path 文件夹路径
	 * @return
	 */
	public boolean createFolder(String path)
	{
		File taskStorageDir=null;
		taskStorageDir=new File(path);
		if(!taskStorageDir.exists())
		{
			if(!taskStorageDir.mkdirs())
			{
				L.e("failed to create directory, " +
						"check if you have the WRITE_EXTERNAL_STORAGE permission");
	            return false;
			}
		}
		return true;
	}
	/**
	 * 读取字符串
	 * @param fileName 路径
	 * @return
	 */
	public String readFileSdcard(String fileName) {
		try {
			FileInputStream fin = new FileInputStream(fileName);
			int length = fin.available();
			byte[] buffer = new byte[length];
			fin.read(buffer);
			res = EncodingUtils.getString(buffer, "GBK");
			fin.close();
//			System.out.println("res--->"+res);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
	/**
	 * 创建图片文件
	 * @param bitmap 图片
	 * @param path	图片路径
	 * @param label 图片名称
	 * @return
	 */
	public boolean createImageFile(Bitmap bitmap, String path, String label)
	{
		ByteArrayOutputStream baos = null;  
        try 
        {  
            baos = new ByteArrayOutputStream();  
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);  
            byte[] photoBytes = baos.toByteArray();  
            if (photoBytes != null) 
            {  
                new FileOutputStream(new File(path + File.separator + label)).write(photoBytes);  
            }
        } 
        catch (IOException e) 
        {  
            e.printStackTrace();  
        } 
        finally 
        {  
            try 
            {  
                if (baos != null)  
                    baos.close();  
            } 
            catch (IOException e) 
            {  
                e.printStackTrace();  
            }  
        }  
		return true;
	}
	/**
	 * 创建spms文件/创建文件前一定要先创建文件夹
	 * @param jsonStr	文件内容，非空
	 * @param rootPath	文件名
	 * @return	创建是否成功
	 * @throws IOException
	 */
	public boolean createSpmsFile(String jsonStr, String rootPath, String fileName) throws IOException
	{
		File task = new File(rootPath);	//先创建文件夹
		if(!task.exists())
		{
			if(!task.mkdirs())
			{
				return false;
			}
		}
		File file = new File(rootPath+
				File.separator+fileName+".spms");	//初始化txt文件
		if(!file.exists())	//判断文件是否存在
		{
			L.d("file path is :"+file.getPath());
			if(!file.createNewFile())	//文件不存在则创建
			{
				return false;	//创建失败
			}
		}
		FileOutputStream fos = new FileOutputStream(file);	//初始化文件字节流
		fos.write(jsonStr.getBytes("GBK"));	//写入字符
		fos.flush();		//输出字符文件
		fos.close();
		return true;
	}
}
