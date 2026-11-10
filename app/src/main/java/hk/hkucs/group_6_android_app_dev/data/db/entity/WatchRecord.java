package hk.hkucs.group_6_android_app_dev.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

/** One viewing of a {@link MediaItem}. Deleted together with its media item. */
@Entity(
        tableName = "watch_record",
        foreignKeys = @ForeignKey(
                entity = MediaItem.class,
                parentColumns = "id",
                childColumns = "media_id",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("media_id"), @Index("date")})
public class WatchRecord {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "media_id")
    @NonNull
    public String mediaId = "";

    /** {@code YYYY-MM-DD}. */
    @NonNull
    public String date = "";

    /** Episode numbers watched this time; empty for a movie. */
    @NonNull
    public List<Integer> episodes = new ArrayList<>();

    public Integer minutes;

    /** This viewing's rating, 0–10, or null. */
    public Float rating;

    @NonNull
    public String note = "";

    @ColumnInfo(name = "created_at")
    public long createdAt;
}
