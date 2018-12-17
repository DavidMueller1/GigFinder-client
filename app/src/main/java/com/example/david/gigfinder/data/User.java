package com.example.david.gigfinder.data;

import java.net.URI;
import java.util.ArrayList;

public class User {

    //region Attributes and Constructor
    private int id, color;
    private String name, description;
    private ArrayList<SocialMediaLink> socialMediaLinks;
    private URI imageURI; //Uri or?

    public User(){
    }

    public User(int id, int color, String name, String description, ArrayList<SocialMediaLink> socialMediaLinks, URI imageURI) {
        this.id = id;
        this.color = color;
        this.name = name;
        this.description = description;
        this.socialMediaLinks = socialMediaLinks;
        this.imageURI = imageURI;
    }
    //endregion

    //region Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<SocialMediaLink> getSocialMediaLinks() {
        return socialMediaLinks;
    }

    public void setSocialMediaLinks(ArrayList<SocialMediaLink> socialMediaLinks) {
        this.socialMediaLinks = socialMediaLinks;
    }

    public URI getImageURI() {
        return imageURI;
    }

    public void setImageURI(URI imageURI) {
        this.imageURI = imageURI;
    }
    //endregion
}
