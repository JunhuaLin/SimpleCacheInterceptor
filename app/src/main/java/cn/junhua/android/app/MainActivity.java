package cn.junhua.android.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import cn.junhua.android.app.Net.DataBean;
import cn.junhua.android.app.Net.Net;
import cn.junhua.android.app.Net.RetrofitBase;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends Activity {
    TextView textview;
    Button button;
    RetrofitBase retrofitBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textview = findViewById(R.id.textview);
        button = findViewById(R.id.button);

        retrofitBase = new RetrofitBase(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSomething();
            }
        });
    }

    private void doSomething() {
        test();
        Net net = retrofitBase.getRetrofit().create(Net.class);
        Call<DataBean> call = net.getIndex("苏州市");
        call.enqueue(new Callback<DataBean>() {
            @Override
            public void onResponse(Call<DataBean> call, Response<DataBean> response) {
                DataBean data = response.body();
                Date date = new Date();
                textview.setText(date.getMinutes() + " " + date.getSeconds() + ":\n" + data + "");
            }

            @Override
            public void onFailure(Call<DataBean> call, Throwable t) {
                textview.setText("请求失败！");
            }
        });
    }

    public void test() {
        try {
            JSONObject root = new JSONObject();

            root.put("q", "123");
            JSONArray array = new JSONArray();
            array.put("1");
            array.put("2");
            array.put("3");

            root.put("array", array);

            System.out.println(root.toString(4));
            array = root.getJSONArray("array");
            array.put("4");
            array.put("5");
            array.put("6");

            System.out.println(root.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
