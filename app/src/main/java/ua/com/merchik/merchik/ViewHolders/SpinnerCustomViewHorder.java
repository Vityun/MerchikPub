package ua.com.merchik.merchik.ViewHolders;

import android.content.Context;
import android.view.View;
import android.widget.Spinner;

import androidx.recyclerview.widget.RecyclerView;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.TestViewHolderData;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;

public class SpinnerCustomViewHorder extends RecyclerView.ViewHolder {

    private Context context;

    private Spinner spinner;

    public SpinnerCustomViewHorder(View itemView) {
        super(itemView);
        this.context = itemView.getContext();
        spinner = itemView.findViewById(R.id.spinner);
    }

    public void bind(TestViewHolderData data, Clicks.clickListener click) {
//        Log.e("SpinnerCustomViewHorder", "data: " + data.);
    }

    public void bindFilter(Globals.SourceAct source, Click click){
        switch (source){
            case WP_DATA:

                break;
        }
    }
}
