package ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Messenger {
    @SerializedName("connected")
    @Expose
    public List<Long> connected = null;
}
