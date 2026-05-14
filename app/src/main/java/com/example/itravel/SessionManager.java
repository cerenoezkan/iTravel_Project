package com.example.itravel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Rol bilgisi SharedPreferences üzerinde tutulur; yönetici doğrulaması için
 * Firebase Auth e-postası {@link #ADMIN_EMAIL} ile eşleştirilir.
 */
public final class SessionManager {

    public static final String ADMIN_EMAIL = "admin@gmail.com";

    private static final String PREF = "itravel_auth";
    private static final String KEY_ROLE = "role";

    public static final int ROLE_NONE = 0;
    public static final int ROLE_USER = 1;
    public static final int ROLE_ADMIN = 2;

    private SessionManager() {
    }

    /** Oturumu kapatır ve rol seçim ekranına döner. */
    public static void signOutAndReturnToRoleSelection(Activity activity) {
        FirebaseAuth.getInstance().signOut();
        clear(activity.getApplicationContext());
        activity.startActivity(new Intent(activity, RoleSelectionActivity.class));
        activity.finish();
    }

    private static android.content.SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static void clear(Context context) {
        prefs(context).edit().clear().apply();
    }

    public static void setUserRole(Context context) {
        prefs(context).edit().putInt(KEY_ROLE, ROLE_USER).apply();
    }

    public static void setAdminRole(Context context) {
        prefs(context).edit().putInt(KEY_ROLE, ROLE_ADMIN).apply();
    }

    public static int getRole(Context context) {
        return prefs(context).getInt(KEY_ROLE, ROLE_NONE);
    }

    public static boolean isUserRole(Context context) {
        return getRole(context) == ROLE_USER;
    }

    public static boolean isAdminRole(Context context) {
        return getRole(context) == ROLE_ADMIN;
    }

    public static boolean isCurrentUserAdminEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            return false;
        }
        return ADMIN_EMAIL.equalsIgnoreCase(user.getEmail().trim());
    }

    /** Oturum hem rol kaydında admin hem de Auth e-postası admin olmalı. */
    public static boolean isAdminSession(Context context) {
        return isAdminRole(context) && isCurrentUserAdminEmail();
    }
}
