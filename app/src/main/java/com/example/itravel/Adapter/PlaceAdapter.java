package com.example.itravel.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.itravel.DetailActivity;
import com.example.itravel.Model.Place;
import com.example.itravel.Model.PlaceCategory;
import com.example.itravel.R;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.VH> {

    private final List<Place> placeData;

    public PlaceAdapter(@NonNull List<Place> placeData) {
        this.placeData = placeData;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place_list, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Place place = placeData.get(position);
        String title = place.getTitle() != null ? place.getTitle() : "";
        holder.title.setText(title.isEmpty()
                ? holder.itemView.getContext().getString(R.string.place_detail_untitled)
                : title);

        double rating = place.getRating();
        if (rating > 0) {
            holder.rating.setText(holder.itemView.getContext().getString(
                    R.string.place_rating_format, rating));
        } else {
            holder.rating.setText(R.string.place_rating_none);
        }

        holder.subtitle.setText(PlaceCategory.labelRes(place.getCategory()));

        String img = place.getImageUrl();
        if (img != null && !img.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(img).centerCrop().into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.noimage);
        }

        holder.itemView.setOnClickListener(v -> {
            if (place.getId() != null && !place.getId().isEmpty()) {
                DetailActivity.launch(v.getContext(), place.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return placeData.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView rating;
        TextView subtitle;

        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.place_image);
            title = itemView.findViewById(R.id.place_title);
            rating = itemView.findViewById(R.id.place_rating);
            subtitle = itemView.findViewById(R.id.place_subtitle);
        }
    }
}
