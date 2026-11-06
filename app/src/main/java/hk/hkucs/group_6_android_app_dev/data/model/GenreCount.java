package hk.hkucs.group_6_android_app_dev.data.model;

/** How many media items carry one genre. */
public class GenreCount {

    public final String genre;
    public final int count;

    public GenreCount(String genre, int count) {
        this.genre = genre;
        this.count = count;
    }
}
