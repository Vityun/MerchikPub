package ua.com.merchik.merchik.data.AppData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppData {

    @SerializedName("os")
    @Expose
    private Os os;
    @SerializedName("browser")
    @Expose
    private Browser browser;
    @SerializedName("device")
    @Expose
    private Device device;

    public AppData() {
    }

    public AppData(Os os, Browser browser) {
        this.os = os;
        this.browser = browser;
    }

    public AppData(Os os, Browser browser, Device device) {
        this.os = os;
        this.browser = browser;
        this.device = device;
    }

    public Os getOs() {
        return os;
    }

    public void setOs(Os os) {
        this.os = os;
    }

    public Browser getBrowser() {
        return browser;
    }

    public void setBrowser(Browser browser) {
        this.browser = browser;
    }
}



/*os тут думаю понятно

device
type: smartphone (если ты не можешь определить тип устройства, то пиши смарт всегда, если можешь, пиши то, что можешь)
brand: твой manufacturer по идее
model: твой model

browser
name: MerchikApp ну или как ты его назовёшь это строка, только без версии
type: mobile_app
VersionApp: номер твоей версии
date: YYYY-MM-DD
type: 0 - релиз, 1 - тест, 2 - альфа (пример привел случайный, там уже смотрите как с Петровым согласуешь)*/