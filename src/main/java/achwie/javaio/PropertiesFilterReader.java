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
  private int maxKeyLen;
  private String readAheadBuff = null;

  public PropertiesFilterReader(Reader reader, Map<Object, Object> replacements) {
    this.reader = reader;
    this.replacements = replacements;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    for (int i = 0; i < len; i++) {
      if (readAheadBuff == null)
        initBuffer();
      else
        readNext();

      String token;
      if ((token = bufferStartsWithToken()) != null)
        replaceTokenInBuffer(token);

      int ch = shiftFromBuffer();
      if (ch == -1)
        return (i != 0) ? i : -1; // Filled the buffer partially or not at all?

      cbuf[off + i] = (char) ch;
    }

    return len; // Filled the whole buffer
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }

  private void replaceTokenInBuffer(String token) {
    readAheadBuff = replacements.get(token).toString() + readAheadBuff.substring(token.length());
  }

  private String bufferStartsWithToken() {
    for (Object token : replacements.keySet())
      if (readAheadBuff.startsWith(token.toString()))
        return token.toString();

    return null;
  }

  private void readNext() throws IOException {
    final int ch = reader.read();

    if (ch != -1)
      readAheadBuff += (char) ch;
  }

  private int shiftFromBuffer() {
    if (readAheadBuff.length() == 0)
      return -1;

    final char firstChar = readAheadBuff.charAt(0);
    readAheadBuff = readAheadBuff.substring(1);
    return firstChar;
  }

  private void initBuffer() throws IOException {
    int maxLen = 0;
    for (Object key : replacements.keySet())
      if (key.toString().length() > maxLen)
        maxLen = key.toString().length();

    this.maxKeyLen = maxLen;

    final char[] buff = new char[maxLen];
    final int charsRead = reader.read(buff);

    readAheadBuff = String.valueOf(buff, 0, (charsRead > 0) ? charsRead : buff.length);
  }
}
