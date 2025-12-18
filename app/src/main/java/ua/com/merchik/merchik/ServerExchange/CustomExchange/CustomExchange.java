package ua.com.merchik.merchik.ServerExchange.CustomExchange;

import static ua.com.merchik.merchik.database.realm.RealmManager.addSynchronizationTimetable;
import static ua.com.merchik.merchik.database.realm.tables.SynchronizationTimetableRealm.getSynchronizationTimetable;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.PhotoMerchikExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.SamplePhotoExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.ShowcaseExchange;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.RetrofitResponse.photos.ImagesViewListImageList;
import ua.com.merchik.merchik.data.RetrofitResponse.photos.PhotoInfoResponseList;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.dialogs.BlockingProgressDialog;
import ua.com.merchik.merchik.dialogs.DialogData;

public class CustomExchange {

    private Exchange exchange = new Exchange();

    public void showDialogExchange(Context context) {
        try {
            DialogData dialog = new DialogData(context);
            dialog.setTitle("Обмін");
            dialog.setText("Нижче у Вас є можливість завантажити або виватажити деякі данні, для цього треба обрати конкретний пункт.");
            dialog.setRecycler(createAdapter(), new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            dialog.setOkRecycler("Зробити повну синхронізацію", () -> {
                exchange.startExchange();
            });

            dialog.setClose(dialog::dismiss);
            dialog.show();
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "CustomExchange/showDialogExchange", "Проблемма где-то в формировании модального окошка Exception e: " + e);
        }
    }

    private RecyclerView.Adapter createAdapter() {
        return new CustomExchangeAdapter(getAdapterData());
    }

    private List<SynchronizationTimetableDB> getAdapterData() {
        List<SynchronizationTimetableDB> res = new ArrayList<>();
//        res = RealmManager.INSTANCE.copyFromRealm(RealmManager.getSynchronizationTimetable());
        res = getSynchronizationTimetable();
        if (res == null || res.size() == 0){
            addSynchronizationTimetable();
        }
        res = getSynchronizationTimetable();
        return res;
    }

    public void startExchangeBySyncTable(Context context, String table) {
        try {
            switch (table) {
                case "photo_tovar":
                    try {
                        Toast.makeText(context, "Починаю завантажувати фото Товарів", Toast.LENGTH_LONG).show();
//                        BlockingProgressDialog progress = new BlockingProgressDialog(context, "Завантаження фото Товарів", "Починаю завантажувати фотографії Товарів");
//                        progress.show();
                        PhotoDownload.getPhotoURLFromServer(TovarRealm.getAllTov(), new Clicks.clickStatusMsg() {
                            @Override
                            public void onSuccess(String data) {
                                Toast.makeText(context, data, Toast.LENGTH_LONG).show();

                                DialogData dialogData = new DialogData(context);
                                dialogData.setTitle("Завантаження фото Товарів");
                                dialogData.setText(data);
                                dialogData.setClose(dialogData::dismiss);
                                dialogData.show();


                                Log.e("test", "String data: " + data);
                                Globals.writeToMLOG("INFO", "CustomExchange/startExchangeBySyncTable/photo_tovar", "onSuccess String data: " + data);
                            }

                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show();

                                DialogData dialogData = new DialogData(context);
                                dialogData.setTitle("Завантаження фото Товарів ПОМИЛКА!: ");
                                dialogData.setText(error);
                                dialogData.setClose(dialogData::dismiss);
                                dialogData.show();

                                Log.e("test", "String error: " + error);
                                Globals.writeToMLOG("INFO", "CustomExchange/startExchangeBySyncTable/photo_tovar", "onFailure error: " + error);
                            }
                        }, new Clicks.clickStatusMsgMode() {
                            @Override
                            public void onSuccess(String data, Clicks.MassageMode mode) {
//                                switch (mode){
//                                    case SHOW -> progress.updateText(data);
//                                    case CLOSE -> {
//                                        progress.updateText(data);
//                                        progress.dismiss();
//
////                                        DialogData dialogData = new DialogData(context);
////                                        dialogData.setTitle("Оновлення фото Товарів");
////                                        dialogData.setText(data);
////                                        dialogData.setClose(dialogData::dismiss);
////                                        dialogData.show();
//                                    }
//                                }
                            }

                            @Override
                            public void onFailure(String error) {
                                DialogData dialogData = new DialogData(context);
                                dialogData.setTitle("Оновлення фото Товарів");
                                dialogData.setText(error);
                                dialogData.setClose(dialogData::dismiss);
                                dialogData.show();
                            }
                        }, context);
                    }catch (Exception e){
                        Toast.makeText(context, "Помилка при завантаженні товарів: " + e, Toast.LENGTH_LONG).show();
                        Globals.writeToMLOG("ERROR", "CustomExchange/startExchangeBySyncTable/photo_tovar", "Exception e: " + e);
                        Globals.writeToMLOG("ERROR", "CustomExchange/startExchangeBySyncTable/photo_tovar", "Exception e: " + Arrays.toString(e.getStackTrace()));
                    }
                    break;
                case "photo_sample":
                    try {
                        Toast.makeText(context, "Завантажую ідентифікатори фото", Toast.LENGTH_LONG).show();
                        SamplePhotoExchange samplePhotoExchange = new SamplePhotoExchange();
                        List<Integer> listPhotosToDownload = samplePhotoExchange.getSamplePhotosToDownload();
                        if (listPhotosToDownload != null && listPhotosToDownload.size() > 0) {
                            Globals.writeToMLOG("INFO", "TOOBAR/CLICK_EXCHANGE/SamplePhotoExchange", "listPhotosToDownload: " + listPhotosToDownload.size());
                            BlockingProgressDialog progress = new BlockingProgressDialog(context, "Ідентифікатори фото", "Починаю завантажувати " + listPhotosToDownload.size() + " ідентифікаторів фото. Це може зайняти деякий час.");
                            progress.show();
                            samplePhotoExchange.downloadSamplePhotosByPhotoIds(listPhotosToDownload, new Clicks.clickStatusMsg() {
                                @Override
                                public void onSuccess(String data) {
                                    Globals.writeToMLOG("INFO", "TOOBAR/CLICK_EXCHANGE/SamplePhotoExchange", "data: " + data);
                                    progress.dismiss();
//                                    Toast.makeText(context, "Завантаження ідентифікаторів фото - завершено.", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(String error) {
                                    Globals.writeToMLOG("INFO", "TOOBAR/CLICK_EXCHANGE/SamplePhotoExchange", "error: " + error);
                                    progress.dismiss();
//                                    Toast.makeText(context, "Виникла помилка при завантаженні Ідентифікаторів фото", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(context, "Всі ідентифікатори вітрин вже завантажені!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "CustomExchange/startExchangeBySyncTable/SamplePhotoExchange", "Exception e: " + e);
                        Globals.writeToMLOG("ERROR", "CustomExchange/startExchangeBySyncTable/SamplePhotoExchange", "Exception e: " + Arrays.toString(e.getStackTrace()));
                    }
                    break;
                case "photo_planogram":
                    Toast.makeText(context, "Завантажую фото планограм", Toast.LENGTH_LONG).show();
                    exchange.planogram(new ExchangeInterface.ExchangeResponseInterface() {
                        @Override
                        public <T> void onSuccess(List<T> data) {
                            try {
                                List<ImagesViewListImageList> datalist = (List<ImagesViewListImageList>) data;
                                new PhotoDownload().savePhotoToDB2(datalist);
                                Globals.writeToMLOG("INFO", "startExchange/planogram.onSuccess", "OK: " + datalist.size());
                            } catch (Exception e) {
                                Globals.writeToMLOG("ERROR", "startExchange/planogram.onSuccess", "Exception e: " + e);
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            Globals.writeToMLOG("FAIL", "startExchange/planogram/onFailure", error);
                        }
                    }); // Получение планограмм
                    break;
                case "photo_showcase":
                    try {
                        Toast.makeText(context, "Завантажую зразки вітрин", Toast.LENGTH_LONG).show();
                        ShowcaseExchange showcaseExchange = new ShowcaseExchange();
                        List<ShowcaseSDB> list = showcaseExchange.getSamplePhotosToDownload();
                        if (list != null && list.size() > 0) {
                            Globals.writeToMLOG("INFO", "TOOBAR/CLICK_EXCHANGE/ShowcaseExchange", "list: " + list.size());
                            showcaseExchange.downloadShowcasePhoto(list);
                        } else {
                            Toast.makeText(context, "Всі зразки вже завантажені!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "CustomExchange/startExchangeBySyncTable/ShowcaseExchange", "Exception e: " + e);
                        Globals.writeToMLOG("ERROR", "CustomExchange/startExchangeBySyncTable/ShowcaseExchange", "Exception e: " + Arrays.toString(e.getStackTrace()));
                    }
                    break;
                case "coments_to_photo":
                    try {// Выгрузка изменённых комментариев
                        Toast.makeText(context, "Вивантажую коментарі до фото", Toast.LENGTH_LONG).show();
                        exchange.sendPhotoInformation(exchange.getPhotoInfoToUpload(Exchange.UploadPhotoInfo.COMMENT), new ExchangeInterface.ExchangeResponseInterface() {
                            @Override
                            public <T> void onSuccess(List<T> data) {
                                try {
                                    List<PhotoInfoResponseList> photo = (List<PhotoInfoResponseList>) data;
                                    if (photo.size() > 0) {
                                        Integer[] ids = new Integer[photo.size()];
                                        int count = 0;
                                        for (PhotoInfoResponseList item : photo) {
                                            ids[count++] = item.elementId;
                                        }

                                        Log.e("sendPhotoInformation", "photo.size(): " + photo.size());

                                        Log.e("sendPhotoInformation", "photoIds: " + Arrays.toString(ids));

                                        List<StackPhotoDB> stackPhoto = RealmManager.INSTANCE.copyFromRealm(StackPhotoRealm.getById(ids));
                                        Log.e("sendPhotoInformation", "stackPhoto: " + stackPhoto.size());

                                        for (StackPhotoDB item : stackPhoto) {
                                            for (PhotoInfoResponseList listItem : photo) {
                                                if (listItem.elementId.equals(item.getId())) {
                                                    if (listItem.state) {
                                                        item.setCommentUpload(false);
                                                        Log.e("sendPhotoInformation", "listItem.state: " + listItem.state);
                                                    } else {
                                                        item.setCommentUpload(false);
                                                        item.setComment(listItem.error);
                                                        Log.e("sendPhotoInformation", "listItem.state: " + listItem.state);
                                                        Log.e("sendPhotoInformation", "listItem.error: " + listItem.error);
                                                    }
                                                    Log.e("sendPhotoInformation", "stackPhoto item save: " + item.photoServerId);
                                                    StackPhotoRealm.setAll(Collections.singletonList(item));
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Globals.writeToMLOG("ERROR", "sendPhotoInformation(getPhotoInfoToUpload(UploadPhotoInfo.COMMENT)", "Exception e: " + e);
                                }
                            }

                            @Override
                            public void onFailure(String error) {

                            }
                        });
                    }catch (Exception e){
                        Globals.writeToMLOG("ERROR", "CustomExchange/startExchangeBySyncTable/coments_to_photo", "Exception e: " + e);
                    }
                    break;
                case "photo_user_from_serv":
                    try {
                        Toast.makeText(context, "Починаю завантажувати фото користувачів", Toast.LENGTH_LONG).show();
                        PhotoMerchikExchange photoMerchikExchange = new PhotoMerchikExchange();
                        photoMerchikExchange.getPhotoFromSite();
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "CustomExchange/startExchangeBySyncTable/photo_user_from_serv", "Exception e: " + e);
                        Globals.writeToMLOG("ERROR", "CustomExchange/startExchangeBySyncTable/photo_user_from_serv", "Exception e: " + Arrays.toString(e.getStackTrace()));
                    }
                    break;
                case "upload_ekl":
                    Toast.makeText(context, "Данний пункт ще не описано", Toast.LENGTH_LONG).show();
                    break;
                case "photo_tar":
                    Toast.makeText(context, "Данний пункт ще не описано", Toast.LENGTH_LONG).show();
                    break;


                // Пока игнор
                case "location":
                    break;
                case "sample_photo":
                    break;
                default:
                    Toast.makeText(context, "Данний пункт ще не описано", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "CustomExchange/startExchangeBySyncTable", "Exception e: " + e);
        }
    }

}
