package ua.com.merchik.merchik.database.room;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class RoomManager {

    public static AppDatabase SQL_DB;

    public static void init(Context context) {
        SQL_DB = Room.databaseBuilder(context,
                AppDatabase.class, "merchik.db")
                .fallbackToDestructiveMigration()
                .enableMultiInstanceInvalidation()
                .allowMainThreadQueries()
//                .addMigrations(MIGRATION_12_13)

                .build();
    }



//    ----------------------------------------------------------------------------------------------

    static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            int s = startVersion;
            int o = endVersion;
            int i = database.getVersion();
            Log.d("", "");
            database.execSQL("ALTER TABLE tasks_and_reclamations ADD COLUMN uploadStatus INTEGER NOT NULL DEFAULT 0");
        }
    };


}
