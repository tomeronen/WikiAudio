package com.example.wikiaudio.wikipedia;

import java.util.List;

public class Wikipage {


    public String getComputerUrl() {
        return computerUrl;
    }

    private String computerUrl;

    public Wikipage() {}

    public void setIndicators(List<String> indicators) {
        this.indicators = indicators;
    }

    public void copy(Wikipage page) {
        if(page != null && page != this)
        {
            this.title = page.getTitle();
            this.url = page.getUrl();
            this.description = page.getDescription();
            this.sections = page.getSections();
            this.indicators = page.getIndicators();
            this.watchers = page.getWatchers();
            this.lat = page.getLat();
            this.lon = page.getLon();
            this.audioUrl = page.getAudioUrl();
            this.thumbnailSrc = page.getThumbnailSrc();
            this.computerUrl = page.getComputerUrl();
        }

    }

    public String getFullText() {
        String fullText = "";
        if(sections != null) {
            for (Section s : sections) {
                fullText += s.title + ".";
                for (String paragraph : s.contents
                ) {
                    fullText += paragraph;
                }
            }
        }
        return fullText;
    }

    public void setComputerUrl(String computerUrl) {
        this.computerUrl = computerUrl;
    }


    // TODO - make to outer class?
    public static class Section{
        String title;
        List<String> contents;


        public Section(String section_one, List<String> content_one) {
            this.title = section_one;
            this.contents = content_one;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<String> getContents() {
            return contents;
        }

        public void setContents(List<String> contents) {
            this.contents = contents;
        }
    }

    private String title;
    private String url;
    private String description;
    private String summary;
    private List<Section> sections;
    private List<String> indicators;
    private int watchers;
    private Double lat;
    private Double lon;
    private String audioUrl;
    private String thumbnailSrc;

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