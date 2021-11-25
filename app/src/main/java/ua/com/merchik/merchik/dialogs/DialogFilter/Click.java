package ua.com.merchik.merchik.dialogs.DialogFilter;

public interface Click {
    // Отработка успешного результата
    <T> void onSuccess(T data);

    // Отработка ошибки
    void onFailure(String error);
}
