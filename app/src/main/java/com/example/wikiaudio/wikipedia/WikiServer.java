package com.example.wikiaudio.wikipedia;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WikiServer {

    @GET("/w/api.php?action=query&" +
            "prop=coordinates|pageimages|description|info&" +
            "inprop=url&" +
            "pithumbsize=144&" +
            "generator=geosearch&" +
            "ggsradius=10000&" +
            "ggslimit=10&" +
            "format=json")
    public Call<Object> callGetPagesNearby(@Query("ggscoord") String geoCoordinates);


}
