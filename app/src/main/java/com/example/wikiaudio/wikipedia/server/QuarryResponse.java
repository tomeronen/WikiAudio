package com.example.wikiaudio.wikipedia.server;


import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;

/**
 * class of wikipedia quarry response.
 */
public class QuarryResponse {
    public LinkedTreeMap<String, Object> warnings;
//    public LinkedTreeMap<String, PageData> pages;
    public QueryData query;

    public static class QueryData{
        LinkedTreeMap<String, PageData> pages;
        Tokens tokens;
        ArrayList<PageData> search;

    }

    public static class SearchResults{
        String title;
    }

    public static class Tokens
    {
        public String csrftoken;
        public String logintoken;
    }
    public static class PageData{
        String title;
        String fullurl;
        String description;
        String pageid;
        int watchers;
        ThumbnailData thumbnail;
        ThumbnailData original;
        List<CoordinatesData> coordinates;



        List<FileInfo> imageinfo;

        public class FileInfo {
            String url;
            String descriptionurl;
            String descriptionshorturl;
        }
    }

    public static class ThumbnailData
    {
        String source;
        int width;
        int height;
    }

    public static class CoordinatesData
    {
        Double lat;
        Double lon;
    }
}
