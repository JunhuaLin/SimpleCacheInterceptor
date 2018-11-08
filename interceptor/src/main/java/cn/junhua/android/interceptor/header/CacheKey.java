package cn.junhua.android.interceptor.header;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * 缓存key处理
 * Created by junhua.lin on 2018/3/22.
 */
public class CacheKey {
    private String key;
    private Map<String, String> params = new TreeMap<>();
    private HttpUrl httpUrl;

    public static CacheKey parse(Request request) {
        return new CacheKey(request);
    }

    private CacheKey(Request request) {
        genCacheKey(request);
    }

    public String getKey() {
        return key;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public HttpUrl getHttpUrl() {
        return httpUrl;
    }

    /**
     * 获取指定的缓存key
     *
     * @param request Request
     * @return String[]
     */
    private String[] getUserCacheKeys(Request request) {
        String cacheKey = request.header(CacheHeader.CACHE_KEY);
        //使用全部url
        if (cacheKey == null) {
            return null;
        }
        //只是用url的参数前半部分
        if ("".equals(cacheKey.trim())) {
            return new String[0];
        }
        //使用指定参数
        return cacheKey.split(",");
    }

    /**
     * 生成缓存key
     *
     * @param request Request
     */
    private void genCacheKey(Request request) {
        String method = request.method();
        HttpUrl httpUrl = request.url();

        this.httpUrl = httpUrl;

        String urlStr = httpUrl.url().toString().split("\\?")[0];
        String[] cacheKeys = getUserCacheKeys(request);//调用者指定key

        switch (method) {
            case "GET":
                genGetCacheKey(httpUrl, urlStr, cacheKeys);
                break;
            case "POST":
                genPostCacheKey(request, urlStr, cacheKeys);
                break;
            default:
                this.key = httpUrl.url().toString();
                break;
        }
    }

    /**
     * 处理GET
     *
     * @param httpUrl   httpUrl
     * @param urlStr    String
     * @param cacheKeys String[]
     */
    private void genGetCacheKey(HttpUrl httpUrl, String urlStr, String[] cacheKeys) {
        //清除原有数据
        this.params.clear();
        //解析所有参数
        Map<String, String> paramsTemp = new HashMap<>();
        Set<String> parameterNames = httpUrl.queryParameterNames();
        for (String name : parameterNames) {
            paramsTemp.put(name, httpUrl.queryParameter(name));
        }
        this.params = paramsTemp;

        //未指定参数 没有CacheHeaders.CACHE_KEY 使用整个url
        if (cacheKeys == null) {
            this.key = httpUrl.url().toString();
            return;
        }
        //指定参数为空
        if (cacheKeys.length == 0) {
            this.key = urlStr;
            return;
        }
        //指定参数 且有CacheHeaders.CACHE_KEY 那么值使用url参数之前的部分
        StringBuilder sb = new StringBuilder(urlStr).append("?");
        String value;
        for (String key : cacheKeys) {
            value = paramsTemp.get(key);
            sb.append(key).append("=").append(value == null ? "" : value).append("&");
        }
        sb.delete(sb.length() - 1, sb.length());

        this.key = sb.toString();
    }

    /**
     * 处理POST
     *
     * @param request   Request
     * @param url       String
     * @param cacheKeys String[]
     */
    private void genPostCacheKey(Request request, String url, String[] cacheKeys) {
        StringBuilder partKey = new StringBuilder(url).append("?");
        StringBuilder fullKey = new StringBuilder(url).append("?");
        Map<String, String> paramsTemp = new HashMap<>();
        if (request.body() instanceof FormBody) {
            FormBody body = (FormBody) request.body();
            //解析全部参数
            if (body != null) {
                String key, value;
                for (int i = 0; i < body.size(); i++) {
                    key = body.encodedName(i);
                    value = body.encodedValue(i);
                    fullKey.append(key).append("=").append(value).append("&");
                    paramsTemp.put(key, value);
                }
                fullKey.delete(fullKey.length() - 1, fullKey.length());
            }
            //处理用户指定参数
            if (cacheKeys == null) {
                this.key = fullKey.toString();
            } else if (cacheKeys.length == 0) {
                this.key = url;
            } else {
                for (String key : cacheKeys) {
                    String value = paramsTemp.get(key);
                    partKey.append(key).append("=")
                            .append(value == null ? "" : value)
                            .append("&");
                }
                partKey.delete(partKey.length() - 1, partKey.length());
                this.key = partKey.toString();
            }
        } else {
            this.key = url;
        }
        //全部参数
        this.params = paramsTemp;
    }
}
