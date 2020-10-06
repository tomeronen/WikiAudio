package com.wikiaudioapp.wikiaudio.wikipedia.wikipage;

import com.wikiaudioapp.wikiaudio.activates.playlist.Playlist;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Wikipage {
    private static Set<String> unwantedTags = new HashSet<>();
    static {
        unwantedTags.add(".mw-ref");
    }


    public static class Section{
        String title;
        String htmlText;

        public Section(String title, String htmlText) {
            this.title = title;
            this.htmlText = htmlText;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContents() {
            return htmlText;
        }

        public void setContents(String contents) {
            this.htmlText = contents;
        }
    }

    private String audioFileName;
    private String computerUrl;
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
    private Playlist playlist;
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
        StringBuilder fullText = new StringBuilder();
        if(sections != null) {
            for (Section s : sections) {
                Document doc = Jsoup.parse(s.htmlText);
                Elements select = doc.select(".mw-ref");
                select.remove();
                fullText.append(s.title).append(". ").append(doc.select("p").text());
            }
        }
        return fullText.toString();
    }

    public void setComputerUrl(String computerUrl) {
        this.computerUrl = computerUrl;
    }

    public void setAudioFileName(String audioFileName) {
        this.audioFileName = audioFileName;
    }

    public String getAudioFileName() {
        return audioFileName;
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

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public String getComputerUrl() {
        return computerUrl;
    }

}