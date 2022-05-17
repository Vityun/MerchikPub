package ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Mail {
    @SerializedName("unread")
    @Expose
    public long unread;
}
