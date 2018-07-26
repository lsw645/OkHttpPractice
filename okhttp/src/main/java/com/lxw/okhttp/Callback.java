package com.lxw.okhttp;

/**
 * <pre>
 *     author : lxw
 *     e-mail : lsw@tairunmh.com
 *     time   : 2018/07/22
 *     desc   :
 * </pre>
 */
public interface Callback {
    void onFailure(RealCall call, Exception error);

    void onSuccess(RealCall call, Response reponse);
}
