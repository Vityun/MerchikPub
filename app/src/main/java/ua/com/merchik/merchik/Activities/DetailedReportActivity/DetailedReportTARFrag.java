package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.checkVideos;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogActivity;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARActivity;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.TARSecondFrag;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.UniversalAdapter;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.ViewListSDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AddressRealm;
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm;
import ua.com.merchik.merchik.database.realm.tables.UsersRealm;
import ua.com.merchik.merchik.dialogs.DialodTAR.DialogCreateTAR;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogVideo;

public class DetailedReportTARFrag extends Fragment {

    private static Integer[] DETAILED_REPORT_FRAGMENT_TAR_VIDEO_LESSONS = new Integer[]{4208, 3527};

    private Context mContext;
    private WpDataDB wpDataDB;
    private List<TasksAndReclamationsSDB> tasksAndReclamationsSDBList;

    private FragmentManager fragmentManager;
    private TARSecondFrag secondFrag;

    private FloatingActionButton fab;


    public DetailedReportTARFrag(Context mContext, WpDataDB wpDataDB) {
        this.mContext = mContext;
        this.wpDataDB = wpDataDB;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_dr_tar, container, false);

        fragmentManager = getParentFragmentManager();
//        tasksAndReclamationsSDBList = SQL_DB.tarDao().getAllByInfo(1, wpDataDB.getClient_id(), wpDataDB.getAddr_id(), (System.currentTimeMillis() / 1000 - 5184000));
        tasksAndReclamationsSDBList = SQL_DB.tarDao().getAllByInfo(0, wpDataDB.getAddr_id());

        try {
            FloatingActionButton fabAdd = v.findViewById(R.id.fabAdd);
            fab = v.findViewById(R.id.fab);
            RecyclerView recycler = v.findViewById(R.id.recycler);

            try {
                // Установка Ресайклера
                UniversalAdapter recyclerViewReclamations = new UniversalAdapter(mContext, tasksAndReclamationsSDBList, false, new Globals.TARInterface() {
                    @Override
                    public void onSuccess(TasksAndReclamationsSDB data) {
                        Intent intent = new Intent(v.getContext(), TARActivity.class);
                        intent.putExtra("TAR_ID", data.id);
                        v.getContext().startActivity(intent);
                    }

                    @Override
                    public void onFailure(String error) {
                        DialogData dialog = new DialogData(v.getContext());
                        dialog.setTitle("Раздел находится в разработке");
                        dialog.setText("Отображение Задачи/Рекламации в данном контексте ещё не реализовано. \n\nПопробуйте открыть этот ЗиР из соответствующего раздела в Меню.");
                        dialog.setClose(dialog::dismiss);
                        dialog.show();
                    }
                });

                recycler.setAdapter(recyclerViewReclamations);
                recycler.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "DetailedReportTARFrag/onCreateView/UniversalAdapter", "Exception e: " + e);
            }

            // Установка Fab
            fabAdd.setOnClickListener(v1 -> {
                Intent intent = new Intent(v.getContext(), PhotoLogActivity.class);

                DialogCreateTAR dialog = new DialogCreateTAR(v.getContext());
                dialog.setClose(dialog::dismiss);
                dialog.users = UsersRealm.getUsersDBById(wpDataDB.getUser_id());
                dialog.address = AddressRealm.getAddressById(wpDataDB.getAddr_id());
                dialog.customer = CustomerRealm.getCustomerById(wpDataDB.getClient_id());
                dialog.setTarType(1);
                dialog.setRecyclerView(new Clicks.click() {
                    @Override
                    public <T> void click(T data) {

                        switch ((Integer) data){
                            case 1:
                                intent.putExtra("choise", true);
                                intent.putExtra("resultCode", 100);
                                if (dialog.address != null) {
                                    intent.putExtra("address", dialog.address.getAddrId());
                                }

                                if (dialog.customer != null) {
                                    intent.putExtra("customer", dialog.customer.getId());
                                } else {
                                    intent.putExtra("customer", "");
                                }

                                startActivityForResult(intent, 100);
                                break;

                            case 2:
                                try {
                                    MakePhoto makePhoto = new MakePhoto();
                                    makePhoto.openCamera(getActivity(), 202);
                                }catch (Exception e){
                                    Globals.writeToMLOG("ERROR", "Tab3Fragment.setAddButton.case2", "Exception e: " + e);
                                }
                                break;

                            default:
                                return;
                        }


                    }
                });
                dialog.clickSave(() -> {
                }, 1);
                dialog.show();
            });

            setFabVideo(v.getContext(), ()->{
                List<ViewListSDB> videos = checkVideos(DETAILED_REPORT_FRAGMENT_TAR_VIDEO_LESSONS, ()->{});

//                if (videos != null && videos.size() != 0) {
//                    DetailedReportActivity.imageView.setVisibility(View.GONE);
//                } else {
//                    DetailedReportActivity.imageView.setVisibility(View.VISIBLE);
//                    Snackbar.make(DetailedReportActivity.imageView.getRootView(), "Вы просмотрели ещё не все ролики", Snackbar.LENGTH_LONG).show();
//                }
            });

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "DetailedReportTARFrag/onCreateView", "Exception e: " + e);
        }

        return v;
    }


    /**
     * 21.06.23.
     * Добавление иконочки для прямого просмотра видео-уроков.
     *
     * @param context*/
    private void setFabVideo(Context context, Clicks.clickVoid click){
        fab.setOnClickListener(view -> {
            DialogVideo dialogVideo = new DialogVideo(context);
            dialogVideo.setTitle("Перелік відео уроків");
            dialogVideo.setVideos(getSiteHints(DETAILED_REPORT_FRAGMENT_TAR_VIDEO_LESSONS), click);
            dialogVideo.setClose(dialogVideo::dismiss);
            dialogVideo.show();
        });
    }

    private List<SiteHintsDB> getSiteHints(Integer[] integers){
        List<SiteObjectsDB> siteObjects = RealmManager.getLesson(integers);
        List<SiteHintsDB> data = null;
        try {
            if (siteObjects != null) {
                Integer[] siteObjectIds = new Integer[siteObjects.size()];
                for (int i = 0; i < siteObjects.size(); i++) {
                    int lessId = Integer.parseInt(siteObjects.get(i).getLessonId());
                    if (lessId != 0) siteObjectIds[i] = lessId;
                }
                data = RealmManager.getVideoLesson(siteObjectIds);
                if (data != null) Collections.reverse(data);
            }
        } catch (Exception e) {
            Log.e("setVideoLesson", "Exception e: " + e);
        }

        return data;
    }

}
