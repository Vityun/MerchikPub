package ua.com.merchik.merchik.data.UploadPhotoData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ImagesPrepareUploadPhoto {
    @SerializedName("state")
    @Expose
    public boolean state;
    @SerializedName("error")
    @Expose
    public String error;
    @SerializedName("list")
    @Expose
    public List<DataList> list = null;


    public class DataList {
        @SerializedName("state")
        @Expose
        public boolean state;
        @SerializedName("error")
        @Expose
        public String error;
        @SerializedName("error_type")
        @Expose
        public String errorType;
        @SerializedName("nm")
        @Expose
        public String nm;
    }
}


