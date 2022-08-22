package ua.com.merchik.merchik.data.RetrofitResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AdditionalMaterialsLinksResponse {
    @SerializedName("state")
    @Expose
    public boolean state;

    @SerializedName("url")
    @Expose
    public String url;
}
