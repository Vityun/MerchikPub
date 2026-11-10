package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.merchik.merchik.Global.UnlockCode;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.dataLayer.ContextUI;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.room.DaoInterfaces.ShelfSizeDao;
import ua.com.merchik.merchik.features.main.options.ProductIssueCorrection;

public final class OptionControlShelfLength extends OptionControl {
    private final long dad2;

    public OptionControlShelfLength(Context context, Object document, OptionsDB option, OptionMassageType type,
                                    Options.NNKMode mode, UnlockCodeResultListener listener) {
        if (!(document instanceof WpDataDB)) throw new IllegalArgumentException("Shelf length control requires a visit");
        this.document = document;
        this.context = context;
        this.optionDB = option;
        this.msgType = type;
        this.nnkMode = mode;
        this.unlockCodeResultListener = listener;
        this.wpDataDB = (WpDataDB) document;
        this.dad2 = wpDataDB.getCode_dad2();
        notCloseSpannableStringBuilderDialog = true;
        executeOption();
    }

    // 2.0. Свернуть по товару; обычные товары не являются строками замера.
    static Map<String, ShelfLengthPolicy.Measurement> measurements(List<ReportPrepareDB> rows, Map<String, TovarDB> cards,
                                                                  String client, String mainOptionId) {
        Map<String, ShelfLengthPolicy.Measurement> result = new LinkedHashMap<>();
        for (ReportPrepareDB row : rows) {
            String id = row.getTovarId();
            TovarDB product = cards.get(id);
            if (!ShelfLengthEntry.eligible(client, mainOptionId, row, product)) continue;
            ShelfLengthPolicy.Measurement measurement = result.get(id);
            if (measurement == null) {
                measurement = new ShelfLengthPolicy.Measurement(catalogGroupId(product));
                result.put(id, measurement);
            }
            measurement.add(row.getFace(), row.getAmount());
        }
        return result;
    }

    private static Integer catalogGroupId(TovarDB product) {
        if (product == null || product.getGroupId() == null) return null;
        try {
            int id = Integer.parseInt(product.getGroupId().trim());
            return id > 0 ? id : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Map<String, ShelfLengthPolicy.Measurement> measurements(WpDataDB visit, List<ReportPrepareDB> rows) {
        return measurements(rows, ShelfLengthEntry.cards(rows), visit.getClient_id(), visit.getMain_option_id());
    }

    public static String counterText(WpDataDB visit) {
        List<ReportPrepareDB> rows = ReportPrepareRealm.getReportPrepareByDad2_LIST(visit.getCode_dad2());
        return ShelfLengthPolicy.progress(measurements(visit, rows).values());
    }

    private void executeOption() {
        String client = wpDataDB.getClient_id();
        boolean hasClient = client != null && !client.isEmpty() && !"0".equals(client);
        List<ReportPrepareDB> rows = ReportPrepareRealm.getReportPrepareByDad2_LIST(dad2);
        Map<String, ShelfLengthPolicy.Measurement> products = measurements(wpDataDB, rows);
        Set<Integer> groups = new LinkedHashSet<>();
        for (ShelfLengthPolicy.Measurement m : products.values()) {
            if (m.groupId != null) groups.add(m.groupId);
        }

        // 4.1. Вся доступная история сети, по дату документа включительно.
        Map<Integer, String> names = new HashMap<>();
        Map<Integer, ShelfLengthPolicy.History> history = new HashMap<>();
        AddressSDB address = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
        int network = address == null || address.tpId == null ? 0 : address.tpId;
        long end = wpDataDB.getDt() == null ? 0 : ShelfLengthPolicy.endExclusive(wpDataDB.getDt().getTime());
        if (!groups.isEmpty()) {
            List<Integer> groupIds = new ArrayList<>(groups);
            for (TovarGroupSDB group : SQL_DB.tovarGroupDao().getAllByIds(groupIds)) names.put(group.id, group.nm);
            if (network > 0 && end > 0) {
                for (ShelfSizeDao.WidthCount row : SQL_DB.shelfSizeDao().getHistoryForShelfLength(network, groupIds,
                        end, wpDataDB.getUser_id(), ShelfLengthPolicy.ROBOT_ID)) {
                    history.computeIfAbsent(row.groupId, ignored -> new ShelfLengthPolicy.History()).add(row.width, row.measurements);
                }
            }
        }

        int issues = 0;
        SpannableStringBuilder details = new SpannableStringBuilder();
        for (Map.Entry<String, ShelfLengthPolicy.Measurement> entry : products.entrySet()) {
            ShelfLengthPolicy.Measurement m = entry.getValue();
            String label = m.groupLabel(names.get(m.groupId));
            ShelfLengthPolicy.History h = history.get(m.groupId);
            String issue = m.issue(label, h);
            Globals.writeToMLOG("INFO", "OptionControlShelfLength/item", "dad2=" + dad2 + ", product=" + entry.getKey()
                    + ", group=" + (m.groupId == null ? ShelfLengthPolicy.UNKNOWN_GROUP : m.groupId) + ", faces=" + m.faces + ", metres=" + m.metres
                    + ", historyCount=" + (h == null ? 0 : h.count)
                    + ", historyTotal=" + (h == null ? 0 : h.total)
                    + ", averageMetres=" + (h == null ? "n/a" : h.total.divide(BigDecimal.valueOf(h.count), 3, RoundingMode.HALF_UP))
                    + ", issue=" + !issue.isEmpty());
            if (issue.isEmpty()) continue;
            issues++;
            details.append("\n");
            int start = details.length() + issue.indexOf(label);
            details.append(issue);
            details.setSpan(new ClickableSpan() {
                @Override public void onClick(View view) {
                    ProductIssueCorrection.open(view, dad2, wpDataDB.getClient_id(),
                            Collections.singletonList(entry.getKey()), issue, () -> {
                                executeOption();
                                refreshOpenSpannableOptionMessage();
                            }, ContextUI.TOVAR_FROM_OPTION_1465);
                }
            }, start, start + label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 5.0–8.0. Сигнал, блокировка по настройке и сохранение результата.
        boolean signal = ShelfLengthPolicy.signal(hasClient, rows.size(), issues);
        spannableStringBuilder.clear();
        spannableStringBuilder.clearSpans();
        spannableStringBuilder.append(ShelfLengthPolicy.summary(hasClient, rows.size(), products.size(), issues)).append(details);
        String notes = spannableStringBuilder.toString();
        RealmManager.INSTANCE.executeTransaction(realm -> {
            optionDB.setNotes(notes);
            optionDB.setIsSignal(signal ? "1" : "2");
            optionDB.setSumPenalty(signal ? String.valueOf(calculatePenalty(wpDataDB)) : "0.00");
            realm.insertOrUpdate(optionDB);
        });
        boolean configured = "1".equals(optionDB.getBlockPns());
        boolean unlocked = signal && configured && new UnlockCode().applyStoredUnlockCode(optionDB);
        setIsBlockOption(signal && configured && !unlocked);
        if (signal) {
            if (unlocked) spannableStringBuilder.append("\n\nАле виконавцю видано код розблокування!");
            else if (configured) spannableStringBuilder.append(wpDataDB.getStatus() == 1
                    ? "\n\nДокумент будет ДЕпроведен!" : "\n\nДокумент проведеним не буде!");
            else spannableStringBuilder.append("\n\nВи можете отримати БІЛЬШІ преміальні, якщо будете вносити корректно ДПП.");
        }
        Globals.writeToMLOG("INFO", "OptionControlShelfLength/result", "dad2=" + dad2 + ", network=" + network
                + ", endExclusive=" + end + ", executor=" + wpDataDB.getUser_id() + ", reportRows=" + rows.size()
                + ", measurements=" + products.size() + ", issues=" + issues + ", signal=" + signal + ", blocked=" + isBlockOption());
    }

}
