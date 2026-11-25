package ua.com.merchik.merchik.database.realm.tables;

import androidx.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.Locale;
import java.util.UUID;
import java.io.File;
import java.io.FileOutputStream;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;

import static org.junit.Assert.*;

public class GalleryPhotoDuplicateTest {
    private static final String HASH = "ef87c334c49001cbd60f9fe3f00f78a0";
    private Realm db;
    @Rule public TemporaryFolder files = new TemporaryFolder(InstrumentationRegistry.getTargetContext().getCacheDir());

    @Before public void open() {
        Realm.init(InstrumentationRegistry.getTargetContext());
        db = Realm.getInstance(new RealmConfiguration.Builder()
                .name("gallery-" + UUID.randomUUID()).inMemory().build());
    }

    @After public void close() {
        db.close();
    }

    @Test public void duplicateDoesNotCountOrReplaceTheOriginalBeforeOrAfterUpload() throws Exception {
        StackPhotoDB original = photo(123, HASH);
        File file = files.newFile("first.jpg");
        try (FileOutputStream out = new FileOutputStream(file)) { out.write(new byte[]{1, 2}); }
        original.setPhoto_num(file.getAbsolutePath());
        original.setPhoto_type(4);
        assertTrue(StackPhotoRealm.insertGalleryPhotoIfNew(db, original));

        StackPhotoDB duplicate = photo(123, HASH.toUpperCase(Locale.ROOT));
        duplicate.setPhoto_num("renamed.jpg");
        duplicate.setPhoto_type(31);
        assertFalse(StackPhotoRealm.insertGalleryPhotoIfNew(db, duplicate));
        StackPhotoDB stored = db.where(StackPhotoDB.class).findFirst();
        db.executeTransaction(r -> stored.setPhotoServerId("60200698"));
        assertFalse(StackPhotoRealm.insertGalleryPhotoIfNew(db, duplicate));
        assertEquals(1, db.where(StackPhotoDB.class).count());
        assertEquals(0, duplicate.getId());
        assertEquals(file.getAbsolutePath(), stored.getPhoto_num());
        assertEquals("60200698", stored.getPhotoServerId());
    }

    @Test public void differentPhotoOrDifferentVisitIsAccepted() {
        assertTrue(StackPhotoRealm.insertGalleryPhotoIfNew(db, photo(123, HASH)));
        assertTrue(StackPhotoRealm.insertGalleryPhotoIfNew(db, photo(124, HASH)));
        assertTrue(StackPhotoRealm.insertGalleryPhotoIfNew(db, photo(123, "900150983cd24fb0d6963f7d28e17f72")));
        assertEquals(3, db.where(StackPhotoDB.class).count());
    }

    private static StackPhotoDB photo(long visit, String hash) {
        StackPhotoDB photo = new StackPhotoDB();
        photo.code_dad2 = visit;
        photo.setPhoto_hash(hash);
        return photo;
    }
}
