package com.lxw.okhttp.http;

import com.lxw.okhttp.HttpUrl;
import com.lxw.okhttp.OkHttpClient;
import com.lxw.okhttp.RealCall;
import com.lxw.okhttp.Request;
import com.lxw.okhttp.Response;
import com.lxw.okhttp.connection.StreamAllocation;
import com.lxw.okhttp.interceptor.Chain;
import com.lxw.okhttp.interceptor.Interceptor;
import com.lxw.okhttp.interceptor.RealInterceptorChain;

import java.io.IOException;
import java.net.ProtocolException;

/**
 * <pre>
 *     author : lxw
 *     e-mail : lsw@tairunmh.com
 *     time   : 2018/07/25
 *     desc   :
 * </pre>
 */
public class RetryAndFollowUpInterceptor implements Interceptor {
    private OkHttpClient okhttpClient;
    private final static int FOLLOW_UP_MAX = 20;
    private volatile boolean canceled;
    private StreamAllocation streamAllocation;

    public RetryAndFollowUpInterceptor(OkHttpClient okhttpClient) {
        this.okhttpClient = okhttpClient;
    }

    public void cancel() {
        canceled = true;
        StreamAllocation streamAllocation = this.streamAllocation;
        if (streamAllocation != null) {
            streamAllocation.cancel();
        }
    }

    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public Response intercep(Chain chain) throws IOException {

        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Request request = chain.request();
        RealCall call = chain.call();
        StreamAllocation streamAllocation = new StreamAllocation(okhttpClient.connectionPool(),
                request.url(), call);
        this.streamAllocation = streamAllocation;
        int followUpCount = 1;
        while (true) {
            if (canceled) {
                streamAllocation.release();
                throw new IOException("Canceled");
            }
            Response response;
            response = realChain.proceed(request, streamAllocation, null, null);
            Request followUp = followUpRequest(response);

            if (followUp == null) {
                streamAllocation.release();
                return response;
            }

            if (++followUpCount > FOLLOW_UP_MAX) {
                streamAllocation.release();
                throw new ProtocolException("Too many follow-up requests: " + followUpCount);
            }

            if (!sameConnection(response, followUp.url())) {
                streamAllocation.release();
                streamAllocation = new StreamAllocation(okhttpClient.connectionPool(),
                        followUp.url(), call);
                this.streamAllocation = streamAllocation;
            }
            request = followUp;
        }
//        return response;
    }

    private boolean sameConnection(Response response, HttpUrl followUp) {
        HttpUrl url = response.request().url();
        return url.host().equals(followUp.host())
                && url.port() == followUp.port()
                && url.scheme().equals(followUp.scheme());
    }

    //TODO 待后续完善
    private Request followUpRequest(Response response) {
        return null;
    }
}
