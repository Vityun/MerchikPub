package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ua.com.merchik.merchik.data.QuestionAnswerDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;

public class QuestionAnswerResponse {

    @SerializedName("state")
    @Expose
    private Boolean state;
    @SerializedName("error")
    @Expose
    private String error;
    @SerializedName("list")
    @Expose
    private List<QuestionAnswerDB> list = null;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<QuestionAnswerDB> getList() {
        return list;
    }

    public void setList(List<QuestionAnswerDB> list) {
        this.list = list;
    }

}