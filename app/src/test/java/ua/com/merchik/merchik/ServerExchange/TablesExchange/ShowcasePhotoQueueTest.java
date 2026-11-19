package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import static org.junit.Assert.*;

public class ShowcasePhotoQueueTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();

    @Test public void numericAndStringIdsMatchAndDownloadedFileIsExcluded() throws Exception {
        File file = temp.newFile();
        Files.write(file.toPath(), new byte[]{1});
        assertTrue(ShowcaseExchange.selectPhotosToDownload(Collections.singletonList(showcase(123)),
                Collections.singletonList(cached("123", file.getAbsolutePath()))).isEmpty());
        assertTrue(ShowcaseExchange.selectPhotosToDownload(Collections.singletonList(showcase(123)),
                Collections.singletonList(cached(" 00123 ", file.getAbsolutePath()))).isEmpty());
    }

    @Test public void cachedMetadataWithoutFileStillDownloads() {
        ShowcaseSDB row = showcase(123);
        for (String path : new String[]{null, "", " ", new File(temp.getRoot(), "missing.jpg").getAbsolutePath()}) {
            assertEquals(Collections.singletonList(row), ShowcaseExchange.selectPhotosToDownload(
                    Collections.singletonList(row), Collections.singletonList(cached("123", path))));
        }
    }

    @Test public void emptyFileStillDownloads() throws Exception {
        ShowcaseSDB row = showcase(123);
        assertEquals(Collections.singletonList(row), ShowcaseExchange.selectPhotosToDownload(Collections.singletonList(row),
                Collections.singletonList(cached("123", temp.newFile().getAbsolutePath()))));
    }

    @Test public void missingPhotoIdOrUrlIsNeverQueued() {
        for (String url : new String[]{null, "", " ", "0", "null"}) {
            ShowcaseSDB row = showcase(123);
            row.photoBig = url;
            assertTrue(ShowcaseExchange.selectPhotosToDownload(Collections.singletonList(row), Collections.emptyList()).isEmpty());
        }
        assertTrue(ShowcaseExchange.selectPhotosToDownload(Arrays.asList(null, showcase(null), showcase(0), showcase(-1)),
                Collections.emptyList()).isEmpty());
    }

    @Test public void samePhotoInSeveralShowcasesDownloadsOnce() {
        ShowcaseSDB first = showcase(123);
        assertEquals(Collections.singletonList(first), ShowcaseExchange.selectPhotosToDownload(
                Arrays.asList(first, showcase(123)), Collections.emptyList()));
    }

    @Test public void invalidCachedIdsCannotBreakQueue() throws Exception {
        File file = temp.newFile();
        Files.write(file.toPath(), new byte[]{1});
        ShowcaseSDB row = showcase(123);
        for (String id : new String[]{null, "", "bad", "0", "-1", "9223372036854775807"}) {
            assertEquals(Collections.singletonList(row), ShowcaseExchange.selectPhotosToDownload(Collections.singletonList(row),
                    Collections.singletonList(cached(id, file.getAbsolutePath()))));
        }
    }

    private static ShowcaseSDB showcase(Integer photoId) {
        ShowcaseSDB result = new ShowcaseSDB();
        result.photoId = photoId;
        result.photoBig = "https://example.invalid/photo.jpg";
        return result;
    }

    private static StackPhotoDB cached(String serverId, String path) {
        StackPhotoDB result = new StackPhotoDB();
        result.setPhotoServerId(serverId);
        result.setPhoto_num(path);
        return result;
    }
}
