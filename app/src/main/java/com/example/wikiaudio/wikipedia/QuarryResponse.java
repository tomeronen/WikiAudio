package com.example.wikiaudio.wikipedia;


import android.media.Image;

import com.google.gson.internal.LinkedTreeMap;

import java.util.List;
import java.util.Map;

/**
 * class of wikipedia quarry response.
 */
public class QuarryResponse {
    public LinkedTreeMap<String, Object> warnings;
//    public LinkedTreeMap<String, PageData> pages;
    public QueryData query;

    public static class QueryData{
        LinkedTreeMap<String, PageData> pages;
    }

    public static class PageData{
        String title;
        String fullurl;
        String description;
        String pageid;
        int watchers;
        ThumbnailData thumbnail;
        List<CoordinatesData> coordinates;
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
