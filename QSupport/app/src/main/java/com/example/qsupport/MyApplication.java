package com.example.qsupport;

import android.app.Application;
import android.content.Intent;
import java.io.PrintWriter;
import java.io.StringWriter;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread thread, Throwable ex) {
					StringWriter sw = new StringWriter();
					ex.printStackTrace(new PrintWriter(sw));

					Intent intent = new Intent(getApplicationContext(), CrashActivity.class);
					intent.putExtra("error", sw.toString());
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(intent);

					android.os.Process.killProcess(android.os.Process.myPid());
					System.exit(1);
				}
			});
    }
}

