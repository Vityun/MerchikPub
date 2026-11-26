package ua.com.merchik.merchik.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.RnoVisitDataExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.OptionsExchange;
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder;
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;
import ua.com.merchik.merchik.toolbar_menus;

public class MenuMainActivity extends toolbar_menus {
    private static final String TAG = "OPTIONS_BY_DAD2_TEST";
    private static final String TEST_DAD2 = "1261126041569061574";
    private Call<ResponseBody> activeRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolbar_new);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((TextView) findViewById(R.id.activity_title)).setText(R.string.title_activity_menu_main);
        new ViewModelProvider(this).get(CronchikViewModel.class);

        try {
            findViewById(R.id.fab).setOnClickListener(this::requestVisitOptions);
            NavigationView navigation = findViewById(R.id.nav_view);
            initDrawerStuff(findViewById(R.id.drawer_layout), findViewById(R.id.my_toolbar), navigation);
            navigation.setCheckedItem(R.id.nav_home);
        } catch (Exception e) {
            globals.alertDialogMsg(this, "ОшибкаMain: " + e);
        }
    }

    private void requestVisitOptions(View button) {
        if (activeRequest != null) return;
        button.setEnabled(false);
        try {
            JsonObject request = OptionsExchange.createRequest(null, null, TEST_DAD2);
            RnoVisitDataExchange.authorizeTargetedRequest(request);
            logResult("REQUEST plan/options_list code_dad2=" + TEST_DAD2);
            activeRequest = RetrofitBuilder.getRetrofitInterface()
                    .rawMobileAppResponse(RetrofitBuilder.contentType, request);
            activeRequest.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    String status = "HTTP " + response.code();
                    try (ResponseBody body = response.isSuccessful() ? response.body() : response.errorBody()) {
                        if (call.isCanceled()) return;
                        String raw = body == null ? "" : body.string();
                        Globals.writeToMLOG("INFO", TAG + "/RAW", raw);
                        CoordOnMapProbe.logChunks(raw, (part, chunk) ->
                                Log.i(TAG, "RAW[" + part + "] " + chunk));
                        deliverResult(call, button, response.isSuccessful() && !raw.isEmpty(),
                                status + (raw.isEmpty() ? "\nПорожня відповідь."
                                        : "\nВідповідь записано в m_log і Logcat: " + TAG));
                    } catch (IOException | RuntimeException error) {
                        deliverResult(call, button, false, status + "\nНе вдалося обробити відповідь: "
                                + error.getClass().getSimpleName());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable error) {
                    deliverResult(call, button, false,
                            "Не вдалося отримати відповідь: " + error.getClass().getSimpleName());
                }
            });
        } catch (RuntimeException error) {
            finishProbe(button, false, "Не вдалося виконати запит: " + error.getClass().getSimpleName());
        }
    }

    private void deliverResult(Call<ResponseBody> call, View button, boolean ok, String message) {
        runOnUiThread(() -> {
            if (call == activeRequest) finishProbe(button, ok, message);
        });
    }

    private void finishProbe(View button, boolean ok, String message) {
        activeRequest = null;
        logResult(message);
        if (isFinishing() || isDestroyed()) return;
        button.setEnabled(true);
        new MessageDialogBuilder(this)
                .setTitle("plan / options_list")
                .setStatus(ok ? DialogStatus.INFO : DialogStatus.ERROR)
                .setMessage(message)
                .show();
    }

    private static void logResult(String message) {
        for (String line : message.split("\n")) Log.i(TAG, line);
        Globals.writeToMLOG("INFO", TAG, message);
    }

    @Override
    protected void onDestroy() {
        if (activeRequest != null) {
            activeRequest.cancel();
            activeRequest = null;
        }
        super.onDestroy();
    }
}
