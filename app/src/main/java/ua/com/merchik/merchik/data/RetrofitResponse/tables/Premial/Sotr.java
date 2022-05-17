package ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Sotr {
    @SerializedName("total")
    @Expose
    public long total;
    @SerializedName("candidate")
    @Expose
    public long candidate;
}
