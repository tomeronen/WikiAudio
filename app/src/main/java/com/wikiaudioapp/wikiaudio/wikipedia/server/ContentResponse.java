package com.wikiaudioapp.wikiaudio.wikipedia.server;

import java.util.List;

public class ContentResponse {

    public LeadData lead;
    public RemainingData remaining;

    class LeadData {
        Double id;
        String displaytitle;
        List<SectionData> sections;
    }

    public class SectionData {
        Double id;
        String text;
        Double toclevel;
        String line;
        String anchor;
    }

    public class RemainingData {
        List<SectionData> sections;
    }
}
