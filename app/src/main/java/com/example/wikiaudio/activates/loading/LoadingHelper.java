package com.example.wikiaudio.activates.loading;

import com.example.wikiaudio.wikipedia.Wikipage;

import java.util.ArrayList;
import java.util.List;

/**
 * Helps coordinating the loading screen with it's calling activity
 * ATM works for Wikipage only
 */
public class LoadingHelper {
    private static LoadingHelper loadingHelper = null;
    private static List<Wikipage> wikipages = new ArrayList<>();

    private LoadingHelper(){}

    public static LoadingHelper getInstance() {
        if (loadingHelper == null) {
            loadingHelper = new LoadingHelper();
        }
        return loadingHelper;
    }

    /**
     * @param wikipage the page we would like to help load
     * @return the index of the page in the wikipages list
     */
    public int loadWikipage(Wikipage wikipage) {
        if (wikipage == null){
            return -1;
        }
        wikipages.add(wikipage);
        return wikipages.indexOf(wikipage);
    }

    /**
     * @param index the index of the wikipage element to remove
     * @return true if we removed it successfully, false ow
     */
    public boolean removeWikipageByIndex(int index){
        return (wikipages.remove(index) != null);
    }

    /**
     * @param wikipage the wikipage element to remove
     * @return true if we removed it successfully, false ow
     */
    public boolean removeWikipageByElement(Wikipage wikipage) {
        return wikipages.remove(wikipage);
    }

    /**
     * @param index the index of the wikipage element to fetch
     * @return the wikipage element
     */
    public Wikipage getWikipageByIndex(int index){
        return wikipages.get(index);
    }


}
