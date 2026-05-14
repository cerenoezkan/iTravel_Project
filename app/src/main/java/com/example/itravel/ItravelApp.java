package com.example.itravel;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class ItravelApp extends Application {

    /** Must match the Realtime Database region used in Firebase Console. */
    public static final String FIREBASE_RTDB_URL =
            "https://itravel-app-94fd4-default-rtdb.europe-west1.firebasedatabase.app";

    /** Realtime Database root child for map / discover / detail (see assignment spec). */
    public static final String RTDB_NODE_PLACES = "places";

    @Override
    public void onCreate() {
        super.onCreate();

        if (!FirebaseApp.getApps(this).isEmpty()) {
            return;
        }

        FirebaseOptions options = FirebaseOptions.fromResource(this);
        if (options != null) {
            FirebaseApp.initializeApp(this, options);
            return;
        }

        FirebaseOptions fallbackOptions = new FirebaseOptions.Builder()
                .setApplicationId("1:5970633776:android:33cdf16d9ebece362ff9f5")
                .setApiKey("AIzaSyAQLRy4A5c6PcD2JkxJs2vKUlZhydKbqKU")
                .setProjectId("itravel-app-94fd4")
                .setStorageBucket("itravel-app-94fd4.firebasestorage.app")
                .setGcmSenderId("5970633776")
                .build();
        FirebaseApp.initializeApp(this, fallbackOptions);
    }
}
