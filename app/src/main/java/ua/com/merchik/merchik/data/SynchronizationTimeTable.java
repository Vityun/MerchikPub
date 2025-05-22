package ua.com.merchik.merchik.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import androidx.annotation.NonNull;

import ua.com.merchik.merchik.data.synchronization.DownloadStatus;
import ua.com.merchik.merchik.data.synchronization.TableName;

@Entity(tableName = "synchronization_timetable")
public class SynchronizationTimeTable {

    @PrimaryKey
    private int id;

    @NonNull
    private TableName tableName;

    private long syncPeriodSeconds;     // Период синхронизации
    private long lastDownloadTime;      // Время последней успешной загрузки
    private long lastUploadTime;        // Время последней успешной отправки

    private int downloadedItems;        // Количество загруженных записей
    private int uploadedItems;          // Количество отправленных записей

    private String description;         // Описание

    private boolean isUserGenerated;    // true если данные может создать пользователь

    private DownloadStatus lastDownloadStatus;  // success / error / pending
    private DownloadStatus lastUploadStatus;    // success / error / pending

    public SynchronizationTimeTable(int id,
                                    @NonNull TableName tableName,
                                    long syncPeriodSeconds,
                                    long lastDownloadTime,
                                    long lastUploadTime,
                                    int downloadedItems,
                                    int uploadedItems,
                                    String description,
                                    boolean isUserGenerated,
                                    DownloadStatus lastDownloadStatus,
                                    DownloadStatus lastUploadStatus) {
        this.id = id;
        this.tableName = tableName;
        this.syncPeriodSeconds = syncPeriodSeconds;
        this.lastDownloadTime = lastDownloadTime;
        this.lastUploadTime = lastUploadTime;
        this.downloadedItems = downloadedItems;
        this.uploadedItems = uploadedItems;
        this.description = description;
        this.isUserGenerated = isUserGenerated;
        this.lastDownloadStatus = lastDownloadStatus;
        this.lastUploadStatus = lastUploadStatus;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public TableName getTableName() { return tableName; }
    public void setTableName(@NonNull TableName tableName) { this.tableName = tableName; }

    public long getSyncPeriodSeconds() { return syncPeriodSeconds; }
    public void setSyncPeriodSeconds(long syncPeriodSeconds) { this.syncPeriodSeconds = syncPeriodSeconds; }

    public long getLastDownloadTime() { return lastDownloadTime; }
    public void setLastDownloadTime(long lastDownloadTime) { this.lastDownloadTime = lastDownloadTime; }

    public long getLastUploadTime() { return lastUploadTime; }
    public void setLastUploadTime(long lastUploadTime) { this.lastUploadTime = lastUploadTime; }

    public int getDownloadedItems() { return downloadedItems; }
    public void setDownloadedItems(int downloadedItems) { this.downloadedItems = downloadedItems; }

    public int getUploadedItems() { return uploadedItems; }
    public void setUploadedItems(int uploadedItems) { this.uploadedItems = uploadedItems; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isUserGenerated() { return isUserGenerated; }
    public void setUserGenerated(boolean userGenerated) { isUserGenerated = userGenerated; }

    public DownloadStatus getLastDownloadStatus() { return lastDownloadStatus; }
    public void setLastDownloadStatus(DownloadStatus lastDownloadStatus) { this.lastDownloadStatus = lastDownloadStatus; }

    public DownloadStatus getLastUploadStatus() { return lastUploadStatus; }
    public void setLastUploadStatus(DownloadStatus lastUploadStatus) { this.lastUploadStatus = lastUploadStatus; }
}
