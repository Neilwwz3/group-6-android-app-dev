package hk.hkucs.group_6_android_app_dev.ui.library;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.ui.detail.MediaDetailActivity;
import hk.hkucs.group_6_android_app_dev.ui.edit.EditMediaActivity;

/** Library page: search, status filter and the media list. */
public class LibraryFragment extends Fragment {

    public LibraryFragment() {
        super(R.layout.fragment_library);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        LibraryViewModel vm = new ViewModelProvider(this).get(LibraryViewModel.class);

        RecyclerView list = view.findViewById(R.id.media_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        LibraryAdapter adapter = new LibraryAdapter(item ->
                startActivity(MediaDetailActivity.newIntent(requireContext(), item.id)));
        list.setAdapter(adapter);

        TextInputEditText search = view.findViewById(R.id.search_input);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                vm.setQuery(s == null ? "" : s.toString());
            }
        });

        ChipGroup chips = view.findViewById(R.id.status_filter);
        chips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                vm.setStatusFilter(LibraryViewModel.FILTER_ALL);
                return;
            }
            Chip chip = group.findViewById(checkedIds.get(0));
            Object tag = chip.getTag();
            vm.setStatusFilter(tag instanceof String ? (String) tag : LibraryViewModel.FILTER_ALL);
        });

        View emptyState = view.findViewById(R.id.empty_state);
        TextView emptyTitle = view.findViewById(R.id.empty_title);
        TextView emptyBody = view.findViewById(R.id.empty_body);
        vm.getVisible().observe(getViewLifecycleOwner(), items -> {
            adapter.submit(items);
            boolean empty = items == null || items.isEmpty();
            emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
        vm.isLibraryEmpty().observe(getViewLifecycleOwner(), libraryEmpty -> {
            if (Boolean.TRUE.equals(libraryEmpty)) {
                emptyTitle.setText(R.string.library_empty_title);
                emptyBody.setText(R.string.library_empty_body);
            } else {
                emptyTitle.setText(R.string.no_results_title);
                emptyBody.setText(R.string.no_results_body);
            }
        });

        view.findViewById(R.id.add_title).setOnClickListener(v ->
                startActivity(EditMediaActivity.newIntent(requireContext(), null)));
    }
}
