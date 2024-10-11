package ua.com.merchik.merchik;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import ua.com.merchik.merchik.data.ServerInfo.AppVersion.AppVersion;
import ua.com.merchik.merchik.data.ServerInfo.AppVersion.Datum;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;


/**
 * Класс занимается тем что определяет последнюю версию приложения с PLAY MARKET
 * */
public class VersionApp {



    private Globals globals = new Globals();

    public String currentVersion, minVersion; // Текущая версия приложения
    public String playMarcketVersion;

    private int massageType;
    private Context contextGl;

    //----------------------------------------------------------------------------------------------


    /**
     * 05.02.2021
     * */
    public static Long VERSION_APP = 0L;
    public void getMinVer(Globals.getVersionInterface callback){
        String mod = "constant_list";
        String act = "get";
        retrofit2.Call<AppVersion> call = RetrofitBuilder.getRetrofitInterface().GET_CONSTANT_APP_VERSION(mod, act);
        call.enqueue(new retrofit2.Callback<AppVersion>() {
            @Override
            public void onResponse(retrofit2.Call<AppVersion> call, retrofit2.Response<AppVersion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("getVer", "response.body(): " + response.body());
                    AppVersion data = response.body();
                    if (data.getState()){
                        for (Datum list : data.getData()){
                            if (list.getID() != null && list.getID().equals("13049")){ // МИНИМАЛЬНАЯ ВЕРСИЯ ПРИЛОЖЕНИЯ
                                VERSION_APP = Long.valueOf(list.getVal());
                                Log.e("getVer", "VERSION_APP: " + VERSION_APP);
                                callback.onSuccess(VERSION_APP);
                            }
                        }
                    }
                }else {
                    callback.onFailure("Получить номер актуальной версии не получилось.");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<AppVersion> call, Throwable t) {
                VERSION_APP = 0L;
                Log.e("getVer", "onFailure: " + t);
                callback.onFailure("Получить номер актуальной версии не получилось. Проверьте наличие интернета и повторите попытку позже.\n\n" + t.toString());
            }
        });
    }





    /**
     * Если версия ниже определённой - приложение будет закрыто
     * */
    public void checkForOldVer(Context context){
        try {
            currentVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        currentVersion = currentVersion.replaceAll("\\.", "");

        if (Integer.parseInt(currentVersion) < 1009191113) {

            String link = context.getResources().getString(R.string.merchik_linl_pm);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            TextView textView = new TextView(context);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setText(R.string.merchik_linl_pm);
            dialogBuilder.setView(textView);
            dialogBuilder.create().show();

        }
    }


    /**
     * Проверка версии приложения.
     *
     * Входит Context в котором будет отображаться сообщение о новой версии
     * Так же будет запущен AsyncTask который проверяет через инет новю версию
     *
     * @return  true    когда новая версия доступна или обновление не требуется
     *          false   когда что-то идёт не так (сделал для того что б потом в
     *                  хэндлере повторить)
     *
     * */
    boolean checkVer(Context context){
//        server server = new server();
        contextGl = context;    // Получаем контекст для того что б потом в GetVersionCode можно было получить имя пакета (лол, это надо переписать)
        try {
            currentVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;   // Получаем текущую версию приложения
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        new GetVersionCode().execute(); // Чекаем новую версию прилы


        // Получение минимальной версии
/*        HashMap<String, String> params = new HashMap<>();
        params.put("mod",  "constant_list" );
        params.put("act",  "get");
        RequestBody formBody = server.createFormBody(params);
        String sJSON = server.serverPostOffline("mobile_app.php", formBody);

        if(sJSON != null && !sJSON.equals("")) {
            JsonObject jsonVer;
            jsonVer = new JsonParser().parse(sJSON).getAsJsonObject();
            if (jsonVer != null) {
                if (!jsonVer.get("state").isJsonNull() && jsonVer.get("state").getAsBoolean()) {
                    JsonArray arr = jsonVer.getAsJsonArray("data");
                    minVersion = arr.get(1).getAsJsonObject().get("val").getAsString(); // В втором реквизите хранится минимальная версия id = 13049 (на случай когда я пойму что что-то поломалось)
                }
            }
        }*/

        if (massageType == 1){
            alertMessageAppVer(context);
            return true;
        }

        return false;
    }


    /**
     * Формирование сообщения о том что есть новая версия приложения
     * */
    private void alertMessageAppVer(final Context context){
        String msg = "";
        String old_id = context.getString(R.string.t_ad_update_app_old);
        String vold_id = context.getString(R.string.t_ad_update_app_vold);

        if (minVersion == null){
            minVersion = "1009191123";
        }

        currentVersion = currentVersion.replaceAll("\\.", "");
        minVersion = minVersion.replaceAll("\\.", "");
        int cV = Integer.parseInt(currentVersion);
        int mV = Integer.parseInt(minVersion);

        boolean enabled = true;

        if (cV >= mV) {
            msg = old_id + " " + currentVersion + ". А на Play Market доступна: " + playMarcketVersion + "\nОбновитесь до последней версии.\n\nЕсли после обновления версия не изменилась - перезагрузите телефон и повторите обновление.";
        } else if (cV < mV) {
            msg = "" + old_id + "\n\n" + vold_id + " (" + playMarcketVersion + ")";
            enabled = false;
        }

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.alertdialog_version_update);
        dialog.setTitle("Внимание!");
        dialog.setCancelable(false);

        //set up text
        TextView text = (TextView) dialog.findViewById(R.id.textView_ad_update_app);
        text.setText(msg);

        //set up button update
        Button button_update = (Button) dialog.findViewById(R.id.button_ad_update_app);
        button_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://play.google.com/store/apps/details?id=ua.com.merchik.merchik";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                context.startActivity(i);
                dialog.cancel();
            }
        });

        //set up button close
        Button button = (Button) dialog.findViewById(R.id.button_ad_update_app_close);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        button.setEnabled(enabled);

        dialog.show();
    }


    /**
     * Ассинхронный Класс для проверки и загрузки новой версии приложения
     * */
    private class GetVersionCode extends AsyncTask<Void, String, String> {

        @Override
        protected String doInBackground(Void... voids) {

            String newVersion = null;

            try {
                Document document = Jsoup.connect("https://play.google.com/store/apps/details?id=" + contextGl.getPackageName() + "&hl=ua")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get();
                if (document != null) {
                    Elements element = document.getElementsContainingOwnText("Current Version");
                    for (Element ele : element) {
                        if (ele.siblingElements() != null) {
                            Elements sibElemets = ele.siblingElements();
                            for (Element sibElemet : sibElemets) {
                                newVersion = sibElemet.text();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newVersion;
        }


        @Override
        protected void onPostExecute(String onlineVersion) {
            super.onPostExecute(onlineVersion);

            if (onlineVersion != null && !onlineVersion.isEmpty()) {
                playMarcketVersion = onlineVersion;

                // Убераем точки из версии приложения
                currentVersion = currentVersion.replaceAll("\\.", "");
                onlineVersion = onlineVersion.replaceAll("\\.", "");

                // Проверка актуальности приложения
                if (Integer.valueOf(currentVersion) < Integer.valueOf(onlineVersion)) {
                    massageType = 1;
                }else if(Integer.valueOf(currentVersion).equals(Integer.valueOf(onlineVersion))){
                    massageType = 2;
                }
            }

        }


    }// END CHECKED CLASS..
}
