package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import static ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm.AdditionalRequirementsModENUM.HIDE_FOR_USER;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogActivity;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.PhotoReportActivity;
import ua.com.merchik.merchik.Utils.UniversalAdapter.UniversalAdapter;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Database.Room.ContentSDB;
import ua.com.merchik.merchik.data.Database.Room.StandartSDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.WPDataObj;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.dialogs.DialogAdditionalRequirements.DialogAdditionalRequirements;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogEKL;
import ua.com.merchik.merchik.toolbar_menus;

public class DetailedReportButtons {
    private final WorkPlan workPlan = new WorkPlan();

    public void buttonClick(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, int mod) {

        int optionId = Integer.parseInt(optionsDB.getOptionId());

        Intent intentPhotoReport = new Intent(context, PhotoReportActivity.class);

        Intent intentPhotoLog = new Intent(context, PhotoLogActivity.class);

        WPDataObj wpDataObj = workPlan.getKPS(wpDataDB.getId());

        PreferenceManager.getDefaultSharedPreferences(context).edit()

                .putLong("wp_data_id", wpDataDB.getId()).apply();
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString("UriToParseFromSite", "").apply();

        Options options = new Options();

        switch (optionId) {
/*            case 135809: // Фото витрины До начала работ
                globals.fixMP(); // Фиксация Местоположения в таблице ЛогМп
                wpDataObj.setPhotoType("14");
                if (mod == 1) {
                    intentPhotoReport.putExtra("dataFromWPObj", wpDataObj);
                    context.startActivity(intentPhotoReport);
                } else {
//                    Log.e("getPhotoType", "wpDataObj.getPhotoType(): " + wpDataObj.getPhotoType());
//                    intentPhotoReport.putExtra("dataFromWPObj", wpDataObj);
//                    MakePhoto.startToMakePhoto(context, wpDataObj);

                    MakePhoto makePhoto = new MakePhoto();
                    makePhoto.pressedMakePhotoOldStyle((Activity) context, wpDataObj, wpDataDB);
                }
                break;*/

            case 158309:
            case 158308:
            case 132968: // Фото витрины
                globals.fixMP(); // Фиксация Местоположения в таблице ЛогМп
                if (mod == 1) {
                    intentPhotoReport.putExtra("dataFromWPObj", wpDataObj);
                    context.startActivity(intentPhotoReport);
                }
                break;

            case 135158: // Фото остатков товаров
                globals.fixMP(); // Фиксация Местоположения в таблице ЛогМп
                wpDataObj.setPhotoType("4");
                if (mod == 1) {
                    intentPhotoReport.putExtra("dataFromWPObj", wpDataObj);
                    context.startActivity(intentPhotoReport);
                } else {
//                    intentPhotoReport.putExtra("dataFromWPObj", wpDataObj);
//                    MakePhoto.startToMakePhoto(context, wpDataObj);
                    MakePhoto makePhoto = new MakePhoto();
                    makePhoto.pressedMakePhotoOldStyle((Activity) context, wpDataObj, wpDataDB);
                }
                break;

            case 132969: // Фото тележка с товаром
                globals.fixMP(); // Фиксация Местоположения в таблице ЛогМп
                wpDataObj.setPhotoType("10");
                if (mod == 1) {
                    intentPhotoReport.putExtra("dataFromWPObj", wpDataObj);
                    context.startActivity(intentPhotoReport);
                } else {
//                    intentPhotoReport.putExtra("dataFromWPObj", wpDataObj);
//                    MakePhoto.startToMakePhoto(context, wpDataObj);
                    MakePhoto makePhoto = new MakePhoto();
                    makePhoto.pressedMakePhotoOldStyle((Activity) context, wpDataObj, wpDataDB);
                }
                break;


            case 141360: // Фото товара на складе
                globals.fixMP(); // Фиксация Местоположения в таблице ЛогМп
                wpDataObj.setPhotoType("31");
                if (mod == 1) {
                    intentPhotoReport.putExtra("dataFromWPObj", wpDataObj);
                    context.startActivity(intentPhotoReport);
                } else {
//                    intentPhotoReport.putExtra("dataFromWPObj", wpDataObj);
//                    MakePhoto.startToMakePhoto(context, wpDataObj);
                    MakePhoto makePhoto = new MakePhoto();
                    makePhoto.pressedMakePhotoOldStyle((Activity) context, wpDataObj, wpDataDB);
                }
                break;

            case 141885: // Фото Документов
                globals.fixMP(); // Фиксация Местоположения в таблице ЛогМп
                wpDataObj.setPhotoType("3");
                if (mod == 1) {
                    intentPhotoReport.putExtra("dataFromWPObj", wpDataObj);
                    context.startActivity(intentPhotoReport);
                } else {
//                    intentPhotoReport.putExtra("dataFromWPObj", wpDataObj);
//                    MakePhoto.startToMakePhoto(context, wpDataObj);
                    MakePhoto makePhoto = new MakePhoto();
                    makePhoto.pressedMakePhotoOldStyle((Activity) context, wpDataObj, wpDataDB);
                }
                break;

            case 138518: // Button Начало работы
                globals.fixMP(); // Фиксация Местоположения в таблице ЛогМп
                // Начало работы.
                // Тут делать запись в БД начала работы
//                Log.e("OPTION_BUTTON", "Начало работы");
//                pressStartWork(context, wpDataDB);
//                sendWpData2();
                break;

            case 138520: // Button окончания работы
                globals.fixMP(); // Фиксация Местоположения в таблице ЛогМп
                // окончания работы.
                // Тут делать запись в БД окончания работы
//                Log.e("OPTION_BUTTON", "Окончания работы");
//                pressEndWork(context, wpDataDB);
//                sendWpData2();
                break;

            case 138773: // 18.08.2020. Button ЗАФИКСИРОВАТЬ МЕСТОПОЛОЖЕНИЕ
                // Фиксация местоположения
                Log.e("OPTION_BUTTON", "Местоположение");
                pressMP(context, wpDataDB);
                break;

            case 137797:
//                // Отобразить диалог с инфой
//                DialogData dialog = new DialogData(context);
//                dialog.setTitle("Представленность");
//
//                String msg = String.format("SKU (План): %s шт.\nSKU (Факт): %s шт.\nОтсутствует: %s шт.\nOOS (out of stock): %s %%\nПредставленность: %s %%\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\tОписание\n\nSKU (План) - количество товарных позиций которые должны быть в торговой точке по плану.\nSKU (Факт) - количество товарных позиций которые фактически стоят на витрине.\nOOS - процент товара, который отсутствует по сравнению с планом\nOOS = 100 - 100*(SKUФакт/SKUПлан) = %s %%\nПредставленность = 100 - OOS = %s %%", (int) Options.SKUPlan, (int) Options.SKUFact, (int) Options.SKUPlan - (int) Options.SKUFact, (int) Options.OOS, (int) Options.OFS, (int) Options.OOS, (int) Options.OFS);
//                dialog.setText(msg);
//
//                dialog.setClose(dialog::dismiss);
//                dialog.show();

                break;

            case 138339:
                Log.e("AdditionalRequirements", "wpDataDB.getClient_id(): " + wpDataDB.getClient_id());
                Log.e("AdditionalRequirements", "wpDataDB.getAddr_id(): " + wpDataDB.getAddr_id());

//                List<AdditionalRequirementsDB> data = AdditionalRequirementsRealm.getData2(String.valueOf(wpDataDB.getClient_id()), String.valueOf(wpDataDB.getAddr_id()));
//                Log.e("AdditionalRequirements", "data.size(): " + data.size());

                List<AdditionalRequirementsDB> data = AdditionalRequirementsRealm.getData3(wpDataDB, HIDE_FOR_USER);
                Log.e("AdditionalRequirements", "data2.size(): " + data.size());


                DialogAdditionalRequirements dialogAdditionalRequirements = new DialogAdditionalRequirements(context);

                dialogAdditionalRequirements.setTitle("Доп. требования (" + data.size() + ")");
                dialogAdditionalRequirements.setRecycler(data);

                dialogAdditionalRequirements.setClose(dialogAdditionalRequirements::dismiss);
                dialogAdditionalRequirements.setLesson(context, true, 1232);
                dialogAdditionalRequirements.setVideoLesson(context, true, 1233, () -> {
                });
                dialogAdditionalRequirements.show();
                break;


            case 141910:
                Log.e("DR_BUTTON_CLICK", "optionId: " + optionId);
                pressReceivingAnOrder(context, wpDataDB);
                break;

            case 141888:
                Log.e("DR_BUTTON_CLICK", "optionId: " + optionId);
                pressRedemptionOfGoods(context, wpDataDB);
                break;

            case 84007:
                Log.e("DR_BUTTON_CLICK", "optionId: " + optionId);
                //  Взятие ЭКЛ-а
                DialogEKL dialogEKL = new DialogEKL(context, wpDataDB);
                dialogEKL.setTitle("Электронный Контрольный Лист (ЭКЛ)");

                dialogEKL.setLesson(context, true, 1273);
                dialogEKL.setVideoLesson(context, true, 1274, () -> {
                });
                dialogEKL.setImgBtnCall(context);
                dialogEKL.setClose(dialogEKL::dismiss);
                dialogEKL.show();
                break;

            case 132666:

                List<StandartSDB> standart = SQL_DB.standartDao().getByDad2(wpDataDB.getCode_dad2());

                UniversalAdapter adapter = new UniversalAdapter(context, standart, new Clicks.click() {
                    @Override
                    public <T> void click(T data) {
                        StandartSDB standartSDB = (StandartSDB) data;
                        ContentSDB content = SQL_DB.contentDao().getById(standartSDB.contentId);

                        if (content != null){
                            DialogData dialog = new DialogData(context);
                            dialog.setTitle("Контент");
                            dialog.setText(content.about);
                            dialog.setClose(dialog::dismiss);
                            dialog.show();
                        }else {
                            Toast.makeText(context, "Контент "+ standartSDB.contentId +" не найден", Toast.LENGTH_SHORT).show();
                        }

                    }
                });




                DialogData dialogData = new DialogData(context);
                dialogData.setTitle("Стандарт " + wpDataDB.getTheme_id());
                dialogData.setText("Отображено " + standart.size() + " Контентов");
                dialogData.setRecycler(adapter, new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                dialogData.setClose(dialogData::dismiss);
                dialogData.show();


                break;

            case 139576:
                options.optionVersion_139576(context, wpDataDB, optionsDB, null, null);
                break;

            case 138767:
                intentPhotoLog.putExtra("planogram", true);
                intentPhotoLog.putExtra("dad2", wpDataDB.getCode_dad2());
                intentPhotoLog.putExtra("customer", wpDataDB.getClient_id());
                intentPhotoLog.putExtra("address", wpDataDB.getAddr_id());
                context.startActivity(intentPhotoLog);
                break;

            case 135742:
                break;


            default:
//                Toast.makeText(context, "Данный раздел находится в разработке", Toast.LENGTH_LONG).show();
                break;
        }

    }



    /**
     * 18.08.2020
     * <p>
     * Запись в БД текущих координат при нажатии на Местоположение
     * <p>
     * НУЖНО ДОПИСАТЬ
     * КАЖДОЕ НАЖАТИЕ ДОЛЖНО
     * ДЕЛАТЬ ЗАПИСЬ В ТАБЛИЦУ МП
     */
    private final Globals globals = new Globals();

    private void pressMP(Context context, WpDataDB wp) {

        // Запись в таблицу Местоположений
        LogMPDB log = new LogMPDB(RealmManager.logMPGetLastId() + 1, globals.POST_10());
        RealmManager.setLogMpRow(log);

        Log.e("LogMp", "LogMpUploadText. LogLASTId: " + RealmManager.logMPGetLastId());

        // Вывод диалога с МП
        if (context instanceof toolbar_menus) {
            ((toolbar_menus) context).dialogMap();
        }

        // Запись в План работ
        int distance;
        if (Globals.measure.equals("м")) {
            distance = (int) Globals.distanceAB;
        } else if (Globals.measure.equals("км")) {
            distance = (int) Globals.distanceAB * 1000;
        } else {
            distance = 0;
        }

        RealmManager.INSTANCE.executeTransaction(realm -> {
            if (wp != null) {
                wp.setVisit_start_geo_distance(distance);
                realm.insertOrUpdate(wp);
                Toast.makeText(context, "Данные о местоположении внесены.", Toast.LENGTH_LONG).show();
            }
        });

    }


    /**
     * 06.08.2020
     * Нажатие на кнопку начала раоты
     * <p>
     * Option id: 138518
     */
    private void pressStartWork(Context context, WpDataDB wp) {
//        globals.writeToMLOG(Clock.getHumanTime() + "_INFO.DetailedReportButtons.class.pressStartWork: " + "ENTER" + "\n");
//        if (wp.getVisit_start_dt() > 0) {
//            Toast.makeText(context, "Работа уже начата!", Toast.LENGTH_SHORT).show();
//        } else {
//            try {
//                long startTime = System.currentTimeMillis() / 1000;
//                RealmManager.INSTANCE.executeTransaction(realm -> {
//                    wp.setDt_update(System.currentTimeMillis() / 1000);
//                    wp.setVisit_start_dt(startTime);
//                    wp.setClient_start_dt(startTime);
//                    wp.startUpdate = true;
//                    realm.insertOrUpdate(wp);
//                });
//                Toast.makeText(context, "Вы начали работу в: " + Clock.getHumanTimeOpt(startTime * 1000), Toast.LENGTH_SHORT).show();
//            } catch (Exception e) {
//                // Set to log error
//                Log.e("test", "test: " + e);
//            }
//        }
    }


    /**
     * 06.08.2020
     * Нажатие на кнопку окончания работы
     * <p>
     * Option id: 138520
     */
    private void pressEndWork(Context context, WpDataDB wp) {
//        globals.writeToMLOG(Clock.getHumanTime() + "_INFO.DetailedReportButtons.class.pressEndWork: " + "ENTER" + "\n");
//        if (wp.getVisit_end_dt() > 0) {
//            Toast.makeText(context, "Работа уже окончена!", Toast.LENGTH_SHORT).show();
//        } else {
//            if (wp.getVisit_start_dt() > 0) {
//                try {
//                    long endTime = System.currentTimeMillis() / 1000;
//                    RealmManager.INSTANCE.executeTransaction(realm -> {
//                        wp.setDt_update(System.currentTimeMillis() / 1000);
//                        wp.setVisit_end_dt(endTime);
//                        wp.setClient_end_dt(endTime);
//                        wp.startUpdate = true;
//                        realm.insertOrUpdate(wp);
//                    });
//                    Toast.makeText(context, "Вы окончили работу в: " + Clock.getHumanTimeOpt(endTime * 1000) + "\n\nНе забудьте нажать 'Провести', что б система проверила текущий документ и начислила Вам премиальные", Toast.LENGTH_SHORT).show();
//                } catch (Exception e) {
//                    // Set to log error
//                }
//            } else {
//                Toast.makeText(context, "Вы не можете закончить работу не начав её", Toast.LENGTH_SHORT).show();
//            }
//        }

    }


    /**
     * 23.06.2021
     * Нажатие на кнопку "Получение заказа в ТТ"
     * <p>
     * Должно отобразить сообщение "что человек должен сделать" для того что б взять Заказ.
     * Внести номер заказа
     */
    private void pressReceivingAnOrder(Context context, WpDataDB wp) {

        List<ReportPrepareDB> rp = ReportPrepareRealm.getReportPrepareByDad2(wp.getCode_dad2());


        String msg;

        if (DetailedReportActivity.rpAmountSum > 0) {
            msg = "Вы заказали " + DetailedReportActivity.rpCount + " наименований товаров общим количеством " + DetailedReportActivity.rpAmountSum + " шт";
        } else {
            msg = "Вы должны выполнить заказ товара (указан в разделе 'Товары') у 'баера' данной ТТ и его номер указать в данной форме\n";
        }


        // Отображение результата
        DialogData dialog = new DialogData(context);

        dialog.setTitle("Получение заказа в ТТ");
        dialog.setText(msg);

        dialog.setOperation(DialogData.Operations.Number, "" + rp.get(0).buyerOrderId, null, () -> {

            Integer code;
            if (dialog.getOperationResult().equals("")){
                code = 0;
            }else {
                code = Integer.valueOf(dialog.getOperationResult());
            }

            RealmManager.INSTANCE.executeTransaction(realm -> {
                for (ReportPrepareDB item : rp) {
                    item.buyerOrderId = code;
                    item.setUploadStatus(1);
                }

                realm.insertOrUpdate(rp);
            });

            Toast.makeText(context, "Внесли номер заказа: " + code, Toast.LENGTH_SHORT).show();
        });

        dialog.setClose(dialog::dismiss);
        dialog.show();
    }


    /**
     * 29.06.2021
     * Нажатие на кнопку "Выкуп Товара с ТТ"
     */
    private void pressRedemptionOfGoods(Context context, WpDataDB wp) {
        String msg = "";
        String sum = String.format("%.2f", DetailedReportActivity.rpTotalSumToRedemptionOfGoods);    // Текстовое "обрезанное" значение allSum

        //1. "Вы приобрели продукцию на сумму СУММА. Получите дальнейшие инструкции у руководителя"
        //2. "Вы должны выкупить в ТТ товар(указанный в списке на сумму КОЛИЧЕСТВО грн. Инструкции у руководителя ТЕЛЕФОН)"
        if (DetailedReportActivity.rpTotalSumToRedemptionOfGoods > 0) {
            msg = "Вы приобрели продукцию на сумму " + sum + ". Получите дальнейшие инструкции у руководителя";
        } else {
            msg = "Вы должны выкупить в ТТ товар (указанный в списке) на сумму " + DetailedReportActivity.rpTotalSumToRedemptionOfGoods + " грн. и записать цены и количества выкупленого товара в разделе Товары. Инструкции в стандартах и доп. требованиях";
        }


        // Отображение результата
        DialogData dialog = new DialogData(context);

        dialog.setTitle("Заголовок 141888й");
        dialog.setText(msg);

        dialog.setClose(dialog::dismiss);

        dialog.show();
    }


}
