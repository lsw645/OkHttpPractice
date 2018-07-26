package com.lxw.okhttp.http;

import com.lxw.okhttp.HttpUrl;
import com.lxw.okhttp.Request;
import com.lxw.okhttp.RequestBody;
import com.lxw.okhttp.Response;
import com.lxw.okhttp.interceptor.Chain;
import com.lxw.okhttp.interceptor.Interceptor;

import java.io.IOException;

/**
 * <pre>
 *     author : lxw
 *     e-mail : lsw@tairunmh.com
 *     time   : 2018/07/25
 *     desc   :
 * </pre>
 */
public class BridgeInterceptor implements Interceptor {
    private CookieJar cookieJar;

    public BridgeInterceptor(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
    }

    @Override
    public Response intercep(Chain chain) throws IOException {
        Request request = chain.request();
        RequestBody body = request.body();
        Request.Builder requestBuilder = request.newBuilder();

        if (body != null) {
            requestBuilder.addHeader("Content-Length", RequestBody.getContentType());

            int contentLength = -1;
            if ((contentLength = body.contentLength()) != -1) {
                requestBuilder.addHeader("Content-Length", String.valueOf(contentLength));
                requestBuilder.removeBuilder("Transfer-Encoding");
            } else {
                requestBuilder.addHeader("Transfer-Encoding", "chunked");
                requestBuilder.removeBuilder("Content-Length");
            }
        }


        requestBuilder.addHeader("Connection", "Keep-Alive");

        String cookies = cookieJar.loadForRequest(request.url());
        if (!cookies.isEmpty()) {
            requestBuilder.addHeader("Cookie", cookies);
        }
//        requestBuilder.addHeader("User-Agent", "real okhttpclient ");
        requestBuilder.addHeader("Host", request.url().host());
        //TODO 执行下一个拦截器
        Response networkResponse = chain.proceed(requestBuilder.build());
        //TODO 解析响应头 获得Cookie 给回调CookieJar
        receiveHeaders(cookieJar, request.url(), networkResponse.request());
        return networkResponse;
    }

    private void receiveHeaders(CookieJar cookieJar, HttpUrl url, Request request) {
        if (cookieJar == CookieJar.NO_COOKIES) return;

//        List<Cookie> cookies = Cookie.parseAll(url, headers);
////        if (cookies.isEmpty()) return;

        cookieJar.saveFromResponse(url, request.header().get("cookie"));
    }
}
