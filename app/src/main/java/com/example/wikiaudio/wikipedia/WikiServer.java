package com.example.wikiaudio.wikipedia;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

//  important wiki documentation:
//    (1)  main decimation - https://www.mediawiki.org/wiki/API:Main_page
//    (2)  query decimation - https://www.mediawiki.org/w/api.php?action=help&modules=query


public interface WikiServer {

// Example full get request:
//https://en.wikipedia.org/w/api.php?action=query&prop=coordinates|pageimages|description|info&inprop=url|watchers&pithumbsize=144&generator=geosearch&ggsradius=10000&ggslimit=10&format=json&ggscoord=32.443814|34.892546
    @GET("/w/api.php?" +
            "action=query&" + // get data from wikipedia
            "prop=coordinates|description|extracts&" + // properties to get for queried pages
            "inprop=url&" +
            "generator=geosearch&" +
            "ggsradius=10000&" + // the radius from Coordinates to query pages in.
            "ggslimit=10&" + // max number of pages to query (50 max)
            "format=json") // format for queried pages (json recommended)
    public Call<Object> callGetPagesNearby(@Query("ggscoord") String geoCoordinates);


    @GET("/w/api.php?" +
            "action=query&" +
            "inprop=url&" +
            "generator=categorymembers&" +
            "gcmnamespace=0&" + // only articles (code name space '0')
            "&prop=info")
    public Call<Object> callGetPagesByCategory(@Query("gcmtitle") String category);

    @GET("/w/api.php?" +
            "format=json" + // the format of response.
            "&action=parse" + // we want to parse the spoken articles page
            "&page=Wikipedia:Spoken_articles" + // the wanted page.
            "&prop=sections") // we want all the section.
    public Call<Object> callGetSpokenPagesCategories();


//   /w/api.php?action=parse&page=Wikipedia:Spoken_articles&format=json&prop=links

    @GET("/w/api.php?" +
            "action=parse" +
            "&page=Wikipedia:Spoken_articles" +
            "&format=json" +
            "&prop=links")
    public Call<Object> callGetSpokenPagesByCategory(@Query("section") String category);

//    @POST()
//    gets loginToken:
//    https://en.wikipedia.org/w/api.php?action=query&meta=tokens&format=json&type=login

@POST("/w/api.php?action=clientlogin" +
        "&username=tomer+ronen" +
        "&password=xTGHTibZAL3cBws" +
        "&loginreturnurl=www.google.com")
    public Call<Object> login(@Body String token);

@POST("/w/api.php?action=query" +
        "&meta=tokens" +
        "&format=json" +
        "&type=login")
    public Call<Object> getToken();

}
