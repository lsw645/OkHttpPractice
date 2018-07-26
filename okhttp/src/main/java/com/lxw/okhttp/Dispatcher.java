package com.lxw.okhttp;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 *     author : lxw
 *     e-mail : lsw@tairunmh.com
 *     time   : 2018/07/22
 *     desc   :
 * </pre>
 */
public class Dispatcher {
    //一个Okhttp  只有一个Dispatcher 所以 队列不适用static

    private final ArrayDeque<RealCall.AsyncCall> runningDeque = new ArrayDeque<>();

    private final ArrayDeque<RealCall.AsyncCall> readyQueue = new ArrayDeque<>();

    private int maxRequests = 64;

    private int maxRequestsPreHost = 5;

    private ExecutorService mExecutorService;

    public Dispatcher() {
    }

    public Dispatcher(ExecutorService executorService) {
        mExecutorService = executorService;
    }


    public synchronized void enqueue(RealCall.AsyncCall asyncCall) {
        if (runningDeque.size() < maxRequests && runningCallForHost(asyncCall) < maxRequestsPreHost) {
            runningDeque.add(asyncCall);
            executor().execute(asyncCall);
        } else {
            readyQueue.add(asyncCall);
        }
    }

    private int runningCallForHost(RealCall.AsyncCall asyncCall) {
        int count = 0;
        for (RealCall.AsyncCall call : runningDeque) {
            if (call.host().equals(asyncCall.host())) {
                count++;
            }
        }
        return count;
    }

    private int count;

    public synchronized ExecutorService executor() {
        if (mExecutorService == null) {
            ThreadFactory threadFactory = new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    count++;
                    return new Thread(r, "HttpClient" + count);
                }
            };
            mExecutorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), threadFactory);
        }
        return mExecutorService;
    }


    public void finished(RealCall.AsyncCall asyncCall) {
        runningDeque.remove(asyncCall);
        promoteCalls();
    }

    /**
     *  查找可加入执行的runnable
     */
    private void promoteCalls() {
        if (runningDeque.size() >= maxRequests) {
            return;
        }
        if (readyQueue.isEmpty()) {
            return;
        }
        Iterator<RealCall.AsyncCall> iterator = readyQueue.iterator();
        while (iterator.hasNext()) {
            RealCall.AsyncCall next = iterator.next();
            if (runningCallForHost(next) < maxRequestsPreHost) {
                runningDeque.add(next);
                iterator.remove();
                executor().execute(next);
            }
            if (runningDeque.size() >= maxRequests) {
                break;
            }
        }


    }
}
