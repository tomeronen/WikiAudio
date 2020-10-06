package com.wikiaudioapp.wikiaudio.wikipedia.server;

import android.util.Log;

import com.wikiaudioapp.wikiaudio.wikipedia.wikipage.PageAttributes;
import com.wikiaudioapp.wikiaudio.wikipedia.wikipage.Wikipage;

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
 * serves as a cache of wikipages data.
 * if we already have some or all wanted data on a asked wikipage, use it.
 * only ask WikiServerHolder to bring us the missing data.
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

    /**
     * get a Wikipage by its name/title.
     * @param name the name of wiki page to get.
     * @param attributes the Attributes to get on the page.
     * @return the found wikipage
     * @throws IOException if there was a IO problem with wiki server.
     */
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

    /**
     * checks if the wikipage has the attributes we want/need.
     * @param wikipage the wikipage to check.
     * @param attributes the attributes we want.
     * @return true if he does.
     */
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

    /**
     * adds a wikipage to our saved wikipages data.
     * @param wikipage the wikipage to be added.
     */
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
     * loadSpokenWikipagesMetaData
     * @throws IOException if there was a IO problem with wiki server.
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

    /**
     * @return the Document jsoup object of the spoken wikipages.
     * @throws IOException if there was a IO problem with wiki server.
     */
    private Document getSpokenDocument()
            throws IOException {
        if (spokenDoc != null) {
            return spokenDoc;
        } else {
                spokenDoc = Jsoup.connect(SPOKEN_URL).get();
                return spokenDoc;
        }
    }

    /**
     * Search a name for relevant wiki pages.
     * Fills list to fill with relevant wiki pages with name and id data.
     * Does not handle bad writing.
     * (if you now the exact name of the page you want use `getPage()` ).
     * @param pageName the value to search.
     * @param pageAttributes currently not in use. can be null.
     * @return list of pages found.
     * @throws IOException if there was a IO problem with wiki server.
     */
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


     /**
     * load Spoken Pages By Categories.
     * @param category the category to bring the pages from.
     * @param pageAttributes what Attributes to bring on result pages.
     * @return a list with found pages.
     * @throws IOException if there was a IO problem with wiki server.
     */
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

    /**
     *
     * @return
     * @throws IOException if there was a problem with the connection to wikipedia.
     */
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

    /**
     * gets Wikipages nearby the given coordinates. if successful,
     * returns the 'listToFill' with found pages. And then runs workerListener.
     * @param latitude the latitude to preform the search on.
     * @param longitude the longitude to preform the search on.
     * @param radius the radius from coordinates to search in.
     * @param pageAttributes the attributes to get on each wiki page found.
     * @return a list with the wiki pages found.
     * @throws IOException if there was a problem with the connection to wikipedia.
     */
    public List<Wikipage> searchPagesNearby(double latitude,
                                          double longitude,
                                          int radius,
                                          List<PageAttributes> pageAttributes)
            throws IOException {


        if(radius == 0 || pageAttributes == null || pageAttributes.isEmpty())
        { // nothing to search.
            return new ArrayList<>();
        }

        // gets basic attributes:
        List<Wikipage> wikipages = WikiServerHolder.getPagesNearby(latitude,
                longitude,
                radius,
                pageAttributes);
        if(wikipages.isEmpty()) // nothing found
        {
            return new ArrayList<>();
        }

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
}
