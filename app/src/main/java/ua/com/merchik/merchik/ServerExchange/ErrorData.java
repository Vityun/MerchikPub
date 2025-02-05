package ua.com.merchik.merchik.ServerExchange;

import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;

public class ErrorData {
    private final String errorType;
    private final String errorMessage;
    private final ErrorParams errorParams;

    // Конструктор для обычной ошибки
    public ErrorData(String errorMessage) {
        this.errorType = null;
        this.errorMessage = errorMessage;
        this.errorParams = null;
    }

    // Конструктор для ошибки с типом
    public ErrorData(String errorType, String errorMessage) {
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.errorParams = null;
    }

    // Конструктор для ошибки с параметрами (включает errorType и errorMessage)
    public ErrorData(String errorType, String errorMessage, ErrorParams errorParams) {
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.errorParams = errorParams;
    }

    public boolean hasType() {
        return errorType != null;
    }

    public boolean hasParams() {
        return errorParams != null;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ErrorParams getErrorParams() {
        return errorParams;
    }
}


