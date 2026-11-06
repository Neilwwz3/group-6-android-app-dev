package hk.hkucs.group_6_android_app_dev.ui.library;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.ui.edit.EditMediaActivity;

/** G1 page shell. Feature data belongs in this page's future ViewModel. */
public class LibraryFragment extends Fragment {
    public LibraryFragment() { super(R.layout.fragment_library); }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        view.findViewById(R.id.add_title).setOnClickListener(v -> startActivity(EditMediaActivity.newIntent(requireContext(), null)));
    }
}
