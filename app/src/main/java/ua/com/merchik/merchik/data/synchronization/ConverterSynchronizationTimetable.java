package ua.com.merchik.merchik.data.synchronization;

import androidx.room.TypeConverter;


public class ConverterSynchronizationTimetable {
    @TypeConverter
    public static DownloadStatus toDownloadStatus(int code) {
        return DownloadStatus.fromCode(code);
    }

    @TypeConverter
    public static int fromDownloadStatus(DownloadStatus status) {
        return status.getCode();
    }

    // Для TableName (сохраняем как String)
    @TypeConverter
    public static TableName toTableName(String tableCode) {
        return TableName.fromCode(tableCode);
    }

    @TypeConverter
    public static String fromTableName(TableName tableName) {
        return tableName.getCode();
    }
}