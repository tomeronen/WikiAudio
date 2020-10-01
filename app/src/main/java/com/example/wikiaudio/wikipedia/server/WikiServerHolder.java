package com.example.wikiaudio.wikipedia.server;

import android.util.Log;

import com.example.wikiaudio.wikipedia.wikipage.PageAttributes;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import org.jsoup.HttpStatusException;
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
    private static final String BASE_URL = "https://en.wikipedia.org";
    private static final String UPLOAD_FILE_STATUS = "file upload status";
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
            List<Wikipage> wikipages = parseQuarryResponse(response.body());
            if(pageAttr.contains(content) ||
                    pageAttr.contains(audioUrl)) {
                for (Wikipage wikipage : wikipages) {

                    WikiHtmlParser.parseAdvanceAttr(wikipage);
                }
            }
            return wikipages;
        } else {
            // task failed.
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
            List<Wikipage> WikipageList = parseQuarryResponse( response.body());
//            List<Wikipage> WikipageList = new ArrayList<>();
//            if(pageAttr.contains(content) ||
//                    pageAttr.contains(audioUrl))
//            {
//                WikiHtmlParser.parseAdvanceAttr(WikipageList.get(0));
//            }
//todo add load audio.
            if(pageAttr.contains(content))
            {
                Response<ContentResponse> responsePageContents = server.callPageContents(name).execute();
                if (response.code() == 200 && response.isSuccessful()) {
                    List<Wikipage.Section> sections
                            = parseContentResponse(responsePageContents.body());
                    WikipageList.get(0).setSections(sections);
                }
            }
            return WikipageList.get(0);
        } else {
            // task failed.
            throw new IOException();
        }
    }

    private List<Wikipage.Section> parseContentResponse(ContentResponse contentResponse)
    {
        List<Wikipage.Section> result = new ArrayList<>();
        ContentResponse.LeadData lead  = contentResponse.lead;
        String leadTitle = lead.displaytitle;
        ContentResponse.SectionData leadSectionData = lead.sections.get(0);
        if(leadSectionData != null)
        {
            String htmlText  = lead.sections.get(0).text;
            Wikipage.Section leadSection = new Wikipage.Section(leadTitle, htmlText);
            result.add(leadSection);
        }
        if(contentResponse.remaining != null
                && contentResponse.remaining.sections != null
                && !contentResponse.remaining.sections.isEmpty())
        {
            for(ContentResponse.SectionData sectionData: contentResponse.remaining.sections)
            {
                Wikipage.Section curSection = new Wikipage.Section(sectionData.line,
                                                                    sectionData.text);
                result.add(curSection);
            }
        }
        return result;
    }

    /**
     * find Wikipages objects by there names.
     * @param names the name of the wikipages to bring.
     * @param pageAttr the Attributes to bring on each page.
     * @return a list of Wikipages based on the names given.
     * @throws IOException if task fails.
     */
    public List<Wikipage> getPagesByName(List<String> names,
                                         List<PageAttributes> pageAttr)
            throws IOException
    {
        String prop = getQueryProp(pageAttr);
        String inprop = getQueryInProp(pageAttr);
        String namesToSearch = getNamesToSearch(names);
        Response<QuarryResponse> response = server.callGetPageByName(namesToSearch,
                prop, inprop, "original|thumbnail").execute();
        if (response.code() == 200 && response.isSuccessful()) {
            // task was successful.
            List<Wikipage> wikipagesRuslt = parseQuarryResponse(response.body());
            if(pageAttr.contains(content) ||
                    pageAttr.contains(audioUrl) ) {
                List<Wikipage> badPages = new ArrayList<>();
                for (Wikipage wikipage : wikipagesRuslt) {
                    try {
                        WikiHtmlParser.parseAdvanceAttr(wikipage);
                    } catch (HttpStatusException e) {
                        Log.e("http error",
                                "something went wrong with bringing " +
                                        "advance Attributes of page:" + wikipage.getTitle());
                        badPages.add(wikipage);
                    }
                }
                wikipagesRuslt.removeAll(badPages); // todo do we want to remove if failed?
            }
            return wikipagesRuslt;
        } else {
            // task failed.
            throw new IOException();
        }
    }



    private static boolean quarryResponseIsLegal(QuarryResponse quarryResponse) {
        return quarryResponse != null
                && quarryResponse.query != null
                && quarryResponse.query.pages != null;
    }

    private String parseAudioFileResponse(QuarryResponse quarryResponse)
            throws IOException {
        if (quarryResponseIsLegal(quarryResponse)) {
            StringBuilder fileNames = new StringBuilder();
            if(    quarryResponse.query.pages.values().size() == 1)
            {
                return quarryResponse.query.pages.get(0).title;
            }
            for(QuarryResponse.PageData pageData:
                    quarryResponse.query.pages.values())
            {
                fileNames.insert(0, pageData.title + "|");
            }
            return fileNames.toString();
        }
        else
        {
            throw new IOException();
        }
    }

    private String getNamesToSearch(List<String> names) {
        StringBuilder result = new StringBuilder();
        for(String name:names)
        {
            result.append(name).append("|");
        }
        if (result.length() > 0 && result.charAt(result.length() - 1) == '|')
        {
            result = new StringBuilder(result.substring(0, result.length() - 1));
        }
        return result.toString();
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
                        inprop,
                        "original|thumbnail").execute();
        if (response.code() == 200 && response.isSuccessful()) {
            // task was successful.
            List<Wikipage> WikipageList = parseQuarryResponse(response.body());
            if(pageAttr.contains(content) ||
                    pageAttr.contains(audioUrl))
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
        if (quarryResponseIsLegal(quarryResponse)) {
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
    public void uploadFile(String fileName,
                           String filePath,
                           WikiUserData userData)
            throws IOException {

        // todo add tests if something went wrong.
        QuarryResponse tokenResponse = this.server.getToken().execute().body();
        String logintoken = null;
        if (tokenResponse != null) {
            logintoken = tokenResponse.query.tokens.logintoken;
            Log.d(UPLOAD_FILE_STATUS, "got login token");
        }
        String name = userData.getUserName();
        String password = userData.getPassword();
        boolean isBot = userData.isBot();
        String csrfToken;
        if(!isBot)
        {
            Object loginResponse = server.loginUser("clientlogin",
                    "json",
                    logintoken,
                    name,
                    "https://www.mediawiki.org/w/api.php?action=help&modules=clientlogin ",
                    password).execute().body();
            //todo add check if login failed
            QuarryResponse csrfResponse = server.getCsrfToken().execute().body();
            csrfToken = csrfResponse.query.tokens.csrftoken;
            Log.d(UPLOAD_FILE_STATUS, "got csrf token");
        }
        else
        {
            QuarryResponse loginResponse = server.loginBot("login",
                    "json",
                    logintoken,
                    name,
                    password).execute().body();
            //todo add check if login failed
            QuarryResponse csrfResponse = server.getCsrfToken().execute().body();
            csrfToken = csrfResponse.query.tokens.csrftoken;
            Log.d(UPLOAD_FILE_STATUS, "got csrf token");
        }
        if (csrfToken != null) { //  we managed to get a csrf token.
            Log.d("file upload status", "got csrf token");
            Response<Object> contentResponseCall = server.checkUserRights().execute();
            //todo here we can see if the user has the permission to load files.
            File file = new File(filePath);
            long length = file.length();
            if (file.exists() && length > 0) {
                sendFile(fileName, csrfToken, file);
            }
        }
    }

    private void sendFile(String fileName, String csrfToken, File file)
            throws IOException {
        Log.d("file upload status", "opened file");
        RequestBody body = RequestBody.create(MediaType.parse("audio/ogg"),
                file);
        MultipartBody.Part fileData =
                MultipartBody.Part.createFormData("file", fileName, body);
        RequestBody actionBody = RequestBody.create(
                MediaType.parse("text/plain"),
                "upload");
        RequestBody fileNameBody = RequestBody.create(
                MediaType.parse("text/plain"),
                fileName);
        RequestBody formatBody = RequestBody.create(
                MediaType.parse("text/plain"),
                "json");
        RequestBody csrfTokenBody = RequestBody.create(
                MediaType.parse("text/plain"),
                csrfToken);
        RequestBody ignoreBody = RequestBody.create(
                MediaType.parse("text/plain"),
                "1");
        RequestBody commentBody = RequestBody.create(
                MediaType.parse("text/plain"),
                "audio file from wiki audio");
        Response<UploadResponse> uploadFileResponse =
                server.uploadFile(actionBody,
                        fileNameBody,
                        formatBody,
                        csrfTokenBody,
                        ignoreBody,
                        commentBody,
                        fileData).execute();
        if(uploadFileResponse.body().upload == null
                || uploadFileResponse.body().upload.result == null
                || !uploadFileResponse.body().upload.result.equals("Success"))
        {
            Log.d("file upload status", "file uploaded failed:" + fileName);
            throw new IOException(); // we had a problem with loading the file.
        }
        else
        {
            Log.d("file upload status", "file uploaded succeeded:" + fileName);
        }
    };


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


    private String getNames(List<Wikipage> wikipages) {
        StringBuilder result = new StringBuilder();
        for(Wikipage wikipage:wikipages)
        {
            result.append(wikipage.getTitle()).append("|");
        }
        if (result.length() > 0 && result.charAt(result.length() - 1) == '|')
        {
            result = new StringBuilder(result.substring(0, result.length() - 1));
        }
        return result.toString();
    }


    public void loadPagesByName(HashMap<String, Wikipage> wikipagesData,
                                List<String> pagesNames,
                                List<PageAttributes> pageAttr)
            throws IOException {
        String prop = getQueryProp(pageAttr) + "|" + "images" ;
        String inprop = getQueryInProp(pageAttr);
        String namesToSearch = getNamesToSearch(pagesNames);
        Response<QuarryResponse> response = server.callGetPageByName(namesToSearch,
                prop, inprop, "original|thumbnail").execute();
        if (response.code() == 200 && response.isSuccessful()) {
            // task was successful.
            loadQuarryResponse(wikipagesData,response.body());
        } else {
            // task failed.
            throw new IOException();
        }
    }

    private void loadQuarryResponse(HashMap<String, Wikipage> wikipagesData,
                                    QuarryResponse quarryResponse)
            throws IOException {
        if (quarryResponseIsLegal(quarryResponse)) {
            for(QuarryResponse.PageData pageData:
                    quarryResponse.query.pages.values())
            {
                Wikipage wikipage = parseWikiData(pageData);
                wikipagesData.put(wikipage.getTitle(), wikipage);
            }
        }
        else
        {
            throw new IOException();
        }

    }



    private HashMap<String, String> parseAudioSourceResponse(QuarryResponse quarryResponse)
            throws IOException {
        if (quarryResponseIsLegal(quarryResponse)) {
            HashMap<String, String> fileSourcesResults = new HashMap<>();
            for(QuarryResponse.PageData pageData:
                    quarryResponse.query.pages.values())
            {
                if(pageData.imageinfo != null && pageData.imageinfo.get(0) != null)
                {
                    fileSourcesResults.put(pageData.title, pageData.imageinfo.get(0).url);
                }
            }
            return fileSourcesResults;
        }
        else
        {
            throw new IOException();
        }


    }

    private String getFileNames(HashMap<String, String> spokenWikipagesMetaData,
                                List<String> pagesNames) {
        StringBuilder fileNames = new StringBuilder();
        boolean first = true;
        for (String name : pagesNames) {
            String curFileName = spokenWikipagesMetaData.get(name);
            if (curFileName != null) {
                if (first) {
                    fileNames = new StringBuilder(curFileName);
                    first = false;
                } else {
                    fileNames.append("|").append(spokenWikipagesMetaData.get(name));
                }
            }
        }
        return fileNames.toString();
    }

    public void loadAudioSource(HashMap<String, Wikipage> wikipagesData,
                            HashMap<String, String> spokenWikipagesMetaData,
                            List<String> pagesNames) throws IOException {
        String fileNames = getFileNames(spokenWikipagesMetaData, pagesNames);
        Response<QuarryResponse> fileResponse = server.callGetAudioFile(fileNames).execute();
        if (fileResponse.code() == 200 && fileResponse.isSuccessful()) {
            HashMap<String, String> fileSourcesResults =
                    parseAudioSourceResponse(fileResponse.body());
            for (String pageName : pagesNames) {
                String fileName = spokenWikipagesMetaData.get(pageName);
                String fileSource = fileSourcesResults.get(fileName);
                Wikipage wikipage = wikipagesData.get(pageName);
                if (wikipage != null) {
                    wikipage.setAudioUrl(fileSource);
                }
            }
        }
    }


    public void getPageContents(String pageTitle)
            throws IOException {
        Response<ContentResponse> response = this.server.callPageContents(pageTitle).execute();
        if (response.code() == 200 && response.isSuccessful()) {

        }
    }
}



// unused options for functions:


//    private void parseAudioSourceResponse(QuarryResponse quarryResponse,
//                                                  List<Wikipage> wikipages) throws IOException {
//        if (quarryResponseIsLegal(quarryResponse)) {
//            List<String> result = new ArrayList<>();
//            int counter = 0;
//            for(QuarryResponse.PageData pageData:
//                    quarryResponse.query.pages.values())
//            {
//                if(pageData.imageinfo != null && pageData.imageinfo.get(0) != null)
//                {
//                    wikipages.get(counter).setAudioUrl(pageData.imageinfo.get(0).url);
////                    result.add(pageData.imageinfo.get(0).url);
//                }
//                counter++;
//            }
////            return result;
//        }
//        else
//        {
//            throw new IOException();
//        }
//
//    }



//    private void loadAudioSources(String names, List<Wikipage> wikipages) throws IOException {
//        Response<QuarryResponse> response = server.callGetAudioFilesNames(names).execute();
//        // todo for a weird reason brings back values in reverse order.
//        if (response.code() == 200 && response.isSuccessful()) {
//            // call to get audio files name was Successful
//            String audioFileNames =  parseAudioFileResponse(response.body());
//            Response<QuarryResponse> fileResponse = server.callGetAudioFile(audioFileNames).execute();
//            if (fileResponse.code() == 200 && fileResponse.isSuccessful()) {
//                // we got the files source.
//                parseAudioSourceResponse(fileResponse.body(), wikipages);
//            }
//        }
//    }


//    public void fillWikipages(List<Wikipage> wikipages, List<PageAttributes> pageAttr)
//            throws IOException {
//        String prop = getQueryProp(pageAttr) + "|" + "images" ;
//        String inprop = getQueryInProp(pageAttr);
//        String namesToSearch = getNames(wikipages);
//        Response<QuarryResponse> response = server.callGetPageByName(namesToSearch,
//                prop, inprop, "original|thumbnail").execute();
//        if (response.code() == 200 && response.isSuccessful()) {
//            // task was successful.
//            HashMap<String, Wikipage> wikipageMap = new HashMap<>();
//            List<Wikipage> wikipagesResult = parseQuarryResponse(response.body());
//
//            if(pageAttr.contains(audioUrl))
//            {
//                loadAudioSources(namesToSearch, wikipagesResult);
//            }
//            if(pageAttr.contains(content) ) {
//                List<Wikipage> badPages = new ArrayList<>();
//                for (Wikipage wikipage : wikipagesResult) {
//                    try {
//                        WikiHtmlParser.parseAdvanceAttr(wikipage);
//                    } catch (HttpStatusException e) {
//                        Log.e("http error",
//                                "something went wrong with bringing " +
//                                        "advance Attributes of page:" + wikipage.getTitle());
//                        badPages.add(wikipage);
//                    }
//                }
//                wikipagesResult.removeAll(badPages); // todo do we want to remove if failed?
//            }
//        } else {
//            // task failed.
//            throw new IOException();
//        }
//    }