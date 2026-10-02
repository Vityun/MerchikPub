package ua.com.merchik.merchik.features.main.options

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SamplePhotoCaptureResultTest {
    @Test
    fun correctionKeepsItsOwnFeedbackKind() {
        SamplePhotoCaptureResult.publishCorrection(101L, "option-row-1")

        assertEquals(
            SamplePhotoCaptureResult.Result(101L, "option-row-1", SamplePhotoCaptureResult.Kind.CORRECTION),
            SamplePhotoCaptureResult.takeForVisit(101L)
        )
        assertNull(SamplePhotoCaptureResult.takeForVisit(101L))
    }

    @Test
    fun newCaptureDoesNotInheritCorrectionMode() {
        SamplePhotoCaptureResult.publishCorrection(101L, "option-row-1")
        SamplePhotoCaptureResult.publish(101L, "option-row-1")

        assertEquals(SamplePhotoCaptureResult.Kind.CAPTURE, SamplePhotoCaptureResult.takeForVisit(101L)?.kind)
    }

    @Test
    fun resultIsConsumedOnlyOnce() {
        SamplePhotoCaptureResult.publish(101L, "option-row-1")

        assertEquals(
            SamplePhotoCaptureResult.Result(101L, "option-row-1"),
            SamplePhotoCaptureResult.takeForVisit(101L)
        )
        assertNull(SamplePhotoCaptureResult.takeForVisit(101L))
    }

    @Test
    fun anotherVisitCannotConsumeTheResult() {
        SamplePhotoCaptureResult.publish(101L, "option-row-1")

        assertNull(SamplePhotoCaptureResult.takeForVisit(202L))
        assertEquals("option-row-1", SamplePhotoCaptureResult.takeForVisit(101L)?.optionRowId)
    }

    @Test
    fun latestCaptureReplacesThePreviousResult() {
        SamplePhotoCaptureResult.publish(101L, "option-row-1")
        SamplePhotoCaptureResult.publish(202L, "option-row-2")

        assertNull(SamplePhotoCaptureResult.takeForVisit(101L))
        assertEquals("option-row-2", SamplePhotoCaptureResult.takeForVisit(202L)?.optionRowId)
        assertNull(SamplePhotoCaptureResult.takeForVisit(202L))
    }

    @Test
    fun invalidIdentifiersCannotReplaceAValidResult() {
        SamplePhotoCaptureResult.publish(101L, "option-row-1")
        SamplePhotoCaptureResult.publish(0L, "option-row-2")
        SamplePhotoCaptureResult.publish(202L, " ")

        assertNull(SamplePhotoCaptureResult.takeForVisit(0L))
        assertNull(SamplePhotoCaptureResult.takeForVisit(202L))
        assertEquals("option-row-1", SamplePhotoCaptureResult.takeForVisit(101L)?.optionRowId)
    }
}
