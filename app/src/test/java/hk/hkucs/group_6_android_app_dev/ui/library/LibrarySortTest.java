package hk.hkucs.group_6_android_app_dev.ui.library;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;

public class LibrarySortTest {

    private static MediaItem item(String title, Float rating, Integer year) {
        MediaItem item = new MediaItem();
        item.title = title;
        item.rating = rating;
        item.year = year;
        return item;
    }

    private static List<String> titles(List<MediaItem> items) {
        List<String> result = new ArrayList<>();
        for (MediaItem item : items) {
            result.add(item.title);
        }
        return result;
    }

    private final List<MediaItem> items = Arrays.asList(
            item("inception", 9f, 2010),
            item("Alien", null, 1979),
            item("Zootopia", 8.4f, null),
            item("Dune", 9.5f, 2021));

    @Test
    public void recentKeepsRepositoryOrder() {
        assertEquals(Arrays.asList("inception", "Alien", "Zootopia", "Dune"),
                titles(LibraryViewModel.sorted(items, LibraryViewModel.SORT_RECENT)));
    }

    @Test
    public void ratingIsHighestFirstWithUnratedLast() {
        assertEquals(Arrays.asList("Dune", "inception", "Zootopia", "Alien"),
                titles(LibraryViewModel.sorted(items, LibraryViewModel.SORT_RATING)));
    }

    @Test
    public void titleIgnoresCase() {
        assertEquals(Arrays.asList("Alien", "Dune", "inception", "Zootopia"),
                titles(LibraryViewModel.sorted(items, LibraryViewModel.SORT_TITLE)));
    }

    @Test
    public void yearIsNewestFirstWithUnknownLast() {
        assertEquals(Arrays.asList("Dune", "inception", "Alien", "Zootopia"),
                titles(LibraryViewModel.sorted(items, LibraryViewModel.SORT_YEAR)));
    }
}
