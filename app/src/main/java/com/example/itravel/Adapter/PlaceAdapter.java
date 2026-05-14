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
import com.example.itravel.R;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.MyViewHolder> {

    private final List<Place> placeData;

    public PlaceAdapter(List<Place> placeData) {
        this.placeData = placeData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_items, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Place place = placeData.get(position);
        holder.place_txt_v.setText(place.getTitle());
        holder.lat_txt_v.setText(place.getLatitude());
        holder.lng_txt_v.setText(place.getLongitude());
        Glide.with(holder.itemView.getContext())
                .load(place.getImageUrl())
                .into(holder.post_img_v);

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

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView post_img_v;
        TextView place_txt_v, lat_txt_v, lng_txt_v;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            post_img_v = itemView.findViewById(R.id.post_img_v);
            place_txt_v = itemView.findViewById(R.id.place_txt_v);
            lat_txt_v = itemView.findViewById(R.id.lat_txt_v);
            lng_txt_v = itemView.findViewById(R.id.lng_txt_v);
        }
    }
}
