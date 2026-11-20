package ua.com.merchik.merchik.dialogs;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class TarPhotoSourceWiringTest {
    private static final String FRAGMENT = "Activities/TaskAndReclamations/TasksActivity/Tab3Fragment.java";

    @Test public void journalChoiceIsOptInAndKeepsExistingCallersCompatible() throws IOException {
        String source = source("dialogs/DialodTAR/TarPhotoSourceDialog.kt");
        assertTrue(source.contains("@JvmOverloads"));
        assertTrue(source.contains("onJournal: Runnable? = null"));
        String optional = between(source, "if (onJournal != null) {", "}");
        assertTrue(optional.contains("MessageDialogChoice(TAR_PHOTO_SOURCE_JOURNAL, \"Додати фото з журналу\")"));
        assertFalse(source("MakePhoto/ProductPhotoCapture.kt").contains("onJournal"));
    }

    @Test public void eachChoiceHasItsOwnCallbackAndDismissalDoesNotStartPhotoAction() throws IOException {
        String source = source("dialogs/DialodTAR/TarPhotoSourceDialog.kt");
        assertTrue(source.contains("TAR_PHOTO_SOURCE_CAMERA -> onCamera"));
        assertTrue(source.contains("TAR_PHOTO_SOURCE_GALLERY -> onGallery"));
        assertTrue(source.contains("TAR_PHOTO_SOURCE_JOURNAL -> onJournal"));
        assertTrue(source.contains("else -> null"));
        String finish = between(source, "private fun finish()", "private fun dismiss()");
        assertTrue(finish.indexOf("dismiss()") < finish.indexOf("action.run()"));
        assertTrue(between(finish, "if (action == null)", "else").contains("onDismiss.run()"));
    }

    @Test public void longPressAndNewChoiceOpenSameJournalWithoutChangingItsFilters() throws IOException {
        String source = source(FRAGMENT);
        assertTrue(between(source, "case 1:", "case 2:").contains("openPhotoJournal();"));
        assertTrue(between(source, "private void openPhotoSourcePicker(", "private boolean hasValidPhoto(")
                .contains("this::openPhotoJournal"));
        String journal = between(source, "private void openPhotoJournal()", "private boolean beginPhotoAction()");
        assertTrue(journal.contains("JournalPhotoSDBViewModel.class.getCanonicalName()"));
        assertTrue(journal.contains("ContextUI.ADD_STACK_PHOTO_TO_ZIR"));
        assertTrue(journal.contains("ModeUI.ONE_SELECT"));
        assertTrue(journal.contains("addProperty(\"clientId\", tarData.client)"));
        assertTrue(journal.contains("addProperty(\"addrId\", tarData.addr.toString())"));
        assertTrue(journal.contains("NEED_UPDATE_UI_REQUEST"));
    }

    @Test public void journalClearsPendingCaptureTypeAndPreviousSelectionBeforeOpening() throws IOException {
        String journal = between(source(FRAGMENT), "private void openPhotoJournal()", "private boolean beginPhotoAction()");
        assertTrue(journal.indexOf("clearPendingPhotoTypeId()") < journal.indexOf("startActivityForResult("));
        assertTrue(journal.indexOf("TarPhotoDataHolder.Companion.instance().init()") < journal.indexOf("startActivityForResult("));
        assertFalse(journal.contains("setPhoto_type("));
        assertFalse(journal.contains("getPhotoFlow().start("));
        assertTrue(journal.contains("photoActionInProgress = false"));
    }

    @Test public void cancelledOrEmptyJournalResultCannotReplaceCommentAttachment() throws IOException {
        String result = between(source("Activities/TaskAndReclamations/TARActivity.java"),
                "if (requestCode == NEED_UPDATE_UI_REQUEST) {", "} catch (Exception e)");
        assertTrue(result.contains("Integer selectedPhotoId = holder.getPhotoToId();"));
        assertTrue(result.contains("holder.init();"));
        assertTrue(result.indexOf("if (resultCode != RESULT_OK || selectedPhotoId == null) return;")
                < result.indexOf("setPhoto(selectedPhotoId)"));
    }

    private static String between(String source, String first, String last) {
        int start = source.indexOf(first);
        assertTrue("Missing " + first, start >= 0);
        int end = source.indexOf(last, start + first.length());
        assertTrue("Missing " + last, end > start);
        return source.substring(start, end);
    }

    private static String source(String path) throws IOException {
        Path root = Path.of("app/src/main/java/ua/com/merchik/merchik");
        if (!Files.isDirectory(root)) root = Path.of("src/main/java/ua/com/merchik/merchik");
        return new String(Files.readAllBytes(root.resolve(path)), StandardCharsets.UTF_8);
    }
}
