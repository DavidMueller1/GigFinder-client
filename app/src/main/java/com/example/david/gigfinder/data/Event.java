package com.example.david.gigfinder.data;

import com.example.david.gigfinder.data.enums.Genre;
import com.google.android.gms.location.places.Place;

import java.sql.Timestamp;
import java.util.ArrayList;

public class Event {

    //region Attributes and Constructor
    private int id;
    private String title, description;
    private ArrayList<Genre> genres;
    private Place location;
    private Timestamp timeFrom;
    private Timestamp timeTo;
    private Host host;

    public Event(int id, String title, String description, ArrayList<Genre> genres, Place location, Timestamp timeFrom, Timestamp timeTo, Host host) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.genres = genres;
        this.location = location;
        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
        this.host = host;
    }
    //endregion

    //region Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Genre> getGenres() {
        return genres;
    }

    public Place getLocation() {
        return location;
    }

    public void setLocation(Place location) {
        this.location = location;
    }

    public void setGenres(ArrayList<Genre> genres) {
        this.genres = genres;
    }

    public Timestamp getTimeFrom() {
        return timeFrom;
    }

    public void setTimeFrom(Timestamp timeFrom) {
        this.timeFrom = timeFrom;
    }

    public Timestamp getTimeTo() {
        return timeTo;
    }

    public void setTimeTo(Timestamp timeTo) {
        this.timeTo = timeTo;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }
    //endregion
}
