package ua.com.merchik.merchik.Utils.Ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import ua.com.merchik.merchik.R;

/** Shared numeric badge for XML, Compose and map markers. */
public class CounterBadgeView extends TextView {
    private final GradientDrawable badgeBackground = new GradientDrawable();

    public CounterBadgeView(Context context) {
        this(context, null);
    }

    public CounterBadgeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CounterBadgeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setGravity(Gravity.CENTER);
        setIncludeFontPadding(false);
        setSingleLine(true);
        setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        int horizontal = getResources().getDimensionPixelSize(R.dimen.counter_badge_horizontal_padding);
        int vertical = getResources().getDimensionPixelSize(R.dimen.counter_badge_vertical_padding);
        setPadding(horizontal, vertical, horizontal, vertical);
        setBackground(badgeBackground);

        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.CounterBadgeView, defStyleAttr, 0);
        try {
            float size = values.getDimension(R.styleable.CounterBadgeView_counterBadgeSize,
                    getResources().getDimension(R.dimen.counter_badge_size));
            int textColor = values.getColor(R.styleable.CounterBadgeView_android_textColor, Color.WHITE);
            setBadgeStyle(size, getResources().getDimension(R.dimen.counter_badge_text_size),
                    values.getColor(R.styleable.CounterBadgeView_counterBadgeColor, Color.RED),
                    values.getColor(R.styleable.CounterBadgeView_counterBadgeBorderColor, textColor), textColor);
        } finally {
            values.recycle();
        }
    }

    public void setBadgeStyle(float diameterPx, float textSizePx, int background, int border, int text) {
        int size = Math.round(diameterPx);
        if (getMinWidth() != size) setMinWidth(size);
        if (getMinHeight() != size) setMinHeight(size);
        if (getTextSize() != textSizePx) setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);
        if (getCurrentTextColor() != text) setTextColor(text);
        badgeBackground.setColor(background);
        badgeBackground.setStroke(getResources().getDimensionPixelSize(R.dimen.counter_badge_border_width), border);
    }

    public void setCount(long count, Integer maxCount) {
        String value = formatCount(count, maxCount);
        if (!value.contentEquals(getText())) setText(value);
    }

    public static String formatCount(long count, Integer maxCount) {
        return maxCount != null && count > maxCount ? maxCount + "+" : Long.toString(count);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // A single digit stays circular; longer numbers and scaled fonts can grow.
        setMeasuredDimension(resolveSizeAndState(Math.max(getMeasuredWidth(), getMeasuredHeight()),
                widthMeasureSpec, getMeasuredState()), getMeasuredHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        badgeBackground.setCornerRadius(h / 2f);
    }
}
