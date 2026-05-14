package com.example.itravel.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.itravel.Model.Place;
import com.example.itravel.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminPlaceAdapter extends RecyclerView.Adapter<AdminPlaceAdapter.VH> {

    public interface Listener {
        void onEdit(@NonNull Place place);

        void onDelete(@NonNull Place place);
    }

    private final List<Place> data = new ArrayList<>();
    private final Listener listener;

    public AdminPlaceAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setPlaces(@NonNull List<Place> places) {
        data.clear();
        data.addAll(places);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_place, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Place p = data.get(position);
        holder.title.setText(p.getTitle() != null && !p.getTitle().isEmpty() ? p.getTitle() : holder.itemView.getContext().getString(R.string.place_detail_untitled));
        String lat = p.getLatitude() != null ? p.getLatitude() : "";
        String lon = p.getLongitude() != null ? p.getLongitude() : "";
        holder.subtitle.setText(lat + ", " + lon);

        holder.edit.setOnClickListener(v -> listener.onEdit(p));
        holder.delete.setOnClickListener(v -> listener.onDelete(p));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        TextView subtitle;
        MaterialButton edit;
        MaterialButton delete;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.admin_item_title);
            subtitle = itemView.findViewById(R.id.admin_item_subtitle);
            edit = itemView.findViewById(R.id.admin_item_edit);
            delete = itemView.findViewById(R.id.admin_item_delete);
        }
    }
}
