package com.example.itravel.Model;

import com.google.firebase.database.DataSnapshot;

/**
 * Firebase Realtime Database node: places/{id}
 * Fields: id, title, description, latitude, longitude, imageUrl
 */
public class Place {

    private String id;
    private String title;
    private String description;
    private String latitude;
    private String longitude;
    private String imageUrl;

    public Place() {
    }

    public Place(String id, String title, String description, String latitude, String longitude, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
    }

    /**
     * Reads a place from RTDB; supports latitude/longitude stored as String or Number.
     */
    public static Place fromSnapshot(DataSnapshot snap) {
        if (snap == null || !snap.exists()) {
            return null;
        }
        Place p = new Place();
        p.setId(snap.getKey());
        String title = asText(snap.child("title").getValue());
        if (title.isEmpty()) {
            title = asText(snap.child("place").getValue());
        }
        p.setTitle(title);
        p.setDescription(asNullableText(snap.child("description").getValue()));
        p.setLatitude(coordToString(snap.child("latitude").getValue()));
        p.setLongitude(coordToString(snap.child("longitude").getValue()));
        String imageUrl = asText(snap.child("imageUrl").getValue());
        if (imageUrl.isEmpty()) {
            imageUrl = asText(snap.child("image").getValue());
        }
        p.setImageUrl(imageUrl);
        return p;
    }

    private static String asText(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            return (String) value;
        }
        return String.valueOf(value);
    }

    private static String asNullableText(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return String.valueOf(value);
    }

    private static String coordToString(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Double) {
            return String.valueOf(value);
        }
        if (value instanceof Long) {
            return String.valueOf(((Long) value).doubleValue());
        }
        if (value instanceof Integer) {
            return String.valueOf(((Integer) value).doubleValue());
        }
        if (value instanceof String) {
            return ((String) value).trim();
        }
        return String.valueOf(value);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
