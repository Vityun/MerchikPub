package ua.com.merchik.merchik.data.AppData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Browser {

    @SerializedName("name")
    @Expose
    private String name;    // MerchikApp
    @SerializedName("version")
    @Expose
    private String version; // Номер версии APP
    @SerializedName("type")
    @Expose
    private String type;    // mobile_app
    @SerializedName("date")
    @Expose
    private String date;    // YYYY-MM-DD
    @SerializedName("test")
    @Expose
    private String test;    // 0 /1 Признак тестовости


    public Browser() {
    }

    public Browser(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public Browser(String name, String version, String type, String date, String test) {
        this.name = name;
        this.version = version;
        this.type = type;
        this.date = date;
        this.test = test;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }
}
