package ua.com.merchik.merchik.ServerExchange;

import org.junit.Test;

import static org.junit.Assert.*;

public class PhotoCommentUploadPolicyTest {
    @Test public void cannotSendWithoutLoggedInUser() {
        PhotoCommentUploadPolicy policy = new PhotoCommentUploadPolicy();
        assertEquals(0, policy.start(100));
        policy.useUser(-1);
        assertEquals(0, policy.start(100));
    }

    @Test public void editServerIdAndCronCannotStartParallelRequests() {
        PhotoCommentUploadPolicy policy = policy();
        long first = policy.start(100);
        assertTrue(first > 0);
        assertEquals(0, policy.start(101));
        assertEquals(0, policy.start(102));
        assertTrue(policy.owns(first));
    }

    @Test public void successfulAckAllowsImmediateUploadOfNewerEdit() {
        PhotoCommentUploadPolicy policy = policy();
        long first = policy.start(100);
        assertTrue(policy.finish(first, true, 200));
        assertEquals(0, policy.remaining(200));
        long second = policy.start(200);
        assertTrue(second > first);
        assertTrue(policy.owns(second));
    }

    @Test public void networkFailureOrPartialAckWaitsThirtySeconds() {
        PhotoCommentUploadPolicy policy = policy();
        assertTrue(policy.finish(policy.start(100), false, 200));
        assertFalse(policy.isRunning());
        assertEquals(30_000, policy.remaining(200));
        assertEquals(0, policy.start(201));
        assertEquals(0, policy.start(30_199));
        assertTrue(policy.start(30_200) > 0);
    }

    @Test public void repeatedTriggersDoNotPostponeRetry() {
        PhotoCommentUploadPolicy policy = policy();
        policy.finish(policy.start(0), false, 100);
        assertFalse(policy.useUser(246665));
        assertEquals(0, policy.start(20_100));
        assertEquals(10_000, policy.remaining(20_100));
        assertTrue(policy.start(30_100) > 0);
    }

    @Test public void duplicateCallbackCannotReleaseNewRequest() {
        PhotoCommentUploadPolicy policy = policy();
        long old = policy.start(100);
        policy.finish(old, true, 200);
        long current = policy.start(200);
        assertFalse(policy.finish(old, false, 201));
        assertTrue(policy.owns(current));
        assertEquals(0, policy.remaining(201));
    }

    @Test public void accountSwitchIgnoresOldAckAndResetsRetry() {
        PhotoCommentUploadPolicy policy = policy();
        long old = policy.start(100);
        assertTrue(policy.useUser(42));
        assertFalse(policy.owns(old));
        long current = policy.start(101);
        assertFalse(policy.finish(old, false, 102));
        assertTrue(policy.owns(current));
        policy.finish(current, false, 200);
        assertTrue(policy.useUser(246665));
        assertEquals(0, policy.remaining(201));
        assertTrue(policy.start(201) > 0);
    }

    @Test public void logoutInvalidatesInFlightRequest() {
        PhotoCommentUploadPolicy policy = policy();
        long token = policy.start(100);
        policy.useUser(0);
        assertFalse(policy.finish(token, true, 200));
        assertEquals(0, policy.start(200));
    }

    private static PhotoCommentUploadPolicy policy() {
        PhotoCommentUploadPolicy policy = new PhotoCommentUploadPolicy();
        policy.useUser(246665);
        return policy;
    }
}
