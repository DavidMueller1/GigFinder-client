package com.example.david.gigfinder.data;

import android.graphics.Color;

import com.example.david.gigfinder.data.enums.Genre;

import java.net.URI;
import java.util.ArrayList;

public class Artist extends User {

    //region Attributes and Constructor
    private ArrayList<Genre> genres; //How many?
    private Color color;

    public Artist(int id, String name, String description, ArrayList<SocialMediaLink> socialMediaLinks, URI imageURI, ArrayList<Genre> genres, Color color) {
        super(id, name, description, socialMediaLinks, imageURI);
        this.genres = genres;
        this.color = color;
    }
    //endregion

    //region Getters and Setters
    public ArrayList<Genre> getGenres() {
        return genres;
    }

    public void setGenres(ArrayList<Genre> genres) {
        this.genres = genres;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    //endregion

}
