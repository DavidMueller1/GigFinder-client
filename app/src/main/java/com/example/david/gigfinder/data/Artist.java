package com.example.david.gigfinder.data;

import com.example.david.gigfinder.data.enums.Genre;

import java.net.URI;
import java.util.ArrayList;

public class Artist extends User {

    //region Attributes and Constructor
    private ArrayList<Genre> genres; //How many?

    public Artist(){
    }

    public Artist(int id, int color, String name, String description, ArrayList<SocialMediaLink> socialMediaLinks, URI imageURI, ArrayList<Genre> genres) {
        super(id, color, name, description, socialMediaLinks, imageURI);
        this.genres = genres;
    }
    //endregion

    //region Getters and Setters
    public ArrayList<Genre> getGenres() {
        return genres;
    }

    public void setGenres(ArrayList<Genre> genres) {
        this.genres = genres;
    }

    //endregion

}
