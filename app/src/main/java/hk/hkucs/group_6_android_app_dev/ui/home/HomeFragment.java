package hk.hkucs.group_6_android_app_dev.ui.home;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.MainActivity;

/** G1 page shell. Feature data belongs in this page's future ViewModel. */
public class HomeFragment extends Fragment {
    public HomeFragment() { super(R.layout.fragment_home); }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        view.findViewById(R.id.explore_library).setOnClickListener(v -> ((MainActivity) requireActivity()).selectTab(R.id.nav_library));
    }
}
