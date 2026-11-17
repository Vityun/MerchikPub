package ua.com.merchik.merchik.features.main.componentsUI

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TovarPhotoDialogTest {
    private fun state() = TovarPhotoDialogUiState(
        tovarId = "123",
        photoId = "456",
        imagePath = "preview.jpg",
        imageQuality = TovarPhotoQuality.PREVIEW,
        barcode = "0012345678905",
        article = "00042",
        isLoadingFull = true,
        tradeMark = "Наш Сік",
        name = "Сік яблучний"
    )

    @Test
    fun renderedBarcodeKeepsOriginalFieldsAndLeadingZeros() {
        assertEquals("Штрихкод: 0012345678905\nАртикул: 00042", buildTovarPhotoDetails(state(), true))
    }

    @Test
    fun failedBarcodeShowsTradeMarkAndNameInstead() {
        val details = buildTovarPhotoDetails(state(), false)
        assertEquals("Наш Сік\nСік яблучний", details)
        assertFalse(details.contains("0012345678905"))
        assertFalse(details.contains("00042"))
    }

    @Test
    fun emptyBarcodeStillShowsProductDetails() {
        assertEquals(
            "Наш Сік\nСік яблучний",
            buildTovarPhotoDetails(state().copy(barcode = ""), false)
        )
    }

    @Test
    fun missingDetailsHaveReadableFallback() {
        assertEquals(
            "Не вказано\nНе вказано",
            buildTovarPhotoDetails(state().copy(tradeMark = "  ", name = ""), false)
        )
    }

    @Test
    fun detailValuesAreTrimmed() {
        assertEquals(
            "Наш Сік\nСік яблучний",
            buildTovarPhotoDetails(state().copy(tradeMark = " Наш Сік ", name = " Сік яблучний "), false)
        )
    }

    @Test
    fun fullPhotoUpdateKeepsFallbackDetails() {
        val preview = state()
        val full = preview.copy(
            imagePath = "full.jpg",
            imageQuality = TovarPhotoQuality.FULL,
            isLoadingFull = false,
            imageVersion = preview.imageVersion + 1
        )
        assertEquals(buildTovarPhotoDetails(preview, false), buildTovarPhotoDetails(full, false))
        assertEquals(preview.loadToken, full.loadToken)
    }
}
