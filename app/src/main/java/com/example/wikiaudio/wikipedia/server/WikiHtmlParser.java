package com.example.wikiaudio.wikipedia.server;

import com.example.wikiaudio.wikipedia.wikipage.Wikipage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WikiHtmlParser {
    private static final String TITLE_CLASS = "firstHeading";
    private static final String BASE_URL = "https://he.wikipedia.org/";

    String url;

   static public void parseAdvanceAttr(Wikipage wikipage) throws IOException {
       //todo url bug. mobile/computer (this is just a patch)
       Document doc = Jsoup.connect(wikipage.getComputerUrl()).get();
        // parse text
        List<Wikipage.Section> sections = new ArrayList<>();
        String pageTitle = doc.getElementsByClass(TITLE_CLASS).text();
        String curSectionName = pageTitle;
        Elements elements = doc.select(".mw-headline, p");
        List<String> paragraphsInSection = new ArrayList<String>();
        for (int i = 1; i < elements.size(); i++) {
            // todo bad implantation does not take last element.
            Element curElement = elements.get(i);
            if (!"mw-headline".equals(curElement.className())) // element is not a p
            {
                Element paragraph = curElement.removeClass("reference");
                paragraphsInSection.add(paragraph.text());
            }
            else {
                sections.add(new Wikipage.Section(curSectionName, paragraphsInSection));
                curSectionName = curElement.text();
                paragraphsInSection = new ArrayList<>();
            }
        }
        wikipage.setSections(sections);


//     parse indicators
       Elements indicators = doc.getElementsByClass("mw-indicator");
       List<String> indicatorsValues = new ArrayList<>();
//       Elements audio = doc.select("audio");
//       Element element = audio.get(0);
//       String src = element.attr("src");
//       String audioSource = doc.select("audio").get(0).attr("src");
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
                   wikipage.setAudioUrl
                           ("https://" + d.getElementsByClass("internal").first()
                                   .attr("href"));
                   break;
           }
       }
       wikipage.setIndicators(indicatorsValues);
    }


    // parse title
//            String pageTitle = doc.getElementsByClass(TITLE_CLASS).text();
//            Wikipage.setTitle(pageTitle);

}
