package com.example.mobilesafe.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.util.Xml;

public class BackupSms {
	private static final String tag = "BackupSms";
	private static int index = 0;
	
    public static void backup(Context context,String path,CallbackSms callbackp){
		FileOutputStream fos =null;
		Cursor cursor = null;
    	   //短信备份的写入文件
		try {
    	   File file = new File(path);
    	   //获取内容解析者，获取短信数据库中的数据
    	   ContentResolver resolver = context.getContentResolver();
    	   cursor  = resolver.query(Uri.parse("content://sms/"), new String[]{"address","date","type","body"}, null, null, null);
    	   //文件的输出流
    	   fos = new FileOutputStream(file);
    	   //序列化获取到的短信信息到xml中
    	   XmlSerializer newSerializer = Xml.newSerializer();
    	   newSerializer.setOutput(fos, "UTF-8");
    	   newSerializer.startDocument("UTF-8", true);
    	   newSerializer.startTag(null, "smss");
    	   
    	   //设置进度条的最大值
    	   if(callbackp!=null){
    		   callbackp.setMax(cursor.getCount());
    	   }
    	   Log.i(tag, "cursor.getCount():"+cursor.getCount());
    	  
           while(cursor.moveToNext()){
        	  newSerializer.startTag(null,"sms");
        	  
        	  newSerializer.startTag(null,"address");
        	  newSerializer.text(cursor.getString(0));
        	  Log.i(tag, "cursor.getString(0):"+cursor.getString(0));
        	  newSerializer.endTag(null,"address");
        	  
        	  newSerializer.startTag(null,"date");
        	  newSerializer.text(cursor.getString(1));
        	  Log.i(tag, "cursor.getString(1):"+cursor.getString(1));
        	  newSerializer.endTag(null,"date");
        	  
        	  newSerializer.startTag(null,"type");
        	  newSerializer.text(cursor.getString(2));
        	  Log.i(tag, "cursor.getString(2):"+cursor.getString(2));
        	  newSerializer.endTag(null,"type");
        	  
        	  newSerializer.startTag(null,"body");
        	  newSerializer.text(cursor.getString(3));
        	  Log.i(tag, "cursor.getString(3):"+cursor.getString(3));
        	  newSerializer.endTag(null,"body");
        	  
			  newSerializer.endTag(null,"sms");
        	  
        	  index ++;
        	  Thread.sleep(500);
        	  if(callbackp!=null){
        		  callbackp.setProgress(index);
        	  }
           }
    	   newSerializer.endTag(null,"smss");
    	   newSerializer.endDocument();
    	   
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
			if(fos!=null && cursor!=null){
					cursor.close();
					fos.close();
				} 
			}catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    
     
	
	  /*回调编写
	1.定义一个接口
	2,定义接口中未实现的业务逻辑方法
	3.传递一个实现了此接口的类的对象,一定实现了上诉接口未实现方法
	4.获取传递进来的对象,在合适的地方,做方法的调用
	   * */
       
	public interface CallbackSms {
		
		public void setMax(int max);
		public void setProgress(int index);
	}
      
}

