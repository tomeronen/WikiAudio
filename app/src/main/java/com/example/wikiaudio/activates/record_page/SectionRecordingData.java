package com.example.wikiaudio.activates.record_page;

import java.io.File;

public class SectionRecordingData {
    String sectionTitle;
    File fileRecording;
    long milSeconds;

    public SectionRecordingData(String sectionTitle, File fileRecording, long milSeconds) {
        this.sectionTitle = sectionTitle;
        this.fileRecording = fileRecording;
        this.milSeconds = milSeconds;
    }
}
