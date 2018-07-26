package com.lxw.okhttp.connection;

import com.lxw.okhttp.OkHttpClient;
import com.lxw.okhttp.Request;
import com.lxw.okhttp.Response;
import com.lxw.okhttp.http.HttpCodec;
import com.lxw.okhttp.interceptor.Chain;
import com.lxw.okhttp.interceptor.Interceptor;
import com.lxw.okhttp.interceptor.RealInterceptorChain;

import java.io.IOException;

/**
 * <pre>
 *     author : lxw
 *     e-mail : lsw@tairunmh.com
 *     time   : 2018/07/25
 *     desc   :
 * </pre>
 */
public class ConnectInterceptor implements Interceptor {
    private OkHttpClient okhttpClient;

    public ConnectInterceptor(OkHttpClient okhttpClient) {
        this.okhttpClient = okhttpClient;
    }

    @Override
    public Response intercep(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Request request = realChain.request();
        StreamAllocation streamAllocation = realChain.streamAllocation();
        // We need the network to satisfy this request. Possibly for validating a conditional GET.
        boolean doExtensiveHealthChecks = !request.method().equals("GET");
        HttpCodec codec = streamAllocation.newStream(okhttpClient,realChain,doExtensiveHealthChecks);
        RealConnection connection = streamAllocation.connection();

        return realChain.proceed(request, streamAllocation, codec,connection);
    }
}
