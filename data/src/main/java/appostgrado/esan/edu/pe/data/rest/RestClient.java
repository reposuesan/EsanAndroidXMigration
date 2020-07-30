package appostgrado.esan.edu.pe.data.rest;

import appostgrado.esan.edu.pe.data.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestClient {
    private static final String API_BASE_URL =  BuildConfig.API_URL;
    private static Api servicesApiInterface;
    private static OkHttpClient.Builder httpClient;

    public static Api getMyApiClient() {
        if (servicesApiInterface == null) {
            Retrofit.Builder builder =new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());
            httpClient = new OkHttpClient.Builder();
            // httpClient.connectTimeout(30, TimeUnit.SECONDS);
            // httpClient.readTimeout(30, TimeUnit.SECONDS);
            if(BuildConfig.DEBUG)
                httpClient.addInterceptor(interceptor());

            Retrofit retrofit = builder.client(httpClient.build()).build();
            servicesApiInterface = retrofit.create(Api.class);
        }
        return servicesApiInterface;
    }

    private  static HttpLoggingInterceptor interceptor(){
        HttpLoggingInterceptor httpLoggingInterceptor= new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return httpLoggingInterceptor;
    }
}
