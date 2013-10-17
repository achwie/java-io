package achwie.javaio;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * 
 * @author Achim Wiedemann, Oct 15, 2013
 */
public class PropertiesFilterReader extends Reader {
  private final Reader reader;
  private final Map<Object, Object> replacements;
  private SimpleReplacementBuffer buffer;
  private boolean bufferInitialized;

  public PropertiesFilterReader(Reader reader, Map<Object, Object> replacements) {
    this.reader = reader;
    this.replacements = replacements;
    this.buffer = new SimpleReplacementBuffer();
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    if (!bufferInitialized)
      initBuffer();

    for (int i = 0; i < len; i++) {
      readNext();

      for (Object token : replacements.keySet())
        buffer.replaceIfExists(token.toString(), replacements.get(token).toString());

      if (!buffer.hasMore())
        return (i != 0) ? i : -1; // Filled the buffer partially or not at all?

      cbuf[off + i] = buffer.take();
    }

    return len; // Filled the whole buffer
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }

  private void readNext() throws IOException {
    final int ch = reader.read();

    if (ch != -1)
      buffer.append((char) ch);
  }

  private void initBuffer() throws IOException {
    int maxLen = 0;
    for (Object key : replacements.keySet())
      if (key.toString().length() > maxLen)
        maxLen = key.toString().length();

    final char[] buff = new char[maxLen];
    final int charsRead = reader.read(buff);

    final String readAheadBuff = String.valueOf(buff, 0, (charsRead > 0) ? charsRead : buff.length);

    buffer.initialize(maxLen, readAheadBuff);
    bufferInitialized = true;
  }

  /**
   * 
   * @author Achim Wiedemann, Oct 16, 2013
   */
  static interface ReplacementBuffer {
    public void initialize(int bufferSize, String readAhead);

    public void append(char ch);

    public boolean hasMore();

    public char take();

    public void replaceIfExists(String token, String replacement);
  }

  /**
   * 
   * @author Achim Wiedemann, Oct 16, 2013
   */
  static class SimpleReplacementBuffer implements ReplacementBuffer {
    private String readAheadBuff = null;

    public void initialize(int bufferSize, String readAhead) {
      this.readAheadBuff = readAhead;
    }

    public void append(char ch) {
      readAheadBuff += ch;
    }

    public boolean hasMore() {
      return readAheadBuff.length() > 0;
    }

    public char take() {
      final char firstChar = readAheadBuff.charAt(0);
      readAheadBuff = readAheadBuff.substring(1);
      return firstChar;
    }

    public void replaceIfExists(String token, String replacement) {
      if (readAheadBuff.startsWith(token))
        readAheadBuff = replacement + readAheadBuff.substring(token.length());
    }
  }
}
