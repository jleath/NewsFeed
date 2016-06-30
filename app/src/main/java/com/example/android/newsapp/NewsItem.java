package com.example.android.newsapp;

/**
 * A class to store information about news articles.
 */
public class NewsItem {

    private String mImageResourceUrl;
    private String mTitle;
    private String mSection;
    private String mContentUrl;

    public NewsItem(String imageResUrl, String title, String section, String contentUrl) {
        mImageResourceUrl = imageResUrl;
        mTitle = title;
        mSection = section;
        mContentUrl = contentUrl;
    }

    public String getImageResourceUrl() {
        return mImageResourceUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSection() {
        return mSection;
    }

    public String getContentUrl() {
        return mContentUrl;
    }

}
