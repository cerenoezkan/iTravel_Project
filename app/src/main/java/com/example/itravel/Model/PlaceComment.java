package com.example.itravel.Model;

import com.google.firebase.database.DataSnapshot;

/**
 * places/{placeId}/comments/{commentId}
 */
public class PlaceComment {

    private String id;
    private String userId;
    private String username;
    private String comment;
    private int stars;
    private long timestamp;

    public PlaceComment() {
    }

    public static PlaceComment fromSnapshot(DataSnapshot snap) {
        if (snap == null || !snap.exists()) {
            return null;
        }
        PlaceComment c = new PlaceComment();
        c.setId(snap.getKey());
        c.setUserId(asText(snap.child("userId").getValue()));
        c.setUsername(asText(snap.child("username").getValue()));
        c.setComment(asText(snap.child("comment").getValue()));
        c.setStars(starsFromSnapshot(snap.child("stars").getValue()));
        c.setTimestamp(timestampFromSnapshot(snap.child("timestamp").getValue()));
        return c;
    }

    private static int starsFromSnapshot(Object value) {
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Double) {
            return ((Double) value).intValue();
        }
        return 0;
    }

    private static long timestampFromSnapshot(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof Double) {
            return ((Double) value).longValue();
        }
        return 0L;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
