package com.example.david.gigfinder.data;

import com.example.david.gigfinder.data.enums.Genre;

import java.net.URI;
import java.util.ArrayList;

public class Host extends User{

    private Genre defaultGenre;
    //private ArrayList locations;

    public Host(int id, String name, String description, ArrayList<SocialMediaLink> socialMediaLinks, URI imageURI) {
        super(id, name, description, socialMediaLinks, imageURI);
    }
}
