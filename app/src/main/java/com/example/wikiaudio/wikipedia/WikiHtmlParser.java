package com.example.wikiaudio.wikipedia;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WikiHtmlParser {
    private static final String TITLE_CLASS = "firstHeading";
    private static final String BASE_URL = "https://he.wikipedia.org/";

    String url;

   static public void parseAdvanceAttr(WikiPage wikiPage) throws IOException {
        Document doc = Jsoup.connect(wikiPage.getUrl()).get();
        // parse text
        List<WikiPage.Section> sections = new ArrayList<>();
        String pageTitle = doc.getElementsByClass(TITLE_CLASS).text();
        String curSectionName = pageTitle;
        Elements elements = doc.select(".mw-headline, p");
        List<String> paragraphsInSection = new ArrayList<String>();
        for (int i = 1; i < elements.size(); i++) {
            // todo bad implantation does not take last element.
            Element curElement = elements.get(i);
            if (!"mw-headline".equals(curElement.className())) // element is not a p
                paragraphsInSection.add(curElement.text());
            else {
                sections.add(new WikiPage.Section(curSectionName, paragraphsInSection));
                curSectionName = curElement.text();
                paragraphsInSection = new ArrayList<>();
            }
        }
        wikiPage.setSections(sections);


//     parse indicators
       Elements indicators = doc.getElementsByClass("mw-indicator");
       List<String> indicatorsValues = new ArrayList<>();
       for (Element e : indicators) {
           indicatorsValues.add(e.id());
           switch (e.id()) {
               case "mw-indicator-spoken-wikipedia":
               case "mw-indicator-spoken-icon":
                   String audioPageUrl =
                           e.select("a").attr("href");
                   String a = BASE_URL + audioPageUrl;
                   Document d = Jsoup.connect(BASE_URL + audioPageUrl).get();

//     todo - assumes first internal link is to audio file, not good!

                   String internal = d.getElementsByClass("internal").text();
                   wikiPage.setAudioUrl
                           (d.getElementsByClass("internal").first()
                                   .attr("href"));
                   break;
           }
       }
       wikiPage.setIndicators(indicatorsValues);
    }


    // parse title
//            String pageTitle = doc.getElementsByClass(TITLE_CLASS).text();
//            wikiPage.setTitle(pageTitle);

}
