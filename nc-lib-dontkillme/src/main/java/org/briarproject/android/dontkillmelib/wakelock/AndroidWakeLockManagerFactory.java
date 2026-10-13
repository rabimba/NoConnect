package org.briarproject.android.dontkillmelib.wakelock;

import android.app.Application;

import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

@NotNullByDefault
public class AndroidWakeLockManagerFactory {

    public static AndroidWakeLockManager createAndroidWakeLockManager(Application app) {
        RejectedExecutionHandler policy = new ThreadPoolExecutor.DiscardPolicy();
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1, policy);
        return new AndroidWakeLockManagerImpl(app, executorService);
    }

    public static AndroidWakeLockManager createAndroidWakeLockManager(Application app,
                                                                      ScheduledExecutorService executorService) {
        return new AndroidWakeLockManagerImpl(app, executorService);
    }

}
