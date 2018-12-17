package com.example.david.gigfinder.data;

import com.example.david.gigfinder.data.enums.Genre;

import java.net.URI;
import java.util.ArrayList;

public class Artist extends User {

    //region Attributes and Constructor
    private ArrayList<Genre> genres; //How many?
    private int color;
    private int fontColor;

    public Artist(){
    }

    public Artist(int id, String name, String description, ArrayList<SocialMediaLink> socialMediaLinks, URI imageURI, ArrayList<Genre> genres, int color, int fontColor) {
        super(id, name, description, socialMediaLinks, imageURI);
        this.genres = genres;
        this.color = color;
        this.fontColor = fontColor;
    }
    //endregion

    //region Getters and Setters
    public ArrayList<Genre> getGenres() {
        return genres;
    }

    public void setGenres(ArrayList<Genre> genres) {
        this.genres = genres;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getFontColor() {
        return fontColor;
    }

    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
    }

    //endregion

}
