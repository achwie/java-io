package achwie.javaio;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

import org.junit.Test;

/**
 * 
 * @author Achim Wiedemann, Oct 15, 2013
 * 
 */
public class FilteringReaderTest {
  @Test
  public void _() throws IOException {
    final String expected = "Hello World!";
    final Properties props = new Properties();
    props.put("name", "World");

    final PropertiesFilterReader fr = new PropertiesFilterReader(new StringReader("Hello ${name}!"), props);

    final String actual = readToString(fr);

    assertEquals(actual, expected);
  }

  // -- End of Tests -----------------------------------------------------------
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
