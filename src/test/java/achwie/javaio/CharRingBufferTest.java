package achwie.javaio;

import static java.lang.String.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 * @author Achim Wiedemann, Oct 21, 2013
 * 
 */
public class CharRingBufferTest {
  @Test
  public void test_append_withString_noOverflow() {
    CharRingBuffer buffer = new CharRingBuffer(256);
    buffer.append("John Doe");

    assertEquals("John Doe", buffer.toString());
  }

  @Test
  public void test_append_withString_singleOverflow() {
    CharRingBuffer buffer = new CharRingBuffer(5);
    buffer.append("John Doe");

    assertEquals("n Doe", buffer.toString());
  }

  @Test
  public void test_append__withCharArray() {
    CharRingBuffer buffer = new CharRingBuffer(5);
    buffer.append("John");

    buffer.append(new char[] { ' ', 'D', 'o', 'e' });

    assertEquals("n Doe", buffer.toString());
  }

  @Test
  public void test_length_noOverflow() {
    CharRingBuffer buffer = new CharRingBuffer(8);
    buffer.append("John Doe");

    assertEquals(8, buffer.length());
  }

  @Test
  public void test_length_withOverflow() {
    CharRingBuffer buffer = new CharRingBuffer(5);
    buffer.append("John Doe");

    assertEquals(5, buffer.length());
  }

  @Test
  public void test_charAt() {
    CharRingBuffer buffer = new CharRingBuffer(5);
    buffer.append("John Doe");

    assertEquals('n', buffer.charAt(0));
  }

  @Test
  public void test_take_noOverflow() {
    CharRingBuffer buffer = new CharRingBuffer(8);
    buffer.append("John Doe");

    assertEquals('J', buffer.take());
    assertEquals(7, buffer.length());
  }

  @Test
  public void test_take_withOverflow() {
    CharRingBuffer buffer = new CharRingBuffer(5);
    buffer.append("John Doe");

    assertEquals('n', buffer.take());
    assertEquals(4, buffer.length());
  }

  @Test
  public void test_toString() {
    CharRingBuffer buffer = new CharRingBuffer(7);
    buffer.append("${name}");

    for (int i = 0; i < 7; i++) {
      final String expected = "${name}".substring(i);
      assertEquals(format("Iteration #%d", i), expected, buffer.toString());
      buffer.take();
    }
  }

  @Test
  public void test_take_untilEmpty() {
    CharRingBuffer buffer = new CharRingBuffer(2);
    buffer.append("Do");

    assertEquals('D', buffer.take());
    assertEquals('o', buffer.take());
    assertEquals(-1, buffer.take());
    assertEquals(-1, buffer.take());
  }
}
