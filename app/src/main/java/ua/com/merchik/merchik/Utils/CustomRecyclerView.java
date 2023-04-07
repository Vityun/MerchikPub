package ua.com.merchik.merchik.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ExpandableListView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class CustomRecyclerView extends RecyclerView {

    private int mLastY;
    private boolean mDisallowIntercept;
    private int mode = 0;

    public CustomRecyclerView(Context context) {
        super(context);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setMode(int mode){
        this.mode = mode;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        Log.e("CustomRecyclerView", "action: " + action);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastY = (int) ev.getRawY();
                mDisallowIntercept = false;
                break;
            case MotionEvent.ACTION_MOVE:
                int y = (int) ev.getRawY();
                int dy = y - mLastY;
                mLastY = y;

                // Проверяем, прокручивается ли ExpandableListView в данный момент
                boolean isExpListScrolling = isExpListScrolling();

                if (isExpListScrolling && !mDisallowIntercept) {
                    // Если ExpandableListView прокручивается, запрещаем RecyclerView перехватывать событие прокрутки
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mDisallowIntercept = false;
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    // Метод для проверки, прокручивается ли в данный момент ExpandableListView
    private boolean isExpListScrolling() {
        Log.e("CustomRecyclerView", "getChildCount(): " + getChildCount());
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            Log.e("CustomRecyclerView", "child: " + child);
            if (child instanceof ExpandableListView) {
                ExpandableListView expList = (ExpandableListView) child;
                if ((expList.getFirstVisiblePosition() > 0) || (expList.getLastVisiblePosition() < (expList.getCount() - 1))) {
                    Log.e("CustomRecyclerView", "isExpListScrolling/true: " + true);
                    return true;
                }
            }else if (child instanceof ConstraintLayout && mode == 1){
                return true;
            }
        }
        Log.e("CustomRecyclerView", "isExpListScrolling/false: " + false);
        return false;
    }
}