package ua.com.merchik.merchik.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.management.ThreadMXBean;

import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportPrepareServer;

/** Standalone A/B check using two compiled adapter versions, never an Android/Realm test. */
public final class ReportPrepareJsonPairedBenchmark {
    private static final String ROOT = "ua.com.merchik.merchik.";
    private static final List<GarbageCollectorMXBean> COLLECTORS =
            ManagementFactory.getGarbageCollectorMXBeans();
    private static volatile Object retained;
    private static volatile long checksum;

    public static void main(String[] args) throws Exception {
        URL before = new File(args[0]).toURI().toURL();
        URL after = new File(args[1]).toURI().toURL();
        int rows = Integer.parseInt(args[2]);
        String mode = args[3];
        int pairs = Integer.parseInt(args[4]);
        int fork = Integer.parseInt(args[5]);
        String json = ReportPrepareJsonFixtures.response(rows, mode);
        ThreadMXBean threads = (ThreadMXBean) ManagementFactory.getThreadMXBean();
        threads.setThreadAllocatedMemoryEnabled(true);
        threads.setThreadCpuTimeEnabled(true);
        Gson output = new Gson();

        try (Variant oldVersion = new Variant(new URL[]{before, after});
             Variant newVersion = new Variant(new URL[]{after})) {
            if (oldVersion.source.equals(newVersion.source)) {
                throw new AssertionError("Both variants loaded the same adapter");
            }
            System.out.println("# before=" + oldVersion.source + " after=" + newVersion.source);
            // Warm both variants and alternate which one runs first in each pair.
            for (int pair = -8; pair < pairs; pair++) {
                boolean oldFirst = (pair + fork) % 2 == 0;
                Sample first = measure(oldFirst ? oldVersion : newVersion, json, rows, mode, threads);
                Sample second = measure(oldFirst ? newVersion : oldVersion, json, rows, mode, threads);
                if (pair >= 0) {
                    Pair result = new Pair();
                    result.fork = fork;
                    result.pair = pair;
                    result.rows = rows;
                    result.mode = mode;
                    result.beforeFirst = oldFirst;
                    result.before = oldFirst ? first : second;
                    result.after = oldFirst ? second : first;
                    if (result.before.checksum != result.after.checksum) {
                        throw new AssertionError("Different parsed data");
                    }
                    System.out.println(output.toJson(result));
                }
            }
        }
    }

    private static Sample measure(Variant variant, String json, int rows, String mode,
                                  ThreadMXBean threads) throws Exception {
        retained = null;
        System.gc();
        Thread.sleep(20);
        long gcCount = gc(false);
        long gcMillis = gc(true);
        long thread = Thread.currentThread().getId();
        long cpu = threads.getCurrentThreadCpuTime();
        long allocated = threads.getThreadAllocatedBytes(thread);
        long start = System.nanoTime();
        retained = variant.parse.invoke(variant.pipeline, json);
        long elapsed = System.nanoTime() - start;
        long allocation = threads.getThreadAllocatedBytes(thread) - allocated;
        long cpuElapsed = threads.getCurrentThreadCpuTime() - cpu;
        Sample result = new Sample();
        result.wallMs = elapsed / 1_000_000.0;
        result.cpuMs = cpuElapsed / 1_000_000.0;
        result.allocatedMiB = allocation / 1048576.0;
        result.gcCount = gc(false) - gcCount;
        result.gcMs = gc(true) - gcMillis;
        checksum = (Long) variant.verify.invoke(variant.pipeline, retained, rows, mode);
        result.checksum = checksum;
        retained = null;
        return result;
    }

    private static long gc(boolean time) {
        long total = 0;
        for (GarbageCollectorMXBean collector : COLLECTORS) {
            total += time ? collector.getCollectionTime() : collector.getCollectionCount();
        }
        return total;
    }

    private static final class Variant implements AutoCloseable {
        final URLClassLoader loader;
        final Object pipeline;
        final Method parse;
        final Method verify;
        final String source;

        Variant(URL[] paths) throws Exception {
            loader = new URLClassLoader(paths, ReportPrepareJsonPairedBenchmark.class.getClassLoader()) {
                @Override
                protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                    synchronized (getClassLoadingLock(name)) {
                        Class<?> type = findLoadedClass(name);
                        if (type == null) {
                            type = isolated(name) ? findClass(name) : super.loadClass(name, false);
                        }
                        if (resolve) resolveClass(type);
                        return type;
                    }
                }
            };
            Class<?> type = loader.loadClass(ReportPrepareJsonPairedBenchmark.class.getName() + "$Pipeline");
            pipeline = type.getConstructor().newInstance();
            parse = type.getMethod("parse", String.class);
            verify = type.getMethod("verify", Object.class, int.class, String.class);
            Class<?> factory = loader.loadClass(ROOT + "retrofit.RoundedCountAdapterFactory");
            if (factory.getClassLoader() != loader) throw new AssertionError("Adapter not isolated");
            source = factory.getProtectionDomain().getCodeSource().getLocation().toString();
        }

        @Override
        public void close() throws Exception {
            loader.close();
        }
    }

    private static boolean isolated(String name) {
        return name.startsWith(ROOT + "retrofit.RoundedCountAdapterFactory")
                || name.equals(ROOT + "retrofit.ReportPrepareJsonPairedBenchmark$Pipeline")
                || name.equals(ROOT + "retrofit.ReportPrepareJsonFixtures")
                || name.equals(ROOT + "data.RealmModels.ReportPrepareDB")
                || name.equals(ROOT + "data.RetrofitResponse.models.ReportPrepareServer");
    }

    public static final class Pipeline {
        private final Gson gson = new GsonBuilder()
                .registerTypeAdapter(String.class, new CleanStringAdapter())
                .registerTypeAdapter(int.class, new FlexibleIntegerTypeAdapter(0))
                .registerTypeAdapter(Integer.class, new FlexibleIntegerTypeAdapter(null))
                .registerTypeAdapter(long.class, new FlexibleLongTypeAdapter(0L))
                .registerTypeAdapter(Long.class, new FlexibleLongTypeAdapter(null))
                .create();

        public Object parse(String json) {
            return gson.fromJson(json, ReportPrepareServer.class);
        }

        public long verify(Object response, int rows, String mode) {
            return ReportPrepareJsonFixtures.verify((ReportPrepareServer) response, rows, mode, true);
        }
    }

    private static final class Sample {
        double wallMs;
        double cpuMs;
        double allocatedMiB;
        long gcCount;
        long gcMs;
        long checksum;
    }

    private static final class Pair {
        int fork;
        int pair;
        int rows;
        String mode;
        boolean beforeFirst;
        Sample before;
        Sample after;
    }
}
