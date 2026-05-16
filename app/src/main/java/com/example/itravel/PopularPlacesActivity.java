package com.example.itravel;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.itravel.fragment.PopularPlacesFragment;

/**
 * Popüler mekânlar ekranı. Ana akışta {@link HomeActivity} bottom navigation
 * {@link PopularPlacesFragment} kullanır; bu activity bağımsız açılış için tanımlıdır.
 */
public class PopularPlacesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_host);

        if (savedInstanceState == null) {
            Fragment fragment = new PopularPlacesFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.popular_host_container, fragment)
                    .commit();
        }
    }
}
