package cn.junhua.android.interceptor.header;

/**
 * 缓存方式：
 * <p>
 * 1. NO_CACHE             不使用缓存，直接请求网络<br/>
 * 2. NORMAL               启用缓存，先请求网络再缓存结果，无网使用缓存 <br/>
 * 3. ONLY_IF_CACHED       有缓存就优先使用缓存，没有缓存再请求网络 <br/>
 * 3. CACHED_AFTER_REQUEST 优使用缓存，然后再请求网络缓存走normal缓存模式 <br/>
 * </p>
 *
 * @author junhua.lin<br />
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
