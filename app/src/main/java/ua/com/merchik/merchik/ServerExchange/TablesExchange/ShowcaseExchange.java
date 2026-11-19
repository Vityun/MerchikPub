package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import static ua.com.merchik.merchik.database.realm.RealmManager.getAllWorkPlan;
import static ua.com.merchik.merchik.database.realm.RealmManager.getAllWorkPlanWithOutRNO_LIST;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import io.realm.RealmResults;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ServerExchange.PhotoDownload;
import ua.com.merchik.merchik.Utils.FileUtils;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;

public class ShowcaseExchange {

    public void downloadShowcaseTable(ExchangeInterface.ExchangeResponseInterface exchange) {
        CatalogDataExchange.showcases(getAllWorkPlanWithOutRNO_LIST(), new Clicks.clickObjectAndStatus() {
            @Override public void onSuccess(Object data) {
                if (exchange != null) exchange.onSuccess((List<ShowcaseSDB>) data);
            }
            @Override public void onFailure(String error) {
                if (exchange != null) exchange.onFailure(error);
            }
        });
    }

    public void downloadShowcasePhoto(List<ShowcaseSDB> data) {
        PhotoDownload photoDownload = new PhotoDownload();
        Set<Integer> queued = new HashSet<>();
        for (ShowcaseSDB item : data) {
            if (!hasPhotoSource(item) || !queued.add(item.photoId)) continue;

            StackPhotoDB stackPhotoDB = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId2(String.valueOf(item.photoId));
            if (stackPhotoDB == null) {
                Log.e("checkRequest", "downloadShowcasePhoto/item: " + item.id);
                photoDownload.downloadPhoto(item.photoBig, new ExchangeInterface.ExchangePhoto() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        try {
                            long dt = System.currentTimeMillis() / 1000;

                            StackPhotoDB photoDB = new StackPhotoDB();
                            photoDB.setId(RealmManager.stackPhotoGetLastId() + 1);
                            photoDB.setPhotoServerId(String.valueOf(item.photoId));
                            photoDB.setDt(dt);
                            photoDB.setClient_id(item.clientId);
                            photoDB.setAddr_id(Integer.valueOf(item.addrId));
                            photoDB.setUser_id(null);
                            photoDB.setPhoto_type(0);

                            photoDB.setCreate_time(dt * 1000);// реквизиты что б фотки не выгружались обратно на сервер
                            photoDB.setUpload_to_server(dt);// реквизиты что б фотки не выгружались обратно на сервер
                            photoDB.setGet_on_server(dt);// реквизиты что б фотки не выгружались обратно на сервер

//                        photoDB.setPhoto_num(Globals.savePhotoToPhoneMemory("/Showcase", "" + item.id, bitmap));

                            photoDB.setPhotoServerURL(item.photoBig);


                            photoDB.img_src_id = String.valueOf(item.photoId);
                            photoDB.showcase_id = String.valueOf(item.id);
                            photoDB.planogram_id = Objects.toString(item.planogramId, "");
                            photoDB.planogram_img_id = String.valueOf(item.photoPlanogramId);
                            photoDB.setVpi(System.currentTimeMillis() / 1000);

                            photoDownload.savePhotoAndUpdateStackPhotoDB("/Showcase", "" + item.photoId, bitmap, photoDB);

//                        RealmManager.stackPhotoSavePhoto(photoDB);

                            Log.e("checkRequest", "downloadShowcasePhoto/photoDB: " + photoDB.getId());
                            Globals.writeToMLOG("INFO", "savePhotoToDB2/downloadPhoto/ShowcaseSDB/photoDB", "photoDB.getId(): " + photoDB.getId());
                        } catch (Exception e) {
                            Log.e("checkRequest", "downloadShowcasePhoto/Exception e: " + e);
                            Globals.writeToMLOG("ERR", "savePhotoToDB2/downloadPhoto/ShowcaseSDB", "Exception e: " + e);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("checkRequest", "downloadShowcasePhoto/String error: " + error);
                        Globals.writeToMLOG("ERR", "savePhotoToDB2/downloadPhoto/ShowcaseSDB/onFailure", "error: " + error);
                    }
                });
            } else if (!FileUtils.fileExistsAndNotEmpty(stackPhotoDB.getPhoto_num())) {
                photoDownload.downloadPhoto(item.photoBig, new ExchangeInterface.ExchangePhoto() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        try {
                            long dt = System.currentTimeMillis() / 1000;

                            stackPhotoDB.setDt(dt);

                            stackPhotoDB.setCreate_time(dt * 1000);// реквизиты что б фотки не выгружались обратно на сервер
                            stackPhotoDB.setUpload_to_server(dt);// реквизиты что б фотки не выгружались обратно на сервер
                            stackPhotoDB.setGet_on_server(dt);// реквизиты что б фотки не выгружались обратно на сервер

//                                stackPhotoDB.img_src_id = String.valueOf(item.photoId);
//                                stackPhotoDB.showcase_id = String.valueOf(item.id);
//                                stackPhotoDB.planogram_id = Objects.toString(item.planogramId, "");
//                                stackPhotoDB.planogram_img_id = String.valueOf(item.photoPlanogramId);

                            photoDownload.savePhotoAndUpdateStackPhotoDB("/Showcase", "" + item.photoId, bitmap, stackPhotoDB);

                            Log.e("checkRequest", "downloadShowcasePhoto/photoDB: " + stackPhotoDB.getId());
                            Globals.writeToMLOG("INFO", "savePhotoToDB2/downloadPhoto/ShowcaseSDB/photoDB", "photoDB.getId(): " + stackPhotoDB.getId());
                        } catch (Exception e) {
                            Log.e("checkRequest", "downloadShowcasePhoto/Exception e: " + e);
                            Globals.writeToMLOG("ERR", "savePhotoToDB2/downloadPhoto/ShowcaseSDB", "Exception e: " + e);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("checkRequest", "downloadShowcasePhoto/String error: " + error);
                        Globals.writeToMLOG("ERR", "savePhotoToDB2/downloadPhoto/ShowcaseSDB/onFailure", "error: " + error);
                    }
                });
            }
        }
    }

    public List<ShowcaseSDB> getSamplePhotosToDownload() {
        List<ShowcaseSDB> showcases = SQL_DB.showcaseDao().getAll();
        Set<Integer> photoIds = new HashSet<>();
        for (ShowcaseSDB row : showcases) if (hasPhotoSource(row)) photoIds.add(row.photoId);
        if (photoIds.isEmpty()) return new ArrayList<>();
        return selectPhotosToDownload(showcases, StackPhotoRealm.getByServerIds(new ArrayList<>(photoIds)));
    }

    static List<ShowcaseSDB> selectPhotosToDownload(List<ShowcaseSDB> showcases, List<StackPhotoDB> cached) {
        Set<Integer> downloaded = new HashSet<>();
        for (StackPhotoDB photo : cached) {
            if (photo == null || photo.getPhotoServerId() == null) continue;
            try {
                int id = Integer.parseInt(photo.getPhotoServerId().trim());
                // Metadata alone must not suppress downloading a missing file.
                if (id > 0 && FileUtils.fileExistsAndNotEmpty(photo.getPhoto_num())) downloaded.add(id);
            } catch (NumberFormatException ignored) { }
        }
        List<ShowcaseSDB> result = new ArrayList<>();
        Set<Integer> queued = new HashSet<>();
        for (ShowcaseSDB row : showcases) {
            if (hasPhotoSource(row) && !downloaded.contains(row.photoId) && queued.add(row.photoId)) result.add(row);
        }
        return result;
    }

    private static boolean hasPhotoSource(ShowcaseSDB row) {
        return row != null && row.photoId != null && row.photoId > 0
                && row.photoBig != null && !row.photoBig.trim().isEmpty()
                && !"0".equals(row.photoBig.trim()) && !"null".equalsIgnoreCase(row.photoBig.trim());
    }

    private List<WpDataDB> getWorkPlanList() {
        RealmResults<WpDataDB> realmResults = getAllWorkPlan(); // Получаем RealmResults
        return realmResults != null ? new ArrayList<>(realmResults) : new ArrayList<>(); // Преобразуем в List
    }

}
