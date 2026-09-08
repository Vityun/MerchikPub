package ua.com.merchik.merchik.features.main.options;

import android.graphics.Color;
import android.view.View;

import ua.com.merchik.merchik.R;

/** Presentation only: no Android widgets or managed Realm objects. */
public final class OptionItemState {
    public final String id;
    public final String optionId;
    public final String controlId;
    public final TextPart title = new TextPart();
    public final TextPart counter = new TextPart();
    public final TextPart secondaryCounter = new TextPart();
    public final Signal signal = new Signal();
    public int backgroundRes = R.drawable.bg_temp;
    public View.OnClickListener onClick;
    public View.OnLongClickListener onLongClick;

    public OptionItemState(String id, String optionId, String controlId) {
        this.id = id;
        this.optionId = optionId;
        this.controlId = controlId;
        secondaryCounter.visibility = View.GONE;
    }

    public static final class TextPart {
        public CharSequence text = "";
        public boolean bold;
        public int visibility = View.VISIBLE;
        public View.OnClickListener onClick;
    }

    public static final class Signal {
        public int iconRes = R.drawable.ic_round;
        public int tint = Color.GRAY;
        public int visibility = View.VISIBLE;
        public View.OnClickListener onClick;
    }
}
