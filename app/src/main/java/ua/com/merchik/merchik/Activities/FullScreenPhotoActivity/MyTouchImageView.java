package ua.com.merchik.merchik.Activities.FullScreenPhotoActivity;

import android.content.Context;
import android.graphics.Canvas;

import com.ortiz.touchview.TouchImageView;

public class MyTouchImageView extends TouchImageView {

    public MyTouchImageView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(0, getHeight());
        canvas.scale(1, -1);
        super.onDraw(canvas);
        canvas.restore();
    }
}
