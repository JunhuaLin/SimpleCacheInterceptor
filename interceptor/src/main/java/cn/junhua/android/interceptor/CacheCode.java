package cn.junhua.android.interceptor;

/**
 * @author junhua.lin<br />
 * CREATED 2018/11/8 13:58
 */
enum CacheCode {
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

    CacheCode(int code, String message) {
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
