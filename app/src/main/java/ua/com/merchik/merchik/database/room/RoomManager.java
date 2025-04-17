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
                .addMigrations(
                        MIGRATION_12_13,
                        MIGRATION_40_41,
                        MIGRATION_41_42,
                        MIGRATION_42_51,
                        MIGRATION_51_52,
                        MIGRATION_52_53,
                        MIGRATION_53_54,
                        MIGRATION_54_55,
                        MIGRATION_55_56,
                        MIGRATION_56_57,
                        MIGRATION_57_58,
                        MIGRATION_58_59,
                        MIGRATION_59_60,
                        MIGRATION_60_61
                        , MIGRATION_61_62
                )

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

    static final Migration MIGRATION_40_41 = new Migration(40, 41) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Удаляем существующую таблицу ekl
            database.execSQL("DROP TABLE IF EXISTS ekl");

            // Создаем новую таблицу ekl с вашей структурой
            database.execSQL("CREATE TABLE IF NOT EXISTS ekl (" +
                    "id INTEGER PRIMARY KEY, " +
                    "dt INTEGER, " +
                    "dt_verify INTEGER, " +
                    "user_id INTEGER, " +
                    "sotr_id INTEGER, " +
                    "client_id TEXT, " +
                    "address_id INTEGER, " +
                    "department INTEGER, " +
                    "dad2 INTEGER, " +
                    "code TEXT, " +
                    "ekl_code TEXT, " +
                    "ekl_hash_code TEXT, " +
                    "state INTEGER, " +
                    "doc_type INTEGER, " +
                    "doc_num INTEGER, " +
                    "doc_num_1c TEXT, " +
                    "vpi INTEGER, " +
                    "vpiSend INTEGER, " +
                    "upload INTEGER, " +
                    "comment TEXT, " +
                    "code_verify INTEGER)");
        }
    };

    static final Migration MIGRATION_41_42 = new Migration(41, 42) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `tasks_and_reclamations_new` (" +
                    "`id` INTEGER NOT NULL, " +
                    "`tp` INTEGER, " +
                    "`dt` INTEGER, " +
                    "`dt_real_post` INTEGER, " +
                    "`dt_change` INTEGER, " +
                    "`author` INTEGER, " +
                    "`addr` INTEGER, " +
                    "`client` TEXT, " +
                    "`state` INTEGER, " +
                    "`photo` INTEGER, " +
                    "`photo_2` INTEGER, " +
                    "`photoHash` TEXT, " +
                    "`comment` TEXT, " +
                    "`vinovnik` INTEGER, " +
                    "`vinovnik2` INTEGER, " +
                    "`vinovnik_read_dt` INTEGER, " +
                    "`zamena_user_id` INTEGER, " +
                    "`zamena_dt` INTEGER, " +
                    "`zamena_who` INTEGER, " +
                    "`contacter_id` INTEGER, " +
                    "`super_id` INTEGER, " +
                    "`territorial_id` INTEGER, " +
                    "`regional_id` INTEGER, " +
                    "`nop_id` INTEGER, " +
                    "`dvi` INTEGER, " +
                    "`zakazchik` INTEGER, " +
                    "`id_1c` TEXT, " +
                    "`doc_num_1c_id` INTEGER, " +
                    "`code_dad2` INTEGER, " +
                    "`code_dad2_src_doc` INTEGER, " +
                    "`tel_num` TEXT, " +
                    "`last_answer` TEXT, " +
                    "`last_answer_user_id` INTEGER, " +
                    "`last_answer_dt_change` TEXT, " +
                    "`respond` INTEGER, " +
                    "`report_id` INTEGER, " +
                    "`discount` INTEGER, " +
                    "`discount_smeta` TEXT, " +
                    "`vote_score` INTEGER, " +
                    "`voter_id` INTEGER, " +
                    "`vinovnik_score` INTEGER, " +
                    "`vinovnik_score_user_id` INTEGER, " +
                    "`vinovnik_score_comment` TEXT, " +
                    "`vinovnik_score_dt` INTEGER, " +
                    "`theme_grp_id` INTEGER, " +
                    "`theme_id` INTEGER, " +
                    "`sum_premiya` TEXT, " +
                    "`sum_penalty` TEXT, " +
                    "`duration` INTEGER, " +
                    "`ref_id` INTEGER, " +
                    "`summa_zp` TEXT, " +
                    "`budget` TEXT, " +
                    "`complete` TEXT, " +
                    "`sotr_opinion_id` INTEGER, " +
                    "`sotr_opinion_author_id` INTEGER, " +
                    "`sotr_opinion_dt` INTEGER, " +
                    "`no_need_reply` INTEGER, " +
                    "`audioId` INTEGER, " + // Изменение типа на INTEGER
                    "`potential_client_id` INTEGER, " +
                    "`dt_start_plan` INTEGER, " +
                    "`dt_end_plan` INTEGER, " +
                    "`dt_start_fact` INTEGER, " +
                    "`dt_end_fact` INTEGER, " +
                    "`uploadStatus` INTEGER, " +
                    "`addr_nm` TEXT, " +
                    "`client_nm` TEXT, " +
                    "`sotr_nm` TEXT, " +
                    "`coord_X` TEXT, " +
                    "`coord_Y` TEXT, " +
                    "PRIMARY KEY(`id`))");

            database.execSQL("INSERT INTO tasks_and_reclamations_new " +
                    "SELECT id, tp, dt, dt_real_post, dt_change, author, addr, client, state, " +
                    "photo, photo_2, photoHash, comment, vinovnik, vinovnik2, vinovnik_read_dt, " +
                    "zamena_user_id, zamena_dt, zamena_who, contacter_id, super_id, territorial_id, " +
                    "regional_id, nop_id, dvi, zakazchik, id_1c, doc_num_1c_id, code_dad2, " +
                    "code_dad2_src_doc, tel_num, last_answer, last_answer_user_id, last_answer_dt_change, " +
                    "respond, report_id, discount, discount_smeta, vote_score, voter_id, vinovnik_score, " +
                    "vinovnik_score_user_id, vinovnik_score_comment, vinovnik_score_dt, theme_grp_id, " +
                    "theme_id, sum_premiya, sum_penalty, duration, ref_id, summa_zp, budget, complete, " +
                    "sotr_opinion_id, sotr_opinion_author_id, sotr_opinion_dt, no_need_reply, " +
                    "CAST(audioId AS INTEGER) AS audioId, potential_client_id, dt_start_plan, dt_end_plan, " +
                    "dt_start_fact, dt_end_fact, uploadStatus, addr_nm, client_nm, sotr_nm, coord_X, coord_Y " +
                    "FROM tasks_and_reclamations");

            database.execSQL("DROP TABLE tasks_and_reclamations");
            database.execSQL("ALTER TABLE tasks_and_reclamations_new RENAME TO tasks_and_reclamations");

        }
    };

    static final Migration MIGRATION_42_51 = new Migration(42, 51) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `settings_ui` (" +
                    "`id` INTEGER NOT NULL, " +
                    "`context_tag` TEXT, " +
                    "`table_db` TEXT, " +
                    "`settings_json` TEXT, " +
                    "PRIMARY KEY(`id`))");
        }
    };

    static final Migration MIGRATION_51_52 = new Migration(51, 52) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE sotr ADD COLUMN tel_corp INTEGER");
            database.execSQL("ALTER TABLE sotr ADD COLUMN tel2_corp INTEGER");
        }
    };

    static final Migration MIGRATION_52_53 = new Migration(52, 53) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tasks_and_reclamations ADD COLUMN voteDtUpload INTEGER");
        }
    };

    static final Migration MIGRATION_53_54 = new Migration(53, 54) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `dossier_sotr` (" +
                    "`id` INTEGER NOT NULL, " +
                    "`theme_id` INTEGER, " +
                    "`doc_num` TEXT, " +
                    "`controller_id` INTEGER, " +
                    "`exam_id` TEXT, " +
                    "`addr_id` INTEGER, " +
                    "`addr_tp_id` INTEGER, " +
                    "`client_id` TEXT, " +
                    "`staj_duration` INTEGER, " +
                    "`notes` TEXT, " +
                    "`status` INTEGER, " +
                    "`dt` TEXT, " +
                    "`coord_id` INTEGER, " +
                    "`priznak` INTEGER, " +
                    "`dt_change` INTEGER, " +
                    "`doljnost` INTEGER, " +
                    "`option_id` INTEGER, " +
                    "`stajirovka_id` INTEGER, " +
                    "`lesson_id` INTEGER, " +
                    "`license` INTEGER, " +
                    "`menu_template_id` INTEGER, " +
                    "`opinion_id` INTEGER, " +
                    "PRIMARY KEY(`id`))");
        }
    };

    static final Migration MIGRATION_54_55 = new Migration(54, 55) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `vacancy` (" +
                    "`id` INTEGER NOT NULL, " +
                    "`city_id` INTEGER, " +
                    "`district_id` INTEGER, " +
                    "`doljnost_id` INTEGER, " +
                    "`dt_change` INTEGER, " +
                    "`dt_create` TEXT, " +
                    "`occupancy_id` INTEGER, " +
                    "`premium_start` INTEGER, " +
                    "`route_id` INTEGER, " +
                    "`salary` INTEGER, " +
                    "`theme_id` INTEGER, " +
                    "`work_time` TEXT, " +
                    "PRIMARY KEY(`id`))");
        }
    };

    static final Migration MIGRATION_55_56 = new Migration(55, 56) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `bonus` (" +
                    "`id` INTEGER NOT NULL, " +
                    "`author_id` INTEGER, " +
                    "`dt_change` INTEGER, " +
                    "`percent` TEXT, " +
                    "`theme_id` INTEGER, " +
                    "`option_id` INTEGER, " +
                    "`so` INTEGER, " +
                    "PRIMARY KEY(`id`))");
        }
    };

    static final Migration MIGRATION_56_57 = new Migration(56, 57) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `site_url` (" +
                    "`id` INTEGER NOT NULL, " +
                    "`author_id` INTEGER, " +
                    "`comment` TEXT, " +
                    "`country_id` INTEGER, " +
                    "`dt_change` INTEGER, " +
                    "`import` TEXT, " +
                    "`phrase` TEXT, " +
                    "`tovar_grp_id` INTEGER, " +
                    "`url` TEXT, " +
                    "`whitelist` TEXT, " +
                    "PRIMARY KEY(`id`))");

            database.execSQL("CREATE TABLE IF NOT EXISTS `site_account` (" +
                    "`id` INTEGER NOT NULL, " +
                    "`author_id` INTEGER, " +
                    "`buy_sell` TEXT, " +
                    "`dt_change` INTEGER, " +
                    "`import` TEXT, " +
                    "`login` TEXT, " +
                    "`nm` TEXT, " +
                    "`pass` TEXT, " +
                    "`phrase` TEXT, " +
                    "`prefix` TEXT, " +
                    "`site_url_id` INTEGER, " +
                    "PRIMARY KEY(`id`))");
        }
    };

    static final Migration MIGRATION_57_58 = new Migration(57, 58) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `planogramm_type` (" +
                    "`id` INTEGER NOT NULL, " +
                    "`planogram_id` TEXT, " +
                    "`tt_id` TEXT, " +
                    "`author_id` TEXT, " +
                    "`dt_update_ut` TEXT, " +
                    "PRIMARY KEY(`id`))");

        }
    };

    static final Migration MIGRATION_58_59 = new Migration(58, 59) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Добавляем новый столбец в таблицу sotr
            database.execSQL(
                    "ALTER TABLE sotr ADD COLUMN last_ekl_date TEXT"
            );
        }
    };

    static final Migration MIGRATION_59_60 = new Migration(59, 60) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Создание таблицы planogram_vizit_showcase
            database.execSQL("CREATE TABLE IF NOT EXISTS `planogram_vizit_showcase` (" +
                    "`id` INTEGER NOT NULL, " + // Уникальный ИД таблицы БД (автоприращение)
                    "`dt` INTEGER, " + // Время визита в Юникс
                    "`isp` TEXT, " + // Код фирмы (строка)
                    "`client_id` TEXT, " + // Код заказчика (строка)
                    "`addr_id` INTEGER, " + // Код адреса (число)
                    "`code_dad2` INTEGER, " + // Код ДАД2 (число)
                    "`planogram_id` INTEGER, " + // ИД планограммы
                    "`planogram_photo_id` INTEGER, " + // ИД фото планограммы
                    "`showcase_id` INTEGER, " + // ИД витрины
                    "`showcase_photo_id` INTEGER, " + // ИД фото витрины
                    "`photo_do_id` INTEGER, " + // Код фото ДО
                    "`theme_id` INTEGER, " + // Код темы (число)
                    "`option_id` INTEGER, " + // Код опции (число)
                    "`comments` TEXT, " + // Комментарий (строка 200)
                    "`object_a` INTEGER, " + // Код объекта А (число)
                    "`object_a_theme_id` INTEGER, " + // Код темы объекта А (число)
                    "`object_b` INTEGER, " + // Код объекта Б (число)
                    "`object_b_theme_id` INTEGER, " + // Код темы объекта Б (число)
                    "`author_id` INTEGER, " + // Код автора изменений в БДСайта
                    "`dt_update` INTEGER, " + // Время последнего изменения
                    "`kol` INTEGER, " + // Количество (для суммирования при свертке)
                    "PRIMARY KEY(`id`))");
        }
    };

    static final Migration MIGRATION_60_61 = new Migration(60, 61) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 1. Создаем временную таблицу с новой структурой
            database.execSQL("CREATE TABLE IF NOT EXISTS `planogram_vizit_showcase_temp` (" +
                    "`id` INTEGER NOT NULL, " +
                    "`dt` INTEGER, " +
                    "`isp` TEXT, " +
                    "`client_id` TEXT, " +
                    "`addr_id` INTEGER, " +
                    "`code_dad2` INTEGER, " +
                    "`planogram_id` INTEGER, " +
                    "`planogram_photo_id` INTEGER, " +
                    "`showcase_id` INTEGER, " +
                    "`showcase_photo_id` INTEGER, " +
                    "`photo_do_id` INTEGER, " +
                    "`theme_id` INTEGER, " +
                    "`option_id` INTEGER, " +
                    "`comments` TEXT, " +
                    "`object_a` INTEGER, " +
                    "`object_a_theme_id` INTEGER, " +
                    "`object_b` INTEGER, " +
                    "`object_b_theme_id` INTEGER, " +
                    "`author_id` INTEGER, " +
                    "`dt_update` TEXT, " + // Измененный тип
                    "`kol` INTEGER, " +
                    "PRIMARY KEY(`id`))");

            // 2. Копируем данные из старой таблицы, преобразуя dt_update в TEXT
            database.execSQL("INSERT INTO `planogram_vizit_showcase_temp` SELECT " +
                    "id, dt, isp, client_id, addr_id, code_dad2, planogram_id, " +
                    "planogram_photo_id, showcase_id, showcase_photo_id, photo_do_id, " +
                    "theme_id, option_id, comments, object_a, object_a_theme_id, " +
                    "object_b, object_b_theme_id, author_id, " +
                    "CASE WHEN dt_update IS NULL THEN NULL ELSE CAST(dt_update AS TEXT) END, " + // Преобразование INTEGER в TEXT
                    "kol " +
                    "FROM `planogram_vizit_showcase`");

            // 3. Удаляем старую таблицу
            database.execSQL("DROP TABLE `planogram_vizit_showcase`");

            // 4. Переименовываем временную таблицу
            database.execSQL("ALTER TABLE `planogram_vizit_showcase_temp` RENAME TO `planogram_vizit_showcase`");
        }
    };

    static final Migration MIGRATION_61_62 = new Migration(61, 62) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("ALTER TABLE planogram_vizit_showcase ADD COLUMN local_synced INTEGER NOT NULL DEFAULT 0");
//            database.execSQL("ALTER TABLE planogram_vizit_showcase ADD COLUMN local_sync_status INTEGER DEFAULT 0");
            database.execSQL("ALTER TABLE planogram_vizit_showcase ADD COLUMN uploadStatus INTEGER DEFAULT 0");

        }
    };
}
