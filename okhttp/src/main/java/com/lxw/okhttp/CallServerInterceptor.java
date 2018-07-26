package com.lxw.okhttp;

import com.lxw.okhttp.connection.RealConnection;
import com.lxw.okhttp.connection.StreamAllocation;
import com.lxw.okhttp.http.HttpCodec;
import com.lxw.okhttp.interceptor.Chain;
import com.lxw.okhttp.interceptor.Interceptor;
import com.lxw.okhttp.interceptor.RealInterceptorChain;

import java.io.IOException;
import java.util.HashMap;

/**
 * <pre>
 *     author : lxw
 *     e-mail : lsw@tairunmh.com
 *     time   : 2018/07/26
 *     desc   :
 * </pre>
 */
public class CallServerInterceptor implements Interceptor {
    @Override
    public Response intercep(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        HttpCodec httpCodec = realChain.httpCodec();
        Request request = realChain.request();
        StreamAllocation streamAllocation = realChain.streamAllocation();
        RealConnection connection = realChain.connection();

        long sendRequestMillis = System.currentTimeMillis();
        //完成写的操作
        httpCodec.writeRequestHeaders(request);
        String statusLine = httpCodec.readLine();
        HashMap<String, String> map = httpCodec.readHeader();
        Integer contentLength = 1;
        if (map.containsKey(HttpCodec.HEAD_CONTENT_LENGTH)) {
            contentLength = Integer.valueOf(map.get(HttpCodec.HEAD_CONTENT_LENGTH));
        }
        boolean isChunked = false;
        if (map.containsKey(HttpCodec.HEAD_TRANSFER_ENCODING)) {
            isChunked = true;
        }
        String body = null;
        if (contentLength != -1 && !isChunked) {
            body = new String(httpCodec.readBody(contentLength));
        } else if (isChunked) {
            body = httpCodec.readChunked();
        }


        String[] status = statusLine.split(" ");
        connection.updateUseTime();
        return new Response(request, Integer.valueOf(status[1]), body, map);
    }
}
