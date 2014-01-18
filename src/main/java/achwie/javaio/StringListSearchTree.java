package achwie.javaio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;

/**
 * <p>
 * Takes a list of strings and builds a search tree by grouping common sections into branches. So the list of strings:
 * </p>
 * <ul>
 * <li>This is great</li>
 * <li>This is exciting</li>
 * <li>This was awesome</li>
 * <li>This was wonderful</li>
 * </ul>
 * <p>
 * Would form the tree:
 * </p>
 * <ul>
 * <li><code>"This "</code>
 * <ul>
 * <li><code>"is "</code>
 * <ul>
 * <li><code>"great"</code></li>
 * <li><code>"exciting"</code></li>
 * </ul>
 * </li>
 * <li><code>"was "</code>
 * <ul>
 * <li><code>"awesome"</code></li>
 * <li><code>"wonderful"</code></li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * This can e.g. be used to efficiently find out whether a string begins with any of the strings in the list (and some
 * of them start with the same substring).
 * </p>
 * 
 * 
 * @author Achim Wiedemann, Jan 9, 2014
 */
public class StringListSearchTree {
  private final Node root = new Node();

  public StringListSearchTree(List<String> sortedListOfStrings) {
    buildSearchTree(root, sortedListOfStrings, 0);
    // TODO: optimize tree to keep it as shallow as possible
  }

  /**
   * Returns the string which forms the start of the given string. For example, given the list {@code ["This",
   * "That", "There"]}, when passing the string {@code"This is a great day"}, the method would return {@code "This"} .
   * 
   * @param str The string to check whether it starts with one of the string in the list.
   * @return The string which forms the start of the given string or {@code null}, if none of the strings in the list
   *         match.
   */
  public Object startOf(String str) {
    if (!str.startsWith(root.value))
      return null;

    Node n = root;
    final StringBuilder sb = new StringBuilder();
    sb.append(n.value);

    while (!n.isEnd()) {
      for (Node child : n.children) {
        if (str.startsWith(child.value, sb.length())) {
          sb.append(child.value);
          n = child;
          break;
        }
      }
    }

    return sb.toString();
  }

  private void buildSearchTree(Node root, List<String> sortedKeys, int offset) {
    if (sortedKeys.isEmpty())
      return;

    String key;
    Node child;
    List<String> matching;
    char ch;

    for (int i = 0; i < sortedKeys.size(); i++) {
      // Search for first string in sorted list that is still long enough
      key = sortedKeys.get(i);
      if (key.length() <= offset)
        continue;

      // Always create first child node
      ch = key.charAt(offset);
      child = new Node(ch);
      root.add(child);
      matching = new ArrayList<>();
      matching.add(key);

      // Group matching strings in branches
      for (int j = i + 1; j < sortedKeys.size(); j++) {
        key = sortedKeys.get(j);

        // Branch off to form a new group
        if (key.charAt(offset) != ch) {
          buildSearchTree(child, matching, offset + 1);
          ch = key.charAt(offset);
          child = new Node(ch);
          root.add(child);
          matching = new ArrayList<>();
        }
        matching.add(key);
      }

      buildSearchTree(child, matching, offset + 1);
      break;
    }

    // Flatten tree (merge single children upwards)
    if (root.children.size() == 1) {
      Node onlyChild = root.children.get(0);
      root.children = onlyChild.children;
      root.value += onlyChild.value;
    }
  }

  @Override
  public String toString() {
    try {
      final StringBuilder sb = new StringBuilder();

      root.printTree(sb);

      return sb.toString();
    } catch (IOException e) {
      throw new RuntimeException("Could not print tree", e);
    }
  }

  public void printTree() {
    try {
      root.printTree(System.out);
    } catch (IOException e) {
      throw new RuntimeException("Could not print tree", e);
    }
  }

  /**
   * 
   * @author Achim Wiedemann, Jan 9, 2014
   */
  private static class Node {
    private static final String NL = System.getProperty("line.separator");
    private String value;
    private List<Node> children = new ArrayList<>();

    public Node() {
      this("");
    }

    public Node(char ch) {
      this.value = String.valueOf(ch);
    }

    public Node(String str) {
      this.value = str;
    }

    public void add(Node node) {
      children.add(node);
    }

    public boolean isEnd() {
      return children.size() == 0;
    }

    @Override
    public String toString() {
      return String.format("Node[value: <%s>, #children: %d]", value, children.size());
    }

    @Ignore("unused")
    public void printTree(Appendable out) throws IOException {
      printTree(this, 0, out);
    }

    private void printTree(Node n, int level, Appendable out) throws IOException {
      for (int i = 0; i < level; i++)
        out.append(" ");

      out.append(n + NL);

      for (Node child : n.children)
        printTree(child, level + 2, out);
    }
  }
}
