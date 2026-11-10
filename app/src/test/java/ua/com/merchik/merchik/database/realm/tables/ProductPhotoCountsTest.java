package ua.com.merchik.merchik.database.realm.tables;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProductPhotoCountsTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void groupsReadyPhotosByProduct() throws Exception {
        File local = temporary.newFile("local.jpg");
        Files.write(local.toPath(), new byte[]{1});
        Map<String, Integer> counts = StackPhotoRealm.countReadyProductPhotos(Arrays.asList(
                photo("175194", null, local.getPath()),
                photo("175194", "59663980", null),
                photo("137427", "59663981", null)));
        assertEquals(2, counts.size());
        assertEquals(Integer.valueOf(2), counts.get("175194"));
        assertEquals(Integer.valueOf(1), counts.get("137427"));
    }

    @Test public void doesNotCountCancelledOrUnfinishedCaptures() throws Exception {
        File empty = temporary.newFile("camera.jpg");
        assertTrue(StackPhotoRealm.countReadyProductPhotos(Arrays.asList(
                photo("175194", "0", empty.getPath()),
                photo("175194", null, null),
                photo("175194", "0", new File(temporary.getRoot(), "missing.jpg").getPath())
        )).isEmpty());
    }

    @Test public void recountSeesSavedFileWithoutAnyReportPrepareChanges() throws Exception {
        File local = temporary.newFile("camera.jpg");
        StackPhotoDB row = photo("175194", null, local.getPath());
        assertTrue(StackPhotoRealm.countReadyProductPhotos(Collections.singletonList(row)).isEmpty());
        Files.write(local.toPath(), new byte[]{1});
        assertEquals(Integer.valueOf(1), StackPhotoRealm.countReadyProductPhotos(
                Collections.singletonList(row)).get("175194"));
        assertEquals(Integer.valueOf(1), StackPhotoRealm.countReadyProductPhotos(
                Collections.singletonList(row)).get("175194"));
        assertTrue(StackPhotoRealm.countReadyProductPhotos(Collections.emptyList()).isEmpty());
    }

    @Test public void serverPhotoDoesNotNeedDownloadedFile() {
        assertEquals(Integer.valueOf(1), StackPhotoRealm.countReadyProductPhotos(
                Collections.singletonList(photo("175194", "59663980", null))).get("175194"));
    }

    @Test public void doesNotCountPhotosWithoutProduct() {
        assertTrue(StackPhotoRealm.countReadyProductPhotos(Arrays.asList(
                photo(null, "59663980", null), photo("", "59663981", null),
                photo(" ", "59663982", null), photo("0", "59663983", null)
        )).isEmpty());
    }

    private static StackPhotoDB photo(String productId, String serverId, String path) {
        StackPhotoDB photo = new StackPhotoDB();
        photo.setTovar_id(productId);
        photo.setPhotoServerId(serverId);
        photo.setPhoto_num(path);
        return photo;
    }
}
