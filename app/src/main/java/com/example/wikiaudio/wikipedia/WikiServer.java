package com.example.wikiaudio.wikipedia;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WikiServer {

// Example full get request:
//https://en.wikipedia.org/w/api.php?action=query&prop=coordinates|pageimages|description|info&inprop=url&pithumbsize=144&generator=geosearch&ggsradius=10000&ggslimit=10&format=json&ggscoord=32.443814|34.892546
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
