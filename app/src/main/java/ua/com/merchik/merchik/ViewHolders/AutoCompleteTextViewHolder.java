package ua.com.merchik.merchik.ViewHolders;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Optional;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.RealmModels.AddressDB;
import ua.com.merchik.merchik.data.RealmModels.CustomerDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;
import ua.com.merchik.merchik.data.TEST_DATA;
import ua.com.merchik.merchik.data.TestViewHolderData;

public class AutoCompleteTextViewHolder extends RecyclerView.ViewHolder {

    private Context context;
    private ConstraintLayout layout;
    private AutoCompleteTextView autoCompleteTextView;
    private TextView textView;

    public AutoCompleteTextViewHolder(@NonNull View itemView) {
        super(itemView);
        context = itemView.getContext();

        layout = itemView.findViewById(R.id.layout);
        autoCompleteTextView = itemView.findViewById(R.id.autoCompleteTextView);
        textView = itemView.findViewById(R.id.textView);
    }

    public void bind(TestViewHolderData data, Clicks.clickListener click) {
        textView.setText(data.title);
        autoCompleteTextView.setHint(data.msg);

        switch (data.type) {
            case ADDRESS:

                List<AddressDB> addressDBList = data.addressList;

                AutoTextAddressAdapter adapter = new AutoTextAddressAdapter(context, android.R.layout.simple_dropdown_item_1line, addressDBList);

                Log.e("TEST_AUTO_HOLDER", "data.addressList.size(): " + data.addressList.size());

                if (addressDBList != null && addressDBList.size() == 1) {
//                    autoCompleteTextView.setHint(data.addressList.get(0).getNm());
                    autoCompleteTextView.setText(addressDBList.get(0).getNm());
                    AddressDB res = addressDBList.get(0);
                    TEST_DATA test = new TEST_DATA();
                    test.address = res;
                    click.click(test);

                }


                autoCompleteTextView.setAdapter(adapter);
                autoCompleteTextView.setOnClickListener(arg0 -> {
                    Log.e("TEST_AUTO_HOLDER", "Click dropdown");
                    autoCompleteTextView.showDropDown();
                });
                autoCompleteTextView.setOnItemClickListener((parent, arg1, position, arg3) -> {
                    Object item = parent.getItemAtPosition(position);
                    if (item instanceof AddressDB) {
                        AddressDB res = (AddressDB) item;
                        TEST_DATA test = new TEST_DATA();
                        test.address = res;
                        click.click(test);
                    }
                });
                break;

            case USERS:

                List<UsersSDB> usersSDBList = data.dataList;

                AutoTextUsersViewHolder adapterUser = new AutoTextUsersViewHolder(context, android.R.layout.simple_dropdown_item_1line, null, usersSDBList);

                Log.e("TEST_AUTO_HOLDER", "data.addressList.size(): " + data.addressList.size());

                if (usersSDBList != null && usersSDBList.size() == 1) {
//                    autoCompleteTextView.setHint(data.addressList.get(0).getNm());
                    autoCompleteTextView.setText(usersSDBList.get(0).fio);
                    UsersSDB res = usersSDBList.get(0);
                    TEST_DATA test = new TEST_DATA();
                    test.users = res;
                    click.click(test);

                }


                autoCompleteTextView.setAdapter(adapterUser);
                autoCompleteTextView.setOnClickListener(arg0 -> {
                    Log.e("TEST_AUTO_HOLDER", "Click dropdown");
                    autoCompleteTextView.showDropDown();
                });
                autoCompleteTextView.setOnItemClickListener((parent, arg1, position, arg3) -> {
                    Object item = parent.getItemAtPosition(position);
                    if (item instanceof UsersSDB) {
                        UsersSDB res = (UsersSDB) item;
                        TEST_DATA test = new TEST_DATA();
                        test.users = res;
                        click.click(test);
                    }
                });
                break;

            case CUSTOMER:

                List<CustomerDB> customerDBS = data.customerList;

                AutoTextCustomerAdapter adapter2 = new AutoTextCustomerAdapter(context, android.R.layout.simple_dropdown_item_1line, customerDBS);

                Log.e("TEST_AUTO_HOLDER", "data.customerList.size(): " + data.customerList.size());
                if (customerDBS != null && customerDBS.size() == 1) {
                    autoCompleteTextView.setText(customerDBS.get(0).getNm());
                    CustomerDB res = customerDBS.get(0);
                    TEST_DATA test = new TEST_DATA();
                    test.customer = res;
                    click.click(test);

                }

                autoCompleteTextView.setAdapter(adapter2);
                autoCompleteTextView.setOnClickListener(arg0 -> {
                    Log.e("TEST_AUTO_HOLDER", "Click dropdown 2");
                    autoCompleteTextView.showDropDown();
                });
                autoCompleteTextView.setOnItemClickListener((parent, arg1, position, arg3) -> {
                    Object item = parent.getItemAtPosition(position);
                    if (item instanceof CustomerDB) {
                        CustomerDB res = (CustomerDB) item;
                        TEST_DATA test = new TEST_DATA();
                        test.customer = res;
                        click.click(test);
                    }
                });
                break;

            case THEME:
                List<ThemeDB> themeDBS = data.themeList;

                AutoTextThemeAdapter adapter3 = new AutoTextThemeAdapter(context, android.R.layout.simple_dropdown_item_1line, themeDBS);

                Log.e("TEST_AUTO_HOLDER", "data.themeList.size(): " + themeDBS.size());
                if (themeDBS != null && themeDBS.size() == 1) {
                    autoCompleteTextView.setText(themeDBS.get(0).getNm());
                    ThemeDB res = themeDBS.get(0);
                    TEST_DATA test = new TEST_DATA();
                    test.theme = res;
                    click.click(test);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Optional<ThemeDB> theme = themeDBS.stream().filter(db -> db.getID().equals("1232")).findFirst();
                    ThemeDB themeDB = theme.get();
                    Log.e("TEST_AUTO_HOLDER", "themeDB: " + themeDB);
                    autoCompleteTextView.setText(themeDB.getNm());
                    ThemeDB res = themeDB;
                    TEST_DATA test = new TEST_DATA();
                    test.theme = res;
                    click.click(test);
                }

                autoCompleteTextView.setAdapter(adapter3);
                autoCompleteTextView.setOnClickListener(arg0 -> autoCompleteTextView.showDropDown());
                autoCompleteTextView.setOnItemClickListener((parent, arg1, position, arg3) -> {
                    Object item = parent.getItemAtPosition(position);
                    if (item instanceof ThemeDB) {
                        ThemeDB res = (ThemeDB) item;
                        TEST_DATA test = new TEST_DATA();
                        test.theme = res;
                        click.click(test);
                    }
                });
                break;

            case OPINION:
                List<OpinionSDB> opinionSDBS = data.opinionList;

                AutoTextOpinionAdapter adapter4 = new AutoTextOpinionAdapter(context, android.R.layout.simple_dropdown_item_1line, opinionSDBS);

                Log.e("TEST_AUTO_HOLDER", "data.themeList.size(): " + data.opinionList.size());
                if (opinionSDBS != null && opinionSDBS.size() == 1) {
                    autoCompleteTextView.setText(opinionSDBS.get(0).nm);

                    OpinionSDB res = opinionSDBS.get(0);
                    TEST_DATA test = new TEST_DATA();
                    test.opinion = res;
                    click.click(test);

                }

                autoCompleteTextView.setAdapter(adapter4);

                autoCompleteTextView.setOnClickListener(arg0 -> autoCompleteTextView.showDropDown());
                autoCompleteTextView.setOnItemClickListener((parent, arg1, position, arg3) -> {
                    Object item = parent.getItemAtPosition(position);
                    if (item instanceof OpinionSDB) {
                        OpinionSDB res = (OpinionSDB) item;
                        TEST_DATA test = new TEST_DATA();
                        test.opinion = res;
                        click.click(test);
                    }
                });
                break;
        }

        autoCompleteTextView.setThreshold(1);
    }


}
