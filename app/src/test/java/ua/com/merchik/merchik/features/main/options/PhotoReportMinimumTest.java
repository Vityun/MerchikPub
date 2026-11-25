package ua.com.merchik.merchik.features.main.options;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

import static org.junit.Assert.*;

public class PhotoReportMinimumTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void counterAndCaptureResolveTheSameMinimum() {
        WpDataDB visit = new WpDataDB();
        for (int sku : new int[]{0, 4, 5, 100}) {
            visit.setSku(sku);
            for (String optionId : new String[]{"158309", "158308", "135809", "164355", "132969",
                    "169108", "174213", "158604", "158605", "172100"}) {
                for (String minimum : new String[]{null, "", "0", "1", "3", "5"}) {
                    OptionsDB option = option(optionId, minimum);
                    assertEquals("option=" + optionId + ", sku=" + sku + ", minimum=" + minimum,
                            PhotoReportMinimum.requiredCount(visit, 10, option, null),
                            PhotoReportMinimum.requiredCount(visit, 10, option, null, true));
                }
            }
        }
    }

    @Test public void configuredWarehouseMinimumIsNotReplacedByDefault() {
        WpDataDB visit = new WpDataDB();
        visit.setSku(100);
        assertEquals(5, PhotoReportMinimum.requiredCount(visit, 10, option("141360", "5"),
                null, false, Collections.emptyList()));
    }

    @Test public void bothWarehouseSamplesOverrideDefaultAndConfiguredMinimum() {
        for (String sample : new String[]{"78", "94"}) {
            for (String minimum : new String[]{null, "", "0", "1", "3", "5"}) {
                for (boolean capture : new boolean[]{false, true}) {
                    assertEquals("sample=" + sample + ", minimum=" + minimum, 2,
                            warehouseMinimum(minimum, Collections.singletonList(serverPhoto(sample)), capture));
                }
            }
        }
    }

    @Test public void removingLastExceptionPhotoRestoresConfiguredRequirement() {
        List<StackPhotoDB> photos = new ArrayList<>();
        assertEquals(5, warehouseMinimum("5", photos, false));
        photos.add(serverPhoto("94"));
        assertEquals(2, warehouseMinimum("5", photos, false));
        photos.clear();
        assertEquals(5, warehouseMinimum("5", photos, false));
    }

    @Test public void ordinaryWarehousePhotoDoesNotActivateException() {
        assertEquals(1, warehouseMinimum("0", Collections.singletonList(serverPhoto("34")), false));
        assertEquals(5, warehouseMinimum("5", Collections.singletonList(serverPhoto("34")), false));
    }

    @Test public void placeholderDoesNotActivateExceptionUntilPhotoIsSaved() throws Exception {
        File image = temporary.newFile("warehouse.jpg");
        StackPhotoDB photo = serverPhoto("94");
        photo.setPhotoServerId("0");
        photo.setPhoto_num(image.getPath());
        List<StackPhotoDB> photos = Collections.singletonList(photo);
        assertEquals(1, warehouseMinimum("1", photos, false));
        assertEquals(0, PhotoReportMinimum.completedCount(option("141360", "1"), photos));
        Files.write(image.toPath(), new byte[]{1, 2});
        assertEquals(2, warehouseMinimum("1", photos, false));
        assertEquals(1, PhotoReportMinimum.completedCount(option("141360", "1"), photos));
    }

    @Test public void wrongPhotoTypeAndNullRowsDoNotActivateWarehouseException() {
        StackPhotoDB otherType = serverPhoto("78");
        otherType.setPhoto_type(39);
        StackPhotoDB missingType = serverPhoto("94");
        missingType.setPhoto_type(null);
        assertEquals(1, warehouseMinimum("1", Arrays.asList(null, otherType, missingType), false));
    }

    @Test public void mixedWarehouseSamplesCountTogether() {
        List<StackPhotoDB> photos = Arrays.asList(serverPhoto("78"), serverPhoto("94"));
        assertEquals(2, PhotoReportMinimum.emptyWarehousePhotoCount(photos));
        assertEquals(2, PhotoReportMinimum.completedCount(option("141360", "1"), photos));
        assertEquals(2, warehouseMinimum("1", photos, false));
    }

    @Test public void ordinaryPhotoCannotReplaceSecondEmptyWarehousePhotoOrCloseCamera() {
        List<StackPhotoDB> photos = new ArrayList<>(Arrays.asList(serverPhoto("34"), serverPhoto("94")));
        OptionsDB option = option("141360", "1");
        int completed = PhotoReportMinimum.completedCount(option, photos);
        int minimum = warehouseMinimum("1", photos, true);
        assertEquals("1/2", completed + "/" + minimum);
        assertFalse(PhotoReportCountPolicy.shouldClose(true, 10, 10, 31, 31, completed, minimum));
        photos.add(serverPhoto("78"));
        completed = PhotoReportMinimum.completedCount(option, photos);
        assertEquals("2/2", completed + "/" + minimum);
        assertTrue(PhotoReportCountPolicy.shouldClose(true, 10, 10, 31, 31, completed, minimum));
    }

    @Test public void photoCommentsDoNotChangeTheNumericCounter() {
        StackPhotoDB photo = serverPhoto("94");
        List<StackPhotoDB> photos = Collections.singletonList(photo);
        assertEquals(1, PhotoReportMinimum.completedCount(option("141360", "1"), photos));
        photo.setComment("Warehouse checked and goods not found");
        assertEquals(1, PhotoReportMinimum.completedCount(option("141360", "1"), photos));
        assertEquals(2, warehouseMinimum("1", photos, false));
    }

    @Test public void warehouseSamplesDoNotChangeOtherOptions() {
        WpDataDB visit = new WpDataDB();
        visit.setSku(100);
        List<StackPhotoDB> photos = Arrays.asList(serverPhoto("94"), serverPhoto("34"));
        assertEquals(4, PhotoReportMinimum.requiredCount(visit, 10, option("158309", "3"),
                null, false, photos));
        assertEquals(2, PhotoReportMinimum.completedCount(option("158309", "3"), photos));
    }

    @Test public void standaloneWarehouseControlUsesTheSameException() {
        assertEquals(2, PhotoReportMinimum.requiredCount(new WpDataDB(), 10, option("141361", "5"),
                null, false, Collections.singletonList(serverPhoto("94"))));
    }

    private int warehouseMinimum(String minimum, List<StackPhotoDB> photos, boolean capture) {
        WpDataDB visit = new WpDataDB();
        visit.setSku(100);
        return PhotoReportMinimum.requiredCount(visit, 10, option("141360", minimum), null, capture, photos);
    }

    private StackPhotoDB serverPhoto(String sample) {
        StackPhotoDB photo = new StackPhotoDB();
        photo.setPhoto_type(31);
        photo.setCode_dad2(10);
        photo.setExample_id(sample);
        photo.setPhotoServerId("123");
        return photo;
    }

    @Test public void exactMinimumDoesNotRequireAnyDatabaseOrOption() {
        assertEquals(4, PhotoReportMinimum.requiredCount(null, 0, null, 4));
        assertEquals(0, PhotoReportMinimum.requiredCount(null, 0, null, -1));
    }

    @Test(expected = NumberFormatException.class)
    public void invalidMinimumIsNotTreatedAsCompleted() {
        PhotoReportMinimum.requiredCount(new WpDataDB(), 10, option("158309", "invalid"), null, true);
    }

    private OptionsDB option(String id, String minimum) {
        OptionsDB option = new OptionsDB();
        option.setOptionId(id);
        option.setOptionControlId("158608");
        option.setAmountMin(minimum);
        return option;
    }
}
