package hk.hkucs.group_6_android_app_dev.ui.diary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import hk.hkucs.group_6_android_app_dev.data.db.entity.WatchRecord;
import hk.hkucs.group_6_android_app_dev.data.model.RecordWithMedia;
import hk.hkucs.group_6_android_app_dev.ui.components.HeatmapView;

public class DiaryLogicTest {

    @Test
    public void toRows_putsOneHeaderBeforeEachDaysRecords() {
        List<DiaryViewModel.Row> rows = DiaryViewModel.toRows(Arrays.asList(
                record("2026-11-05"), record("2026-11-05"), record("2026-11-01")));

        assertEquals(5, rows.size());
        assertTrue(rows.get(0).isHeader());
        assertEquals("2026-11-05", rows.get(0).date);
        assertEquals(2, rows.get(0).dayCount);
        assertFalse(rows.get(1).isHeader());
        assertFalse(rows.get(2).isHeader());
        assertTrue(rows.get(3).isHeader());
        assertEquals("2026-11-01", rows.get(3).date);
        assertEquals(1, rows.get(3).dayCount);
    }

    @Test
    public void toRows_emptyOrNull() {
        assertTrue(DiaryViewModel.toRows(null).isEmpty());
        assertTrue(DiaryViewModel.toRows(Arrays.asList()).isEmpty());
    }

    @Test
    public void heatmapStartsOnASundayAbout52WeeksBack() {
        Calendar today = Calendar.getInstance();
        today.set(2026, Calendar.NOVEMBER, 19); // a Thursday

        // The week containing Nov 19 starts on Sunday Nov 15; 52 weeks earlier is Nov 16, 2025.
        assertEquals("2025-11-16", HeatmapView.firstDay(today));
    }

    @Test
    public void scoresDropTrailingZero() {
        assertEquals("9", DiaryAdapter.formatScore(9f));
        assertEquals("8.5", DiaryAdapter.formatScore(8.5f));
    }

    @Test
    public void longDateFallsBackToTheRawValue() {
        assertEquals("Thu, Nov 19, 2026", DiaryAdapter.longDate("2026-11-19"));
        assertEquals("not a date", DiaryAdapter.longDate("not a date"));
    }

    private static RecordWithMedia record(String date) {
        RecordWithMedia item = new RecordWithMedia();
        item.record = new WatchRecord();
        item.record.date = date;
        item.mediaTitle = "Title";
        return item;
    }
}
