package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UniversalResponse<T> {
    @SerializedName("state")
    @Expose
    public boolean state;

    @SerializedName("list")
    @Expose
    public List<T> list = null;

    @SerializedName("state")
    @Expose
    public String error;

    @SerializedName("missing_field")
    @Expose
    public String missingField;
}
