package hk.hkucs.group_6_android_app_dev;

import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.junit.Test;
import org.junit.runner.RunWith;
import hk.hkucs.group_6_android_app_dev.ui.edit.EditMediaActivity;
import hk.hkucs.group_6_android_app_dev.ui.detail.MediaDetailActivity;
import hk.hkucs.group_6_android_app_dev.ui.record.AddRecordActivity;
import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;

@RunWith(AndroidJUnit4.class)
public class NavigationTest {
    private static final int[] TABS = {R.id.nav_home, R.id.nav_library, R.id.nav_diary, R.id.nav_profile};
    private static final int[] PAGES = {R.id.home_page, R.id.library_page, R.id.diary_page, R.id.profile_page};

    @Test public void allTabsSwitchWithoutDuplicatingFragments() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            for (int round = 0; round < 3; round++) {
                for (int i = 0; i < TABS.length; i++) {
                    onView(withId(TABS[i])).perform(click());
                    onView(withId(PAGES[i])).check(matches(isDisplayed()));
                    onView(withId(TABS[i])).perform(click());
                }
            }
            scenario.onActivity(activity -> {
                assertEquals(4, activity.getSupportFragmentManager().getFragments().size());
                int visible = 0;
                for (Fragment page : activity.getSupportFragmentManager().getFragments()) {
                    if (!page.isHidden()) { visible++; }
                    if (page.isHidden()) { assertEquals(Lifecycle.State.STARTED, page.getLifecycle().getCurrentState()); }
                }
                assertEquals(1, visible);
            });
        }
    }

    @Test public void eachSelectedPageSurvivesRecreation() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            for (int i = 0; i < TABS.length; i++) {
                final int expected = TABS[i];
                onView(withId(expected)).perform(click());
                scenario.recreate();
                onView(withId(PAGES[i])).check(matches(isDisplayed()));
                capture("g1-" + i + ".png");
                scenario.onActivity(activity -> {
                    BottomNavigationView nav = activity.findViewById(R.id.bottom_navigation);
                    assertEquals(expected, nav.getSelectedItemId());
                    assertEquals(activity.getColor(R.color.primary), nav.getItemIconTintList()
                            .getColorForState(new int[] {android.R.attr.state_checked}, 0));
                    assertEquals(activity.getColor(R.color.primary_container), nav.getItemActiveIndicatorColor().getDefaultColor());
                    assertNotNull(activity.getSupportFragmentManager().getPrimaryNavigationFragment());
                });
            }
        }
    }

    @Test public void homeLinkSecondaryPageAndBackKeepNavigationConsistent() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.explore_library)).perform(scrollTo(), click());
            onView(withId(R.id.library_page)).check(matches(isDisplayed()));
            onView(withId(R.id.add_title)).perform(scrollTo(), click());
            onView(withId(R.id.feature_title)).check(matches(withText(R.string.add_title)));
            androidx.test.espresso.Espresso.pressBack();
            onView(withId(R.id.library_page)).check(matches(isDisplayed()));
            androidx.test.espresso.Espresso.pressBack();
            onView(withId(R.id.home_page)).check(matches(isDisplayed()));
        }
    }

    private void capture(String filename) {
        android.app.Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.waitForIdleSync();
        android.os.SystemClock.sleep(400); // Finish Material indicator animation before capturing evidence.
        android.graphics.Bitmap bitmap = instrumentation.getUiAutomation().takeScreenshot();
        assertNotNull(bitmap);
        java.io.File file = new java.io.File(instrumentation.getTargetContext().getExternalFilesDir(null), filename);
        try (java.io.FileOutputStream stream = new java.io.FileOutputStream(file)) {
            assertTrue(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream));
        } catch (java.io.IOException exception) { throw new AssertionError(exception); }
        finally { bitmap.recycle(); }
    }

    @Test public void secondaryEntryContractsCarryTheMediaId() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent edit = EditMediaActivity.newIntent(context, "test-title");
        assertEquals("test-title", edit.getStringExtra(EditMediaActivity.EXTRA_MEDIA_ID));
        try (ActivityScenario<EditMediaActivity> scenario = ActivityScenario.launch(edit)) {
            onView(withId(R.id.feature_title)).check(matches(withText(R.string.edit_title)));
        }
        try (ActivityScenario<MediaDetailActivity> scenario = ActivityScenario.launch(MediaDetailActivity.newIntent(context, "test-title"))) {
            onView(withId(R.id.feature_title)).check(matches(withText(R.string.details_title)));
        }
        try (ActivityScenario<AddRecordActivity> scenario = ActivityScenario.launch(AddRecordActivity.newIntent(context, "test-title"))) {
            onView(withId(R.id.feature_title)).check(matches(withText(R.string.record_title)));
        }
    }
}
