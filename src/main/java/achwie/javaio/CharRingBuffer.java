package achwie.javaio;

import static java.lang.String.*;

/**
 * 
 * @author Achim Wiedemann, Oct 21, 2013
 * 
 */
class CharRingBuffer implements CharSequence {
  private final int maxSize;
  private final char[] buffer;
  private int pos;
  private int length;

  public CharRingBuffer(int maxSize) {
    this.maxSize = maxSize;
    this.buffer = new char[maxSize];
  }

  public int maxSize() {
    return maxSize;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public char charAt(int index) {
    if (index < 0 || index > length)
      throw new IndexOutOfBoundsException(
          format("Could not access char at %d with a buffer size of %d.", index, length));

    return buffer[denormalize(index)];
  }

  private int denormalize(int index) {
    return (pos + index) % maxSize;
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    throw new UnsupportedOperationException();
  }

  public void append(char ch) {
    buffer[denormalize(length)] = ch;

    // Once the buffer is full, it just wraps around
    if (length < maxSize)
      length++;
    else
      incPos(1);
  }

  public void append(char[] chars) {
    for (char ch : chars)
      append(ch);
  }

  public void append(char[] chars, int pos, int length) {
    for (int i = 0; i < length; i++)
      append(chars[pos + i]);
  }

  public void append(String str) {
    append(str.toCharArray());
  }

  public char[] toCharArray() {
    final char[] target = new char[length];

    // Copy portion to end of buffer
    final int copyToEnd = Math.min(maxSize - pos, length);
    System.arraycopy(buffer, pos, target, 0, copyToEnd);

    // Handle wrap around
    if (copyToEnd < length)
      System.arraycopy(buffer, 0, target, copyToEnd, length - copyToEnd);

    return target;
  }

  public String toString() {
    return String.valueOf(toCharArray());
  }

  public int take() {
    if (length == 0)
      return -1;

    final char firstChar = buffer[pos];
    incPos(1);
    length--;

    return firstChar;
  }

  private void incPos(int increment) {
    pos = (pos + increment) % maxSize;
  }
}
