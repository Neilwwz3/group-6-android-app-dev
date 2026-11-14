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
import java.util.List;
import java.util.Locale;

import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.model.MonthTotal;

/**
 * Bar chart of hours watched in each of the last 12 months, current month on the right. Bars
 * grow in the first time data arrives; tapping a bar selects that month.
 */
public class MonthlyBarChartView extends View {

    /** Called when the user taps a month. */
    public interface OnMonthSelectedListener {
        void onMonthSelected(int index);
    }

    public static final int MONTHS = 12;
    private static final float STAGGER = 0.04f;

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final int barColor;
    private final int selectedColor;
    private final int emptyColor;
    private final float labelHeight;
    private final float valueHeight;
    private final float radius;
    private final float minBar;

    private final String[] keys = new String[MONTHS];
    private final String[] labels = new String[MONTHS];
    private final int[] minutes = new int[MONTHS];
    private final int[] counts = new int[MONTHS];
    private int maxMinutes;
    private int selected = MONTHS - 1;
    private float grow = 1f;
    private boolean hasAnimated;
    private ValueAnimator animator;
    private OnMonthSelectedListener listener;

    public MonthlyBarChartView(Context context) {
        this(context, null);
    }

    public MonthlyBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;
        int primary = ContextCompat.getColor(context, R.color.primary);
        int surface = ContextCompat.getColor(context, R.color.surface);
        selectedColor = primary;
        barColor = ColorUtils.blendARGB(surface, primary, 0.4f);
        emptyColor = ContextCompat.getColor(context, R.color.surface_variant);
        labelHeight = 22 * density;
        valueHeight = 20 * density;
        radius = 4 * density;
        minBar = 3 * density;

        labelPaint.setColor(ContextCompat.getColor(context, R.color.text_secondary));
        labelPaint.setTextSize(getResources().getDimension(R.dimen.text_caption));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setColor(ContextCompat.getColor(context, R.color.text_primary));
        valuePaint.setTextSize(getResources().getDimension(R.dimen.text_caption));
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setFakeBoldText(true);
        gridPaint.setColor(ContextCompat.getColor(context, R.color.outline));
        gridPaint.setStrokeWidth(density);

        setMonths(Calendar.getInstance());
        setClickable(true);
        setFocusable(true);
    }

    public void setOnMonthSelectedListener(@Nullable OnMonthSelectedListener listener) {
        this.listener = listener;
    }

    /** First day ({@code YYYY-MM-01}) of the oldest month shown for the given today. */
    @NonNull
    public static String firstDay(@NonNull Calendar today) {
        Calendar start = (Calendar) today.clone();
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.add(Calendar.MONTH, -(MONTHS - 1));
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(start.getTime());
    }

    /** Month keys ({@code YYYY-MM}) oldest first, ending with today's month. */
    @VisibleForTesting
    @NonNull
    public static String[] monthKeys(@NonNull Calendar today) {
        String[] result = new String[MONTHS];
        Calendar month = (Calendar) today.clone();
        month.set(Calendar.DAY_OF_MONTH, 1);
        month.add(Calendar.MONTH, -(MONTHS - 1));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM", Locale.US);
        for (int i = 0; i < MONTHS; i++) {
            result[i] = format.format(month.getTime());
            month.add(Calendar.MONTH, 1);
        }
        return result;
    }

    private void setMonths(Calendar today) {
        String[] monthKeys = monthKeys(today);
        Calendar month = (Calendar) today.clone();
        month.set(Calendar.DAY_OF_MONTH, 1);
        month.add(Calendar.MONTH, -(MONTHS - 1));
        for (int i = 0; i < MONTHS; i++) {
            keys[i] = monthKeys[i];
            labels[i] = month.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH);
            month.add(Calendar.MONTH, 1);
        }
    }

    public void setData(@Nullable List<MonthTotal> totals) {
        maxMinutes = 0;
        for (int i = 0; i < MONTHS; i++) {
            minutes[i] = 0;
            counts[i] = 0;
            if (totals == null) {
                continue;
            }
            for (MonthTotal total : totals) {
                if (keys[i].equals(total.month)) {
                    minutes[i] = total.minutes;
                    counts[i] = total.count;
                }
            }
            maxMinutes = Math.max(maxMinutes, minutes[i]);
        }
        if (!hasAnimated && maxMinutes > 0) {
            hasAnimated = true;
            playGrow();
        }
        invalidate();
    }

    public int getSelectedIndex() {
        return selected;
    }

    public void setSelectedIndex(int index) {
        selected = Math.max(0, Math.min(MONTHS - 1, index));
        invalidate();
    }

    @NonNull
    public String getMonthKey(int index) {
        return keys[index];
    }

    public int getMinutes(int index) {
        return minutes[index];
    }

    public int getCount(int index) {
        return counts[index];
    }

    /** Index of the month with the most minutes, or the current month when all are empty. */
    public int getBusiestIndex() {
        int best = MONTHS - 1;
        for (int i = 0; i < MONTHS; i++) {
            if (minutes[i] > minutes[best]) {
                best = i;
            }
        }
        return best;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = (int) (160 * getResources().getDisplayMetrics().density);
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                resolveSize(height + getPaddingTop() + getPaddingBottom(), heightMeasureSpec));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        float left = getPaddingLeft();
        float width = getWidth() - left - getPaddingRight();
        float top = getPaddingTop() + valueHeight;
        float bottom = getHeight() - getPaddingBottom() - labelHeight;
        float slot = width / MONTHS;
        float barWidth = slot * 0.56f;
        float chartHeight = bottom - top;

        canvas.drawLine(left, bottom, left + width, bottom, gridPaint);
        boolean shortLabels = labelPaint.measureText("May") > slot - 2;

        for (int i = 0; i < MONTHS; i++) {
            float centerX = left + slot * i + slot / 2f;
            // Bars start one after another from the left and all finish together.
            float progress = Math.max(0f, Math.min(1f,
                    (grow - i * STAGGER) / (1f - (MONTHS - 1) * STAGGER)));
            float barHeight = maxMinutes == 0 || minutes[i] == 0 ? 0
                    : Math.max(minBar, chartHeight * minutes[i] / maxMinutes) * progress;
            if (barHeight > 0) {
                barPaint.setColor(i == selected ? selectedColor : barColor);
                rect.set(centerX - barWidth / 2f, bottom - barHeight, centerX + barWidth / 2f,
                        bottom + radius);
                canvas.save();
                canvas.clipRect(centerX - barWidth / 2f, top - valueHeight, centerX + barWidth / 2f,
                        bottom);
                canvas.drawRoundRect(rect, radius, radius, barPaint);
                canvas.restore();
            } else {
                barPaint.setColor(emptyColor);
                rect.set(centerX - barWidth / 2f, bottom - minBar, centerX + barWidth / 2f, bottom);
                canvas.drawRect(rect, barPaint);
            }
            if (i == selected && minutes[i] > 0 && progress >= 1f) {
                canvas.drawText(hoursLabel(minutes[i]), centerX, bottom - barHeight
                        - valueHeight * 0.3f, valuePaint);
            }
            labelPaint.setFakeBoldText(i == selected);
            labelPaint.setColor(ContextCompat.getColor(getContext(),
                    i == selected ? R.color.text_primary : R.color.text_secondary));
            String label = shortLabels ? labels[i].substring(0, 1) : labels[i];
            canvas.drawText(label, centerX, bottom + labelHeight * 0.75f, labelPaint);
        }
    }

    @NonNull
    private String hoursLabel(int minutes) {
        return minutes < 60 ? getResources().getString(R.string.chart_minutes_short, minutes)
                : getResources().getString(R.string.chart_hours_short, Math.round(minutes / 60f));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float width = getWidth() - getPaddingLeft() - getPaddingRight();
            int index = (int) ((event.getX() - getPaddingLeft()) / (width / MONTHS));
            if (index >= 0 && index < MONTHS) {
                selected = index;
                invalidate();
                if (listener != null) {
                    listener.onMonthSelected(index);
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
            grow = 1f;
        }
        super.onDetachedFromWindow();
    }

    private void playGrow() {
        if (animator != null) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(900);
        animator.setInterpolator(new DecelerateInterpolator(1.6f));
        animator.addUpdateListener(a -> {
            grow = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }
}
