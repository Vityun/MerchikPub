package ua.com.merchik.merchik.retrofit;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sun.management.ThreadMXBean;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Locale;

import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportPrepareServer;

/** Standalone JVM benchmark, run in separate before/after processes with identical heap settings. */
public final class ReportPrepareJsonBenchmark {
    private static volatile ReportPrepareServer retained;
    private static volatile long checksum;

    public static void main(String[] args) throws Exception {
        String version = args[0];
        int rows = Integer.parseInt(args[1]);
        String mode = args[2];
        boolean rounded = "after".equals(version);
        int warmups = 5;
        int runs = 9;
        Gson gson = ReportPrepareCountJsonTest.gson();
        String json = ReportPrepareJsonFixtures.response(rows, mode);
        ThreadMXBean threads = (ThreadMXBean) ManagementFactory.getThreadMXBean();
        if (!threads.isThreadAllocatedMemorySupported()) throw new IllegalStateException("Allocation counter unavailable");
        threads.setThreadAllocatedMemoryEnabled(true);
        long threadId = Thread.currentThread().getId();

        try {
            for (int i = 0; i < warmups; i++) {
                retained = gson.fromJson(json, ReportPrepareServer.class);
                checksum = ReportPrepareJsonFixtures.verify(retained, rows, mode, rounded);
            }
        } catch (JsonParseException e) {
            if (rounded || !"fractional".equals(mode)) throw e;
            System.out.printf(Locale.ROOT, "{\"version\":\"%s\",\"rows\":%d,\"mode\":\"%s\",\"status\":\"rejected\"}%n",
                    version, rows, mode);
            return;
        }

        double[] millis = new double[runs];
        double[] allocated = new double[runs];
        double[] liveHeap = new double[runs];
        for (int i = 0; i < runs; i++) {
            retained = null;
            collect();
            long heapBefore = heapUsed();
            long bytesBefore = threads.getThreadAllocatedBytes(threadId);
            long start = System.nanoTime();
            retained = gson.fromJson(json, ReportPrepareServer.class);
            millis[i] = (System.nanoTime() - start) / 1_000_000.0;
            allocated[i] = (threads.getThreadAllocatedBytes(threadId) - bytesBefore) / 1048576.0;
            checksum = ReportPrepareJsonFixtures.verify(retained, rows, mode, rounded);
            collect();
            liveHeap[i] = (heapUsed() - heapBefore) / 1048576.0;
        }
        System.out.printf(Locale.ROOT,
                "{\"version\":\"%s\",\"rows\":%d,\"mode\":\"%s\",\"status\":\"ok\",\"jsonMiB\":%.2f,"
                        + "\"warmups\":%d,\"runs\":%d,\"medianMs\":%.2f,\"minMs\":%.2f,\"maxMs\":%.2f,"
                        + "\"allocatedMiB\":%.2f,\"liveHeapMiB\":%.2f,\"checksum\":%d}%n",
                version, rows, mode, json.length() / 1048576.0, warmups, runs,
                median(millis), Arrays.stream(millis).min().getAsDouble(), Arrays.stream(millis).max().getAsDouble(),
                median(allocated), median(liveHeap), checksum);
    }

    private static double median(double[] data) {
        double[] sorted = data.clone();
        Arrays.sort(sorted);
        return sorted[sorted.length / 2];
    }

    private static long heapUsed() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    private static void collect() throws InterruptedException {
        System.gc();
        Thread.sleep(20);
    }
}
