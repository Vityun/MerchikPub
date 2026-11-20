# Standalone JVM benchmarks

This directory is not an Android or Gradle test source set. The benchmarks require a full JDK 17+ with `jdk.management` for the per-thread allocation counter.

Compile `java/` with `javac` using the app's compiled model and adapter classes, Gson, and the compiled `ReportPrepareCountJsonTest` and `ReportPrepareJsonFixtures` classes on the classpath. Run the benchmark classes by their fully qualified names:

- `ua.com.merchik.merchik.retrofit.ReportPrepareJsonBenchmark`: `version rows mode`
- `ua.com.merchik.merchik.retrofit.ReportPrepareJsonPairedBenchmark`: `beforeClasses afterClasses rows mode pairs fork`. The `afterClasses` directory must also contain the compiled benchmark and fixture classes for classloader isolation.
