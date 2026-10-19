package ua.com.merchik.merchik.Utils;

/** Work times are seconds; each visit/client pair represents one event. */
public final class WorkTimeRules {
    private static final long MAX_SECONDS = 253402300799L;

    private WorkTimeRules() { }

    public static final class Times {
        public final long start;
        public final long end;

        private Times(long start, long end) {
            if (end > 0 && (start == 0 || end < start)) {
                throw new IllegalArgumentException("Work end has no valid preceding start");
            }
            this.start = start;
            this.end = end;
        }

        public long duration() { return end == 0 ? 0 : end - start; }

        public boolean matches(long visitStart, long clientStart, long visitEnd, long clientEnd) {
            return start == visitStart && start == clientStart && end == visitEnd && end == clientEnd;
        }
    }

    public static long seconds(long value) {
        if (value < 0) throw new IllegalArgumentException("Negative work time");
        if (value > MAX_SECONDS) value /= 1000;
        if (value > MAX_SECONDS) throw new IllegalArgumentException("Invalid work timestamp");
        return value;
    }

    public static Times normalize(long visitStart, long clientStart, long visitEnd, long clientEnd) {
        return new Times(pair(visitStart, clientStart), pair(visitEnd, clientEnd));
    }

    private static long pair(long visit, long client) {
        long v = seconds(visit);
        long c = seconds(client);
        return v > 0 ? v : c;
    }

    public static boolean consistent(long visitStart, long clientStart, long visitEnd, long clientEnd) {
        try {
            return normalize(visitStart, clientStart, visitEnd, clientEnd)
                    .matches(visitStart, clientStart, visitEnd, clientEnd);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static Times merge(Times local, Times incoming, boolean pendingLocal, boolean olderServer,
                              boolean conflictingStart, boolean conflictingEnd) {
        if (local == null) return incoming;
        if (pendingLocal || olderServer) return local;
        long start = local.start > 0 && (incoming.start == 0 || conflictingStart) ? local.start : incoming.start;
        long end = local.end > 0 && (incoming.end == 0 || conflictingEnd) ? local.end : incoming.end;
        // Do not combine a retained end with a server start that makes the interval invalid.
        if (end > 0 && (start == 0 || end < start)) return local;
        return new Times(start, end);
    }
}
