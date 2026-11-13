package hk.hkucs.group_6_android_app_dev.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.model.MediaType;
import hk.hkucs.group_6_android_app_dev.util.PosterLoader;

final class WatchingAdapter extends ListAdapter<MediaItem, WatchingAdapter.Holder> {
    private final Consumer<String> onOpen;

    WatchingAdapter(Consumer<String> onOpen) {
        super(new DiffUtil.ItemCallback<MediaItem>() {
            @Override public boolean areItemsTheSame(@NonNull MediaItem oldItem,
                    @NonNull MediaItem newItem) {
                return oldItem.id.equals(newItem.id);
            }

            @Override public boolean areContentsTheSame(@NonNull MediaItem oldItem,
                    @NonNull MediaItem newItem) {
                return Objects.equals(oldItem.title, newItem.title)
                        && Objects.equals(oldItem.type, newItem.type)
                        && Objects.equals(oldItem.year, newItem.year)
                        && Objects.equals(oldItem.poster, newItem.poster);
            }
        });
        this.onOpen = onOpen;
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_watching, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        MediaItem item = getItem(position);
        holder.title.setText(item.title);
        int type = MediaType.SERIES.equals(item.type) ? R.string.home_type_series : R.string.home_type_movie;
        String typeLabel = holder.itemView.getResources().getString(type);
        String metadata = item.year == null ? typeLabel : holder.itemView.getResources()
                .getString(R.string.home_watching_metadata, typeLabel, item.year);
        String monogram = monogramFor(item.title);
        if (monogram.isEmpty()) {
            holder.monogram.setText(R.string.home_poster_placeholder);
        } else {
            holder.monogram.setText(monogram);
        }
        PosterLoader.load(holder.itemView.getContext(), item.poster,
                holder.poster, holder.monogram);
        holder.metadata.setText(metadata);
        holder.itemView.setContentDescription(holder.itemView.getResources()
                .getString(R.string.home_open_media, item.title, metadata));
        holder.itemView.setOnClickListener(v -> onOpen.accept(item.id));
    }

    static String monogramFor(String title) {
        String trimmed = title == null ? "" : title.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        int codePoint = trimmed.codePointAt(0);
        return new String(Character.toChars(codePoint)).toUpperCase(Locale.US);
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ImageView poster;
        final TextView monogram;
        final TextView title;
        final TextView metadata;

        Holder(View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.home_watching_poster);
            monogram = itemView.findViewById(R.id.home_watching_monogram);
            title = itemView.findViewById(R.id.home_watching_title);
            metadata = itemView.findViewById(R.id.home_watching_metadata);
        }
    }
}
