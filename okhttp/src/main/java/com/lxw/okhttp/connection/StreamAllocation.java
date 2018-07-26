package com.lxw.okhttp.connection;

import com.lxw.okhttp.HttpUrl;
import com.lxw.okhttp.OkHttpClient;
import com.lxw.okhttp.RealCall;
import com.lxw.okhttp.http.HttpCodec;
import com.lxw.okhttp.interceptor.RealInterceptorChain;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * <pre>
 *     author : lxw
 *     e-mail : lsw@tairunmh.com
 *     time   : 2018/07/25
 *     desc   :
 * </pre>
 */
public class StreamAllocation {
    private final ConnectionPool connectionPool;
    private RealConnection connection;
    private HttpUrl url;
    private RealCall realCall;
    private HttpCodec codec;
    private boolean released;
    private boolean canceled;

    public StreamAllocation(ConnectionPool connectionPool, HttpUrl url, RealCall realCall) {
        this.connectionPool = connectionPool;
        this.url = url;
        this.realCall = realCall;
    }

    public void release() {


    }

    public void cancel() {

    }

    public RealConnection connection() {
        return connection;
    }

    public HttpCodec newStream(OkHttpClient okhttpClient, RealInterceptorChain realChain,
                               boolean doExtensiveHealthChecks) throws IOException {

        int connectTimeout = realChain.connectTimeOut();
        int readTimeout = realChain.readTimeOut();
        int writeTimeout = realChain.writeTimeOut();

        //TODO RealConnection 对Socket连接的封装
        //TODO TPC/IP协议是传输层协议，主要解决数据如何在网络中传输
        //TODO Socket则是对TCP/IP协议的封装和应用(程序员层面上)。
        //TODO Http 应用层协议,解决如何包装数据
        //TODO 使用Http协议封装数据，借助TCP/IP协议的实现:Socket 进行数据传输
        RealConnection realConnection = findHealthyConnection(connectTimeout, readTimeout,
                writeTimeout, doExtensiveHealthChecks);
        HttpCodec codec = realConnection.newCodec(okhttpClient, realChain, this);
        synchronized (connectionPool) {
            this.codec = codec;
            return codec;
        }
    }

    private RealConnection findHealthyConnection(int connectTimeout, int readTimeout, int writeTimeout, boolean doExtensiveHealthChecks) throws IOException {

//        while (true) {
        RealConnection connection = findConnection(connectTimeout, readTimeout, writeTimeout);
        return connection;
//            if (connection.isHealthy(doExtensiveHealthChecks)) {
//                return connection;
//            }
//        }
    }

    private RealConnection findConnection(int connectTimeout, int readTimeout, int writeTimeout) throws IOException {
        boolean foundPooledConnection = false;
        RealConnection result = null;
        RealConnection releasedConnection;
        synchronized (connectionPool) {
            if (released) throw new IllegalStateException("released");
            if (codec != null) throw new IllegalStateException("codec != null");
            if (canceled) throw new IOException("Canceled");


            releasedConnection = this.connection;
            if (this.connection != null) {
                // We had an already-allocated connection and it's good.
                result = this.connection;
                releasedConnection = null;
            }

            if (result == null) {
                this.connection = connectionPool.get(url, this);
                if (connection != null) {
                    foundPooledConnection = true;
                    result = connection;
                }
            }
        }
        //TODO 连接可用就返回 否则需要创建新的连接
        if (result != null) {
            // If we found an already-allocated or pooled connection, we're done.
            return result;
        }

        synchronized (connectionPool) {
            if (canceled) throw new IOException("Canceled");

            if (!foundPooledConnection) {
                //TODO 创建新的连接
                result = new RealConnection(connectionPool, url);
                this.connection = result;
                //TODO 使用弱引用进行引用计数
            }
        }
        result.connect(connectTimeout, readTimeout, writeTimeout);
        synchronized (connectionPool) {
            //TODO 加入连接池
            connectionPool.put(result);
        }

        return result;
    }

    /**
     * Use this allocation to hold {@code connection}. Each call to this must be paired with a
     * call to
     * {@link #release} on the same connection.
     */
    public void acquire(RealConnection connection) {
        assert (Thread.holdsLock(connectionPool));
        if (this.connection != null) throw new IllegalStateException();

        this.connection = connection;

        connection.allocations.add(new StreamAllocationReference(this));
    }


    public static final class StreamAllocationReference extends WeakReference<StreamAllocation> {
        /**
         * Captures the stack trace at the time the Call is executed or enqueued. This is helpful
         * for
         * identifying the origin of connection leaks.
         */

        StreamAllocationReference(StreamAllocation referent) {
            super(referent);
        }
    }

}
