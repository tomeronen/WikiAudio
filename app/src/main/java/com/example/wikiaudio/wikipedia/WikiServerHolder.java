package com.example.wikiaudio.wikipedia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.wikiaudio.wikipedia.PageAttributes.*;

public class WikiServerHolder {
    static final HashMap<PageAttributes, String> attributesStringMap = new HashMap<>();
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

    public WikiPage getPage(String name, List<PageAttributes> pageAttributes)
            throws IOException
    {
        WikiPage wikiPage = new WikiPage();
        String prop = getQueryProp(pageAttributes);
        Call<QuarryResponse> quarryResponseCall = server.callGetPageByName(name);

        return wikiPage;
    }



    List<WikiPage> getPagesByCategory(String categoryName, List<PageAttributes> pageAttr)
            throws IOException
    {
        String prop = getQueryProp(pageAttr);
        String inprop = getQueryInProp(pageAttr);
        Response<QuarryResponse> response = getInstance().server.
                callGetPagesByCategory(categoryName,
                        prop,
                        inprop).execute();
        if (response.code() == 200 && response.isSuccessful()) {
            // task was successful.
            return parseQuarryResponse(response.body());

        } else {
            // task failed.
            throw new IOException();
        }
    }

    static List<WikiPage> getPagesNearby(Double latitude,
                                         Double longitude,
                                         int radius,
                                         List<PageAttributes> pageAttr)
            throws IOException
    {
        // todo add implantation for content audioUrl and identifiers.
        String prop = getQueryProp(pageAttr);
        String inprop = getQueryInProp(pageAttr);
        Response<QuarryResponse> response = getInstance().server.
                callGetPagesNearby(latitude + "|" + longitude,
                                    Integer.toString(radius),
                                    prop,
                                    inprop).execute();
        if (response.code() == 200 && response.isSuccessful()) {
            // task was successful.
            List<WikiPage> wikiPageList = parseQuarryResponse(response.body());
            if(pageAttr.contains(content))
            {
                for(WikiPage wikiPage: wikiPageList)
                {
                    wikiPage.setSections(WikiHtmlParser.parseContent(wikiPage.getUrl()));
                }
            }
            return wikiPageList;
        } else {
            // task failed.
            throw new IOException();
        }
    }

    private static List<WikiPage> parseQuarryResponse(QuarryResponse quarryResponse) {
        if (quarryResponse != null
                && quarryResponse.query != null
                && quarryResponse.query.pages != null) {
            List<WikiPage> resultList = new ArrayList<>();
            for(QuarryResponse.PageData pageData:
                    quarryResponse.query.pages.values())
            {
                resultList.add(parseWikiData(pageData));
            }
            return resultList;
        }
        else
        {
            return null;
        }
    }


    Call<Object> callGetSpokenPagesCategories()
    {
        return this.server.callGetSpokenPagesCategories();
    }

    List<WikiPage> callGetSpokenPagesByCategories(String category, List<PageAttributes> pageAttr)
    {
        String prop = getQueryProp(pageAttr);
        String inprop = getQueryInProp(pageAttr);
        if(Wikipedia.getInstance().spokenPagesCategories == null)
        {
            // TODO - implement what if categorise were not loaded yet. (load categorise and continue)
            return null;
        }
        else
        {
            String CategoryIndex =
                    Integer.toString(Wikipedia.getInstance().spokenPagesCategories.indexOf(category) + 1 );
            Call<QuarryResponse> quarryResponseCall
                    = this.server.callGetSpokenPagesByCategory(CategoryIndex);
        }
        return null;
    }

    //todo implement.
//    Call<Object> callLogin(String userName, String password)
//    {
//            this.server.getToken()
//                    .enqueue(new Callback<Object>() {
//                @Override
//                public void onResponse(Call<Object> call, Response<Object> response) {
//                        LinkedTreeMap<String, // query->LinkedTreeMap;
//                                LinkedTreeMap<String, // tokens->LinkedTreeMap
//                                        LinkedTreeMap<String, String>>> // logintoken->token
//                          res = (LinkedTreeMap<String, LinkedTreeMap<String, LinkedTreeMap<String, String>>>) response.body();
//                        String token = res.get("query").get("tokens").get("logintoken");
//                        server.login("logintoken="+token).enqueue(new Callback<Object>() {
//                            @Override
//                            public void onResponse(Call<Object> call, Response<Object> response) {
//                                String c = response.body().toString();
//                            }
//
//                            @Override
//                            public void onFailure(Call<Object> call, Throwable t) {
//                                String c = t.getMessage();
//                            }
//                        });
//                }
//
//                @Override
//                public void onFailure(Call<Object> call, Throwable t) {
//                    String s = t.getLocalizedMessage();
//                }
//            });
////            this.server.login().execute();
//        return null;
//    }

    private static WikiPage parseWikiData(QuarryResponse.PageData pageData) {
        // todo complete.
        WikiPage curWikiPage = new WikiPage();
        curWikiPage.setTitle(pageData.title);
        curWikiPage.setUrl(pageData.fullurl);
        curWikiPage.setDescription(pageData.description);
        curWikiPage.setWatchers(pageData.watchers);
        List<QuarryResponse.CoordinatesData> coordinates = pageData.coordinates;
        if(coordinates.size() > 0)
        {
            curWikiPage.setLat(pageData.coordinates.get(0).lat);
            curWikiPage.setLon(pageData.coordinates.get(0).lon);
        }
        QuarryResponse.ThumbnailData thumbnail = pageData.thumbnail;
        if(thumbnail != null)
        {
           curWikiPage.setThumbnailSrc(thumbnail.source);
        }
        return curWikiPage;
    }


    private static String getQueryProp(List<PageAttributes> pageAttributes) {
        Set<String> props = new HashSet<>();
        StringBuilder prop = new StringBuilder();

        if(pageAttributes.contains(title)) props.add("title");
        if(pageAttributes.contains(url))   props.add("info");
        if(pageAttributes.contains(content)) props.add("info");
        if(pageAttributes.contains(watchers))   props.add("info");
        if(pageAttributes.contains(description))  props.add("description");
        if(pageAttributes.contains(categories)) props.add("categories");
        if(pageAttributes.contains(thumbnail))   props.add("pageimages");
        if(pageAttributes.contains(coordinates)) props.add("coordinates");
        boolean first = true;
        for(String p: props)
        {
            if(first)
            {
                prop.append(p);
                first = false;
            }
            else
            {
                prop.append("|").append(p);
            }

        }
        return prop.toString();
    }

    private static String getQueryInProp(List<PageAttributes> pageAttr) {
        StringBuilder inprop = new StringBuilder();
        Set<String> inprops = new HashSet<>();

        if(pageAttr.contains(url))   inprops.add("url");
        if(pageAttr.contains(content)) inprops.add("url");
        if(pageAttr.contains(watchers))   inprops.add("watchers");

        boolean first = true;
        for(String p: inprops)
        {
            if(first)
            {
                inprop.append(p);
                first = false;
            }
            else
            {
                inprop.append("|").append(p);
            }

        }
        return inprop.toString();
    }
}
