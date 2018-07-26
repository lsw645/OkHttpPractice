package com.lxw.okhttp.connection;

import com.lxw.okhttp.HttpUrl;
import com.lxw.okhttp.OkHttpClient;
import com.lxw.okhttp.http.HttpCodec;
import com.lxw.okhttp.interceptor.RealInterceptorChain;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;

/**
 * <pre>
 *     author : lxw
 *     e-mail : lsw@tairunmh.com
 *     time   : 2018/07/25
 *     desc   :
 * </pre>
 */
public class RealConnection {
    private ConnectionPool connnectionPool;
    private Socket socket;
    private InputStream reader;
    private OutputStream writer;
    private HttpUrl httpUrl;
    public long lastUseTime ;

    //使用弱引用管理 StreamAllocation 值  如果存在StreamAllocation,则表示当前还在使用中
    /**
     * Current streams carried by this connection.
     */
    public final List<Reference<StreamAllocation>> allocations = new ArrayList<>();
    //http2中允许 IO多路复用时，该值不为1
    public int allocationLimit = 1;


    public void updateUseTime() {
        this.lastUseTime = System.currentTimeMillis();
    }

    public RealConnection(ConnectionPool connnectionPool, HttpUrl httpUrl) {
        this.connnectionPool = connnectionPool;
        this.httpUrl = httpUrl;
        updateUseTime();
    }

    public HttpUrl httpUrl() {
        return httpUrl;
    }

    public boolean isHealthy(boolean doExtensiveChecks) {
        if (socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
            return false;
        }
        return true;
//   TODO 判断是否能读，后续学习OkIo 后尝试学习。
//        if (doExtensiveChecks) {
//            try {
//                int readTimeout = socket.getSoTimeout();
//                try {
//                    socket.setSoTimeout(1);
//                    if (source.exhausted()) {
//                        return false; // Stream is exhausted; socket is closed.
//                    }
//                    return true;
//                } finally {
//                    socket.setSoTimeout(readTimeout);
//                }
//            } catch (SocketTimeoutException ignored) {
//                // Read timed out; socket is good.
//            } catch (IOException e) {
//                return false; // Couldn't read; socket is closed.
//            }
//        return false;
    }

    public void connect(int connectTimeout, int readTimeout, int writeTimeout) throws IOException {
        String scheme = httpUrl.scheme();
        if (scheme.equalsIgnoreCase("https")) {
            socket = SSLSocketFactory.getDefault().createSocket();
        } else {
            socket = new Socket();
        }
        socket.setSoTimeout(readTimeout);
        InetSocketAddress address = new InetSocketAddress(httpUrl.host(), httpUrl.port());
        socket.connect(address, connectTimeout);
        reader = socket.getInputStream();
        writer = socket.getOutputStream();
    }

    public HttpCodec newCodec(OkHttpClient okhttpClient, RealInterceptorChain realChain,
                              StreamAllocation streamAllocation) {

        return new HttpCodec(okhttpClient, streamAllocation, reader, writer);

    }

    public boolean isEligible(HttpUrl url) {
        if (allocations.size() > allocationLimit) {
            return false;
        }

        return url.host().equals(httpUrl().host()) && url.port() == httpUrl().port();
    }

    public void closeQuickly() {
        if (socket != null) {
            try {
                socket.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {

            }
        }
    }


}
