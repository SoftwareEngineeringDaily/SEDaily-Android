package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

public class DownloadHelper {
    private static DownloadHelper instance = null;


    public static DownloadHelper getInstance() {
        if (instance == null) {
            instance = new DownloadHelper();
        }

        return instance;
    }
}
