package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.NEED_UPDATE_UI_REQUEST;
import static ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogAdapter.photoData;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.Activities.Features.FeaturesActivity;
import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogPhotoAdapter;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammVizitShowcaseSDB;
import ua.com.merchik.merchik.data.Database.Room.VoteSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.dataLayer.ContextUI;
import ua.com.merchik.merchik.dataLayer.ModeUI;
import ua.com.merchik.merchik.dataLayer.common.VizitShowcaseDataHolder;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogFullPhoto;
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR;
import ua.com.merchik.merchik.features.main.DBViewModels.PlanogrammVizitShowcaseViewModel;

public class OptionControlPlanorammVizit<T> extends OptionControl {
    public int OPTION_CONTROL_PLANOGRAMM_VIZIT = 168439;

    private WpDataDB wpDataDB;

    private int optionAmountMin;

    public boolean signal = false;

    private long code_dad2 = 0L;

    public OptionControlPlanorammVizit(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        this.unlockCodeResultListener = unlockCodeResultListener;
        getDocumentVar();
        executeOption();
    }

    private void getDocumentVar() {
        try {
            if (document instanceof WpDataDB) {
                this.wpDataDB = (WpDataDB) document;
            }

            optionAmountMin = Integer.parseInt(optionDB.getAmountMin());
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlPlanorammVizit/getDocumentVar", "Exception e: " + e);
        }
    }

    private void executeOption() {
        try {
            if (wpDataDB != null) {
                code_dad2 = wpDataDB.getCode_dad2();
            }
            //3.0. створимо перелік ідентифікаторів Планограм, котрі БУЛИ актуальні на момент початку роботи по цьому відвідуванню.
            //3.1. Ідея така! У момент початку робіт з цією опцією (можливо при інших обставинах) генерується перелік записів у таблицю ПланограмиПоВізитам по кожному ідентифікатору планограм окремо. Таким чином ми отримаємо перелік планограм (+ідентифікаторів ) актуальний на момент початку робіт і більш ми його не змінюємо, а користувач на боці Додатку визначає до якої вітрини має відношення дана планограма.
            List<PlanogrammVizitShowcaseSDB> planogrammVizitShowcaseSDBList;
            try {
                planogrammVizitShowcaseSDBList = SQL_DB.planogrammVizitShowcaseDao().getByCodeDad2(wpDataDB.getCode_dad2());
            } catch (Exception e) {
                planogrammVizitShowcaseSDBList = List.of();
            }


            //4.0. тепер треба перевірити чи до усіх планограм визначені вітрини, або світлини ДО. або встановлені низькі оцінки ... Ідея така ..
            // Виконується перевірка наявності планограм встановлених до моменту початку візиту, а потім з"ясовується, чи визначені вітрини, до котрих стосуються зазначені планограми. Якщо вітрини, на момент виконання робіт, ще нема, то виконується світлина ДО з зазначенням іденифікатора планогрмаи як джерела. І у випадку, якщо виконавець вважає, що планограма "крива" (не має виконуватись у даній ТТ), то він ставить їй низьку оцінку та коментар.
            int issuesCount = 0;
            for (PlanogrammVizitShowcaseSDB item : planogrammVizitShowcaseSDBList) {
                VoteSDB vote = SQL_DB.votesDao().getVote(
                        code_dad2,
                        item.planogram_photo_id,
                        5
                );
                Integer score = vote != null ? vote.score : null;
                item.score = score == null ? "0" : String.valueOf(score);

//                String comment = "\nДля планограми: " + item.planogram_id + " (" + item.planogram_photo_id + ")";
//                StackPhotoDB stackPhotoDB = RealmManager.getPhotoByPhotoId(item.planogram_photo_id.toString());
                // Создаем SpannableStringBuilder для кликабельной части
                String clickableText = item.planogram_id + " (" + item.planogram_photo_id + ")";
                SpannableStringBuilder clickableSpanBuilder = new SpannableStringBuilder(clickableText);

                // Делаем часть текста кликабельной
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        openFeatureActivity(item.planogram_id);
                        // Вызываем метод dialogFullPhoto при клике
//                        dialogFullPhoto(stackPhotoDB);
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(Color.GREEN); // Цвет ссылки
                        ds.setUnderlineText(true); // Подчеркивание
                    }
                };

                // Применяем ClickableSpan к нужному тексту (всей строке или части)
                clickableSpanBuilder.setSpan(
                        clickableSpan,
                        0, // Начало текста (включая "Для планограми: ")
                        clickableText.length(), // Конец текста
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );


                boolean isShowcaseMissing = item.showcase_id == 0 && item.photo_do_id == 0 && (item.photo_do_hash == null || item.photo_do_hash.isEmpty() || item.photo_do_hash.equals("0"));
                boolean isRatingMissing = item.score.equals("0");
                notCloseSpannableStringBuilderDialog = true;
                if (isShowcaseMissing && isRatingMissing) {
                    signal = true;
                    spannableStringBuilder
                            .append("\nДля планограми: ")
                            .append(clickableSpanBuilder)
                            .append(" НЕ ОБРАНА ВІТРИНА (на котрій треба виконувати зазначену планограму).");
                    issuesCount++;
                } else {
                    signal = false;
                    spannableStringBuilder.append("");
                }
            }

            //5.0. готовим сообщение и сигнал
            if (planogrammVizitShowcaseSDBList.isEmpty()) {
                spannableStringBuilder.append("\n\nДля поточного візиту, на момент його створення, не були визначені планограми. Зауважень нема.");
                signal = false;
            } else if (issuesCount > 0) {
                spannableStringBuilder.append("\n\nЗнайдено ").append(String.valueOf(issuesCount))
                        .append(" планограм, для котрих НЕ визначені вітрини, на котрих повинна здійснюватся викладка (по зазначеним планограмам). Перелік див. у таблиці.");
                signal = true;
            } else {
                spannableStringBuilder.append("\nЗнайдено ").append(String.valueOf(planogrammVizitShowcaseSDBList.size()))
                        .append(" Планограмм для котрих визначені вітрини (або Фото, або Оцінки). Зауважень нема.\n");
                signal = false;
            }

            //5.1 применяем сигнал и финальное сообщение
            if (signal) {
                if (optionDB.getBlockPns().equals("1") && wpDataDB.getStatus() == 0) {    //блокировать проведение, если есть сигнал
                    spannableStringBuilder.append("\n\nДокумент проведено НЕ БУДЕ!");
                } else {
                    spannableStringBuilder.append("\n\n Ви можете отримати БІЛЬШІ преміальні, якщо визначите вітрини на котрих мають виконуватись планограми.");
                }
            }


            //6.0. исключения
            //6.1. исключение на период отладки
//            LocalDate debugUntilDate = LocalDate.of(2025, 5, 1);
//            if (LocalDate.now().isBefore(debugUntilDate.plusDays(1))) {
//                signal = false;
//            }


            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionDB != null) {
                    if (signal) {
                        optionDB.setIsSignal("1");
                    } else {
                        optionDB.setIsSignal("2");
                    }
                    realm.insertOrUpdate(optionDB);
                }
            });

            setIsBlockOption(signal);

            checkUnlockCode(optionDB);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlPlanorammVizit/executeOption", "Exception e: " + e);
        }
    }

    private void openFeatureActivity(int planogramId) {
        VizitShowcaseDataHolder.Companion.getInstance().clear();

        Intent intent = new Intent(context, FeaturesActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("viewModel", PlanogrammVizitShowcaseViewModel.class.getCanonicalName());
        bundle.putString("contextUI", ContextUI.PLANOGRAMM_VIZIT_SHOWCASE.toString());
        bundle.putString("modeUI", ModeUI.DEFAULT.toString());
        JsonObject dataJson = new JsonObject();
        dataJson.addProperty("clientId", String.valueOf(wpDataDB.getClient_id()));
        dataJson.addProperty("addressId", wpDataDB.getAddr_id());
        dataJson.addProperty("wpDataDBId", String.valueOf(wpDataDB.getCode_dad2()));
        dataJson.addProperty("optionDBId", String.valueOf(optionDB.getID()));
        dataJson.addProperty("colorUiContainer", planogramId);
        bundle.putString("dataJson", new Gson().toJson(dataJson));

        bundle.putString("title", "Планограма > Вітрина");
        bundle.putString(
                "subTitle",
                "Для кожної Планограми вкажіть Вітрину, на якiй товар буде викладено згідно поточної планограми. Якщо Фото відповідної вітрини у списку вітрин немає, виберіть Фото Вітрини. Якщо у ТТ немає Вітрини для якої створена ця Планограма, то оцініть цю Планограму низькою оцінкою (нижче 5) і вкажіть коментар"
        );
        intent.putExtras(bundle);

        ActivityCompat.startActivityForResult((Activity) context, intent, NEED_UPDATE_UI_REQUEST, null);
    }

    private void dialogFullPhoto(StackPhotoDB stackPhotoDB) {
        try {
            DialogFullPhoto dialog = new DialogFullPhoto(context);
            dialog.setWpDataDB(wpDataDB);
            dialog.setRatingType(DialogFullPhoto.RatingType.PLANOGRAM);
            dialog.setPhotos(0, Collections.singletonList(stackPhotoDB), new PhotoLogPhotoAdapter.OnPhotoClickListener() {
                @Override
                public void onPhotoClicked(Context context, StackPhotoDB photoDB) {
                    try {
                        DialogFullPhotoR dialogFullPhoto = new DialogFullPhotoR(context);
                        dialogFullPhoto.setPhoto(stackPhotoDB);

                        // Pika
                        dialogFullPhoto.setComment(stackPhotoDB.getComment());

                        dialogFullPhoto.setClose(dialogFullPhoto::dismiss);
                        dialogFullPhoto.show();
                    } catch (Exception e) {
                        Log.e("ShowcaseAdapter", "Exception e: " + e);
                    }
                }
            }, () -> {
            });

            dialog.setTextInfo(photoData(stackPhotoDB));
            dialog.setClose(dialog::dismiss);
            dialog.setRating();
            dialog.setDvi();
            dialog.show();
        } catch (Exception e) {

        }
    }
}
