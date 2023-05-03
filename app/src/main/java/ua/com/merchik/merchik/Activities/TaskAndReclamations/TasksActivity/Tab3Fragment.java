package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import static ua.com.merchik.merchik.MakePhoto.MakePhoto.CAMERA_REQUEST_TAR_COMMENT_PHOTO;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogActivity;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARActivity;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TARCommentsDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.TARCommentsRealm;
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm;
import ua.com.merchik.merchik.dialogs.DialodTAR.DialogCreateTAR;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.toolbar_menus;

public class Tab3Fragment extends Fragment {

    private Context mContext;
    private TasksAndReclamationsSDB tarData;

    private TextView textView;
    private RecyclerView recyclerView;
    private ConstraintLayout add;

    private DialogCreateTAR dialog;

    private Integer photoId;

    public static int TARCommentIndex;  // 15.02.23. Для того что б ОБНОВЛЯТЬ комментарии (добавлять фото)

    public Tab3Fragment() {
    }

    public Tab3Fragment(TasksAndReclamationsSDB data) {
        this.tarData = data;
    }

    public void setPhoto(Integer id) {
        photoId = id;
        if (dialog != null) {
            dialog.refreshAdaper(StackPhotoRealm.getById(id));
        }
    }

    public void setPhotoTARComment(Integer id, int tarCommentIndex) {
        photoId = id;
        StackPhotoDB photoDB = RealmManager.INSTANCE.copyFromRealm(StackPhotoRealm.getById(id));

        TARCommentsDB tarCommentsDB = dataComments.get(tarCommentIndex);
        tarCommentsDB.photo_hash = photoDB.getPhoto_hash();
        tarCommentsDB.dtUpdate = System.currentTimeMillis()/1000;
        tarCommentsDB.startUpdate = true;
        dataComments.set(tarCommentIndex, tarCommentsDB);

        Globals.writeToMLOG("INFO", "setPhotoTARComment/ОбновилФотоУКомментария", "tarCommentsDB: " + new Gson().toJson(tarCommentsDB));

        TARCommentsRealm.saveOrUpdate(Collections.singletonList(tarCommentsDB));

        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_item_third, container, false);

        mContext = v.getContext();


        textView = v.findViewById(R.id.text_data);
        recyclerView = v.findViewById(R.id.RecyclerView);
        add = v.findViewById(R.id.constraintLayoutAdd);

        setFragmentData();  // Установка наполнения фрагмента


        if (TARActivity.TARType == 1) {
            toolbar_menus.textLesson = 1183;
            toolbar_menus.videoLesson = 3623;
        } else {
            toolbar_menus.textLesson = 1185;
            toolbar_menus.videoLesson = 3623;
        }
        toolbar_menus.setFab(v.getContext(), TARActivity.fab); // ГЛАВНАЯ

        return v;
    }


    /**
     * 23.03.2021
     * Установка наполнения фрагмента.
     * <p>
     * Заполнение начального текста и установка переписки в ресайклер
     */
    private void setFragmentData() {
        setTextData();
        setAddButton();
        setRecycler();
    }


    /**
     * 23.03.2021
     * Устанавливает "заголовок" для фрагмента.
     * <p>
     * В этом заголовке должна находиться последнее сообщение(комментарий) из таблички Задач и
     * Рекламаций. Из-за того, что текст может быть большим и занимать много места - это текстовое
     * поле должно или раскрываться вниз (переписать в дальнейшем с помощью либы) или открывать в
     * диалоговом(модальном) окошке подробно весь текст комментария.
     */
    private void setTextData() {
        /* Получение строки с комментарием с строки ЗИР-а. Возможно его надо будет позже доработать.*/
        String comment = tarData.comment;

        textView.setText(Html.fromHtml("<b>Текст рекламации: </b>" + comment));  // Установка текста

        textView.setOnClickListener(v -> {
            DialogData dialog = new DialogData(mContext);
            dialog.setTitle("");
            dialog.setText("Текст рекламации: " + comment);
            dialog.setClose(dialog::dismiss);
            dialog.show();
        });
    }


    /**
     * 23.03.2021
     * Добавление кнопочки "Добавить новый комментарий"
     * <p>
     * В будущем на эту штуку много планов, но на данный момент она просто заглушка. Пока по планам
     * нет чего-то однозначного.
     */
    private final WorkPlan workPlan = new WorkPlan();
    public static int TaRId;

    private void setAddButton() {
        add.setOnClickListener(v -> {
            Intent intentOpen = new Intent(v.getContext(), PhotoLogActivity.class);
            if (tarData.vinovnikScore != null && tarData.vinovnikScore > 0) {
                dialog = new DialogCreateTAR(v.getContext());
                dialog.setTitle("Внесение комментария");
                dialog.addPhoto("Короткий клик - открывает фотоаппарат для выполнения фото\nДолгий клик - открывает Журнал фото для выбора.");
                dialog.addEditText("Добавьте комментарий");
                dialog.serCustomRecyclerView(new Clicks.click() {
                    @Override
                    public <T> void click(T data) {
                        // Подготовка данных для сохранения в БД
                        switch ((int) data) {
                            case 1:
                                Globals.writeToMLOG("INFO", "Tab3Fragment.setAddButton.case1", "start");
                                intentOpen.putExtra("choise", true);
                                intentOpen.putExtra("resultCode", 101);
                                startActivityForResult(intentOpen, 101);
//                                Toast.makeText(v.getContext(), "Короткий клик", Toast.LENGTH_SHORT).show();
                                break;

                            case 2:
                                try {
                                    Globals.writeToMLOG("INFO", "Tab3Fragment.setAddButton.case2", "start");
                                    TARSecondFrag.TaRID = tarData.id;
                                    MakePhoto makePhoto = new MakePhoto();
                                    makePhoto.openCamera(getActivity(), MakePhoto.CAMERA_REQUEST_TAKE_PHOTO);
                                }catch (Exception e){
                                    Globals.writeToMLOG("ERROR", "Tab3Fragment.setAddButton.case2", "Exception e: " + e);
                                }

//                                Toast.makeText(v.getContext(), "Долгий клик", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
                dialog.clickSave(() -> {
                    try {
                        Globals.writeToMLOG("INFO", "Tab3Fragment.dialog.clickSave", "start");
                        String res = dialog.comment;

                        ThemeDB theme = ThemeRealm.getThemeById(String.valueOf(this.tarData.themeId));
                        if(theme.need_photo == 1 && dialog.photo == null){
                            DialogData dialogNoPhoto = new DialogData(mContext);
                            dialogNoPhoto.setTitle("Ошибка комментария");
                            dialogNoPhoto.setText("По данной темы у коментария должна быть фотография, выполните фото прежде чем сохранить коментарий.");
                            dialogNoPhoto.setDialogIco();
                            dialogNoPhoto.setClose(dialogNoPhoto::dismiss);
                            dialogNoPhoto.show();

                            Globals.writeToMLOG("INFO", "Tab3Fragment.dialog.clickSave", "У комментария должна быть фотография. Задача: " + this.tarData.id);
                            dialog.dismiss();
                            return;
                        }

                        if(res.length() < 20){
                            DialogData dialogShortComment = new DialogData(mContext);
                            dialogShortComment.setTitle("Ошибка комментария");
                            dialogShortComment.setText("Коментарий должен быть больше 20 символов");
                            dialogShortComment.setDialogIco();
                            dialogShortComment.setClose(dialog::dismiss);
                            dialogShortComment.show();

                            Globals.writeToMLOG("INFO", "Tab3Fragment.dialog.clickSave", "Коментарий должен быть больше 20 символов");
                            dialog.dismiss();
                            return;
                        }

                        // Сохранение коммента в БД
                        Toast.makeText(mContext, "Сохраняем в БД: " + res, Toast.LENGTH_SHORT).show();

                        TARCommentsDB row = new TARCommentsDB();
                        row.setID(String.valueOf(System.currentTimeMillis()));
                        row.setComment(res);
                        row.setDt(String.valueOf(System.currentTimeMillis() / 1000));
                        row.setRId(String.valueOf(tarData.id));
                        row.startUpdate = true;
                        try {
                            if (dialog.photo != null){
                                row.setPhoto(dialog.photo.getPhotoServerId()); // должно быть ID с сайта
                                row.photo_hash = dialog.photo.getPhoto_hash(); // Хэш фотографии
                            }
                        }catch (Exception e){
                            Toast.makeText(mContext, "Фото сохранить не удалось!", Toast.LENGTH_LONG).show();
                            Globals.writeToMLOG("INFO", "Tab3Fragment.dialog.clickSave", "Фото сохранить не удалось!");
                        }

                        row.setTp(String.valueOf(tarData.tp));
                        row.setWho(String.valueOf(tarData.vinovnik));



                        tarData.lastAnswer = res;
                        tarData.lastAnswerUserId = Globals.userId;
                        SQL_DB.tarDao().insertData(Collections.singletonList(tarData))
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


                        RealmManager.INSTANCE.executeTransaction((realm -> {
                            RealmManager.INSTANCE.copyToRealm(row);

                            Globals.writeToMLOG("INFO", "Tab3Fragment.dialog.clickSave", "save DB");
                            String stackJson = new Gson().toJson(row);
                            Globals.writeToMLOG("INFO", "Tab3Fragment.dialog.clickSave", "row: " + stackJson);
                        }));

                        // Моментальная попытка выгрузить комментарий
//                    Exchange exchange = new Exchange();
//                    exchange.uploadTARComments(row);

                        if (adapter != null && dataComments != null) {
                            dataComments.add(0, row);
                            adapter.notifyItemInserted(0);
                            recyclerView.smoothScrollToPosition(0);
                        }

                        Globals.writeToMLOG("INFO", "Tab3Fragment.dialog.clickSave", "dismiss");
                        dialog.dismiss();
                    }catch (Exception e){
                        Globals.writeToMLOG("ERROR", "Tab3Fragment.dialog.clickSave", "Exception e: " + e);
                    }

                }, 2);
                dialog.setClose(dialog::dismiss);
                dialog.show();
            } else {
                DialogData dialogData = new DialogData(v.getContext());
                dialogData.setDialogIco();
                dialogData.setTitle("Ошибка");
                dialogData.setText("Прежде чем добавлять комментарий - поставьте 'Оценку объективности' данной задачи. Оценку объективности можно установить с закладки 'Главная'");
                dialogData.setClose(dialogData::dismiss);
                dialogData.show();
            }


            // Создать Диалог для вноса данных
/*            DialogData dialog = new DialogData(mContext);

            dialog.setTitle("Комментарий");
            dialog.setText("");

            dialog.setOperation(DialogData.Operations.Text, "", null, () -> {
                String res = dialog.getOperationResult();
                Toast.makeText(mContext, "Сохраняем в БД: " + res, Toast.LENGTH_LONG).show();

                TARCommentsDB row = new TARCommentsDB();
                row.setComment(res);
                row.setDt(String.valueOf(System.currentTimeMillis()/1000));
                row.setRId(String.valueOf(data.id));
                row.setTp(String.valueOf(data.tp));
                row.setWho(String.valueOf(data.vinovnik));

                RealmManager.INSTANCE.executeTransaction((realm -> {
                    RealmManager.INSTANCE.copyToRealm(row);
                }));

                if (adapter != null && dataComments != null){
                    dataComments.add(0, row);
                    adapter.notifyItemInserted(0);
                    recyclerView.smoothScrollToPosition(0);
                }

            });
            dialog.setEditTextHint("Внесите комментарий по задаче");

            dialog.setClose(dialog::dismiss);
            dialog.show();*/
        });
    }


    private TaRCommentsAdapter adapter;
    private List<TARCommentsDB> dataComments;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("Tab3Fragment", "onActivityResult");
        Log.e("Tab3Fragment", "requestCode: " + requestCode);
        Log.e("Tab3Fragment", "resultCode: " + resultCode);
        Log.e("Tab3Fragment", "Intent: " + data);
    }

    private void setRecycler() {
        dataComments = RealmManager.INSTANCE.copyFromRealm(TARCommentsRealm.getTARCommentByTarId(String.valueOf(tarData.id)));

        Collections.reverse(dataComments);

        adapter = new TaRCommentsAdapter(mContext, dataComments, (int index) -> {
            TARCommentIndex = index;

            new MakePhoto().openCamera(getActivity(), CAMERA_REQUEST_TAR_COMMENT_PHOTO);
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
    }
}
