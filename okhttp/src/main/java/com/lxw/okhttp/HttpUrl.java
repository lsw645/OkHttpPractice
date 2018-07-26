package com.lxw.okhttp;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * <pre>
 *     author : lxw
 *     e-mail : lsw@tairunmh.com
 *     time   : 2018/07/22
 *     desc   :
 * </pre>
 */
public class HttpUrl {
    private String file;
    private String scheme;
    private String host;
    private int port;

    public HttpUrl(String url) {
        try {
            URL httpUrl = new URL(url);
            file = httpUrl.getFile();
            file = (null == file || "".equals(file)) ? "/" : file;
            scheme = httpUrl.getProtocol();
            port = httpUrl.getPort();
            if (port == -1) {
                if (scheme.equalsIgnoreCase("http")) {
                    port = 80;
                } else if (scheme.equalsIgnoreCase("https")) {
                    port = 443;
                }
            }
            host = httpUrl.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String file() {
        return file;
    }

    public String scheme() {
        return scheme;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }
}
