package ua.com.merchik.merchik.data.RetrofitResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PremiumResponse {
    @SerializedName("state")
    @Expose
    public Boolean state;

    @SerializedName("error")
    @Expose
    public String error;

    @SerializedName("basis")
    @Expose
    public String basis;

    @SerializedName("basis_list")
    @Expose
    public List<String> basisList;
}
