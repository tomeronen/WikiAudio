package com.example.wikiaudio.wikipedia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WikiServerHolder {
    private static final String BASE_URL = "https://en.wikipedia.org";
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

    Call<Object> getPagesNearby(String latitude, String longitude)
    {
        return this.server.callGetPagesNearby(latitude + "|" + longitude);
    }

    Call<Object> getPagesByCategory(String categoryName)
    {
        return this.server.callGetPagesByCategory("Category:" + categoryName);
    }

    Call<Object> callGetSpokenPagesCategories()
    {
        return this.server.callGetSpokenPagesCategories();
    }

    Call<Object> callGetSpokenPagesByCategories(String category)
    {
        if(Wikipedia.getInstance().spokenPagesCategories == null)
        {
            // TODO - implement what if categorise were not loaded yet. (load categorise and continue)
            return null;
        }
        else
        {
            String CategoryIndex = Integer.toString(Wikipedia.getInstance().spokenPagesCategories.indexOf(category) + 1 );
            return this.server.callGetSpokenPagesByCategory(CategoryIndex);
        }
    }

    Call<Object> callLogin(String userName, String password)
    {
            this.server.getToken()
                    .enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                        LinkedTreeMap<String, // query->LinkedTreeMap;
                                LinkedTreeMap<String, // tokens->LinkedTreeMap
                                        LinkedTreeMap<String, String>>> // logintoken->token
                          res = (LinkedTreeMap<String, LinkedTreeMap<String, LinkedTreeMap<String, String>>>) response.body();
                        String token = res.get("query").get("tokens").get("logintoken");
                        server.login("logintoken="+token).enqueue(new Callback<Object>() {
                            @Override
                            public void onResponse(Call<Object> call, Response<Object> response) {
                                String c = response.body().toString();
                            }

                            @Override
                            public void onFailure(Call<Object> call, Throwable t) {
                                String c = t.getMessage();
                            }
                        });
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    String s = t.getLocalizedMessage();
                }
            });
//            this.server.login().execute();
        return null;
    }


}
