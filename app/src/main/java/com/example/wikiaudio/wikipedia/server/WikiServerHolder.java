package com.example.wikiaudio.wikipedia.server;

import android.util.Log;

import com.example.wikiaudio.wikipedia.wikipage.PageAttributes;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.wikiaudio.wikipedia.wikipage.PageAttributes.audioUrl;
import static com.example.wikiaudio.wikipedia.wikipage.PageAttributes.categories;
import static com.example.wikiaudio.wikipedia.wikipage.PageAttributes.content;
import static com.example.wikiaudio.wikipedia.wikipage.PageAttributes.coordinates;
import static com.example.wikiaudio.wikipedia.wikipage.PageAttributes.description;
import static com.example.wikiaudio.wikipedia.wikipage.PageAttributes.thumbnail;
import static com.example.wikiaudio.wikipedia.wikipage.PageAttributes.url;
import static com.example.wikiaudio.wikipedia.wikipage.PageAttributes.watchers;

public class WikiServerHolder {
    static final HashMap<PageAttributes, String> attributesStringMap = new HashMap<>();
    private static final String BASE_URL = "https://en.wikipedia.org";
    private static WikiServerHolder instance = null;
    private final WikiServer server;

    public synchronized static WikiServerHolder getInstance(){
        if (instance == null) {
            instance = new WikiServerHolder();
        }
        return instance;
    }

    private WikiServerHolder(){
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();


        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // init cookie manager (we need to have a cookie manager for maintaining login token to
        // upload files).
        CookieHandler cookieHandler = new CookieManager();

        OkHttpClient client = new OkHttpClient.Builder().addNetworkInterceptor(interceptor)
                .cookieJar(new JavaNetCookieJar(cookieHandler))
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        this.server = new Retrofit.Builder()
                .client(client)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(WikiServer.class);
    }

    /**
     * search for wikipages by name.
     * @param pageName the value to search.
     * @return list of possible wikipages.
     * @throws IOException if got a bad format response throws a IOException or something went wrong
     * with the communication with wikipedia servers altogether. (can also be our fault)
     */
    public List<Wikipage> searchPage(String pageName,  List<PageAttributes> pageAttr )
            throws IOException {
        String prop = getQueryProp(pageAttr);
        String inprop = getQueryInProp(pageAttr);
        Response<QuarryResponse> response = server.searchPage(pageName, prop, inprop).execute();
        if (response.code() == 200 && response.isSuccessful()) {
            // task was successful.
            List<Wikipage> WikipageList = parseSearchResponse(response.body());
            return WikipageList;
        } else {
            // task failed.
            throw new IOException();
        }
    }

    /**
     * parses a QuarryResponse to List of Wikipage.
     * @param searchResponse the Response to parse.
     * @return the list of wikipages which data is in the response.
     * @throws IOException if got a bad format response throws a IOException, something went wrong
     * with the communication with wikipedia servers. (can also be our fault)
     */
    private List<Wikipage> parseSearchResponse(QuarryResponse searchResponse)
            throws IOException {
        if (searchResponse != null
                && searchResponse.query != null
                && searchResponse.query.search != null) {
            List<Wikipage> resultList = new ArrayList<>();
            for(QuarryResponse.PageData pageData: searchResponse.query.search)
            {
                resultList.add(parseWikiData(pageData));
            }
            return resultList;
        }
        else
        {
            throw new IOException();
        }
    }

    public Wikipage getPage(String name, List<PageAttributes> pageAttr)
            throws IOException
    {
        String prop = getQueryProp(pageAttr);
        String inprop = getQueryInProp(pageAttr);
        Response<QuarryResponse> response = server.callGetPageByName(name, prop, inprop, "original|thumbnail").execute();
        if (response.code() == 200 && response.isSuccessful()) {
            // task was successful.
            List<Wikipage> WikipageList = parseQuarryResponse(response.body());
//            List<Wikipage> WikipageList = new ArrayList<>();
            if(pageAttr.contains(content) ||
                    pageAttr.contains(audioUrl) ||
                    pageAttr.contains(thumbnail))
            {
                WikiHtmlParser.parseAdvanceAttr(WikipageList.get(0));
            }
            return WikipageList.get(0);
        } else {
            // task failed.
            throw new IOException();
        }
    }



    List<Wikipage> getPagesByCategory(String categoryName, List<PageAttributes> pageAttr)
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

    public static List<Wikipage> getPagesNearby(Double latitude,
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
            List<Wikipage> WikipageList = parseQuarryResponse(response.body());
            if(pageAttr.contains(content) ||
                    pageAttr.contains(audioUrl) ||
                    pageAttr.contains(thumbnail))
            {
                for(Wikipage wikipage: WikipageList)
                {
                    WikiHtmlParser.parseAdvanceAttr(wikipage);
                }
            }
            return WikipageList;
        } else {
            // task failed.
            throw new IOException();
        }
    }

    private static List<Wikipage> parseQuarryResponse(QuarryResponse quarryResponse)
            throws IOException {
        if (quarryResponse != null
                && quarryResponse.query != null
                && quarryResponse.query.pages != null) {
            List<Wikipage> resultList = new ArrayList<>();
            for(QuarryResponse.PageData pageData:
                    quarryResponse.query.pages.values())
            {
                resultList.add(parseWikiData(pageData));
            }
            return resultList;
        }
        else
        {
            throw new IOException();
        }
    }


    public List<String> callGetSpokenPagesCategories() throws IOException {
        ArrayList<String> allCategories = new ArrayList<>();
        Response<Object> response = this.server.callGetSpokenPagesCategories().execute();
        if (response.code() != 200 || !response.isSuccessful()) {
            throw new IOException();
        }
        else
        {
            LinkedTreeMap<String, LinkedTreeMap<String, Object>> b =
                    (LinkedTreeMap<String, LinkedTreeMap<String, Object>>) response.body();
            ArrayList<LinkedTreeMap<String, Object>> c =
                    (ArrayList<LinkedTreeMap<String, Object>>) b.get("parse").get("sections");
            for (int i = 0; i < c.size(); ++i) {
                allCategories.add((String) c.get(i).get("line"));
            }
            //todo come back to this!
//            Wikipedia.getInstance().spokenPagesCategories = allCategories;
        }
        return allCategories;
    }

    static List<String> callGetSpokenPagesNamesByCategories(String category) throws IOException {
        List<String> pageNames = new ArrayList<>();
        String url = "https://en.wikipedia.org/wiki/Wikipedia:Spoken_articles";
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select(".mw-headline, li");
        for (int i = 1; i < elements.size(); i++) {
            Element curElement = elements.get(i);
            if ("mw-headline".equals(curElement.className()) && curElement.text().equals(category))
            {
                ++i;
                curElement = elements.get(i);
                while(i < elements.size() && !"mw-headline".equals(curElement.className()))
                {
                    pageNames.add(curElement.text());
                    ++i;
                    curElement = elements.get(i);
                }
                break;
            }
        }
        return pageNames;
    }
    //todo implement.
    public Call<Object> uploadFile(String fileName, String filePath) throws IOException {

        // todo add tests if something went wrong.
        QuarryResponse tokenResponse = this.server.getToken().execute().body();

        String logintoken = null;
        if (tokenResponse != null) {
            logintoken = tokenResponse.query.tokens.logintoken;
            Log.d("file upload status", "got login token");
        }
        QuarryResponse loginResponse = server.login("login",
                "json",
                logintoken,
                "Tomer_ronen@WikiAudio",
                "tkpemajv20jm4t1ofm2amr5mb7p1v9cv").execute().body();
        //todo add check if login failed
        QuarryResponse csrfResponse = server.getCsrfToken().execute().body();
        if (csrfResponse != null) {
            String csrfToken = csrfResponse.query.tokens.csrftoken;
            Log.d("file upload status", "got csrf token");
            File file = new File(filePath);
            long length = file.length();
            if (file.exists()) {
                RequestBody requestFile =
                        RequestBody.create(
                                MediaType.parse("3gp"),
                                file
                        );
                MultipartBody.Part body =
                        MultipartBody.Part.createFormData("file",
                                fileName,
                                requestFile);
                Log.d("file upload status", "opened file");
                Response<Object> execute =
                        server.uploadFile("upload",
                                fileName,
                                "json",
                                csrfToken,
                                1,
                                body).execute();
                Object body1 = execute.body();
                Log.d("file upload status", "file uploaded");


                return null;
            }
        }
        return null;
    }


    private static Wikipage parseWikiData(QuarryResponse.PageData pageData) {
        if(pageData != null)
        {
            String mobileHtml = "https://en.wikipedia.org/api/rest_v1/page/mobile-html/";
            Wikipage curWikipage = new Wikipage();
            curWikipage.setTitle(pageData.title);
            curWikipage.setComputerUrl(pageData.fullurl);
            curWikipage.setUrl(mobileHtml + pageData.title);
            curWikipage.setDescription(pageData.description);
            curWikipage.setWatchers(pageData.watchers);
            List<QuarryResponse.CoordinatesData> coordinates = pageData.coordinates;
            if(coordinates != null && coordinates.size() > 0)
            {
                curWikipage.setLat(pageData.coordinates.get(0).lat);
                curWikipage.setLon(pageData.coordinates.get(0).lon);
            }
            if(pageData.original != null)
            {
                curWikipage.setThumbnailSrc(pageData.original.source);
            }
            else if(pageData.thumbnail != null)
            {
                curWikipage.setThumbnailSrc(pageData.thumbnail.source);

            }
            return curWikipage;
        }
        return null;
    }

    private static String getQueryProp(List<PageAttributes> pageAttributes) {
        Set<String> props = new HashSet<>();
        StringBuilder prop = new StringBuilder();

//        if(pageAttributes.contains(title)) props.add("title");
        if(pageAttributes.contains(url))   props.add("info");
        if(pageAttributes.contains(content)) props.add("info");
        if(pageAttributes.contains(audioUrl)) props.add("info");
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
        if(pageAttr.contains(audioUrl)) inprops.add("url");
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