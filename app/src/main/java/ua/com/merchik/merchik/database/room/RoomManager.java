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
                .enableMultiInstanceInvalidation()
                .allowMainThreadQueries()
//                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build();
    }



//    ----------------------------------------------------------------------------------------------
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE site_objects "
                    + " ADD COLUMN additional_id INTEGER");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            Log.e("MIGRATION_2_3", "HERE");
            Log.e("MIGRATION_2_3", "database: " + database.getVersion());

            database.execSQL("BEGIN TRANSACTION;");

            database.execSQL("CREATE TABLE oborot_ved_test(" +
                    "'id' INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "'fir_id' TEXT," +
                    "'kli_id' TEXT," +
                    "'gru_id' INTEGER," +
                    "'adr_id' INTEGER," +
                    "'tov_id' INTEGER," +
                    "'dog_id' INTEGER," +
                    "'dat' DATE," +
                    "'date_from' DATE," +
                    "'date_to' DATE," +
                    "'kol_post' INTEGER," +
                    "'kol_prod' INTEGER," +
                    "'kol_ost' INTEGER," +
                    "'vpi' INTEGER," +
                    "'DAO' TEXT," +
                    "'author_id' INTEGER," +
                    "'IZA' LONG);");

//            database.execSQL("INSERT INTO oborot_ved_test(" +
//                    "id,fir_id,kli_id,gru_id,adr_id,tov_id,dog_id,dat,date_from,date_to,kol_post,kol_prod,kol_ost,vpi,DAO,author_id,IZA" +
//                    ") SELECT " +
//                    "NULL,'fir_id','kli_id','gru_id','adr_id','tov_id','dog_id','dat','date_from','date_to','kol_post','kol_prod','kol_ost','vpi','DAO','author_id','IZA'" +
//                    "FROM oborot_ved;");

            database.execSQL("DROP TABLE oborot_ved;");

            database.execSQL("ALTER TABLE 'oborot_ved_test' RENAME TO 'oborot_ved';");

            database.execSQL("COMMIT;");

            Log.e("MIGRATION_2_3", "HERE");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            "CREATE TABLE IF NOT EXISTS `translates` (`id` TEXT NOT NULL, `num_1c` TEXT, `internal_name` TEXT, `lang_id` TEXT, `default_value` TEXT, `nm` TEXT, `title` TEXT, `script_mod` TEXT, `script_act` TEXT, `url` TEXT, `platform_id` TEXT, `dt_update` TEXT, PRIMARY KEY(`id`))"

//            database.execSQL("CREATE TABLE IF NOT EXISTS `address` (" +
//                    "id );");
        }
    };


}
