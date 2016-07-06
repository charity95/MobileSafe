package com.example.mobilesafe.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.mobilesafe.R;
import com.example.mobilesafe.R.layout;
import com.example.mobilesafe.R.menu;
import com.example.mobilesafe.utils.ConstantValue;
import com.example.mobilesafe.utils.SpUtil;
import com.example.mobilesafe.utils.StreamUtil;
import com.example.mobilesafe.utils.ToastUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends Activity {
	private String tag = "splash";
    protected static final int UPDATE_VERSION = 100;
	protected static final int ENTER_HOME = 101;
	protected static final int IO_ERROR = 102;
	protected static final int JSON_ERROR = 103;
	protected static final int URL_ERROR = 104;
	
	private TextView mVersionName;
	private int mLocalVersionCode;
	private String mVersionDes;
	private String mDownloadUrl;
	
    private Handler mHandler = new Handler(){
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
			case UPDATE_VERSION:
				//弹出对话框，提示用户更新
				showUpdateDialog();
				break;
            case ENTER_HOME:
				enterHome();
				break;
            case IO_ERROR:
				ToastUtil.show(getApplicationContext(), "IO异常");
				enterHome();
				break;
            case JSON_ERROR:
            	ToastUtil.show(getApplicationContext(), "JSON异常");
            	enterHome();
				break;
            case URL_ERROR:
            	ToastUtil.show(getApplicationContext(), "URL异常");
            	enterHome();
				break;
			}
    	};
    };
	private InputStream stream;
	private FileOutputStream fos;
	
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        initUI();
        initData();
        //初始化数据库
        initDB();
        //生成快捷方式
        if(!SpUtil.getBoolean(this, ConstantValue.SHORTCUT, false)){
        	initShortCut();
        }
    }

	private void initShortCut() {
		Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		//指定生成快捷方式的名称，图标
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "手机安全卫士");
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeResource(getResources(), R.drawable.ic_android_black_48dp));
		//点击快捷方式后跳转的界面
		Intent intent2 = new Intent("android.intent.action.HOME");
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent2);
		
		sendBroadcast(intent);
		SpUtil.putBoolean(this, ConstantValue.SHORTCUT, true);
	}

	private void initDB() {
		//拷贝归属地数据库
		initAddressDB("address.db");
		//拷贝常用号码数据库
		initAddressDB("commonnum.db");
		//拷贝病毒数据库
		initAddressDB("antivirus.db");
	}

	private void initAddressDB(String dbName) {
	    //files是要存储数据库的文件夹.
		File files = getFilesDir();
		//在文件夹下创建名为dbName的数据库
		File file = new File(files,dbName);
		if(file.exists()){
			//数据库存在就不要拷贝了
			return;
		}
		try {
			//从第三方资产下读取数据库文件
			stream = getAssets().open(dbName);
			//将读取的内容写入到指定文件中
			fos = new FileOutputStream(file);
			byte[] bs = new byte[1024];
			int temp = -1;
			while((temp=stream.read(bs))!=-1){
				//一次读入1024个byte
				fos.write(bs, 0, temp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(stream!=null && fos!=null){
				try {
					fos.close();
					stream.close();
				} catch (IOException e){
					e.printStackTrace();
				}
			}
		}
	}

	protected void showUpdateDialog() {
		Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle("版本更新");
		builder.setMessage(mVersionDes);
		
		builder.setPositiveButton("立即更新",new  DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				//下载新版本
				downloadApk();
			}
			
		});
		
		builder.setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				enterHome();
			}
		});
		
		builder.show();
	}

	protected void downloadApk() {
		// 下载地址，下载后放置的路径
		//sd卡是否可用
		String state = Environment.getExternalStorageState();
        Log.d(tag, state);
		if(state.equals(Environment.MEDIA_MOUNTED)){
			//sd卡路径
			Log.d(tag, "sd可用");
			String path = Environment.getExternalStorageDirectory().getAbsolutePath()
					+File.separator+"MobileSafe.apk";
			Log.d(tag, path);
			//发送请求获取Apk,并且放置到指定位置
			HttpUtils httpUtils = new HttpUtils();
			//发送请求，传递参数
			httpUtils.download(mDownloadUrl, path, new RequestCallBack<File>() {
				@Override
				public void onSuccess(ResponseInfo<File> responseInfo) {
					// 下载成功,获取下载后放置在sd卡中的apk
					Log.d(tag, "下载成功");
					File file = responseInfo.result;
					installApk(file);
				}
				@Override
				public void onFailure(HttpException arg0, String arg1) {
					// 下载失败
					Log.d(tag, "下载失败");
				}
				
				//正在下载
				@Override
				public void onStart() {
					Log.d(tag, "开始下载");
                    super.onStart();
				}
				
				//下载过程中
				@Override
				public void onLoading(long total, long current,
						boolean isUploading) {
					Log.d(tag, "正在下载");
					Log.d(tag, "总下载"+total);
					Log.d(tag, "已经下载"+current);
					super.onLoading(total, current, isUploading);
				}
			});
			
		}else{
			Log.d(tag, "sd不可用");
		}
		
	}

	protected void installApk(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		enterHome();
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void initData() {
		mVersionName.setText("版本名称:"+getVersionName());
		
		//获取本地版本号
		mLocalVersionCode = getVersionCode();
		
		boolean update = SpUtil.getBoolean(getApplicationContext(), ConstantValue.OPEN_UPDATE, false);
		if(update){
			//获取服务器端版本号（发送请求）
			checkVersion();
		}else{
			mHandler.sendEmptyMessageDelayed(ENTER_HOME, 4000);
		}
		
	}

	

	/**
	 * 获取清单文件版本号
	 * @return 返回非0则获取成功
	 */
	private int getVersionCode() {
		PackageManager pm = getPackageManager();
		try {
			 PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
			 return info.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 检测版本号
	 */
	private void checkVersion() {
		new Thread(){
			public void run() {
				//发送请求获取数据,参数则为请求json的链接地址
				//http://192.168.13.99:8080/update74.json	测试阶段不是最优
				//仅限于模拟器访问电脑tomcat
				Message msg = Message.obtain();
				long startTime = System.currentTimeMillis();
				try {
					//1,封装url地址
					URL url = new URL("http://10.0.2.2:8080/update74.json");
					//2,开启一个链接
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					//3,设置常见请求参数(请求头)
					
					//请求超时
					connection.setConnectTimeout(2000);
					//读取超时
					connection.setReadTimeout(2000);
					
					//默认就是get请求方式,
//					connection.setRequestMethod("POST");
					
					//4,获取请求成功响应码
					if(connection.getResponseCode() == 200){
						//5,以流的形式,将数据获取下来
						InputStream is = connection.getInputStream();
						//6,将流转换成字符串(工具类封装)
						String json = StreamUtil.streamToString(is);
						
						//7,json解析
						JSONObject jsonObject = new JSONObject(json);
						
						//debug调试,解决问题
						String versionName = jsonObject.getString("versionName");
						mVersionDes = jsonObject.getString("versionDes");
						String versionCode = jsonObject.getString("versionCode");
						mDownloadUrl = jsonObject.getString("downloadUrl");

						//日志打印	
						Log.i(tag, versionName);
						Log.i(tag, mVersionDes);
						Log.i(tag, versionCode);
						Log.i(tag, mDownloadUrl);
						
						//8,比对版本号(服务器版本号>本地版本号,提示用户更新)
						if(mLocalVersionCode<Integer.parseInt(versionCode)){
							//提示用户更新,弹出对话框(UI),消息机制
							msg.what = UPDATE_VERSION;
						}else{
							//进入应用程序主界面
							msg.what = ENTER_HOME;
						}
					}
				}catch(MalformedURLException e) {
					e.printStackTrace();
					msg.what = URL_ERROR;
				}catch (IOException e) {
					e.printStackTrace();
					msg.what = IO_ERROR;
				} catch (JSONException e) {
					e.printStackTrace();
					msg.what = JSON_ERROR;
				}finally{
					//指定睡眠时间,请求网络的时长超过3秒则不做处理
					//请求网络的时长小于3秒,强制让其睡眠满3秒钟
					long endTime = System.currentTimeMillis();
					if(endTime-startTime<3000){
						try {
							Thread.sleep(3000-(endTime-startTime));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					mHandler.sendMessage(msg);
				}
			};
		}.start();
	}

	/**
	 * 进入主界面
	 */
	protected void enterHome() {
		Intent intent = new Intent(this,HomeActivity.class);
		startActivity(intent);
		finish();
		
	}

	/**
	 * 获取清单文件中的版本名称
	 * @return 返回null获取失败
	 */
	private String getVersionName() {
		PackageManager pm = getPackageManager();
		try {
			 PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
			 return info.versionName;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}

	private void initUI() {
		mVersionName = (TextView) findViewById(R.id.tv_versionName);
		
	}

}
