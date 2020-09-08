package com.example.wikiaudio.wikipedia;

import java.util.List;

public class WikiPage {



    public void setIndicators(List<String> indicators) {
        this.indicators = indicators;
    }



    // TODO - make to outer class?
    public static class Section{
        String title;
        List<String> contents;


        public Section(String section_one, List<String> content_one) {
            this.title = section_one;
            this.contents = content_one;
        }
    }

        private String title;
        private String url;
        private String description;
        private List<Section> sections;
        private List<String> indicators;
        private int watchers;
        private Double lat;
        private Double lon;
        private String audioUrl;
        private String thumbnailSrc;

    public static WikiPage getPageForTesting() {
        return null;
    }



    // ************************* Getters and setters ***********************************************

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String pageTitle) {
        this.title = pageTitle;
    }

    public String getThumbnailSrc() {
        return thumbnailSrc;
    }

    public void setThumbnailSrc(String thumbnailSrc) {
        this.thumbnailSrc = thumbnailSrc;
    }
    public int getWatchers() {
        return watchers;
    }

    public void setWatchers(int watchers) {
        this.watchers = watchers;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getIndicators() {
        return indicators;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public int numberOfSections() {
        return sections.size();
    }

    public Section getSection(int section) {
        return this.sections.get(section); // todo make return title and then content.
    }

}
