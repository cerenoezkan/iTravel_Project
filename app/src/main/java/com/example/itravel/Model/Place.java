package com.example.itravel.Model;

import com.google.firebase.database.DataSnapshot;

/**
 * Firebase Realtime Database node: places/{id}
 */
public class Place {

    private String id;
    private String title;
    private String description;
    private String latitude;
    private String longitude;
    private String imageUrl;
    private String city;
    private String category;
    private double rating;

    public Place() {
    }

    public Place(String id, String title, String description, String latitude, String longitude,
                 String imageUrl, String city, String category, double rating) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.city = city;
        this.category = category;
        this.rating = rating;
    }

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

        String city = asText(snap.child("city").getValue());
        p.setCity(city.isEmpty() ? PlaceCategory.CITY_ISTANBUL : city);

        String category = asText(snap.child("category").getValue());
        p.setCategory(category.isEmpty() ? PlaceCategory.HISTORICAL : category);

        p.setRating(ratingFromSnapshot(snap.child("rating").getValue()));
        return p;
    }

    private static double ratingFromSnapshot(Object value) {
        if (value == null) {
            return 0d;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        }
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble(((String) value).trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                return 0d;
            }
        }
        return 0d;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
