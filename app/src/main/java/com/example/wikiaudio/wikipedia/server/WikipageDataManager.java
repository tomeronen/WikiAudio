package com.example.wikiaudio.wikipedia.server;

import android.util.Log;

import com.example.wikiaudio.wikipedia.wikipage.PageAttributes;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * a helper class for Wikipedia facade. manages wikipages data.
 */
public class WikipageDataManager {
    private Document spokenDoc;
    private HashMap<String, Wikipage> wikipagesData = new HashMap<>(); // todo make singelton
    private HashMap<String, List<String>> spokenCategoriesMetaData = new HashMap<>();
    private HashMap<String, String> spokenWikipagesMetaData = new HashMap<>();
    String SPOKEN_URL = "https://en.wikipedia.org/wiki/Wikipedia:Spoken_articles";
    private boolean MetaDataLoaded;

    // todo make singleton!
    public void initWikipageDataManager()
            throws IOException {
        spokenDoc = getSpokenDocument();
        if(spokenDoc != null && !MetaDataLoaded)
        {
            this.loadSpokenWikipagesMetaData();
        }
    }

    public Wikipage getWikipage(String name, List<PageAttributes> attributes)
            throws IOException {
        if(wikipagesData != null) // we were initialized.
        {
            Wikipage wikipage = wikipagesData.getOrDefault(name, null);
            if(wikipage != null) //  we already have the wanted page data. (todo check we have all attributes)
            {
                return wikipage;
            }
            else // we don't have the data -> load it.
            {
                Wikipage page = WikiServerHolder.getInstance().getPage(name, attributes);
                this.setWikipage(page);
                return page;
            }
        }
        throw new IOException(); // attempt to get page from uninitialized WikipageDataManager.
    }

    public void setWikipage(Wikipage wikipage)
    {
        if(wikipagesData != null)
        {
            wikipagesData.put(wikipage.getTitle(), wikipage);
        }
        else // add attributes to the existing page.
        {

        }
     }

    /**
     *
     */
    public synchronized void loadSpokenWikipagesMetaData()
            throws IOException {
        Log.d("loading data", "start parsing spoken wikipages data");
        spokenDoc = getSpokenDocument();
        Elements elements = spokenDoc.select(".mw-headline, li");
        for (int i = 1; i < elements.size(); i++) {
            Element curElement = elements.get(i);
            if ("mw-headline".equals(curElement.className())) { // we got to a start of new category
                String curCategory = curElement.text();
                List<String> pageNames = new ArrayList<>();
                ++i;
                curElement = elements.get(i);
                while (i < elements.size() &&
                        !"mw-headline".equals(curElement.className())) {
                    String wikiTitle = curElement.text();
                    pageNames.add(wikiTitle);
                    ++i;
                    if(i < elements.size())
                    {
                        curElement = elements.get(i);
                    }
                }
                this.spokenCategoriesMetaData.put(curCategory, pageNames);
                --i;
            }
        }
        Elements wikipagesMetaData = spokenDoc.getElementsByAttributeValueStarting("title", "File:");
        for(Element wikipageMetaData: wikipagesMetaData)
        {
            String wikipageTitle = wikipageMetaData.text();
            String FileName = wikipageMetaData.attr("title");
            this.spokenWikipagesMetaData.put(wikipageTitle, FileName);
        }
        Log.d("loading data", "finished parsing spoken wikipages data");
        MetaDataLoaded = true;
    }

    private Document getSpokenDocument()
            throws IOException {
        if (spokenDoc != null) {
            return spokenDoc;
        } else {
                spokenDoc = Jsoup.connect(SPOKEN_URL).get();
                return spokenDoc;
        }
    }



    public List<Wikipage> getSpokenPagesByCategories(String category,
                                                     List<PageAttributes> pageAttributes)
            throws IOException {
        List<Wikipage> resultWikipages = new ArrayList<>();
        List<String> pagesNames = this.getSpokenCategoriesMetaData()
                .getOrDefault(category, null);
        if(pagesNames == null)
        {
            throw new IOException();
        }

        // load basic Attributes:
        WikiServerHolder.getInstance().loadPagesByName(wikipagesData,
                                                        pagesNames,
                                                        pageAttributes);

        // load audio source: (needs a different call to wiki server then basic Attributes)
        if(pageAttributes.contains(PageAttributes.audioUrl))
        {
            WikiServerHolder
                    .getInstance().loadAudioSource(wikipagesData,
                    getSpokenWikipagesMetaData(),
                    pagesNames);
        }
        // todo add option for content.

        // load the created wikipages into the result.
        for(String pageName:pagesNames)
            // all wikipages data was brought from wikipedia servers at once. here we just take
            // all the pages that were created one by one.
        {
            Wikipage wikipage = this.wikipagesData.get(pageName);
            if(wikipage != null)
            {
                resultWikipages.add(wikipage);
            }
        }
        return resultWikipages;
    }

    public List<String> getSpokenPagesNamesByCategories(String category)
            throws IOException {
        return getSpokenCategoriesMetaData().get(category);
    }

    private HashMap<String, List<String>> getSpokenCategoriesMetaData()
            throws IOException {
        if (!MetaDataLoaded) {
            loadSpokenWikipagesMetaData();
        }
        return this.spokenCategoriesMetaData;
    }

    /**
     * @return gets the data of
     * @throws IOException
     */
    public HashMap<String, String> getSpokenWikipagesMetaData()
            throws IOException {
        if (!MetaDataLoaded) {
            loadSpokenWikipagesMetaData();
        }
        return this.spokenWikipagesMetaData;
    }

    /**
     * @return gets the Categories of the spoken wikipedia articles.
     * @throws IOException if there was a problem with the connection to wikipedia.
     */
    public List<String> getSpokenCategories()
            throws IOException {
        if (!MetaDataLoaded) {
            loadSpokenWikipagesMetaData();
        }
        return new ArrayList<>(this.spokenCategoriesMetaData.keySet());
    }
}
