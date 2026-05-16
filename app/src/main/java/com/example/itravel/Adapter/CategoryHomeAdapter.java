package com.example.itravel.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.itravel.Model.PlaceCategory;
import com.example.itravel.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

public class CategoryHomeAdapter extends RecyclerView.Adapter<CategoryHomeAdapter.VH> {

    public interface Listener {
        void onCategoryClick(@NonNull String categoryKey);

        void onMapClick(@NonNull String categoryKey);
    }

    private final Listener listener;

    public CategoryHomeAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Context context = holder.itemView.getContext();
        String key = PlaceCategory.ALL_KEYS[position];

        holder.title.setText(context.getString(PlaceCategory.labelRes(key)));
        holder.desc.setText(context.getString(PlaceCategory.descriptionRes(key)));

        Glide.with(context)
                .load(PlaceCategory.thumbnailRes(key))
                .centerCrop()
                .placeholder(R.drawable.noimage)
                .into(holder.icon);

        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(key));
        holder.mapBtn.setOnClickListener(v -> listener.onMapClick(key));
    }

    @Override
    public int getItemCount() {
        return PlaceCategory.ALL_KEYS.length;
    }

    static class VH extends RecyclerView.ViewHolder {
        ShapeableImageView icon;
        TextView title;
        TextView desc;
        MaterialButton mapBtn;

        VH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.category_icon);
            title = itemView.findViewById(R.id.category_title);
            desc = itemView.findViewById(R.id.category_desc);
            mapBtn = itemView.findViewById(R.id.btn_map_category);
        }
    }
}
