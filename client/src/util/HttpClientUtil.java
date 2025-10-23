package util;


import coockie.SessionCookieJar;
import okhttp3.OkHttpClient;
public class HttpClientUtil {
    public static final OkHttpClient client = new OkHttpClient.Builder()
            .cookieJar(new SessionCookieJar())
            .build();

    static {
        System.out.println("HttpClient with SessionCookieJar initialized");
    }

    public static OkHttpClient getClient() {
        return client;
    }
}
