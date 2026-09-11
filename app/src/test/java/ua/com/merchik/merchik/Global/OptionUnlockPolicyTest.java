package ua.com.merchik.merchik.Global;

import org.junit.Test;

import static org.junit.Assert.*;

public class OptionUnlockPolicyTest {
    private static final String DAD2 = "1090926036220061198";
    private static final String OTHER_VISIT = "1100926036220061198";

    @Test
    public void acceptedCodeTurnsRedSignalYellow() {
        assertEquals("3", OptionUnlockPolicy.signalAfterUnlock("1", "1"));
        assertEquals("3", OptionUnlockPolicy.signalAfterUnlock("1", "2"));
        assertEquals("3", OptionUnlockPolicy.signalAfterUnlock("2", "1"));
    }

    @Test
    public void unlockedSignalSurvivesRepeatedApplicationAndOtherLinkedButtons() {
        String signal = OptionUnlockPolicy.signalAfterUnlock("0", "1");
        signal = OptionUnlockPolicy.signalAfterUnlock(signal, "2");
        assertEquals("3", OptionUnlockPolicy.signalAfterUnlock(signal, "0"));
        assertEquals("3", OptionUnlockPolicy.signalAfterUnlock("2", "3"));
    }

    @Test
    public void codeDoesNotTurnAnOrdinarySuccessYellow() {
        assertEquals("2", OptionUnlockPolicy.signalAfterUnlock("2", "2"));
        assertEquals("2", OptionUnlockPolicy.signalAfterUnlock("0", "2"));
        assertEquals("2", OptionUnlockPolicy.signalAfterUnlock(null, null));
    }

    @Test
    public void detachedControlWithoutStoredRowsStillTurnsYellow() {
        assertEquals("3", OptionUnlockPolicy.signalAfterUnlock("1", null));
    }

    @Test
    public void buttonUsesItsControlInEveryExecutionMode() {
        assertEquals("84006", OptionUnlockPolicy.controlId("84007", "84006", "3161"));
    }

    @Test
    public void controlMustNotFollowItsLinkToTheBlockedAction() {
        assertEquals("84006", OptionUnlockPolicy.controlId("84006", "158309", "3148"));
        assertEquals("84932", OptionUnlockPolicy.controlId("84932", "138520", "229"));
        assertEquals("135329", OptionUnlockPolicy.controlId("135329", "158308", "3148"));
    }

    @Test
    public void lookupDoesNotRequireSeparateControlRow() {
        assertEquals("164354", OptionUnlockPolicy.controlId("164355", "164354", "3161"));
    }

    @Test
    public void noControlFallsBackToOwnOptionId() {
        assertEquals("132623", OptionUnlockPolicy.controlId("132623", "0", "3161"));
        assertEquals("132623", OptionUnlockPolicy.controlId("132623", null, "3161"));
        assertEquals("84006", OptionUnlockPolicy.controlId("84006", "0", "3148"));
    }

    @Test
    public void unknownGroupIsNotTreatedAsAButton() {
        assertEquals("84006", OptionUnlockPolicy.controlId("84006", "158309", null));
    }

    @Test
    public void bothControlAndItsButtonAreUpdated() {
        assertTrue(related("84006", "84006", "158309", "3148"));
        assertTrue(related("84006", "84007", "84006", "3161"));
    }

    @Test
    public void allButtonsOfSameControlAreUpdated() {
        assertTrue(related("8299", "138773", "8299", "3161"));
        assertTrue(related("8299", "138766", "8299", "3161"));
    }

    @Test
    public void blockedActionAndUnrelatedControlsStayUntouched() {
        assertFalse(related("84006", "158309", "158608", "3161"));
        assertFalse(related("84006", "138520", "138521", "3161"));
        assertFalse(related("84006", "159707", "84006", "3148"));
    }

    @Test
    public void differentVisitIsNeverUpdated() {
        assertFalse(OptionUnlockPolicy.isRelated(DAD2, "84006", OTHER_VISIT,
                "84007", "84006", "3161"));
        assertFalse(OptionUnlockPolicy.isRelated(DAD2, "84006", OTHER_VISIT,
                "84006", "158309", "3148"));
    }

    @Test
    public void preservesExistingServerObjectIdFormat() {
        for (String id : new String[]{"579", "84006", "84007", "159707", "175016"}) {
            String expected = "1" + id.substring(id.length() - 3) + DAD2.substring(1, 5)
                    + DAD2.substring(6, 7) + DAD2.substring(8, 13) + DAD2.substring(14, 19);
            assertEquals(Long.valueOf(expected), OptionUnlockPolicy.objectId(DAD2, id));
        }
    }

    @Test
    public void controlAndLegacyButtonHaveDifferentLookupKeys() {
        assertNotEquals(OptionUnlockPolicy.objectId(DAD2, "84006"),
                OptionUnlockPolicy.objectId(DAD2, "84007"));
        assertNotEquals(OptionUnlockPolicy.objectId(DAD2, "84006"),
                OptionUnlockPolicy.objectId(OTHER_VISIT, "84006"));
    }

    @Test
    public void malformedIdentifiersCannotCreateUnlockKeys() {
        assertNull(OptionUnlockPolicy.objectId(null, "84006"));
        assertNull(OptionUnlockPolicy.objectId("0", "84006"));
        assertNull(OptionUnlockPolicy.objectId("109092603622006119x", "84006"));
        assertNull(OptionUnlockPolicy.objectId(DAD2, null));
        assertNull(OptionUnlockPolicy.objectId(DAD2, "0"));
        assertNull(OptionUnlockPolicy.objectId(DAD2, "12"));
        assertNull(OptionUnlockPolicy.objectId(DAD2, "84x06"));
        assertNull(OptionUnlockPolicy.controlId(null, "0", "3148"));
    }

    private boolean related(String controlId, String id, String reference, String group) {
        return OptionUnlockPolicy.isRelated(DAD2, controlId, DAD2, id, reference, group);
    }
}
