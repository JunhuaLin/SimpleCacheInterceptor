package cn.junhua.android.interceptor.header;

/**
 * 缓存方式
 *
 * @author junhua.lin@jinfuzi.com<br/>
 * CREATED 2018/11/8 11:22
 */
public class CacheMode {

    /**
     * 不启用缓存
     */
    public static final String NO_CACHE = Value.CACHE_MODE_KEY + ":" + Value.NO_CACHE;
    /**
     * 启用缓存
     */
    public static final String NORMAL = Value.CACHE_MODE_KEY + ":" + Value.NORMAL;
    /**
     * 优先使用缓存
     */
    public static final String ONLY_IF_CACHED = Value.CACHE_MODE_KEY + ":" + Value.ONLY_IF_CACHED;
    /**
     * 优使用缓存 然后请求网络缓存走normal请求头
     */
    public static final String CACHED_AFTER_REQUEST = Value.CACHE_MODE_KEY + ":" + Value.CACHED_AFTER_REQUEST;


    /**
     * 不启用缓存
     */
    public static CacheHeader noCache() {
        return new CacheHeader(Value.CACHE_MODE_KEY, Value.NO_CACHE);
    }

    /**
     * 启用缓存
     */
    public static CacheHeader normal() {
        return new CacheHeader(Value.CACHE_MODE_KEY, Value.NORMAL);
    }

    /**
     * 优先使用缓存
     */
    public static CacheHeader onlyIfCached() {
        return new CacheHeader(Value.CACHE_MODE_KEY, Value.ONLY_IF_CACHED);
    }

    /**
     * 优使用缓存 然后请求网络缓存走normal请求头
     */
    public static CacheHeader cachedAfterRequest() {
        return new CacheHeader(Value.CACHE_MODE_KEY, Value.CACHED_AFTER_REQUEST);
    }

    public interface Value {
        /**
         * 缓存请求头的key
         */
        String CACHE_MODE_KEY = "simple-cache";

        String NO_CACHE = "";// 不启用缓存
        String NORMAL = "normal";   // 启用缓存
        String ONLY_IF_CACHED = "only-if-cached";
        String CACHED_AFTER_REQUEST = "cached-after-request";
    }

}
