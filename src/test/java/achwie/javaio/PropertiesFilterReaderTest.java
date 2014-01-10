package achwie.javaio;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;
import java.util.Random;

import org.junit.Test;

/**
 * 
 * @author Achim Wiedemann, Oct 15, 2013
 * 
 */
public class PropertiesFilterReaderTest {
  @Test
  public void test_replaceSingleProperty() throws IOException {
    final String expected = "Hello World!";
    final String input = "Hello ${name}!";
    final Properties props = new Properties();
    props.put("${name}", "World");

    final PropertiesFilterReader fr = new PropertiesFilterReader(new StringReader(input), props);

    final String actual = readToString(fr);

    assertEquals(expected, actual);
  }

  @Test
  public void test_emptyString() throws IOException {
    final String expected = "";
    final String input = "";
    final Properties props = new Properties();
    props.put("${name}", "World");

    final PropertiesFilterReader fr = new PropertiesFilterReader(new StringReader(input), props);

    final String actual = readToString(fr);

    assertEquals(expected, actual);
  }

  @Test
  public void test_singleCharString() throws IOException {
    final String expected = "a";
    final String input = "a";
    final Properties props = new Properties();
    props.put("${name}", "World");

    final PropertiesFilterReader fr = new PropertiesFilterReader(new StringReader(input), props);

    final String actual = readToString(fr);

    assertEquals(expected, actual);
  }

  // @Test
  public void test_longString() throws IOException {
    final String part1 = createNonsense(1 * 1024);
    final String part2 = createNonsense(2 * 1024);
    final String part3 = createNonsense(3 * 1024);
    final String part4 = createNonsense(4 * 1024);
    final String expected = new StringBuilder().append(part1).append("John").append(part2).append("4242").append(part3)
        .append("Doe").append(part4).toString();
    final String input = new StringBuilder().append(part1).append("${fname}").append(part2).append("${age}${age}")
        .append(part3).append("${lname}").append(part4).toString();

    final Properties props = new Properties();
    props.put("${fname}", "John");
    props.put("${lname}", "Doe");
    props.put("${age}", "42");

    final PropertiesFilterReader fr = new PropertiesFilterReader(new StringReader(input), props);

    final String actual = readToString(fr);

    assertEquals(expected, actual);
  }

  // This test was added after I encountered an ArrayIndexOutOfBoundsException
  // in the CharRingBuffer while writing the performance tests.
  @Test
  public void test_bufferContentIsOneSmallerThanMaxBufferSize() throws IOException {
    final String expected = "replacement0 in a certain string.";
    final String input = "${property0} in a certain string.";
    final Properties props = new Properties();
    props.put("${property0}", "replacement0");

    final PropertiesFilterReader fr = new PropertiesFilterReader(new StringReader(input), props);

    final String actual = readToString(fr);

    assertEquals(expected, actual);
  }

  // -- End of Tests -----------------------------------------------------------
  private String createNonsense(int length) {
    final Random rand = new Random();
    final char charOffs = 'A';
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++)
      sb.append((char) (charOffs + rand.nextInt('z' - 'A')));

    return sb.toString();
  }

  private String readToString(Reader reader) throws IOException {
    try (Reader r = reader) {
      final StringBuilder sb = new StringBuilder();
      final char[] buff = new char[4096];
      int len;
      while ((len = r.read(buff)) != -1)
        sb.append(buff, 0, len);

      return sb.toString();
    }
  }
}
