package ua.com.merchik.merchik.dialogs;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.*;

public class TaskPhotoLayoutTest {
    private static final String ANDROID = "http://schemas.android.com/apk/res/android";

    private Document xml(String relative) throws Exception {
        Path root = Path.of("src/main/res");
        if (!Files.isDirectory(root)) root = Path.of("app/src/main/res");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(root.resolve(relative).toFile());
    }

    private Element byId(Document doc, String id) {
        NodeList nodes = doc.getElementsByTagName("*");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            if (("@+id/" + id).equals(element.getAttributeNS(ANDROID, "id"))) return element;
        }
        throw new AssertionError("Missing view " + id);
    }

    @Test public void taskBackgroundMatchesActiveTabAndContentIsFramed() throws Exception {
        Document doc = xml("layout/fragment_tab_item_first.xml");
        assertEquals("@color/main_form", doc.getDocumentElement().getAttributeNS(ANDROID, "background"));
        Element card = byId(doc, "tarDetailsCard");
        assertEquals("@drawable/detailed_report_logo_background", card.getAttributeNS(ANDROID, "background"));
        assertEquals("androidx.core.widget.NestedScrollView", card.getParentNode().getNodeName());
        Document background = xml("drawable/detailed_report_logo_background.xml");
        Element solid = (Element) background.getElementsByTagName("solid").item(0);
        assertEquals("@color/white", solid.getAttributeNS(ANDROID, "color"));
    }

    @Test public void photosUseDetailedReportFramesWithoutCropping() throws Exception {
        Document doc = xml("layout/fragment_tab_item_first.xml");
        for (String id : new String[]{"TARPhoto", "TARPhoto2"}) {
            Element photo = byId(doc, id);
            Element frame = (Element) photo.getParentNode();
            assertEquals("96dp", frame.getAttributeNS(ANDROID, "layout_width"));
            assertEquals("96dp", frame.getAttributeNS(ANDROID, "layout_height"));
            assertEquals("@drawable/detailed_report_logo_background", frame.getAttributeNS(ANDROID, "background"));
            assertEquals("8dp", photo.getAttributeNS(ANDROID, "layout_margin"));
            assertEquals("fitCenter", photo.getAttributeNS(ANDROID, "scaleType"));
        }
    }

    @Test public void allTaskControlsRemainInScrollableContentWithoutTextLimit() throws Exception {
        Document doc = xml("layout/fragment_tab_item_first.xml");
        Element card = byId(doc, "tarDetailsCard");
        for (String id : new String[]{"TARPhoto", "TARPhoto2", "text_data", "ratingBar2", "ratingBar4",
                "wpLink", "lastComment", "tar_visit_map"}) {
            Node current = byId(doc, id);
            while (current != null && current != card) current = current.getParentNode();
            assertSame(id, card, current);
        }
        assertFalse(byId(doc, "text_data").hasAttributeNS(ANDROID, "maxLines"));
    }

    @Test public void loadingControlsAreOptInForExistingPhotoDialogCallers() throws Exception {
        Document doc = xml("layout/dialog_photo_fullscreen.xml");
        assertEquals("gone", byId(doc, "photoLoadStatus").getAttributeNS(ANDROID, "visibility"));
        assertEquals("gone", byId(doc, "photoLoadRetry").getAttributeNS(ANDROID, "visibility"));
        byId(doc, "photoLoadProgress");
        byId(doc, "photoLoadMessage");
    }

    @Test public void touchImageViewNeverReceivesNullTouchListener() throws Exception {
        Path source = Path.of("src/main/java/ua/com/merchik/merchik/dialogs/DialogFullPhotoR.java");
        if (!Files.exists(source)) source = Path.of("app").resolve(source);
        String code = Files.readString(source);
        // The Kotlin TouchImageView 3.1.0 override throws before displaying the cached bitmap.
        assertFalse(code.matches("(?s).*photo\\.setOnTouchListener\\(\\s*null\\s*\\).*"));
        assertTrue(code.contains("photo.setOnTouchListener((view, event) -> false)"));
    }
}
