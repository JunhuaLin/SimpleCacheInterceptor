package cn.junhua.android.app.Net;


import cn.junhua.android.interceptor.header.CacheMode;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface Net {
    @Headers(CacheMode.NORMAL)
    @FormUrlEncoded
    @POST("geocoding")
    Call<DataBean> getIndex(@Field("a") String a);
}
