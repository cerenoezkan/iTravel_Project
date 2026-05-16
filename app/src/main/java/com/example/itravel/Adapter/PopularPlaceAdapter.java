package com.example.itravel.Adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.itravel.Model.Place;
import com.example.itravel.Model.PlaceCategory;
import com.example.itravel.R;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class PopularPlaceAdapter extends RecyclerView.Adapter<PopularPlaceAdapter.VH> {

    public interface Listener {
        void onPlaceClick(@NonNull Place place);
    }

    private final List<Place> places = new ArrayList<>();
    private final Listener listener;

    public PopularPlaceAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void setPlaces(@NonNull List<Place> items) {
        places.clear();
        places.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_popular_place, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Place place = places.get(position);
        String title = nz(place.getTitle());
        holder.title.setText(title.isEmpty()
                ? holder.itemView.getContext().getString(R.string.place_detail_untitled)
                : title);

        holder.category.setText(holder.itemView.getContext().getString(
                PlaceCategory.labelRes(place.getCategory())));

        if (place.getRating() > 0) {
            holder.rating.setText(holder.itemView.getContext().getString(
                    R.string.place_rating_format, place.getRating()));
        } else {
            holder.rating.setText(R.string.place_rating_none);
        }

        String desc = nz(place.getDescription());
        if (desc.isEmpty()) {
            holder.description.setText(R.string.place_detail_no_description);
        } else {
            holder.description.setText(desc);
        }

        String img = nz(place.getImageUrl());
        Glide.with(holder.image.getContext())
                .load(img.isEmpty() ? null : img)
                .centerCrop()
                .placeholder(R.drawable.noimage)
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> listener.onPlaceClick(place));
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    private static String nz(String s) {
        return s != null ? s : "";
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        Chip category;
        TextView rating;
        TextView description;

        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.popular_image);
            title = itemView.findViewById(R.id.popular_title);
            category = itemView.findViewById(R.id.popular_category);
            rating = itemView.findViewById(R.id.popular_rating);
            description = itemView.findViewById(R.id.popular_description);
        }
    }
}
