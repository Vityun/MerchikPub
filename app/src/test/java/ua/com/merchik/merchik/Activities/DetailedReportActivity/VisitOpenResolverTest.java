package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import org.junit.Test;

import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class VisitOpenResolverTest {
    private static final long DAD2 = 1121126044315061639L;
    private static final long OLD_ID = 4635526875L;
    private static final long NEW_ID = 4635586261L;

    @Test
    public void opensMatchingIdWithoutFallback() {
        WpDataDB visit = visit(OLD_ID, DAD2);
        assertSame(visit, VisitOpenResolver.resolve(OLD_ID, DAD2, id -> {
            assertEquals(OLD_ID, id);
            return visit;
        }, dad2 -> unexpectedLookup()));
    }

    @Test
    public void opensNewIdForSameVisitWhenCardIdIsStale() {
        WpDataDB visit = visit(NEW_ID, DAD2);
        assertSame(visit, VisitOpenResolver.resolve(OLD_ID, DAD2, id -> null, dad2 -> {
            assertEquals(DAD2, dad2);
            return visit;
        }));
    }

    @Test
    public void usesDad2WhenOldIdBelongsToAnotherVisit() {
        WpDataDB expected = visit(NEW_ID, DAD2);
        assertSame(expected, VisitOpenResolver.resolve(OLD_ID, DAD2,
                id -> visit(OLD_ID, DAD2 + 1), dad2 -> expected));
    }

    @Test
    public void doesNotOpenOtherVisitWhenRequestedDad2IsMissing() {
        assertNull(VisitOpenResolver.resolve(OLD_ID, DAD2,
                id -> visit(OLD_ID, DAD2 + 1), dad2 -> null));
    }

    @Test
    public void handlesRemovedVisit() {
        assertNull(VisitOpenResolver.resolve(OLD_ID, DAD2, id -> null, dad2 -> null));
    }

    @Test
    public void supportsLegacyIdOnlyLaunch() {
        WpDataDB visit = visit(OLD_ID, DAD2);
        assertSame(visit, VisitOpenResolver.resolve(OLD_ID, 0,
                id -> visit, dad2 -> unexpectedLookup()));
    }

    @Test
    public void handlesMissingLegacyIdWithoutGuessingVisit() {
        assertNull(VisitOpenResolver.resolve(OLD_ID, 0,
                id -> null, dad2 -> unexpectedLookup()));
    }

    @Test
    public void supportsDad2OnlyLaunch() {
        WpDataDB visit = visit(NEW_ID, DAD2);
        assertSame(visit, VisitOpenResolver.resolve(0, DAD2,
                id -> unexpectedLookup(), dad2 -> visit));
    }

    @Test
    public void emptyTargetDoesNotLookUpZeroRow() {
        assertNull(VisitOpenResolver.resolve(0, 0,
                id -> unexpectedLookup(), dad2 -> unexpectedLookup()));
    }

    @Test
    public void rejectsMismatchedFallback() {
        assertNull(VisitOpenResolver.resolve(OLD_ID, DAD2,
                id -> null, dad2 -> visit(NEW_ID, DAD2 + 1)));
    }

    private static WpDataDB visit(long id, long dad2) {
        WpDataDB visit = new WpDataDB();
        visit.setId(id);
        visit.setCode_dad2(dad2);
        return visit;
    }

    private static WpDataDB unexpectedLookup() {
        fail("Unexpected visit lookup");
        return null;
    }
}
