package ua.com.merchik.merchik.ServerExchange;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.Keep;
import ua.com.merchik.merchik.Utils.text.NativeTextAction;
import ua.com.merchik.merchik.Utils.text.NativeTextLink;
import ua.com.merchik.merchik.Utils.text.NativeTextParser;
import ua.com.merchik.merchik.Utils.text.ParsedNativeText;

/** Only a received executor-report failure, not an arbitrary mention of a report. */
@Keep
public final class ReportConductFailure {
    private static final Pattern FAILURE = Pattern.compile(
            "^\\s*\u041f\u0440\u043e\u0432\u0435\u0441\u0442\u0438\\s+\u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442\\s+\u041e\u0442\u0447\u0435\u0442\u0418\u0441\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044f\\s+"
                    + "(\u0410\u041e\u0438-[0-9]{8})\\s+\u043d\u0435\\s+(?:\u0432\u0434\u0430\u043b\u043e\u0441\u044c|\u0432\u0434\u0430\u043b\u043e\u0441\u044f|\u0443\u0434\u0430\u043b\u043e\u0441\u044c)[!.](?:\\s|$)");

    public int userId;
    public int messageId;
    public long messageTime;
    public String document;
    public long dad2;

    public String key() { return userId + ":" + messageId; }

    public boolean matchesVisit(String documentNumber, long visitDad2) {
        return document != null && document.equals(documentNumber) && visitDad2 > 0
                && (dad2 == 0 || dad2 == visitDad2);
    }

    public static ReportConductFailure parse(int currentUser, Integer id, Integer sender,
                                            Integer recipient, Long dt, String message) {
        if (currentUser <= 0 || id == null || id <= 0 || recipient == null || recipient != currentUser
                || sender == null || sender == currentUser || dt == null || dt <= 0 || message == null) return null;
        ParsedNativeText parsed = NativeTextParser.parse(message);
        Matcher match = FAILURE.matcher(parsed.getText().replace('\u00a0', ' '));
        if (!match.find()) return null;
        ReportConductFailure failure = new ReportConductFailure();
        failure.userId = currentUser;
        failure.messageId = id;
        failure.messageTime = dt;
        failure.document = match.group(1);
        for (NativeTextLink link : parsed.getLinks()) {
            if (link.getStart() == match.start(1) && link.getEnd() == match.end(1)
                    && link.getAction().getKind() == NativeTextAction.Kind.VISIT_BY_DAD2) {
                failure.dad2 = Long.parseLong(link.getAction().getValue());
            }
        }
        return failure;
    }
}
