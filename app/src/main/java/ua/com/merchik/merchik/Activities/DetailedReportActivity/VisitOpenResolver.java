package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import java.util.function.LongFunction;

import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

final class VisitOpenResolver {
    private VisitOpenResolver() {
    }

    static WpDataDB resolve(long id, long dad2, LongFunction<WpDataDB> byId,
                            LongFunction<WpDataDB> byDad2) {
        WpDataDB visit = id != 0 ? byId.apply(id) : null;
        if (dad2 <= 0) return visit;
        if (visit != null && visit.getCode_dad2() == dad2) return visit;

        // A cached card can outlive its row ID; never fall back to a different visit.
        visit = byDad2.apply(dad2);
        return visit != null && visit.getCode_dad2() == dad2 ? visit : null;
    }
}
