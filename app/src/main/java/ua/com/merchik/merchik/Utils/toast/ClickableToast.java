package ua.com.merchik.merchik.Utils.toast;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

public final class ClickableToast {
    public static void show(@NonNull Context activity, @NonNull CharSequence message, boolean longDuration) {
        Dialog dlg = new Dialog(activity);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);
        dlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Контейнер
        FrameLayout root = new FrameLayout(activity);

        // Карточка
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFF2D3A4A); // фон
        float r = activity.getResources().getDisplayMetrics().density * 14f;
        bg.setCornerRadius(r);

        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(activity.getResources().getDisplayMetrics().density * 12);
        card.setPadding(pad, pad, pad, pad);
        card.setBackground(bg);
        card.setElevation(8f);

        // Текст — ВАЖНО: движение/клики для спанов
        TextView tv = new TextView(activity);
        tv.setText(message);
        tv.setTextColor(0xFFE9F0F8);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tv.setGravity(Gravity.CENTER);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setHighlightColor(Color.TRANSPARENT);

        card.addView(tv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        FrameLayout.LayoutParams lpCard = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        int m = (int)(activity.getResources().getDisplayMetrics().density * 16);
        lpCard.leftMargin = m; lpCard.rightMargin = m;
        root.addView(card, lpCard);

        dlg.setContentView(root);

        // Настраиваем окно ДО show()
        Window w = dlg.getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
//        lp.token = activity.getWindow().getDecorView().getWindowToken();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.y = (int)(activity.getResources().getDisplayMetrics().density * 72);
        if (Build.VERSION.SDK_INT >= 21) lp.windowAnimations = android.R.style.Animation_Dialog;
        w.setAttributes(lp);

        // Не затемнять фон и не фокусировать окно, но разрешить клики по самому диалогу
        w.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        w.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

        dlg.show();

        // Автозакрытие «как у тоста»
        long durationMs = longDuration ? 3500L : 2000L;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try { dlg.dismiss(); } catch (Throwable ignore) {}
        }, durationMs);
    }
}
