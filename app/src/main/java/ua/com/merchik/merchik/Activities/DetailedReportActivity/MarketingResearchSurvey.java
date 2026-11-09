package ua.com.merchik.merchik.Activities.DetailedReportActivity;

/** In-memory answers only; no changes to visit data or exchange payloads. */
public final class MarketingResearchSurvey {
    public static final int THEME_ID = 1182;

    public enum Question { REQUIRED, INTENTION, WARNING, NONE }

    private Boolean merchandisingRequired;
    private Boolean willMerchandise;
    private boolean warningAcknowledged;
    private boolean dismissed;

    public static boolean supportsTheme(int themeId) {
        return themeId == THEME_ID;
    }

    public Question nextQuestion() {
        if (dismissed) return Question.NONE;
        if (merchandisingRequired == null) return Question.REQUIRED;
        if (merchandisingRequired && willMerchandise == null) return Question.INTENTION;
        if (Boolean.TRUE.equals(merchandisingRequired) && Boolean.TRUE.equals(willMerchandise)
                && !warningAcknowledged) return Question.WARNING;
        return Question.NONE;
    }

    public void answer(Question question, boolean yes) {
        if (question == Question.NONE || question != nextQuestion()) return;
        if (question == Question.REQUIRED) merchandisingRequired = yes;
        else if (question == Question.INTENTION) willMerchandise = yes;
    }

    public void acknowledgeWarning() {
        if (nextQuestion() == Question.WARNING) warningAcknowledged = true;
    }

    public void dismiss() {
        dismissed = true;
    }

    public Boolean getMerchandisingRequired() {
        return merchandisingRequired;
    }

    public Boolean getWillMerchandise() {
        return willMerchandise;
    }
}
