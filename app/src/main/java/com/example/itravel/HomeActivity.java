package com.example.itravel;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.IdRes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.itravel.fragment.DiscoverFragment;
import com.example.itravel.fragment.MapBrowseFragment;
import com.example.itravel.fragment.PopularPlacesFragment;
import com.example.itravel.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG_DISCOVER = "tag_discover";
    private static final String TAG_MAP = "tag_map";
    private static final String TAG_POPULAR = "tag_popular";
    private static final String TAG_PROFILE = "tag_profile";
    private static final String STATE_NAV_ITEM = "state_nav_item";

    private Fragment discoverFragment;
    private Fragment mapFragment;
    private Fragment popularFragment;
    private Fragment profileFragment;
    private Fragment activeFragment;

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, R.string.session_expired, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
            return;
        }

        if (!SessionManager.isUserRole(this)) {
            if (SessionManager.isAdminSession(this)) {
                SessionManager.launchAdminPanel(this);
                return;
            }
            Toast.makeText(this, R.string.session_expired, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main_container);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        @IdRes int initialTab = R.id.nav_discover;
        if (savedInstanceState == null) {
            discoverFragment = new DiscoverFragment();
            mapFragment = new MapBrowseFragment();
            popularFragment = new PopularPlacesFragment();
            profileFragment = new ProfileFragment();
            activeFragment = discoverFragment;

            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .add(R.id.main_fragment_container, profileFragment, TAG_PROFILE).hide(profileFragment)
                    .add(R.id.main_fragment_container, popularFragment, TAG_POPULAR).hide(popularFragment)
                    .add(R.id.main_fragment_container, mapFragment, TAG_MAP).hide(mapFragment)
                    .add(R.id.main_fragment_container, discoverFragment, TAG_DISCOVER)
                    .commit();
        } else {
            FragmentManager fm = getSupportFragmentManager();
            discoverFragment = fm.findFragmentByTag(TAG_DISCOVER);
            mapFragment = fm.findFragmentByTag(TAG_MAP);
            popularFragment = fm.findFragmentByTag(TAG_POPULAR);
            profileFragment = fm.findFragmentByTag(TAG_PROFILE);
            initialTab = savedInstanceState.getInt(STATE_NAV_ITEM, R.id.nav_discover);
            activeFragment = resolveFragment(initialTab);
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });

        if (savedInstanceState != null && activeFragment != null) {
            bottomNavigation.setSelectedItemId(initialTab);
        } else {
            bottomNavigation.setSelectedItemId(R.id.nav_discover);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_NAV_ITEM, bottomNavigation.getSelectedItemId());
    }

    private void showTab(int menuItemId) {
        Fragment target = resolveFragment(menuItemId);
        if (target == null || target == activeFragment) {
            return;
        }

        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.setCustomAnimations(R.anim.nav_fade_in, R.anim.nav_fade_out);
        if (activeFragment != null) {
            tx.hide(activeFragment);
        }
        tx.show(target);
        tx.commit();
        activeFragment = target;
    }

    @Override
    public void onBackPressed() {
        if (bottomNavigation.getSelectedItemId() != R.id.nav_discover) {
            bottomNavigation.setSelectedItemId(R.id.nav_discover);
            return;
        }
        super.onBackPressed();
    }

    private Fragment resolveFragment(int menuItemId) {
        if (menuItemId == R.id.nav_discover) {
            return ensureFragment(TAG_DISCOVER, DiscoverFragment::new, discoverFragment);
        }
        if (menuItemId == R.id.nav_map) {
            return ensureFragment(TAG_MAP, MapBrowseFragment::new, mapFragment);
        }
        if (menuItemId == R.id.nav_popular) {
            return ensureFragment(TAG_POPULAR, PopularPlacesFragment::new, popularFragment);
        }
        if (menuItemId == R.id.nav_profile) {
            return ensureFragment(TAG_PROFILE, ProfileFragment::new, profileFragment);
        }
        return null;
    }

  @NonNull
    private Fragment ensureFragment(@NonNull String tag,
                                    @NonNull FragmentFactory factory,
                                    Fragment cached) {
        if (cached != null) {
            return cached;
        }
        FragmentManager fm = getSupportFragmentManager();
        Fragment found = fm.findFragmentByTag(tag);
        if (found != null) {
            cacheFragment(tag, found);
            return found;
        }
        Fragment created = factory.create();
        fm.beginTransaction()
                .add(R.id.main_fragment_container, created, tag)
                .hide(created)
                .commit();
        cacheFragment(tag, created);
        return created;
    }

    private void cacheFragment(@NonNull String tag, @NonNull Fragment fragment) {
        switch (tag) {
            case TAG_DISCOVER:
                discoverFragment = fragment;
                break;
            case TAG_MAP:
                mapFragment = fragment;
                break;
            case TAG_POPULAR:
                popularFragment = fragment;
                break;
            case TAG_PROFILE:
                profileFragment = fragment;
                break;
            default:
                break;
        }
    }

    private interface FragmentFactory {
        Fragment create();
    }
}
