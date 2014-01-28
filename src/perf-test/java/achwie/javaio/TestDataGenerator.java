package achwie.javaio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
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

  private char randomWordChar() {
    return WORD_CHARS[RAND.nextInt(WORD_CHARS.length)];
  }

}
