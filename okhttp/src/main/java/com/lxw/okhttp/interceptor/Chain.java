package com.lxw.okhttp.interceptor;

import com.lxw.okhttp.RealCall;
import com.lxw.okhttp.Request;
import com.lxw.okhttp.Response;

import java.io.IOException;

public interface Chain {
    Response proceed(Request request) throws IOException;

    Request request();

    RealCall call();

    int connectTimeOut();

    int readTimeOut();

    int writeTimeOut();
}