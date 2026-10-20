package ua.com.merchik.merchik.features.main.componentsUI

internal fun isOneTimeDurationBelowAverage(
    isOneTimePaymentIncrease: Boolean,
    currentMinutes: Double?,
    averageMinutes: Double
): Boolean = isOneTimePaymentIncrease &&
        currentMinutes != null && currentMinutes.isFinite() && currentMinutes >= 0.0 &&
        averageMinutes.isFinite() && averageMinutes > 0.0 &&
        currentMinutes < averageMinutes
