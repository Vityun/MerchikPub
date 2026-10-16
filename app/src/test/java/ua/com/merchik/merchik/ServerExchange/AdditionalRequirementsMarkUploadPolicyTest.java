package ua.com.merchik.merchik.ServerExchange;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB;
import ua.com.merchik.merchik.data.ServerData.TARCommentsData.AdditionalRequirementsMarks.AdditionalRequirementsMarksListServerData;

import static org.junit.Assert.*;
import static ua.com.merchik.merchik.ServerExchange.AdditionalRequirementsMarkUploadPolicy.*;

public class AdditionalRequirementsMarkUploadPolicyTest {
    private static final int USER = 97456;

    @Test
    public void loggedFiveEditsProduceOnlyTwoLatestScores() {
        List<AdditionalRequirementsMarkDB> batch = nextBatch(Arrays.asList(
                mark("1", 100, 55377, "6"), mark("2", 101, 35376, "6"),
                mark("3", 102, 35376, "6"), mark("4", 103, 35376, "7"),
                mark("5", 104, 35376, "8")), USER);
        assertEquals(2, batch.size());
        assertEquals(Integer.valueOf(55377), batch.get(0).getItemId());
        assertEquals("6", batch.get(0).getScore());
        assertEquals(Integer.valueOf(35376), batch.get(1).getItemId());
        assertEquals("8", batch.get(1).getScore());
    }

    @Test
    public void selectionDoesNotDependOnQueryOrder() {
        AdditionalRequirementsMarkDB oldest = mark("1", 100, 35376, "6");
        AdditionalRequirementsMarkDB newest = mark("3", 102, 35376, "8");
        AdditionalRequirementsMarkDB middle = mark("2", 101, 35376, "7");
        for (List<AdditionalRequirementsMarkDB> rows : Arrays.asList(
                Arrays.asList(oldest, middle, newest), Arrays.asList(newest, oldest, middle),
                Arrays.asList(middle, newest, oldest))) {
            assertEquals(Collections.singletonList(newest), latestPending(rows));
        }
    }

    @Test
    public void editsWithinSameSecondUseNumericLocalId() {
        AdditionalRequirementsMarkDB newest = mark("10", 100, 35376, "8");
        assertEquals(Collections.singletonList(newest), latestPending(Arrays.asList(
                mark("9", 100, 35376, "7"), newest)));
        assertTrue(compareNewestFirst(mark("1792074146123", 100, 1, "8"),
                mark("1792074146000", 100, 1, "7")) < 0);
    }

    @Test
    public void timestampHasPriorityOverLocalId() {
        AdditionalRequirementsMarkDB newest = mark("9", 101, 35376, "8");
        assertEquals(Collections.singletonList(newest), latestPending(Arrays.asList(
                newest, mark("10", 100, 35376, "6"))));
    }

    @Test
    public void newestCommentWinsEvenIfScoreIsUnchanged() {
        AdditionalRequirementsMarkDB previous = mark("1", 100, 35376, "6");
        AdditionalRequirementsMarkDB newest = mark("2", 100, 35376, "6");
        newest.comment = "Updated comment";
        assertEquals(Collections.singletonList(newest), latestPending(Arrays.asList(newest, previous)));
    }

    @Test
    public void selectionSeparatesUsersTypesAndItems() {
        AdditionalRequirementsMarkDB requirement = mark("1", 100, 1, "6");
        AdditionalRequirementsMarkDB material = mark("2", 100, 1, "7");
        material.setTp("0");
        AdditionalRequirementsMarkDB otherUser = mark("3", 100, 1, "8");
        otherUser.setUserId("5");
        AdditionalRequirementsMarkDB otherItem = mark("4", 100, 2, "9");
        List<AdditionalRequirementsMarkDB> rows = Arrays.asList(requirement, material, otherUser, otherItem);
        assertEquals(4, latestPending(rows).size());
        assertEquals(Arrays.asList(requirement, otherItem), nextBatch(rows, USER));
        assertEquals(Collections.singletonList(otherUser), nextBatch(rows, 5));
    }

    @Test
    public void typesWithSameItemIdAreSentInSeparateBatches() {
        AdditionalRequirementsMarkDB requirement = mark("1", 100, 1, "6");
        AdditionalRequirementsMarkDB material = mark("2", 100, 1, "7");
        material.setTp("0");
        List<AdditionalRequirementsMarkDB> rows = Arrays.asList(requirement, material);
        assertEquals(Collections.singletonList(requirement), nextBatch(rows, USER));
        requirement.setUploadStatus("123");
        assertEquals(Collections.singletonList(material), nextBatch(rows, USER));
    }

    @Test
    public void noActiveUserMeansNoUpload() {
        List<AdditionalRequirementsMarkDB> rows = Collections.singletonList(mark("1", 100, 1, "6"));
        assertTrue(nextBatch(rows, 0).isEmpty());
        assertTrue(nextBatch(rows, -1).isEmpty());
    }

    @Test
    public void acknowledgedAndSupersededHistoryIsNotUploaded() {
        List<AdditionalRequirementsMarkDB> rows = new ArrayList<>();
        for (String status : Arrays.asList(null, "", "123456", SUPERSEDED)) {
            AdditionalRequirementsMarkDB row = mark("1", 100, 1, "6");
            row.setUploadStatus(status);
            rows.add(row);
        }
        assertTrue(latestPending(rows).isEmpty());
    }

    @Test
    public void invalidPendingIdentityDoesNotBecomeARequest() {
        List<Consumer<AdditionalRequirementsMarkDB>> changes = Arrays.asList(
                row -> row.setId(null), row -> row.setId(""), row -> row.setDt(null),
                row -> row.setUserId(null), row -> row.setUserId(""), row -> row.setItemId(null),
                row -> row.setItemId(0), row -> row.setTp(null), row -> row.setTp("2"));
        for (Consumer<AdditionalRequirementsMarkDB> change : changes) {
            AdditionalRequirementsMarkDB row = mark("1", 100, 1, "6");
            change.accept(row);
            assertTrue(latestPending(Collections.singletonList(row)).isEmpty());
        }
        assertTrue(latestPending(Collections.singletonList(null)).isEmpty());
    }

    @Test
    public void comparatorIsConsistentForMissingAndNonNumericServerIds() {
        List<AdditionalRequirementsMarkDB> rows = new ArrayList<>(Arrays.asList(
                mark("10", 100, 1, "6"), mark("9", 100, 1, "6"),
                mark("1x", 100, 1, "6"), mark(null, 100, 1, "6")));
        Collections.sort(rows, AdditionalRequirementsMarkUploadPolicy::compareNewestFirst);
        assertEquals("10", rows.get(0).getId());
        assertEquals("9", rows.get(1).getId());
        assertEquals("1x", rows.get(2).getId());
        assertNull(rows.get(3).getId());
    }

    @Test
    public void identicalPendingSnapshotMayBeAcknowledged() {
        assertTrue(canAcknowledge(mark("1", 100, 1, "6"), mark("1", 100, 1, "6")));
    }

    @Test
    public void anyChangeSinceSendingPreventsAcknowledgement() {
        List<Consumer<AdditionalRequirementsMarkDB>> changes = Arrays.asList(
                row -> row.setId("2"), row -> row.setDt(101L), row -> row.setUserId("5"),
                row -> row.setItemId(2), row -> row.setTp("0"), row -> row.setScore("8"),
                row -> row.comment = "Updated", row -> row.setUploadStatus("123"),
                row -> row.setUploadStatus(SUPERSEDED));
        AdditionalRequirementsMarkDB sent = mark("1", 100, 1, "6");
        for (Consumer<AdditionalRequirementsMarkDB> change : changes) {
            AdditionalRequirementsMarkDB current = mark("1", 100, 1, "6");
            change.accept(current);
            assertFalse(canAcknowledge(sent, current));
        }
    }

    @Test
    public void missingRecordIsNotRecreatedByAcknowledgement() {
        assertFalse(canAcknowledge(mark("1", 100, 1, "6"), null));
        assertFalse(canAcknowledge(null, mark("1", 100, 1, "6")));
    }

    @Test
    public void onlyExplicitMatchingSuccessAcknowledgesItem() {
        List<AdditionalRequirementsMarksListServerData> response = Arrays.asList(
                ack(1, true), ack(2, false), ack(3, null), ack(null, true), null);
        assertTrue(wasAccepted(1, response));
        assertFalse(wasAccepted(2, response));
        assertFalse(wasAccepted(3, response));
        assertFalse(wasAccepted(4, response));
        assertFalse(wasAccepted(null, response));
    }

    @Test
    public void emptyOrMissingResponseKeepsPending() {
        assertFalse(wasAccepted(1, null));
        assertFalse(wasAccepted(1, Collections.emptyList()));
    }

    @Test
    public void conflictingAcknowledgementsKeepPendingRegardlessOfOrder() {
        assertFalse(wasAccepted(1, Arrays.asList(ack(1, true), ack(1, false))));
        assertFalse(wasAccepted(1, Arrays.asList(ack(1, false), ack(1, true))));
        assertFalse(wasAccepted(1, Arrays.asList(ack(1, true), ack(1, null))));
    }

    @Test
    public void repeatedSuccessfulAcknowledgementIsSafe() {
        assertTrue(wasAccepted(1, Arrays.asList(ack(1, true), ack(1, true))));
    }

    @Test
    public void newEditDuringUploadStaysPendingForNextBatch() {
        AdditionalRequirementsMarkDB sent = mark("1", 100, 1, "6");
        AdditionalRequirementsMarkDB original = mark("1", 100, 1, "6");
        AdditionalRequirementsMarkDB changed = mark("2", 101, 1, "8");
        List<AdditionalRequirementsMarkDB> database = Arrays.asList(original, changed);
        for (AdditionalRequirementsMarkDB current : database) {
            if (canAcknowledge(sent, current)) current.setUploadStatus("123");
        }
        assertEquals("123", original.getUploadStatus());
        assertEquals("0", changed.getUploadStatus());
        assertEquals(Collections.singletonList(changed), nextBatch(database, USER));
    }

    @Test
    public void inPlaceEditDuringUploadIsNotOverwritten() {
        AdditionalRequirementsMarkDB sent = mark("1", 100, 1, "6");
        AdditionalRequirementsMarkDB current = mark("1", 100, 1, "8");
        assertFalse(canAcknowledge(sent, current));
        assertEquals(Collections.singletonList(current), nextBatch(Collections.singletonList(current), USER));
        assertEquals("8", current.getScore());
    }

    @Test
    public void failureRetriesLatestAndSuccessDoesNotResendHistory() {
        AdditionalRequirementsMarkDB old = mark("1", 100, 1, "6");
        AdditionalRequirementsMarkDB latest = mark("2", 101, 1, "8");
        List<AdditionalRequirementsMarkDB> rows = Arrays.asList(old, latest);
        assertEquals(Collections.singletonList(latest), nextBatch(rows, USER));
        old.setUploadStatus(SUPERSEDED);
        assertEquals(Collections.singletonList(latest), nextBatch(rows, USER));
        latest.setUploadStatus("123");
        assertTrue(nextBatch(rows, USER).isEmpty());
    }

    private static AdditionalRequirementsMarkDB mark(String id, long dt, int item, String score) {
        AdditionalRequirementsMarkDB row = new AdditionalRequirementsMarkDB();
        row.setId(id);
        row.setDt(dt);
        row.setItemId(item);
        row.setUserId(String.valueOf(USER));
        row.setTp("1");
        row.setScore(score);
        row.comment = "";
        row.setUploadStatus("0");
        return row;
    }

    private static AdditionalRequirementsMarksListServerData ack(Integer id, Boolean state) {
        AdditionalRequirementsMarksListServerData row = new AdditionalRequirementsMarksListServerData();
        row.elementId = id;
        row.state = state;
        return row;
    }
}
