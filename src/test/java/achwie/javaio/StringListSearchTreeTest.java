package achwie.javaio;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * 
 * @author Achim Wiedemann, Jan 9, 2014
 * 
 */
public class StringListSearchTreeTest {
  @Test
  public void test_entriesStartingWith() {
    final String str = "key1Search";
    final List<String> strings = new ArrayList<>();
    strings.add("key1");
    strings.add("key2");

    final Object actual = new StringListSearchTree(strings).startOf(str);

    assertEquals("key1", actual.toString());
  }

  @Test
  public void test_treeIsFlattened() {
    final List<String> strings = new ArrayList<>();
    strings.add("key1");
    strings.add("key2");

    final String tree = new StringListSearchTree(strings).toString();

    assertTrue(tree.startsWith("Node[value: <key>, #children: 2]"));
  }
}
