package com.example.wikiaudio.wikipedia;

import java.util.ArrayList;
import java.util.List;

public class WikiPage {


    public static WikiPage getPageForTesting() {
        WikiPage wp = new WikiPage();
        ArrayList<Section> sections = new ArrayList<>();
        Section section1 = new Section("Short Text",
                "Hey im a short Text");
        Section section2 = new Section("Long Text",
                "<p>The first Jewish settlers lived in a building known as the " +
                        "Khan near Hadera's main synagogue. The population consisted of ten" +
                        " families and four guards. Baron Edmond de Rothschild provided funding" +
                        " for Egyptian laborers to drain the swamps. Old tombstones in the local" +
                        " cemetery reveal that out of a population of 540, 210 died of malaria." +
                        " Therefore a Bible verse from the Psalms (Tehillim) was inscribed in the " +
                        "city's logo: \"Those who sow in tears, will reap with songs of joy.\" " +
                        "(Ps 126:5) Hashomer guards kept watch over the fields to prevent" +
                        " incursions by the neighboring Bedouin.\n" +
                        "</p><p>By the early twentieth century, Hadera had become the" +
                        " regional economic center. In 1913, the settlement included forty " +
                        "households, as well as fields and vineyards, stretching over 30,000 " +
                        "dunam.</p>\n" +
                        "<p>The first Jewish settlers lived in a building known as the " +
                        "Khan near Hadera's main synagogue. The population consisted of ten" +
                        " families and four guards. Baron Edmond de Rothschild provided funding" +
                        " for Egyptian laborers to drain the swamps. Old tombstones in the local" +
                        " cemetery reveal that out of a population of 540, 210 died of malaria." +
                        " Therefore a Bible verse from the Psalms (Tehillim) was inscribed in the " +
                        "city's logo: \"Those who sow in tears, will reap with songs of joy.\" " +
                        "(Ps 126:5) Hashomer guards kept watch over the fields to prevent" +
                        " incursions by the neighboring Bedouin.\n" +
                        "</p><p>By the early twentieth century, Hadera had become the" +
                        " regional economic center. In 1913, the settlement included forty " +
                        "households, as well as fields and vineyards, stretching over 30,000 " +
                        "dunam.</p>\n + " +
                        "<p>Hadera is located on the Israeli Mediterranean coastal plain, 45 km " +
                        "(28 mi) north of Tel Aviv.  The city's jurisdiction covers 53,000" +
                        " dunams (53.0 km<sup>2</sup>; 20.5 sq mi), making it the fourth " +
                        "largest city in the country. Nahal Hadera Park, a eucalyptus" +
                        " forest covering 1,300 dunams (1.3 km<sup>2</sup>; 0.5 sq mi) and" +
                        " Hasharon Park are located on the outskirts of Hadera.</p>" +
                        "<p>Hot water gushing from the Hadera power plant draws schools" +
                        " of hundreds of sandbar and dusky sharks every winter. Scientists" +
                        " are researching the rare phenomenon, which is unknown in the vicinity." +
                        " It is speculated that the water, which is ten degrees warmer than " +
                        "the rest of the sea, may be the attraction.</p>\n"
        );
        Section section3 = new Section("section three", "content three");
        Section section4 = new Section("section four", "content four");

        sections.add(section1);
        sections.add(section2);
        sections.add(section3);
        sections.add(section4);
        wp.sections = sections;
        wp.url = "www.example.org";
        wp.audioUrl = "www.example.org"; // TODO - make a real audio url
        wp.title = "test_page";
        return wp;
    }

    public String getTitle() {
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
