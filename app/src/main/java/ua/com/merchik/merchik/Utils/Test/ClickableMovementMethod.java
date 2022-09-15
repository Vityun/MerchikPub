package ua.com.merchik.merchik.Utils.Test;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class ClickableMovementMethod extends LinkMovementMethod {

    private static ClickableMovementMethod sInstance;

    private static int down;
    private static int up;

    public static ClickableMovementMethod getInstance() {
        down = 0;
        up = 0;
        if (sInstance == null) {
            sInstance = new ClickableMovementMethod();
        }
        return sInstance;
    }

    @Override
    public boolean canSelectArbitrarily() {
        return false;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            Log.d("ClickableMovementMethod", "int line: " + line);
            Log.d("ClickableMovementMethod", "int off: " + off);
            Log.d("ClickableMovementMethod", "widget.getText().length(): " + widget.getText().length());

            if (off >= widget.getText().length()) {
                // Return true so click won't be triggered in the leftover empty space
                Log.d("ClickableMovementMethod", "click outside");
                return true;
            }else {
                Log.d("ClickableMovementMethod", "click inside?");
            }

            if (action == MotionEvent.ACTION_DOWN){
                down = line;
            }else {
                up = line;
            }
        }

        Log.d("ClickableMovementMethod", "down: " + down);
        Log.d("ClickableMovementMethod", "up: " + up);


        if (down != up && action == MotionEvent.ACTION_UP){
            Log.d("ClickableMovementMethod", "WORK/ Я не должен нажаться! Просто ничего не сделать.");
            return true;
        }else if (down == up){
            Log.d("ClickableMovementMethod", "WORK/ Я должен кликнуться.");
        }else {
            Log.d("ClickableMovementMethod", "WORK/ Я продолжаю работу, пока не кликнусь!");
        }


        if (action == MotionEvent.ACTION_CANCEL){return true;}


        Log.d("ClickableMovementMethod", "onTouchEvent: " + event);
        return super.onTouchEvent(widget, buffer, event);
    }

    @Override
    public void initialize(TextView widget, Spannable text) {
        Selection.removeSelection(text);
    }
}