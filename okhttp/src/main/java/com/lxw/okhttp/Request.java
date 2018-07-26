package com.lxw.okhttp;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *     author : lxw
 *     e-mail : lsw@tairunmh.com
 *     time   : 2018/07/22
 *     desc   :
 * </pre>
 */
public class Request {
    private String method;
    private HttpUrl url;
    private Map<String, String> header;
    private RequestBody body;

    private Request() {
    }

    public Request(Builder builder) {
        this.method = builder.method;
        this.url = builder.httpUrl;
        this.header = builder.header;
        this.body = builder.body;
    }


    public String method() {
        return method;
    }

    public HttpUrl url() {
        return url;
    }

    public Map<String, String> header() {
        return header;
    }

    public RequestBody body() {
        return body;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }
    public static class Builder {
        private String method = "GET";
        private HttpUrl httpUrl;
        private Map<String, String> header;
        private RequestBody body;

        public Builder() {
            header = new HashMap<>();
        }

        public Builder(Request request) {
            this.httpUrl = request.url;
            this.method = request.method;
            this.body = request.body;
            this.header = request.header;
        }

        public Builder get() {
            method = "GET";
            return this;
        }

        public Builder post(RequestBody body) {
            this.method = "POST";
            this.body = body;
            return this;
        }

        public Builder url(String url) {
            this.httpUrl = new HttpUrl(url);
            return this;
        }

        public Builder addHeader(String key, String value) {
            header.put(key, value);
            return this;
        }

        public Builder removeBuilder(String key){
            if(header.containsKey(key)){
                header.remove(key);
            }
            return this;
        }

        public Request build() {
            return new Request(this);
        }
    }


}
