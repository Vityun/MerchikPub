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
        // 30.01.25 добавил новый тип используемый в ЭКЛ
        void onFailure(String error_type, String error);
//error_send_send_forbidden - Вы не можете отправить код самому себе
//error_recipient_details_unavailable - Не удалось получить информацию о получателе, пожалуйста, повторите отправку сообщения через 10 минут
//error_messenger_not_registered - Внимание! Получатель сообщения (ПТТ) ещё не подключён к нашему боту. Для того, чтобы он подключился, нажмите кнопку OK. На телефон ПТТ будет отправлена SMS с ссылкой, перейдя по которой он автоматичеки подключится к нашему боту и Вы сможете отправлять сообщения ему через мессенджер.
//error_tel_not_defined - Не удалось определить телефон получателя, пожалуйста свяжитесь с руководителем
//error_document_info_unavailable - Не удалось получить информацию о документе, пожалуйста, повторите отправку сообщения через 10 минут
//error_our_ptt_forbidden - Вы не можете подписывать ЭКЛ у ПТТ, который одновременно выполняет работы в интересах нашей компании
//error_send_failed_possible_duplicate - Не удалось отправить сообщение, возможно Вы уже отправляли подобное сообщение этому сотруднику в течении последних нескольких минут
//error_send_failed - Не удалось отправить сообщение. Попробуйте повторить отправку через 5 минут
    }

}
