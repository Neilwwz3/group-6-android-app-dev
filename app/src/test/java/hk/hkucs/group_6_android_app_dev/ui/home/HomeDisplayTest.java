package hk.hkucs.group_6_android_app_dev.ui.home;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import hk.hkucs.group_6_android_app_dev.data.model.StatsSummary;

public class HomeDisplayTest {

    @Test
    public void welcomeOnlyAppearsWhenTheWholeLibraryIsEmpty() {
        assertFalse(HomeFragment.shouldShowWelcome(null));

        StatsSummary empty = new StatsSummary();
        assertTrue(HomeFragment.shouldShowWelcome(empty));

        StatsSummary libraryOnly = new StatsSummary();
        libraryOnly.mediaCount = 2;
        assertFalse(HomeFragment.shouldShowWelcome(libraryOnly));

        StatsSummary recordsOnly = new StatsSummary();
        recordsOnly.recordCount = 1;
        assertFalse(HomeFragment.shouldShowWelcome(recordsOnly));
    }

    @Test
    public void recordDateUsesTheRequiredEnglishDisplayFormat() {
        assertEquals("Nov 12, 2026", HomeFragment.formatRecordDate("2026-11-12"));
        assertEquals("Feb 29, 2024", HomeFragment.formatRecordDate("2024-02-29"));
    }

    @Test
    public void invalidRecordDateFallsBackToStoredValue() {
        assertEquals("2026-02-30", HomeFragment.formatRecordDate("2026-02-30"));
        assertEquals("not-a-date", HomeFragment.formatRecordDate("not-a-date"));
        assertEquals("", HomeFragment.formatRecordDate(""));
    }

    @Test
    public void monthLabelsAndNavigationUseEnglishCalendarMonths() {
        assertEquals("November 2026", HomeFragment.formatMonthLabel("2026-11"));
        assertEquals("2025-12", HomeViewModel.shiftMonth("2026-01", -1));
        assertEquals("2027-01", HomeViewModel.shiftMonth("2026-12", 1));
        assertEquals("2026-12", HomeViewModel.monthKey(2026, 15));
        assertEquals("2026-01", HomeViewModel.monthKey(2026, 0));
    }

    @Test
    public void watchingMonogramUsesTheFirstVisibleCharacter() {
        assertEquals("I", WatchingAdapter.monogramFor("Interstellar"));
        assertEquals("S", WatchingAdapter.monogramFor("  spirited away"));
        assertEquals("", WatchingAdapter.monogramFor("   "));
    }
}
