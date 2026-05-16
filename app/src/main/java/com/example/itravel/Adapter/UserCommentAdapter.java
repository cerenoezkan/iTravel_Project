package com.example.itravel.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.itravel.Model.UserPlaceComment;
import com.example.itravel.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserCommentAdapter extends RecyclerView.Adapter<UserCommentAdapter.VH> {

    public interface Listener {
        void onCommentClick(@NonNull UserPlaceComment item);
    }

    private final List<UserPlaceComment> items = new ArrayList<>();
    private final Listener listener;
    private final DateFormat dateFormat =
            DateFormat.getDateInstance(DateFormat.MEDIUM, new Locale("tr", "TR"));

    public UserCommentAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void setItems(@NonNull List<UserPlaceComment> comments) {
        items.clear();
        items.addAll(comments);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_comment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        UserPlaceComment item = items.get(position);
        String title = item.getPlaceTitle();
        if (title == null || title.isEmpty()) {
            title = holder.itemView.getContext().getString(R.string.place_detail_untitled);
        }
        holder.placeName.setText(holder.itemView.getContext().getString(R.string.profile_comment_place_format, title));

        bindStars(holder.starsRow, item.getComment().getStars());

        String text = item.getComment().getComment();
        holder.commentText.setText(text != null && !text.isEmpty()
                ? "\"" + text + "\""
                : holder.itemView.getContext().getString(R.string.place_detail_no_description));

        long ts = item.getComment().getTimestamp();
        holder.date.setText(ts > 0 ? dateFormat.format(new Date(ts)) : "");

        String img = item.getPlaceImageUrl();
        Glide.with(holder.placeImage.getContext())
                .load(img != null && !img.isEmpty() ? img : null)
                .centerCrop()
                .placeholder(R.drawable.noimage)
                .into(holder.placeImage);

        holder.itemView.setOnClickListener(v -> listener.onCommentClick(item));
    }

    private static void bindStars(@NonNull LinearLayout row, int stars) {
        row.removeAllViews();
        int count = Math.max(0, Math.min(5, stars));
        float density = row.getContext().getResources().getDisplayMetrics().density;
        int size = (int) (16 * density);
        int margin = (int) (2 * density);
        for (int i = 0; i < 5; i++) {
            ImageView star = new ImageView(row.getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            if (i > 0) {
                lp.setMarginStart(margin);
            }
            star.setLayoutParams(lp);
            star.setImageResource(i < count ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
            row.addView(star);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView placeImage;
        TextView placeName;
        LinearLayout starsRow;
        TextView commentText;
        TextView date;

        VH(@NonNull View itemView) {
            super(itemView);
            placeImage = itemView.findViewById(R.id.user_comment_place_image);
            placeName = itemView.findViewById(R.id.user_comment_place_name);
            starsRow = itemView.findViewById(R.id.user_comment_stars);
            commentText = itemView.findViewById(R.id.user_comment_text);
            date = itemView.findViewById(R.id.user_comment_date);
        }
    }
}
