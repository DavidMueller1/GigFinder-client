package com.example.david.gigfinder.data;

import com.example.david.gigfinder.data.enums.Genre;
import com.google.android.gms.maps.model.LatLng;

import java.net.URI;
import java.util.ArrayList;

public class Host extends User{

    private LatLng location;
    //private ArrayList locations;

    public Host() {

    }

    public Host(int id, int color, String name, String description, LatLng location, ArrayList<SocialMediaLink> socialMediaLinks, URI imageURI, ArrayList<Genre> genres) {
        super(id, color, name, description, socialMediaLinks, imageURI, genres);
        this.location = location;
    }
}
