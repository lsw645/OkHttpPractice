package com.lxw.okhttppractice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.lxw.okhttp.Callback;
import com.lxw.okhttp.OkHttpClient;
import com.lxw.okhttp.RealCall;
import com.lxw.okhttp.Request;
import com.lxw.okhttp.RequestBody;
import com.lxw.okhttp.Response;

public class MainActivity extends AppCompatActivity {
    OkHttpClient okHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        okHttpClient = new OkHttpClient.Builder().build();

    }

    public void get(View view) {
        String baidu = "https://www.baidu.com:443";
//        String url = "http://www.szhrss.gov.cn:80";
        String url = "http://www.kuaidi100.com/query?type=yuantong&postid=222222222";
        Request request = new Request.Builder().get().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(RealCall call, Exception error) {
                System.out.println(error.getMessage());
            }

            @Override
            public void onSuccess(RealCall call, Response reponse) {
                System.out.println(reponse.body());
            }
        });
    }

    public void post(View view) {
        RequestBody body = new RequestBody()
                .addParams("city", "长沙")
                .addParams("key", "13cb58f5884f9749287abbead9c658f2");
        Request request = new Request.Builder().url("http://restapi.amap" +
                ".com/v3/weather/weatherInfo").post(body).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(RealCall call, Exception throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onSuccess(RealCall call, Response response) {
                Log.e("响应体", response.body());
            }
        });
    }
}
