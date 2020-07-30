package com.example.wikiaudio.wikipedia;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

//  important wiki documentation:
//    (1)  main decimation - https://www.mediawiki.org/wiki/API:Main_page
//    (2)  query decimation - https://www.mediawiki.org/w/api.php?action=help&modules=query


public interface WikiServer {

// Example full get request:
//https://en.wikipedia.org/w/api.php?action=query&prop=coordinates|pageimages|description|info&inprop=url&pithumbsize=144&generator=geosearch&ggsradius=10000&ggslimit=10&format=json&ggscoord=32.443814|34.892546
    @GET("/w/api.php?" +
            "action=query&" + // get data from wikipedia
            "prop=coordinates|pageimages|description|info&" + // properties to get for queried pages
            "inprop=url&" +
            "pithumbsize=144&" +
            "generator=geosearch&" +
            "ggsradius=10000&" + // the radius from Coordinates to query pages in.
            "ggslimit=10&" + // max number of pages to query (50 max)
            "format=json") // format for queried pages (json recommended)
    public Call<Object> callGetPagesNearby(@Query("ggscoord") String geoCoordinates);


// Example full get request:
//https://en.wikipedia.org/w/api.php?action=query&prop=coordinates|pageimages|description|info&inprop=url&pithumbsize=144&generator=geosearch&ggsradius=10000&ggslimit=10&format=json&ggscoord=32.443814|34.892546
    @GET("/w/api.php?" +
            "action=query&" +
            "generator=categorymembers&" +
            "&prop=info")
    public Call<Object> callGetPagesByCategory(@Query("gcmtitle") String category);
}
