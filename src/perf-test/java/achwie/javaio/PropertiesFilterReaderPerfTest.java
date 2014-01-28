package achwie.javaio;

import static java.lang.String.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Compares the performance of the {@link PropertiesFilterReader} to other methods of replacing properties.
 * 
 * @author Achim Wiedemann, Jan 27, 2014
 * 
 */
public class PropertiesFilterReaderPerfTest {
  private boolean logEnabled = true;

  public static void main(String[] args) throws Exception {
    // To run with VisualVM:
    // System.out.println("Press a key to start performance test");
    // System.in.read();

    final int oneMegabyte = 1 * 1024 * 1024;
    final int magnitudes = 3;

    final PropertiesFilterReaderPerfTest test = new PropertiesFilterReaderPerfTest();

    final List<PerfTest> tests = new ArrayList<>();
    tests.add(new ReplaceInMemoryTest());
    tests.add(new ReplaceWithPropertiesFilterReader());

    final List<TestRunResult> results = new ArrayList<>();
    for (int i = 0; i < magnitudes; i++) {
      for (int j = 0; j < magnitudes; j++) {
        final int numProperties = ((int) Math.pow(10, i));
        final int fileSizeBytes = ((int) Math.pow(10, i)) * oneMegabyte;

        results.addAll(test.testRun(numProperties, fileSizeBytes, tests));
      }
    }

    System.out.println("Test run results:");
    for (TestRunResult r : results)
      System.out.println("  " + r);
  }

  public List<TestRunResult> testRun(int numProperties, int testFileSize, List<PerfTest> tests) throws Exception {
    final String inFilename = "test-in.txt";

    log("Running perf test with %d properties in a file of %d bytes.", numProperties, testFileSize);

    final List<String> propertyNames = new ArrayList<>();
    for (int i = 0; i < numProperties; i++)
      propertyNames.add(String.format("${property%d}", i));

    final Properties replacements = new Properties();
    for (int i = 0; i < propertyNames.size(); i++)
      replacements.put(propertyNames.get(i), String.format("replacement%d", i));

    final File inFile = new File(inFilename);

    final TestDataGenerator tdg = new TestDataGenerator();
    tdg.createTestFile(inFile, propertyNames, testFileSize);

    log("Created testfile (location: %s, size: %d bytes)", inFile.getAbsolutePath(), testFileSize);

    final List<TestRunResult> testResults = new ArrayList<>();
    for (PerfTest test : tests) {
      final StopWatch watch = new StopWatch();

      try {
        System.gc();
        System.gc();
        Thread.sleep(5 * 1000);

        test.setUp();
        watch.start();
        test.run(inFile, replacements);
        watch.stop();
        test.tearDown();

        final double durationMillis = watch.deltaMillis();

        testResults.add(new TestRunResult(format("%s (file-size: %d bytes, properties: %d)", test.getName(),
            testFileSize, replacements.size()), durationMillis));
        log("%s: %.2fms", test.getName(), watch.deltaMillis());
      } catch (OutOfMemoryError oom) {
        testResults.add(new TestRunResult(format("%s (file-size: %d bytes, properties: %d) -> %s", test.getName(),
            testFileSize, replacements.size(), oom.getMessage()), -1));
        log("%s: %s", test.getName(), oom.getMessage());
      }

    }

    inFile.delete();
    log("Done.");

    return testResults;
  }

  protected void log(String msg, Object... args) {
    if (!logEnabled)
      return;

    if (args.length > 0)
      System.out.println(format(msg, args));
    else
      System.out.println(msg);
  }

  /**
   * 
   * @author Achim Wiedemann, Jan 27, 2014
   * 
   */
  public static interface PerfTest {
    public void setUp() throws Exception;

    public void run(File testFile, Properties replacements) throws Exception;

    public void tearDown() throws Exception;

    public String getName();
  }

  /**
   * Loads the entire input-file into memory, replaces the properties, and writes the whole file back to disk.
   * 
   * @author Achim Wiedemann, Jan 27, 2014
   * 
   */
  public static class ReplaceInMemoryTest implements PerfTest {
    private File outFile;

    @Override
    public void run(File testFile, Properties replacements) throws IOException {
      final StringBuilder sb = new StringBuilder();
      try (final BufferedReader r = new BufferedReader(new FileReader(testFile))) {
        final char[] buff = new char[4096];
        int len;
        while ((len = r.read(buff)) != -1)
          sb.append(buff, 0, len);
      }

      String str = sb.toString();
      for (final Entry<Object, Object> replacement : replacements.entrySet())
        str = str.replace(replacement.getKey().toString(), replacement.getValue().toString());

      try (final FileWriter writer = new FileWriter(outFile)) {
        writer.write(str, 0, str.length());
      }
    }

    @Override
    public String getName() {
      return getClass().getSimpleName();
    }

    @Override
    public void setUp() throws Exception {
      outFile = File.createTempFile(getName(), "txt");
    }

    @Override
    public void tearDown() throws Exception {
      if (outFile != null)
        outFile.delete();
    }
  }

  /**
   * Streams the file and replaces properties "on the fly" before writing to disk.
   * 
   * @author Achim Wiedemann, Jan 27, 2014
   * 
   */
  public static class ReplaceWithPropertiesFilterReader implements PerfTest {
    private File outFile;

    @Override
    public void run(File testFile, Properties replacements) throws IOException {
      final FileWriter writer = new FileWriter(outFile);
      try {
        try (final PropertiesFilterReader propsReader = new PropertiesFilterReader(new FileReader(testFile),
            replacements)) {
          final char[] buff = new char[4096];
          int len;
          while ((len = propsReader.read(buff)) != -1)
            writer.write(buff, 0, len);
        }
      } finally {
        writer.close();
      }
    }

    @Override
    public String getName() {
      return getClass().getSimpleName();
    }

    @Override
    public void setUp() throws IOException {
      outFile = File.createTempFile(getName(), "txt");
    }

    @Override
    public void tearDown() {
      if (outFile != null)
        outFile.delete();
    }
  }

  /**
   * 
   * @author Achim Wiedemann, Jan 27, 2014
   * 
   */
  public static class TestRunResult {
    public final String name;
    public final double durationMillis;

    public TestRunResult(String name, double durationMillis) {
      this.name = name;
      this.durationMillis = durationMillis;
    }

    @Override
    public String toString() {
      return String.format("Test: %s, duration: %.2fms", name, durationMillis);
    }
  }
}
