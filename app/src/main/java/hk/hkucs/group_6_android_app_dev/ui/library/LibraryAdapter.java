package hk.hkucs.group_6_android_app_dev.ui.library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.util.MediaMeta;
import hk.hkucs.group_6_android_app_dev.util.PosterLoader;
import hk.hkucs.group_6_android_app_dev.util.StatusBadge;

/** Compact media cards for the library list. */
public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.Holder> {

    public interface OnItemClickListener {
        void onItemClick(MediaItem item);
    }

    private final List<MediaItem> items = new ArrayList<>();
    private final OnItemClickListener onClick;

    public LibraryAdapter(OnItemClickListener onClick) {
        this.onClick = onClick;
    }

    public void submit(List<MediaItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_media, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        MediaItem item = items.get(position);
        holder.title.setText(item.title);
        holder.meta.setText(MediaMeta.line(holder.itemView.getContext(), item));
        StatusBadge.apply(holder.badge, item.status);
        holder.posterLetter.setText(item.title.isEmpty()
                ? "?" : item.title.substring(0, 1).toUpperCase(java.util.Locale.US));
        PosterLoader.load(holder.itemView.getContext(), item.poster,
                holder.posterImage, holder.posterLetter);
        if (item.rating == null) {
            holder.rating.setVisibility(View.GONE);
            holder.score.setText(R.string.not_rated);
        } else {
            holder.rating.setVisibility(View.VISIBLE);
            holder.rating.setRating(item.rating / 2f);
            holder.score.setText(holder.itemView.getContext().getString(
                    R.string.rating_value, MediaMeta.score(item.rating)));
        }
        holder.itemView.setOnClickListener(v -> onClick.onItemClick(item));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView meta;
        final TextView badge;
        final TextView score;
        final TextView posterLetter;
        final ImageView posterImage;
        final RatingBar rating;

        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.media_title);
            meta = itemView.findViewById(R.id.media_meta);
            badge = itemView.findViewById(R.id.status_badge);
            score = itemView.findViewById(R.id.rating_score);
            posterLetter = itemView.findViewById(R.id.poster_letter);
            posterImage = itemView.findViewById(R.id.poster_image);
            rating = itemView.findViewById(R.id.rating_bar);
        }
    }
}
