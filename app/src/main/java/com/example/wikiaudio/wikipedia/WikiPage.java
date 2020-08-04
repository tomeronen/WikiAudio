package com.example.wikiaudio.wikipedia;

import java.util.ArrayList;
import java.util.List;

public class WikiPage {


    public static WikiPage getPageForTesting() {
        WikiPage wp = new WikiPage();
        ArrayList<Section> sections = new ArrayList<>();
        Section section1 = new Section("section one", "content one");
        Section section2 = new Section("section two", "content two");
        Section section3 = new Section("section three", "content three");
        Section section4 = new Section("section four", "content four");

        sections.add(section1);
        sections.add(section2);
        sections.add(section3);
        sections.add(section4);
        wp.sections = sections;
        wp.url = "www.example.org";
        wp.audioUrl = "www.example.org"; // TODO - make a real audio url
        return wp;
    }

    public Object getTitle() {
        return this.title;
    }

    // TODO - make to outer class
    public static class Section{
        String title;
        String content;

        public Section(String section_one, String content_one) {
            this.title = section_one;
            this.content = content_one;
        }
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
