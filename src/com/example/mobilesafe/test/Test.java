package com.example.mobilesafe.test;

import java.util.List;
import java.util.Random;

import com.example.mobilesafe.db.bean.BlackNumberInfo;
import com.example.mobilesafe.db.dao.BlackNumberDao;

import android.test.AndroidTestCase;

//测试BlackNumberDao
public class Test extends AndroidTestCase {
      public void insert(){
    	  BlackNumberDao dao = BlackNumberDao.getInstance(getContext());
    	  for(int i=0;i<100;i++){
    		  if(i<10){
    			  dao.insert("1860000000"+i,new Random().nextInt(3)+1+"");
    		  }else{
    			  dao.insert("186000000"+i,new Random().nextInt(3)+1+"");
    		  }
    	  }
      }
      
      public void delete(){
    	  BlackNumberDao dao = BlackNumberDao.getInstance(getContext());
    	  dao.delete("110");
      }
      
      public void update(){
    	  BlackNumberDao dao = BlackNumberDao.getInstance(getContext());
    	  dao.update("110", "2");
      }
      
      public void findAll(){
    	  BlackNumberDao dao = BlackNumberDao.getInstance(getContext());
    	  List<BlackNumberInfo> list = dao.findAll();
      }
}
