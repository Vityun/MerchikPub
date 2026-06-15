package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers;


public class FaceSaveCheckResult {
    public final boolean success;
    public final String error;

    private FaceSaveCheckResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public static FaceSaveCheckResult success() {
        return new FaceSaveCheckResult(true, null);
    }

    public static FaceSaveCheckResult error(String error) {
        return new FaceSaveCheckResult(false, error);
    }
}


