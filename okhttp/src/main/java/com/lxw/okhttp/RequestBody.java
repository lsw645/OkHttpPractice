package com.lxw.okhttp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
public class RequestBody {
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String CHARSET = "UTF-8";
    private Map<String, String> body = new HashMap<>();

    public static String getContentType() {
        return CONTENT_TYPE;
    }

    public int contentLength() {
        return body().getBytes().length;
    }

    public String body() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : body.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }

        int index = sb.lastIndexOf("&");
        if (index != -1) {
            sb.deleteCharAt(index);
        }
        return sb.toString();
    }

    public RequestBody addParams(String key, String value) {
        try {
            body.put(URLEncoder.encode(key, CHARSET), URLEncoder.encode(value, CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this;
    }

}
