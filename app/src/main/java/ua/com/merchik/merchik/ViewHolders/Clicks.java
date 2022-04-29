package ua.com.merchik.merchik.ViewHolders;

import java.util.List;

import ua.com.merchik.merchik.data.TEST_DATA;

public interface Clicks {

    void click();

    interface clickVoid{
        void click();
    }

    interface click{
        <T> void click(T data);
    }

    interface clickText{
        void click(String data);
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

}
