package ua.com.merchik.merchik.Utils.text;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

/** Formats legacy MVS markers without depending on Android or an active Realm session. */
public final class MvsLinkedTextFormatter {
    private MvsLinkedTextFormatter() {
    }

    public static String format(String source, Function<String, String> urlFactory,
                                Consumer<String> diagnostic) {
        if (source == null) {
            report(diagnostic, "text=null");
            return "";
        }
        StringBuilder result = new StringBuilder(source.length());
        int cursor = 0;
        int start;
        while ((start = source.indexOf('{', cursor)) >= 0) {
            result.append(source, cursor, start);
            int end = source.indexOf('}', start + 1);
            int nextStart = source.indexOf('{', start + 1);
            if (nextStart >= 0 && (end < 0 || nextStart < end)) {
                String fragment = source.substring(start, nextStart);
                reportBlock(diagnostic, "unclosed_marker", fragment);
                result.append(escapeHtml(fragment));
                cursor = nextStart;
                continue;
            }
            if (end < 0) {
                String fragment = source.substring(start);
                reportBlock(diagnostic, "unclosed_marker", fragment);
                result.append(escapeHtml(fragment));
                return result.toString();
            }
            String block = source.substring(start, end + 1);
            result.append(formatBlock(block, urlFactory, diagnostic));
            cursor = end + 1;
        }
        return result.append(source, cursor, source.length()).toString();
    }

    private static String formatBlock(String block, Function<String, String> urlFactory,
                                      Consumer<String> diagnostic) {
        // Keep an empty optional platform; three fields are sufficient for MVS navigation.
        String[] fields = block.substring(1, block.length() - 1).split("\\|", -1);
        if (fields.length < 3 || fields.length > 4) {
            reportBlock(diagnostic, "field_count=" + fields.length, block);
            return escapeHtml(block);
        }
        String type = fields[0].trim().toLowerCase(Locale.ROOT);
        String id = fields[1].trim();
        String label = fields[2].trim();
        if (type.isEmpty() || id.isEmpty() || label.isEmpty()) {
            reportBlock(diagnostic, "empty_required_field", block);
            return escapeHtml(block);
        }
        String path = path(type, id);
        if (path == null) {
            reportBlock(diagnostic, "unknown_type", block);
            return escapeHtml(block);
        }
        String url = null;
        try {
            if (urlFactory != null) {
                url = urlFactory.apply(path);
            }
        } catch (RuntimeException error) {
            reportBlock(diagnostic, "url_error=" + error.getClass().getSimpleName(), block);
        }
        if (url == null || url.isEmpty()) {
            reportBlock(diagnostic, "link_unavailable", block);
            return escapeHtml(block);
        }
        return "<a href=\"" + escapeHtml(url) + "\">" + escapeHtml(label) + "</a>";
    }

    private static String path(String type, String id) {
        switch (type) {
            case "фото":
                return "mobile.php?mod=images_view&act=image_rotate&id=" + id;
            case "фото_шахматка_фильтр":
                return "mobile.php?mod=photo&act=grid_table&" + id;
            case "достижение":
                return "mobile.php?mod=images_achieve&act=view&id=" + id;
            case "рекламация":
            case "задача":
                return "mobile.php?mod=reclamation&act=view&id=" + id;
            case "рекламация_1с":
            case "задача_1с":
                return "mobile.php?mod=reclamation&act=reclamation_by_num&doc_num=" + id;
            case "аудио_отчёт":
                return "mobile.php?mod=audio&act=audio_rotate&id=" + id;
            case "детализированный_отчёт":
                return "mobile.php?mod=report_prepare&act=report_by_num&doc_num=" + id;
            case "посещение_фильтр":
            case "товары":
                return "mobile.php?mod=report_prepare&act=prepare_tovar&ppa_only=1&" + id;
            case "документ":
                return "mobile.php?mod=plan&act=open_document_item&code_dad2=" + id;
            case "контенты":
                return "mobile.php?mod=standart&act=view&code_dad2=" + id;
            default:
                return null;
        }
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static void reportBlock(Consumer<String> diagnostic, String reason, String block) {
        report(diagnostic, reason + ", block=" + logValue(block));
    }

    public static String logValue(String value) {
        if (value == null) return "null";
        String shortened = value.length() > 300 ? value.substring(0, 300) + "..." : value;
        return shortened.replace('\n', ' ').replace('\r', ' ');
    }

    private static void report(Consumer<String> diagnostic, String message) {
        if (diagnostic != null) diagnostic.accept(message);
    }
}
