package com.example.wikiaudio.wikipedia;

import java.util.List;

public class WikiPage {


    public Object getTitle() {
        return this.title;
    }

    public class Section{
        String title;
        String content;
    }

    String url;
    String title;
    String audioUrl;
    List<Section> sections;

    public int numberOfSections() {
        return sections.size();
    }

    public String getSection(int section) {
        return this.sections.get(section).content; // todo make return title and then content.
    }

}
