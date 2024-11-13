package ua.com.merchik.merchik.ViewHolders;

import java.util.List;

import ua.com.merchik.merchik.data.TEST_DATA;

public interface Clicks<T> {

    /**
     * 15.12.23.
     * MassageMode - существует для того что б описывать поведение Прогресса (на момент написания только для загрузки фото Товаров)
     * */
    enum MassageMode{
        SHOW,                               // показываем сообщение/обновляем статус
        CLOSE,                              // Закрываем сообщение/подводим итог
        UNEXPECTED_SITUATION_OCCURRED       // Не придвиденная ситуация
    }

    void click();

    interface clickVoid{
        void click();
    }

    public interface CodeInputListener {
        void onCodeEntered(boolean success);
    }

    interface click{
        <T> void click(T data);
    }

    interface OnUpdateUI{
        void update();
    }

    interface clickText{
        void click(String data);
    }

    interface clickObject<T>{
        void click(T data);
    }

    interface clickList{
        <T> void click(List<T> data);
    }

    interface clickListener{
        void click(TEST_DATA data);
    }

    interface clickStatusMsg{
        // Отработка успешного результата
         void onSuccess(String data);

        // Отработка ошибки
        void onFailure(String error);
    }

    interface clickStatusMsgMode{
        // Отработка успешного результата
        void onSuccess(String data, MassageMode mode);

        // Отработка ошибки
        void onFailure(String error);
    }

    interface clickObjectAndStatus<T>{
        // Отработка успешного результата
        void onSuccess(T data);

        // Отработка ошибки
        void onFailure(String error);
    }

}
