package hk.hkucs.group_6_android_app_dev.ui.library;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.ui.detail.MediaDetailActivity;
import hk.hkucs.group_6_android_app_dev.ui.edit.EditMediaActivity;

/** Library page: search, status filter and the media list. */
public class LibraryFragment extends Fragment {

    private static final int[] SORT_LABELS = {
            R.string.library_sort_recent, R.string.library_sort_rating,
            R.string.library_sort_title, R.string.library_sort_year};
    private static final int[] SORT_ITEMS = {
            R.id.sort_recent, R.id.sort_rating, R.id.sort_title, R.id.sort_year};
    private static final int[] CHIP_IDS = {
            R.id.filter_all, R.id.filter_want, R.id.filter_watching, R.id.filter_watched,
            R.id.filter_dropped};
    private static final int[] CHIP_LABELS = {
            R.string.filter_all, R.string.status_want, R.string.status_watching,
            R.string.status_watched, R.string.status_dropped};

    /** Replay the list's entrance animation on the next update (after a filter or sort). */
    private boolean animateNext = true;

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
            animateNext = true;
            vm.setStatusFilter(tag instanceof String ? (String) tag : LibraryViewModel.FILTER_ALL);
        });
        vm.getStatusCounts().observe(getViewLifecycleOwner(), counts -> {
            for (int i = 0; i < CHIP_IDS.length; i++) {
                Chip chip = chips.findViewById(CHIP_IDS[i]);
                Integer count = counts == null ? null : counts.get((String) chip.getTag());
                chip.setText(getString(R.string.filter_with_count, getString(CHIP_LABELS[i]),
                        count == null ? 0 : count));
            }
        });

        MaterialButton sort = view.findViewById(R.id.library_sort);
        vm.getSort().observe(getViewLifecycleOwner(), order -> sort.setText(getString(
                R.string.library_sort_label, getString(SORT_LABELS[order == null ? 0 : order]))));
        sort.setOnClickListener(button -> {
            PopupMenu menu = new PopupMenu(requireContext(), button);
            menu.inflate(R.menu.menu_library_sort);
            Integer current = vm.getSort().getValue();
            menu.getMenu().findItem(SORT_ITEMS[current == null ? 0 : current]).setChecked(true);
            menu.setOnMenuItemClickListener(menuItem -> {
                for (int i = 0; i < SORT_ITEMS.length; i++) {
                    if (SORT_ITEMS[i] == menuItem.getItemId()) {
                        animateNext = true;
                        vm.setSort(i);
                        list.scrollToPosition(0);
                        return true;
                    }
                }
                return false;
            });
            menu.show();
        });
        TextView count = view.findViewById(R.id.library_count);

        View emptyState = view.findViewById(R.id.empty_state);
        TextView emptyTitle = view.findViewById(R.id.empty_title);
        TextView emptyBody = view.findViewById(R.id.empty_body);
        vm.getVisible().observe(getViewLifecycleOwner(), items -> {
            adapter.submit(items);
            boolean empty = items == null || items.isEmpty();
            emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            int size = items == null ? 0 : items.size();
            count.setText(getResources().getQuantityString(R.plurals.library_count, size, size));
            count.setVisibility(empty ? View.INVISIBLE : View.VISIBLE);
            sort.setVisibility(empty ? View.INVISIBLE : View.VISIBLE);
            if (animateNext && !empty) {
                animateNext = false;
                list.scheduleLayoutAnimation();
            }
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
