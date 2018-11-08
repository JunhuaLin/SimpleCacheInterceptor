package cn.junhua.android.interceptor;

import android.support.annotation.Nullable;

import cn.junhua.android.interceptor.header.CacheKey;

/**
 * 处理缓存的过滤器
 * Created by junhua.lin on 2018/3/22.
 */
public interface SimpleFilter {
    /**
     * 在存储缓存之前调用
     *
     * @param cacheKey 缓存key封装类
     * @param newData  当前请求的数据
     * @param oldData  之前缓存的数据
     * @return 过滤后的结果作为存储值
     */
    @Nullable
    String filter(CacheKey cacheKey, @Nullable String newData, @Nullable String oldData);
}
