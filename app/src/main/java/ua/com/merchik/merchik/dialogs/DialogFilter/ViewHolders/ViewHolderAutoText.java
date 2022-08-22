package ua.com.merchik.merchik.dialogs.DialogFilter.ViewHolders;

import android.content.Context;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;
import ua.com.merchik.merchik.dialogs.DialogFilter.data.DialogFilterRecyclerData;

public class ViewHolderAutoText extends RecyclerView.ViewHolder {

    private Context mContext;
    private TextView title;
    private AutoCompleteTextView autoText;

    public ViewHolderAutoText(@NonNull View itemView) {
        super(itemView);

        mContext = itemView.getContext();
        title = itemView.findViewById(R.id.title);
        autoText = itemView.findViewById(R.id.autoCompleteTextView);
    }

    public void bind(DialogFilterRecyclerData dialogFilterRecyclerData, Clicks.click click) {

        title.setText(dialogFilterRecyclerData.msg);

//        AutoTextAdapter adapter = new AutoTextAdapter(mContext, android.R.layout.simple_dropdown_item_1line, dialogFilterRecyclerData.dataList);
        AutoTextAdapter adapter = new AutoTextAdapter(mContext, R.layout.dropdown_item_text, dialogFilterRecyclerData.dataList);
        autoText.setAdapter(adapter);
        autoText.setOnClickListener(arg0 -> {
            autoText.showDropDown();
        });
        autoText.setOnItemClickListener((parent, arg1, position, arg3) -> {
            Object item = parent.getItemAtPosition(position);

            if (item instanceof AddressSDB) {
                AddressSDB result = (AddressSDB) item;
                autoText.setText(result.nm);
            }

            if (item instanceof UsersSDB) {
                UsersSDB result = (UsersSDB) item;
                autoText.setText(result.fio);
            }

            if (item instanceof CustomerSDB) {
                CustomerSDB result = (CustomerSDB) item;
                autoText.setText(result.nm);
            }

            if (item instanceof ThemeDB) {
                ThemeDB result = (ThemeDB) item;
                autoText.setText(result.getNm());
            }


            click.click(item);
        });

    }


}
