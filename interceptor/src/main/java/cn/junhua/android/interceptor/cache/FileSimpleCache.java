package cn.junhua.android.interceptor.cache;

import android.content.Context;

import cn.junhua.android.interceptor.SimpleCache;


/**
 * 拦截器的文件缓存实现
 * Created by junhua.lin on 2018/3/19.
 */
public class FileSimpleCache implements SimpleCache {

    private CacheManager cacheManager;

    public FileSimpleCache(Context context) {
        this.cacheManager = CacheManager.getInstance(context);
    }

    @Override
    public void put(String key, String value) {
        cacheManager.setCache(CacheManager.encryptMD5(key), value);
    }

    @Override
    public String get(String key) {
        return cacheManager.getCache(CacheManager.encryptMD5(key));
    }
}
