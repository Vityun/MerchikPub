package ua.com.merchik.merchik.Global;

public final class OptionUnlockPolicy {
    public static final String BUTTON_GROUP = "3161";

    private OptionUnlockPolicy() {
    }

    public static String controlId(String optionId, String optionControlId, String optionGroup) {
        // On a control row optionControlId names an action it blocks, not its own control.
        if (BUTTON_GROUP.equals(optionGroup) && isPositiveId(optionControlId)) {
            return optionControlId;
        }
        return isPositiveId(optionId) ? optionId : null;
    }

    public static boolean isRelated(String dad2, String controlId, String rowDad2,
                                    String rowOptionId, String rowControlId, String rowGroup) {
        return dad2 != null && dad2.equals(rowDad2) && controlId != null
                && controlId.equals(controlId(rowOptionId, rowControlId, rowGroup));
    }

    public static Long objectId(String dad2, String optionId) {
        if (dad2 == null || dad2.length() != 19 || !isPositiveId(dad2)
                || !isPositiveId(optionId) || optionId.length() < 3) return null;
        // Keep the server's existing ODAD format, including its option-id suffix.
        String value = "1" + optionId.substring(optionId.length() - 3)
                + dad2.substring(1, 5) + dad2.substring(6, 7)
                + dad2.substring(8, 13) + dad2.substring(14, 19);
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static boolean isPositiveId(String value) {
        if (value == null || value.isEmpty()) return false;
        boolean positive = false;
        for (int i = 0; i < value.length(); i++) {
            char digit = value.charAt(i);
            if (digit < '0' || digit > '9') return false;
            positive |= digit != '0';
        }
        return positive;
    }
}
