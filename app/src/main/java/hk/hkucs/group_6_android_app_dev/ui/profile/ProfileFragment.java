package hk.hkucs.group_6_android_app_dev.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.repository.SampleDataLoader;


/** G1 page shell. Feature data belongs in this page's future ViewModel. */
public class ProfileFragment extends Fragment {
    public ProfileFragment() { super(R.layout.fragment_profile); }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);

        // Load the bundled sample dataset so the library, detail and diary pages have content.
        view.findViewById(R.id.load_sample_data).setOnClickListener(v -> {
            v.setEnabled(false);
            SampleDataLoader.load(requireContext().getApplicationContext(), () -> {
                v.setEnabled(true);
                if (isAdded()) {
                    Toast.makeText(v.getContext(), R.string.load_sample_data_done,
                            Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                v.setEnabled(true);
                if (isAdded()) {
                    Toast.makeText(v.getContext(), R.string.load_sample_data_failed,
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
