package ua.com.merchik.merchik.dialogs.DialogsRecyclerViewAdapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.RetrofitResponse.EDRPOUResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class AutoCompleteTextViewHolder extends RecyclerView.ViewHolder {

    private Context context;
    private ConstraintLayout layout;
    private TextView textView;
    private AutoCompleteTextView autoCompleteTextView;

    public AutoCompleteTextViewHolder(@NonNull View itemView) {
        super(itemView);
        context = itemView.getContext();
        layout = itemView.findViewById(R.id.layout);
        textView = itemView.findViewById(R.id.textView);
        autoCompleteTextView = itemView.findViewById(R.id.autoCompleteTextView);
    }

    public void bind(ViewHolderTypeList.AutoTextLayoutData autoTextBlock) {
        if (autoTextBlock.dataTextTitle != null){
            textView.setVisibility(View.VISIBLE);
            textView.setText(autoTextBlock.dataTextTitle);
        }else {
            textView.setVisibility(View.GONE);
        }

        autoCompleteTextView.setHint(autoTextBlock.dataTextAutoTextHint);
        findCompany(autoTextBlock);
        selectCompany(autoTextBlock);
    }

    private void selectCompany(ViewHolderTypeList.AutoTextLayoutData autoTextBlock){
        autoCompleteTextView.setOnItemClickListener((adapterView, view, i, l) -> {
            EDRPOUResponse selected = (EDRPOUResponse) adapterView.getAdapter().getItem(i);
            autoTextBlock.click.onSuccess(selected);
        });
    }


    private void findCompany(ViewHolderTypeList.AutoTextLayoutData autoTextBlock){
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                autoTextBlock.result = charSequence.toString();
                String text = autoCompleteTextView.getText().toString().toLowerCase(Locale.getDefault());

                StandartData data = new StandartData();
                data.mod = "auth";
                data.act = "company_search";
                data.term = text;

                Gson gson = new Gson();
                String json = gson.toJson(data);
                JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

                Call<List<EDRPOUResponse>> call = RetrofitBuilder.getRetrofitInterface().GET_EDRPOU(RetrofitBuilder.contentType, convertedObject);
                call.enqueue(new retrofit2.Callback<List<EDRPOUResponse>>() {
                    @Override
                    public void onResponse(Call<List<EDRPOUResponse>> call, Response<List<EDRPOUResponse>> response) {

                        Log.e("autoCompleteTextView", "response: " + response);

                        if (response.isSuccessful()){
                            if (response.body() != null){
                                if (response.body().size() > 0){
                                    testAutoTextAdapter adapter = new testAutoTextAdapter(context, android.R.layout.simple_list_item_1, response.body());
                                    autoCompleteTextView.setAdapter(adapter);
                                }
                            }else {
                                autoTextBlock.click.onFailure(context.getString(R.string.internet_connection_error_body_is_null));
                            }
                        }else {
                            autoTextBlock.click.onFailure(context.getString(R.string.internet_connection_error_code_not_200));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<EDRPOUResponse>> call, Throwable t) {
                        Log.e("getUserLogin", "onFailure: " + t.toString());
                    }
                });

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}
