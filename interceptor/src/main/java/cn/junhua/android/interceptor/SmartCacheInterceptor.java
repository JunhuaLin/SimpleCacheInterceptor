package cn.junhua.android.interceptor;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.junhua.android.interceptor.log.Log;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 字符串的缓存类
 * Created by junhua on 2017/12/9.
 */
public class SmartCacheInterceptor implements Interceptor {
    private SmartCache smartCache;

    public SmartCacheInterceptor(SmartCache smartCache) {
        if (smartCache == null) {
            throw new IllegalArgumentException("SmartCacheInterceptor need a SmartCache's object");
        }
        this.smartCache = smartCache;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String cacheHead = request.header(CacheHeaders.CACHE);
        long oldNow = System.currentTimeMillis();

        //有缓存直接使用缓存
        if (CacheHeaders.ONLY_IF_CACHED.getValue().equalsIgnoreCase(cacheHead)) {
            Response cacheResponse = getCacheResponse(request, CacheHeaders.Code.ONLY_IF_CACHED, oldNow);
            if (cacheResponse != null) {
                return cacheResponse;
            }
        }

        //缓存网络结果
        if (!"".equalsIgnoreCase(cacheHead)) {
            String url = request.url().url().toString();
            String responseStr = null;
            String cacheKey = genCacheKey(request);
            try {
                Response response = chain.proceed(request);
                // 只有在网络请求返回成功之后，才进行缓存处理，否则，404存进缓存，岂不笑话
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        responseStr = responseBody.string();
                        if (responseStr == null) {
                            responseStr = "";
                        }
                        //存缓存，以链接+参数进行MD5编码为KEY存
                        smartCache.put(cacheKey, responseStr);
                        Log.i("HttpRetrofit", "--> Push Cache:" + url + " :Success");
                    }
                    return getOnlineResponse(response, responseStr);
                } else {
                    return chain.proceed(request);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Response response = getCacheResponse(request, CacheHeaders.Code.ERROR_CACHE, oldNow); // 发生异常了，我这里就开始去缓存，但是有可能没有缓存，那么久需要丢给下一轮处理了
                if (response == null) {
                    return chain.proceed(request);//丢给下一轮处理
                } else {
                    return response;
                }
            }
        }

        //默认处理情况
        return chain.proceed(request);
    }

    private String[] getUserCacheKeys(Request request) {
        String cacheKey = request.header(CacheHeaders.CACHE_KEY);
        if (cacheKey == null || "".equals(cacheKey.trim())) {
            return null;
        }
        return cacheKey.split(",");
    }

    private Response getCacheResponse(Request request, CacheHeaders.Code code, long oldNow) {
        Log.i("HttpRetrofit", "--> Try to Get Cache   --------");
        String url = request.url().url().toString();
        String cacheKey = genCacheKey(request);
        String cacheStr = smartCache.get(cacheKey);//取缓存
        if (cacheStr == null) {
            Log.i("HttpRetrofit", "<-- Get Cache Failure ---------");
            return null;
        }
        Response response = new Response.Builder()
                .code(code.getCode())
                .body(ResponseBody.create(null, cacheStr))
                .request(request)
                .message(code.getMessage())
                .protocol(Protocol.HTTP_1_0)
                .build();
        long useTime = System.currentTimeMillis() - oldNow;
        Log.i("HttpRetrofit", "<-- Get Cache: " + response.code() + " " + response.message() + " " + url + " (" + useTime + "ms)");
        Log.i("HttpRetrofit", cacheStr + "");
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

    /**
     * 生成缓存key
     *
     * @param request Request
     * @return 原始key
     */
    private String genCacheKey(Request request) {
        String method = request.method();
        String url = request.url().url().toString();
        // 非post直接使用url作为key
        if (!"POST".equals(method)) return url;

        // 处理POST情况
        String cacheKey = "";
        String[] cacheKeys = getUserCacheKeys(request);//调用者指定key
        StringBuilder sb = new StringBuilder(url);
        if (request.body() instanceof FormBody) {
            FormBody body = (FormBody) request.body();
            if (cacheKeys == null || cacheKeys.length <= 0) {
                if (body != null) {
                    for (int i = 0; i < body.size(); i++) {
                        sb.append(body.encodedName(i)).append("=").append(body.encodedValue(i)).append(",");
                    }
                    sb.delete(sb.length() - 1, sb.length());
                }
            } else {
                if (body != null) {
                    Map<String, String> kvMap = new HashMap<>(body.size());
                    for (int i = 0; i < body.size(); i++) {
                        kvMap.put(body.encodedName(i), body.encodedValue(i));
                    }
                    for (String key : cacheKeys) {
                        String value = kvMap.get(key);
                        sb.append(key).append("=")
                                .append(value == null ? "" : value)
                                .append(",");
                    }
                    sb.delete(sb.length() - 1, sb.length());
                } else {
                    for (String key : cacheKeys) {
                        sb.append(key).append("=")
                                .append(" ").append(",");
                    }
                    sb.delete(sb.length() - 1, sb.length());
                }
            }
            cacheKey = sb.toString();
            sb.delete(0, sb.length());
        }
        return cacheKey;
    }

}
