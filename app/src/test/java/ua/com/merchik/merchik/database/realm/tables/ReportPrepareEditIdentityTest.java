package ua.com.merchik.merchik.database.realm.tables;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import static org.junit.Assert.*;

public class ReportPrepareEditIdentityTest {
    private static ReportPrepareDB row(long id, String visit, String product) {
        ReportPrepareDB row = new ReportPrepareDB();
        row.setID(id);
        row.setCodeDad2(visit);
        row.setTovarId(product);
        return row;
    }

    @Test public void unchangedIdUsesCurrentRow() {
        ReportPrepareDB current = row(10, "100", "1");
        assertSame(current, ReportPrepareRealm.resolveEditRow(row(10, "100", "1"), current, Collections.emptyList()));
    }

    @Test public void replacedIdResolvesSameVisitAndProductWithoutChangingOtherFields() {
        ReportPrepareDB snapshot = row(10, "100", "1");
        snapshot.setPrice("55");
        ReportPrepareDB current = row(12, "100", "1");
        current.setPrice("70");
        assertSame(current, ReportPrepareRealm.resolveEditRow(snapshot, null, Arrays.asList(
                row(13, "101", "1"), row(14, "100", "2"), current)));
        assertEquals("55", snapshot.getPrice());
        assertEquals("70", current.getPrice());
    }

    @Test public void ambiguousProductIsRejected() {
        assertNull(ReportPrepareRealm.resolveEditRow(row(10, "100", "1"), null,
                Arrays.asList(row(12, "100", "1"), row(13, "100", "1"))));
    }

    @Test public void reusedIdFromAnotherVisitOrProductIsRejected() {
        assertNull(ReportPrepareRealm.resolveEditRow(row(10, "100", "1"), row(10, "101", "1"), Collections.emptyList()));
        assertNull(ReportPrepareRealm.resolveEditRow(row(10, "100", "1"), row(10, "100", "2"), Collections.emptyList()));
    }

    @Test public void deletedRowIsNotRecreated() {
        assertNull(ReportPrepareRealm.resolveEditRow(row(10, "100", "1"), null, Collections.emptyList()));
    }

    @Test public void incompleteIdentityCannotFallBackToAnotherRow() {
        for (String missing : Arrays.asList(null, "", "0")) {
            assertNull(ReportPrepareRealm.resolveEditRow(row(10, missing, "1"), null,
                    Collections.singletonList(row(12, missing, "1"))));
            assertNull(ReportPrepareRealm.resolveEditRow(row(10, "100", missing), null,
                    Collections.singletonList(row(12, "100", missing))));
        }
    }
}
