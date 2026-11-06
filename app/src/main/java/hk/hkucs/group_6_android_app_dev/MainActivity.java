package hk.hkucs.group_6_android_app_dev;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import hk.hkucs.group_6_android_app_dev.ui.home.HomeFragment;
import hk.hkucs.group_6_android_app_dev.ui.library.LibraryFragment;
import hk.hkucs.group_6_android_app_dev.ui.diary.DiaryFragment;
import hk.hkucs.group_6_android_app_dev.ui.profile.ProfileFragment;

/** Owns the four top-level destinations; feature pages own their own content. */
public class MainActivity extends AppCompatActivity {
    private static final String STATE_TAB = "selected_tab";
    private static final int[] TABS = {R.id.nav_home, R.id.nav_library, R.id.nav_diary, R.id.nav_profile};
    private int selectedTab = R.id.nav_home;
    private BottomNavigationView navigation;
    private OnBackPressedCallback backToHome;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // Material's navigation view handles its own bottom system-bar inset.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(bars.left, bars.top, bars.right, 0);
            return insets;
        });
        backToHome = new OnBackPressedCallback(false) {
            @Override public void handleOnBackPressed() { selectTab(R.id.nav_home); }
        };
        getOnBackPressedDispatcher().addCallback(this, backToHome);
        navigation = findViewById(R.id.bottom_navigation);
        int restored = state == null ? R.id.nav_home : state.getInt(STATE_TAB, R.id.nav_home);
        navigation.setOnItemSelectedListener(item -> showTab(item.getItemId()));
        // Reselection must not add a second instance or change the current page state.
        navigation.setOnItemReselectedListener(item -> { });
        selectTab(isTab(restored) ? restored : R.id.nav_home);
    }

    public void selectTab(@IdRes int id) {
        if (!isTab(id)) { throw new IllegalArgumentException("Unknown top-level destination"); }
        if (navigation.getSelectedItemId() == id) { showTab(id); }
        else { navigation.setSelectedItemId(id); }
    }

    private boolean showTab(int id) {
        FragmentManager manager = getSupportFragmentManager();
        if (!isTab(id) || manager.isStateSaved()) { return false; }
        Fragment target = manager.findFragmentByTag(tag(id));
        FragmentTransaction transaction = manager.beginTransaction().setReorderingAllowed(true);
        for (int tab : TABS) {
            Fragment page = manager.findFragmentByTag(tag(tab));
            if (page != null && page != target) {
                transaction.hide(page).setMaxLifecycle(page, Lifecycle.State.STARTED);
            }
        }
        if (target == null) {
            target = newPage(id);
            transaction.add(R.id.main_fragment_container, target, tag(id));
        } else { transaction.show(target); }
        transaction.setMaxLifecycle(target, Lifecycle.State.RESUMED).setPrimaryNavigationFragment(target);
        // Immediate completion prevents duplicate pages under rapid repeated taps.
        transaction.commitNow();
        selectedTab = id;
        backToHome.setEnabled(id != R.id.nav_home);
        return true;
    }

    private Fragment newPage(int id) {
        if (id == R.id.nav_library) { return new LibraryFragment(); }
        if (id == R.id.nav_diary) { return new DiaryFragment(); }
        if (id == R.id.nav_profile) { return new ProfileFragment(); }
        return new HomeFragment();
    }

    private static boolean isTab(int id) {
        for (int tab : TABS) { if (tab == id) { return true; } }
        return false;
    }

    private static String tag(int id) { return "g1_tab_" + id; }

    @Override protected void onSaveInstanceState(@NonNull Bundle state) {
        state.putInt(STATE_TAB, selectedTab);
        super.onSaveInstanceState(state);
    }
}
