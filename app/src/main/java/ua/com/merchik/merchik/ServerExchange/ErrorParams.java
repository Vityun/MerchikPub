package ua.com.merchik.merchik.ServerExchange;


import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;

public class ErrorParams {

    private final String title;
    private final DialogStatus icon;

    public ErrorParams(String title, DialogStatus icon) {
        this.title = title;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public DialogStatus getIcon() {
        return icon;
    }
}