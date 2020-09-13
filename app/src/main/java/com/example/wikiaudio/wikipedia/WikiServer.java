package com.example.wikiaudio.wikipedia;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

//  important wiki documentation:
//    (1)  main decimation - https://www.mediawiki.org/wiki/API:Main_page
//    (2)  query decimation - https://www.mediawiki.org/w/api.php?action=help&modules=query
//    (3) REST api - https://en.wikipedia.org/api/rest_v1/#/

public interface WikiServer {

    @GET("/w/api.php?" +
            "action=query&" + // get data from wikipedia
            "format=json") // format for queried pages (json recommended)
    public Call<QuarryResponse> callGetPageByName(@Query("titles") String pageName,
                                                  @Query("prop") String prop,
                                                  @Query("inprop") String inprop);

// Example full get request:
//https://en.wikipedia.org/w/api.php?action=query&prop=coordinates|pageimages|description|info&inprop=url|watchers&pithumbsize=144&generator=geosearch&ggsradius=10000&ggslimit=10&format=json&ggscoord=32.443814|34.892546
    @GET("/w/api.php?" +
            "action=query&" + // get data from wikipedia
            "prop=coordinates|info|description|extracts|pageviews&" + // properties to get for queried pages
            "generator=geosearch&" +
            "ggslimit=10&" + // max number of pages to query (50 max)
            "inprop=url&" +
            "format=json") // format for queried pages (json recommended)
    public Call<QuarryResponse> callGetPagesNearby(@Query("ggscoord") String geoCoordinates,
                                                   @Query("ggsradius") String radius,
                                                   @Query("prop") String prop,
                                                   @Query("inprop") String inprop);


    @GET("/w/api.php?" +
            "action=query&" +
            "generator=categorymembers&" +
            "gcmnamespace=0&"  // only articles (code name space '0')
            )
    public Call<QuarryResponse> callGetPagesByCategory(@Query("gcmtitle") String category,
                                               @Query("prop") String prop,
                                               @Query("inprop") String inprop);

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

@POST("/w/api.php?action=login" +
        "&lgname=tomer_ronen" +
        "&lgpassword=WikiAudio@tkpemajv20jm4t1ofm2amr5mb7p1v9cv" +
        "&format=json")
    public Call<Object> login(@Query("lgtoken") String token);


    @GET("/w/api.php?action=query" +
            "&meta=tokens" +
            "&format=json" +
            "&type=login")
        public Call<Object> getToken();


    @GET("/w/api.php?action=query" +
            "&list=search" +
            "&format=json")
    Call<QuarryResponse> searchPage(@Query("srsearch") String pageName);


    @POST("/w/api.php?" +
            "action=upload" +
            "&format=json" +
            "&ignorewarnings=1?")
    Call<Object> uploadFile(@Query("filename") String fileName,
                            @Query("file") String filePath,
                            @Query("token") String token);
}
