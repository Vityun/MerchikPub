package ua.com.merchik.merchik.Utils;

public final class DistanceValue {
    private DistanceValue() { }

    public static int metres(double value, boolean kilometres) {
        double metres = value * (kilometres ? 1000.0 : 1.0);
        if (!Double.isFinite(metres) || metres < 0 || metres > Integer.MAX_VALUE) return -1;
        return (int) Math.round(metres);
    }
}
