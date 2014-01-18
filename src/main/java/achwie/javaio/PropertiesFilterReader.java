package achwie.javaio;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Achim Wiedemann, Oct 15, 2013
 */
public class PropertiesFilterReader extends Reader {
  private final Reader reader;
  private final Map<Object, Object> replacements;
  private final StringListSearchTree searchMap;
  private ReplacementBuffer buffer;

  public PropertiesFilterReader(Reader reader, Map<Object, Object> replacements) {
    this.reader = reader;
    this.replacements = replacements;
    this.buffer = createBuffer();
    this.searchMap = new StringListSearchTree(keysAsSortedListOfStrings(replacements));

    initBuffer();
  }

  private List<String> keysAsSortedListOfStrings(Map<Object, Object> map) {
    List<String> sortedStrings = new ArrayList<>(map.size());
    for (Object key : map.keySet())
      sortedStrings.add(key.toString());

    Collections.sort(sortedStrings);

    return sortedStrings;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    for (int i = 0; i < len; i++) {
      buffer.readAhead(reader);

      final Object key = searchMap.startOf(buffer.toString());
      if (key != null)
        buffer.replaceIfExists(key.toString(), replacements.get(key).toString());

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

  protected ReplacementBuffer createBuffer() {
    return new RingReplacementBuffer();
  }

  private void initBuffer() {
    int maxLen = 0;
    for (Object key : replacements.keySet())
      if (key.toString().length() > maxLen)
        maxLen = key.toString().length();

    buffer.initialize(maxLen);
  }

  /**
   * 
   * @author Achim Wiedemann, Oct 16, 2013
   */
  static interface ReplacementBuffer {
    public void initialize(int bufferSize);

    public void readAhead(Reader reader) throws IOException;

    public boolean hasMore();

    public char take();

    public void replaceIfExists(String token, String replacement);

    public String toString();
  }

  /**
   * 
   * @author Achim Wiedemann, Oct 16, 2013
   */
  static class SimpleReplacementBuffer implements ReplacementBuffer {
    private String readAheadBuff = "";
    private int bufferSize;

    @Override
    public void initialize(int bufferSize) {
      this.bufferSize = bufferSize;
    }

    @Override
    public void readAhead(Reader reader) throws IOException {
      final int readAheadSize = bufferSize - readAheadBuff.length();

      if (readAheadSize < 1)
        return;

      final char[] buff = new char[readAheadSize];
      final int charsRead = reader.read(buff);

      if (charsRead == -1)
        return;

      this.readAheadBuff += String.valueOf(buff, 0, charsRead);
    }

    @Override
    public boolean hasMore() {
      return readAheadBuff.length() > 0;
    }

    @Override
    public char take() {
      final char firstChar = readAheadBuff.charAt(0);
      readAheadBuff = readAheadBuff.substring(1);
      return firstChar;
    }

    @Override
    public void replaceIfExists(String token, String replacement) {
      if (readAheadBuff.startsWith(token))
        readAheadBuff = replacement + readAheadBuff.substring(token.length());
    }

    @Override
    public String toString() {
      return readAheadBuff;
    }
  }

  /**
   * 
   * @author Achim Wiedemann, Oct 21, 2013
   */
  static class RingReplacementBuffer implements ReplacementBuffer {
    private String replacement;
    private int replacementPos;
    private CharRingBuffer buffer;

    @Override
    public void initialize(int bufferSize) {
      buffer = new CharRingBuffer(bufferSize);
    }

    @Override
    public void readAhead(Reader reader) throws IOException {
      if (inReplacement())
        return;

      final int readAheadSize = buffer.maxSize() - buffer.length();

      if (readAheadSize < 1)
        return;

      final char[] buff = new char[readAheadSize];
      final int charsRead = reader.read(buff);

      if (charsRead == -1)
        return;

      buffer.append(buff, 0, charsRead);
    }

    @Override
    public boolean hasMore() {
      if (inReplacement())
        return replacement.length() - replacementPos > 0;
      else
        return buffer.length() > 0;
    }

    @Override
    public char take() {
      if (inReplacement()) {
        final char ch = replacement.charAt(replacementPos++);
        if (replacement.length() == replacementPos) {
          replacement = null;
          replacementPos = 0;
        }
        return ch;
      } else {
        return (char) buffer.take();
      }
    }

    @Override
    public void replaceIfExists(String token, String replacement) {
      if (!buffer.toString().startsWith(token))
        return;

      // Remove token from buffer
      // TODO: Allow to take multiple chars at once
      for (int i = 0; i < token.length(); i++)
        buffer.take();

      // Populate replacement buffer
      this.replacement = replacement;
      this.replacementPos = 0;
    }

    private boolean inReplacement() {
      return replacement != null;
    }

    @Override
    public String toString() {
      return buffer.toString();
    }
  }
}
