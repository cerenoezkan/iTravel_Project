package com.example.itravel.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.itravel.Model.PlaceComment;
import com.example.itravel.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlaceCommentAdapter extends RecyclerView.Adapter<PlaceCommentAdapter.VH> {

    public interface Listener {
        void onEdit(@NonNull PlaceComment comment);

        void onDelete(@NonNull PlaceComment comment);
    }

    private final List<PlaceComment> comments = new ArrayList<>();
    @Nullable
    private final String currentUserId;
  @Nullable
    private final Listener listener;
    private final DateFormat dateFormat =
            DateFormat.getDateInstance(DateFormat.MEDIUM, new Locale("tr", "TR"));

    public PlaceCommentAdapter(@Nullable String currentUserId, @Nullable Listener listener) {
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void setComments(@NonNull List<PlaceComment> items) {
        comments.clear();
        comments.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place_comment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        PlaceComment item = comments.get(position);
        String name = item.getUsername();
        holder.username.setText(name == null || name.isEmpty()
                ? holder.itemView.getContext().getString(R.string.comment_anonymous)
                : name);
        holder.text.setText(item.getComment());
        bindStars(holder.starsRow, item.getStars());

        if (item.getTimestamp() > 0) {
            holder.date.setText(dateFormat.format(new Date(item.getTimestamp())));
        } else {
            holder.date.setText("");
        }

        boolean own = currentUserId != null
                && item.getUserId() != null
                && currentUserId.equals(item.getUserId());
        holder.actions.setVisibility(own && listener != null ? View.VISIBLE : View.GONE);

        holder.editBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(item);
            }
        });
        holder.deleteBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(item);
            }
        });
    }

    private static void bindStars(@NonNull LinearLayout row, int stars) {
        row.removeAllViews();
        int count = Math.max(0, Math.min(5, stars));
        for (int i = 0; i < 5; i++) {
            ImageView star = new ImageView(row.getContext());
            int size = (int) (18 * row.getContext().getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            if (i > 0) {
                lp.setMarginStart((int) (2 * row.getContext().getResources().getDisplayMetrics().density));
            }
            star.setLayoutParams(lp);
            star.setImageResource(i < count ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
            row.addView(star);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView username;
        TextView text;
        TextView date;
        LinearLayout starsRow;
        LinearLayout actions;
        ImageButton editBtn;
        ImageButton deleteBtn;

        VH(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.comment_username);
            text = itemView.findViewById(R.id.comment_text);
            date = itemView.findViewById(R.id.comment_date);
            starsRow = itemView.findViewById(R.id.comment_stars_row);
            actions = itemView.findViewById(R.id.comment_actions);
            editBtn = itemView.findViewById(R.id.btn_edit_comment);
            deleteBtn = itemView.findViewById(R.id.btn_delete_comment);
        }
    }
}
