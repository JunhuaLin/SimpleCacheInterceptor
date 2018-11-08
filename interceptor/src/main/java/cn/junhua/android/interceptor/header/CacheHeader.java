package cn.junhua.android.interceptor.header;


import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import okhttp3.Request;

/**
 * 所有请求的缓存头
 * Created by junhua on 2017/12/9.
 */
public class CacheHeader {

    //POST可选参数的请求头
    static final String CACHE_KEY = "simple-cache-key";

    /**
     * 黑名单优先级低于白名单
     */
    private static Set<String> BLACK_KEY_SET;
    /**
     * 白名单优先级高于黑名单
     */
    private static Set<String> WHITE_KEY_SET;
    private String key;
    private String value;

    CacheHeader(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * 黑名单<br/>
     * 黑名单优先级低于白名单
     *
     * @param blackKeys 需要过滤的key
     */
    public static void setBlackKeys(String... blackKeys) {
        if (blackKeys == null) return;
        CacheHeader.BLACK_KEY_SET = new TreeSet<>();
        Collections.addAll(CacheHeader.BLACK_KEY_SET, blackKeys);
    }

    /**
     * 白名单<br/>
     * 白名单优先级高于黑名单
     *
     * @param whiteKeys 通用的key
     */
    public static void setWhiteKeys(String... whiteKeys) {
        if (whiteKeys == null) return;
        CacheHeader.WHITE_KEY_SET = new TreeSet<>();
        Collections.addAll(CacheHeader.WHITE_KEY_SET, whiteKeys);
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
        private CacheHeader cacheHeader;

        private Set<String> keySet;

        public Builder() {
            cacheHeader = new CacheHeader(CACHE_KEY, "");
            keySet = new TreeSet<>();
        }

        /**
         * 设置缓存key
         * 注意：<br/>
         * 1.传单个空字符串时默认不使用缓存key仅用url的path和path之前的部分作为缓存key<br/>
         * 2.当不使用该请求头并且使用缓存模式时默认使用整个url最为缓存key<br/>
         *
         * @param keys 可变key列表
         * @return Builder
         */
        public Builder setValue(String... keys) {
            keySet.clear();
            if (keys != null) {
                Collections.addAll(keySet, keys);
            }
            return this;
        }

        public Builder addValue(String... keys) {
            if (keys != null) {
                Collections.addAll(keySet, keys);
            }
            return this;
        }

        public CacheHeader build() {
            StringBuilder sb = new StringBuilder();
            //处理黑名单
            if (CacheHeader.BLACK_KEY_SET != null) {
                keySet.removeAll(CacheHeader.BLACK_KEY_SET);
            }
            //处理白名单
            if (CacheHeader.WHITE_KEY_SET != null) {
                keySet.addAll(CacheHeader.WHITE_KEY_SET);
            }

            //生成key请求头
            for (String key : keySet) {
                sb.append(key).append(",");
            }
            sb.delete(sb.length() - 1, sb.length());
            cacheHeader.value = sb.toString();
            sb.delete(0, sb.length());

            return cacheHeader;
        }
    }
}
