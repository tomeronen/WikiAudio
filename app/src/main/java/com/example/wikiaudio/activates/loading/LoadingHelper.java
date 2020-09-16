package com.example.wikiaudio.activates.loading;

import com.example.wikiaudio.wikipedia.Wikipage;

import java.util.ArrayList;
import java.util.List;

public class LoadingHelper {
    private static LoadingHelper loadingHelper = null;
    List<Wikipage> wikipages = new ArrayList<>();

    private LoadingHelper(){}

    public static LoadingHelper getInstance() {
        if (loadingHelper == null) {
            loadingHelper = new LoadingHelper();
        }
        return loadingHelper;
    }

    public int loadWikipage(Wikipage wikipage) {
        if (wikipage == null){
            return -1;
        }
        wikipages.add(wikipage);
        return wikipages.indexOf(wikipage);
    }

    public boolean removeWikipage(int index){
        Wikipage wikipage = wikipages.remove(index);
        return (wikipage != null);
    }

    public Wikipage getWikipageByIndex(int index){
        return wikipages.get(index);
    }


}
