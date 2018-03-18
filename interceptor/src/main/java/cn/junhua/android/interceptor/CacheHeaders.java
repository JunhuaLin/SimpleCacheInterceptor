package cn.junhua.android.interceptor;


import java.util.Arrays;

/**
 * 所有请求的缓存头
 * Created by junhua on 2017/12/9.
 */
public class CacheHeaders {

    public enum Code {
        /**
         * 强制使用缓存
         */
        ONLY_IF_CACHED(210, "only if cached"),
        /**
         * 网络异常使用缓存
         */
        ERROR_CACHE(211, "net error");


        private int code;
        private String message;

        Code(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

    }

    //缓存请求头的key
    static final String CACHE = "cache";
    //POST可选参数的请求头
    static final String CACHE_KEY = "cache-key";

    public static final String NORMAL_HEADER = CACHE + ":true";
    public static final String ONLY_IF_CACHED_HEADER = CACHE + ":only-if-cached";

    // 启用缓存
    public static final CacheHeaders NORMAL = new CacheHeaders(CACHE, "true");
    // 优先使用缓存
    public static final CacheHeaders ONLY_IF_CACHED = new CacheHeaders(CACHE, "only-if-cached");


    private String key;
    private String value;

    CacheHeaders(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String header() {
        return key + ":" + value;
    }

    public static class Builder {
        private CacheHeaders cacheHeaders;


        public Builder() {
            cacheHeaders = new CacheHeaders(CACHE_KEY, "");
        }

        public Builder setValue(String... keys) {
            Arrays.sort(keys);
            for (String key : keys) {
                cacheHeaders.value = key;
            }
            return this;
        }

        public CacheHeaders build() {
            return cacheHeaders;
        }
    }
}
