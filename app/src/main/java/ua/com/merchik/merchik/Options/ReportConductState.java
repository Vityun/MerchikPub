package ua.com.merchik.merchik.Options;

import java.util.UUID;
import androidx.annotation.Keep;
import ua.com.merchik.merchik.Utils.WorkTimeRules;

/** Local delivery state, independent of the work-plan upload flag. */
@Keep
public final class ReportConductState {
    @Keep
    public enum Stage { WAITING_CHECK, WAITING_CONFIRMATION, FINALIZING_WORK_END, READY, ACCEPTED, REJECTED }

    public int userId;
    public long dad2;
    public String revision = UUID.randomUUID().toString();
    public Stage stage = Stage.WAITING_CHECK;
    public long retryAfter;
    public String message = "";
    public boolean resultPending;
    public long visitStart;
    public long visitEnd;
    public long clientStart;
    public long clientEnd;
    public boolean workDataUploaded;
    public boolean workEndRequested;
    public Long requestedWorkEnd;
    public boolean conductAfterWorkEnd;

    public void requestWorkEnd(long visitStart, long clientStart, Long requestedEnd) {
        if (!WorkTimeRules.consistent(visitStart, clientStart, 0, 0)) {
            throw new IllegalStateException("Inconsistent work start");
        }
        if (visitStart <= 0) throw new IllegalStateException("Work has not started");
        this.visitStart = visitStart;
        this.clientStart = clientStart;
        workEndRequested = true;
        requestedWorkEnd = requestedEnd;
    }

    public boolean matchesUnfinishedWork(long visitStart, long visitEnd, long clientStart, long clientEnd) {
        return workEndRequested && this.visitStart == visitStart && this.clientStart == clientStart
                && visitEnd == 0 && clientEnd == 0;
    }

    public long resolveWorkEnd(long nowSeconds) {
        long end = requestedWorkEnd != null ? requestedWorkEnd : nowSeconds;
        if (WorkTimeRules.seconds(end) != end) throw new IllegalStateException("Work end must be seconds");
        if (end < visitStart || end < clientStart) {
            throw new IllegalStateException("Work end precedes work start");
        }
        return end;
    }

    public void prepareWorkEnd(long nowSeconds, boolean conductAfterWorkEnd) {
        if (!workEndRequested || !awaitingDecision()) {
            throw new IllegalStateException("Work-end decision is no longer active");
        }
        visitEnd = clientEnd = resolveWorkEnd(nowSeconds);
        this.conductAfterWorkEnd = conductAfterWorkEnd;
        stage = Stage.FINALIZING_WORK_END;
    }

    public boolean canApplyWorkEnd(long visitStart, long visitEnd, long clientStart, long clientEnd) {
        return stage == Stage.FINALIZING_WORK_END
                && (matchesUnfinishedWork(visitStart, visitEnd, clientStart, clientEnd)
                || matchesWorkTimes(visitStart, visitEnd, clientStart, clientEnd));
    }

    public void workEndSaved() {
        if (stage != Stage.FINALIZING_WORK_END) throw new IllegalStateException("Work end was not authorized");
        workEndRequested = false;
        requestedWorkEnd = null;
        workDataUploaded = false;
        stage = Stage.WAITING_CHECK;
        if (conductAfterWorkEnd) approve();
    }

    public boolean matchesWorkTimes(long visitStart, long visitEnd, long clientStart, long clientEnd) {
        return this.visitStart == visitStart && this.visitEnd == visitEnd
                && this.clientStart == clientStart && this.clientEnd == clientEnd;
    }

    public boolean awaitingDecision() {
        return stage == Stage.WAITING_CHECK || stage == Stage.WAITING_CONFIRMATION;
    }

    public boolean canSend(long now) {
        return stage == Stage.READY && retryAfter <= now;
    }

    public void approve() {
        if (workEndRequested) throw new IllegalStateException("Work end must be committed before delivery");
        stage = Stage.READY;
        retryAfter = 0;
        message = "";
        resultPending = false;
    }

    public void complete(boolean accepted, String message) {
        stage = accepted ? Stage.ACCEPTED : Stage.REJECTED;
        this.message = message == null ? "" : message;
        resultPending = true;
    }
}
