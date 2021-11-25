package ua.com.merchik.merchik.ServerExchange;

import android.graphics.Bitmap;

import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.LanguagesSDB;
import ua.com.merchik.merchik.data.Database.Room.TranslatesSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.Translation.LangListDB;

public interface ExchangeInterface {


    interface ExchangeRes {
        void onSuccess(String ok);

        void onFailure(String error);
    }

    interface ExchangePhoto {
        void onSuccess(Bitmap bitmap);

        void onFailure(String error);
    }


    /*Для работы с таблицей SQL Языки*/
    interface Languages {
        // Отработка успешного результата
        void onSuccess(List<LanguagesSDB> data);

        // Отработка ошибки
        void onFailure(String error);
    }

    /*Для работы с таблицей SQL Переводы*/
    interface Translates {
        // Отработка успешного результата
        void onSuccess(List<TranslatesSDB> data);

        // Отработка ошибки
        void onFailure(String error);
    }

    /*Выгрузка фотоотчётов.*/
    interface UploadPhotoReports {
        // Отработка успешного результата
        void onSuccess(StackPhotoDB photoDB, String s);

        // Отработка ошибки
        void onFailure(StackPhotoDB photoDB, String error);
    }

    /*Получение с сервера и обработка Языков*/
    interface LanguagesResponce {
        // Отработка успешного результата
        void onSuccess(List<LangListDB> data, String s);

        // Отработка ошибки
        void onFailure(List<LangListDB> data, String error);
    }

    interface ExchangeResponseInterface {
        // Отработка успешного результата
        <T> void onSuccess(List<T> data);

        // Отработка ошибки
        void onFailure(String error);
    }

    interface ExchangeResponseInterfaceSingle {
        // Отработка успешного результата
        <T> void onSuccess(T data);

        // Отработка ошибки
        void onFailure(String error);
    }

}
