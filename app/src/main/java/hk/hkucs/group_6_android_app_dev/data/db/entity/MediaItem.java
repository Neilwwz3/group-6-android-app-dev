package hk.hkucs.group_6_android_app_dev.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

/** One movie or TV show in the user's library. Field meanings are documented in docs/data-api.md. */
@Entity(tableName = "media_item")
public class MediaItem {

    /** Empty for a new item; {@code MediaRepository.saveMedia} assigns one. */
    @PrimaryKey
    @NonNull
    public String id = "";

    @NonNull
    public String title = "";

    @ColumnInfo(name = "original_title")
    public String originalTitle;

    public Integer year;

    /** One of {@link hk.hkucs.group_6_android_app_dev.data.model.MediaType}. */
    @NonNull
    public String type = "movie";

    @NonNull
    public List<String> genres = new ArrayList<>();

    @NonNull
    public List<String> directors = new ArrayList<>();

    @NonNull
    public List<String> cast = new ArrayList<>();

    @NonNull
    public List<String> tags = new ArrayList<>();

    public String overview;

    @ColumnInfo(name = "runtime_minutes")
    public Integer runtimeMinutes;

    @ColumnInfo(name = "total_episodes")
    public Integer totalEpisodes;

    /** File name of the poster, or null to show a placeholder. */
    public String poster;

    /** One of {@link hk.hkucs.group_6_android_app_dev.data.model.MediaStatus}. */
    @NonNull
    public String status = "want";

    /** 0–10 in steps of 1 (half stars); null means not rated. */
    public Float rating;

    /** {@code manual} or {@code sample}. */
    @NonNull
    public String source = "manual";

    @ColumnInfo(name = "tmdb_id")
    public String tmdbId;

    @ColumnInfo(name = "imdb_id")
    public String imdbId;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;
}
