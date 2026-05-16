package com.example.itravel.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.itravel.Adapter.CategoryHomeAdapter;
import com.example.itravel.CategoryPlacesActivity;
import com.example.itravel.MapActivity;
import com.example.itravel.R;
import com.example.itravel.SessionManager;
import com.google.android.material.button.MaterialButton;

public class DiscoverFragment extends Fragment implements CategoryHomeAdapter.Listener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton logout = view.findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
            if (getActivity() != null) {
                SessionManager.signOutAndReturnToRoleSelection(getActivity());
            }
        });

        RecyclerView rvCategories = view.findViewById(R.id.rv_categories);
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategories.setHasFixedSize(true);
        rvCategories.setItemAnimator(null);
        rvCategories.setAdapter(new CategoryHomeAdapter(this));
    }

    @Override
    public void onCategoryClick(@NonNull String categoryKey) {
        Intent i = new Intent(requireContext(), CategoryPlacesActivity.class);
        i.putExtra(CategoryPlacesActivity.EXTRA_CATEGORY, categoryKey);
        startActivity(i);
        if (getActivity() != null) {
            getActivity().overridePendingTransition(R.anim.nav_fade_in, R.anim.nav_fade_out);
        }
    }

    @Override
    public void onMapClick(@NonNull String categoryKey) {
        MapActivity.launch(requireContext(), categoryKey);
        if (getActivity() != null) {
            getActivity().overridePendingTransition(R.anim.nav_fade_in, R.anim.nav_fade_out);
        }
    }
}
