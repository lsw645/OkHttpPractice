package com.lxw.okhttp.connection;

import com.lxw.okhttp.HttpUrl;

import java.lang.ref.Reference;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 *     author : lxw
 *     e-mail : lsw@tairunmh.com
 *     time   : 2018/07/25
 *     desc   :
 * </pre>
 */
public class ConnectionPool {
    private final Deque<RealConnection> connections = new ArrayDeque<>();

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread result = new Thread(r, "OkHttp ConnectionPool");
            ;
            //守护线程，当进程内其他线程被回收完后，守护线程开始回收
            result.setDaemon(true);
            return result;
        }
    });

    private final int maxIdleConnections;
    private final long keepAliveDurations;
    boolean cleanupRunning;

    private Runnable cleanupRunnable = new Runnable() {
        @Override
        public void run() {
            long waitNanos = cleanup(System.currentTimeMillis());
            if (waitNanos == -1) {
                return;
            }
            synchronized (executor) {
            if (waitNanos > 0) {
                    try {
                        executor.wait(waitNanos);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private long cleanup(long now) {
        int idleUseCount = 0;
        int connectUseCount = 0;
        long longestDuration = Long.MIN_VALUE;
        RealConnection longestIdleConnection = null;
        synchronized (ConnectionPool.this) {
            for (RealConnection next : connections) {
                if (pruneAndGetAllocationCount(next, now) > 0) {
                    connectUseCount++;
                    continue;
                }
                //限制时间
                long idleDuration = now - next.lastUseTime;
                idleUseCount++;
                if (idleDuration > longestDuration) {
                    longestDuration = idleDuration;
                    longestIdleConnection = next;
                }
            }

            if (idleUseCount > maxIdleConnections || longestDuration > keepAliveDurations) {
                longestIdleConnection.closeQuickly();
                connections.remove(longestIdleConnection);
            } else if (idleUseCount > 0) {
                return keepAliveDurations - longestDuration;
            } else if (connectUseCount > 0) {
                return keepAliveDurations;
            } else {
                cleanupRunning = false;
                return -1;
            }
        }
        return 0;
    }

    /**
     * Prunes any leaked allocations and then returns the number of remaining live allocations on
     * {@code connection}. Allocations are leaked if the connection is tracking them but the
     * application code has abandoned them. Leak detection is imprecise and relies on garbage
     * collection.
     */
    private int pruneAndGetAllocationCount(RealConnection connection, long now) {
        List<Reference<StreamAllocation>> references = connection.allocations;
        for (int i = 0; i < references.size(); ) {
            Reference<StreamAllocation> reference = references.get(i);

            if (reference.get() != null) {
                i++;
                continue;
            }

            // We've discovered a leaked allocation. This is an application bug.
            StreamAllocation.StreamAllocationReference streamAllocRef =
                    (StreamAllocation.StreamAllocationReference) reference;
//            String message = "A connection to " + connection.route().address().url()
//                    + " was leaked. Did you forget to close a response body?";
//            Platform.get().logCloseableLeak(message, streamAllocRef.callStackTrace);

            references.remove(i);
//            connection.noNewStreams = true;

            // If this was the last allocation, the connection is eligible for immediate eviction.
            if (references.isEmpty()) {
                connection.lastUseTime = now - keepAliveDurations;
                return 0;
            }
        }

        return references.size();
    }

//    private long cleanup(long now) {
//        int inUseConnectionCount = 0;
//        int idleConnectionCount = 0;
//        RealConnection longestIdleConnection = null;
//        long longestIdleDurationNs = Long.MIN_VALUE;
//        Iterator<RealConnection> iterator = connections.iterator();
//        while (iterator.hasNext()) {
//            RealConnection connection = iterator.next();
//            long idleDurtation = now - connection.lastUseTime;
//            if (idleDurtation >= keepAliveDurations) {
//                iterator.remove();
//            }
//            //记录 最长的 闲置时间
//            if (longestIdleDurationNs < idleDurtation) {
//                longestIdleDurationNs = idleDurtation;
//            }
//            // 假如 keepAlive 10s
//            // longestIdleDuration 是5s
//            if (longestIdleDurationNs >= 0) {
//                return keepAliveDurations - longestIdleDurationNs;
//            }
//            //连接池中没有连接
//            cleanupRunning = false;
//        }
//        return longestIdleDurationNs;
//    }


    public ConnectionPool() {
        this(5, 5, TimeUnit.MINUTES);
    }

    public ConnectionPool(int maxIdleConnections, long keepAliveDurations, TimeUnit unit) {
        this.maxIdleConnections = maxIdleConnections;
        this.keepAliveDurations = unit.toNanos(keepAliveDurations);
        // Put a floor on the keep alive duration, otherwise cleanup will spin loop.
        if (keepAliveDurations <= 0) {
            throw new IllegalArgumentException("keepAliveDuration <= 0: " + keepAliveDurations);
        }
    }

    public RealConnection get(HttpUrl url, StreamAllocation streamAllocation) {
        for (RealConnection connection : connections) {
            if (connection.isEligible(url)) {
                streamAllocation.acquire(connection);
                return connection;
            }
        }
        return null;
    }

    public void put(RealConnection connection) {
        if (!cleanupRunning) {
            cleanupRunning = true;
            executor.execute(cleanupRunnable);
        }
        connections.add(connection);
    }
}
