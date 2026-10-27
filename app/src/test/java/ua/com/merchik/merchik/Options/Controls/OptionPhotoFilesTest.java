package ua.com.merchik.merchik.Options.Controls;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;

import static org.junit.Assert.*;

public class OptionPhotoFilesTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    private StackPhotoDB photo(String path) {
        StackPhotoDB photo = new StackPhotoDB();
        photo.setPhoto_num(path);
        return photo;
    }

    private File finishedFile() throws Exception {
        File file = temporary.newFile();
        Files.write(file.toPath(), new byte[]{1});
        return file;
    }

    @Test public void nullPhotoAndBlankPathsAreRejected() {
        assertFalse(OptionPhotoFiles.isReady(null));
        for (String path : new String[]{null, "", " ", "\t\n"}) {
            assertFalse(OptionPhotoFiles.isReady(photo(path)));
        }
    }

    @Test public void missingFileIsRejected() {
        assertFalse(OptionPhotoFiles.isReady(photo(new File(temporary.getRoot(), "missing.jpg").getPath())));
    }

    @Test public void cameraPlaceholderFromIncidentIsRejected() throws Exception {
        StackPhotoDB placeholder = photo(temporary.newFile("camera.jpg").getPath());
        placeholder.setId(932);
        placeholder.setPhoto_type(31);
        assertFalse(OptionPhotoFiles.isReady(placeholder));
    }

    @Test public void directoryIsNotAPhoto() {
        assertFalse(OptionPhotoFiles.isReady(photo(temporary.getRoot().getPath())));
    }

    @Test public void localFileCountsWithoutUploadOrHash() throws Exception {
        StackPhotoDB local = photo(finishedFile().getPath());
        local.setPhotoServerId("0");
        local.setPhoto_hash("");
        local.setUpload_to_server(0);
        assertTrue(OptionPhotoFiles.isReady(local));
    }

    @Test public void downloadedServerFileCounts() throws Exception {
        StackPhotoDB downloaded = photo(finishedFile().getPath());
        downloaded.setPhotoServerId("59904767");
        downloaded.setUpload_to_server(1);
        assertTrue(OptionPhotoFiles.isReady(downloaded));
    }

    @Test public void serverPhotoCountsWithoutLocalFile() throws Exception {
        for (String path : new String[]{null, "", " ", temporary.newFile().getPath(),
                new File(temporary.getRoot(), "missing.jpg").getPath()}) {
            StackPhotoDB server = photo(path);
            server.setPhotoServerId("59904767");
            server.setUpload_to_server(0);
            assertTrue(OptionPhotoFiles.isReady(server));
        }
    }

    @Test public void invalidServerIdStillRequiresLocalFile() throws Exception {
        String readyPath = finishedFile().getPath();
        for (String id : new String[]{null, "", " ", "0", "00", "-1", "null", "abc", "1.2",
                "9999999999999999999999999999"}) {
            StackPhotoDB row = photo(null);
            row.setPhotoServerId(id);
            assertFalse("serverId=" + id, OptionPhotoFiles.isReady(row));
            row.setPhoto_num(readyPath);
            assertTrue("serverId=" + id, OptionPhotoFiles.isReady(row));
        }
    }

    @Test public void uploadFlagAloneDoesNotBypassFileRequirement() throws Exception {
        for (String path : new String[]{null, temporary.newFile().getPath()}) {
            StackPhotoDB row = photo(path);
            row.setUpload_to_server(1);
            assertFalse(OptionPhotoFiles.isReady(row));
        }
    }

    @Test public void sameRowCountsAfterCameraWritesFile() throws Exception {
        File file = temporary.newFile("pending.jpg");
        StackPhotoDB pending = photo(file.getPath());
        assertFalse(OptionPhotoFiles.isReady(pending));
        Files.write(file.toPath(), new byte[]{1});
        assertTrue(OptionPhotoFiles.isReady(pending));
    }

    @Test public void deletedFileDoesNotStayInCachedResults() throws Exception {
        File file = finishedFile();
        StackPhotoDB row = photo(file.getPath());
        assertTrue(OptionPhotoFiles.isReady(row));
        Files.delete(file.toPath());
        assertFalse(OptionPhotoFiles.isReady(row));
    }

    @Test public void serverPhotoStillCountsAfterLocalFileDeleted() throws Exception {
        File file = finishedFile();
        StackPhotoDB row = photo(file.getPath());
        row.setPhotoServerId("59904767");
        assertTrue(OptionPhotoFiles.isReady(row));
        Files.delete(file.toPath());
        assertTrue(OptionPhotoFiles.isReady(row));
    }

    @Test public void filtersWithoutMutatingInputOrPhotoMetadata() throws Exception {
        StackPhotoDB incomplete = photo(temporary.newFile().getPath());
        StackPhotoDB local = photo(finishedFile().getPath());
        StackPhotoDB server = photo(null);
        server.setPhotoServerId("59904767");
        local.setExample_id("78");
        local.setComment("Warehouse is empty");
        List<StackPhotoDB> input = Arrays.asList(incomplete, local, null, server);
        List<StackPhotoDB> ready = OptionPhotoFiles.ready(input);
        assertEquals(4, input.size());
        assertEquals(2, ready.size());
        assertSame(local, ready.get(0));
        assertSame(server, ready.get(1));
        assertEquals("78", local.getExample_id());
        assertEquals("Warehouse is empty", local.getComment());
    }

    @Test public void incompleteWarehouseExceptionsDoNotCount() throws Exception {
        StackPhotoDB placeholder78 = photo(temporary.newFile().getPath());
        StackPhotoDB placeholder94 = photo(temporary.newFile().getPath());
        placeholder78.setExample_id("78");
        placeholder94.setExample_id("94");
        StackPhotoDB ready78 = photo(finishedFile().getPath());
        ready78.setExample_id("78");
        List<StackPhotoDB> ready = OptionPhotoFiles.ready(Arrays.asList(placeholder78, placeholder94, ready78));
        assertEquals(1, ready.size());
        assertSame(ready78, ready.get(0));
    }

    @Test public void nullListIsEmpty() {
        assertTrue(OptionPhotoFiles.ready(null).isEmpty());
    }
}
