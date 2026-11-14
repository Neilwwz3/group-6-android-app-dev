package hk.hkucs.group_6_android_app_dev.ui.home;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import hk.hkucs.group_6_android_app_dev.MainActivity;
import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.repository.MediaRepository;
import hk.hkucs.group_6_android_app_dev.data.repository.SampleDataLoader;

@RunWith(AndroidJUnit4.class)
public class HomeScreenTest {
    private Context context;

    @Before
    public void setUp() throws InterruptedException {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        clearData();
    }

    @After
    public void tearDown() throws InterruptedException {
        clearData();
    }

    @Test
    public void emptyLibraryShowsWelcomeInsteadOfDashboard() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            waitForHomeData();
            onView(withId(R.id.home_welcome)).check(matches(isDisplayed()));
            onView(withId(R.id.home_dashboard))
                    .check(matches(withEffectiveVisibility(GONE)));
            onView(withId(R.id.home_subtitle))
                    .check(matches(withEffectiveVisibility(VISIBLE)));
            capture("g5-home-empty.png");
        }
    }

    @Test
    public void sampleLibraryShowsDashboardSections() throws InterruptedException {
        CountDownLatch loaded = new CountDownLatch(1);
        AtomicReference<Exception> error = new AtomicReference<>();
        SampleDataLoader.load(context, loaded::countDown, exception -> {
            error.set(exception);
            loaded.countDown();
        });
        assertTrue(loaded.await(10, TimeUnit.SECONDS));
        assertNull(error.get());

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            waitForHomeData();
            onView(withId(R.id.home_welcome))
                    .check(matches(withEffectiveVisibility(GONE)));
            onView(withId(R.id.home_dashboard))
                    .check(matches(withEffectiveVisibility(VISIBLE)));
            onView(withId(R.id.home_subtitle))
                    .check(matches(withEffectiveVisibility(GONE)));
            onView(withId(R.id.home_eyebrow))
                    .check(matches(withEffectiveVisibility(VISIBLE)));
            onView(withId(R.id.home_watching_list))
                    .check(matches(withEffectiveVisibility(VISIBLE)));
            onView(withId(R.id.home_month_count))
                    .check(matches(withText(context.getString(R.string.home_month_count, 11))));
            onView(withId(R.id.home_month_hours))
                    .check(matches(withText(context.getString(
                            R.string.home_month_whole_hours, 24))));
            onView(withId(R.id.home_month_selector))
                    .check(matches(withText("November 2026")));
            onView(withId(R.id.home_month_next)).check(matches(isNotEnabled()));

            onView(withId(R.id.home_month_previous)).perform(click());
            waitForHomeData();
            onView(withId(R.id.home_month_selector))
                    .check(matches(withText("October 2026")));
            onView(withId(R.id.home_month_count)).check(matches(withText("2")));
            onView(withId(R.id.home_month_hours))
                    .check(matches(withText(context.getString(
                            R.string.home_month_hours_minutes, 7, 11))));

            onView(withId(R.id.home_month_selector)).perform(click());
            onView(withText(R.string.home_choose_month_title)).check(matches(isDisplayed()));
            onView(withId(R.id.home_month_picker_month)).check(matches(isDisplayed()));
            onView(withId(R.id.home_month_picker_year)).check(matches(isDisplayed()));
            onView(withText(R.string.home_month_cancel)).perform(click());

            onView(withId(R.id.home_month_next)).perform(click());
            waitForHomeData();
            onView(withId(R.id.home_month_selector))
                    .check(matches(withText("November 2026")));

            scenario.onActivity(activity -> {
                RecyclerView watching = activity.findViewById(R.id.home_watching_list);
                LinearLayout recent = activity.findViewById(R.id.home_recent_list);
                assertEquals(5, watching.getAdapter().getItemCount());
                assertEquals(5, recent.getChildCount());
                RecyclerView.ViewHolder first =
                        watching.findViewHolderForAdapterPosition(0);
                ImageView poster = first.itemView.findViewById(R.id.home_watching_poster);
                assertEquals(View.VISIBLE, poster.getVisibility());
            });
            capture("g5-home-loaded.png");
            onView(withId(R.id.home_page)).perform(swipeUp(), swipeUp());
            capture("g5-home-loaded-recent.png");
        }
    }

    private void clearData() throws InterruptedException {
        CountDownLatch cleared = new CountDownLatch(1);
        MediaRepository.get(context).clearAll(cleared::countDown);
        assertTrue(cleared.await(10, TimeUnit.SECONDS));
    }

    private static void waitForHomeData() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        SystemClock.sleep(500);
    }

    private static void capture(String filename) {
        android.app.Instrumentation instrumentation =
                InstrumentationRegistry.getInstrumentation();
        instrumentation.waitForIdleSync();
        android.graphics.Bitmap bitmap = instrumentation.getUiAutomation().takeScreenshot();
        assertTrue(bitmap != null);
        java.io.File file = new java.io.File(
                instrumentation.getTargetContext().getExternalFilesDir(null), filename);
        try (java.io.FileOutputStream stream = new java.io.FileOutputStream(file)) {
            assertTrue(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream));
        } catch (java.io.IOException exception) {
            throw new AssertionError(exception);
        } finally {
            bitmap.recycle();
        }
    }
}
