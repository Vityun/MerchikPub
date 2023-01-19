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
                .addMigrations(MIGRATION_29_30)

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

    static final Migration MIGRATION_21_22 = new Migration(21, 22) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE sample_photo ADD COLUMN grpId INTEGER DEFAULT 0");
        }
    };

    static final Migration MIGRATION_22_23 = new Migration(22, 23) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE client ADD COLUMN recl_reply_mode INTEGER DEFAULT 0");
        }
    };

    static final Migration MIGRATION_23_24 = new Migration(23, 24) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `achievements` (`id` INTEGER NOT NULL, " +
                    "`serverId` INTEGER, `dt` TEXT, `img_before_id` INTEGER, `img_before` TEXT, `img_before_big` TEXT, " +
                    "`img_after_id` INTEGER, `img_after` TEXT, `img_after_big` TEXT, `score` TEXT, " +
                    "`score_who_nm` TEXT, `score_dt` TEXT, `adresa_nm` TEXT, `adresa_addr` TEXT, " +
                    "`adresa_tp` TEXT, `addr_id` INTEGER, `spiskli_nm` TEXT, `client_id` TEXT, " +
                    "`code_dad2` INTEGER, `user_id` INTEGER, `sotr_fio` TEXT, `comment_dt` TEXT, `comment_txt` TEXT, " +
                    "`comment_user` TEXT, `prem_reason` TEXT, `prem_amount` TEXT, `prem_amount_dt` TEXT, " +
                    "`prem_sotr` TEXT, `dvi` INTEGER, `confirm_state` INTEGER, PRIMARY KEY(`id`))");
        }
    };

    static final Migration MIGRATION_24_25 = new Migration(24, 25) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `votes` (`id` INTEGER NOT NULL, `serverId` INTEGER," +
                    "`isp` TEXT, `kli` TEXT, `addr_id` INTEGER, `dt` INTEGER, `df` INTEGER, `photo_id` INTEGER, " +
                    "`code_dad2` INTEGER, `code_iza` INTEGER, `voter_id` INTEGER, `score` INTEGER, `merchik` INTEGER, " +
                    "`ip` TEXT, `vote_type` INTEGER, `vote_class` INTEGER, `theme_id` INTEGER, `dt_day` INTEGER, " +
                    "`dt_month` INTEGER, `dt_year` INTEGER, `cntrl_doc` INTEGER, `flag` INTEGER, `comments` TEXT" +
                    ", PRIMARY KEY(`id`))");
        }
    };

    static final Migration MIGRATION_25_26 = new Migration(25, 26) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE achievements ADD COLUMN dt_ut INTEGER DEFAULT 0");
        }
    };

    static final Migration MIGRATION_27_28 = new Migration(27, 28) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `chat_grp` (`id` INTEGER NOT NULL, `nm` TEXT," +
                    "`dt` INTEGER, `dt_last_update` INTEGER, `author_id` INTEGER, `theme_id` INTEGER, `addr_id` INTEGER, `client_id` TEXT, " +
                    "`doc_id` INTEGER, `code_dad2` INTEGER, `code` INTEGER, `last_msg` TEXT, " +
                    " PRIMARY KEY(`id`))");
        }
    };

    static final Migration MIGRATION_28_29 = new Migration(28, 29) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("create table IF NOT EXISTS t_ch_msg_gr\n" +
                    "(\n" +
                    "    chat_id int, \n" +
                    "    kol_vsego int, \n" +
                    "    kol_read int, \n" +
                    "    kol_unread int, PRIMARY KEY(`chat_id`) \n" +
                    ")");
        }
    };


    static final Migration MIGRATION_29_30 = new Migration(29, 30) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `reclamation_percentage` (`id` INTEGER NOT NULL," +
                    "`tp` INTEGER," +
                    "`dt` TEXT," +
                    "`percent` REAL, " +
                    "`dt_update` INTEGER, " +

                    " PRIMARY KEY(`id`))");


            database.execSQL("CREATE TABLE IF NOT EXISTS `shelf_size` (`id` INTEGER NOT NULL," +

                    "`client_id` TEXT," +
                    "`addr_id` INTEGER," +
                    "`gruadr_id` INTEGER, " +
                    "`grp_id` INTEGER, " +
                    "`width` REAL, " +
                    "`planzn` REAL, " +
                    "`author_id` INTEGER, " +
                    "`dt` INTEGER, " +
                    "`dt_day` TEXT, " +
                    "`dt_change` INTEGER, " +

                    " PRIMARY KEY(`id`))");
        }
    };

}
