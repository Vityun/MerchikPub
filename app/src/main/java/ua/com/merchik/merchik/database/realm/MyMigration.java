package ua.com.merchik.merchik.database.realm;

import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

// Example migration adding a new class
public class MyMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        // DynamicRealm exposes an editable schema
        RealmSchema schema = realm.getSchema();

        Log.e("MyMigration", "oldVersion: " + oldVersion);
        Log.e("MyMigration", "newVersion: " + newVersion);


        if (oldVersion == 7){
            schema.get("StackPhotoDB")
                    .addField("dt", Long.class)
                    .addField("approve", Integer.class);
            oldVersion++;
        }

        if (oldVersion == 8){
            schema.get("WpDataDB")
                    .addField("startUpdate", boolean.class);
            oldVersion++;
        }

        if(oldVersion == 9){
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

        if(oldVersion == 10){
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

        if(oldVersion == 11){
            RealmObjectSchema wpDataSchema = schema.get("WpDataDB");
            wpDataSchema.addField("wp_duration", long.class)
                    .transform(obj -> obj.setLong("wp_duration", obj.getInt("duration")))
                    .removeField("duration")
                    .renameField("wp_duration", "duration");

            oldVersion++;
        }

        if (oldVersion == 12){
            RealmObjectSchema schemaRP = schema.get("ReportPrepareDB");

            schemaRP.addField("facesPlan", Integer.class);

            oldVersion++;
        }

        if (oldVersion == 13){
            RealmObjectSchema schemaWp = schema.get("WpDataDB");

            schemaWp.addField("ptt_user_id", Integer.class);
            schemaWp.addField("sku_plan", Double.class);
            schemaWp.addField("sku_fact", Double.class);
            schemaWp.addField("oos", Double.class);

            oldVersion++;
        }

        if (oldVersion == 0 || oldVersion == 15){
            RealmObjectSchema schemaAR = schema.get("AdditionalRequirementsMarkDB");

            schemaAR.addField("comment", String.class);

            oldVersion=newVersion;
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