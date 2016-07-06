##day12##
	- 看门狗
		
		- 看门狗原理介绍
		- 创建服务WatchDogService
		- 设置页面增加启动服务的开关
		- 看门狗轮询检测任务栈

				打印当前最顶上的activity

				/**
				 * 看门狗服务 需要权限: android.permission.GET_TASKS
				 * 
				 * @author Kevin
				 * 
				 */
				public class WathDogService extends Service {
				
					private boolean isRunning;// 表示线程是否正在运行
					private ActivityManager mAM;
				
					@Override
					public IBinder onBind(Intent intent) {
						return null;
					}
				
					@Override
					public void onCreate() {
						super.onCreate();
						mAM = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
				
						isRunning = true;
						new Thread() {
							public void run() {
								while (isRunning) {// 看门狗每隔100毫秒巡逻一次
									List<RunningTaskInfo> runningTasks = mAM.getRunningTasks(1);// 获取正在运行的任务栈
									String packageName = runningTasks.get(0).topActivity
											.getPackageName();// 获取任务栈最上层activity的包名
									System.out.println("top Activity=" + packageName);
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							};
						}.start();
					}
				
					@Override
					public void onDestroy() {
						super.onDestroy();
						isRunning = false;// 结束线程
					}
				}

	- 轮询获取最近的task, 如果发现是加锁的,跳EnterPwdActivity

			if (mDao.find(packageName)) {// 查看当前页面是否在加锁的数据库中
				Intent intent = new Intent(WatchDogService.this,
						EnterPwdActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("packageName", packageName);
				startActivity(intent);
			}

			-----------------------------------

			/**
			 * 加锁输入密码页面
			 * 
			 * @author Kevin
			 * 
			 */
			public class EnterPwdActivity extends Activity {
			
				private TextView tvName;
				private ImageView ivIcon;
				private EditText etPwd;
				private Button btnOK;
			
				@Override
				protected void onCreate(Bundle savedInstanceState) {
					super.onCreate(savedInstanceState);
					setContentView(R.layout.activity_enter_pwd);
			
					tvName = (TextView) findViewById(R.id.tv_name);
					ivIcon = (ImageView) findViewById(R.id.iv_icon);
					etPwd = (EditText) findViewById(R.id.et_pwd);
					btnOK = (Button) findViewById(R.id.btn_ok);
			
					Intent intent = getIntent();
					String packageName = intent.getStringExtra("packageName");
			
					PackageManager pm = getPackageManager();
					try {
						ApplicationInfo info = pm.getApplicationInfo(packageName, 0);// 根据包名获取应用信息
						Drawable icon = info.loadIcon(pm);// 加载应用图标
						ivIcon.setImageDrawable(icon);
						String name = info.loadLabel(pm).toString();// 加载应用名称
						tvName.setText(name);
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
			
					btnOK.setOnClickListener(new OnClickListener() {
			
						@Override
						public void onClick(View v) {
							String pwd = etPwd.getText().toString().trim();
							if (!TextUtils.isEmpty(pwd)) {// 密码校验
								if (pwd.equals("123")) {
									finish();
								} else {
									Toast.makeText(EnterPwdActivity.this, "密码错误",
											Toast.LENGTH_LONG).show();
								}
							} else {
								Toast.makeText(EnterPwdActivity.this, "请输入密码",
										Toast.LENGTH_LONG).show();
							}
						}
					});
				}
			
			}

	
	- 重写返回事件,跳转到主页面

			//查看系统Launcher源码,确定跳转逻辑
			@Override
			public void onBackPressed() {
				// 跳转主页面
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				startActivity(intent);

				finish();//销毁当前页面
			}

	- 发送广播,看门狗跳过检测

			认证成功后,发送广播

			EnterPwdActivity.java

			// 发送广播,通知看门狗不要再拦截当前应用
			Intent intent = new Intent();
			intent.setAction("com.itheima.mobilesafeteach.ACTION_STOP_PROTECT");
			intent.putExtra("packageName", packageName);
			sendBroadcast(intent);

			-------------------------------------------

			WatchDogService.java
			
			class InnerReceiver extends BroadcastReceiver {

				@Override
				public void onReceive(Context context, Intent intent) {
					// 看门狗得到了消息，临时的停止对某个应用程序的保护
					mSkipPackageName = intent.getStringExtra("packageName");
				}
			}

			if (packageName.equals(mSkipPackageName)) {// 用过已经认证通过了,需跳过验证
					System.out.println("无需验证...");
					continue;
			}

	- 相关优化

			知识拓展：看门狗后台一直在运行，这样是比较耗电的。
			
			我们要优化的的话怎么做呢？
			在看门狗服务里，监听锁屏事件，如果锁屏了我就把看门狗停止（flag = false;）；屏幕开启了，我就让看门狗开始工作启动服务并且flag = true;；
			
			避免一次输入密码了不再输入；防止别人在我使用的时候，接着使用不用输入密码的情形；
			也可以在锁屏的时候把mSkipPackageName赋值为空就行了。

	- 利用activity启动模式修复密码输入bug

			1. 演示bug(进入手机卫士,按home退到后台,然后再打开加锁app,进入后发现跳转到手机卫士页面)
			2. 画图分析，正常情况下的任务栈和bug时的任务栈图；
			3. 解决问题；在功能清单文件EnterPwdActivity加上字段
			<activity android:name="com.itheima.mobilesafe.EnterPwdActivity" android:launchMode="singleInstance"/>
			4. 然后再画图分析正确的任务栈；

	- 隐藏最近打开的activity

			长按小房子键：弹出历史记录页面，就会列出最近打开的Activity;

			1. 演示由于最近打开的Activity导致的Bug;
			
			2. 容易暴露用户的隐私
			  最近打开的Activity，是为了用户可以很快打开最近打开的应用而设计的；2.2、2.3普及后就把问题暴露出来了，很容易暴露用户的隐私。比如你玩一些日本开发的游戏：吹裙子、扒衣服这类游戏。你正在玩这些有些，这个时候，爸妈或者大学女辅导员过来了，赶紧按小房子，打开背单词的应用，这时大学女辅导员走过来说，干嘛呢，把手机交出来，长按一下小房子键，这个时候很尴尬的事情就产生了。
			
				A：低版本是无法移除的。低版本记录最近8个；想要隐藏隐私，打开多个挤出去；
				B:4.0以后高版本就可以直接移除了。考虑用户呼声比较高。
			
			3. 设置不在最近任务列表显示activity
				<activity
				        android:excludeFromRecents="true"
				            android:name="com.itheima.mobilesafe.EnterPwdActivity"
				            android:launchMode="singleInstance" />
			
			4. 在装有腾讯管家的模拟器演示腾讯管理的程序锁功能；也没用现实最近的Activity,它也是这样做的。
			
			知识拓展，以后开发带有隐私的软件，或者软件名称不好听的应用，就可以加载在最近打开列表不包括字段.

	- 腾讯管家和手机卫士同时加锁,谁更快?

			腾讯管家会更快一些, 所以需要再进一步优化

	- 提高性能

		- 缩短每次巡逻时间

				//将100改为20
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
						e.printStackTrace();
				}
		- 不频繁调用数据库

				从数据库中读取所有已加锁应用列表,每次从集合中查询判断

				mLockedPackages = mDao.getInstance(this).findAll();// 查询所有已加锁的应用列表

				// if (mDao.find(packageName)) {// 查看当前页面是否在加锁的数据库中
				if (mLockedPackages.contains(packageName)) {}

		- 重新和腾讯管家比拼速度
		- 监听数据库变化, 更新集合

			- 增加另外一款软件进入程序锁。打开看看，是无法打开输入密码页面的；解析原因；
				这个时候就需要根据数据库的数据变化而改变集合的信息了，就用到了观察者；

			- 联想监听来电拦截时,监听通话日志变化的逻辑,解释原理

			- 具体实现

					AppLockDao.java

					// 数据库改变后发送通知
					mContext.getContentResolver().notifyChange(
							Uri.parse("content://com.itheima.mobilesafe/applockdb"), null);					

					-------------------------------------

					WatchDogService.java	

					// 监听程序锁数据库内容变化
					mObserver = new MyContentObserver(new Handler());
					getContentResolver().registerContentObserver(
							Uri.parse("content://com.itheima.mobilesafe/applockdb"), true,
							mObserver);		

					getContentResolver().unregisterContentObserver(mObserver);// 注销观察者		

					class MyContentObserver extends ContentObserver {
	
						public MyContentObserver(Handler handler) {
							super(handler);
						}
				
						@Override
						public void onChange(boolean selfChange) {
							System.out.println("数据变化了...");
							mLockedPackages = mDao.findAll();// 查询所有已加锁的应用列表
						}
				
					}
- 手机杀毒
	- 什么是病毒?

			计算机病毒是一个程序，一段可执行码。就像生物病毒一样，具有自我繁殖、互相传染以及激活再生等生物病毒特征。计算机病毒有独特的复制能力，它们能够快速蔓延，又常常难以根除。它们能把自身附着在各种类型的文件上，当文件被复制或从一个用户传送到另一个用户时，它们就随同文件一起蔓延开来。

	- 计算机第一个病毒

			诞生于麻省理工大学
			
	- 蠕虫病毒
		
			熊猫烧香，蠕虫病毒的一种，感染电脑上的很多文件；exe文件被感染，html文件被感染。
		 	主要目的：证明技术有多牛。写这种病毒的人越来越少了

	- 木马

			盗窃信息，盗号、窃取隐私、偷钱，玩了一个游戏，买了很多装备，监听你的键盘输入，下次进入的话，装备全部没了。
			主要目目的：挣钱，产生利益；

	- 灰鸽子

			主要特征，控制别人电脑，为我所有。比如挖金矿游戏挣钱的，控制几十万台机器为你干活。
			总会比银河处理器快的多。
			特点是：不知情情况下安装下的。

	- 所有的病毒，都是执行后才有危害，如果病毒下载了，没有安装运行，是没有危害的。	
	- 杀毒原理介绍

			定位出特殊的程序，把程序的文件给删除。
	
			王江民, 江民杀毒软件
			Kv300
			Kv300 干掉300个病毒
			
			开发kv300后很多人用盗版的。
			江民炸弹

	- 病毒怎么找到？-收集病毒的样本
		
			电信 网络运营商主节点 部署服务器集群（蜜罐）
			一组没有防火墙 没有安全软件 没有补丁的服务器, 主动联网,下载一些软件运行。这样情况下，特别容易中病毒。
			工作原理相当于：苍蝇纸

	- 360互联网云安全计划

			所有的用户都是你的蜜罐；
			收集的数据量就大大提高了；

			国内安全厂商，有些没有职业道德。
			收集一些个人隐私，或者商业机密的文件也收集过去 3Q大战

	- 传统杀毒软件的缺陷

			目前卡巴斯基病毒库已经有了2千多万病毒

			传统杀毒软件的缺陷： 病毒数据库越来越大；
			只能查杀已知的病毒，不能查杀未知病毒；

			360免杀
			写了一个木马，在加一个壳，加壳后360就识别不了了

	- 主动防御

			检查软件
			1.检查开机启动项
			2.检查注册表；
			3.检查进程列表

			病毒特征：
			1、开启启动
			2、隐藏自身
			3、监视键盘
			4、联网发邮件

			启发式扫描-扫描单个文件
			拷贝文件到虚拟机-相当于精简版的系统, 运行后检测是否具备病毒特点

	- 杀毒引擎

			优化后的数据库查询算法,优先扫描当下最常见的病毒, 速度快

	- Android上的杀毒软件

			大多数停留在基于数据库方式杀毒

			LBE主动防御方式杀毒。敏感权限扫描,敏感操作提示,和小米深度合作

			金山手机卫士病毒库

- 手机杀毒模块开发

	- 创建AntiVirusActivity
	- 布局文件开发

			<LinearLayout
	        android:layout_width="match_parent"
	        android:layout_height="wrap_content"
	        android:orientation="horizontal"
	        android:padding="10dp" >
	
	        <FrameLayout
	            android:layout_width="80dp"
	            android:layout_height="80dp" >
	
	            <ImageView
	                android:id="@+id/imageView1"
	                android:layout_width="match_parent"
	                android:layout_height="match_parent"
	                android:src="@drawable/ic_scanner_malware" />
	
	            <ImageView
	                android:id="@+id/iv_scanning"
	                android:layout_width="match_parent"
	                android:layout_height="match_parent"
	                android:src="@drawable/act_scanning_03" />
	        </FrameLayout>
	
	        <LinearLayout
	            android:layout_width="match_parent"
	            android:layout_height="match_parent"
	            android:gravity="center"
	            android:orientation="vertical" >
	
	            <TextView
	                android:id="@+id/tv_scan_status"
	                android:layout_width="wrap_content"
	                android:layout_height="wrap_content"
	                android:singleLine="true"
	                android:text="正在初始化8核杀毒引擎"
	                android:textColor="#000"
	                android:textSize="18sp" />
	
	            <ProgressBar
	                android:id="@+id/progressBar1"
	                style="?android:attr/progressBarStyleHorizontal"
	                android:layout_width="match_parent"
	                android:layout_height="wrap_content"
	                android:layout_marginLeft="10dp"
	                android:layout_marginRight="10dp" />
	        </LinearLayout>
	    </LinearLayout>

	- 扫描动画

			RotateAnimation anim = new RotateAnimation(0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
			anim.setDuration(1000);//间隔时间
			anim.setRepeatCount(Animation.INFINITE);//无限循环
			anim.setInterpolator(new LinearInterpolator());//匀速循环,不停顿
			ivScanning.startAnimation(anim);

	- 自定义进度条样式

			1. 查看android系统对Progressbar样式的定义

			开发环境\platforms\android-16\data\res\values\styles.xml,搜索Widget.Holo.ProgressBar.Horizontal->progress_horizontal_holo_light

			2. 拷贝xml文件,修改成自己的图片

			<layer-list xmlns:android="http://schemas.android.com/apk/res/android" >
			    <item
			        android:id="@android:id/background"
			        android:drawable="@drawable/security_progress_bg"/>
			    <item
			        android:id="@android:id/secondaryProgress"
			        android:drawable="@drawable/security_progress">
			    </item>
			    <item
			        android:id="@android:id/progress"
			        android:drawable="@drawable/security_progress">
			    </item>
			</layer-list>

			3. 将xml文件设置给Progressbar

			<ProgressBar
                android:id="@+id/progressBar1"
                style="?android:attr/progressBarStyleHorizontal"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginLeft="10dp"
                android:layout_marginRight="10dp"
                android:progress="50"
                android:layout_marginTop="5dp"
                android:progressDrawable="@drawable/custom_progress" />
			
			4. 进度更新

				// 更新进度条
				new Thread() {
					public void run() {
						pbProgress.setMax(100);
		
						for (int i = 0; i <= 100; i++) {
							pbProgress.setProgress(i);
		
							try {
								Thread.sleep(30);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					};
				}.start();
			

- 获取系统安装包的MD5

		PackageManager pm = getPackageManager();
		// 获取所有已安装/未安装的包的安装包信息
		// GET_UNINSTALLED_PACKAGES代表已删除，但还有安装目录的
		List<PackageInfo> packages = pm
					.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

		for (PackageInfo packageInfo : packages) {
			//apk安装路径
			String apkPath = packageInfo.applicationInfo.sourceDir;
			//计算apk的md5
			String md5 = MD5Utils.getFileMd5(apkPath);
		}

		-------------------------------------------------

		/**
		 * 获取某个文件的md5
		 * @param path
		 * @return
		 */
		public static String getFileMd5(String path) {
			try {
				MessageDigest digest = MessageDigest.getInstance("MD5");
				FileInputStream in = new FileInputStream(path);
	
				int len = 0;
				byte[] buffer = new byte[1024];
	
				while ((len = in.read(buffer)) != -1) {
					digest.update(buffer, 0, len);
				}
	
				byte[] result = digest.digest();
	
				StringBuffer sb = new StringBuffer();
				for (byte b : result) {
					int i = b & 0xff;// 将字节转为整数
					String hexString = Integer.toHexString(i);// 将整数转为16进制
	
					if (hexString.length() == 1) {
						hexString = "0" + hexString;// 如果长度等于1, 加0补位
					}
	
					sb.append(hexString);
				}
	
				System.out.println(sb.toString());// 打印得到的md5
				return sb.toString();
	
			} catch (Exception e) {
				e.printStackTrace();
			}
	
			return "";
		}


- 扫描病毒数据库

		AntiVirusDao.java

		/**
		 * 病毒数据库的封装
		 * 
		 * @author Kevin
		 * 
		 */
		public class AntiVirusDao {
		
			public static final String PATH = "data/data/com.itheima.mobilesafeteach/files/antivirus.db";
		
				/**
				 * 根据签名的md5判断是否是病毒
				 * 
				 * @param md5
				 * @return 返回病毒描述,如果不是病毒,返回null
				 */
				public static String isVirus(String md5) {
					SQLiteDatabase db = SQLiteDatabase.openDatabase(PATH, null,
							SQLiteDatabase.OPEN_READONLY);
			
					Cursor cursor = db.rawQuery("select desc from datable where md5=? ",
							new String[] { md5 });
			
					String desc = null;
					if (cursor.moveToFirst()) {
						desc = cursor.getString(0);
					}
			
					cursor.close();
					db.close();
					return desc;
				}
		}

- 扫描安装包并更新进度条

		int progress = 0;
		Random random = new Random();
		for (PackageInfo packageInfo : packages) {
			String name = packageInfo.applicationInfo.loadLabel(pm)
					.toString();

			String apkPath = packageInfo.applicationInfo.sourceDir;
			String md5 = MD5Utils.getFileMd5(apkPath);
			String desc = AntiVirusDao.isVirus(md5);

			if (desc != null) {
				// 是病毒
				System.out.println("是病毒....");
			} else {
				// 不是病毒
				System.out.println("不是病毒....");
			}

			progress++;
			pbProgress.setProgress(progress);

			try {
				Thread.sleep(50 + random.nextInt(50));//随机休眠一段时间
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

- 扫描过程中,更新扫描状态文字

		- 扫描前,强制休眠2秒,展示"正在初始化8核杀毒引擎"
		- 使用handler发送消息,更新TextView为:正在扫描:应用名称
		- 扫描结束后, 发送消息,更新TextView为:扫描完毕
		- 扫描结束后,关闭扫描的动画

- 扫描过程中,更新扫描文件列表

		- 布局文件中添加空的LinearLayout(竖直方向),动态给线性布局添加TextView
		- 使用ScrollView包裹线性布局,保证可以上下滑动

			 <ScrollView
		        android:layout_width="match_parent"
		        android:layout_height="match_parent" >
		
		        <LinearLayout
		            android:id="@+id/ll_scanning"
		            android:layout_width="match_parent"
		            android:layout_height="wrap_content"
		            android:orientation="vertical" >
		        </LinearLayout>
		    </ScrollView>

		- 如果发现是病毒, TextView需要展示为红色, 为了区分是不是病毒,可以把扫描的文件封装成一个对象ScanInfo

			class ScanInfo {
				public String packageName;
				public String desc;
				public String name;
				public boolean isVirus;
			}

			private Handler mHandler = new Handler() {
				public void handleMessage(android.os.Message msg) {
					switch (msg.what) {
					case SCANNING:
						ScanInfo info = (ScanInfo) msg.obj;
						tvScanStatus.setText("正在扫描:" + info.name);
		
						TextView tvScan = new TextView(AntiVirusActivity.this);
						if (info.isVirus) {
							tvScan.setText("发现病毒:" + info.name);
							tvScan.setTextColor(Color.RED);
						} else {
							tvScan.setText("扫描安全:" + info.name);
						}
		
						llScanning.addView(tvScan);
						break;
					case SCANNING_FINISHED:
						tvScanStatus.setText("扫描完毕");
						ivScanning.clearAnimation();// 清除扫描的动画
						break;
					default:
						break;
					}
				};
			};

- 制作病毒

		制作两个apk文件,并将这两个apk文件的md5加入到病毒数据库中,这样的话就可以测试扫出病毒的情况了

		注意: 将原来的antivirus.db替换为新的文件后,一定要把app的数据清除后再运行,重新进行拷贝数据库的操作, 否则app仍找的是data/data目录下的旧版数据库!

- 创建病毒集合		
- 发现病毒后，提示用户删除病毒

		if (mVirusList.isEmpty()) {
			Toast.makeText(getApplicationContext(), "你的手机很安全了，继续加油哦!",
					Toast.LENGTH_SHORT).show();
		} else {
			showAlertDialog();
		}

		----------------------------

		/**
		 * 发现病毒后,弹出警告弹窗
		 */
		protected void showAlertDialog() {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("警告!");
			builder.setMessage("发现" + mVirusList.size() + "个病毒, 非常危险,赶紧清理!");
			builder.setPositiveButton("立即清理",
					new DialogInterface.OnClickListener() {
	
						@Override
						public void onClick(DialogInterface dialog, int which) {
							for (ScanInfo info : mVirusList) {
								// 卸载apk
								Intent intent = new Intent(Intent.ACTION_DELETE);
								intent.setData(Uri.parse("package:"
										+ info.packageName));
								startActivity(intent);
							}
						}
					});
	
			builder.setNegativeButton("下次再说", null);
			AlertDialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(false);// 点击弹窗外面,弹窗不消失
			dialog.show();
		}

- 处理横竖屏切换

		fn+ctrl+f11 切换模拟器横竖屏后, Activity的onCreate方法会从新走一次, 可以通过清单文件配置,Activity强制显示竖屏

		<activity
            android:name=".activity.AntiVirusActivity"
            android:screenOrientation="portrait" />

		或者, 可以显示横屏, 通过此配置可以不重新创建Activity

		<activity
            android:name=".activity.AntiVirusActivity"
            android:configChanges="orientation|screenSize|keyboardHidden" />