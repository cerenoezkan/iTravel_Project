package com.example.itravel.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.itravel.Model.Place;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class SimilarPlacesFinder {

    private static final int DEFAULT_LIMIT = 3;

    private SimilarPlacesFinder() {
    }

    @NonNull
    public static List<Place> find(@NonNull Place current, @NonNull List<Place> all) {
        return find(current, all, DEFAULT_LIMIT);
    }

    @NonNull
    public static List<Place> find(@NonNull Place current, @NonNull List<Place> all, int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }

        String currentId = current.getId();
        String category = current.getCategory();
        LatLng currentLat = MapPlaceUtils.latLngFromPlace(current);

        List<Place> istanbul = PlaceFilter.forIstanbul(all);
        List<Place> sameCategory = new ArrayList<>();
        List<Place> other = new ArrayList<>();

        for (Place place : istanbul) {
            if (place.getId() == null || place.getId().isEmpty()) {
                continue;
            }
            if (currentId != null && currentId.equals(place.getId())) {
                continue;
            }
            if (category != null && category.equals(place.getCategory())) {
                sameCategory.add(place);
            } else {
                other.add(place);
            }
        }

        Comparator<Place> score = scoreComparator(currentLat);
        Collections.sort(sameCategory, score);
        Collections.sort(other, score);

        List<Place> result = new ArrayList<>();
        appendUntilLimit(result, sameCategory, limit);
        appendUntilLimit(result, other, limit);
        return result;
    }

    private static void appendUntilLimit(@NonNull List<Place> target,
                                         @NonNull List<Place> source,
                                         int limit) {
        for (Place place : source) {
            if (target.size() >= limit) {
                break;
            }
            target.add(place);
        }
    }

    @NonNull
    private static Comparator<Place> scoreComparator(@Nullable LatLng origin) {
        return (a, b) -> {
            int ratingCmp = Double.compare(b.getRating(), a.getRating());
            if (ratingCmp != 0) {
                return ratingCmp;
            }
            if (origin == null) {
                return 0;
            }
            LatLng la = MapPlaceUtils.latLngFromPlace(a);
            LatLng lb = MapPlaceUtils.latLngFromPlace(b);
            if (la == null || lb == null) {
                return 0;
            }
            return Double.compare(
                    MapPlaceUtils.distanceMeters(origin, la),
                    MapPlaceUtils.distanceMeters(origin, lb));
        };
    }
}
