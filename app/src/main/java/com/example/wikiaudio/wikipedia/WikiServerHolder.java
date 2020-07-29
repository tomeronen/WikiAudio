package com.example.wikiaudio.wikipedia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WikiServerHolder {
    private static final String BASE_URL = "https://en.wikipedia.org/w/api.php";
    private static WikiServerHolder instance = null;
    private final WikiServer server;

    synchronized static WikiServerHolder getInstance(){
        if (instance == null) {
            instance = new WikiServerHolder();
        }
        return instance;
    }

    private WikiServerHolder(){
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        this.server = new Retrofit.Builder()
                .client(new OkHttpClient())
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(WikiServer.class);
    }

    public Call<Object> getPagesNearby(String latitude , String longitude)
    {
        return this.server.callGetPagesNearby(latitude + "|" + longitude);
    }

}
