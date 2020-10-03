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
    // right now we cant ask for more then 50 pages at once.
    private static final int MAX_QUERY_AT_ONCE = 40;
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
            if(wikipage != null && (!attributes.contains(PageAttributes.content)
                    || wikipage.getSections() != null
                    &&  !attributes.contains(PageAttributes.thumbnail))) //  we already have the wanted page data. (todo check we have all attributes)
            {
                    return wikipage;
            }
            else // we don't have the data -> load it.
            {
                Wikipage page = WikiServerHolder.getInstance().getPage(name, attributes);
                if(wikipage != null)
                {
                    // we had a wikipage just without sections, add sections to page.
                    wikipage.setSections(page.getSections());
                    return wikipage;

                }
                else
                {  // there was no data on the page, set a new page.
                    this.setWikipage(page);
                    return page;
                }
            }
        }
        throw new IOException(); // attempt to get page from uninitialized WikipageDataManager.
    }

    private boolean haveAttributes(Wikipage wikipage, List<PageAttributes> attributes) {
        boolean haveAttributes = true;
        if(attributes.contains(PageAttributes.title))
        {
            if(wikipage.getTitle() == null)
            {
                return false;
            }

        }
        if(attributes.contains(PageAttributes.url))
        {
            if(wikipage.getTitle() == null)
            {
                return false;
            }
        }
        if(attributes.contains(PageAttributes.content))
        {
            if(wikipage.getTitle() == null)
            {
                return false;
            }
        }
        if(attributes.contains(PageAttributes.description))
        {
            if(wikipage.getTitle() == null)
            {
                return false;
            }
        }
        if(attributes.contains(PageAttributes.categories))
        {
            if(wikipage.getTitle() == null)
            {
                return false;
            }
        }
        if(attributes.contains(PageAttributes.indicators))
        {
            if(wikipage.getTitle() == null)
            {
                return false;
            }
        }
        if(attributes.contains(PageAttributes.watchers))
        {
            if(wikipage.getTitle() == null)
            {
                return false;
            }
        }
        if(attributes.contains(PageAttributes.thumbnail))
        {
            if(wikipage.getTitle() == null)
            {
                return false;
            }
        }
        if(attributes.contains(PageAttributes.coordinates))
        {
            if(wikipage.getTitle() == null)
            {
                return false;
            }
        }
        if(attributes.contains(PageAttributes.audioUrl))
        {
            if(wikipage.getTitle() == null)
            {
                return false;
            }
        }

        return haveAttributes;
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


    public List<Wikipage> searchWikiPageByName(String pageName,
                                               List<PageAttributes> pageAttributes)
            throws IOException
    {
        if(pageName == null || pageAttributes == null || pageAttributes.isEmpty())
        { // nothing to return.
            return new ArrayList<>();
        }
        // gets basic attributes:
        List<Wikipage> wikipages = WikiServerHolder.searchPage(pageName, pageAttributes);
        List<String> pagesNames = new ArrayList<>();
        for(Wikipage wikipage: wikipages)
        {
            pagesNames.add(wikipage.getTitle());
            wikipagesData.put(wikipage.getTitle(), wikipage);
        }

        // get content if asked:
        if(pageAttributes.contains(PageAttributes.content))
        {
            for(String name:pagesNames) // page contents needs to be one by one.
            {
                Wikipage wikipage = wikipagesData.get(name);
                if(wikipage != null) {
                    wikipage.setSections(WikiServerHolder.getInstance().getPageContent(name));
                }
            }
        }

        // get audio source if asked:
        if(pageAttributes.contains(PageAttributes.audioUrl))
        {
                WikiServerHolder.getInstance().loadAudioSource(wikipagesData,
                getSpokenWikipagesMetaData(),
                pagesNames);

        }
        return wikipages;
    }


    public List<Wikipage> getSpokenPagesByCategories(String category,
                                                     List<PageAttributes> pageAttributes)
            throws IOException {
        List<Wikipage> resultWikipages = new ArrayList<>();
        List<String> pagesNames = this.getSpokenCategoriesMetaData()
                .getOrDefault(category, null);
        if(pagesNames == null)
        {
            return new ArrayList<>();
        }
        if(pagesNames.size() > MAX_QUERY_AT_ONCE)
        {
            List<String> subPagesNames = pagesNames.subList(0, MAX_QUERY_AT_ONCE);
            pagesNames = subPagesNames; // todo can this be done in one line?
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

        // load contents:
        if(pageAttributes.contains(PageAttributes.content))
        {
            for(String name:pagesNames) // page contents needs to be one by one.
            {
                Wikipage wikipage = wikipagesData.get(name);
                if(wikipage != null) {
                    wikipage.setSections(WikiServerHolder.getInstance().getPageContent(name));
                }
            }
        }

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
