package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.SamplePhotoResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

/**MERCHIK_1
 * Якщо я правий - ДИВИТИСЯ ЦЕЙ КЛАСС
 * */
public class SamplePhotoExchange {

    public SynchronizationTimetableDB synchronizationTimetableDB;
    private final ExecutorService executorService;

    public SamplePhotoExchange(){
        this.executorService = Executors.newFixedThreadPool(8);
    }
    public SamplePhotoExchange(ExecutorService executor) {
        this.executorService = executor;
    }

    public void downloadSamplePhotoTable(Clicks.clickObjectAndStatus click) {
        ReferenceDataExchange.samplePhotos(click);
    }

    public void downloadSamplePhotos(List<SamplePhotoSDB> list, Clicks.clickStatusMsg click) {
        try {
            List<Integer> dataList = new ArrayList<>();
            for (SamplePhotoSDB item : list) {
                dataList.add(item.photoId);
            }

            // Проверяем какие фотки у нас УЖЕ есть
            List<StackPhotoDB> stack = StackPhotoRealm.getByServerIds(dataList);

            for (StackPhotoDB item : stack) {
                dataList.remove(Integer.valueOf(item.getPhotoServerId()));
            }

            Globals.writeToMLOG("INFO", "downloadSamplePhotos", "start downloadPhotoByIds dataList: " + dataList.size());

            new PhotoDownload().downloadPhotoByIds("/Sample", "SAMPLE_", dataList, new Clicks.clickStatusMsg() {
                @Override
                public void onSuccess(String data) {
                    Globals.writeToMLOG("INFO", "downloadSamplePhotos", "data: " + data);
                    click.onSuccess(data);
                }

                @Override
                public void onFailure(String error) {
                    Globals.writeToMLOG("ERROR", "downloadSamplePhotos", "data: " + error);
                    click.onFailure(error);
                }
            });
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "downloadSamplePhotos", "Exception e: " + e);
        }
    }

    public void downloadSamplePhotosByPhotoIds(List<Integer> list, Clicks.clickStatusMsg click) {
        try {
            Globals.writeToMLOG("INFO", "downloadSamplePhotos", "Получил айдишек list: " + list.size());

//            Вот место где ошибки
            new PhotoDownload().downloadPhotoByIds("/Sample", "SAMPLE_", list, new Clicks.clickStatusMsg() {
                @Override
                public void onSuccess(String data) {
                    Globals.writeToMLOG("INFO", "downloadSamplePhotos", "data: " + data);
                    click.onSuccess(data);
                }

                @Override
                public void onFailure(String error) {
                    Globals.writeToMLOG("ERROR", "downloadSamplePhotos", "data: " + error);
                    click.onFailure(error);
                }
            });
        }catch (Exception e){
            Log.i("````", "Exception: ", e);
        }
    }

    public List<Integer> getSamplePhotosToDownload(){
        return SQL_DB.samplePhotoDao().getAllPhotosIds();
    }
}
