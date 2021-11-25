package ua.com.merchik.merchik.data.RetrofitResponse.photos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PhotoInfoResponse {
    @SerializedName("state")
    @Expose
    public boolean state;

    @SerializedName("list")
    @Expose
    public List<PhotoInfoResponseList> list = null;
}
