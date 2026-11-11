package hk.hkucs.group_6_android_app_dev.ui.diary;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.model.RecordWithMedia;
import hk.hkucs.group_6_android_app_dev.ui.detail.MediaDetailActivity;

/** G4 Diary tab: last year's heatmap above a timeline of watch records grouped by date. */
public class DiaryFragment extends Fragment {

    private DiaryViewModel viewModel;
    private DiaryAdapter adapter;
    private LinearLayoutManager layoutManager;

    public DiaryFragment() {
        super(R.layout.fragment_diary);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        viewModel = new ViewModelProvider(this).get(DiaryViewModel.class);

        adapter = new DiaryAdapter(new DiaryAdapter.Listener() {
            @Override
            public void onDayClick(@NonNull String date, int count) {
                viewModel.selectDate(date);
                scrollToDate(date);
            }

            @Override
            public void onRecordClick(@NonNull RecordWithMedia record) {
                startActivity(MediaDetailActivity.newIntent(requireContext(),
                        record.record.mediaId));
            }
        });

        RecyclerView list = view.findViewById(R.id.diary_page);
        layoutManager = new LinearLayoutManager(requireContext());
        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);

        viewModel.getRows().observe(getViewLifecycleOwner(), adapter::setRows);
        viewModel.getDailyCounts().observe(getViewLifecycleOwner(), adapter::setDailyCounts);
        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), adapter::setSelectedDate);
    }

    private void scrollToDate(String date) {
        int position = adapter.positionOfDate(date);
        if (position < 0) {
            Toast.makeText(requireContext(),
                    getString(R.string.diary_no_entries_on, DiaryAdapter.longDate(date)),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        layoutManager.scrollToPositionWithOffset(position, 0);
    }
}
