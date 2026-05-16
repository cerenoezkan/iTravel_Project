package com.example.itravel.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.itravel.Model.Place;
import com.example.itravel.Model.PlaceCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PlaceFilter {

    private PlaceFilter() {
    }

    @NonNull
    public static List<Place> forIstanbul(@NonNull List<Place> all) {
        List<Place> out = new ArrayList<>();
        for (Place p : all) {
            if (p == null || p.getId() == null || p.getId().isEmpty()) {
                continue;
            }
            String city = p.getCity();
            if (city == null || city.isEmpty() || PlaceCategory.CITY_ISTANBUL.equalsIgnoreCase(city)) {
                out.add(p);
            }
        }
        return out;
    }

    @NonNull
    public static List<Place> byCategory(@NonNull List<Place> all, @Nullable String categoryKey) {
        List<Place> istanbul = forIstanbul(all);
        if (categoryKey == null || categoryKey.isEmpty()) {
            return istanbul;
        }
        List<Place> out = new ArrayList<>();
        for (Place p : istanbul) {
            if (categoryKey.equals(p.getCategory())) {
                out.add(p);
            }
        }
        return out;
    }

    @NonNull
    public static List<Place> sortByRatingDesc(@NonNull List<Place> places) {
        List<Place> sorted = new ArrayList<>(places);
        Collections.sort(sorted, (a, b) -> Double.compare(b.getRating(), a.getRating()));
        return sorted;
    }
}
