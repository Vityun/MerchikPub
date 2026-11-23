package ua.com.merchik.merchik.Options;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OptionCorrectionListTest {

    @Test
    public void activeCorrectionListBlocksPassiveDismissButRetainsCloseButton() throws Exception {
        String show = section(source("Options/OptionControl.java"),
                "public void showOptionMassage(", "private void setSpannableOptionText(");
        assertTrue(show.contains("dialog.setClose(dialog::dismiss)"));
        assertTrue(show.contains("if (correctionList) dialog.setPassiveDismissEnabled(false)"));

        String mode = section(source("dialogs/DialogData.java"),
                "private void applyDismissMode()", "if (dismissOnlyByButtonsMode)");
        assertTrue(mode.contains("passiveDismissEnabled && !dismissOnlyByButtonsMode"));
        assertTrue(mode.contains("dialog.setCancelable(allowPassiveDismiss)"));
        assertTrue(mode.contains("dialog.setCanceledOnTouchOutside(allowPassiveDismiss)"));
    }

    @Test
    public void recheckClosesOnSuccessWithoutRenderingItAndUpdatesSameDialogOtherwise() throws Exception {
        String refresh = section(source("Options/OptionControl.java"),
                "private void refreshCorrectionList(", "protected CharSequence optionMessageHeading()");
        int rerun = refresh.indexOf("correctionCheck.run()");
        int success = refresh.indexOf("if (\"2\".equals(optionDB.getIsSignal()))");
        int dismiss = refresh.indexOf("message.dismiss()");
        int render = refresh.indexOf("setSpannableOptionText(");

        assertTrue(rerun >= 0 && rerun < success && success < dismiss && dismiss < render);
        assertTrue(refresh.indexOf("spannableStringBuilder.clear()") < rerun);
        assertFalse(refresh.contains("showOptionMassage("));
    }

    @Test
    public void watchesVisitEditsAndStopsWatchingWhenTheDialogCloses() throws Exception {
        String observe = section(source("Options/OptionControl.java"),
                "private void observeCorrectionList(", "private void refreshCorrectionList(");
        assertTrue(observe.contains("ReportPrepareRealm.getReportPrepareByDad2(dad2)"));
        assertTrue(observe.contains("StackPhotoRealm.getPhotosByDAD2AllTypes(dad2)"));
        assertTrue(observe.contains("onResume(LifecycleOwner owner)"));
        assertTrue(observe.contains("message.setOnDismissAction("));
        assertTrue(observe.contains("subscriptions) unsubscribe.run()"));
        assertTrue(observe.contains("rows.removeChangeListener(listener)"));
    }

    private static String source(String relativePath) throws Exception {
        Path app = Files.isDirectory(Paths.get("app/src/main")) ? Paths.get("app") : Paths.get(".");
        Path file = app.resolve("src/main/java/ua/com/merchik/merchik").resolve(relativePath);
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    private static String section(String source, String start, String end) {
        int from = source.indexOf(start);
        return source.substring(from, source.indexOf(end, from));
    }
}
