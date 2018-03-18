package cn.junhua.android.interceptor.Catch;

import android.content.Context;

import cn.junhua.android.interceptor.SmartCache;


public class FileSmartCache implements SmartCache {

    public CacheManager cacheManager;

    public FileSmartCache(Context context) {
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
