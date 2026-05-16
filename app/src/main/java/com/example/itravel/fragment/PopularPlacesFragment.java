package com.example.itravel.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.itravel.Adapter.PopularPlaceAdapter;
import com.example.itravel.DetailActivity;
import com.example.itravel.ItravelApp;
import com.example.itravel.Model.Place;
import com.example.itravel.R;
import com.example.itravel.util.PlaceFilter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PopularPlacesFragment extends Fragment implements PopularPlaceAdapter.Listener {

    private DatabaseReference placesRef;
    private ValueEventListener placesListener;
    private PopularPlaceAdapter adapter;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_popular, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_popular);
        emptyView = view.findViewById(R.id.popular_empty);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PopularPlaceAdapter(this);
        rv.setAdapter(adapter);

        placesRef = FirebaseDatabase.getInstance(ItravelApp.FIREBASE_RTDB_URL)
                .getReference(ItravelApp.RTDB_NODE_PLACES);

        placesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Place> all = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Place p = Place.fromSnapshot(child);
                    if (p != null && p.getId() != null && !p.getId().isEmpty()) {
                        all.add(p);
                    }
                }
                List<Place> istanbul = PlaceFilter.forIstanbul(all);
                List<Place> sorted = PlaceFilter.sortByRatingDesc(istanbul);
                adapter.setPlaces(sorted);
                boolean empty = sorted.isEmpty();
                emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
                rv.setVisibility(empty ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                emptyView.setVisibility(View.VISIBLE);
            }
        };
        placesRef.addValueEventListener(placesListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (placesRef != null && placesListener != null) {
            placesRef.removeEventListener(placesListener);
        }
    }

    @Override
    public void onPlaceClick(@NonNull Place place) {
        if (place.getId() != null) {
            DetailActivity.launch(requireContext(), place.getId());
            if (getActivity() != null) {
                getActivity().overridePendingTransition(R.anim.nav_fade_in, R.anim.nav_fade_out);
            }
        }
    }
}
