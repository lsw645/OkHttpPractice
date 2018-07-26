package com.lxw.okhttp;

import com.lxw.okhttp.connection.ConnectionPool;
import com.lxw.okhttp.http.CookieJar;

/**
 * <pre>
 *     author : lxw
 *     e-mail : lsw@tairunmh.com
 *     time   : 2018/07/22
 *     desc   :
 * </pre>
 */
public class OkHttpClient {
    private Dispatcher dispatcher;
    private int readTimeOut;
    private int writeTimeOut;
    private int connectiTimeOut;
    private ConnectionPool connectionPool;
    private CookieJar cookieJar;

    private OkHttpClient() {

    }

    public OkHttpClient(Builder builder) {
        dispatcher = builder.dispatcher;
        readTimeOut = builder.readTimeOut;
        writeTimeOut = builder.writeTimeOut;
        connectiTimeOut = builder.connectiTimeOut;
        connectionPool = builder.connectionPool;
        cookieJar = builder.cookieJar;
    }

    public ConnectionPool connectionPool() {
        return connectionPool;
    }

    public int readTimeOut() {
        return readTimeOut;
    }

    public int writeTimeOut() {
        return writeTimeOut;
    }

    public int connectiTimeOut() {
        return connectiTimeOut;
    }

    public RealCall newCall(Request request) {
        return new RealCall(this, request);
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public CookieJar cookieJar() {
        return cookieJar;
    }

    public static class Builder {
        private Dispatcher dispatcher;
        private int readTimeOut;
        private int writeTimeOut;
        private int connectiTimeOut;
        private ConnectionPool connectionPool;
        private CookieJar cookieJar;

        public Builder() {
            dispatcher = new Dispatcher();
            writeTimeOut = 10_000;
            readTimeOut = 10_000;
            connectiTimeOut = 10_000;
            connectionPool = new ConnectionPool();
            cookieJar = CookieJar.NO_COOKIES;
        }

        public OkHttpClient build() {
            return new OkHttpClient(this);
        }
    }
}
