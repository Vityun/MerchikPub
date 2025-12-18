package ua.com.merchik.merchik.database.realm;

import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import ua.com.merchik.merchik.Globals;

// Example migration adding a new class
public class MyMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        // DynamicRealm exposes an editable schema
        RealmSchema schema = realm.getSchema();

        Log.e("MyMigration", "oldVersion: " + oldVersion);
        Log.e("MyMigration", "newVersion: " + newVersion);


        if (oldVersion == 7) {
            schema.get("StackPhotoDB")
                    .addField("dt", Long.class)
                    .addField("approve", Integer.class);
            oldVersion++;
        }

        if (oldVersion == 8) {
            schema.get("WpDataDB")
                    .addField("startUpdate", boolean.class);
            oldVersion++;
        }

        if (oldVersion == 9) {
            RealmObjectSchema wpDataSchema = schema.get("WpDataDB");
            wpDataSchema.addField("wp_id", long.class)
                    .transform(obj -> obj.setLong("wp_id", obj.getInt("ID")))
                    .removeField("ID")
                    .renameField("wp_id", "ID")
                    .addPrimaryKey("ID");

            RealmObjectSchema reportSchema = schema.get("ReportPrepareDB");
            reportSchema.addField("rp_id", long.class)
                    .transform(obj -> obj.setLong("rp_id", obj.getInt("iD")))
                    .removeField("iD")
                    .renameField("rp_id", "iD")
                    .addPrimaryKey("iD");
            oldVersion++;
        }

        if (oldVersion == 10) {
            RealmObjectSchema wpDataSchema = schema.get("WpDataDB");
            wpDataSchema.addField("wp_id", long.class)
                    .transform(obj -> obj.setLong("wp_id", obj.getInt("ID")))
                    .removeField("ID")
                    .renameField("wp_id", "ID")
                    .addPrimaryKey("ID");

            RealmObjectSchema reportSchema = schema.get("ReportPrepareDB");
            reportSchema.addField("rp_id", Long.class)
                    .transform(obj -> obj.setLong("rp_id", obj.getInt("iD")))
                    .removeField("iD")
                    .renameField("rp_id", "iD")
                    .addPrimaryKey("iD");
            oldVersion++;
        }

        if (oldVersion == 11) {
            RealmObjectSchema wpDataSchema = schema.get("WpDataDB");
            wpDataSchema.addField("wp_duration", long.class)
                    .transform(obj -> obj.setLong("wp_duration", obj.getInt("duration")))
                    .removeField("duration")
                    .renameField("wp_duration", "duration");

            oldVersion++;
        }

        if (oldVersion == 12) {
            RealmObjectSchema schemaRP = schema.get("ReportPrepareDB");

            schemaRP.addField("facesPlan", Integer.class);

            oldVersion++;
        }

        if (oldVersion == 13) {
            RealmObjectSchema schemaWp = schema.get("WpDataDB");

            schemaWp.addField("ptt_user_id", Integer.class);
            schemaWp.addField("sku_plan", Double.class);
            schemaWp.addField("sku_fact", Double.class);
            schemaWp.addField("oos", Double.class);

            oldVersion++;
        }

//        if (oldVersion == 0 || oldVersion == 15){
//            RealmObjectSchema schemaAR = schema.get("AdditionalRequirementsMarkDB");
//
//            schemaAR.addField("comment", String.class);
//
//            oldVersion=newVersion;
//        }

        if (oldVersion == 15 || oldVersion == 16) {
            RealmObjectSchema wpDataDB = schema.get("WpDataDB");

            wpDataDB.addField("cash_fact", Double.class);
            wpDataDB.addField("cash_penalty", Double.class);

            oldVersion = newVersion;
        }

        if (oldVersion == 10 || oldVersion == 11 || oldVersion == 12 || oldVersion == 13 || oldVersion == 14 || oldVersion == 15 || oldVersion == 16) {
            RealmObjectSchema schemaTovar = schema.get("TovarDB");

            Globals.writeToMLOG("ERROR", "MyMigration/old_to_17", "oldVersion: " + oldVersion + " /newVersion: " + newVersion);

            if (schemaTovar != null) {
                schemaTovar.addField("height", Double.class);
                schemaTovar.addField("width", Double.class);
                schemaTovar.addField("depth", Double.class);
            } else {
                Globals.writeToMLOG("ERROR", "MyMigration/old_to_17", "schemaTovar is null");
            }

            oldVersion++;

            /**/
        }


        if (oldVersion == 11 || oldVersion == 12 || oldVersion == 13 || oldVersion == 14 || oldVersion == 15 || oldVersion == 16 || oldVersion == 17) {
            RealmObjectSchema schemaStackPhoto18 = schema.get("StackPhotoDB");

            Globals.writeToMLOG("ERROR", "MyMigration/old_to_18", "oldVersion: " + oldVersion + " /newVersion: " + newVersion);

            if (schemaStackPhoto18 != null) {
                schemaStackPhoto18.addField("tovar_id", String.class);
            } else {
                Globals.writeToMLOG("ERROR", "MyMigration/old_to_18", "schemaTovar is null");
            }

            oldVersion++;
        }


        if (oldVersion >= 10 && oldVersion <= 18) {
            RealmObjectSchema reportPrepareDBSchema = schema.get("ReportPrepareDB");

            if (reportPrepareDBSchema != null) {
                // Добавляем новый столбец akciya_plan_id
                reportPrepareDBSchema.addField("akciya_plan_id", String.class);
            } else {
                Globals.writeToMLOG("ERROR", "MyMigration/migrate", "ReportPrepareDB schema is null");
            }

            oldVersion++;
        }

        if (oldVersion == 19) {
            RealmObjectSchema wpDataSchema = schema.get("WpDataDB");

            // Добавляем новые поля
            if (wpDataSchema != null) {
                wpDataSchema
                        .addField("user_opinion_id", String.class)
                        .addField("user_opinion_author_id", String.class)
                        .addField("user_opinion_dt_update", long.class);
            } else {
                Globals.writeToMLOG("ERROR", "MyMigration/migrate", "WpDataDB schema is null");
            }

            oldVersion++; // Увеличиваем версию схемы
        }

        if (oldVersion == 20) {
            RealmObjectSchema wpDataSchema = schema.get("WpDataDB");

            // Добавляем новые поля
            if (wpDataSchema != null) {
                wpDataSchema
                        .addField("main_option_id", String.class);
            } else {
                Globals.writeToMLOG("ERROR", "MyMigration/migrate", "WpDataDB schema is null");
            }
            oldVersion++; // Увеличиваем версию схемы
        }

        if (oldVersion == 21) {
            RealmObjectSchema wpDataSchema = schema.get("WpDataDB");

            // Добавляем новые поля
            if (wpDataSchema != null) {
                wpDataSchema
                        .addField("controller_opinion_id", String.class)
                        .addField("controller_opinion_author_id", String.class);
            } else {
                Globals.writeToMLOG("ERROR", "MyMigration/migrate", "WpDataDB schema is null");
            }
            oldVersion++; // Увеличиваем версию схемы
        }

        if (oldVersion == 22) {
            RealmObjectSchema additionalRequirementsDB = schema.get("AdditionalRequirementsDB");

            // Добавляем новые поля
            if (additionalRequirementsDB != null) {
                additionalRequirementsDB
                        .addField("main_option_id", String.class);
            } else {
                Globals.writeToMLOG("ERROR", "MyMigration/migrate", "WpDataDB schema is null");
            }
            oldVersion++; // Увеличиваем версию схемы
        }

    }
}


// EXAMPLE
/*
        // Migrate to VersionApp 1: Add a new class.
        // Example:
        // public Person extends RealmObject {
        //     private String name;
        //     private int age;
        //     // getters and setters left out for brevity
        // }
        if (oldVersion == 0) {
            schema.create("Person")
                    .addField("name", String.class)
                    .addField("age", int.class);
            oldVersion++;
        }

        // Migrate to VersionApp 2: Add a primary key + object references
        // Example:
        // public Person extends RealmObject {
        //     private String name;
        //     private int age;
        //     @PrimaryKey
        //     private int id;
        //     private Dog favoriteDog;
        //     private RealmList<Dog> dogs;
        //     // getters and setters left out for brevity
        // }
        if (oldVersion == 1) {
            schema.get("Person")
                    .addField("id", long.class, FieldAttribute.PRIMARY_KEY)
                    .addRealmObjectField("favoriteDog", schema.get("Dog"))
                    .addRealmListField("dogs", schema.get("Dog"));
            oldVersion++;
        }
*/