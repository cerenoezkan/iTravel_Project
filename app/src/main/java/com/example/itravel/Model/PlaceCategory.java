package com.example.itravel.Model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.example.itravel.R;

/**
 * Fixed Istanbul place categories stored in Firebase as {@code category} field.
 */
public final class PlaceCategory {

    public static final String CITY_ISTANBUL = "Istanbul";

    public static final String HISTORICAL = "historical";
    public static final String MUSEUMS = "museums";
    public static final String NATURE = "nature";
    public static final String FREE = "free";
    public static final String CAFES_RESTAURANTS = "cafes_restaurants";

    public static final String[] ALL_KEYS = {
            HISTORICAL,
            MUSEUMS,
            NATURE,
            FREE,
            CAFES_RESTAURANTS
    };

    private PlaceCategory() {
    }

    @StringRes
    public static int labelRes(@Nullable String key) {
        if (key == null) {
            return R.string.category_other;
        }
        switch (key) {
            case HISTORICAL:
                return R.string.category_historical;
            case MUSEUMS:
                return R.string.category_museums;
            case NATURE:
                return R.string.category_nature;
            case FREE:
                return R.string.category_free;
            case CAFES_RESTAURANTS:
                return R.string.category_cafes;
            default:
                return R.string.category_other;
        }
    }

    @StringRes
    public static int descriptionRes(@Nullable String key) {
        if (key == null) {
            return R.string.category_other_desc;
        }
        switch (key) {
            case HISTORICAL:
                return R.string.category_historical_desc;
            case MUSEUMS:
                return R.string.category_museums_desc;
            case NATURE:
                return R.string.category_nature_desc;
            case FREE:
                return R.string.category_free_desc;
            case CAFES_RESTAURANTS:
                return R.string.category_cafes_desc;
            default:
                return R.string.category_other_desc;
        }
    }

    /** Circular thumbnail on home category cards. */
    public static int thumbnailRes(@Nullable String key) {
        if (key == null) {
            return R.drawable.cat_tarihi;
        }
        switch (key) {
            case MUSEUMS:
                return R.drawable.cat_muze;
            case NATURE:
                return R.drawable.cat_doga;
            case FREE:
                return R.drawable.cat_ucretsiz;
            case CAFES_RESTAURANTS:
                return R.drawable.cat_cafe;
            case HISTORICAL:
            default:
                return R.drawable.cat_tarihi;
        }
    }

    public static boolean isValid(@Nullable String key) {
        if (key == null) {
            return false;
        }
        for (String k : ALL_KEYS) {
            if (k.equals(key)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    public static String normalize(@Nullable String key) {
        return isValid(key) ? key : HISTORICAL;
    }
}
