package com.lxw.okhttp.interceptor;

import com.lxw.okhttp.RealCall;
import com.lxw.okhttp.Request;
import com.lxw.okhttp.Response;
import com.lxw.okhttp.connection.RealConnection;
import com.lxw.okhttp.connection.StreamAllocation;
import com.lxw.okhttp.http.HttpCodec;

import java.io.IOException;
import java.util.List;

/**
 * <pre>
 *     author : lxw
 *     e-mail : lsw@tairunmh.com
 *     time   : 2018/07/25
 *     desc   :
 * </pre>
 */
public class RealInterceptorChain implements Chain {
    private List<Interceptor> interceptors;
    private StreamAllocation streamAllocation;
    private RealConnection connection;
    private Request request;
    private HttpCodec httpCodec;
    private RealCall call;
    private int index;
    private int readTimeOut;
    private int writeTimeOut;
    private int connectiTimeOut;

    public RealInterceptorChain(List<Interceptor> interceptors, StreamAllocation streamAllocation,
                                RealConnection connection, Request request, HttpCodec httpCodec,
                                RealCall call, int index, int readTimeOut, int writeTimeOut, int connectiTimeOut) {
        this.interceptors = interceptors;
        this.streamAllocation = streamAllocation;
        this.connection = connection;
        this.request = request;
        this.httpCodec = httpCodec;
        this.call = call;
        this.index = index;
        this.readTimeOut = readTimeOut;
        this.writeTimeOut = writeTimeOut;
        this.connectiTimeOut = connectiTimeOut;
    }

    @Override
    public Response proceed(Request request) throws IOException {
       return proceed(request,streamAllocation,httpCodec,connection);
    }

    public Response proceed(Request request, StreamAllocation streamAllocation, HttpCodec codec,RealConnection connection) throws IOException {
        if (index >= interceptors.size()) {
            throw new AssertionError();
        }
        RealInterceptorChain realInterceptorChain = new RealInterceptorChain(
                interceptors, streamAllocation, connection, request, codec, call,
                index + 1, readTimeOut, writeTimeOut, connectiTimeOut
        );
        Interceptor interceptor = interceptors.get(index);
        return interceptor.intercep(realInterceptorChain);
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public RealCall call() {
        return call;
    }

    @Override
    public int connectTimeOut() {
        return connectiTimeOut;
    }

    @Override
    public int readTimeOut() {
        return readTimeOut;
    }

    @Override
    public int writeTimeOut() {
        return writeTimeOut;
    }

    public StreamAllocation streamAllocation() {
        return streamAllocation;
    }

    public HttpCodec httpCodec() {
        return httpCodec;
    }

    public RealConnection connection() {
        return connection;
    }
}
