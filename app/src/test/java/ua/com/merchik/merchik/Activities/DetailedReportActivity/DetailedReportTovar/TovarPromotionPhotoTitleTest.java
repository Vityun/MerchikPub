package ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar;

import org.junit.Test;

import ua.com.merchik.merchik.data.Database.Room.ImagesTypeListSDB;

import static org.junit.Assert.assertEquals;

public class TovarPromotionPhotoTitleTest {
    @Test public void prefersCatalogName() {
        ImagesTypeListSDB type = new ImagesTypeListSDB();
        type.id = 28;
        type.nm = "  Акційний цінник  ";
        assertEquals("Акційний цінник", TovarRequisites.promotionPhotoTitle(type));
    }

    @Test public void fallsBackOnlyForMissingRowOrName() {
        String fallback = "Фото Акцiйного товара з цiнником";
        assertEquals(fallback, TovarRequisites.promotionPhotoTitle(null));
        ImagesTypeListSDB type = new ImagesTypeListSDB();
        for (String name : new String[]{null, "", "  "}) {
            type.nm = name;
            assertEquals(fallback, TovarRequisites.promotionPhotoTitle(type));
        }
    }
}
