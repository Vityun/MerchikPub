package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;
import static ua.com.merchik.merchik.menu_main.decodeSampledBitmapFromResource;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ua.com.merchik.merchik.Activities.FullScreenPhotoActivity.PhotoFragments;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARActivity;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.FabYoutube;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.data.Database.Room.FragmentSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.AddressRealm;
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.TasksAndReclamationsRealm;
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm;
import ua.com.merchik.merchik.database.realm.tables.UsersRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR;
import ua.com.merchik.merchik.dialogs.DialogPhotoTovar;
import ua.com.merchik.merchik.toolbar_menus;

public class Tab1Fragment extends Fragment {

    private Context mContext;

    private FabYoutube fabYoutube = new FabYoutube();
    private FloatingActionButton fabYouTube;
    private TextView badgeTextView;
    public static final Integer[] Tab1Fragment_VIDEO_LESSONS = new Integer[]{3528, 4208};

    private TextView textViewData, goToWpData;

    // Pika создание блока для последнего комментария, кликнув на котором перейти в закладку комментариев
    private TextView goToTab3;
    public static TARSecondFrag secFrag;

    private ImageView imageView, imageView2;
    private RatingBar ratingBar1, ratingBar2;

    private TasksAndReclamationsSDB data;

    public Tab1Fragment() {
    }

    public Tab1Fragment(TasksAndReclamationsSDB data) {
        this.data = data;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_item_first, container, false);

        mContext = v.getContext();

        textViewData = v.findViewById(R.id.text_data);
        textViewData.setMovementMethod(LinkMovementMethod.getInstance());
        goToWpData = v.findViewById(R.id.wpLink);
        imageView = v.findViewById(R.id.TARPhoto);
        imageView2 = v.findViewById(R.id.TARPhoto2);
        ratingBar1 = v.findViewById(R.id.ratingBar2);
        ratingBar2 = v.findViewById(R.id.ratingBar4);
        // Pika блок для последнего комментария, кликнув на котором перейти в закладку комментариев
        goToTab3 = v.findViewById(R.id.lastComment);

        // Pika - вопрос как сделать так чтоб моя переменная secFrag теперь ссылалась именно на тот
        // экземпляр класса который создан и его меню уже отображено
        // тогда я вот так вызову его публичный метод clickOn3(), который я там создал и он должен сымитировать клик на вкладке "Комментарии"
        goToTab3.setOnClickListener(view -> {
            if (secFrag != null) {
                secFrag.clickOn3();
            }
        });

        fabYouTube = v.findViewById(R.id.fab3);
        badgeTextView = v.findViewById(R.id.badge_text_view_tar);

        fabYoutube.setFabVideo(fabYouTube, Tab1Fragment_VIDEO_LESSONS, () -> fabYoutube.showYouTubeFab(fabYouTube, badgeTextView, Tab1Fragment_VIDEO_LESSONS));
        fabYoutube.showYouTubeFab(fabYouTube, badgeTextView, Tab1Fragment_VIDEO_LESSONS);

        SpannableString spannableString = new SpannableString("Перейти в Отчёт Исполнителя..");
        spannableString.setSpan(new URLSpan(""), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        goToWpData.setText(spannableString);
        goToWpData.setVisibility(View.GONE);
        goToWpData.setOnClickListener((view) -> {
        });

        setData();

        if (TARActivity.TARType == 1) {
            toolbar_menus.textLesson = 1183;
//            toolbar_menus.videoLesson = 3528;   // 4208
            toolbar_menus.videoLessons = new Integer[]{3528, 4208};
            Log.e("SET_TAR_FAB", "Tab1Fragment 1");
        } else {
            toolbar_menus.textLesson = 1185;
//            toolbar_menus.videoLesson = 3528;   // 4208
            toolbar_menus.videoLessons = new Integer[]{3528, 4208};
            Log.e("SET_TAR_FAB", "Tab1Fragment 0");
        }


        toolbar_menus.setFab(v.getContext(), TARActivity.fab, ()->{}); // ГЛАВНАЯ

        return v;
    }

    private void setFragmentsOnPhoto() {
        List<FragmentSDB> fragmentSDB = SQL_DB.fragmentDao().getAllByPhotoId(Integer.parseInt(String.valueOf(data.photo)));
        for (int i = 0; i < fragmentSDB.size(); i++) {
            new PhotoFragments().setPhotoFragment(fragmentSDB.get(i), String.valueOf(i + 1), imageView, PhotoFragments.PhotoFragmentsSize.SMALL);
        }
    }


    /**
     * 22.03.2021
     */
    private void setData() {
        SpannableStringBuilder stringData = new SpannableStringBuilder();

        try {
            CharSequence info = Html.fromHtml("<b>ID: </b>" + data.id + " / " + data.id1c + "<br>");
            stringData.append(info);
        } catch (Exception e) {
        }


        try {
            CharSequence setTaskDate = Html.fromHtml("<b>Поставлена: </b>" + Clock.getHumanTime2(Long.valueOf(data.dtRealPost)) + "<br>");
            stringData.append(setTaskDate);
        } catch (Exception e) {
        }


        // ---
        try {
            WpDataDB wpDataDB = WpDataRealm.getWpDataRowByDad2Id(data.codeDad2SrcDoc);
            Spanned setDateSrcDock = Html.fromHtml("<b>Дата посещения: </b>" + Clock.getHumanTime3(wpDataDB.getDt().getTime() / 1000) + "<br>");
            stringData.append(setDateSrcDock);
        } catch (Exception e) {
        }
        // ---


        try {
            String s = "";

            ThemeDB theme = ThemeRealm.getThemeById(String.valueOf(data.themeId));
            if (theme != null) {
                if (theme.getNm() != null && !theme.getNm().equals("")) {
                    s = theme.getNm();
                } else {
                    s = String.valueOf(data.themeId);
                }
            }

            CharSequence customer = Html.fromHtml("<b>Тема: </b>" + s + "<br>");
            stringData.append(customer);
        } catch (Exception e) {
        }


        try {
            int tp = data.tp;
            int state = data.state;

            CharSequence status = Html.fromHtml("<b>Статус: </b>" + TasksAndReclamationsRealm.getStatusTxt(tp, state) + "<br>");
            stringData.append(status);
        } catch (Exception e) {
        }


        try {
            String money;
            String title;

            title = "<b>Сумма штрафа: </b>";
//            if (data.state == 1 || data.state == 3) {
//                money = "0";
//            } else {
                money = "<font color='red'> -" + data.sumPenalty + " (виновнику)</font>";
//            }


            CharSequence penalty = Html.fromHtml(title + money + "<br>");
            stringData.append(penalty);

            if (data.zamenaUserId != null && data.zamenaUserId != 0) {
                String titlePr = "<b>Сумма премии: </b>";
                String moneyPr = "+" + data.sumPenalty + " (замене)";

                CharSequence premiya = Html.fromHtml(titlePr + moneyPr + "<br>");
                stringData.append(premiya);
            }
        } catch (Exception e) {
            Log.d("test", "test" + e);
        }


        try {
            CharSequence address = Html.fromHtml("<b>Адрес: </b>" + AddressRealm.getAddressById(data.addr).getNm() + "<br>");
            stringData.append(address);
        } catch (Exception e) {
        }

        try {
            CharSequence customer = Html.fromHtml("<b>Заказчик: </b>" + CustomerRealm.getCustomerById(String.valueOf(data.client)).getNm() + "<br>");
            stringData.append(customer);
        } catch (Exception e) {
        }

        try {
            CharSequence autor = Html.fromHtml("<b>Автор: </b>" + UsersRealm.getUsersDBById(data.author).getNm() + "<br>");
            stringData.append(autor);
        } catch (Exception e) {
        }

        try {
            CharSequence autor = Html.fromHtml("<b>Виновник: </b>" + UsersRealm.getUsersDBById(data.vinovnik).getNm() + "<br>");
            stringData.append(autor);
        } catch (Exception e) {
        }

        try {
            String zamena = "";
            if (data.zamenaUserId != null && data.zamenaUserId != 0) {
                zamena = UsersRealm.getUsersDBById(data.zamenaUserId).getNm();
            } else {
                zamena = "Не установлена";
            }

            CharSequence autor = Html.fromHtml("<b>Замена: </b>" + zamena + "<br>");
            stringData.append(autor);
        } catch (Exception e) {
        }


        try {
            CharSequence autorTel = Html.fromHtml("<b>Телефон автора: </b>" + data.telNum + "<br>");
            stringData.append(autorTel);
        } catch (Exception e) {
        }

        try {
            CharSequence answer = Html.fromHtml("<b>Текст задачи: </b>" + data.comment + "<br>");
            stringData.append(answer);
        } catch (Exception e) {
        }

        try {
            CharSequence answer = Html.fromHtml("<b>Ответ: </b>" + data.lastAnswer + "<br>");
            stringData.append(answer);
        } catch (Exception e) {
        }


        try {
            CharSequence dad2A = Html.fromHtml("<b>ДАД2 (А): </b>" + data.codeDad2 + "<br>");
            stringData.append(dad2A);
        } catch (Exception e) {
        }


        try {
            CharSequence dad2B = Html.fromHtml("<b>ДАД2 (Б): </b>" + data.codeDad2SrcDoc + "<br>");
            stringData.append(dad2B);
        } catch (Exception e) {
        }


        try {
//            CharSequence qwe = Html.fromHtml("<b>----------</b>" + "" + "<br>");
//            stringData.append(qwe);
//
//            CompositeDisposable disposable = new CompositeDisposable();
//            disposable.add(SQL_DB.opinionDao().getOpinionByIdF(data.sotrOpinionId)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(res -> {
//                        CharSequence opinion = Html.fromHtml("<b>Мнение: </b>" + res.nm + "<br>");
//                        opinion = Html.fromHtml("<b>Мнение: </b>" + res.nm + "<br>");
//                        stringData.append(opinion);
//                        disposable.dispose();
//                    })
//            );

            CharSequence opinion = Html.fromHtml("<b>Мнение: </b>" + SQL_DB.opinionDao().getOpinionById(data.sotrOpinionId).nm + "<br>");
            stringData.append(opinion);

//            CharSequence qwe2 = Html.fromHtml("<b>----------</b>" + "" + "<br>");
//            stringData.append(qwe2);
        } catch (Exception e) {
            Log.e("test", "test");
        }

        try {
            CharSequence opinionUser = Html.fromHtml("<b>Автор мнения: </b>" + SQL_DB.usersDao().getById(data.sotrOpinionAuthorId).fio + "<br>");
            stringData.append(opinionUser);
        } catch (Exception e) {
        }

        try {
            CharSequence opinionDate = Html.fromHtml("<b>Дата мнения: </b>" + (data.sotrOpinionDt > 0 ? Clock.getHumanTime3(data.sotrOpinionDt) : "Не указано") + "<br>");
            stringData.append(opinionDate);
        } catch (Exception e) {
        }


        textViewData.setText(stringData);

        // Pika
        if (data.lastAnswer != null) {
            goToTab3.setVisibility(View.VISIBLE);
            goToTab3.setText(data.lastAnswer);
        }
        else {
            goToTab3.setVisibility(View.GONE);
        }

        Uri photo1 = getPhotoPath(String.valueOf(data.photo));
        Uri photo2 = getPhotoPath(String.valueOf(data.photo2));

        Log.e("downloadAndSetFullPhoto", "photo1: " + photo1);
        Log.e("downloadAndSetFullPhoto", "photo2: " + photo2);


//        imageView.setImageURI(photo1);
        if (photo1 != null) {
            File file = new File(photo1.toString());
            Bitmap b = decodeSampledBitmapFromResource(file, 200, 200);
            if (b != null) {
                imageView.setImageBitmap(b);
                setFragmentsOnPhoto();
                try {
                    List<FragmentSDB> fragmentSDB = SQL_DB.fragmentDao().getAllByPhotoId(Integer.parseInt(String.valueOf(data.photo)));
                    StackPhotoDB stackPhotoDB = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(String.valueOf(data.photo));
                    if (stackPhotoDB != null && fragmentSDB != null && fragmentSDB.size() > 0){
                        DialogFullPhotoR dialog = new DialogFullPhotoR(mContext);
                        dialog.setPhoto(stackPhotoDB);

                        // Pika
                        dialog.setComment(stackPhotoDB.getComment());

                        dialog.setClose(dialog::dismiss);
                        dialog.show();
                    }
                }catch (Exception e){

                }
            }
        }


//        imageView2.setImageURI(photo2);
        if (photo2 != null) {
            File file2 = new File(photo2.toString());
            Bitmap b2 = decodeSampledBitmapFromResource(file2, 200, 200);
            if (b2 != null) {
                imageView2.setImageBitmap(b2);
            }
        }


        imageView.setOnClickListener(v -> {
            downloadAndSetFullPhoto(String.valueOf(data.photo));
        });

        imageView2.setOnClickListener(v -> {
            try {
                downloadAndSetFullPhoto(String.valueOf(data.photo2));
            } catch (Exception e) {
                Toast.makeText(imageView.getContext(), "НЕ УДАЛОСЬ СКАЧАТЬ ФОТО! \n\nОбратитесь к Вашему руководителю. \n\nОшибка: " + e, Toast.LENGTH_LONG).show();
            }
        });


        if (data.voteScore != null) {
            ratingBar1.setRating(data.voteScore);
        } else {
            ratingBar1.setRating(0);
        }

        if (data.vinovnikScore != null) {
            ratingBar2.setRating(data.vinovnikScore);
        } else {
            ratingBar2.setRating(0);
        }


        ratingBar1.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            int rate = (int) rating;
            ratingBar.setRating(rate);
            saveRatingTAR(rate);

/*            if (rate > 5){
                saveRatingTAR(rate);
                Toast.makeText(ratingBar.getContext(), "Оценка: " + rate + " установлена.", Toast.LENGTH_SHORT).show();
            }else {
                DialogData dialog = new DialogData(getContext());
                dialog.setTitle("Низкая оценка");
                dialog.setText("Прокомментируйте причину низкой оценки.");
                dialog.setOperation(DialogData.Operations.TEXT, "Ваш Комментарий", null, ()->{});
                dialog.setCancel("Сохранить", ()->{
                    String comment = dialog.getOperationResult();

                    if (comment != null && comment.length() > 1){
                        // Сохранение коммента
                        Toast.makeText(ratingBar.getContext(), "Комментарий: " + comment + " сохранён.", Toast.LENGTH_SHORT).show();

                        saveRatingTAR(rate);
                        dialog.dismiss();
                    }else {
                        Toast.makeText(dialog.context, "Комментарий НЕ сохранён. Заполните корректно поле для комментария!", Toast.LENGTH_LONG).show();
                    }
                });
                dialog.setClose(()->{
                    Toast.makeText(getContext(), "Комментарий НЕ сохранён", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            }*/
        });

        ratingBar2.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            int rate = (int) rating;
            ratingBar.setRating(rate);

            Globals.writeToMLOG("INFO", "Tab1Fragment/ratingBar2.setOnRatingBarChangeListener", "rate: " + rate);

            if (rate > 5) {
                saveRatingTARVote(rate, null);
                Toast.makeText(ratingBar.getContext(), "Оценка: " + rate + " установлена.", Toast.LENGTH_SHORT).show();
            } else {
                DialogData dialog = new DialogData(getContext());
                dialog.setTitle("Низкая оценка");
                dialog.setText("Прокомментируйте причину низкой оценки.");
                dialog.setOperation(DialogData.Operations.TEXT, "Ваш Комментарий", null, () -> {
                });
                dialog.setCancel("Сохранить", () -> {
                    String comment = dialog.getOperationResult();

                    if (comment != null && comment.length() > 1) {
                        // Сохранение коммента
                        Toast.makeText(ratingBar.getContext(), "Комментарий: " + comment + " сохранён.", Toast.LENGTH_SHORT).show();

                        saveRatingTARVote(rate, comment);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(dialog.context, "Комментарий НЕ сохранён. Заполните корректно поле для комментария!", Toast.LENGTH_LONG).show();
                    }
                });
                dialog.setClose(() -> {
                    Toast.makeText(getContext(), "Комментарий НЕ сохранён", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
                dialog.show();
            }
        });
    }

    /**
     * Сохранение рейтинга ЗАДАЧИ
     */
    private void saveRatingTAR(int rate) {
        data.voteScore = rate;
        SQL_DB.tarDao().insertData(Collections.singletonList(data))
                .subscribeOn(Schedulers.io())
                .subscribe(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Log.d("test", "test");
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        Log.d("test", "test");
                    }
                });
        Exchange.updateTAR(data);
    }

    /**
     * Сохранение Оценки Обьективности
     */
    private void saveRatingTARVote(int rate, String comment) {
        data.vinovnikScore = rate;
        data.vinovnikScoreDt = System.currentTimeMillis();
        data.vinovnikScoreUserId = Globals.userId;
        if (comment != null && !comment.equals("")) {
            data.vinovnikScoreComment = comment;
        }
        SQL_DB.tarDao().insertData(Collections.singletonList(data))
                .subscribeOn(Schedulers.io())
                .subscribe(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Log.d("test", "test");
                        Globals.writeToMLOG("INFO", "Tab1Fragment/ratingBar2.setOnRatingBarChangeListener/saveRatingTARVote", "onComplete");
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        Log.d("test", "test");
                        Globals.writeToMLOG("INFO", "Tab1Fragment/ratingBar2.setOnRatingBarChangeListener/saveRatingTARVote", "onError Throwable e: " + e);
                    }
                });

        Exchange.updateTAR(data);
    }


    /**
     * 22.03.2021 (ты явно не тут должна быть) TODO
     *
     * @return
     */
    private Uri getPhotoPath(String photo) {
        Log.e("getPhotoPath", "photo: " + photo);
        try {
            if (photo != null && !photo.equals("") && !photo.equals("0")) {
                StackPhotoDB stackPhotoDB = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(photo);

                if (stackPhotoDB == null) {
                    stackPhotoDB = StackPhotoRealm.getById(Integer.parseInt(photo));
                }

                Log.e("getPhotoPath", "stackPhotoDB: " + stackPhotoDB.getPhotoServerId());
                Log.e("getPhotoPath", "stackPhotoDB: " + stackPhotoDB.getPhotoServerURL());

                return Uri.parse(stackPhotoDB.getPhoto_num());
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 23.03.21
     * Скачивание и установка полноразмерной фотографии при нажатии на фото.
     */
    private void downloadAndSetFullPhoto(String photo) {
        StackPhotoDB stackPhotoDB = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(photo);

        if (stackPhotoDB == null) {
            stackPhotoDB = StackPhotoRealm.getById(Integer.parseInt(photo));

            DialogPhotoTovar dialogPhotoTovar = new DialogPhotoTovar(mContext);
//            dialogPhotoTovar.setPhotoTovar(Uri.parse(stackPhotoDB.getPhoto_num()));
            dialogPhotoTovar.setPhotoTovar(stackPhotoDB);
            dialogPhotoTovar.setClose(dialogPhotoTovar::dismiss);
            dialogPhotoTovar.show();

            return;
        }

        if (stackPhotoDB != null) {
//            Gson gson = new Gson();
//            String json = gson.toJson(stackPhotoDB);
//            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            Log.e("downloadAndSetFullPhoto", "convertedObject: " + stackPhotoDB);

//        Log.e("stackPhotoDB", "stackPhotoDB.size: " + stackPhotoDB.getPhoto_size());

            StackPhotoDB finalStackPhotoDB = stackPhotoDB;
            new PhotoDownload().downloadPhoto(true, stackPhotoDB, "/TaR", new PhotoDownload.downloadPhotoInterface() {
                @Override
                public void onSuccess(StackPhotoDB data) {
//                    DialogPhotoTovar dialogPhotoTovar = new DialogPhotoTovar(mContext);
////                    dialogPhotoTovar.setPhotoTovar(Uri.parse(finalStackPhotoDB.getPhoto_num()));
//                    dialogPhotoTovar.setPhotoTovar(finalStackPhotoDB);
//                    dialogPhotoTovar.setClose(dialogPhotoTovar::dismiss);
//                    dialogPhotoTovar.show();

                    DialogFullPhotoR dialog = new DialogFullPhotoR(mContext);
                    dialog.setPhoto(finalStackPhotoDB);

                    // Pika
                    dialog.setComment(data.getComment());

                    dialog.setClose(dialog::dismiss);
                    dialog.show();
                }

                @Override
                public void onFailure(String s) {

                }
            });
        } else {
            Toast.makeText(mContext, "Фото не обнаружено", Toast.LENGTH_SHORT).show();
        }


    }

}
