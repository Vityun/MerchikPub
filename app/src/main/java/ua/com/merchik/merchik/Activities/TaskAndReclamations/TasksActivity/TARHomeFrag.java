package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogActivity;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARFragmentHome;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoTableRequest;
import ua.com.merchik.merchik.dialogs.DialodTAR.DialogCreateTAR;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;
import ua.com.merchik.merchik.dialogs.DialogFilter.DialogFilter;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

public class TARHomeFrag extends Fragment  implements TARFragmentHome.OnFragmentInteractionListener {

    private OnFragmentInteractionListener mListener;

    private EditText editText;
    private ImageButton filterImg;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;

    private UniversalAdapter recyclerViewReclamations;
    public DialogCreateTAR dialog;

    //    private List<TasksAndReclamationsDB> data = new ArrayList<>();
    private List<TasksAndReclamationsSDB> data = new ArrayList<>();
    private static Globals.TARInterface onClickListener;


    private int type;

    // Скорее всего не нужно
    public TARHomeFrag() {
//        super(R.layout.fragment_tar_base);
        Log.e("TARHomeFrag_L", "TARHomeFrag()");
    }


    // Мне это скорее всего не нужно
    public static TARHomeFrag newInstance(int type, Globals.TARInterface listener) {
        Log.e("TARHomeFrag_L", "newInstance");
        TARHomeFrag tarHomeFrag = new TARHomeFrag();
        Bundle args = new Bundle();
        args.putInt("type", type);
        tarHomeFrag.setArguments(args);

        Log.e("TARHomeFrag_L", "newInstance: " + args.getInt("type"));
        onClickListener = listener;

        return tarHomeFrag;
    }


    //-----------------------------------------


    //-----------------------------------------


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_tar_base, container, false);

        Log.e("TARHomeFrag_L", "Я должен ыл создаться");

        Long testDate = Clock.getDateLong(-30).getTime()/1000;

        editText = v.findViewById(R.id.searchViewReclamations);
        filterImg = v.findViewById(R.id.filter);
        recyclerView = v.findViewById(R.id.recyclerViewReclamations);
        fabAdd = v.findViewById(R.id.floatingActionButtonAdd);


        type = getArguments().getInt("type");
        Log.e("TARHomeFrag_L", "type: " + type);

//        if (type == 0){
//            data = SQL_DB.tarDao().getAllByTp(1, Clock.getDateLong(-30).getTime()/1000);
//        }else if (type == 1){
//            data = SQL_DB.tarDao().getAllByTp(0, Clock.getDateLong(-30).getTime()/1000);
//        }

        // 1 = задача; 0 = рекламация
        data = SQL_DB.tarDao().getAllByTp(Globals.userId, type, Clock.getDateLong(-30).getTime()/1000);


        setFab();
        getPhoto(data);
        setRecycler(v.getContext());

        return v;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void messageFromParentFragment(String msg) {

    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void messageFromChildFragment(String msg);
    }

    private void setFab() {
        fabAdd.setOnClickListener(v->{
            Intent intent = new Intent(v.getContext(), PhotoLogActivity.class);

            dialog = new DialogCreateTAR(v.getContext());
            dialog.setClose(dialog::dismiss);
            dialog.setTarType(type);
            dialog.setRecyclerView(() -> {
                intent.putExtra("choise", true);

                if (dialog.address != null) {
                    intent.putExtra("address", dialog.address.getAddrId());
                }

                if (dialog.customer != null) {
                    intent.putExtra("customer", dialog.customer.getId());
                } else {
                    intent.putExtra("customer", "");
                }

                startActivityForResult(intent, 100);
            });
            dialog.clickSave(()->{
//                if (type == 0){
//                    data = SQL_DB.tarDao().getAllByTp(1, Clock.getDateLong(-30).getTime()/1000);
//                }else if (type == 1){
//                    data = SQL_DB.tarDao().getAllByTp(0, Clock.getDateLong(-30).getTime()/1000);
//                }

                data = SQL_DB.tarDao().getAllByTp(Globals.userId, type, Clock.getDateLong(-30).getTime()/1000);

                recyclerViewReclamations.updateData(data);
                recyclerViewReclamations.notifyDataSetChanged();
            }, 1);
            dialog.show();
        });
    }


    private void setRecycler(Context context) {
        // FILTER
        filterImg.setOnClickListener(v -> {
            DialogFilter dialog = new DialogFilter(context, Globals.SourceAct.TASK_AND_RECLAMATION);
            dialog.setClose(dialog::dismiss);

            dialog.serEditFilter(editText.getHint(), editText.getText());
            dialog.setTaRBlock();

            dialog.setApply(new Click() {
                @Override
                public <T> void onSuccess(T data) {
                    DialogFilter.ResultData resultData = (DialogFilter.ResultData) data;

                    if (resultData.dateFrom == 0){
                        resultData.dateFrom = null;
                    }

                    if (resultData.dateTo == 0){
                        resultData.dateTo = null;
                    }

                    if (resultData.editText != null && resultData.editText.length()>0){
                        editText.setText(resultData.editText);
                    }

                    List<TasksAndReclamationsSDB> tarData = new ArrayList<>();

                    if (type == 0){
                        tarData = SQL_DB.tarDao().getTaRBy(0, resultData.dateFrom, resultData.dateTo, resultData.themeId, resultData.statusId);
                    }else if (type == 1){
                        tarData = SQL_DB.tarDao().getTaRBy(1, resultData.dateFrom, resultData.dateTo, resultData.themeId, resultData.statusId);
                    }

                    recyclerViewReclamations.updateData(tarData);
                    recyclerViewReclamations.notifyDataSetChanged();

                    Toast.makeText(context, "Отобрано: " + tarData.size(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {

                }
            });

/*            dialog.clickApply(new Clicks.click() {
                @Override
                public <T> void click(T data) {
                    DialogFilterResult dialogResult = (DialogFilterResult) data;
                }
            });*/

            dialog.show();
        });

        recyclerViewReclamations = new UniversalAdapter(context, data, onClickListener);
        recyclerView.setAdapter(recyclerViewReclamations);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        editText.setHint("Введите текст для поиска по любым реквизитам");
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                recyclerViewReclamations.getFilter().filter(s);
            }
        });
    }


    private void getPhoto(List<TasksAndReclamationsSDB> data) {
        PhotoDownload photoDownloader = new PhotoDownload();

        PhotoTableRequest request = new PhotoTableRequest();
        request.mod = "images_view";
        request.act = "list_image";
        request.nolimit = "1";

        String ids = "";
//        for (TasksAndReclamationsDB item : data){
//            if (item.getPhoto() != null && !item.getPhoto().equals("") && !item.getPhoto().equals("0")){
//                ids += item.getPhoto() + ", ";
//            }
//        }

        for (TasksAndReclamationsSDB item : data) {
            if (item.photo != null && !item.photo.equals("") && !item.photo.equals("0")) {
                ids += item.photo + ", ";
            }
        }
        request.id_list = ids;

        photoDownloader.getPhotoInfoAndSaveItToDB(request);
    }


}
