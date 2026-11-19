package ua.com.merchik.merchik.features.main.options;

/** Shared numeric rules for the photo counter and the end of a capture session. */
public final class PhotoReportCountPolicy {
    private PhotoReportCountPolicy() { }

    public static boolean isWarehouseOption(String optionId, String controlId) {
        return "141360".equals(optionId) || "141361".equals(optionId)
                || "141361".equals(controlId);
    }

    public static boolean isEmptyWarehouseSample(String sampleId) {
        return "78".equals(sampleId) || "94".equals(sampleId);
    }

    public static int warehouseMinimum(int regularMinimum, long emptyWarehousePhotoCount) {
        return emptyWarehousePhotoCount > 0 ? 2 : regularMinimum;
    }

    public static int defaultMinimum(String optionId, String controlId) {
        if ("151139".equals(optionId) || "164351".equals(optionId)
                || "164351".equals(controlId) || "169108".equals(optionId)) return 1;
        if ("164355".equals(optionId)) return 0;
        if ("132969".equals(optionId) || "135158".equals(optionId)
                || "174213".equals(optionId) || "141360".equals(optionId)) return 1;
        return 3;
    }

    public static int adjustedMinimum(String optionId, int minimum, Integer sku,
                                      Integer exactRequiredCount) {
        if (exactRequiredCount != null) return Math.max(0, exactRequiredCount);
        if (!"135158".equals(optionId) && !"141360".equals(optionId)
                && !"132969".equals(optionId) && (sku == null || sku >= 5)) minimum++;
        if ("158308".equals(optionId)) minimum++;
        return minimum;
    }

    public static int reportPhotoType(String optionId) {
        if (optionId == null) return -1;
        switch (optionId) {
            case "132968": case "158308": return 0;
            case "151139": case "164355": return 5;
            case "132969": return 10;
            case "135809": return 14;
            case "141360": return 31;
            case "158606": return 36;
            case "159725": case "159726": return 37;
            case "158309": return 39;
            case "158605": return 40;
            case "158604": return 41;
            case "157354": return 42;
            case "164351": return 45;
            case "165481": return 46;
            case "169108": return 47;
            case "172100": return 48;
            case "174213": return 49;
            default: return -1;
        }
    }

    public static boolean isReportSession(String contextName, boolean referenceSelection,
                                           String productId) {
        return contextName != null && contextName.startsWith("SAMPLE_PHOTO_FROM_OPTION_")
                && !referenceSelection
                && (productId == null || productId.trim().isEmpty() || "0".equals(productId.trim()));
    }

    public static boolean shouldClose(boolean saved, long visitDad2, long photoDad2,
                                      int expectedType, int actualType,
                                      int readyCount, int minimum) {
        return saved && visitDad2 > 0 && visitDad2 == photoDad2
                && expectedType >= 0 && expectedType == actualType
                && minimum >= 0 && readyCount > 0 && readyCount >= minimum;
    }
}
