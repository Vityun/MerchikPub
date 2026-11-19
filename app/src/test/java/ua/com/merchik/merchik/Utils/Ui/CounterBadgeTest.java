package ua.com.merchik.merchik.Utils.Ui;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;

import static org.junit.Assert.*;

public class CounterBadgeTest {
    private static final String VIEW = "ua.com.merchik.merchik.Utils.Ui.CounterBadgeView";

    @Test public void zeroAndSingleDigitsKeepTheirValue() {
        for (long count = 0; count <= 9; count++) {
            assertEquals(Long.toString(count), CounterBadgeView.formatCount(count, 9));
        }
    }

    @Test public void cappedCountersKeepThePlusSuffix() {
        assertEquals("9+", CounterBadgeView.formatCount(10, 9));
        assertEquals("9+", CounterBadgeView.formatCount(Long.MAX_VALUE, 9));
        assertEquals("99", CounterBadgeView.formatCount(99, 99));
        assertEquals("99+", CounterBadgeView.formatCount(100, 99));
    }

    @Test public void unlimitedCountersDoNotLoseDigits() {
        for (long count : new long[] {0, 1, 25, 134, 1000, Long.MAX_VALUE}) {
            assertEquals(Long.toString(count), CounterBadgeView.formatCount(count, null));
        }
    }

    @Test public void compactDefaultsAndRaisedOffsetAreShared() throws Exception {
        Document doc = resource("values/dimens.xml");
        assertEquals("18dp", dimension(doc, "counter_badge_size"));
        assertEquals("10sp", dimension(doc, "counter_badge_text_size"));
        assertEquals("1dp", dimension(doc, "counter_badge_border_width"));
        assertEquals("4dp", dimension(doc, "counter_badge_text_raise"));
        assertEquals("-4dp", dimension(doc, "counter_badge_raised_offset"));
    }

    @Test public void legacyCountersUseSharedViewAndCanExpand() throws Exception {
        String[] layouts = {"activity_menu_login", "activity_workplan", "counter", "exchange_layout",
                "fragment_dr_home", "fragment_dr_tar", "fragment_dr_tovar", "fragment_tab_item_first",
                "fragment_tab_item_third", "item_menu_sub", "item_rv_sync_button", "menu_item",
                "pause_work_toolbar_layout", "button_lighter", "feed_update_count"};
        int count = 0;
        for (String layout : layouts) {
            NodeList badges = resource("layout/" + layout + ".xml").getElementsByTagName(VIEW);
            assertTrue(layout, badges.getLength() > 0);
            for (int i = 0; i < badges.getLength(); i++) {
                Element badge = (Element) badges.item(i);
                assertEquals(layout, "wrap_content", badge.getAttribute("android:layout_width"));
                assertEquals(layout, "wrap_content", badge.getAttribute("android:layout_height"));
                for (String attr : new String[] {"background", "textSize", "padding", "minWidth", "minHeight"}) {
                    assertFalse(layout + ": " + attr, badge.hasAttribute("android:" + attr));
                }
                count++;
            }
        }
        assertEquals(21, count);
    }

    @Test public void drawerCountersAreRaisedRelativeToTheTitle() throws Exception {
        for (String layout : new String[] {"menu_item", "item_menu_sub"}) {
            NodeList badges = resource("layout/" + layout + ".xml").getElementsByTagName(VIEW);
            for (int i = 0; i < badges.getLength(); i++) {
                Element badge = (Element) badges.item(i);
                assertEquals("@id/menuText", badge.getAttribute("app:layout_constraintTop_toTopOf"));
                assertEquals("@dimen/counter_badge_raised_offset", badge.getAttribute("android:translationY"));
            }
        }
    }

    @Test public void workPlanBadgeKeepsItsAnchor() throws Exception {
        Element badge = (Element) resource("layout/activity_workplan.xml").getElementsByTagName(VIEW).item(0);
        assertEquals("@+id/fab3", badge.getAttribute("app:layout_constraintTop_toTopOf"));
        assertEquals("@+id/fab3", badge.getAttribute("app:layout_constraintEnd_toEndOf"));
        assertFalse(badge.hasAttribute("android:translationY"));
    }

    private static Document resource(String name) throws Exception {
        Path res = Paths.get("src/main/res");
        if (!Files.isDirectory(res)) res = Paths.get("app/src/main/res");
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(res.resolve(name).toFile());
    }

    private static String dimension(Document document, String name) throws Exception {
        return XPathFactory.newInstance().newXPath().evaluate("/resources/dimen[@name='" + name + "']", document);
    }
}
