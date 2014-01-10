package achwie.javaio;

import static java.lang.String.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;

/**
 * 
 * @author Achim Wiedemann, Dec 12, 2013
 * 
 */
public class TestDataGenerator {
  private static final char[] WORD_CHARS = new char[90];
  private static final Random RAND = new Random();

  static {
    for (int i = 0; i < WORD_CHARS.length; i++)
      WORD_CHARS[i] = (char) (32 + i);
  }

  public void createTestFile(File f, List<String> propertyNames, int fileSize) throws FileNotFoundException,
      IOException {
    int propertyNamesLenTotal = 0;
    for (String propName : propertyNames)
      propertyNamesLenTotal += propName.length();

    final int distance = (int) Math.floor((fileSize - propertyNamesLenTotal) / propertyNames.size());

    final int propCount = propertyNames.size();
    int propNumber = 0;
    try (Writer fos = new FileWriter(f)) {
      for (int i = 0; i < fileSize;) {
        if (i % distance == 0 && propNumber < propCount) {
          final String pname = propertyNames.get(propNumber++);
          fos.write(pname);
          i += pname.length();
        } else {
          fos.write(randomWordChar());
          i++;
        }
      }
    }
  }

  public void replaceInMemory(File inFile, File outFile, Properties replacements) throws IOException {
    final StopWatch watch = new StopWatch();
    watch.start();

    final StringBuilder sb = new StringBuilder();
    try (final BufferedReader r = new BufferedReader(new FileReader(inFile))) {
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
    watch.stop();

    System.out.println(format("In memory replacement: %.2fms", watch.deltaMillis()));

    outFile.deleteOnExit();
  }

  public void replaceWithPropertiesFilterReader(File inFile, File outFile, Properties replacements) throws IOException {
    final StopWatch watch = new StopWatch();
    watch.start();

    final FileWriter writer = new FileWriter(outFile);
    try {
      try (final PropertiesFilterReader propsReader = new PropertiesFilterReader(new FileReader(inFile), replacements)) {
        final char[] buff = new char[4096];
        int len;
        while ((len = propsReader.read(buff)) != -1)
          writer.write(buff, 0, len);
      }
    } finally {
      writer.close();
    }
    watch.stop();

    System.out.println(format("PropertiesFilterReader replacement: %.2fms", watch.deltaMillis()));

    outFile.deleteOnExit();
  }

  private char randomWordChar() {
    return WORD_CHARS[RAND.nextInt(WORD_CHARS.length)];
  }

  public static void main(String[] args) throws FileNotFoundException, IOException {
    final int numProperties = 10;
    final int testFileSize = 1 * 1024 * 1024; // 1MB
    final String inFilename = "test-in.txt";
    final String outFilenamePropertiesFilterReader = "test-out-pfr.txt";
    final String outFilenameInMemory = "test-out-mem.txt";

    final TestDataGenerator tdg = new TestDataGenerator();

    final List<String> propertyNames = new ArrayList<>();
    for (int i = 0; i < numProperties; i++)
      propertyNames.add(String.format("${property%d}", i));

    final Properties replacements = new Properties();
    for (int i = 0; i < propertyNames.size(); i++)
      replacements.put(propertyNames.get(i), String.format("replacement%d", i));

    final File inFile = new File(inFilename);
    tdg.createTestFile(inFile, propertyNames, testFileSize);

    System.out.println(String.format("Created testfile (%d bytes of data)", testFileSize));

    tdg.replaceWithPropertiesFilterReader(inFile, new File(outFilenamePropertiesFilterReader), replacements);
    tdg.replaceInMemory(inFile, new File(outFilenameInMemory), replacements);

    inFile.delete();
    System.out.println("Done.");
  }
}
