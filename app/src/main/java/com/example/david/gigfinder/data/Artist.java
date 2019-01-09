package com.example.david.gigfinder.data;

import com.example.david.gigfinder.data.enums.Genre;

import java.net.URI;
import java.util.ArrayList;

public class Artist extends User {

    //region Attributes and Constructor

    public Artist(){
    }

    public Artist(int id, int color, String name, String description, ArrayList<SocialMediaLink> socialMediaLinks, URI imageURI, ArrayList<Genre> genres) {
        super(id, color, name, description, socialMediaLinks, imageURI, genres);
    }
    //endregion

    //region Getters and Setters


    //endregion

}
