package achwie.javaio;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * 
 * @author Achim Wiedemann, Oct 15, 2013
 * 
 */
public class PropertiesFilterReader extends Reader {
  private final Reader reader;
  private final Map<Object, Object> replacements;

  public PropertiesFilterReader(Reader reader, Map<Object, Object> replacements) {
    this.reader = reader;
    this.replacements = replacements;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    // TODO Auto-generated method stub
    return -1;
  }

  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub

  }
}
