package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

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

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogActivity;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARFragmentHome;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoTableRequest;
import ua.com.merchik.merchik.database.room.DaoInterfaces.TarDao;
import ua.com.merchik.merchik.dialogs.DialodTAR.DialogCreateTAR;
import ua.com.merchik.merchik.dialogs.DialogFilter.DialogFilter;

public class TARHomeFrag extends Fragment implements TARFragmentHome.OnFragmentInteractionListener {

    private OnFragmentInteractionListener mListener;

    private EditText editText;
    private ImageButton filterImg;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;

    private Date dateFrom;
    private Date dateTo;
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

        Long testDate = Clock.getDateLong(-30).getTime() / 1000;

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
        int tarId = getActivity().getIntent().getIntExtra("TAR_ID", 0);
        if (tarId != 0) {
            data = Collections.singletonList(SQL_DB.tarDao().getById(tarId));
        } else {
            long time = Clock.getDateLong(-30).getTime() / 1000;
            data = SQL_DB.tarDao().getAllByTp(Globals.userId, type, time);
        }


        setFab();
        getPhoto(data);
        setRecycler(v.getContext());
        setFilter();

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
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PhotoLogActivity.class);

            dialog = new DialogCreateTAR(v.getContext());
            dialog.setClose(dialog::dismiss);
            dialog.setTarType(type);
            dialog.setRecyclerView(new Clicks.click() {
                @Override
                public <T> void click(T data) {

                    switch ((Integer) data) {
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
                            } catch (Exception e) {
                                Globals.writeToMLOG("ERROR", "Tab3Fragment.setAddButton.case2", "Exception e: " + e);
                            }
                            break;

                        default:
                            return;
                    }
                }
            });
            dialog.clickSave(() -> {

                data = SQL_DB.tarDao().getAllByTp(Globals.userId, type, Clock.getDateLong(-30).getTime() / 1000);

                recyclerViewReclamations.updateData(data);
                recyclerViewReclamations.notifyDataSetChanged();

                dialog.dismiss();
            }, 1);
            dialog.show();
        });
    }


    private void setRecycler(Context context) {
        // FILTER
        filterImg.setOnClickListener(v -> {
            setFilter();
        });

        recyclerViewReclamations = new UniversalAdapter(context, data, true, onClickListener);
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

        for (TasksAndReclamationsSDB item : data) {
            if (item.photo != null && !item.photo.equals("") && !item.photo.equals("0")) {
                ids += item.photo + ", ";
            }
        }
        request.id_list = ids;

        photoDownloader.getPhotoInfoAndSaveItToDB(request, new Clicks.clickObjectAndStatus<StackPhotoDB>() {
            @Override
            public void onSuccess(StackPhotoDB data) {

            }

            @Override
            public void onFailure(String error) {

            }
        });
    }


    private void setFilter() {
        try {
            DialogFilter dialog = new DialogFilter(getContext(), Globals.SourceAct.WP_DATA);

            try {
                // Данные для фильтра даты
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(System.currentTimeMillis());
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                dateFrom = cal.getTime();
                dateTo = Clock.timeLongToDAte(Clock.getDatePeriodLong(cal.getTime().getTime(), +7) / 1000);

                dialog.setDates(dateFrom, dateTo);
                dialog.setDefaultTARType(1);    // 0 - Активные +1 ибо первый элеиент - Все
                dialog.setRecyclerTAR();

                dialog.setTextFilter(editText.getText().toString());
                dialog.setClose(dialog::dismiss);

                dialog.setCancel(() -> {
                    editText.setText("");
                    setFilterIco(dialog);
                    recyclerViewReclamations.updateData(data);
                    recyclerView.scheduleLayoutAnimation();
                    recyclerViewReclamations.notifyDataSetChanged();
                });
                dialog.setApply(() -> {
                    applyFilter(dialog);
                });
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "WPDataFragmentHome/setFilter/createDialog", "Exception e: " + e + "\n\n" + Arrays.toString(e.getStackTrace()));
            }

            try {
                setFilterIco(dialog);
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "WPDataFragmentHome/setFilter/setFilterIco", "Exception e: " + e + "\n\n" + Arrays.toString(e.getStackTrace()));
            }
            try {
                filterImg.setOnClickListener((v) -> {
                    dialog.show();
                });
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "WPDataFragmentHome/setFilter/setOnClickListener", "Exception e: " + e + "\n\n" + Arrays.toString(e.getStackTrace()));
            }


            applyFilter(dialog);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "WPDataFragmentHome/setFilter", "Exception e: " + e + "\n\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    private void applyFilter(DialogFilter dialog) {
        List<TasksAndReclamationsSDB> filteredData = new ArrayList<>();

        TarDao dao = SQL_DB.tarDao();

        List<TasksAndReclamationsSDB> tasksAndReclamationsSDBS = data; // Получение всех данных

        if (dialog.clientId != null) {
            filteredData.addAll(dao.getByClientIdFilter(dialog.clientId));
        } else {
            filteredData.addAll(tasksAndReclamationsSDBS);
        }

        if (dialog.addressId != null) {
            List<TasksAndReclamationsSDB> addressFiltered = dao.getByAddressIdFilter(dialog.addressId);
            filteredData.retainAll(addressFiltered);
        }

        if (dialog.tarType != null) {
            List<TasksAndReclamationsSDB> tarTypeFiltered = dao.getByTarTypeFilter(dialog.tarType);
            filteredData.retainAll(tarTypeFiltered);
        }

//        if (dialog.dateFrom != null && dialog.dateTo != null) {
//            Date dt1 = Clock.stringDateConvertToDate(dialog.dateFrom);
//            Date dt2 = Clock.stringDateConvertToDate(dialog.dateTo);
//
//            if (dt1 != null && dt2 != null) {
//                List<TasksAndReclamationsSDB> dateFiltered = dao.getByDateRangeFilter(dt1.getTime() / 1000, dt2.getTime() / 1000);
//                filteredData.retainAll(dateFiltered);
//            }
//        }

        recyclerViewReclamations.updateData(filteredData);


        if (dialog.textFilter != null && !dialog.textFilter.equals("")) {
            editText.setText(dialog.textFilter);
        }
        recyclerViewReclamations.notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
        setFilterIco(dialog);
    }

    private void setFilterIco(DialogFilter dialog) {
        if (dialog.isFiltered()) {
            filterImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_filterbold));
        } else {
            filterImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_filter));
        }
    }


}
