package ua.com.merchik.merchik.dialogs.DialogFilter;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter.DialogAdapter;
import ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter.ViewHolderTypeList;

public class DialogFilter {

    private final Dialog dialog;

    // filtered data
    public String textFilter = null;
    public String clientId = null;
    public Integer addressId = null;
    public String dateFrom = null;
    public String dateTo = null;
    public Integer tarType = null;
    public Integer tarTypeDefault = null;

    private Date dateFromF = null;
    private Date dateToF = null;
    //----------------------------------

    private final TextView title;
    private final EditText editText;
    private final Button apply;
    private final Button cancel;
    private final RecyclerView recycler;
    private final ImageButton close;
    private ImageButton help;
    private ImageButton videoHelp;
    private ImageButton call;

    public DialogFilter(Context context, Globals.SourceAct source) {
        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_filter);

        title = dialog.findViewById(R.id.title);
        editText = dialog.findViewById(R.id.editText);
        apply = dialog.findViewById(R.id.apply);
        cancel = dialog.findViewById(R.id.cancel);
        recycler = dialog.findViewById(R.id.recycler);

        close = dialog.findViewById(R.id.imageButtonClose);

//        setRecycler();
    }

    public void show() {
        if (dialog != null) dialog.show();
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void setClose(DialogData.DialogClickListener clickListener) {
        close.setOnClickListener(v -> {
            clickListener.clicked();
        });
    }

    public boolean isFiltered() {
        return textFilter != null && !textFilter.equals("") || clientId != null || addressId != null || dateFrom != null || dateTo != null;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Кнопка "Закрыть/Отменить".
     * <p>
     * Должна сбрасывать все выше установленные фильтры и закрыть модальное окно фильтра
     */
    public void setCancel(DialogData.DialogClickListener click) {
        cancel.setOnClickListener(l -> {
            // todo Сбросить в ноль фильтры
            recycler.invalidate();
            setRecycler();

            editText.setText("");
            textFilter = null;
            clientId = null;
            addressId = null;
            dateFrom = null;
            dateTo = null;

            click.clicked();

//            dialog.dismiss();
        });
    }

    /**
     * Должны примениться все фильтры которые навыбирал пользователь
     */
    public void setApply(DialogData.DialogClickListener click) {
        apply.setOnClickListener(v -> {
            click.clicked();
            dialog.dismiss();
        });
    }

    public void setTextFilter(String textToFilter) {
        this.textFilter = textToFilter;
        editText.setText(textToFilter);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                textFilter = s.toString();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    //----------------------------------------------------------------------------------------------

    public void setRecycler() {
        recycler.setVisibility(View.VISIBLE);
        recycler.setAdapter(setAdapter());
        recycler.setLayoutManager(new LinearLayoutManager(dialog.getContext(), LinearLayoutManager.VERTICAL, false));
    }

    public void setRecyclerTAR() {
        recycler.setVisibility(View.VISIBLE);
        recycler.setAdapter(setAdapterTAR());
        recycler.setLayoutManager(new LinearLayoutManager(dialog.getContext(), LinearLayoutManager.VERTICAL, false));
    }

    public void setDates(Date dtF, Date dtTo) {
        this.dateFromF = dtF;
        this.dateToF = dtTo;

        SimpleDateFormat simpleDateFormatDateFrom = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.dateFrom = simpleDateFormatDateFrom.format(dtF);

        SimpleDateFormat simpleDateFormatDateTo = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.dateTo = simpleDateFormatDateTo.format(dtTo);
    }

    public void setDefaultTARType(int defaultTARType){
        tarTypeDefault = defaultTARType;
    }

    private DialogAdapter setAdapter() {
        List<ViewHolderTypeList> data = new ArrayList<>();
        data.add(createChoiceDateBlockRV(dateFromF, dateToF)); // Блок с выбором дат.
        data.add(createChoiceCustomerBlockRV()); // Блок с выбором Клиента.
        data.add(createChoiceAddressBlockRV()); // Блок с выбором Адреса.
        return new DialogAdapter(data);
    }

    private DialogAdapter setAdapterTAR() {
        List<ViewHolderTypeList> data = new ArrayList<>();
        data.add(createChoiceDateBlockRV(dateFromF, dateToF)); // Блок с выбором дат.
        data.add(createChoiceCustomerBlockRV()); // Блок с выбором Клиента.
        data.add(createChoiceAddressBlockRV()); // Блок с выбором Адреса.
        data.add(createChoiceTarTypeBlockRV()); // Блок с выбором Типа статуса ЗИР.
        return new DialogAdapter(data);
    }

    private ViewHolderTypeList createChoiceDateBlockRV(Date dateFromF, Date dateToF) {
        ViewHolderTypeList res = new ViewHolderTypeList();

        ViewHolderTypeList.ChoiceDateLayoutData block = new ViewHolderTypeList.ChoiceDateLayoutData();
        block.dataTextTitle = "Дата с: ";
        block.dataTextTitle2 = "Дата по: ";
        if (dateFromF != null) {
            block.dateFrom = dateFromF;
            block.state = true;
        } else {
            block.dateFrom = Calendar.getInstance().getTime();
            block.state = false;
        }

        if (dateToF != null) {
            block.dateTo = dateToF;
            block.state = true;
        } else {
            block.dateTo = Calendar.getInstance().getTime();
            block.state = false;
        }


        SimpleDateFormat simpleDateFormatDateFrom = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFrom = simpleDateFormatDateFrom.format(block.dateFrom);

        SimpleDateFormat simpleDateFormatDateTo = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateTo = simpleDateFormatDateTo.format(block.dateTo);

        block.click = new ViewHolderTypeList.ClickData() {
            @Override
            public <T> void onSuccess(T data) {
                Toast.makeText(dialog.getContext(), "" + data, Toast.LENGTH_SHORT).show();

                dateFrom = block.resultDateFrom;
                dateTo = block.resultDateTo;
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(dialog.getContext(), "" + error, Toast.LENGTH_SHORT).show();
            }
        };

        res.type = 6;
        res.choiceDateLayoutData = block;

        return res;
    }

    private ViewHolderTypeList createChoiceCustomerBlockRV() {
        ViewHolderTypeList res = new ViewHolderTypeList();

        ViewHolderTypeList.ChoiceSpinnerLayoutData block = new ViewHolderTypeList.ChoiceSpinnerLayoutData();
        block.dataTextTitle = "Клиент: ";

        List<CustomerSDB> customerSDB = SQL_DB.customerDao().getAllSortedByNm();

        List<String> dataSpinnerList = new ArrayList<>();
        dataSpinnerList.add("Все");
        for (CustomerSDB item : customerSDB) {
            dataSpinnerList.add(item.nm);
        }

        block.dataSpinner = null;
        block.dataSpinnerList = dataSpinnerList;
        block.click = new ViewHolderTypeList.ClickData() {
            @Override
            public <T> void onSuccess(T data) {
                Toast.makeText(dialog.getContext(), "Выбран клиент: " + data, Toast.LENGTH_SHORT).show();

                if ((int) block.resultData == 0) {
                    clientId = null;
                } else {
                    clientId = customerSDB.get((int) block.resultData - 1).id;  // -1 потому что выше мы добавляли в Список "Все"
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(dialog.getContext(), error, Toast.LENGTH_SHORT).show();
                clientId = null;
            }
        };

        res.type = 5;
        res.choiceSpinnerLayoutData = block;

        return res;
    }

    private ViewHolderTypeList createChoiceAddressBlockRV() {
        ViewHolderTypeList res = new ViewHolderTypeList();

        ViewHolderTypeList.ChoiceSpinnerLayoutData block = new ViewHolderTypeList.ChoiceSpinnerLayoutData();
        block.dataTextTitle = "Адрес: ";

        List<AddressSDB> addressSDB = SQL_DB.addressDao().getAllSortedByNm();

        List<String> dataSpinnerList = new ArrayList<>();
        dataSpinnerList.add("Все");
        for (AddressSDB item : addressSDB) {
            dataSpinnerList.add(item.nm);
        }

        block.dataSpinner = null;
        block.dataSpinnerList = dataSpinnerList;
        block.click = new ViewHolderTypeList.ClickData() {
            @Override
            public <T> void onSuccess(T data) {
                Toast.makeText(dialog.getContext(), "Выбран адрес: " + data, Toast.LENGTH_SHORT).show();

                if ((int) block.resultData == 0) {
                    addressId = null;
                } else {
                    addressId = addressSDB.get((int) block.resultData - 1).id;  // -1 потому что выше мы добавляли в Список "Все"
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(dialog.getContext(), error, Toast.LENGTH_SHORT).show();
                addressId = null;
            }
        };

        res.type = 5;
        res.choiceSpinnerLayoutData = block;

        return res;
    }

    private ViewHolderTypeList createChoiceTarTypeBlockRV() {
        ViewHolderTypeList res = new ViewHolderTypeList();

        ViewHolderTypeList.ChoiceSpinnerLayoutData block = new ViewHolderTypeList.ChoiceSpinnerLayoutData();
        block.dataTextTitle = "Тип задачі: ";

        List<Integer> tarTypeTxt = new ArrayList<>();
        tarTypeTxt.add(0);    //0 - Активные
        tarTypeTxt.add(1);    //1 - Выполненные
        tarTypeTxt.add(2);    //2 - Не выполненные
        tarTypeTxt.add(3);    //3 - Отмененные

        List<String> dataSpinnerList = new ArrayList<>();
        dataSpinnerList.add("Всі");
        dataSpinnerList.add("Активні"); //0
        dataSpinnerList.add("Виконані");//1
        dataSpinnerList.add("Не виконані");//2
        dataSpinnerList.add("Відмінені");//3

        // Установка значения по умолчанию
        if (tarTypeDefault != null){
            block.defaultPosition = tarTypeDefault;
        }

        block.dataSpinner = null;
        block.dataSpinnerList = dataSpinnerList;
        block.click = new ViewHolderTypeList.ClickData() {
            @Override
            public <T> void onSuccess(T data) {
                Toast.makeText(dialog.getContext(), "Обрані задачі: " + data, Toast.LENGTH_SHORT).show();

                if ((int) block.resultData == 0) {
                    tarType = null;
                } else {
                    tarType = tarTypeTxt.get((int) block.resultData - 1);  // -1 потому что выше мы добавляли в Список "Все"
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(dialog.getContext(), error, Toast.LENGTH_SHORT).show();
                tarType = null;
            }
        };

        res.type = 5;
        res.choiceSpinnerLayoutData = block;

        return res;
    }


    //----------------------------------------------------------------------------------------------


}
