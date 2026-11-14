package hk.hkucs.group_6_android_app_dev.ui.components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import hk.hkucs.group_6_android_app_dev.R;

/**
 * GitHub-style heatmap of the last year: one column per week, one cell per day, darker cells
 * for days with more watch records. The last column holds today. Put it in a
 * HorizontalScrollView; it is wider than a phone screen.
 */
public class HeatmapView extends View {

    /** Called when the user taps a day that is inside the grid. */
    public interface OnDayClickListener {
        void onDayClick(@NonNull String date, int count);
    }

    @VisibleForTesting
    static final int WEEKS = 53;
    private static final int DAYS = 7;
    private static final float[] LEVEL_ALPHA = {0f, 0.3f, 0.55f, 0.8f, 1f};

    private final Paint cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint todayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final int[] levelColors = new int[LEVEL_ALPHA.length];
    /** Reused while drawing; only touched on the main thread. */
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private final float cell;
    private final float gap;
    private final float radius;
    private final float leftLabelWidth;
    private final float topLabelHeight;

    private Map<String, Integer> counts = Collections.emptyMap();
    private Calendar today = Calendar.getInstance();
    private String selectedDate;
    private OnDayClickListener listener;
    private float reveal = 1f;
    private ValueAnimator animator;
    private boolean hasAnimated;

    public HeatmapView(Context context) {
        this(context, null);
    }

    public HeatmapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;
        cell = 14 * density;
        gap = 3 * density;
        radius = 3 * density;
        leftLabelWidth = 30 * density;
        topLabelHeight = 18 * density;

        for (int i = 0; i < LEVEL_ALPHA.length; i++) {
            levelColors[i] = levelColor(context, i);
        }

        selectedPaint.setStyle(Paint.Style.STROKE);
        selectedPaint.setStrokeWidth(2 * density);
        selectedPaint.setColor(ContextCompat.getColor(context, R.color.text_primary));

        todayPaint.setStyle(Paint.Style.STROKE);
        todayPaint.setStrokeWidth(1.5f * density);
        todayPaint.setColor(ContextCompat.getColor(context, R.color.primary));

        labelPaint.setColor(ContextCompat.getColor(context, R.color.text_secondary));
        labelPaint.setTextSize(getResources().getDimension(R.dimen.text_caption));

        setClickable(true);
        setFocusable(true);
    }

    /** Day ({@code YYYY-MM-DD}) to record count. Days missing from the map are empty. */
    public void setData(@Nullable Map<String, Integer> dailyCounts) {
        counts = dailyCounts == null ? Collections.emptyMap() : dailyCounts;
        int activeDays = 0;
        for (Integer count : counts.values()) {
            if (count != null && count > 0) {
                activeDays++;
            }
        }
        setContentDescription(getResources().getQuantityString(
                R.plurals.diary_heatmap_description, activeDays, activeDays));
        if (!hasAnimated && !counts.isEmpty()) {
            hasAnimated = true;
            playReveal();
        }
        invalidate();
    }

    public void setOnDayClickListener(@Nullable OnDayClickListener listener) {
        this.listener = listener;
    }

    /** Outlines one day, or none when {@code date} is null. */
    public void setSelectedDate(@Nullable String date) {
        selectedDate = date;
        invalidate();
    }

    /** For tests and screenshots: pretend today is {@code date}. */
    @VisibleForTesting
    public void setToday(@NonNull Calendar date) {
        today = (Calendar) date.clone();
        invalidate();
    }

    /** First day shown in the grid for the given today, as {@code YYYY-MM-DD}. */
    @NonNull
    public static String firstDay(@NonNull Calendar today) {
        return format(gridStart(today));
    }

    /** Fill color for a heatmap level 0–4, for drawing a legend next to the grid. */
    public static int levelColor(@NonNull Context context, int level) {
        int surface = ContextCompat.getColor(context, R.color.surface);
        if (level <= 0) {
            return ContextCompat.getColor(context, R.color.surface_variant);
        }
        return ColorUtils.blendARGB(surface, ContextCompat.getColor(context, R.color.primary),
                LEVEL_ALPHA[Math.min(level, LEVEL_ALPHA.length - 1)]);
    }

    @NonNull
    public static String format(@NonNull Calendar day) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(day.getTime());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) Math.ceil(leftLabelWidth + WEEKS * (cell + gap));
        int height = (int) Math.ceil(topLabelHeight + DAYS * (cell + gap));
        setMeasuredDimension(resolveSize(width + getPaddingLeft() + getPaddingRight(),
                        widthMeasureSpec),
                resolveSize(height + getPaddingTop() + getPaddingBottom(), heightMeasureSpec));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        Calendar day = gridStart(today);
        String todayKey = dayFormat.format(today.getTime());
        int lastMonth = -1;
        float labelBaseline = getPaddingTop() + topLabelHeight - 6 * getResources()
                .getDisplayMetrics().density;

        for (int week = 0; week < WEEKS; week++) {
            if (day.get(Calendar.MONTH) != lastMonth && day.get(Calendar.DAY_OF_MONTH) <= 7) {
                lastMonth = day.get(Calendar.MONTH);
                canvas.drawText(day.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH),
                        x(week), labelBaseline, labelPaint);
            }
            boolean visible = (week + 1f) / WEEKS <= reveal + 0.02f;
            for (int weekday = 0; weekday < DAYS; weekday++) {
                String key = dayFormat.format(day.getTime());
                if (key.compareTo(todayKey) > 0) {
                    break;
                }
                Integer count = counts.get(key);
                int level = visible ? level(count == null ? 0 : count) : 0;
                cellPaint.setColor(levelColors[level]);
                rect.set(x(week), y(weekday), x(week) + cell, y(weekday) + cell);
                canvas.drawRoundRect(rect, radius, radius, cellPaint);
                if (key.equals(selectedDate)) {
                    canvas.drawRoundRect(rect, radius, radius, selectedPaint);
                } else if (key.equals(todayKey)) {
                    canvas.drawRoundRect(rect, radius, radius, todayPaint);
                }
                day.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        String[] weekdays = {"Mon", "Wed", "Fri"};
        int[] rows = {1, 3, 5};
        for (int i = 0; i < rows.length; i++) {
            canvas.drawText(weekdays[i], getPaddingLeft(),
                    y(rows[i]) + cell - 3 * getResources().getDisplayMetrics().density,
                    labelPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            String date = dateAt(event.getX(), event.getY());
            if (date != null) {
                selectedDate = date;
                invalidate();
                Integer count = counts.get(date);
                if (listener != null) {
                    listener.onDayClick(date, count == null ? 0 : count);
                }
                performClick();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.cancel();
            reveal = 1f;
        }
        super.onDetachedFromWindow();
    }

    /** The date under a touch point, or null outside the grid or after today. */
    @VisibleForTesting
    @Nullable
    String dateAt(float touchX, float touchY) {
        float gridX = touchX - getPaddingLeft() - leftLabelWidth;
        float gridY = touchY - getPaddingTop() - topLabelHeight;
        if (gridX < 0 || gridY < 0) {
            return null;
        }
        int week = (int) (gridX / (cell + gap));
        int weekday = (int) (gridY / (cell + gap));
        if (week >= WEEKS || weekday >= DAYS) {
            return null;
        }
        Calendar day = gridStart(today);
        day.add(Calendar.DAY_OF_MONTH, week * DAYS + weekday);
        String key = dayFormat.format(day.getTime());
        return key.compareTo(dayFormat.format(today.getTime())) > 0 ? null : key;
    }

    private void playReveal() {
        if (animator != null) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(900);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            reveal = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    private float x(int week) {
        return getPaddingLeft() + leftLabelWidth + week * (cell + gap);
    }

    private float y(int weekday) {
        return getPaddingTop() + topLabelHeight + weekday * (cell + gap);
    }

    @VisibleForTesting
    static int level(int count) {
        return count <= 0 ? 0 : Math.min(count, LEVEL_ALPHA.length - 1);
    }

    /** Sunday of the week 52 weeks before the week containing today. */
    private static Calendar gridStart(Calendar today) {
        Calendar start = (Calendar) today.clone();
        start.set(Calendar.HOUR_OF_DAY, 12);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        start.add(Calendar.DAY_OF_MONTH, -(start.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY));
        start.add(Calendar.DAY_OF_MONTH, -7 * (WEEKS - 1));
        return start;
    }
}
