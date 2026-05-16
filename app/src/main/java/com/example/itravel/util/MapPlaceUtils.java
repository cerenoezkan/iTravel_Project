package com.example.itravel.util;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.itravel.Model.Place;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class MapPlaceUtils {

    private MapPlaceUtils() {
    }

    public static double distanceMeters(@NonNull LatLng from, @NonNull LatLng to) {
        float[] results = new float[1];
        Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, results);
        return results[0];
    }

  /** Visible map area radius from zoom level (approximate). */
    public static double radiusMetersForZoom(float zoom) {
        double scale = Math.pow(2, 16 - zoom);
        return Math.max(800d, Math.min(12000d, 2500d * scale));
    }

    @NonNull
    public static List<Place> nearbyPlaces(
            @NonNull List<Place> all,
            @NonNull LatLng center,
            double radiusMeters,
            int maxCount) {
        List<PlaceDistance> ranked = new ArrayList<>();
        for (Place place : all) {
            LatLng latLng = latLngFromPlace(place);
            if (latLng == null) {
                continue;
            }
            double distance = distanceMeters(center, latLng);
            if (distance <= radiusMeters) {
                ranked.add(new PlaceDistance(place, distance));
            }
        }
        Collections.sort(ranked, Comparator.comparingDouble(a -> a.distance));
        List<Place> out = new ArrayList<>();
        int limit = Math.min(maxCount, ranked.size());
        for (int i = 0; i < limit; i++) {
            out.add(ranked.get(i).place);
        }
        return out;
    }

    @Nullable
    public static LatLng latLngFromPlace(@NonNull Place place) {
        try {
            String la = place.getLatitude();
            String lo = place.getLongitude();
            if (la == null || lo == null) {
                return null;
            }
            la = la.trim();
            lo = lo.trim();
            if (la.isEmpty() || lo.isEmpty()) {
                return null;
            }
            return new LatLng(Double.parseDouble(la), Double.parseDouble(lo));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static final class PlaceDistance {
        final Place place;
        final double distance;

        PlaceDistance(Place place, double distance) {
            this.place = place;
            this.distance = distance;
        }
    }
}
