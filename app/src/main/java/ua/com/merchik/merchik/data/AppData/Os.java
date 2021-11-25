package ua.com.merchik.merchik.data.AppData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Os {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("api")
    @Expose
    private String api;

    public Os() {
    }

    public Os(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public Os(String name, String version, String api) {
        this.name = name;
        this.version = version;
        this.api = api;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
