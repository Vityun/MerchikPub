package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import android.util.Log;

import com.google.gson.Gson;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoTableRequest;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;

/**
 * 11.12.23. Тут я буду скачивать фото мерчандайзера
 * */
public class PhotoMerchikExchange {


    /**
     * 11.12.2023 Это я скопировал с Exchange. В Перспективе нужно ТАМ удалить
     * 03.03.2021 (ДОПОЛНИТЬ)
     * Получение со стороны сервера фотографий.
     * <p>
     * Задумка функции в том что Руководители смогут по полученным фоткам подчинённых ставить
     * оценки, комментарии, дви. Получаем ВСЕ фотки за сегодняшний день с сайта.
     * Какие именно и сколько фото - зависит от пользователя. Подчинённый должен получить только
     * свои фото, а руководитель - Все
     */
    public void getPhotoFromSite() {
        try {

            PhotoDownload server = new PhotoDownload();
            PhotoTableRequest data = new PhotoTableRequest();
            data.mod = "images_view";
            data.act = "list_image";
            data.nolimit = "1";
            data.date_from = Clock.today_7;
            data.date_to = Clock.tomorrow7;

            Globals.writeToMLOG("INFO", "PhotoMerchikExchange/getPhotoFromSite", "PhotoTableRequest data: " + new Gson().toJson(data));

            WpDataRealm.UserPostRes info = WpDataRealm.userPost(Globals.userId);
            switch (info) {
                case EMPTY:
                    Globals.writeToMLOG("INFO", "PhotoMerchikExchange/getPhotoFromSite", "EMPTY");
                    break;

                /**MERCHIK_1
                 * Саме тут завантажуються фото за минулі роботи*/
                case SUBORDINATE:
                    data.sotr_id = String.valueOf(Globals.userId);
                    Globals.writeToMLOG("INFO", "PhotoMerchikExchange/getPhotoFromSite", "SUBORDINATE: " + data.sotr_id);
                    server.getPhotoFromServer(data);
                    break;

                case MANAGER:
                    Log.e("getPhotoFromSite", "MANAGER");
                    Globals.writeToMLOG("INFO", "PhotoMerchikExchange/getPhotoFromSite", "MANAGER");
                    break;
            }
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "PhotoMerchikExchange/getPhotoFromSite", "Exception e: " + e);
        }
    }
}
