package com.guanjiangjun2.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.example.guanjiangjun2.R;
import com.guanjiangjun2.socket.SocketInputThread;
import com.guanjiangjun2.socket.SocketOutputThread;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

public class CrashHandler implements UncaughtExceptionHandler {

    private static CrashHandler instance;  //�������ã������������ɵ����ģ���Ϊ����һ��Ӧ�ó�������ֻ��Ҫһ��UncaughtExceptionHandlerʵ��
    private Logger logger ;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private CrashHandler(){}
    private MyApplication app;
    private Context context;
    public static final String TAG = "CrashHandler"; 
    
    //�����洢�豸��Ϣ���쳣��Ϣ  
    private Map<String, String> infos = new HashMap<String, String>();
    
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    
    public synchronized static CrashHandler getInstance(){  //ͬ�����������ⵥ�����̻߳����³����쳣
        if (instance == null){
            instance = new CrashHandler();
        }
        return instance;
    }
    
    public void init(Context ctx){//��ʼ�����ѵ�ǰ�������ó�UncaughtExceptionHandler������
    	mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        logger = Logger.getLogger(ctx.getClass());
        this.context=ctx;
        app = (MyApplication) ctx.getApplicationContext();
        //SetLogDir();
    }
    
    private void SetLogDir(){
    	String str_root_dir = Environment.getExternalStorageDirectory().getPath();
        str_root_dir += "/gjjlogdir";
        
        File root_dir = new File(str_root_dir);
        if(!root_dir.exists())root_dir.mkdirs();
   
        
        String str_log_dir = str_root_dir+"/logs";
        File dir = new File(str_log_dir);
        if(!dir.exists())dir.mkdirs();
        Init_log4j(dir.getAbsolutePath());
    }
    
    private void Init_log4j(String log_dir)
    {
        try
        { 
            PatternLayout layout = new PatternLayout("%d{yyyy-mm-dd HH:mm:ss} : %m [%l]%n");
            logger.addAppender(new DailyRollingFileAppender(layout, log_dir + "/daily.log", "yyyy-MM-dd'.log'"));
            logger.setLevel((Level) Level.INFO);
            logger.info("config logger finish.");
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
	@Override
	public void uncaughtException(Thread thread, Throwable ex) { // ����δ������쳣����ʱ���ͻ����������
		
		String threadName = thread.getName();
		 if (!handleException(ex) && mDefaultHandler != null) {  
	            //����û�û�д�������ϵͳĬ�ϵ��쳣������������  
	            mDefaultHandler.uncaughtException(thread, ex);  
	        } else {  
	            try {  
	                Thread.sleep(3000);  
	            } catch (InterruptedException e) {  
	                Log.e(TAG, "error : ", e);  
	            }  
	            //�˳�����  

	            if ("main".equals(threadName)) {
	            	android.os.Process.killProcess(android.os.Process.myPid());  
		            System.exit(1);
	    		} else if("InputThread".equals(threadName)){
	    			SocketInputThread inputthread=new SocketInputThread(app.GetDatagramSocket(), context);
	    			inputthread.start();
	    			if(app.Getbackground()!=null){
	    				app.SetSocketInputThread(inputthread);
	    				app.Getbackground().SetSocketInputThread(inputthread);
	    			}
	    		}else if("OutputThread".equals(threadName)){
	    			SocketOutputThread outputthread=new SocketOutputThread(app.GetDatagramSocket(),context);
	    			outputthread.start();
	    			if(app.Getbackground()!=null){
	    				app.SetSocketOutputThread(outputthread);
	    				app.Getbackground().SetSocketOutputThread(outputthread);
	    			}
	    		}
	        }  
	}
	
	/** 
     * �Զ��������,�ռ�������Ϣ ���ʹ��󱨸�Ȳ������ڴ����. 
     *  
     * @param ex 
     * @return true:��������˸��쳣��Ϣ;���򷵻�false. 
     */  
    private boolean handleException(Throwable ex) {  
        if (ex == null) {  
            return false;  
        }  
        //ʹ��Toast����ʾ�쳣��Ϣ  
        new Thread() {  
            @Override  
            public void run() {  
                Looper.prepare();  
                Toast.makeText(context, context.getString(R.string.crash_handler_software_exception), Toast.LENGTH_LONG).show();  
                Looper.loop();  
            }  
        }.start();  
        //�ռ��豸������Ϣ   
        collectDeviceInfo(context);  
        //������־�ļ�   
        saveCrashInfo2File(ex);  
        return true;  
    }  
    /** 
     * �ռ��豸������Ϣ 
     * @param ctx 
     */  
    public void collectDeviceInfo(Context ctx) {  
        try {  
            PackageManager pm = ctx.getPackageManager();  
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);  
            if (pi != null) {  
                String versionName = pi.versionName == null ? "null" : pi.versionName;  
                String versionCode = pi.versionCode + "";  
                infos.put("versionName", versionName);  
                infos.put("versionCode", versionCode);  
            }  
        } catch (NameNotFoundException e) {  
            Log.e(TAG, "an error occured when collect package info", e);  
        }  
        Field[] fields = Build.class.getDeclaredFields();  
        for (Field field : fields) {  
            try {  
                field.setAccessible(true);  
                infos.put(field.getName(), field.get(null).toString());  
                Log.d(TAG, field.getName() + " : " + field.get(null));  
            } catch (Exception e) {  
                Log.e(TAG, "an error occured when collect crash info", e);  
            }  
        }  
    }  
    /** 
     * ���������Ϣ���ļ��� 
     *  
     * @param ex 
     * @return  �����ļ�����,���ڽ��ļ����͵������� 
     */  
    private String saveCrashInfo2File(Throwable ex) {  
          
        StringBuffer sb = new StringBuffer();  
        for (Map.Entry<String, String> entry : infos.entrySet()) {  
            String key = entry.getKey();  
            String value = entry.getValue();  
            sb.append(key + "=" + value + "\n");  
        }  
          
        Writer writer = new StringWriter();  
        PrintWriter printWriter = new PrintWriter(writer);  
        ex.printStackTrace(printWriter);  
        Throwable cause = ex.getCause();  
        while (cause != null) {  
            cause.printStackTrace(printWriter);  
            cause = cause.getCause();  
        }  
        printWriter.close();  
        String result = writer.toString();  
        sb.append(result);  
        try {  
            long timestamp = System.currentTimeMillis();  
            String time = formatter.format(new Date());  
            String fileName = "crash-" + time + "-" + timestamp + ".log";  
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {  
                String path = "/sdcard/crash/";  
                File dir = new File(path);  
                if (!dir.exists()) {  
                    dir.mkdirs();  
                }  
                FileOutputStream fos = new FileOutputStream(path + fileName);  
                fos.write(sb.toString().getBytes());  
                fos.close();  
            }  
            return fileName;  
        } catch (Exception e) {  
            Log.e(TAG, "an error occured while writing file...", e);  
        }  
        return null;  
    }  
}
