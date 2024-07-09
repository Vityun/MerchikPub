package ua.com.merchik.merchik;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.preference.PreferenceManager;

import com.google.gson.JsonObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.SiteObjectsExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.TranslationsExchange;
import ua.com.merchik.merchik.data.Database.Room.LanguagesSDB;
import ua.com.merchik.merchik.data.Database.Room.TranslatesSDB;
import ua.com.merchik.merchik.data.Translation.AddTranslation;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class Translate {

    private Context mContext;


    public static void setLanguage(Context context, int langId) {
        LanguagesSDB languages = SQL_DB.langListDao().getLanguageById(langId);

        Globals.langId = langId;

        Log.e("CLASS_Translate", "setLanguage/ Globals.langId: " + Globals.langId);

        try {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putString("lang_id", languages.id).apply();
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putString("lang_short", languages.nmShort).apply();
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putString("lang_long", languages.nm).apply();
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "Translate.setLanguage", "Exception e: " + e);
        }
    }


    /**
     * 12.02.2021
     * Установка языка приложения
     *
     * @param context -- нужен для того что.
     * @param lang    -- Язык который устанавливаем. Фотмат - большие буквы типа (UA, RU, PL, GB..)
     */
    public static void setAppLanguage(Context context, String lang) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("lang", lang).apply();
    }


    /**
     * 12.02.2021
     * Получение языка приложения.
     * <p>
     * Дефолтное значение языка изначально - Украинский (UA)
     *
     * @param context -- нужен для того что.
     */
    public static String getAppLanguage(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("lang", "UA");
    }


    /**
     * 12.02.2021
     * Выбор языка.
     * <p>
     * Вызов POPUP блока для выбора языка
     */
    public static void showPopupMenu(Context context, View view) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.popup_language);

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getOrder()) {
                case 0:
                    setAppLanguage(context, "UA");
                    Toast.makeText(context, "Вы сменили язык. Дождитесь сообщения о загрузке нового языка", Toast.LENGTH_SHORT).show();
                    Translate.setLanguage(context, 2);
                    new SiteObjectsExchange().downloadSiteObjects(new Exchange.ExchangeInt() {
                        @Override
                        public void onSuccess(String msg) {
                            Toast.makeText(context, "Новый язык загружен.", Toast.LENGTH_SHORT).show();
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("lang", "UA").apply();
                        }

                        @Override
                        public void onFailure(String error) {
                            Translate.setLanguage(context, 1);
                            Toast.makeText(context, "Произошла ошибка при загрузке нового языка, повторите попытку позже.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Globals.refreshActivity(context);
                    return true;

                case 1:
                    setAppLanguage(context, "RU");
                    Toast.makeText(context, "Вы сменили язык. Дождитесь сообщения о загрузке нового языка", Toast.LENGTH_SHORT).show();
                    Translate.setLanguage(context, 1);
                    new SiteObjectsExchange().downloadSiteObjects(new Exchange.ExchangeInt() {
                        @Override
                        public void onSuccess(String msg) {
                            Toast.makeText(context, "Новый язык загружен.", Toast.LENGTH_SHORT).show();
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("lang", "RU").apply();
                        }

                        @Override
                        public void onFailure(String error) {
                            Translate.setLanguage(context, 1);
                            Toast.makeText(context, "Произошла ошибка при загрузке нового языка, повторите попытку позже.", Toast.LENGTH_SHORT).show();
                            Log.e("СМЕНА_ЯЗЫКА", error);
                        }
                    });
                    Globals.refreshActivity(context);
                    return true;

                case 2:
                    setAppLanguage(context, "GB");
                    Toast.makeText(context, "Вы сменили язык. Дождитесь сообщения о загрузке нового языка", Toast.LENGTH_SHORT).show();
                    Translate.setLanguage(context, 3);
                    new SiteObjectsExchange().downloadSiteObjects(new Exchange.ExchangeInt() {
                        @Override
                        public void onSuccess(String msg) {
                            Toast.makeText(context, "Новый язык загружен.", Toast.LENGTH_SHORT).show();
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("lang", "GB").apply();
                        }

                        @Override
                        public void onFailure(String error) {
                            Translate.setLanguage(context, 1);
                            Toast.makeText(context, "Произошла ошибка при загрузке нового языка, повторите попытку позже.", Toast.LENGTH_SHORT).show();
                            Log.e("СМЕНА_ЯЗЫКА", error);
                        }
                    });
                    Globals.refreshActivity(context);
                    return true;

                case 3:
                    setAppLanguage(context, "PL");
                    Toast.makeText(context, "Вы сменили язык. Дождитесь сообщения о загрузке нового языка", Toast.LENGTH_SHORT).show();
                    Translate.setLanguage(context, 7);
                    new SiteObjectsExchange().downloadSiteObjects(new Exchange.ExchangeInt() {
                        @Override
                        public void onSuccess(String msg) {
                            Toast.makeText(context, "Новый язык загружен.", Toast.LENGTH_SHORT).show();
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("lang", "PL").apply();
                        }

                        @Override
                        public void onFailure(String error) {
                            Translate.setLanguage(context, 1);
                            Toast.makeText(context, "Произошла ошибка при загрузке нового языка, повторите попытку позже.", Toast.LENGTH_SHORT).show();
                            Log.e("СМЕНА_ЯЗЫКА", error);
                        }
                    });
                    Globals.refreshActivity(context);
                    return true;
                default:
                    return false;
            }
        });


        // Не знаю как это работает. Нужно для того что б появлялись иконочки в POPUP
        Object menuHelper;
        Class[] argTypes;
        try {
            Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
            fMenuHelper.setAccessible(true);
            menuHelper = fMenuHelper.get(popupMenu);
            argTypes = new Class[]{boolean.class};
            menuHelper.getClass().getDeclaredMethod("setForceShowIcon", argTypes).invoke(menuHelper, true);
        } catch (Exception e) {
            // Possible exceptions are NoSuchMethodError and NoSuchFieldError
            //
            // In either case, an exception indicates something is wrong with the reflection code, or the
            // structure of the PopupMenu class or its dependencies has changed.
            //
            // These exceptions should never happen since we're shipping the AppCompat library in our own apk,
            // but in the case that they do, we simply can't force icons to display, so log the error and
            // show the menu normally.
        }

        popupMenu.show();
    }


    /**
     * 12.02.2021
     * id языка
     */
    public static void getTranslateByObjectId() {

    }


    /**
     * 15.05.2021
     */
    public void getTranslates() {
        new TranslationsExchange().downloadTranslations(new ExchangeInterface.Translates() {
            @Override
            public void onSuccess(List<TranslatesSDB> data) {
                SQL_DB.translatesDao().insertAll(data);

//                Globals.translatesList = SQL_DB.translatesDao().getAll();
            }

            @Override
            public void onFailure(String error) {

                try {
//                    List<TranslatesSDB> list = SQL_DB.translatesDao().getAll();

//                    if (list != null) {
//                        Globals.translatesList = list;
//                    } else {
                        Globals.writeToMLOG("INFO", "Translate.getTranslates.onFailure", "String error: " + error);
//                    }
                }catch (Exception e){
                    // todo DB is locked
                }
            }
        });
    }

    public TranslatesSDB getTranslationText(String internalName) {
        Log.e("TRANSLATES_DEBUG", "====================================================");

        Log.e("TRANSLATES_DEBUG", "---internalName: " + internalName);
        Log.e("TRANSLATES_DEBUG", "---Globals.langId: " + Globals.langId);

//        if (Globals.translatesList != null){
//            for (TranslatesSDB item : Globals.translatesList) {
//
//                Gson gson = new Gson();
//                String json = gson.toJson(item);
//                JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
//
//                Log.e("TRANSLATES_DEBUG", "TranslatesSDB: " + convertedObject);
//
//                if (item.internalName.equals(internalName) && item.langId.equals(String.valueOf(Globals.langId))) {
//                    return item;
//                }
//            }
//        }

        return null;
    }


    /**
     * 12.02.2021
     * Отправка на сервер новых переводов.
     * <p>
     * Отправка на сервер новых тестовок на перевод. Отправляются только под Петровым.
     */
    public void uploadNewTranslate() {
        String mod = "translation";
        String act = "translation_add";

        List<AddTranslation> data = getAddTarnslationData();

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().SET_NEW_TRANSLATE(mod, act, data);
        call.enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                Log.e("uploadNewTranslate", "uploadNewTranslate.response: " + response.body());
            }

            @Override
            public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
                Log.e("uploadNewTranslate", "uploadNewTranslate.t:" + t);
            }
        });
    }


    /**
     * 12.02.2021
     * Подготовка данных на выгрузку.
     */
    private List<AddTranslation> getAddTarnslationData() {
        List<AddTranslation> res = new ArrayList<>();

        AddTranslation data = new AddTranslation(
                "1",
                "WP_Title",
                "План работ",
                "WP",
                "",
                ""
        );

        AddTranslation data1 = new AddTranslation(
                "2",
                "date",
                "Дата",
                "app",
                "all",
                ""
        );

        AddTranslation data2 = new AddTranslation(
                "3",
                "address",
                "Адрес",
                "app",
                "all",
                ""
        );


        AddTranslation data3 = new AddTranslation(
                "4",
                "customer",
                "Клиент",
                "app",
                "all",
                ""
        );


        AddTranslation data4 = new AddTranslation(
                "5",
                "executor",
                "Исполнитель",
                "app",
                "all",
                ""
        );


        AddTranslation data5 = new AddTranslation(
                "6",
                "options",
                "Товары",
                "app",
                "all",
                ""
        );


        res.add(data);
//        res.add(data1);
//        res.add(data2);
//        res.add(data3);
//        res.add(data4);
//        res.add(data5);

        return res;
    }


}
