# SimpleCacheInterceptor 拦截器

缓存拦截器用于缓存http请求结果。适用于GET、POST等常见请求方法的缓存。该拦截器通过识别特殊指定的请求头来判断本次请求是否需要缓存。

### 缓存策略

缓存策略由CacheMode管理，其中包括拼接好的字符串形式的请求头和方法返回CacheHeader对象两种。

缓存策略的缓存请求头格式：CacheMode.Value.CACHE_MODE_KEY + ":" + CacheMode.Value.NO_CACHE

1. NO_CACHE             不使用缓存，直接请求网络
2. NORMAL               启用缓存，先请求网络再缓存结果，无网使用缓存
3. ONLY_IF_CACHED       有缓存就优先使用缓存，没有缓存再请求网络
3. CACHED_AFTER_REQUEST 优使用缓存，然后再请求网络缓存走normal缓存模式


### 缓存key策略

1. 缓存key请求头的value部分为空     使用url路径之前部分作为缓存文件名
2. 缓存key请求头的value部分不为空   使用url路径之前部分和指定key作为缓存文件名
3. 不含缓存key请求头                使用整个url作为缓存文件名

可以通过CacheHeader.Builder来生成该缓存key策略请求头。

CacheHeader还支持设置黑白名单来过滤或者添加某些通用的缓存key。

注：缓存key策略请求头格式：CacheHeader.CACHE_KEY + ":" + "page,pageSize"

### SimpleCache储存器接口
每一个缓存SimpleCacheInterceptor实例化都需要一个具体缓存器来存储数据。
该内部提供了默认的文件缓存器：FileSimpleCache
缓存大小为10MB，暂时不支持缓存过期和调整缓存大小（以后更新会支持）。

你可以自己实现各种缓存器，如数据库缓存器，内存缓存等，只需要实现SimpleCache接口即可。

自定义数据库缓存器
```java
public class DbSimpleCache implements SimpleCache {
    @Override
    public void put(String key, String value) {
        // todo
    }

    @Override
    public String get(String key) {
        // todo
        return null;
    }
}
```

### 使用添加拦截器

#### 添加SimpleCacheInterceptor
```java
OkHttpClient client = new OkHttpClient.Builder()
                //添加缓存拦截器，默认使用FileSimpleCache
                .addInterceptor(new SimpleCacheInterceptor(context))
                //...
                .build();
```

#### 指定某个请求使用缓存功能

Retrofit使用NORMAL缓存策略，缓存Key为整个url
```java
@Headers({CacheMode.NORMAL, CacheHeader.CACHE_KEY + ":"})
@GET("fortune/common/config/upgrade")
Observable<Upgrade> upgrade();
```

