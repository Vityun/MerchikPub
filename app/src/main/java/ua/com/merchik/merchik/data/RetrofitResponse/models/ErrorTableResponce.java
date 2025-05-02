package ua.com.merchik.merchik.data.RetrofitResponse.models;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.ErrorDB;

public class ErrorTableResponce {

    private Boolean state;
    private List<ErrorDB> list = null;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public List<ErrorDB> getList() {
        return list;
    }

    public void setList(List<ErrorDB> list) {
        this.list = list;
    }

}
