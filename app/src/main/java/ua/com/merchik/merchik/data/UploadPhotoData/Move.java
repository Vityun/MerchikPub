package ua.com.merchik.merchik.data.UploadPhotoData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Move {
    @SerializedName("res")
    @Expose
    private String res;
    @SerializedName("nm")
    @Expose
    private String nm;
    @SerializedName("hash")
    @Expose
    private String hash;
    @SerializedName("geo_location")
    @Expose
    private GeoLocation geoLocation;

    public String getRes() {
        return res;
    }

    public void setRes(String res) {
        this.res = res;
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }
}
