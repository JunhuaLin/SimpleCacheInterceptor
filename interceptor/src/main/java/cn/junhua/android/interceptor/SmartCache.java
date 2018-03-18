package cn.junhua.android.interceptor;

/**
 * 缓存接口
 * Created by junhua on 2018/3/18.
 */
public interface SmartCache {
    void put(String key, String value);

    String get(String key);

}
