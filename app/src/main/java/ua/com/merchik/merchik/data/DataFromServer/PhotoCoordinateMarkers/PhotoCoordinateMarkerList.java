package ua.com.merchik.merchik.data.DataFromServer.PhotoCoordinateMarkers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PhotoCoordinateMarkerList {
    @SerializedName("ID")
    @Expose
    public String id;

    @SerializedName("img_id")
    @Expose
    public String imgId;

    @SerializedName("region_num")
    @Expose
    public String regionNum;

    @SerializedName("x1")
    @Expose
    public String x1;

    @SerializedName("y1")
    @Expose
    public String y1;

    @SerializedName("x2")
    @Expose
    public String x2;

    @SerializedName("y2")
    @Expose
    public String y2;

    @SerializedName("comment")
    @Expose
    public String comment;

    @SerializedName("author_id")
    @Expose
    public String authorId;

    @SerializedName("dt_update")
    @Expose
    public String dtUpdate;
}
