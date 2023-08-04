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
                .addMigrations(MIGRATION_38_39)

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

    static final Migration MIGRATION_30_31 = new Migration(30, 31) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `fragment` (`id` INTEGER NOT NULL," +
                    "`img_id` INTEGER," +
                    "`region_num` INTEGER," +
                    "`x1` INTEGER," +
                    "`y1` INTEGER," +
                    "`x2` INTEGER," +
                    "`y2` INTEGER," +
                    "`comment` TEXT," +
                    "`author_id` INTEGER," +
                    "`dt_update` INTEGER, " +

                    " PRIMARY KEY(`id`))");
        }
    };

    static final Migration MIGRATION_31_32 = new Migration(31, 32) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 1. Создайте временную таблицу с новой схемой
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS client_temp (" +
                            "id TEXT NOT NULL PRIMARY KEY, " +
                            "nm TEXT, " +
                            "edrpou TEXT, " +
                            "main_tov_grp INTEGER, " +
                            "dt_update INTEGER, " +
                            "recl_reply_mode INTEGER)");

            // 2. Копируйте данные из таблицы client в client_temp
            // Обратите внимание, что мы используем функцию CAST(edrpou AS TEXT) в запросе INSERT, чтобы сконвертировать значение из типа LONG в TEXT.
            database.execSQL("INSERT INTO client_temp SELECT id, nm, CAST(edrpou AS TEXT), main_tov_grp, dt_update, recl_reply_mode FROM client");

            // 3. Удалите оригинальную таблицу client
            database.execSQL("DROP TABLE client");

            // 4. Переименуйте временную таблицу client_temp в client
            database.execSQL("ALTER TABLE client_temp RENAME TO client");
        }
    };


    static final Migration MIGRATION_32_33 = new Migration(32, 33) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE client ADD COLUMN ppa_auto INTEGER");
        }
    };

    static final Migration MIGRATION_33_34 = new Migration(33, 34) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE address ADD COLUMN nomer_tt INTEGER");
        }
    };

    private static final String CREATE_ADDITIONAL_MATERIALS_GROUPS_TABLE =
            "CREATE TABLE IF NOT EXISTS additional_materials_groups (" +
                    "id INTEGER PRIMARY KEY NOT NULL," +
                    "file_id INTEGER," +
                    "group_id INTEGER," +
                    "author_id INTEGER," +
                    "dt_update INTEGER" +
                    ")";


    static final Migration MIGRATION_34_35 = new Migration(34, 35) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(CREATE_ADDITIONAL_MATERIALS_GROUPS_TABLE);
        }
    };


    private static final String CREATE_showcase_TABLE =
            "CREATE TABLE IF NOT EXISTS `showcase` (" +
                    "`ID` INTEGER PRIMARY KEY NOT NULL, " +
                    "`select_id` TEXT, " +
                    "`select_name` TEXT, " +
                    "`photo` TEXT, " +
                    "`photo_id` INTEGER, " +
                    "`photo_big` TEXT, " +
                    "`photo_planogram_id` INTEGER, " +
                    "`photo_planogram_txt` TEXT, " +
                    "`photo_planogram_author_id` TEXT, " +
                    "`photo_planogram_author_txt` TEXT, " +
                    "`photo_planogram_dt_update` TEXT, " +
                    "`isp_id` TEXT, " +
                    "`isp_txt` TEXT, " +
                    "`client_id` TEXT, " +
                    "`client_txt` TEXT, " +
                    "`addr_id` TEXT, " +
                    "`addr_addr` TEXT, " +
                    "`line` TEXT, " +
                    "`rack` TEXT, " +
                    "`status` INTEGER, " +
                    "`status_author_id` TEXT, " +
                    "`status_author_txt` TEXT, " +
                    "`status_dt_update` TEXT, " +
                    "`author_id` TEXT, " +
                    "`author_txt` TEXT, " +
                    "`dt_update` TEXT, " +
                    "`nm` TEXT, " +
                    "`tovar_grp` INTEGER, " +
                    "`isp` TEXT, " +
                    "`photo_planogram_id_author` INTEGER, " +
                    "`photo_planogram_id_dt_update` INTEGER, " +
                    "`status_author` INTEGER" +
                    ")";


    static final Migration MIGRATION_36_37 = new Migration(36, 37) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(CREATE_showcase_TABLE);
        }
    };

    static final Migration MIGRATION_37_38 = new Migration(37, 38) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE sotr ADD COLUMN flag TEXT");
        }
    };


    static final Migration MIGRATION_38_39 = new Migration(38, 39) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE showcase ADD COLUMN planogram_id INTEGER");
        }
    };

    static final Migration MIGRATION_39_40 = new Migration(39, 40) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE article_temp AS SELECT * FROM article");

            // Drop the old table
            database.execSQL("DROP TABLE article");

            // Create the new table with the updated schema
            database.execSQL("CREATE TABLE article (" +
                    "id INTEGER PRIMARY KEY NOT NULL, " +
                    "vendor_code TEXT, " + // Changed data type from INTEGER to TEXT
                    "tovar_id INTEGER, " +
                    "addr_tp_id INTEGER, " +
                    "dt_update INTEGER)");

            // Restore the data from the temporary table into the new table
            database.execSQL("INSERT INTO article (id, vendor_code, tovar_id, addr_tp_id, dt_update) " +
                    "SELECT id, CAST(vendor_code AS TEXT), tovar_id, addr_tp_id, dt_update FROM article_temp");

            // Drop the temporary table
            database.execSQL("DROP TABLE article_temp");
        }
    };
}
