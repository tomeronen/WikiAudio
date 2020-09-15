package com.example.wikiaudio.wikipedia;

import android.util.Log;

import androidx.work.ListenableWorker;

import com.example.wikiaudio.file_manager.FileManager;
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

import static com.example.wikiaudio.wikipedia.PageAttributes.*;

public class WikiServerHolder {
    static final HashMap<PageAttributes, String> attributesStringMap = new HashMap<>();
    private static final String BASE_URL = "https://en.wikipedia.org";
    private static WikiServerHolder instance = null;
    private static FileManager fileManager;
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

        // init cookie manager
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

    public List<WikiPage> searchPage(String pageName)
            throws IOException {
        Response<QuarryResponse> response = server.searchPage(pageName).execute();
        if (response.code() == 200 && response.isSuccessful()) {
            // task was successful.
            List<WikiPage> wikiPageList = parseSearchResponse(response.body());
            return wikiPageList;
        } else {
            // task failed.
            throw new IOException();
        }
    }

    private List<WikiPage> parseSearchResponse(QuarryResponse searchResponse)
            throws IOException {
        if (searchResponse != null
                && searchResponse.query != null
                && searchResponse.query.search != null) {
            List<WikiPage> resultList = new ArrayList<>();
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

    public WikiPage getPage(String name, List<PageAttributes> pageAttr)
            throws IOException
    {
        String prop = getQueryProp(pageAttr);
        String inprop = getQueryInProp(pageAttr);
        Response<QuarryResponse> response = server.callGetPageByName(name, prop, inprop).execute();
        if (response.code() == 200 && response.isSuccessful()) {
            // task was successful.
            List<WikiPage> wikiPageList = parseQuarryResponse(response.body());
            if(pageAttr.contains(content) ||
                    pageAttr.contains(audioUrl) ||
                    pageAttr.contains(thumbnail))
            {
                WikiHtmlParser.parseAdvanceAttr(wikiPageList.get(0));
            }
            return wikiPageList.get(0);
        } else {
            // task failed.
            throw new IOException();
        }
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
            if(pageAttr.contains(content) ||
                    pageAttr.contains(audioUrl) ||
                        pageAttr.contains(thumbnail))
            {
                for(WikiPage wikiPage: wikiPageList)
                {
                    WikiHtmlParser.parseAdvanceAttr(wikiPage);
                }
            }
            return wikiPageList;
        } else {
            // task failed.
            throw new IOException();
        }
    }

    private static List<WikiPage> parseQuarryResponse(QuarryResponse quarryResponse)
            throws IOException {
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
            throw new IOException();
        }
    }


    List<String> callGetSpokenPagesCategories() throws IOException {
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
    public Call<Object> uploadFile(String fileName, String filePath)
            throws IOException {

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
            if(file.exists())
            {
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

//                Response<Object> execute =
//                        server.uploadFile("upload",
//                                "testimg.jpg",
//                                "https://cdn.pixabay.com/photo/2018/07/26/07/45/valais-3562988_1280.jpg",
//                                "json",
//                                csrfToken,
//                                1).execute();
                Object body1  = execute.body();
                Log.d("file upload status", "file uploaded");
            }
        }


        return null;
    }


    private static WikiPage parseWikiData(QuarryResponse.PageData pageData) {
        if(pageData != null)
        {
            WikiPage curWikiPage = new WikiPage();
            curWikiPage.setTitle(pageData.title);
            curWikiPage.setUrl(pageData.fullurl);
            curWikiPage.setDescription(pageData.description);
            curWikiPage.setWatchers(pageData.watchers);
            List<QuarryResponse.CoordinatesData> coordinates = pageData.coordinates;
            if(coordinates != null && coordinates.size() > 0)
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
        return null;
    }

    private static String getQueryProp(List<PageAttributes> pageAttributes) {
        Set<String> props = new HashSet<>();
        StringBuilder prop = new StringBuilder();

        if(pageAttributes.contains(title)) props.add("title");
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
