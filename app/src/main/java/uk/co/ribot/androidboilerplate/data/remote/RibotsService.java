package uk.co.ribot.androidboilerplate.data.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import uk.co.ribot.androidboilerplate.data.model.Ribot;
import uk.co.ribot.androidboilerplate.data.model.WeatherResponse;
import uk.co.ribot.androidboilerplate.util.MyGsonTypeAdapterFactory;

public interface RibotsService {

    String ENDPOINT = "http://api.openweathermap.org/data/2.5/";
    String OPEN_WEATHER_API_KEY = "177826481b1383540f32efa36c57e69f";

    @GET("ribots")
    Observable<List<Ribot>> getRibots();

    @GET("weather?&units=metric&appid=" + OPEN_WEATHER_API_KEY)
    Observable<WeatherResponse> getWeather(@Query("id") int cityId);

    /******** Helper class that sets up a new services *******/
    class Creator {

        public static RibotsService newRibotsService() {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapterFactory(MyGsonTypeAdapterFactory.create())

                    .create();
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(interceptor);
            Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                    .baseUrl(RibotsService.ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create());
            Retrofit retrofit = retrofitBuilder.client(httpClient.build()).build();
            return retrofit.create(RibotsService.class);
        }
    }
}
