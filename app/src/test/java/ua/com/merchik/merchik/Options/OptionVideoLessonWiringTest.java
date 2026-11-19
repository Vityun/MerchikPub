package ua.com.merchik.merchik.Options;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class OptionVideoLessonWiringTest {
    @Test
    public void previousWorkWarningOffersLesson87BeforeAnyDialogTextBranch() throws IOException {
        String control = source("Options/OptionControl.java");
        String binding = "if (block && this instanceof OptionControlEndAnotherWork) {\n"
                + "                            dialog.setVideoLessonById(context, 87);\n"
                + "                        }";
        assertTrue(control.contains(binding));
        assertTrue(control.indexOf(binding) > control.indexOf("case DIALOG:"));
        assertTrue(control.indexOf(binding) < control.indexOf("CharSequence heading = optionMessageHeading();"));
        assertTrue(source("Options/Options.java").contains(
                "case 156928:\n                OptionControlEndAnotherWork optionControlEndAnotherWork ="));
    }

    @Test
    public void directLessonButtonDoesNotTreatLessonIdAsSiteObjectId() throws IOException {
        String dialog = source("dialogs/DialogData.java");
        String method = dialog.substring(dialog.indexOf("public void setVideoLessonById("),
                dialog.indexOf("public void setImgBtnCall("));
        assertTrue(method.contains("imgBtnVideoLesson.setVisibility(View.VISIBLE)"));
        assertTrue(method.contains("imgBtnVideoLesson.setColorFilter(Color.WHITE)"));
        assertTrue(method.contains("RealmManager.getVideoLesson(lessonId) == null"));
        assertTrue(method.contains("VideoLessonsLauncher.openByLessonIds(context, Collections.singletonList(lessonId))"));
        assertFalse(method.contains("RealmManager.getLesson("));
        assertFalse(method.contains("openByObject"));
        assertFalse(method.contains("dismiss("));
    }

    @Test
    public void additionalRequirementsWarningKeepsItsExistingLessonBinding() throws IOException {
        assertTrue(source("Options/OptionControl.java").contains(
                "if (block && this instanceof OptionControlAdditionalRequirementsMark) {\n"
                        + "                            dialog.setVideoLesson(context, true, new Integer[]{10407}, null, null);\n"
                        + "                        }"));
    }

    private static String source(String file) throws IOException {
        Path root = Path.of("app/src/main/java/ua/com/merchik/merchik");
        if (!Files.isDirectory(root)) root = Path.of("src/main/java/ua/com/merchik/merchik");
        return Files.readString(root.resolve(file), StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}
