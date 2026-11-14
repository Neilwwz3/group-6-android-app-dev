package hk.hkucs.group_6_android_app_dev.ui.components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.model.GenreCount;

public class DonutChartView extends View {
    private static final int[] PALETTE = {
            R.color.primary,
            R.color.status_watched_text,
            R.color.rating,
            R.color.status_dropped_text,
            R.color.text_secondary,
            R.color.status_watched_bg,
            R.color.error,
            R.color.status_watching_bg,
            R.color.text_hint,
            R.color.status_dropped_bg,
            R.color.text_primary,
            R.color.status_watching_text
    };

    private final int[] colors = new int[PALETTE.length];
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds = new RectF();
    private final List<GenreCount> counts = new ArrayList<>();
    private final List<String> percentages = new ArrayList<>();
    private final float gapWidth;
    private long total;
    /** 0 to 1 while the ring sweeps in the first time it gets data. */
    private float sweep = 1f;
    private boolean hasAnimated;
    private ValueAnimator animator;

    public DonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.STROKE);
        gapWidth = getResources().getDimension(R.dimen.space_4);
        int surface = ContextCompat.getColor(context, R.color.surface);
        for (int i = 0; i < PALETTE.length; i++) {
            int color = ContextCompat.getColor(context, PALETTE[i]);
            colors[i] = i == 0 ? color : ColorUtils.blendARGB(color, surface, 0.35f);
        }
    }

    public void setCounts(@NonNull List<GenreCount> values) {
        counts.clear();
        counts.addAll(values);
        total = 0;
        for (GenreCount value : counts) {
            total += value.count;
        }
        NumberFormat percent = NumberFormat.getPercentInstance(Locale.US);
        percent.setMaximumFractionDigits(1);
        percentages.clear();
        for (GenreCount value : counts) {
            percentages.add(percent.format(total == 0 ? 0 : (double) value.count / total));
        }
        if (!hasAnimated && total > 0) {
            hasAnimated = true;
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(900);
            animator.setInterpolator(new DecelerateInterpolator(1.5f));
            animator.addUpdateListener(a -> {
                sweep = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }
        invalidate();
    }

    public String getPercentage(int index) {
        return percentages.get(index);
    }

    public int getSegmentColor(int index) {
        return colors[index % colors.length];
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.cancel();
            sweep = 1f;
        }
        super.onDetachedFromWindow();
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

        float strokeWidth = diameter / 6f;
        float radius = (diameter - strokeWidth) / 2f;
        float centerX = getPaddingLeft() + width / 2f;
        float centerY = getPaddingTop() + height / 2f;
        bounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        paint.setStrokeWidth(strokeWidth);

        float limit = -90f + 360f * sweep;
        if (counts.size() == 1) {
            paint.setColor(getSegmentColor(0));
            if (sweep >= 1f) {
                canvas.drawOval(bounds, paint);
            } else {
                canvas.drawArc(bounds, -90f, 360f * sweep, false, paint);
            }
        } else {
            float gapAngle = (float) Math.toDegrees(gapWidth / radius);
            long cumulativeCount = 0;
            float start = -90f;
            for (int i = 0; i < counts.size(); i++) {
                cumulativeCount += counts.get(i).count;
                float end = -90f + 360f * cumulativeCount / total;
                float segment = end - start;
                float gap = Math.min(gapAngle, segment / 4f);
                float drawn = Math.min(end, limit) - start - gap;
                if (drawn > 0) {
                    paint.setColor(getSegmentColor(i));
                    canvas.drawArc(bounds, start + gap / 2f, drawn, false, paint);
                }
                start = end;
            }
        }
    }
}
