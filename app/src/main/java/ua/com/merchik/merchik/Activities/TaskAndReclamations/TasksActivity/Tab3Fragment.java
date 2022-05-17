package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogActivity;
import ua.com.merchik.merchik.MakePhoto;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.TARCommentsDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.TARCommentsRealm;
import ua.com.merchik.merchik.dialogs.DialodTAR.DialogCreateTAR;
import ua.com.merchik.merchik.dialogs.DialogData;

public class Tab3Fragment extends Fragment {

    private Context mContext;
    private TasksAndReclamationsSDB tarData;

    private TextView textView;
    private RecyclerView recyclerView;
    private ConstraintLayout add;

    private DialogCreateTAR dialog;

    private Integer photoId;

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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_item_third, container, false);

        mContext = v.getContext();


        textView = v.findViewById(R.id.text_data);
        recyclerView = v.findViewById(R.id.RecyclerView);
        add = v.findViewById(R.id.constraintLayoutAdd);

        setFragmentData();  // Установка наполнения фрагмента

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
                dialog.addPhoto("Короткий клик - открывает Журнал фото для выбора фото\nДолгий клик - фотоаппарат для выполнения");
                dialog.addEditText("Добавьте комментарий");
                dialog.serCustomRecyclerView(new Clicks.click() {
                    @Override
                    public <T> void click(T data) {
                        // Подготовка данных для сохранения в БД
                        switch ((int) data) {
                            case 1:
                                intentOpen.putExtra("choise", true);
                                intentOpen.putExtra("resultCode", 101);
                                startActivityForResult(intentOpen, 101);
                                Toast.makeText(v.getContext(), "Короткий клик", Toast.LENGTH_SHORT).show();
                                break;

                            case 2:
                                try {
                                    TARSecondFrag.TaRID = tarData.id;
                                    MakePhoto makePhoto = new MakePhoto();
                                    makePhoto.openCamera(getActivity());
                                }catch (Exception e){

                                }

                                Toast.makeText(v.getContext(), "Долгий клик", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
                dialog.clickSave(() -> {
                    String res = dialog.comment;

                    if(res.length() < 20){
                        Toast.makeText(mContext, "Коментарий должен быть больше 20 символов", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }

                    // Сохранение коммента в БД
                    Toast.makeText(mContext, "Сохраняем в БД: " + res, Toast.LENGTH_SHORT).show();

                    TARCommentsDB row = new TARCommentsDB();
                    row.setComment(res);
                    row.setDt(String.valueOf(System.currentTimeMillis() / 1000));
                    row.setRId(String.valueOf(tarData.id));
                    try {
                        if (dialog.photo != null){
                            row.setPhoto(dialog.photo.getPhotoServerId()); // должно быть ID с сайта
                            row.photo_hash = dialog.photo.getPhoto_hash(); // Хэш фотографии
                        }
                    }catch (Exception e){
                        Toast.makeText(mContext, "Фото сохранить не удалось!", Toast.LENGTH_LONG).show();
                    }

                    row.setTp(String.valueOf(tarData.tp));
                    row.setWho(String.valueOf(tarData.vinovnik));

                    RealmManager.INSTANCE.executeTransaction((realm -> {
                        RealmManager.INSTANCE.copyToRealm(row);
                    }));

                    // Моментальная попытка выгрузить комментарий
                    Exchange exchange = new Exchange();
                    exchange.uploadTARComments(row);

                    if (adapter != null && dataComments != null) {
                        dataComments.add(0, row);
                        adapter.notifyItemInserted(0);
                        recyclerView.smoothScrollToPosition(0);
                    }

                    dialog.dismiss();
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

    private void setRecycler() {
        dataComments = RealmManager.INSTANCE.copyFromRealm(TARCommentsRealm.getTARCommentByTarId(String.valueOf(tarData.id)));

        Collections.reverse(dataComments);

        adapter = new TaRCommentsAdapter(mContext, dataComments);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
    }
}
