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
import java.util.Objects;

import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.SiteObjectsExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.TranslationsExchange;
import ua.com.merchik.merchik.data.Database.Room.LanguagesSDB;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
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
                } catch (Exception e) {
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

    /*
    создал метод для перевода
     */
    public static String translationText(int id, String defaultText) {
        SiteObjectsSDB obj = SQL_DB.siteObjectsDao().getObjectsByRealId(id);
        return obj != null && obj.comments != null ? obj.comments : defaultText;
    }
//    public static String translationText(int id, String defaultText) {
//        return Objects.requireNonNullElse(
//                SQL_DB.siteObjectsDao().getObjectsByRealId(id).comments, defaultText);
//        }


        /**
         * 12.02.2021
         * Отправка на сервер новых переводов. (заготовок под обьекты)
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

        // Справочник Фото.
        // Список фотоотчётов с типом:

        // Для создания достижения выберите...
        res.add(new AddTranslation(
                "1",
                "message_dialog_mp_title",
                "Історія місцерозташування",
                "app",
                "all",
                ""
        ));
        res.add(new AddTranslation(
                "2",
                "message_dialog_mp_sub_title",
                "Історія місцерозташування виконавця під час відвідування знаходиться в лічильнику на кнопці\"",
                "app",
                "all",
                ""
        ));
        res.add(new AddTranslation(
                "3",
                "message_dialog_mp_message",
                "Визначити та додати поточне розташування пристрою до бази даних?",
                "app",
                "all",
                ""
        ));


//        res.add(new AddTranslation(
//                "2",
//                "detailed_report_home_frag_report_status",
//                "Статус отчёта:",
//                "app",
//                "all",
//                ""
//        ));
//        res.add(new AddTranslation(
//                "3",
//                "detailed_report_home_frag_bonus_plan",
//                "Премия (план):",
//                "app",
//                "all",
//                ""
//        ));
//        res.add(new AddTranslation(
//                "4",
//                "detailed_report_home_frag_reduction_by_options",
//                "Снижение (по опциям):",
//                "app",
//                "all",
//                ""
//        ));
//        res.add(new AddTranslation(
//                "5",
//                "detailed_report_home_frag_prize_fact",
//                "Премия (факт):",
//                "app",
//                "all",
//                ""
//        ));
//        res.add(new AddTranslation(
//                "6",
//                "detailed_report_home_frag_cont_works_doc",
//                "Продолж. работ (по документу):",
//                "app",
//                "all",
//                ""
//        ));
//        res.add(new AddTranslation(
//                "7",
//                "detailed_report_home_frag_theme_cont_works_average",
//                "Продолж. работ (средняя):",
//                "app",
//                "all",
//                ""
//        ));
//        res.add(new AddTranslation(
//                "8",
//                "detailed_report_home_frag_cost_per_hour",
//                "Стоимость часа:",
//                "app",
//                "all",
//                ""
//        ));
//        res.add(new AddTranslation(
//                "9",
//                "detailed_report_home_frag_date",
//                "Дата:",
//                "app",
//                "all",
//                ""
//        ));
//        res.add(new AddTranslation(
//                "10",
//                "detailed_report_home_frag_client",
//                "Клиент:",
//                "app",
//                "all",
//                ""
//        ));
//        res.add(new AddTranslation(
//                "11",
//                "detailed_report_home_frag_executor",
//                "Исполнитель:",
//                "app",
//                "all",
//                ""
//        ));
//        res.add(new AddTranslation(
//                "12",
//                "detailed_report_home_frag_options",
//                "Опции:",
//                "app",
//                "all",
//                ""
//        ));
//        res.add(new AddTranslation(
//                "13",
//                "detailed_report_home_frag_opinion_about_visit",
//                "Мнение исп. о посещении:",
//                "app",
//                "all",
//                ""
//        ));
//        res.add(new AddTranslation(
//                "14",
//                "detailed_report_home_frag_comment_about_visit",
//                "Комментарий исп. о посещении:",
//                "app",
//                "all",
//                ""
//        ));
//        res.add(new AddTranslation(
//                "16",
//                "detailed_report_home_frag_click_add_comment_about_visit",
//                "Нажмите для оставления комментария",
//                "app",
//                "all",
//                ""
//        ));
//        res.add(new AddTranslation(
//                "17",
//                "detailed_report_home_frag_add_comment_about_visit",
//                "Оставить комментарий о посещении",
//                "app",
//                "all",
//                ""
//        ));
//        res.add(new AddTranslation(
//                "18",
//                "detailed_report_home_frag_click_add_opinion_about_visit",
//                "Нажмите для выбора мнения о посещении",
//                "app",
//                "all",
//                ""
//        ));
//        res.add(new AddTranslation(
//                "19",
//                "ufmd_title_opinion_about_visit",
//                "Оставить мнение",
//                "app",
//                "all",
//                ""
//        ));
//        res.add(new AddTranslation(
//                "20",
//                "ufmd_subtitle_opinion_about_visit",
//                "Выберите мнение которое вы хотите оставить о данном посещении",
//                "app",
//                "all",
//                ""
//        ));


//        AddTranslation data2 = new AddTranslation(
//                "2",
//                "alert_dialog_subtitle_server_is_busy",
//                "Время ответа от сервера может быть больше чем обычно",
//                "app",
//                "all",
//                ""
//        );
//
//        AddTranslation data3 = new AddTranslation(
//                "3",
//                "alert_dialog_msg_server_is_busy",
//                "На данный момент сервер загружен и время ожидания может быть больше чем обычно. Ни в коем случае не переустанавливайте приложение, так как время ожидания увеличиться во много раз, и вы можете потерять часть данных, которые не были переданы на сервер." +
//                        "Если после ожидания ни чего не изменилось, повторите вашу попытку через несколько минут",
//                "app",
//                "all",
//                ""
//        );
//        AddTranslation data4 = new AddTranslation(
//                "4",
//                "alert_dialog_message_tovar_group_empty",
//                "Не обнаружено ни одной группы товаров по данному клиенту. Сообщите об этом Администратору!",
//                "app",
//                "all",
//                ""
//        );
//        AddTranslation data5 = new AddTranslation(
//                "5",
//                "alert_dialog_message_type_photo_erorr",
//                "Список типов фото получить не удалось, попробуйте нажать в меню 3х точек на \"Перейти на главную\". Если после этого ошибка повторится - обратитесь к Вашему руководителю.",
//                "app",
//                "all",
//                ""
//        );
//
//        AddTranslation data6 = new AddTranslation(
//                "6",
//                "alert_dialog_message_title_no_data",
//                "Відсутні дані щодо цього відвідування",
//                "app",
//                "all",
//                ""
//        );
//
//        AddTranslation data7 = new AddTranslation(
//                "7",
//                "alert_dialog_message_msg_no_data",
//                "На даний момент немає даних для відображення. Можливо вони ще не завантаженi з боку сервера. Зачекайте завершення обміну даними з сервером, якщо завантаження не вiдбулося знайдіть місце з кращим інтернет-з'єднанням, натисніть 'Синхронізація' (у правому вехньому кутку) і дочекайтеся завершення процесу. Дані мають відобразитися." +
//                        "\nЯкщо це не допомогло, звернiться до керiвника",
//                "app",
//                "all",
//                ""
//        );
//
//        AddTranslation data8 = new AddTranslation(
//                "8",
//                "option_open_error_msg",
//                "Не удалось открыть Опцию. Если ошибка повторяется - обратитесь к своему руководителю.\n\nОшибка: ",
//                "app",
//                "all",
//                ""
//        );
//
//        AddTranslation data9 = new AddTranslation(
//                "9",
//                "alert_dialog_msg_photo_error",
//                "Ошибка при выполнении фото: ",
//                "app",
//                "all",
//                ""
//        );
//
//        AddTranslation data10 = new AddTranslation(
//                "10",
//                "alert_dialog_msg_synchronization",
//                "Синхронизация окончена",
//                "app",
//                "all",
//                ""
//        );
//        AddTranslation data11 = new AddTranslation(
//                "11",
//                "alert_dialog_title_synchronization",
//                "Обмен данными с сервером завершен",
//                "app",
//                "all",
//                ""
//        );
//
//        AddTranslation data12 = new AddTranslation(
//                "12",
//                "alert_dialog_msg_photo_is_there",
//                "Такое фото уже существует. Если ошибка повторяется - обратитесь к Вашему руководителю",
//                "app",
//                "all",
//                ""
//        );
//        AddTranslation data13 = new AddTranslation(
//                "13",
//                "alert_dialog_msg_photo_is_there_2",
//                "Ошибка при сохранении фото. При возникновении этой ошибки - обратитесь к руководителю. Код ошибки: ",
//                "app",
//                "all",
//                ""
//        );
//        AddTranslation data14 = new AddTranslation(
//                "14",
//                "alert_dialog_msg_bd_error",
//                "Ошибка сохранения в БД: ",
//                "app",
//                "all",
//                ""
//        );
//
//        AddTranslation data15 = new AddTranslation(
//                "15",
//                "alert_dialog_msg_photo_server_error",
//                "Фото не выгружено. Сообщите об этом руководителю. Ответ от сервера: ",
//                "app",
//                "all",
//                ""
//        );
//        AddTranslation data16 = new AddTranslation(
//                "16",
//                "alert_dialog_title_default_error",
//                "Произошла ошибка",
//                "app",
//                "all",
//                ""
//        );
//        AddTranslation data17 = new AddTranslation(
//                "17",
//                "alert_dialog_sub_title_default_server_response",
//                "Ответ от сервера",
//                "app",
//                "all",
//                ""
//        );
//        AddTranslation data18 = new AddTranslation(
//                "18",
//                "alert_dialog_msg_error_photo_not_created",
//                "Фото не было создано, повторите попытку",
//                "app",
//                "all",
//                ""
//        );
//        AddTranslation data6 = new AddTranslation(
//                "7",
//                "sub_title_umfd_photo_option_158605",
//                "Фото Вітрини Корпоративний блок, якi зробив виконавець під час відвідування",
//                "app",
//                "all",
//                ""
//        );
//        AddTranslation data7 = new AddTranslation(
//                "8",
//                "sub_title_umfd_photo_option_157277",
//                "Фото Акційного Товару, якi зробив виконавець під час відвідування",
//                "app",
//                "all",
//                ""
//        );
//
//        AddTranslation data8 = new AddTranslation(
//                "9",
//                "dialog_options_control_main_part",
//                "ПТТ работает в отделе",
//                "app",
//                "all",
//                ""
//        );
//
//        AddTranslation data9 = new AddTranslation(
//                "10",
//                "dialog_options_control_cancel_part",
//                " и не может подписывать ЭКЛ для: ",
//                "app",
//                "all",
//                ""
//        );
//
//        AddTranslation data10 = new AddTranslation(
//                "11",
//                "dialog_options_control_accept_20_report",
//                " (но исполнитель не провел свой 20-й отчет и эту блокировку пропускаем)",
//                "app",
//                "all",
//                ""
//        );
//
//        AddTranslation data11 = new AddTranslation(
//                "12",
//                "dialog_options_control_accept_40_report",
//                " (но исполнитель не провел свой 40-й отчет и эту блокировку пропускаем)",
//                "app",
//                "all",
//                ""
//        );
//
//        AddTranslation data12 = new AddTranslation(
//                "13",
//                "dialog_options_control_accept_5_cash_register",
//                " (но в данном магазине меньше 5 касс и это допустимо)",
//                "app",
//                "all",
//                ""
//        );
//
//        AddTranslation data13 = new AddTranslation(
//                "14",
//                "dialog_options_control_cancel_end",
//                " (для магазина в котором более 5 касс и исполнитель провел 20-й отчет)",
//                "app",
//                "all",
//                ""
//        );

//        res.add(data);
//        res.add(data1);
//        res.add(data2);
//        res.add(data3);
//        res.add(data4);
//        res.add(data5);
//        res.add(data6);
//        res.add(data7);
//        res.add(data8);
//        res.add(data9);
//        res.add(data10);
//        res.add(data11);
//        res.add(data12);
//        res.add(data13);
//        res.add(data14);
//        res.add(data15);
//        res.add(data16);
//        res.add(data17);
//        res.add(data18);


        /*
        *
Требование

Точность
Время GPS/GSM (д.м ч:м)
Расстояние до ТТ (м)
Расстояние до ТТ (км)
Источник
*
*
        *
        * */

//        AddTranslation data = new AddTranslation(
//                "1",
//                "AR_name",
//                "Требование",
//                "WP",
//                "",
//                ""
//        );
//
//        AddTranslation data1 = new AddTranslation(
//                "2",
//                "coord_accuracy",
//                "Точность",
//                "app",
//                "all",
//                ""
//        );
//
//        AddTranslation data2 = new AddTranslation(
//                "3",
//                "coord_time_ddMM_hhmm",
//                "Время GPS/GSM (д.м ч:м)",
//                "app",
//                "all",
//                ""
//        );
//
//
//        AddTranslation data3 = new AddTranslation(
//                "4",
//                "coord_distance_m",
//                "Расстояние до ТТ (м)",
//                "app",
//                "all",
//                ""
//        );
//
//
//        AddTranslation data4 = new AddTranslation(
//                "5",
//                "coord_distance_km",
//                "Расстояние до ТТ (км)",
//                "app",
//                "all",
//                ""
//        );

//        String str1="AdditionalRequirementsDB";
//        String str2="addr_id-Код адреса, id-ИД, color-Цвет";

/*
        String[] str1 = new String[20];
        String[] str2 = new String[20];
        String str3="";
        String str4="";
        String[] dparts;
        String[] parts;
        int i;
        int kol=0;

        str1[kol]="LogDB";
        str2[kol]="id-ИД,dt_action-Дата действия,comments-Коммент,tp-Тип,client_id-Код зак.,addr_id-Код адр.," +
                "obj_id-Код оъекта,author-Код автора,dt-Время,session-Сессия,obj_date-дата объекта";
        kol=kol+1;

        str1[kol]="AddressSDB";
        str2[kol]="id-Код адреса,nm-Название,city_id-Код города,tp_id-Код сети,obl_id-Код области,tt_id-Код ТТ,dt_update-ВПИ," +
                "location_xd-Широта,location_yd-Долгота,kol_kass-Кол. касс,nomer_tt-Номер ТТ";
        kol=kol+1;

        str1[kol]="CustomerSDB";
        str2[kol]="id-ИД,nm-Название,edrpou-Код ОКПО,main_tov_grp-Код осн. группы тов.,dt_update-ВПИ,recl_reply_mode-Режим ответа на ЗиР," +
                "ppa_auto-Авто корр. ППА,work_start_date-Дата нач. работ,work_restart_date-Дата возобн. работ";
        kol=kol+1;

        str1[kol]="TradeMarkDB";
        str2[kol]="ID-ИД,nm-Название,dt_update-ВПИ,sort_type-Тип сортировки";
        kol=kol+1;

        str1[kol]="LogMPDB";
        str2[kol]="id-ИД,serverId-ИД сервера,gp-ГП,provider-Поставщик усл.,CoordX-Коо Х,CoordY-Коо У,CoordAltitude-Коо отн.," +
                "CoordTime-Время изм. коо,CoordSpeed-Коо скор.,CoordAccuracy-Точность,mocking-Подделка коо,codeDad2-Код ДАД," +
                "vpi-ВПИ,address-Код адр.,distance-Рассотяние,inPlace-На месте,upload-Время загр.,locationUniqueString-Уник. строка местопол.";
        kol=kol+1;

        str1[kol]="TovarDB";
        str2[kol]="ID-ИД,client_id-Код зак.,client_id2-Код зак.2,nm-Название,weight-Вес,weight_gr-Вес гр.,group_id-Код группы," +
                "manufacturer_id-Код произв.,barcode-Штрихкод,related_tovar_id-Код соп. тов.,dt_update-ВПИ," +
                "sortcol-Порядок сорт.,photo_id-Код фото,height-Ширина,width-Длина,depth-Высота,deleted-Удален,expire_period-Срок год.";
        kol=kol+1;

        str1[kol]="ThemeDB";
        str2[kol]="grp_id-Код группы,tp-Тип,need_photo-Нужна ФО,need_report-Нужна ДО,dt_update-ВПИ";
        kol=kol+1;

        i=1;
        for (int j = 0; j < kol; j++) {
            dparts = str2[j].split(",");
            for (String word : dparts) {
                parts=word.split("-");
                str3=str1[j]+"_"+parts[0].trim();
                str4=parts[1].trim();
                AddTranslation data = new AddTranslation(
                        Integer.toString(i),
                        str3,
                        str4,
                        "app",
                        "all",
                        ""
                );
                res.add(data);
                i=i+1;
            }
        }
*/


        /*
        AddTranslation data1 = new AddTranslation(
                "1",
                "AdditionalRequirementsDB_addr_id",
                "Код адреса",
                "app",
                "all",
                ""
        );

        AddTranslation data2 = new AddTranslation(
                "2",
                "AdditionalRequirementsDB_author_id",
                "Код автора",
                "app",
                "all",
                ""
        );

        AddTranslation data3 = new AddTranslation(
                "3",
                "AdditionalRequirementsDB_disable_score",
                "Без оценки",
                "app",
                "all",
                ""
        );

        AddTranslation data4 = new AddTranslation(
                "4",
                "AdditionalRequirementsDB_dt_change",
                "ВПИ",
                "app",
                "all",
                ""
        );

        AddTranslation data5 = new AddTranslation(
                "5",
                "AdditionalRequirementsDB_exam_id",
                "ИД экзамена",
                "app",
                "all",
                ""
        );

        AddTranslation data6 = new AddTranslation(
                "6",
                "AdditionalRequirementsDB_grp_id",
                "Код группы",
                "app",
                "all",
                ""
        );

        AddTranslation data7 = new AddTranslation(
                "7",
                "AdditionalRequirementsDB_hide_client",
                "Не показ. заказчику",
                "app",
                "all",
                ""
        );

        AddTranslation data8 = new AddTranslation(
                "8",
                "AdditionalRequirementsDB_hide_user",
                "Не показ. сотруднику",
                "app",
                "all",
                ""
        );

        AddTranslation data9 = new AddTranslation(
                "9",
                "AdditionalRequirementsDB_ID",
                "ИД",
                "app",
                "all",
                ""
        );

        AddTranslation data10 = new AddTranslation(
                "10",
                "AdditionalRequirementsDB_not_approve",
                "Не утверждено",
                "app",
                "all",
                ""
        );

        AddTranslation data11 = new AddTranslation(
                "11",
                "AdditionalRequirementsDB_options_id",
                "ИД опций",
                "app",
                "all",
                ""
        );

        AddTranslation data12 = new AddTranslation(
                "12",
                "AdditionalRequirementsDB_showcase_tp_id",
                "ИД типа витрины",
                "app",
                "all",
                ""
        );

        AddTranslation data13 = new AddTranslation(
                "13",
                "AdditionalRequirementsDB_site_id",
                "ИД на сайте",
                "app",
                "all",
                ""
        );

        AddTranslation data14 = new AddTranslation(
                "14",
                "AdditionalRequirementsDB_summ",
                "Сумма",
                "app",
                "all",
                ""
        );

        AddTranslation data15 = new AddTranslation(
                "15",
                "AdditionalRequirementsDB_theme_id",
                "Код темы",
                "app",
                "all",
                ""
        );

        AddTranslation data16 = new AddTranslation(
                "16",
                "AdditionalRequirementsDB_tovar_id",
                "Код товара",
                "app",
                "all",
                ""
        );

        AddTranslation data17 = new AddTranslation(
                "17",
                "AdditionalRequirementsDB_user_id",
                "Код сотрудника",
                "app",
                "all",
                ""
        );

        AddTranslation data18 = new AddTranslation(
                "18",
                "AdditionalRequirementsDB_option_id",
                "Код опции",
                "app",
                "all",
                ""
        );

        AddTranslation data19 = new AddTranslation(
                "19",
                "AdditionalRequirementsDB_dt_end",
                "Дата конца",
                "app",
                "all",
                ""
        );

        AddTranslation data20 = new AddTranslation(
                "20",
                "AdditionalRequirementsDB_dt_start",
                "Дата начала",
                "app",
                "all",
                ""
        );

        AddTranslation data21 = new AddTranslation(
                "21",
                "AdditionalRequirementsDB_client_id",
                "Код заказчика",
                "app",
                "all",
                ""
        );

        AddTranslation data22 = new AddTranslation(
                "22",
                "AdditionalRequirementsDB_color",
                "Цвет",
                "app",
                "all",
                ""
        );

//        res.add(data);
        res.add(data1);
        res.add(data2);
        res.add(data3);
        res.add(data4);
        res.add(data5);
        res.add(data6);
        res.add(data7);
        res.add(data8);
        res.add(data9);
        res.add(data10);
        res.add(data11);
        res.add(data12);
        res.add(data13);
        res.add(data14);
        res.add(data15);
        res.add(data16);
        res.add(data17);
        res.add(data18);
        res.add(data19);
        res.add(data20);
        res.add(data21);
        res.add(data22);

        */

        return res;
    }


}
