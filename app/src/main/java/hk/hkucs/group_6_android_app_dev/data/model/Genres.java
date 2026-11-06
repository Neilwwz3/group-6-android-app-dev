package hk.hkucs.group_6_android_app_dev.data.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** The fixed genre vocabulary. Media items store these exact words. */
public final class Genres {

    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(
            "Drama", "Comedy", "Action", "Sci-Fi", "Animation", "Mystery",
            "Crime", "Romance", "Horror", "Documentary", "Fantasy", "War"));

    private Genres() {
    }
}
