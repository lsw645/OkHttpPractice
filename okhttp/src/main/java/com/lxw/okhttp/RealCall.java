package com.lxw.okhttp;

import com.lxw.okhttp.connection.ConnectInterceptor;
import com.lxw.okhttp.http.BridgeInterceptor;
import com.lxw.okhttp.http.RetryAndFollowUpInterceptor;
import com.lxw.okhttp.interceptor.Interceptor;
import com.lxw.okhttp.interceptor.RealInterceptorChain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : lxw
 *     e-mail : lsw@tairunmh.com
 *     time   : 2018/07/22
 *     desc   :
 * </pre>
 */
public class RealCall {

    private OkHttpClient okHttpClient;
    private Request request;
    private boolean executed;
    private RetryAndFollowUpInterceptor retryAndFollowUpInterceptor;

    public RealCall(OkHttpClient okHttpClient, Request request) {
        this.okHttpClient = okHttpClient;
        this.request = request;
        this.retryAndFollowUpInterceptor = new RetryAndFollowUpInterceptor(okHttpClient);
    }

    public void cancel() {
        retryAndFollowUpInterceptor.cancel();
    }

    public synchronized boolean isExecuted() {
        return executed;
    }

    public boolean isCanceled() {
        return retryAndFollowUpInterceptor.isCanceled();
    }

    public void enqueue(Callback callback) {
        synchronized (this) {
            if (executed) {
                throw new IllegalStateException("");
            }
            executed = true;
        }
        okHttpClient.dispatcher().enqueue(new AsyncCall(callback));
    }

    public class AsyncCall implements Runnable {
        private Callback callback;

        public AsyncCall(Callback callback) {
            this.callback = callback;
        }

        public Request request() {
            return request;
        }

        public String host() {
            return request.url().host();
        }

        @Override
        public void run() {
            execute();
        }

        private void execute() {
            boolean sigalledCallback = false;
            try {
                Response response = getResponseWithInterceptorChain(request);
                if (retryAndFollowUpInterceptor.isCanceled()) {
                    sigalledCallback = true;
                    callback.onFailure(RealCall.this, new Exception("on cancel"));
                } else {
                    sigalledCallback = true;
                    callback.onSuccess(RealCall.this, response);
                }

            } catch (IOException e) {
                e.printStackTrace();
                if (sigalledCallback) {
                    callback.onFailure(RealCall.this, e);
                } else {
                    callback.onFailure(RealCall.this,e);
                }
            } finally {
                okHttpClient.dispatcher().finished(this);
            }

        }
    }

    private Response getResponseWithInterceptorChain(Request request) throws IOException {
        List<Interceptor> interceptors = new ArrayList<>();

        interceptors.add(retryAndFollowUpInterceptor);
        interceptors.add(new BridgeInterceptor(okHttpClient.cookieJar()));
        interceptors.add(new ConnectInterceptor(okHttpClient));
        interceptors.add(new CallServerInterceptor());
        RealInterceptorChain chain = new RealInterceptorChain(interceptors, null, null, request,
                null, RealCall.this, 0,
                okHttpClient.readTimeOut(), okHttpClient.writeTimeOut(), okHttpClient.connectiTimeOut());
        return chain.proceed(request);
    }
}
