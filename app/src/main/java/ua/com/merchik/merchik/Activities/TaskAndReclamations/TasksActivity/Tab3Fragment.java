package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.NEED_UPDATE_UI_REQUEST;
import static ua.com.merchik.merchik.MakePhoto.MakePhoto.CAMERA_REQUEST_TAR_COMMENT_PHOTO;
import static ua.com.merchik.merchik.MakePhoto.MakePhotoFromGalery.MakePhotoFromGaleryTasksAndReclamationsSDB;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.compose.ui.platform.ComposeView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import java.util.Collections;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARActivity;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARViewModel;
import ua.com.merchik.merchik.FabYoutube;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.Utils.PhotoEvidencePolicy;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TARCommentsDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;
import ua.com.merchik.merchik.dataLayer.ContextUI;
import ua.com.merchik.merchik.dataLayer.ModeUI;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.TARCommentsRealm;
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm;
import ua.com.merchik.merchik.dialogs.DialodTAR.DialogCreateTAR;
import ua.com.merchik.merchik.dialogs.DialodTAR.TarPhotoSourceDialog;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.features.main.DBViewModels.JournalPhotoSDBViewModel;
import ua.com.merchik.merchik.features.main.DBViewModels.TarCommentsViewModel;
import ua.com.merchik.merchik.features.main.tar.TarCommentsContent;
import ua.com.merchik.merchik.toolbar_menus;

public class Tab3Fragment extends Fragment {

    private FabYoutube fabYoutube = new FabYoutube();
    private FloatingActionButton fabYouTube;
    private TextView badgeTextView;
    public static final Integer[]  Tab3Fragment_VIDEO_LESSONS = new Integer[]{3623};
    private static final int REQUEST_SELECT_TAR_PHOTO_TYPE = 7418;
    private boolean photoActionInProgress;

//    private Context mContext;
    private TasksAndReclamationsSDB tarData;
    private TARViewModel viewModel;

    private TarCommentsViewModel commentsViewModel;
    private String pendingPhotoCommentId;
    private static final String STATE_PHOTO_COMMENT_ID = "photo_comment_id";

    private DialogCreateTAR dialog;

    private Integer photoId;
    private static final String ARG_TAR_ID = "tar_id";

    public static int TARCommentIndex;  // 15.02.23. Для того что б ОБНОВЛЯТЬ комментарии (добавлять фото)


    public Tab3Fragment() {
    }

    @Deprecated
    public Tab3Fragment(TasksAndReclamationsSDB data) {
        this.tarData = data;
    }

    public static Tab3Fragment newInstance(TasksAndReclamationsSDB data) {
        Tab3Fragment fragment = new Tab3Fragment();
        fragment.tarData = data;

        Bundle args = new Bundle();
        if (data != null && data.id != null) {
            args.putInt(ARG_TAR_ID, data.id);
        }
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TARViewModel.class);
        tarData = resolveTarData();
        if (savedInstanceState != null) {
            pendingPhotoCommentId = savedInstanceState.getString(STATE_PHOTO_COMMENT_ID);
        }

        if (tarData != null) {
            viewModel.setTasksAndReclamations(tarData);
        }
    }


    public void setPhoto(Integer id) {
        photoId = id;
        if (dialog != null) {
            dialog.refreshAdaper(StackPhotoRealm.getById(id));
        }
    }

    public void setPhotoTARComment(Integer id, int tarCommentIndex) {
        StackPhotoDB photoDB = StackPhotoRealm.getById(id);
        TARCommentsDB storedComment = pendingPhotoCommentId == null ? null
                : TARCommentsRealm.getTARCommentById(pendingPhotoCommentId);
        if (photoDB == null || storedComment == null || tarData == null
                || !String.valueOf(tarData.id).equals(storedComment.getRId())) {
            Globals.writeToMLOG(
                    "ERROR",
                    "Tab3Fragment/setPhotoTARComment",
                    "Missing photo or comment. commentId=" + pendingPhotoCommentId
            );
            return;
        }

        if (!TARCommentPhoto.isUsablePhoto(photoDB)) {
            Globals.writeToMLOG("ERROR", "Tab3Fragment/setPhotoTARComment",
                    "Photo rejected, comment unchanged: photoId=" + id + ", hash=" + photoDB.getPhoto_hash());
            Toast.makeText(getContext(), "Фото недоступне або пошкоджене. Зробіть фотографію ще раз.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        photoId = id;
        TARCommentsDB tarCommentsDB = RealmManager.INSTANCE.copyFromRealm(storedComment);
        tarCommentsDB.photo = PhotoEvidencePolicy.hasServerPhotoId(photoDB.getPhotoServerId())
                ? photoDB.getPhotoServerId() : "0";
        tarCommentsDB.photo_hash = photoDB.getPhoto_hash();
        tarCommentsDB.dtUpdate = System.currentTimeMillis()/1000;
        tarCommentsDB.startUpdate = true;

        Globals.writeToMLOG("INFO", "setPhotoTARComment/ОбновилФотоУКомментария", "tarCommentsDB: " + new Gson().toJson(tarCommentsDB));

        TARCommentsRealm.saveOrUpdate(Collections.singletonList(tarCommentsDB));

        pendingPhotoCommentId = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_item_third, container, false);

        return v;
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        ComposeView content = v.findViewById(R.id.tarCommentsContent);
        int tarId = tarData != null && tarData.id != null ? tarData.id
                : getArguments() != null ? getArguments().getInt(ARG_TAR_ID, 0) : 0;
        commentsViewModel = new ViewModelProvider(requireActivity())
                .get("tar_comments_" + tarId, TarCommentsViewModel.class);
        commentsViewModel.setContext(requireActivity());
        commentsViewModel.setOnAddComment(this::addComment);
        commentsViewModel.setOnAddPhoto(commentId -> {
            if (!beginPhotoAction()) return;
            pendingPhotoCommentId = commentId;
            new MakePhoto().openCamera(requireActivity(), CAMERA_REQUEST_TAR_COMMENT_PHOTO);
        });
        TarCommentsContent.setContentTarComments(content, commentsViewModel);
        viewModel.getTasksAndReclamations().observe(getViewLifecycleOwner(), data -> {
            if (data != null && data.id != null && (tarId == 0 || data.id == tarId)) {
                tarData = data;
                configureComments();
                if (getLifecycle().getCurrentState().isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
                    commentsViewModel.startObserving();
                }
            }
        });
        configureComments();
        fabYouTube = v.findViewById(R.id.fab3);
        badgeTextView = v.findViewById(R.id.badge_text_view_tar);

        fabYoutube.setFabVideo(fabYouTube, Tab3Fragment_VIDEO_LESSONS, () -> fabYoutube.showYouTubeFab(fabYouTube, badgeTextView, Tab3Fragment_VIDEO_LESSONS));
        fabYoutube.showYouTubeFab(fabYouTube, badgeTextView, Tab3Fragment_VIDEO_LESSONS);


        if (TARActivity.TARType == 1) {
            toolbar_menus.textLesson = 1183;
            toolbar_menus.videoLesson = 3623;
            toolbar_menus.videoLessons = null;
            Log.e("SET_TAR_FAB", "Tab3Fragment 1");
        } else {
            toolbar_menus.textLesson = 1185;
            toolbar_menus.videoLesson = 3623;
            toolbar_menus.videoLessons = null;
            Log.e("SET_TAR_FAB", "Tab3Fragment 0");
        }
        toolbar_menus.setFab(v.getContext(), TARActivity.fab, ()->{}); // ГЛАВНАЯ

    }


    private void configureComments() {
        if (tarData != null && commentsViewModel != null) {
            commentsViewModel.configure(tarData.id, tarData.tp == null ? TARActivity.TARType : tarData.tp, tarData.comment);
        }
    }

    public static int TaRId;

    private void addComment() {
        if (!isAdded() || tarData == null) return;
        if (tarData.vinovnikScore != null && tarData.vinovnikScore > 0) {
            dialog = new DialogCreateTAR(requireContext());
            dialog.setTitle("Внесение комментария");
            dialog.addPhoto("Короткий клик - открывает фотоаппарат для выполнения фото\nДолгий клик - открывает Журнал фото для выбора.");
            dialog.setPhotoActions(false, true);
            dialog.addEditText("Добавьте комментарий");

            TarPhotoDataHolder.Companion.instance();
            dialog.serCustomRecyclerView(new Clicks.click() {
                @Override
                public <T> void click(T data) {
                    int action = (int) data;
                    if ((action == 1 || action == 2) && !beginPhotoAction()) {
                        return;
                    }
                    // Подготовка данных для сохранения в БД
                    switch (action) {
                        case 1:
                            openPhotoJournal();
                            break;
                        case 2:
                            try {
                                openPhotoTypePicker();
                            }catch (Exception e){
                                photoActionInProgress = false;
                                Globals.writeToMLOG("ERROR", "Tab3Fragment.setAddButton.case2", "Exception e: " + e);
                            }
                            break;
                    }
                }
            });
            dialog.clickSave(() -> {
                try {
                    Globals.writeToMLOG("INFO", "Tab3Fragment.dialog.clickSave", "start");
                    String res = dialog.comment;

                    boolean photoRequired = isPhotoRequiredForTheme();
                    if (photoRequired && !hasValidPhoto(dialog.photo)) {
                        DialogData dialogNoPhoto = new DialogData(requireContext());
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
                        DialogData dialogShortComment = new DialogData(requireContext());
                        dialogShortComment.setTitle("Ошибка комментария");
                        dialogShortComment.setText("Коментарий должен быть больше 20 символов");
                        dialogShortComment.setDialogIco();
                        dialogShortComment.setClose(dialogShortComment::dismiss);
                        dialogShortComment.show();

                        Globals.writeToMLOG("INFO", "Tab3Fragment.dialog.clickSave", "Коментарий должен быть больше 20 символов");
                        dialog.dismiss();
                        return;
                    }

                    // Сохранение коммента в БД
                    Toast.makeText(requireContext(), "Сохраняем в БД: " + res, Toast.LENGTH_SHORT).show();

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
                        Toast.makeText(requireContext(), "Фото сохранить не удалось!", Toast.LENGTH_LONG).show();
                        Globals.writeToMLOG("INFO", "Tab3Fragment.dialog.clickSave", "Фото сохранить не удалось!");
                    }

                    if (photoRequired && !hasValidCommentPhoto(row)) {
                        DialogData dialogNoPhoto = new DialogData(requireContext());
                        dialogNoPhoto.setTitle("Ошибка комментария");
                        dialogNoPhoto.setText("Для этой темы комментарий обязательно должен содержать фотографию. Добавьте фото перед сохранением комментария.");
                        dialogNoPhoto.setDialogIco();
                        dialogNoPhoto.setClose(dialogNoPhoto::dismiss);
                        dialogNoPhoto.show();

                        Globals.writeToMLOG("INFO", "Tab3Fragment.dialog.clickSave", "Required photo is missing in TAR comment row. TAR: " + this.tarData.id);
                        return;
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

                    Globals.writeToMLOG("INFO", "Tab3Fragment.dialog.clickSave", "dismiss");
                    dialog.dismiss();
                }catch (Exception e){
                    Globals.writeToMLOG("ERROR", "Tab3Fragment.dialog.clickSave", "Exception e: " + e);
                }

            }, 2);
            dialog.setClose(dialog::dismiss);
            dialog.show();
        } else {
            DialogData dialogData = new DialogData(requireContext());
            dialogData.setDialogIco();
            dialogData.setTitle("Ошибка");
            dialogData.setText("Прежде чем добавлять комментарий - поставьте 'Оценку объективности' данной задачи. Оценку объективности можно установить с закладки 'Главная'");
            dialogData.setClose(dialogData::dismiss);
            dialogData.show();
        }


    }

    private boolean isPhotoRequiredForTheme() {
        try {
            ThemeDB theme = ThemeRealm.getThemeById(String.valueOf(this.tarData.themeId));
            return theme != null && theme.need_photo == 1;
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "Tab3Fragment.isPhotoRequiredForTheme", "Exception e: " + e);
            return false;
        }
    }

    private void openPhotoTypePicker() {
        TarPhotoDataHolder holder = TarPhotoDataHolder.Companion.instance();
        holder.preparePhotoTypeSelection();

        JsonArray excludedTypeIds = new JsonArray();
        for (int id : new int[]{3, 25, 29, 32, 33, 34, 35, 37, 38, 46, 49, 52}) {
            excludedTypeIds.add(id);
        }
        JsonObject dataJson = new JsonObject();
        dataJson.add("excludedTypeIds", excludedTypeIds);

        Intent intent = new Intent(requireContext(), FeaturesActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("viewModel", ua.com.merchik.merchik.features.main.DBViewModels.ImagesTypeListDBViewModel.class.getCanonicalName());
        bundle.putString("modeUI", ModeUI.ONE_SELECT.toString());
        bundle.putString("title", "Типы фото");
        bundle.putString("subTitle", "Укажите тип фото которое Вы собираетесь сделать");
        bundle.putString("dataJson", new Gson().toJson(dataJson));
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_SELECT_TAR_PHOTO_TYPE);
    }

    private void openPhotoJournal() {
        try {
            // A journal attachment keeps its existing type; it is not a pending capture.
            ((TARActivity) requireActivity()).clearPendingPhotoTypeId();
            TarPhotoDataHolder.Companion.instance().init();
            JsonObject journalData = new JsonObject();
            journalData.addProperty("clientId", tarData.client);
            journalData.addProperty("addrId", tarData.addr.toString());

            Intent journalIntent = new Intent(requireContext(), FeaturesActivity.class);
            Bundle journalBundle = new Bundle();
            journalBundle.putString("viewModel", JournalPhotoSDBViewModel.class.getCanonicalName());
            journalBundle.putString("contextUI", ContextUI.ADD_STACK_PHOTO_TO_ZIR.toString());
            journalBundle.putString("modeUI", ModeUI.ONE_SELECT.toString());
            journalBundle.putString("dataJson", new Gson().toJson(journalData));
            journalBundle.putString("title", "Перелік фото звітів");
            journalBundle.putString("subTitle", "Справочник Фото");
            journalIntent.putExtras(journalBundle);
            ActivityCompat.startActivityForResult(requireActivity(), journalIntent, NEED_UPDATE_UI_REQUEST, null);
        } catch (Exception e) {
            photoActionInProgress = false;
            Globals.writeToMLOG("ERROR", "Tab3Fragment.openPhotoJournal", "Exception e: " + e);
        }
    }

    private boolean beginPhotoAction() {
        if (photoActionInProgress || ((TARActivity) requireActivity()).getPhotoFlow().isActive()) return false;
        photoActionInProgress = true;
        return true;
    }

    private void openPhotoSourcePicker(int photoTypeId) {
        TARActivity tarActivity = (TARActivity) requireActivity();
        tarActivity.setPendingPhotoTypeId(photoTypeId);
        TARSecondFrag.TaRID = tarData.id;
        MakePhotoFromGaleryTasksAndReclamationsSDB = tarData;

        TarPhotoSourceDialog.show(
                tarActivity,
                () -> tarActivity.getPhotoFlow().start(tarData.id, photoTypeId, false),
                () -> tarActivity.getPhotoFlow().start(tarData.id, photoTypeId, true),
                () -> {
                    tarActivity.clearPendingPhotoTypeId();
                    photoActionInProgress = false;
                },
                this::openPhotoJournal
        );
    }

    private boolean hasValidPhoto(StackPhotoDB photo) {
        return photo != null && (hasValidPhotoValue(photo.getPhotoServerId()) || hasValidPhotoValue(photo.getPhoto_hash()));
    }

    private boolean hasValidCommentPhoto(TARCommentsDB row) {
        return row != null && (hasValidPhotoValue(row.getPhoto()) || hasValidPhotoValue(row.photo_hash));
    }

    private boolean hasValidPhotoValue(String value) {
        if (value == null) return false;
        String trimmed = value.trim();
        return !trimmed.isEmpty() && !trimmed.equals("0") && !trimmed.equalsIgnoreCase("null");
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("Tab3Fragment", "onActivityResult");
        Log.e("Tab3Fragment", "requestCode: " + requestCode);
        Log.e("Tab3Fragment", "resultCode: " + resultCode);
        Log.e("Tab3Fragment", "Intent: " + data);
        if (requestCode == REQUEST_SELECT_TAR_PHOTO_TYPE) {
            TarPhotoDataHolder holder = TarPhotoDataHolder.Companion.instance();
            Integer photoTypeId = holder.consumeSelectedPhotoTypeId();
            if (resultCode != Activity.RESULT_OK) {
                photoActionInProgress = false;
                return;
            }
            if (photoTypeId == null) {
                photoActionInProgress = false;
                Toast.makeText(requireContext(), "Оберіть тип фото", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                openPhotoSourcePicker(photoTypeId);
            } catch (Exception e) {
                photoActionInProgress = false;
                ((TARActivity) requireActivity()).clearPendingPhotoTypeId();
                Globals.writeToMLOG("ERROR", "Tab3Fragment.onActivityResult.photoType", "Exception: " + e);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        photoActionInProgress = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (commentsViewModel != null) commentsViewModel.startObserving();
    }

    @Override
    public void onStop() {
        if (commentsViewModel != null) commentsViewModel.stopObserving();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_PHOTO_COMMENT_ID, pendingPhotoCommentId);
    }

    @Override
    public void onDestroyView() {
        if (commentsViewModel != null) {
            commentsViewModel.stopObserving();
            commentsViewModel.closeDialogs();
            commentsViewModel.setOnAddComment(null);
            commentsViewModel.setOnAddPhoto(null);
            commentsViewModel.setContext(null);
            commentsViewModel.setLauncher(null);
        }
        if (dialog != null) dialog.dismiss();
        dialog = null;
        fabYouTube = null;
        badgeTextView = null;
        super.onDestroyView();
    }

    private TasksAndReclamationsSDB resolveTarData() {
        if (tarData != null) {
            return tarData;
        }

        try {
            Bundle args = getArguments();
            if (args != null && args.containsKey(ARG_TAR_ID)) {
                int tarId = args.getInt(ARG_TAR_ID, 0);
                if (tarId > 0) {
                    TasksAndReclamationsSDB tar = SQL_DB.tarDao().getById(tarId);
                    if (tar != null) {
                        return tar;
                    }
                }
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "Tab3Fragment/resolveTarData", "Exception e: " + e);
        }

        return viewModel != null ? viewModel.getTasksAndReclamations().getValue() : null;
    }
}
