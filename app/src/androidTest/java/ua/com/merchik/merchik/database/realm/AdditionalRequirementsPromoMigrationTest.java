package ua.com.merchik.merchik.database.realm;

import androidx.test.InstrumentationRegistry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.RealmObjectSchema;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.retrofit.FlexibleIntegerTypeAdapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class AdditionalRequirementsPromoMigrationTest {
    private RealmConfiguration legacyConfig;
    private RealmConfiguration currentConfig;

    @Before
    public void setUp() {
        Realm.init(InstrumentationRegistry.getTargetContext());
        String name = "additional-requirements-promo-" + UUID.randomUUID() + ".realm";
        legacyConfig = new RealmConfiguration.Builder()
                .name(name)
                .schemaVersion(31)
                .build();
        currentConfig = new RealmConfiguration.Builder()
                .name(name)
                .schemaVersion(32)
                .migration(new MyMigration())
                .build();
    }

    @After
    public void tearDown() {
        Realm.deleteRealm(currentConfig);
    }

    @Test
    public void migrationAddsZeroPromotionTypeAndPreservesExistingData() {
        try (Realm realm = Realm.getInstance(legacyConfig)) {
            realm.executeTransaction(r -> {
                AdditionalRequirementsDB requirement = r.createObject(AdditionalRequirementsDB.class);
                requirement.setId(63258);
                requirement.setOptionId("175121");
                requirement.setNotes("original requirement");

                ReportPrepareDB report = r.createObject(ReportPrepareDB.class, 1L);
                report.setPrice("25");
                report.setNotes("unsent report");
                report.setUploadStatus(1);
            });
        }
        // Recreate the version-31 schema while retaining records in all other tables.
        try (DynamicRealm realm = DynamicRealm.getInstance(legacyConfig)) {
            realm.executeTransaction(r -> r.getSchema().get("AdditionalRequirementsDB")
                    .removeField("promoTpId"));
        }

        try (Realm realm = Realm.getInstance(currentConfig)) {
            assertEquals(32L, realm.getVersion());
            RealmObjectSchema schema = realm.getSchema().get("AdditionalRequirementsDB");
            assertNotNull(schema);
            assertEquals(RealmFieldType.INTEGER, schema.getFieldType("promoTpId"));
            assertFalse(schema.isNullable("promoTpId"));

            AdditionalRequirementsDB requirement = realm.where(AdditionalRequirementsDB.class)
                    .equalTo("id", 63258).findFirst();
            assertNotNull(requirement);
            assertEquals(0, requirement.getPromoTpId());
            assertEquals("175121", requirement.getOptionId());
            assertEquals("original requirement", requirement.getNotes());
            assertEquals(1L, realm.where(AdditionalRequirementsDB.class).count());

            ReportPrepareDB report = realm.where(ReportPrepareDB.class).equalTo("iD", 1L).findFirst();
            assertNotNull(report);
            assertEquals("25", report.getPrice());
            assertEquals("unsent report", report.getNotes());
            assertEquals(1, report.getUploadStatus());
        }
    }

    @Test
    public void serverPromotionTypeIsParsedAndPersisted() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(int.class, new FlexibleIntegerTypeAdapter(0))
                .create();
        String[] payloads = {
                "{\"promo_tp_id\":\"357\"}",
                "{\"promo_tp_id\":357}",
                "{\"promo_tp_id\":\"0\"}",
                "{}",
                "{\"promo_tp_id\":null}",
                "{\"promo_tp_id\":\"\"}"
        };
        int[] expected = {357, 357, 0, 0, 0, 0};

        try (Realm realm = Realm.getInstance(currentConfig)) {
            for (int i = 0; i < payloads.length; i++) {
                AdditionalRequirementsDB requirement = gson.fromJson(payloads[i], AdditionalRequirementsDB.class);
                requirement.setId(i + 1);
                assertEquals(expected[i], requirement.getPromoTpId());
                realm.executeTransaction(r -> r.copyToRealm(requirement));
            }
        }

        try (Realm realm = Realm.getInstance(currentConfig)) {
            for (int i = 0; i < expected.length; i++) {
                AdditionalRequirementsDB saved = realm.where(AdditionalRequirementsDB.class)
                        .equalTo("id", i + 1).findFirst();
                assertNotNull(saved);
                assertEquals(expected[i], saved.getPromoTpId());
                assertEquals(expected[i], gson.toJsonTree(realm.copyFromRealm(saved))
                        .getAsJsonObject().get("promo_tp_id").getAsInt());
            }
        }
    }
}
