package com.lxw.okhttp;

import java.util.HashMap;

/**
 * <pre>
 *     author : lxw
 *     e-mail : lsw@tairunmh.com
 *     time   : 2018/07/22
 *     desc   :
 * </pre>
 */
public class Response {
    private Request request;
    private int code;
    private String body;
    private HashMap<String,String> header ;

    public Response(Request request, int code, String body, HashMap<String, String> header) {
        this.request = request;
        this.code = code;
        this.body = body;
        this.header = header;
    }

    public Request request() {
        return request;
    }


    public int code() {
        return code;
    }

    public String body() {
        return body;
    }

    public HashMap<String, String> header() {
        return header;
    }

    @Override
    public String toString() {
        return "Response{" +
                "request=" + request +
                ", code=" + code +
                ", body='" + body + '\'' +
                ", header=" + header +
                '}';
    }
}
