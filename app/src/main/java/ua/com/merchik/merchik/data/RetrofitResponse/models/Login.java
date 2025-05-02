package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.SerializedName;

public class Login {

    @SerializedName("state")
    private Boolean state;
    @SerializedName("error")
    private String error;
    @SerializedName("register_company")
    public Boolean registerCompany;

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
}
