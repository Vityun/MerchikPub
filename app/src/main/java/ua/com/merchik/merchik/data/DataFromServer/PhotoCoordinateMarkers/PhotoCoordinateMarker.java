package ua.com.merchik.merchik.data.DataFromServer.PhotoCoordinateMarkers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PhotoCoordinateMarker {
    @SerializedName("state")
    @Expose
    public boolean state;

    @SerializedName("list")
    @Expose
    public List<PhotoCoordinateMarkerList> list = null;

    @SerializedName("error")
    @Expose
    public String error;
}
