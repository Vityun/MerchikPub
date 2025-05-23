//package ua.com.merchik.merchik.ServerExchange.synchronization;
//
//import android.app.AlarmManager;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.os.SystemClock;
//import android.util.Log;
//
//import java.util.List;
//
//import ua.com.merchik.merchik.data.SynchronizationTimeTable;
//import ua.com.merchik.merchik.data.synchronization.DownloadStatus;
//import ua.com.merchik.merchik.database.room.AppDatabase;
//
//public class SynchronizationManager {
//    private final AppDatabase database;
//    private final ApiService apiService;
//    private final Context context;
//
//    public SynchronizationManager(AppDatabase database, ApiService apiService, Context context) {
//        this.database = database;
//        this.apiService = apiService;
//        this.context = context;
//    }
//
//    /**
//     * Проверяет все таблицы на необходимость синхронизации
//     * и выполняет синхронизацию по мере необходимости
//     */
//    public void checkAndSyncTables() {
//        new Thread(() -> {
//            try {
//                List<SynchronizationTimeTable> tables = database.syncTimeTableDao().getAll();
//                long currentTime = System.currentTimeMillis() / 1000;
//
//                for (SynchronizationTimeTable table : tables) {
//                    if (shouldSyncDownload(table, currentTime)) {
//                        syncTableDownload(table);
//                    }
//
//                    if (shouldSyncUpload(table, currentTime)) {
//                        syncTableUpload(table);
//                    }
//                }
//            } catch (Exception e) {
//                Log.e("SyncManager", "Error during sync check", e);
//            }
//        }).start();
//    }
//
//    /**
//     * Проверяет, нужно ли выполнять загрузку данных для таблицы
//     */
//    private boolean shouldSyncDownload(SynchronizationTimeTable table, long currentTime) {
//        // Если еще никогда не синхронизировали
//        if (table.getLastDownloadTime() == 0) {
//            return true;
//        }
//
//        // Проверяем период синхронизации
//        long timeSinceLastSync = currentTime - table.getLastDownloadTime();
//        return timeSinceLastSync >= table.getSyncPeriodSeconds();
//    }
//
//    /**
//     * Проверяет, нужно ли выполнять выгрузку данных
//     */
//    private boolean shouldSyncUpload(SynchronizationTimeTable table, long currentTime) {
//        // Только для таблиц с пользовательскими данными
//        if (!table.isUserGenerated()) {
//            return false;
//        }
//
//        // Если еще никогда не синхронизировали
//        if (table.getLastUploadTime() == 0) {
//            return true;
//        }
//
//        // Проверяем период синхронизации
//        long timeSinceLastSync = currentTime - table.getLastUploadTime();
//        return timeSinceLastSync >= table.getSyncPeriodSeconds();
//    }
//
//    /**
//     * Выполняет загрузку данных для указанной таблицы
//     */
//    private void syncTableDownload(SynchronizationTimeTable table) {
//        try {
//            // Обновляем статус на "в процессе"
//            table.setLastDownloadStatus(DownloadStatus.PENDING);
//            database.syncTimeTableDao().update(table);
//
//            // Выполняем загрузку данных
//            SyncResponse response = apiService.downloadTableData(table.getTableName().name());
//
//            // Обрабатываем полученные данные
//            processDownloadedData(table, response);
//
//            // Обновляем статус и время
//            table.setLastDownloadStatus(DownloadStatus.SUCCESS);
//            table.setLastDownloadTime(System.currentTimeMillis() / 1000);
//            table.setDownloadedItems(response.getItems().size());
//            database.syncTimeTableDao().update(table);
//
//        } catch (Exception e) {
//            Log.e("SyncManager", "Download failed for table: " + table.getTableName(), e);
//            table.setLastDownloadStatus(DownloadStatus.ERROR);
//            database.syncTimeTableDao().update(table);
//        }
//    }
//
//    /**
//     * Выполняет выгрузку данных для указанной таблицы
//     */
//    private void syncTableUpload(SynchronizationTimeTable table) {
//        try {
//            // Обновляем статус на "в процессе"
//            table.setLastUploadStatus(DownloadStatus.PENDING);
//            database.syncTimeTableDao().update(table);
//
//            // Получаем локальные данные для выгрузки
//            List<?> dataToUpload = getLocalDataForUpload(table.getTableName());
//
//            // Выполняем выгрузку
//            SyncResponse response = apiService.uploadTableData(table.getTableName().name(), dataToUpload);
//
//            // Обновляем статус и время
//            table.setLastUploadStatus(DownloadStatus.SUCCESS);
//            table.setLastUploadTime(System.currentTimeMillis() / 1000);
//            table.setUploadedItems(dataToUpload.size());
//            database.syncTimeTableDao().update(table);
//
//        } catch (Exception e) {
//            Log.e("SyncManager", "Upload failed for table: " + table.getTableName(), e);
//            table.setLastUploadStatus(DownloadStatus.ERROR);
//            database.syncTimeTableDao().update(table);
//        }
//    }
//
//    /**
//     * Обрабатывает загруженные данные и сохраняет их в соответствующую таблицу
//     */
//    private void processDownloadedData(SynchronizationTimeTable table, SyncResponse response) {
//        switch (table.getTableName()) {
//            case USERS:
//                List<User> users = parseUsers(response.getData());
//                database.userDao().insertAll(users);
//                break;
//            case PRODUCTS:
//                List<Product> products = parseProducts(response.getData());
//                database.productDao().insertAll(products);
//                break;
//            // Добавьте обработку других таблиц по аналогии
//            default:
//                throw new IllegalArgumentException("Unknown table: " + table.getTableName());
//        }
//    }
//
//    /**
//     * Получает локальные данные для выгрузки
//     */
//    private List<?> getLocalDataForUpload(TableName tableName) {
//        switch (tableName) {
//            case ORDERS:
//                return database.orderDao().getAllForSync();
//            case COMMENTS:
//                return database.commentDao().getAllForSync();
//            // Добавьте другие таблицы по аналогии
//            default:
//                throw new IllegalArgumentException("Table not marked as user-generated: " + tableName);
//        }
//    }
//
//    /**
//     * Запускает периодическую проверку синхронизации
//     */
//    public void startPeriodicSync() {
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(context, SyncReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        // Проверяем каждые 15 минут
//        long interval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
//        alarmManager.setInexactRepeating(
//                AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                SystemClock.elapsedRealtime() + interval,
//                interval,
//                pendingIntent
//        );
//    }
//}