package ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ViewHolders.TextViewHolder;

public class DialogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static int LAYOUT_TEXT = 0;
    private static int LAYOUT_EDIT_TEXT = 1;
    private static int LAYOUT_BUTTON = 2;
    private static int LAYOUT_AUTO_TEXT_VIEW = 3;
    private static int LAYOUT_ADD_PHOTO = 4;
    private static int LAYOUT_SPINNER = 5;
    private static int LAYOUT_CHOICE_DATE = 6;
    private static int LAYOUT_TABLE_PREMIUM = 7;

    private List<ViewHolderTypeList> data;


    public DialogAdapter(List<ViewHolderTypeList> data) {
        this.data = data;
    }

    @Override
    public int getItemViewType(int position) {
        switch (data.get(position).type) {
            case 0:
                return LAYOUT_TEXT;
            case 1:
                return LAYOUT_EDIT_TEXT;
            case 2:
                return LAYOUT_BUTTON;
            case 3:
                return LAYOUT_AUTO_TEXT_VIEW;
            case 4:
                return LAYOUT_ADD_PHOTO;
            case 5:
                return LAYOUT_SPINNER;
            case 6:
                return LAYOUT_CHOICE_DATE;
            case 7:
                return LAYOUT_TABLE_PREMIUM;
            default:
                return -1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 3:
                return new ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter.AutoCompleteTextViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_auto_complete_text, parent, false));
            case 1:
                return new EditTextViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_edittext, parent, false));
            case 2:
                return new ButtonDialogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_button, parent, false));
            case 0:
                return new TextViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_text, parent, false));
            case 4:
                return new AddPhotoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_photo_and_info, parent, false));
            case 5:
                return new SpinnerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_spinner, parent, false));
            case 6:
                return new DatePickerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_date_picker, parent, false));
            case 7:
                return new TablePremiumViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vh_table_premium, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:
                TextViewHolder textViewHolder = (TextViewHolder) holder;
                textViewHolder.bind(data.get(position).textBlock);
                break;

            case 1:
                EditTextViewHolder editTextViewHolder = (EditTextViewHolder) holder;
                editTextViewHolder.bind(data.get(position).editTextBlock);
                break;

            case 2:
                ButtonDialogViewHolder buttonDialogViewHolder = (ButtonDialogViewHolder) holder;
                buttonDialogViewHolder.bind(data.get(position).buttonBlock);
                break;

            case 3:
                ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter.AutoCompleteTextViewHolder autoCompleteTextViewHolder = (ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter.AutoCompleteTextViewHolder) holder;
                autoCompleteTextViewHolder.bind(data.get(position).autoTextBlock);
                break;

            case 4:
                AddPhotoViewHolder addPhotoViewHolder = (AddPhotoViewHolder) holder;
                addPhotoViewHolder.bind(data.get(position).addPhotoLayoutData);
                break;

            case 5:
                SpinnerViewHolder spinnerViewHolder = (SpinnerViewHolder) holder;
                spinnerViewHolder.bind(data.get(position).choiceSpinnerLayoutData);
                break;

            case 6:
                DatePickerViewHolder datePickerViewHolder = (DatePickerViewHolder) holder;
                datePickerViewHolder.bind(data.get(position).choiceDateLayoutData);
                break;

            case 7:
                TablePremiumViewHolder tablePremiumViewHolder = (TablePremiumViewHolder) holder;
                tablePremiumViewHolder.bind(data.get(position).tablePremiumLayoutData);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
