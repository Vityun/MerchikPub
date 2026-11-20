package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;

import static org.junit.Assert.*;

public class TarNavigationTest {
    private static final String JAVA_PATH = "java/ua/com/merchik/merchik/Activities/TaskAndReclamations/";
    private static final String ANDROID = "http://schemas.android.com/apk/res/android";

    @Test public void selectedIdCannotBeReplacedByThePreviouslyOpenedTask() {
        TasksAndReclamationsSDB record = new TasksAndReclamationsSDB();
        record.id = 12001;
        assertTrue(TARSecondFrag.matchesRequestedId(12001, record));
        assertFalse(TARSecondFrag.matchesRequestedId(12002, record));
        assertFalse(TARSecondFrag.matchesRequestedId(12001, null));
        record.id = null;
        assertFalse(TARSecondFrag.matchesRequestedId(12001, record));
    }

    @Test public void bothBackActionsPopTheChildStackToTheSameList() throws Exception {
        String detail = source("TasksActivity/TARSecondFrag.java");
        String container = source("TARFragmentHome.java");
        assertTrue(detail.contains("addCallback(getViewLifecycleOwner(), callback)"));
        assertTrue(detail.contains("handleOnBackPressed() { parent.returnToList(); }"));
        assertTrue(detail.contains("back.setOnClickListener(v -> parent.returnToList())"));
        assertTrue(container.contains(".addToBackStack(DETAIL_BACK_STACK)"));
        assertTrue(container.contains("children.popBackStackImmediate(DETAIL_BACK_STACK,"));
        assertTrue(source("TasksActivity/TARHomeFrag.java").contains("view.setId(R.id.tar_home_list)"));
    }

    @Test public void openingByLinkBuildsTheListBeforeOpeningTheExactRecord() throws Exception {
        String container = source("TARFragmentHome.java");
        int init = container.indexOf("public void onViewCreated(");
        int list = container.indexOf("showList();", init);
        int detail = container.indexOf("showDetail(TARSecondFrag.newInstanceById(initialTaskId))", init);
        assertTrue(init >= 0 && list > init && detail > list);
        assertTrue(source("TARActivity.java").contains("intent.getIntExtra(\"TAR_ID\", 0)"));
    }

    @Test public void backArrowIsInsideTheHeaderOnTheStartSide() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        NodeList nodes = factory.newDocumentBuilder().parse(mainPath("res/layout/fragment_tar_tabs.xml").toFile())
                .getElementsByTagName("androidx.appcompat.widget.AppCompatImageButton");
        assertEquals(1, nodes.getLength());
        Element button = (Element) nodes.item(0);
        assertEquals("@+id/tar_header_back", button.getAttributeNS(ANDROID, "id"));
        assertEquals("@+id/tar_header", ((Element) button.getParentNode()).getAttributeNS(ANDROID, "id"));
        assertEquals("start|center_vertical", button.getAttributeNS(ANDROID, "layout_gravity"));
        assertEquals("@string/tar_back_to_list", button.getAttributeNS(ANDROID, "contentDescription"));
    }

    private static String source(String path) throws Exception {
        return new String(Files.readAllBytes(mainPath(JAVA_PATH + path)), StandardCharsets.UTF_8);
    }

    private static Path mainPath(String path) {
        Path root = Path.of("app/src/main");
        if (!Files.isDirectory(root)) root = Path.of("src/main");
        return root.resolve(path);
    }
}
