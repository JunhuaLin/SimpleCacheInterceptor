package cn.junhua.android.interceptor;


import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;

import cn.junhua.android.interceptor.cache.FileSimpleCache;
import cn.junhua.android.interceptor.header.CacheHeader;
import cn.junhua.android.interceptor.header.CacheKey;
import cn.junhua.android.interceptor.header.CacheMode;
import cn.junhua.android.interceptor.log.Log;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 缓存类:缓存请求结果
 * Created by junhua on 2017/12/9.
 */
public class SimpleCacheInterceptor implements Interceptor {
    private final String TAG = SimpleCacheInterceptor.class.getCanonicalName();

    private SimpleCache simpleCache;
    private SimpleFilter simpleFilter;
    private OkHttpClient mOkHttpClient = null;

    public SimpleCacheInterceptor(Context context) {
        this.simpleCache = new FileSimpleCache(context);
    }


    public SimpleCacheInterceptor(SimpleCache simpleCache) {
        if (simpleCache == null) {
            throw new IllegalArgumentException("SimpleCacheInterceptor need a SimpleCache's object");
        }
        this.simpleCache = simpleCache;
    }

    /**
     * 用于先缓存后网络的请求发出
     *
     * @param okHttpClient OkHttpClient
     */
    public void setOkHttpClient(OkHttpClient okHttpClient) {
        mOkHttpClient = okHttpClient;
    }

    public void setFilter(SimpleFilter simpleFilter) {
        this.simpleFilter = simpleFilter;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        String cacheHead = request.header(CacheMode.Value.CACHE_MODE_KEY);
        cacheHead = cacheHead == null ? "" : cacheHead;
        //①不走缓存 直接跳过后面的逻辑
        if (CacheMode.Value.NO_CACHE.equals(cacheHead)) {
            return chain.proceed(request);
        }

        Log.i(TAG, "-----> start  SimpleCacheInterceptor");
        long oldNow = System.currentTimeMillis();
        CacheKey cacheKey = CacheKey.parse(request);
        Response response;
        switch (cacheHead) {
            case CacheMode.Value.ONLY_IF_CACHED:
                //有缓存直接使用缓存 没有请求网络后再缓存
                response = getCacheResponse(cacheKey, request, CacheCode.ONLY_IF_CACHED);
                if (response == null) {
                    response = doNormalRequest(chain, request, cacheKey);
                }
                break;

            case CacheMode.Value.NORMAL:
                //④处理正常缓存情况-->请求网络，缓存，返回结果
                response = doNormalRequest(chain, request, cacheKey);
                break;

            case CacheMode.Value.CACHED_AFTER_REQUEST:
                //③反向处理缓存情况-->有缓存直接返回缓存，返回结果，再次发起请求网络走④的情况
                response = getCacheResponse(cacheKey, request, CacheCode.ONLY_IF_CACHED);
                if (response == null) {
                    response = doNormalRequest(chain, request, cacheKey);
                } else {
                    //再次发出该请求并且走 CacheHeader.Value.normal
                    doRequestAgain(request);
                }
                break;

            default:
                //默认 CacheHeader.Value.normal
                response = doNormalRequest(chain, request, cacheKey);
                break;
        }
        long useTime = System.currentTimeMillis() - oldNow;
        Log.i(TAG, "<----- end  SimpleCacheInterceptor  cache:" + cacheHead + "   url: " + cacheKey.getKey() + "   useTime(" + useTime + "ms)");

        return response;
    }

    /**
     * 重新发出该请求并且走 CacheMode.normal
     *
     * @param request 原有请求
     */
    private void doRequestAgain(Request request) {
        CacheHeader cacheHeader = CacheMode.normal();
        request = request.newBuilder()
                .header(cacheHeader.getKey(), cacheHeader.getValue())
                .build();

        if (mOkHttpClient == null) {
            throw new IllegalStateException("CACHED_AFTER_REQUEST模式需要设置setOkHttpClient()！");
        }

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {

            }
        });
    }

    private Response doNormalRequest(Chain chain, Request request, CacheKey cacheKey) throws IOException {
        Log.i(TAG, "--> Request network   --------");
        String url = request.url().url().toString();
        String responseStr = null;
        try {
            Response response = chain.proceed(request);
            // 只有在网络请求返回成功之后，才进行缓存处理
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    responseStr = responseBody.string();
                    //处理过滤
                    if (simpleFilter != null) {
                        String oldData = simpleCache.get(cacheKey.getKey());
                        responseStr = simpleFilter.filter(cacheKey, responseStr, oldData);
                    }
                    //存缓存，以链接+参数进行MD5编码为KEY存
                    if (responseStr != null) {
                        simpleCache.put(cacheKey.getKey(), responseStr);
                        Log.i(TAG, "-----> Push Cache Success  key:" + cacheKey.getKey() + "  url:" + url + " <-----");
                    }
                }
                response = getOnlineResponse(response, responseStr);
                Log.i(TAG, "<-- request network Success  key:" + cacheKey.getKey() + "  url:" + url);
                return response;
            } else {
                return chain.proceed(request);
            }
        } catch (Exception e) {
            Log.i(TAG, "<-- request network Failure key:" + cacheKey.getKey() + "  url:" + url);
            e.printStackTrace();
            // 发生异常了，我这里就开始去缓存，但是有可能没有缓存，那么久需要丢给下一轮处理了
            Response response = getCacheResponse(cacheKey, request, CacheCode.ERROR_CACHE);
            if (response == null) {
                return chain.proceed(request);//丢给下一轮处理
            } else {
                return response;
            }
        }
    }

    private Response getCacheResponse(CacheKey cacheKey, Request request, CacheCode code) {
        Log.i(TAG, "--> Try to Get Cache   --------");
        String url = request.url().url().toString();
        String cacheStr = simpleCache.get(cacheKey.getKey());//取缓存
        if (cacheStr == null) {
            Log.i(TAG, "<-- Get Cache Failure  " + " cacheKey:" + cacheKey.getKey() + "    ---------");
            return null;
        }
        Response response = new Response.Builder()
                .code(code.getCode())
                .body(ResponseBody.create(null, cacheStr))
                .request(request)
                .message(code.getMessage())
                .protocol(Protocol.HTTP_1_0)
                .build();
        Log.i(TAG, "<-- Get Cache  Success  " + " cacheKey:" + cacheKey.getKey() + "   " + response.code() + " " + response.message() + " " + url);
        Log.i(TAG, cacheStr + "");
        return response;
    }

    private Response getOnlineResponse(Response response, String body) {
        ResponseBody responseBody = response.body();
        return new Response.Builder()
                .code(response.code())
                .body(ResponseBody.create(responseBody == null ? null : responseBody.contentType(), body))
                .request(response.request())
                .message(response.message())
                .protocol(response.protocol())
                .build();
    }

}
