package ua.com.merchik.merchik.data.AppData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Device {

    @SerializedName("type")
    @Expose
    private String type;    //smartphone (если ты не можешь определить тип устройства, то пиши смарт всегда, если можешь, пиши то, что можешь)
    @SerializedName("brand")
    @Expose
    private String brand;   // твой manufacturer по идее
    @SerializedName("model")
    @Expose
    private String model;   // твой model
    @SerializedName("modem_serial")
    @Expose
    private String modemSerial;   // твой model

    public Device(String type, String brand, String model) {
        this.type = type;
        this.brand = brand;
        this.model = model;
    }

    public Device(String type, String brand, String model, String modemSerial) {
        this.type = type;
        this.brand = brand;
        this.model = model;
        this.modemSerial = modemSerial;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModemSerial() {
        return modemSerial;
    }

    public void setModemSerial(String modemSerial) {
        this.modemSerial = modemSerial;
    }
}
