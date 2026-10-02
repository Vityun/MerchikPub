package ua.com.merchik.merchik.Options;

import java.util.UUID;
import androidx.annotation.Keep;

/** Local delivery state, independent of the work-plan upload flag. */
@Keep
public final class ReportConductState {
    @Keep
    public enum Stage { WAITING_CHECK, WAITING_CONFIRMATION, READY, ACCEPTED, REJECTED }

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
