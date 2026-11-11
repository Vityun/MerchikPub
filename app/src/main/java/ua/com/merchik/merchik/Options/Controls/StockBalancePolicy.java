package ua.com.merchik.merchik.Options.Controls;

final class StockBalancePolicy {
    private StockBalancePolicy() { }

    static long cutoffSeconds(long nowMillis) {
        return nowMillis / 1000L - 20L * 24 * 60 * 60;
    }

    static double average(long amount, int presentSku) {
        return presentSku == 0 ? 0 : (double) amount / presentSku;
    }

    static boolean belowMinimum(int stock, int minimum, double average) {
        return stock < (minimum > 0 ? minimum : average / 10.0);
    }
}
