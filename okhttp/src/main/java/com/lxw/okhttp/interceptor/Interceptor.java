package com.lxw.okhttp.interceptor;

import com.lxw.okhttp.Response;

import java.io.IOException;

/**
 * <pre>
 *     author : lxw
 *     e-mail : lsw@tairunmh.com
 *     time   : 2018/07/22
 *     desc   :
 * </pre>
 */
public interface Interceptor {

    Response intercep(Chain chain) throws IOException;


}
