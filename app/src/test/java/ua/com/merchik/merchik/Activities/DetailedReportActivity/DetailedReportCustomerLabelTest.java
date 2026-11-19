package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.*;

public class DetailedReportCustomerLabelTest {
    @Test public void showsMainGroupBesideCustomer() {
        assertEquals("Витмарк-Украина СП (Напитки)",
                DetailedReportHomeFrag.formatCustomerLabel("Витмарк-Украина СП", "Напитки"));
    }

    @Test public void absentOrInvalidGroupLeavesCustomerWithoutEmptyBrackets() {
        for (String group : new String[]{null, "", "  ", "0", "null", " NULL "}) {
            assertEquals("Customer", DetailedReportHomeFrag.formatCustomerLabel("Customer", group));
        }
    }

    @Test public void missingCustomerDoesNotShowOrphanedGroup() {
        for (String name : new String[]{null, "", " "}) {
            assertEquals("", DetailedReportHomeFrag.formatCustomerLabel(name, "Group"));
        }
    }

    @Test public void trimsOuterWhitespaceAndPreservesPunctuation() {
        assertEquals("Client & Co. (Drinks (cold))",
                DetailedReportHomeFrag.formatCustomerLabel(" Client & Co. ", " Drinks (cold) "));
    }

    @Test public void alreadyAppendedGroupIsNotDuplicated() {
        assertEquals("Customer (Group)", DetailedReportHomeFrag.formatCustomerLabel("Customer (Group)", "Group"));
    }

    @Test public void readsMainGroupFromCustomerWithoutChangingVisitOrEkl() throws Exception {
        String source = Files.readString(Path.of("app/src/main/java/ua/com/merchik/merchik/Activities/"
                + "DetailedReportActivity/DetailedReportHomeFrag.java"), StandardCharsets.UTF_8);
        String method = source.substring(source.indexOf("private String getCustomerLabel("),
                source.indexOf("private void setTransleted()"));
        assertTrue(source.contains("textDRCustV.setText(getCustomerLabel(wpDataDB));"));
        assertTrue(method.contains("customerDao().getById(data.getClient_id())"));
        assertTrue(method.contains("customer.mainTovGrp != null && customer.mainTovGrp > 0"));
        assertTrue(method.contains("tovarGroupDao().getById(customer.mainTovGrp)"));
        assertTrue(method.contains("group == null ? null : group.nm"));
        assertFalse(method.contains("setClient_txt"));
        assertFalse(method.contains("tovarGroupClientDao"));
    }

    @Test public void customerWrapsAndPerformerRemainsBelowWholeCustomerRow() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        NodeList nodes = factory.newDocumentBuilder()
                .parse(Path.of("app/src/main/res/layout/fragment_dr_home.xml").toFile()).getElementsByTagName("*");
        Element customer = find(nodes, "textDRCustVal");
        assertEquals("wrap_content", android(customer, "layout_height"));
        assertEquals("0dp", android(customer, "layout_width"));
        assertFalse(customer.hasAttributeNS("http://schemas.android.com/apk/res/android", "maxLines"));
        assertFalse(customer.hasAttributeNS("http://schemas.android.com/apk/res/android", "ellipsize"));
        assertEquals("textDRCust,textDRCustVal", app(find(nodes, "reportCustomerBottom"), "constraint_referenced_ids"));
        assertEquals("@id/reportCustomerBottom", app(find(nodes, "textDRMerc"), "layout_constraintTop_toBottomOf"));
    }

    private static Element find(NodeList nodes, String id) {
        for (int index = 0; index < nodes.getLength(); index++) {
            Element element = (Element) nodes.item(index);
            if (("@+id/" + id).equals(android(element, "id"))) return element;
        }
        throw new AssertionError("Missing view " + id);
    }

    private static String android(Element element, String key) {
        return element.getAttributeNS("http://schemas.android.com/apk/res/android", key);
    }

    private static String app(Element element, String key) {
        return element.getAttributeNS("http://schemas.android.com/apk/res-auto", key);
    }
}
