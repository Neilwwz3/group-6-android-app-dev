package hk.hkucs.group_6_android_app_dev.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.model.GenreCount;

public class DonutChartView extends View {
    private static final int[] PALETTE = {
            R.color.primary,
            R.color.status_watched_text,
            R.color.rating,
            R.color.status_dropped_text,
            R.color.text_secondary,
            R.color.on_primary_container,
            R.color.error,
            R.color.text_hint,
            R.color.text_primary,
            R.color.status_watched_bg,
            R.color.status_watching_bg,
            R.color.status_dropped_bg
    };

    private final int[] colors = new int[PALETTE.length];
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds = new RectF();
    private final List<GenreCount> counts = new ArrayList<>();
    private long total;

    public DonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < PALETTE.length; i++) {
            colors[i] = ContextCompat.getColor(context, PALETTE[i]);
        }
    }

    public void setCounts(@NonNull List<GenreCount> values) {
        counts.clear();
        counts.addAll(values);
        total = 0;
        for (GenreCount value : counts) {
            total += value.count;
        }
        invalidate();
    }

    public int getSegmentColor(int index) {
        return colors[index % colors.length];
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth() - getPaddingLeft() - getPaddingRight();
        float height = getHeight() - getPaddingTop() - getPaddingBottom();
        float diameter = Math.min(width, height);
        if (total <= 0 || diameter <= 0) {
            return;
        }

        float strokeWidth = diameter / 5f;
        float radius = (diameter - strokeWidth) / 2f;
        float centerX = getPaddingLeft() + width / 2f;
        float centerY = getPaddingTop() + height / 2f;
        bounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        paint.setStrokeWidth(strokeWidth);

        long cumulativeCount = 0;
        float start = -90f;
        for (int i = 0; i < counts.size(); i++) {
            cumulativeCount += counts.get(i).count;
            float end = -90f + 360f * cumulativeCount / total;
            paint.setColor(getSegmentColor(i));
            canvas.drawArc(bounds, start, end - start, false, paint);
            start = end;
        }
    }
}