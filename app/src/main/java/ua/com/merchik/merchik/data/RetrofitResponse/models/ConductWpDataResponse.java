package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ConductWpDataResponse {
    @SerializedName("state")
    @Expose
    public boolean state;   // Поведён, Не проведен

    @SerializedName("error")
    @Expose
    public String error;

    @SerializedName("document_complete")
    @Expose
    public boolean document_complete;

    @SerializedName("notice")
    @Expose
    public String notice;
}
