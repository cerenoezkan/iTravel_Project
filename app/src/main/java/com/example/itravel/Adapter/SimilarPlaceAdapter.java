package com.example.itravel.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.itravel.Model.Place;
import com.example.itravel.Model.PlaceCategory;
import com.example.itravel.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class SimilarPlaceAdapter extends RecyclerView.Adapter<SimilarPlaceAdapter.VH> {

    public interface Listener {
        void onPlaceClick(@NonNull Place place);
    }

    private final List<Place> places = new ArrayList<>();
    private final Listener listener;

    public SimilarPlaceAdapter(@NonNull Listener listener) {
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_similar_place, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Place place = places.get(position);
        String title = place.getTitle();
        holder.title.setText(title == null || title.isEmpty()
                ? holder.itemView.getContext().getString(R.string.place_detail_untitled)
                : title);
        holder.category.setText(holder.itemView.getContext().getString(
                PlaceCategory.labelRes(place.getCategory())));

        String img = place.getImageUrl();
        Glide.with(holder.image.getContext())
                .load(img != null && !img.isEmpty() ? img : null)
                .centerCrop()
                .placeholder(R.drawable.noimage)
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> listener.onPlaceClick(place));
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ShapeableImageView image;
        TextView title;
        Chip category;

        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.similar_place_image);
            title = itemView.findViewById(R.id.similar_place_title);
            category = itemView.findViewById(R.id.similar_place_category);
        }
    }
}
