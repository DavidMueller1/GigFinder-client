package com.example.david.gigfinder.data;

import com.example.david.gigfinder.data.enums.SocialMedia;

import java.net.URL;

public class SocialMediaLink {

    //region Attributes and Constructor
    private SocialMedia socialMedia;
    private URL url;

    public SocialMediaLink(SocialMedia socialMedia, URL url) {
        this.socialMedia = socialMedia;
        this.url = url;
    }
    //endregion

    //region Getters and Setters
    public SocialMedia getSocialMedia() {
        return socialMedia;
    }

    public void setSocialMedia(SocialMedia socialMedia) {
        this.socialMedia = socialMedia;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
    //endregion
}

