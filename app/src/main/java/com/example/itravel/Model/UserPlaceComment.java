package com.example.itravel.Model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Kullanıcının bir mekâna yazdığı yorum + mekân özeti (profil listesi için).
 */
public class UserPlaceComment {

    private final String placeId;
    private final String placeTitle;
    @Nullable
    private final String placeImageUrl;
    @NonNull
    private final PlaceComment comment;

    public UserPlaceComment(@NonNull String placeId,
                            @Nullable String placeTitle,
                            @Nullable String placeImageUrl,
                            @NonNull PlaceComment comment) {
        this.placeId = placeId;
        this.placeTitle = placeTitle;
        this.placeImageUrl = placeImageUrl;
        this.comment = comment;
    }

    @NonNull
    public String getPlaceId() {
        return placeId;
    }

    @Nullable
    public String getPlaceTitle() {
        return placeTitle;
    }

    @Nullable
    public String getPlaceImageUrl() {
        return placeImageUrl;
    }

    @NonNull
    public PlaceComment getComment() {
        return comment;
    }
}
